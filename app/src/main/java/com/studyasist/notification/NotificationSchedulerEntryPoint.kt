package com.studyasist.notification

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/** Entry point for obtaining [NotificationScheduler] from non-injected code (e.g. BootReceiver). */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface NotificationSchedulerEntryPoint {
    fun getNotificationScheduler(): NotificationScheduler
}
