package org.aura.assistant.smarthome

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HomeAssistantRequestValidatorTest {
    @Test fun acceptsHttpsOriginAndNormalizesTrailingSlash() {
        assertEquals("https://home.example", HomeAssistantRequestValidator.baseUrl(" https://home.example/ "))
    }

    @Test fun rejectsHttpAndUrlsWithPathsOrCredentials() {
        listOf(
            "http://home.example",
            "https://user:pass@home.example",
            "https://home.example/config",
            "https://home.example/?token=secret"
        ).forEach { value ->
            val error = runCatching { HomeAssistantRequestValidator.baseUrl(value) }.exceptionOrNull()
            assertTrue("Expected URL to be rejected: $value", error is IllegalArgumentException)
        }
    }

    @Test fun rejectsUnsupportedOrMalformedEntities() {
        listOf("sensor.temperature", "light", "light.bad/name", "unknown.device").forEach { value ->
            val error = runCatching { HomeAssistantRequestValidator.entityId(value) }.exceptionOrNull()
            assertTrue("Expected entity to be rejected: $value", error is IllegalArgumentException)
        }
    }

    @Test fun rejectsMismatchedEntityDomain() {
        val error = runCatching {
            HomeAssistantRequestValidator.validateService("light", "turn_on", "switch.kitchen")
        }.exceptionOrNull()
        assertTrue(error is IllegalArgumentException)
    }

    @Test fun restrictsClimateAndSceneServices() {
        HomeAssistantRequestValidator.validateService("climate", "turn_on", "climate.hall")
        HomeAssistantRequestValidator.validateService("scene", "turn_on", "scene.evening")
        assertTrue(runCatching {
            HomeAssistantRequestValidator.validateService("scene", "toggle", "scene.evening")
        }.exceptionOrNull() is IllegalArgumentException)
    }
}
