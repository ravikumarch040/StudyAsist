package com.studyasist.notification

import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class RescheduleNotificationsWorker @AssistedInject constructor(
    @Assisted context: android.content.Context,
    @Assisted params: WorkerParameters,
    private val scheduler: NotificationScheduler
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            scheduler.rescheduleAll()
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "rescheduleAll failed", e)
            Result.failure()
        }
    }

    companion object {
        private const val TAG = "RescheduleNotificationsWorker"
    }
}
