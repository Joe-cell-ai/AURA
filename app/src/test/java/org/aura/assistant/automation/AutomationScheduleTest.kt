package org.aura.assistant.automation

import java.time.DayOfWeek
import java.time.LocalDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AutomationScheduleTest {
    @Test fun dailyScheduleUsesNextSameDayOccurrence() {
        val now = LocalDateTime.of(2026, 11, 17, 8, 30)
        assertEquals(90L * 60L * 1000L, AutomationSchedule.nextDailyDelayMillis(now, 10, 0))
    }

    @Test fun dailyScheduleMovesToTomorrowWhenTimeHasPassed() {
        val now = LocalDateTime.of(2026, 11, 17, 10, 0)
        assertEquals(24L * 60L * 60L * 1000L, AutomationSchedule.nextDailyDelayMillis(now, 10, 0))
    }

    @Test fun weeklyScheduleFindsNextRequestedDay() {
        // Tuesday at 08:00, next Friday at 09:30.
        val now = LocalDateTime.of(2026, 11, 17, 8, 0)
        val expected = 3L * 24L * 60L * 60L * 1000L + 90L * 60L * 1000L
        assertEquals(expected, AutomationSchedule.nextWeeklyDelayMillis(now, DayOfWeek.FRIDAY, 9, 30))
    }

    @Test fun weeklyScheduleMovesOneWeekWhenTargetTimePassedToday() {
        val now = LocalDateTime.of(2026, 11, 17, 10, 0)
        val delay = AutomationSchedule.nextWeeklyDelayMillis(now, DayOfWeek.TUESDAY, 9, 0)
        assertEquals(6L * 24L * 60L * 60L * 1000L + 23L * 60L * 60L * 1000L, delay)
        assertTrue(delay > 0)
    }

    @Test fun rejectsInvalidTime() {
        val error = runCatching {
            AutomationSchedule.nextDailyDelayMillis(LocalDateTime.now(), 24, 0)
        }.exceptionOrNull()
        assertTrue(error is IllegalArgumentException)
    }
}
