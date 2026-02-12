package com.studyasist.ui.goaldetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studyasist.data.local.entity.Goal
import com.studyasist.data.local.entity.GoalItem
import com.studyasist.data.repository.GoalDashboardMetrics
import com.studyasist.data.repository.GoalDashboardRepository
import com.studyasist.data.repository.GoalRepository
import com.studyasist.data.repository.RecentAttemptSummary
import com.studyasist.data.repository.TrackPrediction
import com.studyasist.data.repository.SubjectChapterProgress
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
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
    val totalQuestions: Int = 0,
    val questionsPracticed: Int = 0,
    val percentComplete: Float = 0f,
    val recentAttempts: List<RecentAttemptSummary> = emptyList(),
    val subjectProgress: List<SubjectChapterProgress> = emptyList(),
    val trackPrediction: TrackPrediction? = null,
    val isLoading: Boolean = true
)

@HiltViewModel
class GoalDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val goalRepository: GoalRepository,
    private val dashboardRepository: GoalDashboardRepository
) : ViewModel() {

    private val goalId: Long = checkNotNull(savedStateHandle["goalId"]) { "goalId required" }

    private val _dashboardMetrics = MutableStateFlow<GoalDashboardMetrics?>(null)

    val uiState: StateFlow<GoalDetailUiState> = combine(
        goalRepository.getGoalFlow(goalId),
        goalRepository.getGoalItemsFlow(goalId),
        _dashboardMetrics
    ) { goal, items, metrics ->
        val days = goal?.let { com.studyasist.util.daysUntil(it.examDate) } ?: 0L
        GoalDetailUiState(
            goal = goal,
            items = items,
            daysRemaining = days,
            totalQuestions = metrics?.totalQuestions ?: 0,
            questionsPracticed = metrics?.questionsPracticed ?: 0,
            percentComplete = metrics?.percentComplete ?: 0f,
            recentAttempts = metrics?.recentAttempts ?: emptyList(),
            subjectProgress = metrics?.subjectProgress ?: emptyList(),
            trackPrediction = metrics?.trackPrediction,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = GoalDetailUiState(isLoading = true)
    )

    init {
        viewModelScope.launch {
            _dashboardMetrics.value = dashboardRepository.getDashboardMetrics(goalId)
        }
    }

    fun deleteGoal(onDeleted: () -> Unit) {
        viewModelScope.launch {
            goalRepository.deleteGoal(goalId)
            onDeleted()
        }
    }
}
