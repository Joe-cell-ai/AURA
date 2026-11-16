package org.aura.assistant.automation

import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * Calculates the delay until the next local wall-clock occurrence.
 * The caller supplies the current local time so the calculation is deterministic and testable.
 */
object AutomationSchedule {
    fun nextDailyDelayMillis(now: LocalDateTime, hour: Int, minute: Int): Long {
        val targetTime = validatedTime(hour, minute)
        var next = now.toLocalDate().atTime(targetTime)
        if (!next.isAfter(now)) next = next.plusDays(1)
        return Duration.between(now, next).toMillis()
    }

    fun nextWeeklyDelayMillis(now: LocalDateTime, day: DayOfWeek, hour: Int, minute: Int): Long {
        val targetTime = validatedTime(hour, minute)
        var nextDate = now.toLocalDate().with(java.time.temporal.TemporalAdjusters.nextOrSame(day))
        var next = nextDate.atTime(targetTime)
        if (!next.isAfter(now)) {
            nextDate = nextDate.plusWeeks(1)
            next = nextDate.atTime(targetTime)
        }
        return Duration.between(now, next).toMillis()
    }

    private fun validatedTime(hour: Int, minute: Int): LocalTime {
        require(hour in 0..23) { "Hour must be between 0 and 23." }
        require(minute in 0..59) { "Minute must be between 0 and 59." }
        return LocalTime.of(hour, minute)
    }
}
