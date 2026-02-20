package com.studyasist.ui.onlineresources

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studyasist.data.repository.StudentClassRepository
import com.studyasist.data.repository.GeminiRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import javax.inject.Inject
import javax.inject.Named

data class OnlineResourceItem(
    val title: String,
    val url: String,
    val type: String // "book" | "sample_paper"
)

data class OnlineResourcesUiState(
    val standard: String = "",
    val board: String = "",
    val subject: String = "",
    val subjectOptions: List<String> = emptyList(),
    val resourceType: String = "books",
    val results: List<OnlineResourceItem> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val downloadMessage: String? = null
)

@HiltViewModel
class OnlineResourcesViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val studentClassRepository: StudentClassRepository,
    private val geminiRepository: GeminiRepository,
    private val settingsRepository: com.studyasist.data.repository.SettingsRepository,
    @Named("Download") private val okHttpClient: OkHttpClient
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnlineResourcesUiState())
    val uiState: StateFlow<OnlineResourcesUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val profile = studentClassRepository.getProfile()
            val subjects = studentClassRepository.getSubjectsForDropdown()
            _uiState.update {
                it.copy(
                    standard = profile.standard.ifEmpty { "10" },
                    board = profile.board.ifEmpty { "CBSE" },
                    subject = profile.subjects.firstOrNull() ?: subjects.firstOrNull() ?: "",
                    subjectOptions = subjects
                )
            }
        }
    }

    fun setStandard(s: String) { _uiState.update { it.copy(standard = s) } }
    fun setBoard(s: String) { _uiState.update { it.copy(board = s) } }
    fun setSubject(s: String) { _uiState.update { it.copy(subject = s) } }
    fun setResourceType(t: String) { _uiState.update { it.copy(resourceType = t) } }

    fun search() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, results = emptyList()) }
            val apiKey = settingsRepository.settingsFlow.first().geminiApiKey
            if (apiKey.isBlank()) {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = "API key required in Settings")
                }
                return@launch
            }
            val s = _uiState.value
            val typeQuery = if (s.resourceType == "sample_papers") "sample papers or important question bank" else "textbook or PDF"
            val prompt = """
                For an Indian student: Class ${s.standard}, Board ${s.board}, Subject: ${s.subject}.
                Suggest exactly 5 free online ${typeQuery} resources.
                Return ONLY a JSON array of objects, each with "title" and "url" and "type" (book or sample_paper).
                Example: [{"title":"NCERT Math Class 10","url":"https://example.com/ncert.pdf","type":"book"}]
                No other text.
            """.trimIndent()
            val result = geminiRepository.generateContent(apiKey, prompt)
            if (result.isSuccess) {
                val text = result.getOrNull() ?: ""
                val items = parseResourcesFromResponse(text)
                _uiState.update {
                    it.copy(isLoading = false, results = items)
                }
            } else {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = result.exceptionOrNull()?.message ?: "Search failed")
                }
            }
        }
    }

    private fun parseResourcesFromResponse(text: String): List<OnlineResourceItem> {
        return try {
            val json = text.substringAfter('[').substringBeforeLast(']')
            val parts = mutableListOf<OnlineResourceItem>()
            var i = 0
            while (i < json.length) {
                val start = json.indexOf('{', i)
                if (start < 0) break
                var depth = 1
                var j = start + 1
                while (j < json.length && depth > 0) {
                    when (json[j]) {
                        '{' -> depth++
                        '}' -> depth--
                    }
                    j++
                }
                val obj = json.substring(start, j)
                val title = """"title"\s*:\s*"([^"]+)"""".toRegex().find(obj)?.groupValues?.get(1) ?: ""
                val url = """"url"\s*:\s*"([^"]+)"""".toRegex().find(obj)?.groupValues?.get(1) ?: ""
                val type = """"type"\s*:\s*"([^"]+)"""".toRegex().find(obj)?.groupValues?.get(1) ?: "book"
                if (title.isNotBlank() && url.isNotBlank()) {
                    parts.add(OnlineResourceItem(title = title, url = url, type = type))
                }
                i = j
            }
            parts
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun clearDownloadMessage() {
        _uiState.update { it.copy(downloadMessage = null) }
    }

    fun download(item: OnlineResourceItem) {
        viewModelScope.launch {
            _uiState.update { it.copy(downloadMessage = null) }
            val result = withContext(Dispatchers.IO) {
                try {
                    val request = Request.Builder().url(item.url).build()
                    val response = okHttpClient.newCall(request).execute()
                    if (!response.isSuccessful) {
                        return@withContext "Download failed: ${response.code}"
                    }
                    val body = response.body ?: return@withContext "No response body"
                    val contentDisposition = response.header("Content-Disposition")
                    val suggestedName = contentDisposition?.substringAfter("filename=")?.trim('"', '\'')
                        ?: item.title.replace(Regex("[^a-zA-Z0-9.-]"), "_") + ".pdf"
                    val name = if (suggestedName.endsWith(".pdf") || suggestedName.contains(".")) suggestedName else "$suggestedName.pdf"

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        val values = ContentValues().apply {
                            put(MediaStore.Downloads.DISPLAY_NAME, name)
                            put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/StudyAsist")
                        }
                        val resolver = context.contentResolver
                        val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                            ?: return@withContext "Could not create file in Downloads"
                        resolver.openOutputStream(uri)?.use { os ->
                            body.byteStream().use { it.copyTo(os) }
                        }
                    } else {
                        val dir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "StudyAsist")
                        if (!dir.exists()) dir.mkdirs()
                        val file = File(dir, name)
                        file.outputStream().use { os ->
                            body.byteStream().use { it.copyTo(os) }
                        }
                    }
                    "Saved to Downloads/StudyAsist"
                } catch (e: Exception) {
                    "Download failed: ${e.message}"
                }
            }
            _uiState.update { it.copy(downloadMessage = result) }
        }
    }
}
