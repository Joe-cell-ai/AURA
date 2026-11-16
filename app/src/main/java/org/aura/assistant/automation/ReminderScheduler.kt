package org.aura.assistant.automation

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import android.app.NotificationChannel
import android.app.NotificationManager
import java.util.UUID
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

class ReminderScheduler(private val context: Context) {
    fun schedule(title: String, delayMinutes: Long): String {
        require(delayMinutes in 1L..10080L) { "Choose a reminder between 1 minute and 7 days." }
        val safeTitle = AutomationRuleValidator.validate(AutomationRule(title, AutomationRepeat.ONCE)).title
        val id = UUID.randomUUID().toString()
        val request = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(delayMinutes, TimeUnit.MINUTES)
            .setInputData(workDataOf("title" to safeTitle, "reminder_id" to id))
            .addTag("aura-reminder")
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(id, ExistingWorkPolicy.REPLACE, request)
        return id
    }

    fun scheduleRecurring(title: String, repeat: AutomationRepeat): String {
        require(repeat == AutomationRepeat.DAILY || repeat == AutomationRepeat.WEEKLY) {
            "Choose a daily or weekly repeat schedule."
        }
        val rule = AutomationRuleValidator.validate(AutomationRule(title, repeat))
        val id = UUID.randomUUID().toString()
        val request = PeriodicWorkRequestBuilder<ReminderWorker>(repeat.intervalDays.toLong(), TimeUnit.DAYS)
            .setInitialDelay(repeat.intervalDays.toLong(), TimeUnit.DAYS)
            .setInputData(workDataOf("title" to rule.title, "reminder_id" to id))
            .addTag("aura-reminder")
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(id, ExistingPeriodicWorkPolicy.UPDATE, request)
        return id
    }

    /**
     * Schedules a daily notification at a local wall-clock time.
     * WorkManager owns execution and persists the request across process death and reboot.
     */
    /**
     * Schedules a daily notification at a local wall-clock time. Each run schedules
     * the next occurrence, so daylight-saving and manual clock changes are respected.
     */
    fun scheduleDailyAt(title: String, hour: Int, minute: Int): String {
        val rule = AutomationRuleValidator.validate(AutomationRule(title, AutomationRepeat.DAILY))
        val delay = AutomationSchedule.nextDailyDelayMillis(LocalDateTime.now(), hour, minute)
        val id = UUID.randomUUID().toString()
        val request = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(workDataOf(
                "title" to rule.title, "reminder_id" to id, "repeat" to "DAILY",
                "hour" to hour, "minute" to minute
            ))
            .addTag("aura-reminder")
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(id, ExistingWorkPolicy.REPLACE, request)
        return id
    }

    /**
     * Schedules a weekly notification on the selected local day and time.
     */
    fun scheduleWeeklyAt(title: String, day: DayOfWeek, hour: Int, minute: Int): String {
        val rule = AutomationRuleValidator.validate(AutomationRule(title, AutomationRepeat.WEEKLY))
        val delay = AutomationSchedule.nextWeeklyDelayMillis(LocalDateTime.now(), day, hour, minute)
        val id = UUID.randomUUID().toString()
        val request = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(workDataOf(
                "title" to rule.title, "reminder_id" to id, "repeat" to "WEEKLY",
                "day" to day.value, "hour" to hour, "minute" to minute
            ))
            .addTag("aura-reminder")
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(id, ExistingWorkPolicy.REPLACE, request)
        return id
    }

    fun cancel(id: String) {
        WorkManager.getInstance(context).cancelUniqueWork(id)
    }

    fun cancelAll() {
        WorkManager.getInstance(context).cancelAllWorkByTag("aura-reminder")
    }
}

class ReminderWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    private fun scheduleNextOccurrence(reminderId: String) {
        val repeat = inputData.getString("repeat") ?: return
        val title = inputData.getString("title")?.take(120) ?: "AURA reminder"
        val hour = inputData.getInt("hour", -1)
        val minute = inputData.getInt("minute", -1)
        val now = LocalDateTime.now()
        val delay = when (repeat) {
            "DAILY" -> AutomationSchedule.nextDailyDelayMillis(now, hour, minute)
            "WEEKLY" -> {
                val day = inputData.getInt("day", -1)
                if (day !in 1..7) return
                AutomationSchedule.nextWeeklyDelayMillis(now, DayOfWeek.of(day), hour, minute)
            }
            else -> return
        }
        val nextRequest = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(inputData)
            .addTag("aura-reminder")
            .build()
        WorkManager.getInstance(applicationContext).enqueueUniqueWork(
            reminderId, ExistingWorkPolicy.APPEND_OR_REPLACE, nextRequest
        )
    }

    override suspend fun doWork(): Result {
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) return Result.failure()

        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "aura_reminders"
        if (Build.VERSION.SDK_INT >= 26) {
            manager.createNotificationChannel(NotificationChannel(channelId, "AURA reminders", NotificationManager.IMPORTANCE_DEFAULT))
        }
        val title = inputData.getString("title")?.take(120) ?: "AURA reminder"
        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText("Your scheduled reminder")
            .setAutoCancel(true)
            .build()
        return try {
            val reminderId = inputData.getString("reminder_id") ?: UUID.randomUUID().toString()
            NotificationManagerCompat.from(applicationContext).notify(reminderId.hashCode(), notification)
            scheduleNextOccurrence(reminderId)
            Result.success()
        } catch (_: SecurityException) {
            Result.failure()
        }
    }
}
