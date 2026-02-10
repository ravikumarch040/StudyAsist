package com.studyasist.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.studyasist.R

/**
 * Foreground service that runs during a Study activity block. Checks periodically
 * whether the user has switched to a restricted app (YouTube, Instagram, games, etc.)
 * and shows a notification to nudge them back. Does not block the app.
 */
class StudyGuardService : Service() {

    private val handler = Handler(Looper.getMainLooper())
    private var endTimeMillis: Long = 0
    private var activityTitle: String = ""
    private val lastAlertByPackage = mutableMapOf<String, Long>()

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null) {
            stopSelf()
            return START_NOT_STICKY
        }
        endTimeMillis = intent.getLongExtra(EXTRA_END_TIME, 0)
        activityTitle = intent.getStringExtra(EXTRA_ACTIVITY_TITLE) ?: "Study"

        if (endTimeMillis <= System.currentTimeMillis()) {
            stopSelf()
            return START_NOT_STICKY
        }

        if (!hasUsageStatsPermission(this)) {
            stopSelf()
            return START_NOT_STICKY
        }

        createChannelIfNeeded()
        val notification = NotificationCompat.Builder(this, CHANNEL_ID_FOCUS_GUARD)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(getString(R.string.focus_guard_notification_title))
            .setContentText(getString(R.string.focus_guard_notification_text, activityTitle))
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(NOTIFICATION_ID_FOCUS_GUARD, notification, android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else {
            startForeground(NOTIFICATION_ID_FOCUS_GUARD, notification)
        }

        scheduleNextCheck()
        return START_NOT_STICKY
    }

    private fun scheduleNextCheck() {
        handler.postDelayed({
            if (System.currentTimeMillis() >= endTimeMillis) {
                stopSelf()
                return@postDelayed
            }
            val pkg = getForegroundPackage(this)
            if (pkg != null && pkg != packageName && FOCUS_GUARD_RESTRICTED_PACKAGES.contains(pkg)) {
                val last = lastAlertByPackage[pkg] ?: 0L
                if (System.currentTimeMillis() - last >= ALERT_COOLDOWN_MS) {
                    lastAlertByPackage[pkg] = System.currentTimeMillis()
                    val appName = getAppDisplayName(this, pkg)
                    showAlertNotification(appName)
                }
            }
            scheduleNextCheck()
        }, CHECK_INTERVAL_MS)
    }

    private fun showAlertNotification(appName: String) {
        createChannelIfNeeded()
        val intent = packageManager.getLaunchIntentForPackage(packageName)?.apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        } ?: Intent(this, com.studyasist.MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pending = android.app.PendingIntent.getActivity(
            this,
            0,
            intent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(this, CHANNEL_ID_FOCUS_GUARD)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(getString(R.string.focus_guard_alert_title))
            .setContentText(resources.getString(R.string.focus_guard_alert_text, appName, activityTitle))
            .setContentIntent(pending)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        NotificationManagerCompat.from(this).notify(NOTIFICATION_ID_FOCUS_ALERT, notification)
    }

    private fun createChannelIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            CHANNEL_ID_FOCUS_GUARD,
            CHANNEL_NAME_FOCUS_GUARD,
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = getString(R.string.focus_guard_channel_description)
        }
        (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(channel)
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }

    companion object {
        private const val ALERT_COOLDOWN_MS = 2 * 60 * 1000L
        private const val CHECK_INTERVAL_MS = 5000L
    }
}
