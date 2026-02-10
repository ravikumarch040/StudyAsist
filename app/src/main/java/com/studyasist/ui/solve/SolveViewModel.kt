package com.studyasist.ui.solve

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

data class SolveUiState(
    val problemText: String = "",
    val solution: String = "",
    val imageUri: Uri? = null,
    val isExtractingFromImage: Boolean = false,
    val isLoading: Boolean = false,
    val isSpeaking: Boolean = false,
    val errorMessage: String? = null,
    val selectedLanguageCode: String = "en"
)

@HiltViewModel
class SolveViewModel @Inject constructor(
    @ApplicationContext private val context: android.content.Context,
    private val geminiRepository: GeminiRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SolveUiState())
    val uiState: StateFlow<SolveUiState> = _uiState.asStateFlow()

    val languageOptions = LanguageOptions.LIST

    fun setProblemText(text: String) {
        _uiState.update { it.copy(problemText = text, errorMessage = null) }
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
            val fallbackText = _uiState.value.problemText
            _uiState.update {
                it.copy(
                    isExtractingFromImage = false,
                    problemText = result.getOrElse { fallbackText },
                    errorMessage = result.exceptionOrNull()?.message
                )
            }
        }
    }

    fun clearImage() {
        _uiState.update { it.copy(imageUri = null) }
    }

    fun solve() {
        val text = _uiState.value.problemText.trim()
        if (text.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Enter or capture a problem to solve") }
            return
        }
        viewModelScope.launch {
            val apiKey = settingsRepository.settingsFlow.first().geminiApiKey
            val lang = _uiState.value.selectedLanguageCode
            val langName = languageOptions.find { it.first == lang }?.second ?: lang
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = geminiRepository.generateContent(
                apiKey,
                """Solve this problem. Write the solution in $langName. Use EXACTLY this format:

1) QUICK STEPS (for students to quickly check): List only simple steps, one per line, with calculation and result. Example format:
   Step 1: 2 + 3 = 5
   Step 2: 5 × 4 = 20
   Answer: 20

2) After the quick steps, write a line with only: ---

3) FULL EXPLANATION: Then write a detailed explanation of each step (definitions, why each step is done, etc.).

Do not add any preamble before the quick steps.

Problem:
$text"""
            )
            _uiState.update {
                it.copy(
                    isLoading = false,
                    solution = result.getOrElse { "" },
                    errorMessage = result.exceptionOrNull()?.message
                )
            }
        }
    }

    fun speakSolution() {
        val text = _uiState.value.solution
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
