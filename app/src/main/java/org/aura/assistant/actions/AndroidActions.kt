package org.aura.assistant.actions

import android.content.Context
import android.content.Intent
import android.provider.CalendarContract

/** Explicit user-invoked Android actions. Model text is never executed as an action. */
class AndroidActions(private val context: Context) {
    fun launchApp(packageName: String): Boolean {
        if (packageName.isBlank()) return false
        val intent = context.packageManager.getLaunchIntentForPackage(packageName.trim()) ?: return false
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
        return true
    }

    fun createCalendarReminder(title: String) {
        val intent = Intent(Intent.ACTION_INSERT).setData(CalendarContract.Events.CONTENT_URI)
            .putExtra(CalendarContract.Events.TITLE, title.ifBlank { "AURA reminder" })
            .putExtra(CalendarContract.Events.ALL_DAY, false)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}
