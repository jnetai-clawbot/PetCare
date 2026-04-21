package com.jnetai.petcare.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat

class ReminderReceiver : BroadcastReceiver() {

    companion object {
        private const val CHANNEL_ID = "petcare_reminders"
        private const val CHANNEL_NAME = "Pet Care Reminders"
    }

    override fun onReceive(context: Context, intent: Intent) {
        createNotificationChannel(context)

        val message = intent.getStringExtra("message") ?: "Pet care reminder"
        val type = intent.getStringExtra("type") ?: "general"
        val notificationId = when (type) {
            "feeding" -> 1000 + (intent.getLongExtra("scheduleId", 0) % 1000).toInt()
            "vet" -> 2000 + (intent.getLongExtra("appointmentId", 0) % 1000).toInt()
            "medication" -> 3000 + (intent.getLongExtra("medicationId", 0) % 1000).toInt()
            "vaccination" -> 4000 + (intent.getLongExtra("vaccinationId", 0) % 1000).toInt()
            else -> 9999
        }

        val iconRes = when (type) {
            "feeding" -> android.R.drawable.ic_menu_agenda
            "vet" -> android.R.drawable.ic_menu_today
            "medication" -> android.R.drawable.ic_menu_manage
            "vaccination" -> android.R.drawable.ic_menu_today
            else -> android.R.drawable.ic_dialog_info
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(iconRes)
            .setContentTitle("PetCare Reminder")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, notification)
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Reminders for pet care tasks"
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }
}