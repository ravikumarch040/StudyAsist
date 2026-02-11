package com.studyasist.ui.goaldetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studyasist.data.local.entity.Goal
import com.studyasist.data.local.entity.GoalItem
import com.studyasist.data.repository.GoalRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GoalDetailUiState(
    val goal: Goal? = null,
    val items: List<GoalItem> = emptyList(),
    val daysRemaining: Long = 0L,
    val isLoading: Boolean = true
)

@HiltViewModel
class GoalDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val goalRepository: GoalRepository
) : ViewModel() {

    private val goalId: Long = checkNotNull(savedStateHandle["goalId"]) { "goalId required" }

    val uiState: StateFlow<GoalDetailUiState> = combine(
        goalRepository.getGoalFlow(goalId),
        goalRepository.getGoalItemsFlow(goalId)
    ) { goal, items ->
        val days = goal?.let { com.studyasist.util.daysUntil(it.examDate) } ?: 0L
        GoalDetailUiState(
            goal = goal,
            items = items,
            daysRemaining = days,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = GoalDetailUiState(isLoading = true)
    )

    fun deleteGoal(onDeleted: () -> Unit) {
        viewModelScope.launch {
            goalRepository.deleteGoal(goalId)
            onDeleted()
        }
    }
}
