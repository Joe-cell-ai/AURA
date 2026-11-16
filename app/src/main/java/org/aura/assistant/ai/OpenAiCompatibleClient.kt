package org.aura.assistant.ai

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object AiEndpointValidator {
    fun validate(value: String): String {
        val url = value.trim().toHttpUrlOrNull()
            ?: throw IllegalArgumentException("Enter a valid AI endpoint URL.")
        require(url.isHttps && url.host.isNotBlank() && url.username.isEmpty() && url.password.isEmpty() && url.fragment == null) {
            "AI endpoint must use HTTPS and must not contain embedded credentials or a fragment."
        }
        return url.toString()
    }
}

/** OpenAI-compatible SSE client. The caller supplies the user's own endpoint and credential. */
class OpenAiCompatibleClient {
    private val http = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .callTimeout(130, TimeUnit.SECONDS)
        .build()

    suspend fun stream(
        endpoint: String,
        model: String,
        apiKey: String,
        messages: List<ChatMessage>,
        maxTokens: Int,
        imageDataUrl: String? = null,
        documentText: String? = null,
        onDelta: suspend (String) -> Unit
    ): String = withContext(Dispatchers.IO) {
        val safeEndpoint = AiEndpointValidator.validate(endpoint)
        require(model.isNotBlank()) { "Enter a model name in Settings." }
        require(messages.isNotEmpty()) { "Send a message to start a conversation." }
        require(messages.all { it.role in setOf("system", "user", "assistant") }) { "Unsupported chat message role." }

        // Keep system instructions even when the conversation history reaches its limit.
        val systemMessages = messages.filter { it.role == "system" }.take(2)
        val recentMessages = messages.filterNot { it.role == "system" }.takeLast(10)
        val selected = systemMessages + recentMessages
        val payload = JSONObject().apply {
            put("model", model.trim())
            put("stream", true)
            put("max_tokens", maxTokens.coerceIn(64, 2048))
            put("messages", JSONArray().also { array ->
                selected.forEachIndexed { index, message ->
                    val isLastUser = index == selected.lastIndex && message.role == "user"
                    val content: Any = if (isLastUser && (imageDataUrl != null || !documentText.isNullOrBlank())) {
                        JSONArray().apply {
                            put(JSONObject().put("type", "text").put("text", buildString {
                                append(message.content.take(12000))
                                if (!documentText.isNullOrBlank()) {
                                    append("\n\nDocument content (user-selected):\n")
                                    append(documentText.take(32000))
                                }
                            }))
                            if (imageDataUrl != null) {
                                require(imageDataUrl.startsWith("data:image/")) { "Invalid image attachment." }
                                put(JSONObject().put("type", "image_url").put("image_url", JSONObject().put("url", imageDataUrl)))
                            }
                        }
                    } else message.content.take(12000)
                    array.put(JSONObject().put("role", message.role).put("content", content))
                }
            })
        }
        val builder = Request.Builder().url(safeEndpoint)
            .post(payload.toString().toRequestBody("application/json".toMediaType()))
            .header("Accept", "text/event-stream")
        if (apiKey.isNotBlank()) builder.header("Authorization", "Bearer $apiKey")

        http.newCall(builder.build()).execute().use { response ->
            if (!response.isSuccessful) {
                val detail = response.body?.string()?.take(500).orEmpty()
                throw IllegalStateException("AI endpoint returned HTTP ${response.code}: $detail")
            }
            val source = response.body?.source() ?: throw IllegalStateException("Empty AI response.")
            val complete = StringBuilder()
            while (!source.exhausted()) {
                val line = source.readUtf8Line() ?: break
                if (!line.startsWith("data:")) continue
                val data = line.removePrefix("data:").trim()
                if (data.isEmpty()) continue
                if (data == "[DONE]") break
                val event = try { JSONObject(data) } catch (_: Exception) { continue }
                if (event.has("error")) {
                    throw IllegalStateException(event.optJSONObject("error")?.optString("message")?.take(400)
                        ?.takeIf { it.isNotBlank() } ?: "AI provider reported a streaming error.")
                }
                val delta = event.optJSONArray("choices")?.optJSONObject(0)
                    ?.optJSONObject("delta")?.optString("content").orEmpty()
                if (delta.isNotEmpty() && delta != "null") {
                    complete.append(delta)
                    withContext(Dispatchers.Main) { onDelta(delta) }
                }
            }
            complete.toString()
        }
    }
}
