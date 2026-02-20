package com.studyasist.ui.studyplan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studyasist.data.repository.GeminiRepository
import com.studyasist.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StudyPlanViewModel @Inject constructor(
    private val geminiRepository: GeminiRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _planText = MutableStateFlow("")
    val planText: StateFlow<String> = _planText.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun generatePlan(goalName: String, subjects: List<String>, examDate: String) {
        viewModelScope.launch {
            _error.update { null }
            _isLoading.update { true }
            val apiKey = settingsRepository.settingsFlow.first().geminiApiKey
            val subjectsText = if (subjects.isNotEmpty()) subjects.joinToString(", ") else "general topics"
            val prompt = buildString {
                append("Create a personalized study plan as markdown. ")
                if (goalName.isNotBlank()) append("Goal: $goalName. ")
                append("Subjects: $subjectsText. ")
                if (examDate.isNotBlank()) append("Exam date: $examDate. ")
                append(
                    """
                    Include:
                    - Weekly breakdown with specific topics per week
                    - Daily study suggestions
                    - Revision tips and milestones
                    - Format using markdown (headers, bullet points)
                    """.trimIndent()
                )
            }
            val result = geminiRepository.generateContent(apiKey, prompt)
            _isLoading.update { false }
            result.fold(
                onSuccess = { text -> _planText.update { text }; _error.update { null } },
                onFailure = { e -> _error.update { e.message ?: "Failed to generate plan" } }
            )
        }
    }

    fun clearError() {
        _error.update { null }
    }
}
