package org.aura.assistant.ai

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import org.json.JSONObject

data class WebSearchResult(val title: String, val url: String, val snippet: String)

class WebSearchClient {
    private val client = OkHttpClient()

    fun search(endpoint: String, query: String, limit: Int = 5): List<WebSearchResult> {
        require(query.trim().length in 2..300) { "Enter a search query between 2 and 300 characters." }
        val base = endpoint.trim().toHttpUrlOrNull() ?: error("Configure a valid HTTPS SearXNG search URL.")
        require(base.isHttps && base.username.isEmpty() && base.password.isEmpty() && base.fragment == null) {
            "Web search endpoint must use HTTPS and must not contain embedded credentials or a fragment."
        }
        val url = base.newBuilder()
            .addQueryParameter("q", query.trim())
            .addQueryParameter("format", "json")
            .build()
        val request = Request.Builder().url(url).header("Accept", "application/json").get().build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) error("Search provider returned HTTP " + response.code)
            val body = response.body?.string()?.take(500000) ?: error("Search provider returned no data.")
            val results = JSONObject(body).optJSONArray("results") ?: error("Search response has no results array.")
            return (0 until minOf(results.length(), limit.coerceIn(1, 8))).mapNotNull { index ->
                val item = results.optJSONObject(index) ?: return@mapNotNull null
                val title = item.optString("title").take(180)
                val link = item.optString("url")
                val snippet = item.optString("content").take(500)
                if (title.isBlank() || !isSafeResultUrl(link)) null
                else WebSearchResult(title, link, snippet)
            }
        }
    }
}

internal fun isSafeResultUrl(value: String): Boolean {
    val url = value.toHttpUrlOrNull() ?: return false
    return url.isHttps && url.host.isNotBlank() && url.username.isEmpty() &&
        url.password.isEmpty() && url.fragment == null
}

object SearchResultFormatter {
    fun format(results: List<WebSearchResult>): String =
        if (results.isEmpty()) "No search results were returned."
        else results.joinToString("\n\n") { it.title + "\n" + it.url + "\n" + it.snippet }
}
