package org.aura.assistant.extensions

/** Declarative-only extension metadata. Downloaded executable code is deliberately unsupported. */
data class ExtensionManifest(
    val id: String,
    val name: String,
    val version: String,
    val publisher: String,
    val license: String,
    val capabilities: Set<String>
)

object ExtensionManifestValidator {
    private val allowedCapabilities = setOf("web_search", "home_assistant", "reminders", "app_launch")
    fun validate(manifest: ExtensionManifest): Boolean =
        manifest.id.matches(Regex("[a-zA-Z0-9_.-]{3,80}")) &&
            manifest.name.isNotBlank() && manifest.version.isNotBlank() &&
            manifest.publisher.isNotBlank() && manifest.license.isNotBlank() &&
            manifest.capabilities.isNotEmpty() &&
            manifest.capabilities.all { it in allowedCapabilities }
}
