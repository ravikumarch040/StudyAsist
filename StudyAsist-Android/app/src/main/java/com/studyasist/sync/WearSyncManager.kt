package com.studyasist.sync

import android.content.Context
import android.util.Log
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

private const val PATH_STREAK = "/studyasist/streak"
private const val KEY_STREAK_DAYS = "streak_days"
private const val TAG = "WearSyncManager"

/**
 * Syncs study streak to Wear OS via Wearable Data Layer.
 * Call when streak may have changed (e.g. after assessment, on Home load).
 */
@Singleton
class WearSyncManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    /**
     * Sends the current streak to connected Wear devices.
     * Safe to call frequently; runs on background dispatcher.
     */
    suspend fun syncStreak(streakDays: Int) = withContext(Dispatchers.IO) {
        try {
            val putDataMapReq = PutDataMapRequest.create(PATH_STREAK)
            putDataMapReq.dataMap.putInt(KEY_STREAK_DAYS, streakDays)
            val putDataReq = putDataMapReq.asPutDataRequest()
            Wearable.getDataClient(context).putDataItem(putDataReq).await()
            Log.d(TAG, "Synced streak: $streakDays days")
        } catch (e: Exception) {
            Log.w(TAG, "Failed to sync streak to Wear", e)
        }
    }
}
