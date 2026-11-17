package org.aura.assistant.actions

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.CalendarContract
import android.provider.AlarmClock
import android.content.pm.PackageManager
import android.provider.Settings

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

    /** Launches an installed launcher app by its visible label, without shell access. */
    fun launchAppByLabel(label: String): Boolean {
        val wanted = label.trim()
        if (wanted.isBlank() || wanted.length > 80) return false
        val launcher = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val matches = context.packageManager.queryIntentActivities(launcher, PackageManager.MATCH_DEFAULT_ONLY)
        val match = matches.firstOrNull {
            it.loadLabel(context.packageManager).toString().equals(wanted, ignoreCase = true)
        } ?: matches.firstOrNull {
            it.loadLabel(context.packageManager).toString().contains(wanted, ignoreCase = true)
        } ?: return false
        val intent = context.packageManager.getLaunchIntentForPackage(match.activityInfo.packageName) ?: return false
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
        return true
    }

    /** Opens the system Clock timer UI; the user confirms the timer there. */
    fun setTimer(minutes: Int, label: String?) {
        require(minutes in 1..1440) { "Timer must be between 1 minute and 24 hours." }
        val intent = Intent(AlarmClock.ACTION_SET_TIMER)
            .putExtra(AlarmClock.EXTRA_LENGTH, minutes * 60)
            .putExtra(AlarmClock.EXTRA_MESSAGE, label?.take(40) ?: "AURA timer")
            .putExtra(AlarmClock.EXTRA_SKIP_UI, false)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    /** Opens the system Clock alarm UI; it never silently changes an existing alarm. */
    fun setAlarm(hour: Int, minute: Int, label: String?) {
        require(hour in 0..23 && minute in 0..59) { "Enter a valid alarm time." }
        val intent = Intent(AlarmClock.ACTION_SET_ALARM)
            .putExtra(AlarmClock.EXTRA_HOUR, hour)
            .putExtra(AlarmClock.EXTRA_MINUTES, minute)
            .putExtra(AlarmClock.EXTRA_MESSAGE, label?.take(40) ?: "AURA alarm")
            .putExtra(AlarmClock.EXTRA_SKIP_UI, false)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    fun createCalendarReminder(title: String) {
        val safeTitle = title.trim().take(120).ifBlank { "AURA reminder" }
        val intent = Intent(Intent.ACTION_INSERT).setData(CalendarContract.Events.CONTENT_URI)
            .putExtra(CalendarContract.Events.TITLE, safeTitle)
            .putExtra(CalendarContract.Events.ALL_DAY, false)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    /** Opens the dialler; AURA never places a call directly. */
    fun openDialler(phoneNumber: String) {
        val safe = (ActionRequestValidator.validate(ActionRequest.DialNumber(phoneNumber)).request as ActionRequest.DialNumber).phoneNumber
        context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$safe")).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }

    /** Opens the SMS composer with a prefilled recipient/body; user presses Send. */
    fun composeSms(phoneNumber: String, message: String) {
        val safe = ActionRequestValidator.validate(ActionRequest.ComposeSms(phoneNumber, message)).request as ActionRequest.ComposeSms
        val intent = Intent(Intent.ACTION_SENDTO, Uri.fromParts("smsto", safe.phoneNumber, null))
            .putExtra("sms_body", safe.message)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    fun openSystemSettings(destination: SettingsDestination) {
        val action = when (destination) {
            SettingsDestination.WIFI -> Settings.ACTION_WIFI_SETTINGS
            SettingsDestination.BLUETOOTH -> Settings.ACTION_BLUETOOTH_SETTINGS
            SettingsDestination.SOUND -> Settings.ACTION_SOUND_SETTINGS
            SettingsDestination.DISPLAY -> Settings.ACTION_DISPLAY_SETTINGS
            SettingsDestination.APPLICATIONS -> Settings.ACTION_APPLICATION_SETTINGS
            SettingsDestination.SECURITY -> Settings.ACTION_SECURITY_SETTINGS
            SettingsDestination.LOCATION -> Settings.ACTION_LOCATION_SOURCE_SETTINGS
        }
        context.startActivity(Intent(action).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }
}
