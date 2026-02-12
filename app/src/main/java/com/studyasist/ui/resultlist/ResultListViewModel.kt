package com.studyasist.ui.resultlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studyasist.data.repository.ResultListItem
import com.studyasist.data.repository.ResultRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ResultListUiState(
    val items: List<ResultListItem> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

@HiltViewModel
class ResultListViewModel @Inject constructor(
    private val resultRepository: ResultRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ResultListUiState())
    val uiState: StateFlow<ResultListUiState> = _uiState.asStateFlow()

    init {
        loadResults()
    }

    suspend fun getExportCsv(): String = resultRepository.getExportCsv()

    fun loadResults() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val items = resultRepository.getAllResultListItems()
                _uiState.update {
                    it.copy(items = items, isLoading = false)
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Failed to load results"
                    )
                }
            }
        }
    }
}
