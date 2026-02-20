package com.studyasist.ui.downloadeddocs

import android.content.Context
import androidx.core.content.FileProvider
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

data class DownloadedFileItem(
    val displayName: String,
    val uri: String,
    val mimeType: String?
)

data class DownloadedDocsUiState(
    val files: List<DownloadedFileItem> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class DownloadedDocsViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(DownloadedDocsUiState())
    val uiState: StateFlow<DownloadedDocsUiState> = _uiState.asStateFlow()

    init {
        loadFiles()
    }

    fun refresh() {
        loadFiles()
    }

    private fun loadFiles() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = withContext(Dispatchers.IO) {
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        val files = mutableListOf<DownloadedFileItem>()
                        val projection = arrayOf(
                            MediaStore.Downloads._ID,
                            MediaStore.Downloads.DISPLAY_NAME,
                            MediaStore.Downloads.MIME_TYPE
                        )
                        val selection = "${MediaStore.Downloads.RELATIVE_PATH}=?"
                        val selectionArgs = arrayOf(Environment.DIRECTORY_DOWNLOADS + "/StudyAsist")
                        context.contentResolver.query(
                            MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                            projection,
                            selection,
                            selectionArgs,
                            "${MediaStore.Downloads.DATE_ADDED} DESC"
                        )?.use { cursor ->
                            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Downloads._ID)
                            val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Downloads.DISPLAY_NAME)
                            val mimeColumn = cursor.getColumnIndexOrThrow(MediaStore.Downloads.MIME_TYPE)
                            while (cursor.moveToNext()) {
                                val id = cursor.getLong(idColumn)
                                val name = cursor.getString(nameColumn) ?: "unknown"
                                val mime = cursor.getString(mimeColumn)
                                val uri = Uri.withAppendedPath(MediaStore.Downloads.EXTERNAL_CONTENT_URI, id.toString()).toString()
                                files.add(DownloadedFileItem(displayName = name, uri = uri, mimeType = mime))
                            }
                        }
                        files
                    } else {
                        val dir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "StudyAsist")
                        if (!dir.exists()) return@withContext emptyList<DownloadedFileItem>()
                        dir.listFiles()?.filter { it.isFile }?.sortedByDescending { it.lastModified() }?.map { file ->
                            val uri = try {
                                FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file).toString()
                            } catch (_: Exception) {
                                Uri.fromFile(file).toString()
                            }
                            DownloadedFileItem(
                                displayName = file.name,
                                uri = uri,
                                mimeType = null
                            )
                        } ?: emptyList()
                    }
                } catch (e: Exception) {
                    _uiState.update { it.copy(errorMessage = "Failed to load: ${e.message}") }
                    emptyList<DownloadedFileItem>()
                }
            }
            _uiState.update { it.copy(isLoading = false, files = result) }
        }
    }
}
