package org.aura.assistant.actions

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PackageNameValidatorTest {
    @Test fun acceptsPackageIdentifiersOnly() {
        assertTrue(PackageNameValidator.isValid("com.example.app"))
        assertFalse(PackageNameValidator.isValid(""))
        assertFalse(PackageNameValidator.isValid("com.example; rm -rf"))
        assertFalse(PackageNameValidator.isValid("not-a-package"))
    }
}
