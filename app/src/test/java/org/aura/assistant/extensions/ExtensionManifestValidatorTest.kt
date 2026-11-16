package org.aura.assistant.extensions

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExtensionManifestValidatorTest {
    @Test fun acceptsOnlyKnownDeclarativeCapabilities() {
        val safe = ExtensionManifest("com.example.demo", "Demo", "1.0", "Community", "MIT", setOf("reminders"))
        val unsafe = safe.copy(capabilities = setOf("run_arbitrary_code"))
        assertTrue(ExtensionManifestValidator.validate(safe))
        assertFalse(ExtensionManifestValidator.validate(unsafe))
    }
}
