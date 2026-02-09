package com.studyasist.notification

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.studyasist.data.repository.SettingsRepositoryEntryPoint
import dagger.hilt.android.EntryPointAccessors
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_SHOW_REMINDER) return
        val timetableName = intent.getStringExtra(EXTRA_TIMETABLE_NAME) ?: "StudyAsist"
        val title = intent.getStringExtra(EXTRA_TITLE) ?: ""
        val body = intent.getStringExtra(EXTRA_BODY) ?: ""
        val activityId = intent.getLongExtra(EXTRA_ACTIVITY_ID, 0L)
        val notificationId = activityId.toInt().and(0x7FFFFFFF)

        val settings = runBlocking {
            val entryPoint = EntryPointAccessors.fromApplication(
                context.applicationContext,
                SettingsRepositoryEntryPoint::class.java
            )
            entryPoint.getSettingsRepository().settingsFlow.first()
        }
        val ttsMessage = settings.alarmTtsMessage
        val soundEnabled = settings.soundEnabled

        val alarmIntent = Intent(context, ReminderAlarmActivity::class.java).apply {
            putExtra(EXTRA_TITLE, title)
            putExtra(EXTRA_BODY, body)
            putExtra(EXTRA_TIMETABLE_NAME, timetableName)
            putExtra(EXTRA_ACTIVITY_ID, activityId)
            putExtra(EXTRA_ALARM_TTS_MESSAGE, ttsMessage)
            putExtra(EXTRA_SOUND_ENABLED, soundEnabled)
            addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS or
                    Intent.FLAG_ACTIVITY_NO_HISTORY or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED or
                    Intent.FLAG_ACTIVITY_NO_USER_ACTION
            )
        }
        val fullScreenPending = PendingIntent.getActivity(
            context,
            notificationId,
            alarmIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val useTts = ttsMessage.isNotBlank() && soundEnabled
        if (useTts) {
            val serviceIntent = Intent(context, AlarmTtsService::class.java).apply {
                putExtra(EXTRA_ALARM_TTS_MESSAGE, ttsMessage)
                putExtra(EXTRA_TITLE, title)
                putExtra(EXTRA_BODY, body)
                putExtra(EXTRA_TIMETABLE_NAME, timetableName)
                putExtra(EXTRA_ACTIVITY_ID, activityId)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
        } else {
            NotificationHelper.createChannel(context)
            NotificationHelper.showReminderAlarm(context, notificationId, timetableName, title, body, fullScreenPending, useTtsMessage = false)
            context.startActivity(alarmIntent)
        }

        if (activityId != 0L) {
            val request = OneTimeWorkRequestBuilder<RescheduleOneActivityWorker>()
                .setInputData(workDataOf(WORK_INPUT_ACTIVITY_ID to activityId))
                .build()
            WorkManager.getInstance(context).enqueue(request)
        }
    }
}
