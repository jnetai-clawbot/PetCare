package com.jnetai.petcare.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.jnetai.petcare.receiver.ReminderReceiver
import java.util.Calendar

object ReminderScheduler {

    private const val REQUEST_CODE_FEEDING = 1000
    private const val REQUEST_CODE_VET = 2000
    private const val REQUEST_CODE_MEDICATION = 3000
    private const val REQUEST_CODE_VACCINATION = 4000

    fun scheduleFeedingReminder(
        context: Context,
        scheduleId: Long,
        petName: String,
        foodName: String,
        hour: Int,
        minute: Int
    ) {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = "ACTION_FEEDING_REMINDER"
            putExtra("type", "feeding")
            putExtra("scheduleId", scheduleId)
            putExtra("petName", petName)
            putExtra("foodName", foodName)
            putExtra("message", "Time to feed $petName: $foodName")
        }

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            if (before(Calendar.getInstance())) add(Calendar.DAY_OF_YEAR, 1)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_FEEDING + scheduleId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        } else {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        }
    }

    fun scheduleVetReminder(
        context: Context,
        appointmentId: Long,
        petName: String,
        vetName: String,
        dateTimeMillis: Long
    ) {
        val reminderTime = dateTimeMillis - 3600000 // 1 hour before

        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = "ACTION_VET_REMINDER"
            putExtra("type", "vet")
            putExtra("appointmentId", appointmentId)
            putExtra("petName", petName)
            putExtra("vetName", vetName)
            putExtra("message", "Vet appointment for $petName with $vetName in 1 hour")
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_VET + appointmentId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                reminderTime,
                pendingIntent
            )
        } else {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                reminderTime,
                pendingIntent
            )
        }
    }

    fun scheduleMedicationReminder(
        context: Context,
        medicationId: Long,
        petName: String,
        medName: String,
        dose: String
    ) {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = "ACTION_MEDICATION_REMINDER"
            putExtra("type", "medication")
            putExtra("medicationId", medicationId)
            putExtra("petName", petName)
            putExtra("medName", medName)
            putExtra("message", "Time for $petName's medication: $medName ($dose)")
        }

        // Schedule for 8am daily as default
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 8)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            if (before(Calendar.getInstance())) add(Calendar.DAY_OF_YEAR, 1)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_MEDICATION + medicationId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val interval = AlarmManager.INTERVAL_DAY
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            interval,
            pendingIntent
        )
    }

    fun scheduleVaccinationReminder(
        context: Context,
        vaccinationId: Long,
        petName: String,
        vaccineName: String,
        dueDateMillis: Long
    ) {
        val reminderTime = dueDateMillis - 86400000L * 7 // 7 days before

        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = "ACTION_VACCINATION_REMINDER"
            putExtra("type", "vaccination")
            putExtra("vaccinationId", vaccinationId)
            putExtra("petName", petName)
            putExtra("vaccineName", vaccineName)
            putExtra("message", "$petName's $vaccineName vaccination is due in 7 days")
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_VACCINATION + vaccinationId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                reminderTime,
                pendingIntent
            )
        } else {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                reminderTime,
                pendingIntent
            )
        }
    }

    fun cancelFeedingReminder(context: Context, scheduleId: Long) {
        cancelReminder(context, REQUEST_CODE_FEEDING + scheduleId.toInt(), "ACTION_FEEDING_REMINDER")
    }

    fun cancelVetReminder(context: Context, appointmentId: Long) {
        cancelReminder(context, REQUEST_CODE_VET + appointmentId.toInt(), "ACTION_VET_REMINDER")
    }

    fun cancelMedicationReminder(context: Context, medicationId: Long) {
        cancelReminder(context, REQUEST_CODE_MEDICATION + medicationId.toInt(), "ACTION_MEDICATION_REMINDER")
    }

    fun cancelVaccinationReminder(context: Context, vaccinationId: Long) {
        cancelReminder(context, REQUEST_CODE_VACCINATION + vaccinationId.toInt(), "ACTION_VACCINATION_REMINDER")
    }

    private fun cancelReminder(context: Context, requestCode: Int, action: String) {
        val intent = Intent(context, ReminderReceiver::class.java).apply { this.action = action }
        val pendingIntent = PendingIntent.getBroadcast(
            context, requestCode, intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        pendingIntent?.let {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.cancel(it)
        }
    }
}