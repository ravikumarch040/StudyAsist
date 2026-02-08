package com.studyasist.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Build.VERSION_CODES
import androidx.core.app.NotificationManagerCompat
import com.studyasist.data.local.entity.ActivityEntity
import com.studyasist.data.repository.ActivityRepository
import com.studyasist.data.repository.SettingsRepository
import com.studyasist.data.repository.TimetableRepository
import com.studyasist.util.formatTimeMinutes
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val activityRepository: ActivityRepository,
    private val timetableRepository: TimetableRepository,
    private val settingsRepository: SettingsRepository
) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun createChannelIfNeeded() {
        NotificationHelper.createChannel(context)
    }

    suspend fun rescheduleAll() {
        createChannelIfNeeded()
        val activeId = settingsRepository.activeTimetableIdFlow.first() ?: return
        if (activeId < 0) return
        val timetable = timetableRepository.getTimetable(activeId) ?: return
        val activities = activityRepository.getActivitiesForTimetable(activeId).first()
            .filter { it.notifyEnabled }
        activities.forEach { scheduleActivity(it, timetable.name) }
    }

    /** Schedules the next occurrence of one activity (one-shot exact alarm). When it fires, AlarmReceiver shows the notification and enqueues work to schedule the following week. */
    fun scheduleActivity(activity: ActivityEntity, timetableName: String) {
        if (!activity.notifyEnabled) return
        createChannelIfNeeded()
        val triggerTime = nextTriggerTime(activity.dayOfWeek, activity.startTimeMinutes - activity.notifyLeadMinutes.coerceAtLeast(0))
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = ACTION_SHOW_REMINDER
            putExtra(EXTRA_ACTIVITY_ID, activity.id)
            putExtra(EXTRA_TIMETABLE_ID, activity.timetableId)
            putExtra(EXTRA_TIMETABLE_NAME, timetableName)
            putExtra(EXTRA_TITLE, activity.title)
            putExtra(EXTRA_BODY, "${formatTimeMinutes(activity.startTimeMinutes)}–${formatTimeMinutes(activity.endTimeMinutes)} ${activity.title}" + (activity.note?.let { " ($it)" } ?: ""))
            putExtra(EXTRA_DAY_OF_WEEK, activity.dayOfWeek)
        }
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        val pending = PendingIntent.getBroadcast(context, activity.id.toInt().and(0x7FFFFFFF), intent, flags)
        // One-shot exact alarm; AlarmReceiver will reschedule the next occurrence via RescheduleOneActivityWorker
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pending)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pending)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pending)
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, pending)
        }
    }

    /** Loads the activity and timetable, then schedules its next reminder. Used when an alarm fires to schedule the following week. */
    suspend fun scheduleActivityById(activityId: Long) {
        val activity = activityRepository.getActivity(activityId) ?: return
        val timetable = timetableRepository.getTimetable(activity.timetableId) ?: return
        scheduleActivity(activity, timetable.name)
    }

    fun cancelActivity(activityId: Long) {
        val intent = Intent(context, AlarmReceiver::class.java).apply { action = ACTION_SHOW_REMINDER }
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        val pending = PendingIntent.getBroadcast(context, activityId.toInt().and(0x7FFFFFFF), intent, flags)
        alarmManager.cancel(pending)
    }

    private fun nextTriggerTime(dayOfWeek: Int, minutesFromMidnight: Int): Long {
        val cal = Calendar.getInstance()
        // Our dayOfWeek: 1=Mon .. 7=Sun. Calendar: Sunday=1, Monday=2, ... Saturday=7
        val calendarDay = when (dayOfWeek) {
            1 -> Calendar.MONDAY
            2 -> Calendar.TUESDAY
            3 -> Calendar.WEDNESDAY
            4 -> Calendar.THURSDAY
            5 -> Calendar.FRIDAY
            6 -> Calendar.SATURDAY
            7 -> Calendar.SUNDAY
            else -> Calendar.MONDAY
        }
        cal.set(Calendar.DAY_OF_WEEK, calendarDay)
        cal.set(Calendar.HOUR_OF_DAY, minutesFromMidnight / 60)
        cal.set(Calendar.MINUTE, minutesFromMidnight % 60)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        if (cal.timeInMillis <= System.currentTimeMillis()) {
            cal.add(Calendar.DAY_OF_YEAR, 7)
        }
        return cal.timeInMillis
    }
}
