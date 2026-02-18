package com.studyasist.ui.resultlist

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.qualifiers.ApplicationContext
import com.studyasist.data.repository.ResultListItem
import com.studyasist.data.repository.ResultRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class ResultSortOrder {
    DATE_DESC,
    DATE_ASC,
    SCORE_DESC,
    SCORE_ASC
}

data class ResultListUiState(
    val items: List<ResultListItem> = emptyList(),
    val sortedFilteredItems: List<ResultListItem> = emptyList(),
    val sortOrder: ResultSortOrder = ResultSortOrder.DATE_DESC,
    val filterAssessmentId: Long? = null,
    val distinctAssessments: List<Pair<Long, String>> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

@HiltViewModel
class ResultListViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val resultRepository: ResultRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ResultListUiState())
    val uiState: StateFlow<ResultListUiState> = _uiState.asStateFlow()

    init {
        loadResults()
    }

    suspend fun getExportCsv(): String = resultRepository.getExportCsv()

    suspend fun getExportPdf(): ByteArray = resultRepository.getExportPdf()

    suspend fun getExportExcel(): ByteArray = resultRepository.getExportExcel()

    fun setSortOrder(order: ResultSortOrder) {
        _uiState.update { applySortFilter(it.copy(sortOrder = order)) }
    }

    fun setFilterAssessment(assessmentId: Long?) {
        _uiState.update { applySortFilter(it.copy(filterAssessmentId = assessmentId)) }
    }

    fun loadResults() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val items = resultRepository.getAllResultListItems()
                val distinct = items.distinctBy { it.assessmentId }.map { it.assessmentId to it.assessmentTitle }.sortedBy { it.second }
                _uiState.update {
                    applySortFilter(it.copy(
                        items = items,
                        distinctAssessments = distinct,
                        isLoading = false
                    ))
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: context.getString(com.studyasist.R.string.err_failed_to_load_results)
                    )
                }
            }
        }
    }

    private fun applySortFilter(state: ResultListUiState): ResultListUiState {
        var list = state.items
        if (state.filterAssessmentId != null) {
            list = list.filter { it.assessmentId == state.filterAssessmentId }
        }
        val sorted = when (state.sortOrder) {
            ResultSortOrder.DATE_DESC -> list.sortedByDescending { it.startedAt }
            ResultSortOrder.DATE_ASC -> list.sortedBy { it.startedAt }
            ResultSortOrder.SCORE_DESC -> list.sortedByDescending { it.percent }
            ResultSortOrder.SCORE_ASC -> list.sortedBy { it.percent }
        }
        return state.copy(sortedFilteredItems = sorted)
    }
}
