package org.aura.assistant.ai

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SearchResultUrlValidationTest {
    @Test fun acceptsHttpsSourceUrls() {
        assertTrue(isSafeResultUrl("https://example.org/article?q=a"))
    }

    @Test fun rejectsHttpAndMalformedUrls() {
        assertFalse(isSafeResultUrl("http://example.org/article"))
        assertFalse(isSafeResultUrl("not a url"))
        assertFalse(isSafeResultUrl("javascript:alert(1)"))
    }

    @Test fun rejectsEmbeddedCredentialsAndFragments() {
        assertFalse(isSafeResultUrl("https://user:secret@example.org/article"))
        assertFalse(isSafeResultUrl("https://example.org/article#fragment"))
    }
}
