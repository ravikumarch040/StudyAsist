package com.studyasist.ui.assessmentresult

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.qualifiers.ApplicationContext
import com.studyasist.data.repository.AssessmentRepository
import com.studyasist.data.repository.AttemptRepository
import com.studyasist.data.repository.ResultRepository
import com.studyasist.data.repository.SubjectChapter
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
    val subjectChapter: SubjectChapter? = null,
    val needsManualReview: Boolean = false,
    val manualFeedback: String? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

data class ResultDetailItem(
    val qaId: Long,
    val questionText: String,
    val correct: Boolean,
    val partialCredit: Boolean,
    val questionScore: Float,
    val userAnswer: String?,
    val modelAnswer: String,
    val feedback: String,
    val subject: String?,
    val chapter: String?
)

@HiltViewModel
class AssessmentResultViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    savedStateHandle: SavedStateHandle,
    private val resultRepository: ResultRepository,
    private val attemptRepository: AttemptRepository,
    private val assessmentRepository: AssessmentRepository
) : ViewModel() {

    private val attemptId: Long = checkNotNull(savedStateHandle["attemptId"]) { "attemptId required" }

    private val _uiState = MutableStateFlow(AssessmentResultUiState())
    val uiState: StateFlow<AssessmentResultUiState> = _uiState.asStateFlow()

    init {
        loadResult()
    }

    /**
     * Creates a retry assessment and returns the new assessment ID.
     * @param onlyWrongPartial If true, only include questions that were wrong or partial. If false, include all.
     * @return New assessment ID to run, or null if creation failed
     */
    suspend fun createRetryAssessment(onlyWrongPartial: Boolean): Long? {
        val attempt = attemptRepository.getAttempt(attemptId) ?: return null
        val qaIds = if (onlyWrongPartial) {
            _uiState.value.details
                .filter { !it.correct }
                .map { it.qaId }
                .distinct()
        } else {
            _uiState.value.details.map { it.qaId }.distinct()
        }
        if (qaIds.isEmpty()) return null
        val suffix = if (onlyWrongPartial) " (Retry weak)" else " (Retry)"
        return assessmentRepository.createRetryAssessment(attempt.assessmentId, qaIds, suffix)
    }

    fun toggleFlagForReview() {
        viewModelScope.launch {
            val current = _uiState.value.needsManualReview
            attemptRepository.setNeedsManualReview(attemptId, !current)
            _uiState.update { it.copy(needsManualReview = !current) }
        }
    }

    suspend fun getExportPdf(): ByteArray? = resultRepository.getExportPdfForAttempt(attemptId)

    private fun loadResult() {
        viewModelScope.launch {
            val result = resultRepository.getResult(attemptId)
            val attempt = attemptRepository.getAttempt(attemptId)
            if (result == null) {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = context.getString(com.studyasist.R.string.err_result_not_found))
                }
                return@launch
            }
            val details = parseDetails(result.detailsJson)
            val subjectChapter = resultRepository.getSubjectChapterForAttempt(attemptId)
            _uiState.update {
                it.copy(
                    score = result.score,
                    maxScore = result.maxScore,
                    percent = result.percent,
                    details = details,
                    subjectChapter = subjectChapter,
                    needsManualReview = attempt?.needsManualReview ?: false,
                    manualFeedback = result.manualFeedback,
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
                val gradeLevel = obj.optString("gradeLevel", "").lowercase()
                val correct = obj.optBoolean("correct")
                val partialCredit = gradeLevel == "partial"
                val questionScore = when (gradeLevel) {
                    "full" -> 1f
                    "partial" -> 0.5f
                    else -> 0f
                }
                ResultDetailItem(
                    qaId = obj.optLong("qaId"),
                    questionText = obj.optString("questionText", ""),
                    correct = correct,
                    partialCredit = partialCredit,
                    questionScore = questionScore,
                    userAnswer = obj.optString("userAnswer").takeIf { it.isNotBlank() },
                    modelAnswer = obj.optString("modelAnswer", ""),
                    feedback = obj.optString("feedback", ""),
                    subject = obj.optString("subject").takeIf { it.isNotBlank() },
                    chapter = obj.optString("chapter").takeIf { it.isNotBlank() }
                )
            }
        } catch (_: Exception) {
            emptyList()
        }
    }
}
