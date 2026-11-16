package org.aura.assistant.actions

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ActionRequestValidatorTest {
    @Test fun marksDeviceActionsForConfirmation() {
        val launch = ActionRequestValidator.validate(ActionRequest.LaunchApp("com.example.app"))
        assertTrue(launch.requiresConfirmation)
        assertEquals(ActionRisk.MEDIUM, launch.risk)

        val home = ActionRequestValidator.validate(
            ActionRequest.HomeAssistantService("light.office", "light", "turn_off")
        )
        assertTrue(home.requiresConfirmation)
        assertEquals(ActionRisk.HIGH, home.risk)
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsUnsupportedHomeAssistantService() {
        ActionRequestValidator.validate(
            ActionRequest.HomeAssistantService("light.office", "light", "run_shell")
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsOutOfRangeReminderDelay() {
        ActionRequestValidator.validate(ActionRequest.ScheduledReminder("Test", 0))
    }
}
