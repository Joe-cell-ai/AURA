package org.aura.assistant.voice

import org.junit.Assert.*
import org.junit.Test

class AssistantCommandParserTest {
    @Test fun parsesTimerAndOptionalLabel() {
        assertEquals(AssistantCommand.SetTimer(10, null), AssistantCommandParser.parse("set a timer for 10 minutes"))
        assertEquals(AssistantCommand.SetTimer(5, "tea"), AssistantCommandParser.parse("timer for 5 minutes called tea"))
    }

    @Test fun rejectsInvalidTimer() {
        assertNull(AssistantCommandParser.parse("timer for 0 minutes"))
        assertNull(AssistantCommandParser.parse("timer for 2000 minutes"))
    }

    @Test fun parsesAlarm() {
        assertEquals(AssistantCommand.SetAlarm(7, 30, null), AssistantCommandParser.parse("set an alarm for 7:30"))
    }

    @Test fun rejectsInvalidAlarmTime() {
        assertNull(AssistantCommandParser.parse("alarm for 25:00"))
        assertNull(AssistantCommandParser.parse("alarm for 7:90"))
    }

    @Test fun parsesPhoneAndSettingsCommands() {
        assertEquals(AssistantCommand.DialNumber("+91 98765 43210"), AssistantCommandParser.parse("call +91 98765 43210"))
        assertEquals(AssistantCommand.OpenSettings(SettingsPage.WIFI), AssistantCommandParser.parse("open Wi-Fi settings"))
        assertEquals(AssistantCommand.OpenApp("YouTube"), AssistantCommandParser.parse("open YouTube"))
    }

    @Test fun leavesConversationAndHomeControlToChat() {
        assertNull(AssistantCommandParser.parse("What is the weather today?"))
        assertNull(AssistantCommandParser.parse("turn on my living room lights"))
    }
}
