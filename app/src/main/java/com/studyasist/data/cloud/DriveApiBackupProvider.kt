package com.studyasist.data.cloud

import android.accounts.Account
import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.ByteArrayContent
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.File
import com.google.api.services.drive.model.FileList
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.util.Collections
import javax.inject.Inject
import javax.inject.Singleton

private const val APP_FOLDER_NAME = "StudyAsist"
private const val MIME_JSON = "application/json"
private const val TAG = "DriveApiBackupProvider"

/**
 * Cloud backup provider using Google Drive API.
 * Requires user to sign in with Google and grant Drive scope.
 */
@Singleton
class DriveApiBackupProvider @Inject constructor(
    @ApplicationContext private val context: Context
) : CloudBackupProvider {

    override val target: CloudBackupTarget = CloudBackupTarget.GOOGLE_DRIVE

    private fun getSignedInAccountEmail(): String? {
        val account = GoogleSignIn.getLastSignedInAccount(context)
        return account?.email?.takeIf { it.isNotBlank() }
    }

    /** True if user has signed in with Google and granted Drive scope. */
    fun isSignedIn(): Boolean = getSignedInAccountEmail() != null

    private fun buildDriveService(accountEmail: String): Drive {
        val credential = GoogleAccountCredential.usingOAuth2(
            context,
            Collections.singleton(DriveScopes.DRIVE_FILE)
        )
        credential.selectedAccount = Account(accountEmail, "com.google")
        return Drive.Builder(
            NetHttpTransport(),
            GsonFactory.getDefaultInstance(),
            credential
        )
            .setApplicationName("StudyAsist")
            .build()
    }

    private suspend fun getOrCreateAppFolder(drive: Drive): String? = withContext(Dispatchers.IO) {
        // Find existing folder
        val query = "name = '$APP_FOLDER_NAME' and mimeType = 'application/vnd.google-apps.folder' and trashed = false"
        val result: FileList = drive.files().list()
            .setQ(query)
            .setSpaces("drive")
            .setFields("files(id, name)")
            .execute()
        val files = result.files
        if (files != null && files.isNotEmpty()) {
            return@withContext files[0].id
        }
        // Create folder
        val folderMetadata = File().apply {
            name = APP_FOLDER_NAME
            mimeType = "application/vnd.google-apps.folder"
        }
        val folder = drive.files().create(folderMetadata)
            .setFields("id")
            .execute()
        folder.id
    }

    override suspend fun writeBackup(json: String, filename: String): String? = withContext(Dispatchers.IO) {
        val accountEmail = getSignedInAccountEmail() ?: run {
            Log.e(TAG, "No signed-in Google account")
            return@withContext null
        }
        try {
            val drive = buildDriveService(accountEmail)
            val folderId = getOrCreateAppFolder(drive) ?: run {
                Log.e(TAG, "Failed to get or create app folder")
                return@withContext null
            }
            val fileMetadata = File().apply {
                name = filename
                parents = listOf(folderId)
            }
            val content = ByteArrayContent(MIME_JSON, json.toByteArray(Charsets.UTF_8))
            val file = drive.files().create(fileMetadata, content)
                .setFields("id, name")
                .execute()
            Log.d(TAG, "Backup uploaded: ${file.name} (id=${file.id})")
            file.id
        } catch (e: Exception) {
            Log.e(TAG, "Drive upload failed", e)
            null
        }
    }

    override suspend fun listBackups(): List<Pair<String, Uri>> = withContext(Dispatchers.IO) {
        val accountEmail = getSignedInAccountEmail() ?: return@withContext emptyList()
        try {
            val drive = buildDriveService(accountEmail)
            val folderId = getOrCreateAppFolder(drive) ?: return@withContext emptyList()
            val query = "'$folderId' in parents and mimeType = '$MIME_JSON' and trashed = false"
            val result: FileList = drive.files().list()
                .setQ(query)
                .setSpaces("drive")
                .setOrderBy("createdTime desc")
                .setFields("files(id, name, webViewLink)")
                .execute()
            val files = result.files ?: return@withContext emptyList()
            files
                .filter { it.name.endsWith(".json") }
                .map { it.name to Uri.parse("$DRIVE_BACKUP_URI_SCHEME://${it.id}") }
        } catch (e: Exception) {
            Log.e(TAG, "Drive list failed", e)
            emptyList()
        }
    }

    override suspend fun readBackup(uri: Uri): String? = withContext(Dispatchers.IO) {
        if (uri.scheme != DRIVE_BACKUP_URI_SCHEME) return@withContext null
        val fileId = uri.host ?: uri.path?.trimStart('/') ?: return@withContext null
        val accountEmail = getSignedInAccountEmail() ?: return@withContext null
        try {
            val drive = buildDriveService(accountEmail)
            ByteArrayOutputStream().use { out ->
                drive.files().get(fileId).executeMediaAndDownloadTo(out)
                String(out.toByteArray(), Charsets.UTF_8)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Drive read failed", e)
            null
        }
    }

    companion object {
        fun getSignInOptions(): GoogleSignInOptions =
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestScopes(
                    com.google.android.gms.common.api.Scope(DriveScopes.DRIVE_FILE)
                )
                .build()
    }
}
