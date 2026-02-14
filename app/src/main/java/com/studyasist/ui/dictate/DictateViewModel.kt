package com.studyasist.ui.dictate

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studyasist.data.local.dao.StudyToolHistoryDao
import com.studyasist.data.local.entity.StudyToolHistoryEntity
import com.studyasist.data.repository.SettingsRepository
import com.studyasist.util.extractTextFromImage
import com.studyasist.util.speakText
import com.studyasist.util.speakTextWithProgress
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import com.studyasist.util.LanguageOptions
import java.util.Locale
import javax.inject.Inject

data class DictateUiState(
    val imageUri: Uri? = null,
    val extractedText: String = "",
    val isLoading: Boolean = false,
    val isSpeaking: Boolean = false,
    val errorMessage: String? = null,
    val selectedLanguageCode: String = "en",
    val recentItems: List<StudyToolHistoryEntity> = emptyList(),
    val highlightedSentenceIndex: Int = -1,
    val sentences: List<String> = emptyList()
)

@HiltViewModel
class DictateViewModel @Inject constructor(
    @ApplicationContext private val context: android.content.Context,
    private val settingsRepository: SettingsRepository,
    private val studyToolHistoryDao: StudyToolHistoryDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(DictateUiState())
    val uiState: StateFlow<DictateUiState> = _uiState.asStateFlow()

    val languageOptions = LanguageOptions.LIST

    init {
        viewModelScope.launch {
            val recent = studyToolHistoryDao.getRecentByTool("dictate")
            val savedLang = settingsRepository.getDictateLanguage()
            _uiState.update { it.copy(recentItems = recent, selectedLanguageCode = savedLang) }
        }
    }

    fun setImageUri(uri: Uri?) {
        _uiState.update { it.copy(imageUri = uri, errorMessage = null, extractedText = "") }
    }

    fun setLanguage(code: String) {
        _uiState.update { it.copy(selectedLanguageCode = code) }
        viewModelScope.launch { settingsRepository.setDictateLanguage(code) }
    }

    fun extractText() {
        val uri = _uiState.value.imageUri ?: run {
            _uiState.update { it.copy(errorMessage = context.getString(com.studyasist.R.string.add_image_first)) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = extractTextFromImage(context, uri)
            val text = result.getOrElse { "" }
            _uiState.update {
                it.copy(
                    isLoading = false,
                    extractedText = text,
                    errorMessage = result.exceptionOrNull()?.message
                )
            }
            if (text.isNotBlank()) {
                studyToolHistoryDao.insert(
                    StudyToolHistoryEntity(toolType = "dictate", inputText = text.take(2000), usedAt = System.currentTimeMillis())
                )
                val recent = studyToolHistoryDao.getRecentByTool("dictate")
                val sentences = text.trim().split(Regex("(?<=[.!?])\\s+")).filter { it.isNotBlank() }
                _uiState.update {
                    it.copy(recentItems = recent, isSpeaking = true, sentences = sentences, highlightedSentenceIndex = -1)
                }
                val voiceName = settingsRepository.settingsFlow.first().ttsVoiceName
                val locale = Locale.forLanguageTag(_uiState.value.selectedLanguageCode)
                withContext(Dispatchers.Main) {
                    speakTextWithProgress(
                        context = context,
                        text = text,
                        locale = locale,
                        voiceName = voiceName,
                        onSentenceProgress = { idx, _ ->
                            viewModelScope.launch { _uiState.update { it.copy(highlightedSentenceIndex = idx) } }
                        }
                    ) {
                        viewModelScope.launch {
                            _uiState.update { it.copy(isSpeaking = false, highlightedSentenceIndex = -1) }
                        }
                    }
                }
            }
        }
    }

    fun speak() {
        val text = _uiState.value.extractedText
        if (text.isBlank()) {
            _uiState.update { it.copy(errorMessage = context.getString(com.studyasist.R.string.err_no_text_to_read)) }
            return
        }
        val sentences = text.trim().split(Regex("(?<=[.!?])\\s+")).filter { it.isNotBlank() }
        _uiState.update {
            it.copy(
                isSpeaking = true,
                sentences = sentences,
                highlightedSentenceIndex = -1
            )
        }
        viewModelScope.launch {
            val voiceName = settingsRepository.settingsFlow.first().ttsVoiceName
            val locale = Locale.forLanguageTag(_uiState.value.selectedLanguageCode)
            withContext(Dispatchers.Main) {
                speakTextWithProgress(
                    context = context,
                    text = text,
                    locale = locale,
                    voiceName = voiceName,
                    onSentenceProgress = { idx, _ ->
                        viewModelScope.launch { _uiState.update { it.copy(highlightedSentenceIndex = idx) } }
                    }
                ) {
                    viewModelScope.launch {
                        _uiState.update {
                            it.copy(isSpeaking = false, highlightedSentenceIndex = -1)
                        }
                    }
                }
            }
        }
    }

    fun stopSpeaking() {
        viewModelScope.launch {
            withContext(Dispatchers.Main) { com.studyasist.util.stopSpeaking() }
            _uiState.update { it.copy(isSpeaking = false) }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun selectRecent(text: String) {
        _uiState.update { it.copy(extractedText = text) }
    }

    fun clearRecent() {
        viewModelScope.launch {
            studyToolHistoryDao.deleteByTool("dictate")
            _uiState.update { it.copy(recentItems = emptyList()) }
        }
    }
}
