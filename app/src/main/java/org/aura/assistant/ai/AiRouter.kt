package org.aura.assistant.ai

enum class AiMode { OFFLINE_ONLY, ONLINE_ONLY, ASK_EACH_TIME, AUTOMATIC }

class AiRouter(private val providers: List<AiProvider>, private val mode: AiMode) {
    fun candidates(): List<AiProvider> = when (mode) {
        AiMode.OFFLINE_ONLY -> providers.filter { it.isOnDevice }
        AiMode.ONLINE_ONLY -> providers.filterNot { it.isOnDevice }
        AiMode.ASK_EACH_TIME -> emptyList()
        AiMode.AUTOMATIC -> providers
    }
}
