package org.aura.assistant.smarthome

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import org.json.JSONObject
import org.json.JSONArray


data class HomeAssistantEntity(val entityId: String, val friendlyName: String, val state: String)\n\ndata class HomeAssistantConversationResult(val text: String, val conversationId: String?)

object HomeAssistantRequestValidator {
    private val domains = setOf("light", "switch", "fan", "climate", "scene", "script")

    fun baseUrl(value: String): String {
        val url = value.trim().trimEnd('/').toHttpUrlOrNull()
            ?: throw IllegalArgumentException("Enter a valid Home Assistant URL.")
        require(url.isHttps && url.username.isEmpty() && url.password.isEmpty() &&
            url.query == null && url.fragment == null && url.encodedPath.trimEnd('/').isEmpty()) {
            "Home Assistant must be an HTTPS origin without credentials, path, query or fragment."
        }
        return url.toString().trimEnd('/')
    }

    fun conversationText(value: String): String {
        val prompt = value.trim()
        require(prompt.isNotBlank() && prompt.length <= 2000) {
            "Enter a message up to 2,000 characters."
        }
        return prompt
    }

    fun entityId(value: String): String {
        val id = value.trim()
        require(Regex("^[a-z_]+\\.[a-z0-9_]+$").matches(id) && id.substringBefore('.') in domains) {
            "Choose a supported Home Assistant entity."
        }
        return id
    }

    fun validateService(domain: String, service: String, entityId: String) {
        val id = entityId(entityId)
        require(id.substringBefore('.') == domain && domain in domains) {
            "The selected entity does not belong to this Home Assistant domain."
        }
        val supported = when (domain) {
            "light", "switch", "fan" -> setOf("turn_on", "turn_off", "toggle")
            "climate" -> setOf("turn_on", "turn_off")
            "scene", "script" -> setOf("turn_on")
            else -> emptySet()
        }
        require(service in supported) { "This service is not supported for the selected entity type." }
    }
}

class HomeAssistantClient {
    private val client = OkHttpClient()

    suspend fun listEntities(baseUrl: String, token: String): List<HomeAssistantEntity> =
        withContext(Dispatchers.IO) {
            val safeBaseUrl = HomeAssistantRequestValidator.baseUrl(baseUrl)
            require(token.isNotBlank()) { "Configure the Home Assistant token first." }
            val request = Request.Builder()
                .url(safeBaseUrl + "/api/states")
                .header("Authorization", "Bearer $token")
                .get()
                .build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) error("Home Assistant returned HTTP " + response.code)
                val array = JSONArray(response.body?.string() ?: "[]")
                val allowed = setOf("light", "switch", "fan", "climate", "scene", "script")
                (0 until array.length()).mapNotNull { index ->
                    val item = array.optJSONObject(index) ?: return@mapNotNull null
                    val id = runCatching { HomeAssistantRequestValidator.entityId(item.optString("entity_id")) }.getOrNull() ?: return@mapNotNull null
                    val attrs = item.optJSONObject("attributes") ?: JSONObject()
                    HomeAssistantEntity(id, attrs.optString("friendly_name", id).take(100), item.optString("state").take(80))
                }.sortedBy { it.friendlyName.lowercase() }.take(200)
            }
        }

    suspend fun callService(baseUrl: String, token: String, domain: String, service: String, entityId: String) =
        withContext(Dispatchers.IO) {
            require(baseUrl.startsWith("https://")) { "Home Assistant must use HTTPS." }
            require(token.isNotBlank()) { "Configure the Home Assistant access token first." }
            val safeBaseUrl = HomeAssistantRequestValidator.baseUrl(baseUrl)
            HomeAssistantRequestValidator.validateService(domain, service, entityId)
            val url = safeBaseUrl + "/api/services/$domain/$service"
            val body = JSONObject().put("entity_id", entityId).toString()
                .toRequestBody("application/json".toMediaType())
            val request = Request.Builder().url(url).post(body)
                .header("Authorization", "Bearer $token").build()
            client.newCall(request).execute().use {
                if (!it.isSuccessful) error("Home Assistant returned HTTP ${it.code}.")
            }
        }
}
