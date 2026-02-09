package com.studyasist.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

object NotificationHelper {

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val defaultChannel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Reminders for timetable activities"
            setSound(Uri.parse("content://settings/system/notification_sound"), AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_NOTIFICATION).build())
            enableVibration(true)
        }
        nm.createNotificationChannel(defaultChannel)
        val alarmChannel = NotificationChannel(
            CHANNEL_ID_ALARM,
            CHANNEL_NAME_ALARM,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Alarm-style reminders that ring until you dismiss"
            setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM), AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM).build())
            enableVibration(true)
        }
        nm.createNotificationChannel(alarmChannel)
        val alarmTtsChannel = NotificationChannel(
            CHANNEL_ID_ALARM_TTS,
            CHANNEL_NAME_ALARM_TTS,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Custom spoken message plays when reminder fires; no notification sound"
            setSound(null, null)
            enableVibration(true)
        }
        nm.createNotificationChannel(alarmTtsChannel)
    }

    fun showReminder(
        context: Context,
        notificationId: Int,
        timetableName: String,
        title: String,
        body: String,
        timetableId: Long,
        dayOfWeek: Int
    ) {
        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
            flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pending = android.app.PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(timetableName)
            .setContentText("$title – $body")
            .setStyle(NotificationCompat.BigTextStyle().bigText("$title – $body"))
            .setContentIntent(pending)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        NotificationManagerCompat.from(context).notify(notificationId, notification)
    }

    /**
     * Shows a high-priority alarm notification with full-screen intent so the alarm activity
     * can appear over the lock screen.
     * @param useTtsMessage true when user set a custom TTS message: use silent channel so only
     *                      the activity plays TTS; false uses system alarm sound from the channel.
     */
    fun showReminderAlarm(
        context: Context,
        notificationId: Int,
        timetableName: String,
        title: String,
        body: String,
        fullScreenIntent: PendingIntent,
        useTtsMessage: Boolean = false
    ) {
        val channelId = if (useTtsMessage) CHANNEL_ID_ALARM_TTS else CHANNEL_ID_ALARM
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(timetableName)
            .setContentText("$title – $body")
            .setStyle(NotificationCompat.BigTextStyle().bigText("$title – $body"))
            .setContentIntent(fullScreenIntent)
            .setFullScreenIntent(fullScreenIntent, true)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()
        NotificationManagerCompat.from(context).notify(notificationId, notification)
    }
}
