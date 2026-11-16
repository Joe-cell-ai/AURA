package org.aura.assistant.smarthome

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import org.json.JSONArray


data class HomeAssistantEntity(val entityId: String, val friendlyName: String, val state: String)

class HomeAssistantClient {
    private val client = OkHttpClient()

    suspend fun listEntities(baseUrl: String, token: String): List<HomeAssistantEntity> =
        withContext(Dispatchers.IO) {
            require(baseUrl.startsWith("https://")) { "Home Assistant must use HTTPS." }
            require(token.isNotBlank()) { "Configure the Home Assistant token first." }
            val request = Request.Builder()
                .url(baseUrl.trimEnd('/') + "/api/states")
                .header("Authorization", "Bearer $token")
                .get()
                .build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) error("Home Assistant returned HTTP " + response.code)
                val array = JSONArray(response.body?.string() ?: "[]")
                val allowed = setOf("light", "switch", "fan", "climate", "scene", "script")
                (0 until array.length()).mapNotNull { index ->
                    val item = array.optJSONObject(index) ?: return@mapNotNull null
                    val id = item.optString("entity_id")
                    if (id.substringBefore(".") !in allowed || !id.contains(".")) return@mapNotNull null
                    val attrs = item.optJSONObject("attributes") ?: JSONObject()
                    HomeAssistantEntity(id, attrs.optString("friendly_name", id).take(100), item.optString("state").take(80))
                }.sortedBy { it.friendlyName.lowercase() }.take(200)
            }
        }

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
