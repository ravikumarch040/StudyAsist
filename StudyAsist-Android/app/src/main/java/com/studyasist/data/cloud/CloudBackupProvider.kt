package com.studyasist.data.cloud

import android.net.Uri

/**
 * Abstraction for writing backup JSON to a cloud location.
 * Implementations: DocumentsProvider (FOLDER), Google Drive API (GOOGLE_DRIVE).
 */
interface CloudBackupProvider {

    val target: CloudBackupTarget

    /**
     * Write backup JSON to cloud. Returns the URI or identifier of the created file, or null on failure.
     */
    suspend fun writeBackup(json: String, filename: String): String?

    /**
     * List existing backup files (name, uri). For Drive, uri scheme is "studyasist-drive" with path = fileId.
     */
    suspend fun listBackups(): List<Pair<String, Uri>>

    /**
     * Read backup content from uri. For Drive URIs (studyasist-drive scheme), fetches via API.
     */
    suspend fun readBackup(uri: Uri): String?
}

/** Scheme for Drive backup file URIs (path = fileId). */
const val DRIVE_BACKUP_URI_SCHEME = "studyasist-drive"
