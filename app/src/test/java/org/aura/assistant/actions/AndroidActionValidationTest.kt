package org.aura.assistant.actions

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AndroidActionValidationTest {
    @Test fun diallerActionNormalizesNumberAndRequiresConfirmation() {
        val result = ActionRequestValidator.validate(ActionRequest.DialNumber(" +91 98765-43210 "))
        assertEquals(ActionRisk.MEDIUM, result.risk)
        assertTrue(result.requiresConfirmation)
        assertEquals("+919876543210", (result.request as ActionRequest.DialNumber).phoneNumber)
    }

    @Test fun smsActionTrimsMessageAndRequiresConfirmation() {
        val result = ActionRequestValidator.validate(ActionRequest.ComposeSms("98765 43210", "  Hello  "))
        assertEquals(ActionRisk.HIGH, result.risk)
        assertTrue(result.requiresConfirmation)
        val safe = result.request as ActionRequest.ComposeSms
        assertEquals("9876543210", safe.phoneNumber)
        assertEquals("Hello", safe.message)
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsSmsWithEmptyBody() {
        ActionRequestValidator.validate(ActionRequest.ComposeSms("9876543210", "  "))
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsPhoneNumberWithLetters() {
        ActionRequestValidator.validate(ActionRequest.DialNumber("call-me"))
    }

    @Test fun settingsActionsAreAlwaysConfirmationGated() {
        val result = ActionRequestValidator.validate(ActionRequest.OpenSystemSettings(SettingsDestination.WIFI))
        assertTrue(result.requiresConfirmation)
        assertEquals(ActionRisk.MEDIUM, result.risk)
    }
}
