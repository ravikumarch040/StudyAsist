package com.studyasist.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studyasist.data.local.entity.ActivityEntity
import com.studyasist.data.local.entity.TimetableEntity
import com.studyasist.data.repository.ActivityRepository
import com.studyasist.data.repository.BadgeRepository
import com.studyasist.data.repository.EarnedBadge
import com.studyasist.data.repository.SettingsRepository
import com.studyasist.data.repository.StreakRepository
import com.studyasist.data.repository.TimetableRepository
import com.studyasist.notification.NotificationScheduler
import com.studyasist.util.currentTimeMinutesFromMidnight
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
import javax.inject.Inject

data class HomeUiState(
    val activeTimetable: TimetableEntity? = null,
    val todayActivities: List<ActivityEntity> = emptyList(),
    val currentActivityId: Long? = null,
    val timetables: List<TimetableEntity> = emptyList(),
    val activeTimetableId: Long? = null,
    val studyStreak: Int = 0,
    val earnedBadges: List<EarnedBadge> = emptyList()
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val timetableRepository: TimetableRepository,
    private val activityRepository: ActivityRepository,
    private val settingsRepository: SettingsRepository,
    private val notificationScheduler: NotificationScheduler,
    private val streakRepository: StreakRepository,
    private val badgeRepository: BadgeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _streak = MutableStateFlow(0)

    init {
        viewModelScope.launch {
            val streak = streakRepository.getCurrentStreak()
            _streak.value = streak
            badgeRepository.checkAndAwardStreakBadges(streak)
        }
        viewModelScope.launch {
            val activitiesFlow = settingsRepository.activeTimetableIdFlow.flatMapLatest { id ->
                if (id != null && id > 0) activityRepository.getActivitiesForTimetable(id).map { it }
                else flowOf(emptyList())
            }
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
                    earnedBadges = badges
                )
            }.collect { _uiState.value = it }
        }
    }

    fun refreshStreak() {
        viewModelScope.launch {
            val streak = streakRepository.getCurrentStreak()
            _streak.value = streak
            badgeRepository.checkAndAwardStreakBadges(streak)
        }
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
