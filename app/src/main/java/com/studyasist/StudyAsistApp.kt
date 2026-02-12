package com.studyasist

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.studyasist.notification.DeferredGradingWorker
import com.studyasist.notification.ExamGoalAlertWorker
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class StudyAsistApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        scheduleExamGoalAlert()
        scheduleDeferredGrading()
    }

    private fun scheduleExamGoalAlert() {
        val request = PeriodicWorkRequestBuilder<ExamGoalAlertWorker>(24, TimeUnit.HOURS)
            .build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "exam_goal_alert",
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    private fun scheduleDeferredGrading() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val request = PeriodicWorkRequestBuilder<DeferredGradingWorker>(24, TimeUnit.HOURS)
            .setConstraints(constraints)
            .build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "deferred_grading",
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }
}
