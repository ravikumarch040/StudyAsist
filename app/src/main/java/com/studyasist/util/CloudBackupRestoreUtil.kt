package com.studyasist.util

import android.content.ContentResolver
import android.net.Uri
import android.provider.DocumentsContract
import java.io.InputStreamReader

/**
 * Lists backup JSON files in the user's cloud backup folder (tree URI).
 * Returns (displayName, documentUri) pairs for .json files, sorted by name descending (newest first).
 */
fun listBackupFilesInFolder(contentResolver: ContentResolver, treeUriString: String): List<Pair<String, Uri>> {
    if (treeUriString.isBlank()) return emptyList()
    val treeUri = Uri.parse(treeUriString)
    return try {
        val docId = DocumentsContract.getTreeDocumentId(treeUri)
        val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, docId)
        val projection = arrayOf(
            DocumentsContract.Document.COLUMN_DOCUMENT_ID,
            DocumentsContract.Document.COLUMN_DISPLAY_NAME
        )
        contentResolver.query(childrenUri, projection, null, null, null)?.use { cursor ->
            val idIdx = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DOCUMENT_ID)
            val nameIdx = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
            if (idIdx < 0 || nameIdx < 0) return@use emptyList()
            val list = mutableListOf<Pair<String, Uri>>()
            while (cursor.moveToNext()) {
                val name = cursor.getString(nameIdx) ?: continue
                if (!name.endsWith(".json")) continue
                val childId = cursor.getString(idIdx) ?: continue
                val docUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, childId)
                list.add(name to docUri)
            }
            list.sortedByDescending { it.first }
        } ?: emptyList()
    } catch (e: Exception) {
        emptyList()
    }
}

/**
 * Reads the content of a document URI as text (for JSON backup).
 */
fun readDocumentAsText(contentResolver: ContentResolver, docUri: Uri): String? = try {
    contentResolver.openInputStream(docUri)?.use { InputStreamReader(it).readText() }
} catch (e: Exception) {
    null
}
