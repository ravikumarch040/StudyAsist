package com.studyasist.ui.qascan

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studyasist.data.qa.HeuristicQaParser
import com.studyasist.data.local.entity.QuestionType
import com.studyasist.data.repository.QABankRepository
import com.studyasist.data.repository.SettingsRepository
import com.studyasist.data.repository.GeminiRepository
import com.studyasist.util.extractTextFromImage
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONArray
import javax.inject.Inject

data class EditableQARow(
    val question: String,
    val answer: String,
    val type: QuestionType
)

data class QAScanUiState(
    val imageUri: Uri? = null,
    val rawOcrText: String = "",
    val parsedRows: List<EditableQARow> = emptyList(),
    val subject: String = "",
    val chapter: String = "",
    val distinctSubjects: List<String> = emptyList(),
    val distinctChapters: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val saveSuccess: Boolean = false,
    val useAiExtraction: Boolean = true
)

@HiltViewModel
class QAScanViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val qaBankRepository: QABankRepository,
    private val settingsRepository: SettingsRepository,
    private val geminiRepository: GeminiRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(QAScanUiState())
    val uiState: StateFlow<QAScanUiState> = _uiState.asStateFlow()

    init {
        loadDistinctValues()
        syncUseAiFromSettings()
    }

    private fun syncUseAiFromSettings() {
        viewModelScope.launch {
            val useCloud = settingsRepository.settingsFlow.first().useCloudForParsing
            _uiState.update { it.copy(useAiExtraction = useCloud) }
        }
    }

    private fun loadDistinctValues() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    distinctSubjects = qaBankRepository.getDistinctSubjects(),
                    distinctChapters = qaBankRepository.getDistinctChapters()
                )
            }
        }
    }

    fun loadChaptersForSubject(subject: String) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    distinctChapters = if (subject.isNotBlank()) {
                        qaBankRepository.getDistinctChaptersForSubject(subject)
                    } else {
                        qaBankRepository.getDistinctChapters()
                    }
                )
            }
        }
    }

    fun setImageUri(uri: Uri?) {
        _uiState.update {
            it.copy(imageUri = uri, errorMessage = null, rawOcrText = "", parsedRows = emptyList())
        }
    }

    fun extractAndParse() {
        val uri = _uiState.value.imageUri ?: run {
            _uiState.update { it.copy(errorMessage = context.getString(com.studyasist.R.string.add_image_first)) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val apiKey = settingsRepository.settingsFlow.first().geminiApiKey
            val useAi = _uiState.value.useAiExtraction && apiKey.isNotBlank()
            val result = if (useAi) extractWithAi(context, uri, apiKey) else extractWithOcr(context, uri)
            result.fold(
                onSuccess = { rows ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            parsedRows = rows.ifEmpty { listOf(EditableQARow("", "", QuestionType.SHORT)) }
                        )
                    }
                },
                onFailure = { e ->
                    if (useAi) {
                        extractWithOcr(context, uri).fold(
                            onSuccess = { fallbackRows ->
                                _uiState.update {
                                    it.copy(
                                        isLoading = false,
                                        parsedRows = fallbackRows.ifEmpty { listOf(EditableQARow("", "", QuestionType.SHORT)) },
                                        errorMessage = context.getString(com.studyasist.R.string.err_ai_extraction_fallback, e.message ?: "")
                                    )
                                }
                            },
                            onFailure = { ocrErr ->
                                _uiState.update {
                                    it.copy(
                                        isLoading = false,
                                        errorMessage = ocrErr.message ?: context.getString(com.studyasist.R.string.err_extraction_failed)
                                    )
                                }
                            }
                        )
                    } else {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = e.message ?: context.getString(com.studyasist.R.string.err_extraction_failed)
                            )
                        }
                    }
                }
            )
        }
    }

    private suspend fun extractWithAi(context: Context, uri: Uri, apiKey: String): Result<List<EditableQARow>> {
        val bytes = context.contentResolver.openInputStream(uri)?.readBytes() ?: return Result.failure(Exception(context.getString(com.studyasist.R.string.err_could_not_read_image)))
        val mimeType = when {
            uri.toString().contains(".png", ignoreCase = true) -> "image/png"
            else -> "image/jpeg"
        }
        val prompt = """
            You are a Q&A extraction assistant. Analyze this image which contains exam questions and answers.
            Extract ALL question-answer pairs. Preserve math expressions, fractions (e.g. 1/2, 1/4), and symbols exactly.
            Return ONLY a valid JSON array, no other text. Format:
            [{"question":"<question text>","answer":"<answer text>"},{"question":"...","answer":"..."},...]
            If a question has no visible answer, use "" for answer. Preserve order as in the image.
        """.trimIndent()
        val apiResult = geminiRepository.generateContentFromImage(apiKey, bytes, mimeType, prompt)
        return apiResult.mapCatching { jsonText ->
            val trimmed = jsonText.trim()
            val array = if (trimmed.startsWith("```")) {
                val content = trimmed.removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
                JSONArray(content)
            } else {
                JSONArray(trimmed)
            }
            (0 until array.length()).map { i ->
                val obj = array.getJSONObject(i)
                EditableQARow(
                    question = obj.optString("question", ""),
                    answer = obj.optString("answer", ""),
                    type = QuestionType.SHORT
                )
            }
        }
    }

    private suspend fun extractWithOcr(context: Context, uri: Uri): Result<List<EditableQARow>> {
        val ocrResult = extractTextFromImage(context, uri)
        return ocrResult.map { text ->
            HeuristicQaParser.parse(text).map { p ->
                EditableQARow(
                    question = p.question,
                    answer = p.answer,
                    type = p.type
                )
            }
        }
    }

    fun updateSubject(subject: String) {
        _uiState.update { it.copy(subject = subject) }
        loadChaptersForSubject(subject)
    }

    fun updateChapter(chapter: String) {
        _uiState.update { it.copy(chapter = chapter) }
    }

    fun updateRow(index: Int, question: String, answer: String, type: QuestionType) {
        _uiState.update { state ->
            if (index !in state.parsedRows.indices) return@update state
            val list = state.parsedRows.toMutableList()
            list[index] = EditableQARow(question = question, answer = answer, type = type)
            state.copy(parsedRows = list)
        }
    }

    fun addEmptyRow() {
        _uiState.update {
            it.copy(parsedRows = it.parsedRows + EditableQARow("", "", QuestionType.SHORT))
        }
    }

    fun removeRow(index: Int) {
        _uiState.update { state ->
            if (index !in state.parsedRows.indices) return@update state
            state.copy(parsedRows = state.parsedRows.filterIndexed { i, _ -> i != index })
        }
    }

    fun saveToBank(onSaved: () -> Unit) {
        val state = _uiState.value
        val valid = state.parsedRows.filter { it.question.isNotBlank() }
        if (valid.isEmpty()) {
            _uiState.update { it.copy(errorMessage = context.getString(com.studyasist.R.string.err_add_at_least_one_question)) }
            return
        }
        val subject = state.subject.takeIf { it.isNotBlank() }
        val chapter = state.chapter.takeIf { it.isNotBlank() }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val parsed = valid.map { row ->
                    QABankRepository.ParsedQA(
                        question = row.question,
                        answer = row.answer,
                        type = row.type
                    )
                }
                qaBankRepository.insertQABatch(
                    subject = subject,
                    chapter = chapter,
                    items = parsed
                )
                _uiState.update { it.copy(isLoading = false, saveSuccess = true) }
                onSaved()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: context.getString(com.studyasist.R.string.err_save_failed)
                    )
                }
            }
        }
    }

    fun setUseAiExtraction(use: Boolean) {
        _uiState.update { it.copy(useAiExtraction = use) }
    }

    /**
     * Re-runs extraction with AI on the current image.
     * Use when user has OCR/heuristic results and wants AI-improved parsing.
     */
    fun improveWithAi() {
        val uri = _uiState.value.imageUri ?: run {
            _uiState.update { it.copy(errorMessage = context.getString(com.studyasist.R.string.add_image_first)) }
            return
        }
        viewModelScope.launch {
            val settings = settingsRepository.settingsFlow.first()
            if (!settings.useCloudForParsing) {
                _uiState.update { it.copy(errorMessage = context.getString(com.studyasist.R.string.err_enable_ai_parsing)) }
                return@launch
            }
            val apiKey = settings.geminiApiKey
            if (apiKey.isBlank()) {
                _uiState.update { it.copy(errorMessage = context.getString(com.studyasist.R.string.err_gemini_api_key_for_ai)) }
                return@launch
            }
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            extractWithAi(context, uri, apiKey).fold(
                onSuccess = { rows ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            parsedRows = rows.ifEmpty { listOf(EditableQARow("", "", QuestionType.SHORT)) }
                        )
                    }
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = e.message ?: context.getString(com.studyasist.R.string.err_ai_improvement_failed)
                        )
                    }
                }
            )
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
