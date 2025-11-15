package com.vtoroychadaev.pestyNotify

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.work.Constraints
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
    val NOTIFY_WORK_TAG = "notifyWorkTag"
    var delay: Long = 30
    lateinit var buttonPlanNotify: Button
    lateinit var buttonCancelNotify: Button
    lateinit var reminderInp: EditText
    lateinit var timerInp: EditText

    companion object {
        const val CHANNEL_ID = "reminderNotificationChannel"
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                planReminder()
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        reminderInp = findViewById<EditText>(R.id.editReminderText)
        timerInp = findViewById<EditText>(R.id.editTimerValue)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
        }

        buttonPlanNotify = findViewById<Button>(R.id.buttonPlanNotify)
        buttonPlanNotify.setOnClickListener {
            askForPermissionAndStart()
       }

        buttonCancelNotify = findViewById<Button>(R.id.buttonCancelNotify)
        buttonCancelNotify.setOnClickListener {
            val workManager = WorkManager.getInstance(this)
            workManager.cancelAllWorkByTag(NOTIFY_WORK_TAG)
        }
    }

    override fun onResume() {
        super.onResume()
        enableEdgeToEdge()


    }
    private fun askForPermissionAndStart() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                planReminder()
            } else {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            planReminder()
        }
    }

    private fun planReminder() {
        val reminderText = reminderInp.text.toString()
        val timerText = timerInp.text.toString()

        delay = timerText.toLongOrNull() ?: 30

        val constraints = Constraints.Builder().setRequiresBatteryNotLow(true).build()

        val delayedNotify = OneTimeWorkRequestBuilder<NotifyWorker>()
            .setInitialDelay(delay, TimeUnit.SECONDS)
            .setConstraints(constraints)
            .setInputData(workDataOf("REMINDER_TEXT" to reminderText))
            .addTag(NOTIFY_WORK_TAG)
            .build()

        WorkManager.getInstance(this).enqueue(delayedNotify)
        Toast.makeText(this, "Reminder will be shown in $delay sec.", Toast.LENGTH_SHORT).show()
    }

    private fun createNotificationChannel() {
        val name = "reminderChannel"
        val descriptionText = "channel for reminder"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}