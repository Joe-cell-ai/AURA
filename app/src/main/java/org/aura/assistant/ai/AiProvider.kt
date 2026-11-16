package org.aura.assistant.ai

interface AiProvider {
    val id: String
    val displayName: String
    val isOnDevice: Boolean
    suspend fun generate(messages: List<ChatMessage>, options: GenerationOptions = GenerationOptions()): AiResult
}

data class ChatMessage(val role: Role, val content: String) {
    enum class Role { SYSTEM, USER, ASSISTANT }
}

data class GenerationOptions(val temperature: Float = 0.7f, val maxTokens: Int = 512)

sealed interface AiResult {
    data class Success(val text: String) : AiResult
    data class Failure(val reason: String, val cause: Throwable? = null) : AiResult
}
