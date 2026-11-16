package org.aura.assistant.ai

import org.junit.Assert.assertEquals
import org.junit.Test

class AiRouterTest {
    private class FakeProvider(override val id: String, override val isOnDevice: Boolean) : AiProvider {
        override val displayName = id
        override suspend fun generate(messages: List<ChatMessage>, options: GenerationOptions) = AiResult.Success("ok")
    }

    @Test fun offlineModeOnlyReturnsOnDeviceProviders() {
        val router = AiRouter(
            listOf(FakeProvider("local", true), FakeProvider("cloud", false)),
            AiMode.OFFLINE_ONLY
        )
        assertEquals(listOf("local"), router.candidates().map { it.id })
    }
}
