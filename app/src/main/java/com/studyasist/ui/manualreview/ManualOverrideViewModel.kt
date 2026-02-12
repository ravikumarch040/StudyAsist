package com.studyasist.ui.manualreview

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studyasist.data.repository.AttemptRepository
import com.studyasist.data.repository.ResultRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ManualOverrideUiState(
    val assessmentTitle: String = "",
    val currentScore: Float = 0f,
    val maxScore: Float = 0f,
    val currentPercent: Float = 0f,
    val manualFeedback: String? = null,
    val overrideScore: String = "",
    val overrideMaxScore: String = "",
    val overrideFeedback: String = "",
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

@HiltViewModel
class ManualOverrideViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val resultRepository: ResultRepository,
    private val attemptRepository: AttemptRepository,
    private val assessmentRepository: com.studyasist.data.repository.AssessmentRepository
) : ViewModel() {

    private val attemptId: Long = checkNotNull(savedStateHandle["attemptId"]) { "attemptId required" }

    private val _uiState = MutableStateFlow(ManualOverrideUiState())
    val uiState: StateFlow<ManualOverrideUiState> = _uiState.asStateFlow()

    init {
        loadResult()
    }

    private fun loadResult() {
        viewModelScope.launch {
            val result = resultRepository.getResult(attemptId)
            val attempt = attemptRepository.getAttempt(attemptId)
            if (result == null || attempt == null) {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = "Result not found")
                }
                return@launch
            }
            val assessment = assessmentRepository.getAssessment(attempt.assessmentId)
            _uiState.update {
                it.copy(
                    assessmentTitle = assessment?.title ?: "Assessment",
                    currentScore = result.score,
                    maxScore = result.maxScore,
                    currentPercent = result.percent,
                    manualFeedback = result.manualFeedback,
                    overrideScore = result.score.toString(),
                    overrideMaxScore = result.maxScore.toString(),
                    overrideFeedback = result.manualFeedback ?: "",
                    isLoading = false
                )
            }
        }
    }

    fun updateOverrideScore(value: String) {
        _uiState.update { it.copy(overrideScore = value) }
    }

    fun updateOverrideMaxScore(value: String) {
        _uiState.update { it.copy(overrideMaxScore = value) }
    }

    fun updateOverrideFeedback(value: String) {
        _uiState.update { it.copy(overrideFeedback = value) }
    }

    fun applyOverride(score: Float, maxScore: Float, manualFeedback: String?) {
        viewModelScope.launch {
            val result = resultRepository.getResult(attemptId) ?: return@launch
            val percent = if (maxScore > 0) (score / maxScore) * 100f else 0f
            val updated = result.copy(
                score = score,
                maxScore = maxScore,
                percent = percent,
                manualFeedback = manualFeedback
            )
            resultRepository.updateResult(updated)
            attemptRepository.setNeedsManualReview(attemptId, false)
        }
    }
}
