package org.aura.assistant.actions

import android.content.Context
import android.content.Intent
import android.provider.CalendarContract

/** Explicit user-invoked Android actions. Model text is never executed as an action. */
class AndroidActions(private val context: Context) {
    fun launchApp(packageName: String): Boolean {
        val normalized = packageName.trim()
        if (!PackageNameValidator.isValid(normalized)) return false
        val intent = context.packageManager.getLaunchIntentForPackage(normalized) ?: return false
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
        return true
    }

    fun createCalendarReminder(title: String) {
        val safeTitle = title.trim().take(120).ifBlank { "AURA reminder" }
        val intent = Intent(Intent.ACTION_INSERT).setData(CalendarContract.Events.CONTENT_URI)
            .putExtra(CalendarContract.Events.TITLE, safeTitle)
            .putExtra(CalendarContract.Events.ALL_DAY, false)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}

object PackageNameValidator {
    private val pattern = Regex("[A-Za-z][A-Za-z0-9_]*(\\.[A-Za-z][A-Za-z0-9_]*)+")
    fun isValid(value: String): Boolean = value.length <= 255 && pattern.matches(value)
}
