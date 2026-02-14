package com.studyasist.ui.assessmentedit

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studyasist.data.local.entity.Assessment
import dagger.hilt.android.qualifiers.ApplicationContext
import com.studyasist.data.repository.AssessmentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AssessmentEditUiState(
    val title: String = "",
    val timeLimitMinutes: Int = 30,
    val randomize: Boolean = true,
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

@HiltViewModel
class AssessmentEditViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    savedStateHandle: SavedStateHandle,
    private val assessmentRepository: AssessmentRepository
) : ViewModel() {

    private val assessmentId: Long = checkNotNull(savedStateHandle["assessmentId"]) { "assessmentId required" }

    private val _uiState = MutableStateFlow(AssessmentEditUiState())
    val uiState: StateFlow<AssessmentEditUiState> = _uiState.asStateFlow()

    init {
        loadAssessment()
    }

    private fun loadAssessment() {
        viewModelScope.launch {
            val assessment = assessmentRepository.getAssessment(assessmentId)
            if (assessment != null) {
                _uiState.update {
                    it.copy(
                        title = assessment.title,
                        timeLimitMinutes = assessment.totalTimeSeconds / 60,
                        randomize = assessment.randomizeQuestions,
                        isLoading = false
                    )
                }
            } else {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = context.getString(com.studyasist.R.string.err_assessment_not_found))
                }
            }
        }
    }

    fun setTitle(title: String) {
        _uiState.update { it.copy(title = title, errorMessage = null) }
    }

    fun setTimeLimitMinutes(minutes: Int) {
        _uiState.update { it.copy(timeLimitMinutes = minutes.coerceIn(1, 180), errorMessage = null) }
    }

    fun setRandomize(randomize: Boolean) {
        _uiState.update { it.copy(randomize = randomize) }
    }

    fun save(onSaved: () -> Unit) {
        val state = _uiState.value
        if (state.title.isBlank()) {
            _uiState.update { it.copy(errorMessage = context.getString(com.studyasist.R.string.err_enter_title)) }
            return
        }
        viewModelScope.launch {
            val assessment = assessmentRepository.getAssessment(assessmentId)
                ?: run {
                    _uiState.update { it.copy(errorMessage = context.getString(com.studyasist.R.string.err_assessment_not_found)) }
                    return@launch
                }
            assessmentRepository.updateAssessment(
                assessment.copy(
                    title = state.title,
                    totalTimeSeconds = state.timeLimitMinutes * 60,
                    randomizeQuestions = state.randomize
                )
            )
            onSaved()
        }
    }
}
