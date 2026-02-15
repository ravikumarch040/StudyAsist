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
import com.studyasist.R
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

object NotificationHelper {

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val defaultChannel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.channel_study_reminder),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = context.getString(R.string.channel_reminders_description)
            setSound(Uri.parse("content://settings/system/notification_sound"), AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_NOTIFICATION).build())
            enableVibration(true)
        }
        nm.createNotificationChannel(defaultChannel)
        val alarmChannel = NotificationChannel(
            CHANNEL_ID_ALARM,
            context.getString(R.string.channel_study_alarm),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = context.getString(R.string.channel_alarm_description)
            setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM), AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM).build())
            enableVibration(true)
        }
        nm.createNotificationChannel(alarmChannel)
        val alarmTtsChannel = NotificationChannel(
            CHANNEL_ID_ALARM_TTS,
            context.getString(R.string.channel_study_alarm_tts),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = context.getString(R.string.channel_alarm_tts_description)
            setSound(null, null)
            enableVibration(true)
        }
        nm.createNotificationChannel(alarmTtsChannel)
        val examGoalChannel = NotificationChannel(
            CHANNEL_ID_EXAM_GOAL_ALERT,
            context.getString(R.string.channel_exam_goal_alert),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = context.getString(R.string.channel_exam_goal_description)
            enableVibration(true)
        }
        nm.createNotificationChannel(examGoalChannel)
    }

    fun showExamGoalAlert(
        context: Context,
        notificationId: Int,
        goalName: String,
        daysRemaining: Int,
        percentComplete: Int
    ) {
        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
            flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pending = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val title = context.getString(R.string.exam_in_days_format, daysRemaining)
        val body = context.getString(R.string.exam_goal_coverage_format, goalName, percentComplete)
        val notification = NotificationCompat.Builder(context, CHANNEL_ID_EXAM_GOAL_ALERT)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setContentIntent(pending)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        NotificationManagerCompat.from(context).notify(notificationId, notification)
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
        val titleBody = context.getString(R.string.reminder_title_body_format, title, body)
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(timetableName)
            .setContentText(titleBody)
            .setStyle(NotificationCompat.BigTextStyle().bigText(titleBody))
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
        val titleBody = context.getString(R.string.reminder_title_body_format, title, body)
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(timetableName)
            .setContentText(titleBody)
            .setStyle(NotificationCompat.BigTextStyle().bigText(titleBody))
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
