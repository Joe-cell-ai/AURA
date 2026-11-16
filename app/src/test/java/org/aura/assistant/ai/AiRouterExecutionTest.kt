package org.aura.assistant.ai

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AiRouterExecutionTest {
    private class FakeProvider(
        override val id: String,
        override val isOnDevice: Boolean,
        private val result: AiResult
    ) : AiProvider {
        override val displayName = id
        var calls = 0
        override suspend fun generate(messages: List<ChatMessage>, options: GenerationOptions) : AiResult {
            calls++
            return result
        }
    }

    @Test fun automaticModeFallsBackInConfiguredOrder() = runBlocking {
        val broken = FakeProvider("primary", false, AiResult.Failure("offline"))
        val working = FakeProvider("backup", true, AiResult.Success("answer"))
        val result = AiRouter(listOf(broken, working), AiMode.AUTOMATIC)
            .generate(listOf(ChatMessage("user", "hello")))
        assertEquals(AiResult.Success("answer"), result)
        assertEquals(1, broken.calls)
        assertEquals(1, working.calls)
    }

    @Test fun offlineModeNeverCallsCloudProvider() = runBlocking {
        val local = FakeProvider("local", true, AiResult.Success("local answer"))
        val cloud = FakeProvider("cloud", false, AiResult.Success("cloud answer"))
        val result = AiRouter(listOf(cloud, local), AiMode.OFFLINE_ONLY)
            .generate(listOf(ChatMessage("user", "hello")))
        assertEquals(AiResult.Success("local answer"), result)
        assertEquals(0, cloud.calls)
    }

    @Test fun askEachTimeRequiresExplicitProvider() = runBlocking {
        val provider = FakeProvider("cloud", false, AiResult.Success("ok"))
        val router = AiRouter(listOf(provider), AiMode.ASK_EACH_TIME)
        val missing = router.generate(listOf(ChatMessage("user", "hello")))
        assertTrue(missing is AiResult.Failure)
        assertEquals(0, provider.calls)
        assertEquals(AiResult.Success("ok"), router.generate(
            listOf(ChatMessage("user", "hello")), selectedProviderId = "cloud"
        ))
    }
}
