package org.aura.assistant.ai

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class AiEndpointValidatorTest {
    @Test fun acceptsHttpsOpenAiCompatibleEndpoint() {
        assertEquals(
            "https://api.example.com/v1/chat/completions",
            AiEndpointValidator.validate(" https://api.example.com/v1/chat/completions ")
        )
    }

    @Test fun rejectsHttpEndpoint() {
        assertThrows(IllegalArgumentException::class.java) {
            AiEndpointValidator.validate("http://api.example.com/v1/chat/completions")
        }
    }

    @Test fun rejectsEmbeddedCredentials() {
        assertThrows(IllegalArgumentException::class.java) {
            AiEndpointValidator.validate("https://user:secret@api.example.com/v1/chat/completions")
        }
    }

    @Test fun rejectsMalformedEndpoint() {
        assertThrows(IllegalArgumentException::class.java) {
            AiEndpointValidator.validate("not a URL")
        }
    }

    @Test fun rejectsUrlFragment() {
        assertThrows(IllegalArgumentException::class.java) {
            AiEndpointValidator.validate("https://api.example.com/v1/chat/completions#secret")
        }
    }
}
