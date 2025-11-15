package com.vtoroychadaev.pestyNotify

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters

class NotifyWorker(private val context: Context, params: WorkerParameters) : Worker(context, params) {
    override fun doWork(): Result {
        val reminderText = inputData.getString("REMINDER_TEXT") ?: return Result.failure()

        showNotification(reminderText)
        return Result.success()
    }

    fun showNotification(notifyText: String) {
        val notificationId = 1

        val builder = NotificationCompat.Builder(context, MainActivity.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_icon)
            .setContentTitle("pestyRemainder")
            .setContentText(notifyText)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        with(NotificationManagerCompat.from(context)) {
            notify(notificationId, builder.build())
        }
    }
}