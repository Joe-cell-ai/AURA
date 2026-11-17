package org.aura.assistant.voice

/**
 * Small, deterministic command parser for phone-first assistant actions.
 *
 * Only explicit, well-formed commands become actions. Everything else remains
 * a normal conversation request; model-generated text is never parsed/executed.
 */
sealed interface AssistantCommand {
    data class OpenApp(val appName: String) : AssistantCommand
    data class SetTimer(val durationMinutes: Int, val label: String?) : AssistantCommand
    data class SetReminder(val title: String, val delayMinutes: Int) : AssistantCommand
    data class SetAlarm(val hour: Int, val minute: Int, val label: String?) : AssistantCommand
    data class OpenSettings(val destination: SettingsPage) : AssistantCommand
    data class DialNumber(val phoneNumber: String) : AssistantCommand
}

enum class SettingsPage { WIFI, BLUETOOTH, SOUND, DISPLAY, LOCATION }

object AssistantCommandParser {
    private val reminder = Regex("^remind me to (.{2,80}) in ([0-9]{1,5}) minutes?$", RegexOption.IGNORE_CASE)\n    private val timer = Regex("^(?:set )?(?:a )?timer for ([0-9]{1,4}) minutes?(?: called (.{1,40}))?$", RegexOption.IGNORE_CASE)
    private val alarm = Regex("^(?:set )?an? alarm for ([0-2]?[0-9])(?::([0-5][0-9]))?(?: (?:called|named) (.{1,40}))?$", RegexOption.IGNORE_CASE)
    private val dial = Regex("^(?:call|dial) ([+0-9][+0-9 ()-]{2,24})$", RegexOption.IGNORE_CASE)

    fun parse(utterance: String): AssistantCommand? {
        val text = utterance.trim().replace(Regex("\\s+"), " ")
        if (text.isEmpty() || text.length > 240) return null

        reminder.matchEntire(text)?.let { match ->\n            val title = match.groupValues[1].trim()\n            val minutes = match.groupValues[2].toIntOrNull() ?: return null\n            if (title.isBlank() || minutes !in 1..10080) return null\n            return AssistantCommand.SetReminder(title, minutes)\n        }\n        timer.matchEntire(text)?.let { match ->
            val minutes = match.groupValues[1].toIntOrNull() ?: return null
            if (minutes !in 1..1440) return null
            return AssistantCommand.SetTimer(minutes, match.groupValues[2].trim().ifBlank { null })
        }
        alarm.matchEntire(text)?.let { match ->
            val hour = match.groupValues[1].toIntOrNull() ?: return null
            val minute = match.groupValues[2].takeIf { it.isNotBlank() }?.toIntOrNull() ?: 0
            if (hour !in 0..23) return null
            return AssistantCommand.SetAlarm(hour, minute, match.groupValues[3].trim().ifBlank { null })
        }
        dial.matchEntire(text)?.let { match ->
            val number = match.groupValues[1].trim()
            val digits = number.count(Char::isDigit)
            if (digits !in 3..20) return null
            return AssistantCommand.DialNumber(number)
        }

        val lower = text.lowercase()
        val settings = when (lower) {
            "open wi-fi settings", "open wifi settings", "show wi-fi settings" -> SettingsPage.WIFI
            "open bluetooth settings", "show bluetooth settings" -> SettingsPage.BLUETOOTH
            "open sound settings", "show sound settings" -> SettingsPage.SOUND
            "open display settings", "show display settings" -> SettingsPage.DISPLAY
            "open location settings", "show location settings" -> SettingsPage.LOCATION
            else -> null
        }
        if (settings != null) return AssistantCommand.OpenSettings(settings)

        val app = Regex("^(?:open|launch) (.{2,60})$", RegexOption.IGNORE_CASE)
            .matchEntire(text)?.groupValues?.get(1)?.trim()
        if (app != null && app.lowercase() !in setOf("settings", "home assistant", "smart home")) {
            return AssistantCommand.OpenApp(app)
        }
        return null
    }
}
