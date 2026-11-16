package org.aura.assistant.data

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MemoryTextTest {
    @Test fun acceptsBoundedNonBlankMemory() {
        assertTrue(MemoryText.isValid("Prefer concise answers"))
        assertFalse(MemoryText.isValid("  "))
        assertFalse(MemoryText.isValid("ok"))
        assertFalse(MemoryText.isValid("x".repeat(501)))
    }
}
