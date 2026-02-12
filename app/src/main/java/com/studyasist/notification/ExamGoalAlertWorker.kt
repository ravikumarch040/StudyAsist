package com.studyasist.notification

import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.studyasist.data.repository.GoalDashboardRepository
import com.studyasist.data.repository.GoalRepository
import com.studyasist.util.daysUntil
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class ExamGoalAlertWorker @AssistedInject constructor(
    @Assisted context: android.content.Context,
    @Assisted params: WorkerParameters,
    private val goalRepository: GoalRepository,
    private val dashboardRepository: GoalDashboardRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            NotificationHelper.createChannel(applicationContext)
            val goals = goalRepository.getActiveGoalsOnce()
            var notifiedCount = 0
            for (goal in goals) {
                val daysRemaining = daysUntil(goal.examDate).toInt()
                if (daysRemaining > DAYS_THRESHOLD) continue
                val metrics = dashboardRepository.getDashboardMetrics(goal.id)
                if (metrics.percentComplete >= PERCENT_THRESHOLD) continue
                val notificationId = NOTIFICATION_ID_EXAM_GOAL_ALERT_BASE + goal.id.toInt().coerceIn(0, 99)
                NotificationHelper.showExamGoalAlert(
                    context = applicationContext,
                    notificationId = notificationId,
                    goalName = goal.name,
                    daysRemaining = daysRemaining.coerceAtLeast(0),
                    percentComplete = metrics.percentComplete.toInt().coerceIn(0, 100)
                )
                notifiedCount++
            }
            Log.d(TAG, "Exam goal alert: checked ${goals.size} goals, notified $notifiedCount")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Exam goal alert failed", e)
            Result.failure()
        }
    }

    companion object {
        private const val TAG = "ExamGoalAlertWorker"
        private const val DAYS_THRESHOLD = 7
        private const val PERCENT_THRESHOLD = 50f
    }
}
