package com.studyasist.ui.goaldetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studyasist.data.local.entity.Goal
import com.studyasist.data.local.entity.GoalItem
import com.studyasist.data.repository.AssessmentRepository
import com.studyasist.data.repository.GoalDashboardMetrics
import com.studyasist.data.repository.QABankRepository
import com.studyasist.data.repository.GoalDashboardRepository
import com.studyasist.data.repository.GoalRepository
import com.studyasist.data.repository.RecentAttemptSummary
import com.studyasist.data.repository.SuggestedPracticeArea
import com.studyasist.data.repository.TrackPrediction
import com.studyasist.data.repository.SubjectChapterProgress
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
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
    val suggestedPractice: List<SuggestedPracticeArea> = emptyList(),
    val activityByDay: Map<Long, Int> = emptyMap(),
    val isLoading: Boolean = true
)

@HiltViewModel
class GoalDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val goalRepository: GoalRepository,
    private val dashboardRepository: GoalDashboardRepository,
    private val assessmentRepository: AssessmentRepository,
    private val qaBankRepository: QABankRepository
) : ViewModel() {

    private val goalId: Long = checkNotNull(savedStateHandle["goalId"]) { "goalId required" }

    private val _dashboardMetrics = MutableStateFlow<GoalDashboardMetrics?>(null)
    sealed class QuickPracticeEvent {
        data class Navigate(val assessmentId: Long) : QuickPracticeEvent()
        object NoQuestions : QuickPracticeEvent()
    }
    private val _quickPracticeEvent = MutableSharedFlow<QuickPracticeEvent>()
    val quickPracticeEvent: SharedFlow<QuickPracticeEvent> = _quickPracticeEvent.asSharedFlow()

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
            suggestedPractice = metrics?.suggestedPractice ?: emptyList(),
            activityByDay = metrics?.activityByDay ?: emptyMap(),
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

    /** Creates a 5-question assessment from the given subject/chapter and emits the assessment ID for navigation, or NoQuestions if none available. */
    fun startQuickPractice(subject: String, chapter: String?) {
        viewModelScope.launch {
            val subj = subject.takeIf { it.isNotBlank() }
            val ch = chapter?.takeIf { it.isNotBlank() }
            val count = qaBankRepository.countQA(subj, ch)
            if (count == 0) {
                _quickPracticeEvent.emit(QuickPracticeEvent.NoQuestions)
                return@launch
            }
            val assessmentId = assessmentRepository.createAssessmentFromRandom(
                title = "Practice: $subject${chapter?.let { " - $it" } ?: ""}",
                goalId = goalId,
                subject = subj,
                chapter = ch,
                totalTimeSeconds = 5 * 60,
                randomizeQuestions = true,
                count = minOf(5, count)
            )
            _quickPracticeEvent.emit(QuickPracticeEvent.Navigate(assessmentId))
        }
    }
}
