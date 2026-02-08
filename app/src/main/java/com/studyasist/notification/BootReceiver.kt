package com.studyasist.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build

/**
 * Reschedules all activity notifications after device boot.
 * Will be wired to NotificationScheduler in a follow-up.
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        // TODO: Reschedule notifications from database via NotificationScheduler
    }
}
