package org.aura.assistant.ai

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/** Minimal OpenAI-compatible streaming client. The caller supplies the user's own endpoint and credential. */
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
        require(endpoint.startsWith("https://")) { "Use an HTTPS AI endpoint." }
        require(model.isNotBlank()) { "Enter a model name in Settings." }
        val payload = JSONObject().apply {
            put("model", model)
            put("stream", true)
            put("max_tokens", maxTokens.coerceIn(64, 2048))
            put("messages", JSONArray().also { array ->
                val selected = messages.takeLast(12)
                selected.forEachIndexed { index, m ->
                    val isLastUser = index == selected.lastIndex && m.role == "user"
                    val content: Any = if (isLastUser && (imageDataUrl != null || !documentText.isNullOrBlank())) {
                        JSONArray().apply {
                            put(JSONObject().put("type", "text").put("text", buildString {
                                append(m.content.take(12000))
                                if (!documentText.isNullOrBlank()) {
                                    append("\n\nDocument content (user-selected):\n")
                                    append(documentText.take(32000))
                                }
                            }))
                            if (imageDataUrl != null) {
                                put(JSONObject().put("type", "image_url").put("image_url", JSONObject().put("url", imageDataUrl)))
                            }
                        }
                    } else m.content.take(12000)
                    array.put(JSONObject().put("role", m.role).put("content", content))
                }
            })
        }
        val builder = Request.Builder().url(endpoint)
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
                if (data == "[DONE]") break
                try {
                    val delta = JSONObject(data).optJSONArray("choices")
                        ?.optJSONObject(0)?.optJSONObject("delta")
                        ?.optString("content").orEmpty()
                    if (delta.isNotEmpty() && delta != "null") {
                        complete.append(delta)
                        withContext(Dispatchers.Main) { onDelta(delta) }
                    }
                } catch (_: Exception) {
                    // Ignore non-JSON SSE keepalive/comment frames.
                }
            }
            complete.toString()
        }
    }
}
