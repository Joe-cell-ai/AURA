package org.aura.assistant.smarthome

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class HomeAssistantClient {
    private val client = OkHttpClient()

    suspend fun callService(baseUrl: String, token: String, domain: String, service: String, entityId: String) =
        withContext(Dispatchers.IO) {
            require(baseUrl.startsWith("https://")) { "Home Assistant must use HTTPS." }
            require(token.isNotBlank() && entityId.contains(".")) { "Configure the access token and entity ID first." }
            require(domain in setOf("light", "switch", "fan", "climate", "scene", "script")) { "Unsupported Home Assistant domain." }
            require(service in setOf("turn_on", "turn_off", "toggle")) { "Unsupported service." }
            val url = baseUrl.trimEnd('/') + "/api/services/$domain/$service"
            val body = JSONObject().put("entity_id", entityId).toString()
                .toRequestBody("application/json".toMediaType())
            val request = Request.Builder().url(url).post(body)
                .header("Authorization", "Bearer $token").build()
            client.newCall(request).execute().use {
                if (!it.isSuccessful) error("Home Assistant returned HTTP ${it.code}.")
            }
        }
}
