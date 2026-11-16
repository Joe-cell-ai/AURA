package org.aura.assistant.automation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AutomationRuleValidatorTest {
    @Test fun trimsTitleAndKeepsDailyRepeat() {
        val rule = AutomationRuleValidator.validate(AutomationRule("  Take medicine  ", AutomationRepeat.DAILY))
        assertEquals("Take medicine", rule.title)
        assertEquals(1, rule.repeat.intervalDays)
    }

    @Test fun weeklyRepeatUsesSevenDays() {
        assertEquals(7, AutomationRepeat.WEEKLY.intervalDays)
    }

    @Test fun rejectsBlankTitle() {
        val error = runCatching {
            AutomationRuleValidator.validate(AutomationRule("  ", AutomationRepeat.ONCE))
        }.exceptionOrNull()
        assertTrue(error is IllegalArgumentException)
    }

    @Test fun rejectsLongTitle() {
        val error = runCatching {
            AutomationRuleValidator.validate(AutomationRule("x".repeat(121), AutomationRepeat.DAILY))
        }.exceptionOrNull()
        assertTrue(error is IllegalArgumentException)
    }

    @Test fun disabledRuleRemainsDisabled() {
        val rule = AutomationRuleValidator.validate(
            AutomationRule("Do not run", AutomationRepeat.WEEKLY, enabled = false)
        )
        assertFalse(rule.enabled)
    }
}
