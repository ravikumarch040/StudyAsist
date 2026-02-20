package com.studyasist.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studyasist.data.local.entity.ActivityEntity
import com.studyasist.data.local.entity.TimetableEntity
import com.studyasist.data.repository.ActivityRepository
import com.studyasist.data.repository.BadgeRepository
import com.studyasist.data.repository.EarnedBadge
import com.studyasist.data.repository.GoalDashboardRepository
import com.studyasist.data.repository.GoalRepository
import com.studyasist.data.repository.ResultListItem
import com.studyasist.data.repository.ResultRepository
import com.studyasist.data.repository.SettingsRepository
import com.studyasist.data.repository.StreakRepository
import com.studyasist.data.repository.TimetableRepository
import com.studyasist.notification.NotificationScheduler
import com.studyasist.util.currentTimeMinutesFromMidnight
import com.studyasist.util.daysUntil
import com.studyasist.util.todayDayOfWeek
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class GoalProgressSummary(
    val goalName: String,
    val percentComplete: Int,
    val daysUntilExam: Int
)

data class HomeUiState(
    val backupNotSetup: Boolean = false,
    val activeTimetable: TimetableEntity? = null,
    val todayActivities: List<ActivityEntity> = emptyList(),
    val currentActivityId: Long? = null,
    val timetables: List<TimetableEntity> = emptyList(),
    val activeTimetableId: Long? = null,
    val studyStreak: Int = 0,
    val earnedBadges: List<EarnedBadge> = emptyList(),
    val topResults: List<ResultListItem> = emptyList(),
    val activeGoalProgress: GoalProgressSummary? = null,
    val lastResult: ResultListItem? = null,
    val userName: String = ""
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val timetableRepository: TimetableRepository,
    private val activityRepository: ActivityRepository,
    private val settingsRepository: SettingsRepository,
    private val notificationScheduler: NotificationScheduler,
    private val streakRepository: StreakRepository,
    private val badgeRepository: BadgeRepository,
    private val resultRepository: ResultRepository,
    private val goalRepository: GoalRepository,
    private val goalDashboardRepository: GoalDashboardRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _streak = MutableStateFlow(0)
    private val _topResults = MutableStateFlow<List<ResultListItem>>(emptyList())
    private val _goalProgress = MutableStateFlow<GoalProgressSummary?>(null)
    private val _userName = MutableStateFlow("")

    init {
        viewModelScope.launch {
            _topResults.value = resultRepository.getTopResultListItems(5)
        }
        viewModelScope.launch {
            val streak = streakRepository.getCurrentStreak()
            _streak.value = streak
            badgeRepository.checkAndAwardStreakBadges(streak)
        }
        viewModelScope.launch {
            settingsRepository.userNameFlow.collect { _userName.value = it }
        }
        viewModelScope.launch {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            combine(
                settingsRepository.settingsFlow,
                goalRepository.getAllActiveGoals().map { it.isNotEmpty() },
                settingsRepository.backupCheckCacheFlow
            ) { settings, hasGoals, cached ->
                Triple(settings, hasGoals, cached)
            }.collect { (settings, hasGoals, cached) ->
                if (!hasGoals) {
                    _uiState.value = _uiState.value.copy(backupNotSetup = false)
                    return@collect
                }
                val notSetup = when (settings.cloudBackupTarget) {
                    "folder" -> settings.cloudBackupFolderUri.isNullOrBlank()
                    "google_drive" -> false
                    else -> settings.cloudBackupFolderUri.isNullOrBlank()
                }
                val today = dateFormat.format(Date())
                val (lastDate, _) = cached
                if (lastDate != today) {
                    settingsRepository.setLastBackupCheck(today, notSetup)
                }
                _uiState.value = _uiState.value.copy(backupNotSetup = notSetup)
            }
        }
        viewModelScope.launch {
            loadGoalProgress()
        }
        viewModelScope.launch {
            val activitiesFlow = settingsRepository.activeTimetableIdFlow.flatMapLatest { id ->
                if (id != null && id > 0) activityRepository.getActivitiesForTimetable(id).map { it }
                else flowOf(emptyList())
            }
            combine(
                combine(
                    settingsRepository.activeTimetableIdFlow,
                    timetableRepository.getAllTimetables(),
                    activitiesFlow,
                    _streak,
                    badgeRepository.getEarnedBadgesFlow()
                ) { activeId, timetables, allActivities, streak, badges ->
                    val active = if (activeId != null && activeId > 0) timetables.find { it.id == activeId } else null
                    val today = if (active != null) {
                        allActivities.filter { it.dayOfWeek == todayDayOfWeek() }.sortedBy { it.startTimeMinutes }
                    } else emptyList()
                    val now = currentTimeMinutesFromMidnight()
                    val currentId = today.firstOrNull { now >= it.startTimeMinutes && now < it.endTimeMinutes }?.id
                    HomeUiState(
                        activeTimetable = active,
                        todayActivities = today,
                        currentActivityId = currentId,
                        timetables = timetables,
                        activeTimetableId = activeId,
                        studyStreak = streak,
                        earnedBadges = badges,
                        topResults = emptyList()
                    )
                },
                _topResults,
                _goalProgress,
                _userName
            ) { partialState, topResults, goalProgress, userName ->
                partialState.copy(
                    topResults = topResults,
                    activeGoalProgress = goalProgress,
                    lastResult = topResults.firstOrNull(),
                    userName = userName,
                    backupNotSetup = _uiState.value.backupNotSetup
                )
            }.collect { _uiState.value = it }
        }
    }

    private suspend fun loadGoalProgress() {
        try {
            val goals = goalRepository.getActiveGoalsOnce()
            val firstGoal = goals.firstOrNull() ?: return
            val metrics = goalDashboardRepository.getDashboardMetrics(firstGoal.id)
            _goalProgress.value = GoalProgressSummary(
                goalName = firstGoal.name,
                percentComplete = metrics.percentComplete.toInt(),
                daysUntilExam = daysUntil(firstGoal.examDate).toInt()
            )
        } catch (_: Exception) { }
    }

    fun refreshStreak() {
        viewModelScope.launch {
            val streak = streakRepository.getCurrentStreak()
            _streak.value = streak
            badgeRepository.checkAndAwardStreakBadges(streak)
        }
    }

    fun refreshTopResults() {
        viewModelScope.launch {
            _topResults.value = resultRepository.getTopResultListItems(5)
        }
    }

    fun refreshDashboard() {
        refreshStreak()
        refreshTopResults()
        viewModelScope.launch { loadGoalProgress() }
    }

    fun setActiveTimetableId(id: Long) {
        viewModelScope.launch {
            settingsRepository.setActiveTimetableId(id)
            notificationScheduler.rescheduleAll()
        }
    }

    fun refreshToday() {
        viewModelScope.launch {
            val activeId = _uiState.value.activeTimetableId ?: return@launch
            val timetables = timetableRepository.getAllTimetables().first()
            val activeTimetable = timetables.find { it.id == activeId }
            val todayActivities = if (activeTimetable != null) {
                activityRepository.getActivitiesForDay(activeTimetable.id, todayDayOfWeek())
                    .sortedBy { it.startTimeMinutes }
            } else emptyList()
            val now = currentTimeMinutesFromMidnight()
            val currentActivityId = todayActivities
                .firstOrNull { now >= it.startTimeMinutes && now < it.endTimeMinutes }
                ?.id
            _uiState.value = _uiState.value.copy(
                activeTimetable = activeTimetable,
                todayActivities = todayActivities,
                currentActivityId = currentActivityId
            )
        }
    }
}
