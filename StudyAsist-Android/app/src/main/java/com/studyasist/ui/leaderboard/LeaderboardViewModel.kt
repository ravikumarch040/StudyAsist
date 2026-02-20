package com.studyasist.ui.leaderboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studyasist.data.api.LeaderboardItem
import com.studyasist.data.repository.LeaderboardRepository
import com.studyasist.data.repository.LeaderboardResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LeaderboardUiState(
    val items: List<LeaderboardItem> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val signInRequired: Boolean = false
)

@HiltViewModel
class LeaderboardViewModel @Inject constructor(
    private val leaderboardRepository: LeaderboardRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LeaderboardUiState())
    val uiState: StateFlow<LeaderboardUiState> = _uiState.asStateFlow()

    init {
        loadLeaderboard()
    }

    fun loadLeaderboard() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val r = leaderboardRepository.getTop(50)) {
                is LeaderboardResult.Success ->
                    _uiState.update {
                        it.copy(
                            items = r.data,
                            isLoading = false,
                            errorMessage = null,
                            signInRequired = false
                        )
                    }
                is LeaderboardResult.Error -> {
                    val signInRequired = r.message.contains("signed in", ignoreCase = true) ||
                        r.message.contains("auth", ignoreCase = true)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = r.message,
                            signInRequired = signInRequired
                        )
                    }
                }
            }
        }
    }
}
