package com.studyasist.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.studyasist.R
import com.studyasist.data.repository.BackupRepository
import com.studyasist.data.repository.SettingsRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * WorkManager worker for scheduled cloud backup.
 * Writes backup JSON to the user-selected cloud folder (Drive, Dropbox, etc.).
 */
@HiltWorker
class CloudBackupWorker @AssistedInject constructor(
    @Assisted context: android.content.Context,
    @Assisted params: WorkerParameters,
    private val backupRepository: BackupRepository,
    private val settingsRepository: SettingsRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val uriStr = settingsRepository.settingsFlow.first().cloudBackupFolderUri ?: run {
            Log.d(TAG, "Cloud backup: no folder set, skipping")
            return Result.success()
        }
        val isManual = inputData.getBoolean(KEY_MANUAL, false)
        if (!isManual && !settingsRepository.settingsFlow.first().cloudBackupAuto) {
            Log.d(TAG, "Cloud backup: auto backup disabled, skipping")
            return Result.success()
        }
        return try {
            val treeUri = Uri.parse(uriStr)
            val json = backupRepository.exportToJson()
            val dateFormat = SimpleDateFormat("yyyy-MM-dd_HHmmss", Locale.US)
            val filename = "StudyAsist_Backup_${dateFormat.format(Date())}.json"
            val docUri = DocumentsContract.createDocument(
                applicationContext.contentResolver,
                treeUri,
                "application/json",
                filename
            )
            if (docUri != null) {
                applicationContext.contentResolver.openOutputStream(docUri)?.use { os ->
                    os.write(json.toByteArray(Charsets.UTF_8))
                }
                Log.d(TAG, "Cloud backup: saved to $filename")
                settingsRepository.setCloudBackupLastSuccessMillis(System.currentTimeMillis())
                showCompletionNotification(applicationContext, success = true, errorMessage = null)
                Result.success()
            } else {
                Log.e(TAG, "Cloud backup: failed to create document")
                showCompletionNotification(applicationContext, success = false, errorMessage = "Could not create file in folder")
                Result.failure()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Cloud backup failed", e)
            val errMsg = e.message?.take(100) ?: e.javaClass.simpleName
            showCompletionNotification(applicationContext, success = false, errorMessage = errMsg)
            Result.failure()
        }
    }

    private fun showCompletionNotification(context: Context, success: Boolean, errorMessage: String?) {
        createChannel(context)
        val title = if (success) context.getString(R.string.cloud_backup_completed)
        else context.getString(R.string.cloud_backup_failed)
        val contentText = if (!success && !errorMessage.isNullOrBlank()) errorMessage else null
        val builder = NotificationCompat.Builder(context, CHANNEL_ID_CLOUD_BACKUP)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(true)
        if (contentText != null) builder.setContentText(contentText)
        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_CLOUD_BACKUP, builder.build())
    }

    private fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID_CLOUD_BACKUP,
                CHANNEL_NAME_CLOUD_BACKUP,
                NotificationManager.IMPORTANCE_LOW
            )
            (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                .createNotificationChannel(channel)
        }
    }

    companion object {
        private const val TAG = "CloudBackupWorker"
        const val KEY_MANUAL = "manual"
        fun manualWorkData() = workDataOf(KEY_MANUAL to true)
    }
}
