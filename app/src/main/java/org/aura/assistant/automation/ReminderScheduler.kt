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
import java.util.concurrent.TimeUnit

class ReminderScheduler(private val context: Context) {
    fun schedule(title: String, delayMinutes: Long): String {
        require(delayMinutes in 1L..10080L) { "Choose a reminder between 1 minute and 7 days." }
        val safeTitle = title.trim().take(120).ifBlank { "AURA reminder" }
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

    fun cancel(id: String) {
        WorkManager.getInstance(context).cancelUniqueWork(id)
    }

    fun cancelAll() {
        WorkManager.getInstance(context).cancelAllWorkByTag("aura-reminder")
    }
}

class ReminderWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
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
            NotificationManagerCompat.from(applicationContext).notify(
                inputData.getString("reminder_id")?.hashCode() ?: System.currentTimeMillis().toInt(),
                notification
            )
            Result.success()
        } catch (_: SecurityException) {
            Result.failure()
        }
    }
}
