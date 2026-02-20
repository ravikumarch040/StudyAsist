package com.studyasist.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.runBlocking
import java.util.concurrent.Executors

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        val appContext = context.applicationContext
        val pendingResult = goAsync()
        val executor = Executors.newSingleThreadExecutor()
        executor.execute {
            try {
                val entryPoint = EntryPointAccessors.fromApplication(
                    appContext,
                    NotificationSchedulerEntryPoint::class.java
                )
                val scheduler = entryPoint.getNotificationScheduler()
                runBlocking { scheduler.rescheduleAll() }
                Log.d(TAG, "Boot: rescheduleAll() completed")
            } catch (e: Exception) {
                Log.e(TAG, "Boot: rescheduleAll() failed", e)
            } finally {
                pendingResult.finish()
                executor.shutdown()
            }
        }
    }

    companion object {
        private const val TAG = "BootReceiver"
    }
}
