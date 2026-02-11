package com.studyasist.ui.assessmentlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studyasist.data.local.entity.Assessment
import com.studyasist.data.repository.AssessmentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AssessmentListUiState(
    val assessments: List<AssessmentWithCount> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

data class AssessmentWithCount(
    val assessment: Assessment,
    val questionCount: Int
)

@HiltViewModel
class AssessmentListViewModel @Inject constructor(
    private val assessmentRepository: AssessmentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AssessmentListUiState())
    val uiState: StateFlow<AssessmentListUiState> = _uiState.asStateFlow()

    init {
        loadAssessments()
    }

    fun loadAssessments() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            assessmentRepository.getAllAssessments().collect { list ->
                val withCounts = list.map { a ->
                    AssessmentWithCount(
                        assessment = a,
                        questionCount = assessmentRepository.getQuestionCount(a.id)
                    )
                }
                _uiState.update {
                    it.copy(assessments = withCounts, isLoading = false)
                }
            }
        }
    }

    fun deleteAssessment(id: Long, onDeleted: () -> Unit) {
        viewModelScope.launch {
            assessmentRepository.deleteAssessment(id)
            onDeleted()
        }
    }
}
