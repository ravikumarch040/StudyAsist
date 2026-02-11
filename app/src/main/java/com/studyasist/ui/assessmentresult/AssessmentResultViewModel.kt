package com.studyasist.ui.assessmentresult

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studyasist.data.repository.ResultRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AssessmentResultUiState(
    val score: Float = 0f,
    val maxScore: Float = 0f,
    val percent: Float = 0f,
    val details: List<ResultDetailItem> = emptyList(),
    val assessmentTitle: String = "",
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

data class ResultDetailItem(
    val qaId: Long,
    val questionText: String,
    val correct: Boolean,
    val userAnswer: String?,
    val modelAnswer: String,
    val feedback: String
)

@HiltViewModel
class AssessmentResultViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val resultRepository: ResultRepository
) : ViewModel() {

    private val attemptId: Long = checkNotNull(savedStateHandle["attemptId"]) { "attemptId required" }

    private val _uiState = MutableStateFlow(AssessmentResultUiState())
    val uiState: StateFlow<AssessmentResultUiState> = _uiState.asStateFlow()

    init {
        loadResult()
    }

    private fun loadResult() {
        viewModelScope.launch {
            val result = resultRepository.getResult(attemptId)
            if (result == null) {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = "Result not found")
                }
                return@launch
            }
            val details = parseDetails(result.detailsJson)
            _uiState.update {
                it.copy(
                    score = result.score,
                    maxScore = result.maxScore,
                    percent = result.percent,
                    details = details,
                    isLoading = false
                )
            }
        }
    }

    private fun parseDetails(json: String): List<ResultDetailItem> {
        return try {
            val arr = org.json.JSONArray(json)
            (0 until arr.length()).mapNotNull { i ->
                val obj = arr.getJSONObject(i)
                ResultDetailItem(
                    qaId = obj.optLong("qaId"),
                    questionText = obj.optString("questionText", ""),
                    correct = obj.optBoolean("correct"),
                    userAnswer = obj.optString("userAnswer").takeIf { it.isNotBlank() },
                    modelAnswer = obj.optString("modelAnswer", ""),
                    feedback = obj.optString("feedback", "")
                )
            }
        } catch (_: Exception) {
            emptyList()
        }
    }
}
