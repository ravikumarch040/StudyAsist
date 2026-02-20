package com.studyasist.ui.goallist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studyasist.data.local.entity.Goal
import com.studyasist.data.repository.GoalRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GoalListUiState(
    val goals: List<Goal> = emptyList(),
    val isLoading: Boolean = false
)

@HiltViewModel
class GoalListViewModel @Inject constructor(
    private val goalRepository: GoalRepository
) : ViewModel() {

    val uiState: StateFlow<GoalListUiState> = goalRepository
        .getAllActiveGoals()
        .map { goals -> GoalListUiState(goals = goals, isLoading = false) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = GoalListUiState()
        )

    fun deleteGoal(id: Long) {
        viewModelScope.launch {
            goalRepository.deleteGoal(id)
        }
    }
}
