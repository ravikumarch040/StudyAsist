package com.studyasist.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.studyasist.R
import com.studyasist.data.local.dao.QADao
import com.studyasist.data.local.dao.ActivityDao
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class SmartNotificationWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val qaDao: QADao,
    private val activityDao: ActivityDao
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val CHANNEL_ID = "smart_notifications"
        const val CHANNEL_NAME = "Smart Study Alerts"
        const val NOTIFICATION_ID_RETENTION = 2001
        const val NOTIFICATION_ID_NUDGE = 2002
    }

    override suspend fun doWork(): Result {
        createChannel()
        checkRetentionAlerts()
        return Result.success()
    }

    private suspend fun checkRetentionAlerts() {
        val now = System.currentTimeMillis()
        val overdueCount = qaDao.getDueCount(now)
        if (overdueCount > 5) {
            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(context.getString(R.string.daily_review))
                .setContentText(context.getString(R.string.cards_due_today, overdueCount))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .build()
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.notify(NOTIFICATION_ID_RETENTION, notification)
        }
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Smart study alerts based on your learning patterns"
            }
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(channel)
        }
    }
}
