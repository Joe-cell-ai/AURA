package org.aura.assistant.extensions

import android.content.Context

data class CatalogExtension(
    val manifest: ExtensionManifest,
    val description: String
)

object ExtensionCatalog {
    val entries = listOf(
        CatalogExtension(
            ExtensionManifest("aura.web-search", "Web search", "1.0.0", "AURA", "AGPL-3.0-only", setOf("web_search")),
            "Search through your configured SearXNG endpoint."
        ),
        CatalogExtension(
            ExtensionManifest("aura.home-assistant", "Home Assistant", "1.0.0", "AURA", "AGPL-3.0-only", setOf("home_assistant")),
            "Control a configured Home Assistant entity after confirmation."
        ),
        CatalogExtension(
            ExtensionManifest("aura.reminders", "Scheduled reminders", "1.0.0", "AURA", "AGPL-3.0-only", setOf("reminders")),
            "Schedule local notifications on this device."
        ),
        CatalogExtension(
            ExtensionManifest("aura.app-launch", "App launcher", "1.0.0", "AURA", "AGPL-3.0-only", setOf("app_launch")),
            "Open an installed app after you tap the action."
        )
    )
}

class ExtensionGrantStore(context: Context) {
    private val prefs = context.getSharedPreferences("aura_extension_grants", Context.MODE_PRIVATE)

    fun isGranted(id: String): Boolean = prefs.getBoolean(id, false)
    fun setGranted(id: String, granted: Boolean) {
        require(ExtensionCatalog.entries.any { it.manifest.id == id }) { "Unknown extension." }
        prefs.edit().putBoolean(id, granted).apply()
    }
}
