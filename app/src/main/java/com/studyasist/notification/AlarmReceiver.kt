package com.studyasist.notification

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_SHOW_REMINDER) return
        val timetableName = intent.getStringExtra(EXTRA_TIMETABLE_NAME) ?: "StudyAsist"
        val title = intent.getStringExtra(EXTRA_TITLE) ?: ""
        val body = intent.getStringExtra(EXTRA_BODY) ?: ""
        val activityId = intent.getLongExtra(EXTRA_ACTIVITY_ID, 0L)
        val notificationId = activityId.toInt().and(0x7FFFFFFF)

        val alarmIntent = Intent(context, ReminderAlarmActivity::class.java).apply {
            putExtra(EXTRA_TITLE, title)
            putExtra(EXTRA_BODY, body)
            putExtra(EXTRA_TIMETABLE_NAME, timetableName)
            putExtra(EXTRA_ACTIVITY_ID, activityId)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS or Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
        }
        val fullScreenPending = PendingIntent.getActivity(
            context,
            notificationId,
            alarmIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        NotificationHelper.createChannel(context)
        NotificationHelper.showReminderAlarm(context, notificationId, timetableName, title, body, fullScreenPending)
        context.startActivity(alarmIntent)

        if (activityId != 0L) {
            val request = OneTimeWorkRequestBuilder<RescheduleOneActivityWorker>()
                .setInputData(workDataOf(WORK_INPUT_ACTIVITY_ID to activityId))
                .build()
            WorkManager.getInstance(context).enqueue(request)
        }
    }
}
