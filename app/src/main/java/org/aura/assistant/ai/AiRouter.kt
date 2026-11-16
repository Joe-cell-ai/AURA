package org.aura.assistant.ai

enum class AiMode { OFFLINE_ONLY, ONLINE_ONLY, ASK_EACH_TIME, AUTOMATIC }

/**
 * Routes requests only to configured providers. Automatic mode tries configured providers in
 * their declared priority order; it never silently crosses the online/offline boundary in a
 * restricted mode. ASK_EACH_TIME requires an explicit provider ID from the UI.
 */
class AiRouter(private val providers: List<AiProvider>, private val mode: AiMode) {
    fun candidates(): List<AiProvider> = when (mode) {
        AiMode.OFFLINE_ONLY -> providers.filter { it.isOnDevice }
        AiMode.ONLINE_ONLY -> providers.filterNot { it.isOnDevice }
        AiMode.ASK_EACH_TIME -> emptyList()
        AiMode.AUTOMATIC -> providers
    }

    suspend fun generate(
        messages: List<ChatMessage>,
        options: GenerationOptions = GenerationOptions(),
        selectedProviderId: String? = null
    ): AiResult {
        val targets = if (mode == AiMode.ASK_EACH_TIME) {
            val selected = providers.firstOrNull { it.id == selectedProviderId }
                ?: return AiResult.Failure("Choose a configured AI provider for this request.")
            listOf(selected)
        } else candidates()

        if (targets.isEmpty()) {
            return AiResult.Failure(
                when (mode) {
                    AiMode.OFFLINE_ONLY -> "No on-device AI model is installed. Add an offline provider or change AI mode."
                    AiMode.ONLINE_ONLY -> "No online AI provider is configured."
                    AiMode.ASK_EACH_TIME -> "Choose a configured AI provider for this request."
                    AiMode.AUTOMATIC -> "No AI providers are configured."
                }
            )
        }

        val failures = mutableListOf<String>()
        for (provider in targets) {
            when (val result = try { provider.generate(messages, options) } catch (e: Exception) {
                AiResult.Failure(e.message ?: "Provider request failed.", e)
            }) {
                is AiResult.Success -> if (result.text.isNotBlank()) return result
                    else failures += "${provider.displayName}: empty response"
                is AiResult.Failure -> failures += "${provider.displayName}: ${result.reason}"
            }
        }
        return AiResult.Failure(failures.joinToString("; ").take(1200))
    }
}
