package com.studyasist.ui.manualreview

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.qualifiers.ApplicationContext
import com.studyasist.data.repository.AssessmentRepository
import com.studyasist.data.repository.AttemptRepository
import com.studyasist.data.repository.ResultRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ManualReviewItem(
    val attemptId: Long,
    val assessmentTitle: String,
    val attemptLabel: String,
    val percent: Float
)

data class ManualReviewListUiState(
    val items: List<ManualReviewItem> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

@HiltViewModel
class ManualReviewListViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val attemptRepository: AttemptRepository,
    private val resultRepository: ResultRepository,
    private val assessmentRepository: AssessmentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ManualReviewListUiState())
    val uiState: StateFlow<ManualReviewListUiState> = _uiState.asStateFlow()

    init {
        loadItems()
    }

    fun loadItems() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val attempts = attemptRepository.getNeedingManualReview()
                val items = attempts.mapNotNull { attempt ->
                    val assessment = assessmentRepository.getAssessment(attempt.assessmentId) ?: return@mapNotNull null
                    val attemptsForAssessment = attemptRepository.getAttempts(attempt.assessmentId)
                    val attemptIndex = attemptsForAssessment.indexOfFirst { it.id == attempt.id }
                    val attemptNum = if (attemptIndex >= 0) attemptIndex + 1 else 1
                    val result = resultRepository.getResult(attempt.id)
                    ManualReviewItem(
                        attemptId = attempt.id,
                        assessmentTitle = assessment.title,
                        attemptLabel = "Attempt $attemptNum",
                        percent = result?.percent ?: 0f
                    )
                }
                _uiState.update {
                    it.copy(items = items, isLoading = false)
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: context.getString(com.studyasist.R.string.err_failed_to_load)
                    )
                }
            }
        }
    }
}
