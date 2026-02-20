package com.studyasist.data.cloud

/**
 * Cloud backup target type.
 * - FOLDER: Use DocumentsProvider (user-picked folder). Works with Drive, Dropbox, etc.
 * - GOOGLE_DRIVE: Direct Google Drive API (planned, see docs/PLAN-DRIVE-BACKUP.md).
 */
enum class CloudBackupTarget {
    /** DocumentsProvider folder (current implementation). */
    FOLDER,

    /** Direct Google Drive API (optional, requires OAuth setup). */
    GOOGLE_DRIVE
}
