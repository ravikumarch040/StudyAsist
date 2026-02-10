package com.studyasist.ui.explain

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studyasist.data.repository.GeminiRepository
import com.studyasist.data.repository.SettingsRepository
import com.studyasist.util.LanguageOptions
import com.studyasist.util.extractTextFromImage
import com.studyasist.util.speakText
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import javax.inject.Inject

data class ExplainUiState(
    val inputText: String = "",
    val explanation: String = "",
    val imageUri: Uri? = null,
    val isExtractingFromImage: Boolean = false,
    val isLoading: Boolean = false,
    val isSpeaking: Boolean = false,
    val errorMessage: String? = null,
    val selectedLanguageCode: String = "en"
)

@HiltViewModel
class ExplainViewModel @Inject constructor(
    @ApplicationContext private val context: android.content.Context,
    private val geminiRepository: GeminiRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExplainUiState())
    val uiState: StateFlow<ExplainUiState> = _uiState.asStateFlow()

    val languageOptions = LanguageOptions.LIST

    fun setInputText(text: String) {
        _uiState.update { it.copy(inputText = text, errorMessage = null) }
    }

    fun setLanguage(code: String) {
        _uiState.update { it.copy(selectedLanguageCode = code) }
    }

    fun setImageUri(uri: Uri?) {
        if (uri == null) {
            _uiState.update { it.copy(imageUri = null) }
            return
        }
        _uiState.update {
            it.copy(imageUri = uri, isExtractingFromImage = true, errorMessage = null)
        }
        viewModelScope.launch {
            val result = extractTextFromImage(context, uri)
            val fallbackText = _uiState.value.inputText
            _uiState.update {
                it.copy(
                    isExtractingFromImage = false,
                    inputText = result.getOrElse { fallbackText },
                    errorMessage = result.exceptionOrNull()?.message
                )
            }
        }
    }

    fun clearImage() {
        _uiState.update { it.copy(imageUri = null) }
    }

    fun explain() {
        val text = _uiState.value.inputText.trim()
        if (text.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Enter or capture text to explain") }
            return
        }
        viewModelScope.launch {
            val apiKey = settingsRepository.settingsFlow.first().geminiApiKey
            val lang = _uiState.value.selectedLanguageCode
            val langName = languageOptions.find { option -> option.first == lang }?.second ?: lang
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = geminiRepository.generateContent(
                apiKey,
                "Explain the following in simple terms. Write the explanation in $langName. Do not add any preamble.\n\n$text"
            )
            _uiState.update {
                it.copy(
                    isLoading = false,
                    explanation = result.getOrElse { "" },
                    errorMessage = result.exceptionOrNull()?.message
                )
            }
        }
    }

    fun speakExplanation() {
        val text = _uiState.value.explanation
        if (text.isBlank()) return
        _uiState.update { it.copy(isSpeaking = true) }
        viewModelScope.launch {
            val voiceName = settingsRepository.settingsFlow.first().ttsVoiceName
            val locale = Locale.forLanguageTag(_uiState.value.selectedLanguageCode)
            withContext(Dispatchers.Main) {
                speakText(context, text, locale, voiceName) {
                    viewModelScope.launch { _uiState.update { it.copy(isSpeaking = false) } }
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
}
