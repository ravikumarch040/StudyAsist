package com.studyasist.notification

import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * Schedules the next occurrence of a single activity's reminder (used after an alarm fires).
 */
@HiltWorker
class RescheduleOneActivityWorker @AssistedInject constructor(
    @Assisted context: android.content.Context,
    @Assisted params: WorkerParameters,
    private val scheduler: NotificationScheduler
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val activityId = inputData.getLong(WORK_INPUT_ACTIVITY_ID, 0L)
        if (activityId == 0L) return Result.failure()
        return try {
            scheduler.scheduleActivityById(activityId)
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}
