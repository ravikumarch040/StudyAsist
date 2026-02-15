package com.studyasist

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.studyasist.data.repository.SettingsRepository
import com.studyasist.notification.CloudBackupWorker
import com.studyasist.notification.DeferredGradingWorker
import com.studyasist.notification.ExamGoalAlertWorker
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class StudyAsistApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var settingsRepository: SettingsRepository

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        applyAppLocale()
        scheduleExamGoalAlert()
        scheduleDeferredGrading()
        scheduleCloudBackupIfEnabled()
    }

    private fun scheduleCloudBackupIfEnabled() {
        runBlocking {
            if (settingsRepository.settingsFlow.first().cloudBackupAuto) {
                val request = PeriodicWorkRequestBuilder<CloudBackupWorker>(24, TimeUnit.HOURS).build()
                WorkManager.getInstance(this@StudyAsistApp).enqueueUniquePeriodicWork(
                    "cloud_backup_auto",
                    ExistingPeriodicWorkPolicy.KEEP,
                    request
                )
            }
        }
    }

    private fun applyAppLocale() {
        runBlocking {
            val locale = settingsRepository.settingsFlow.first().appLocale
            val locales = when (locale) {
                "en", "hi", "es", "fr", "de" -> LocaleListCompat.forLanguageTags(locale)
                else -> LocaleListCompat.getEmptyLocaleList()
            }
            AppCompatDelegate.setApplicationLocales(locales)
        }
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
