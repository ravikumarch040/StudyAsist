package com.studyasist.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studyasist.data.local.entity.ActivityEntity
import com.studyasist.data.local.entity.TimetableEntity
import com.studyasist.data.repository.ActivityRepository
import com.studyasist.data.repository.SettingsRepository
import com.studyasist.data.repository.TimetableRepository
import com.studyasist.notification.NotificationScheduler
import com.studyasist.util.currentTimeMinutesFromMidnight
import com.studyasist.util.todayDayOfWeek
import dagger.hilt.android.lifecycle.HiltViewModel
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
    val activeTimetableId: Long? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val timetableRepository: TimetableRepository,
    private val activityRepository: ActivityRepository,
    private val settingsRepository: SettingsRepository,
    private val notificationScheduler: NotificationScheduler
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val activitiesFlow = settingsRepository.activeTimetableIdFlow.flatMapLatest { activeId ->
                if (activeId != null && activeId > 0) {
                    activityRepository.getActivitiesForTimetable(activeId).map { it }
                } else flowOf(emptyList())
            }
            combine(
                settingsRepository.activeTimetableIdFlow,
                timetableRepository.getAllTimetables(),
                activitiesFlow
            ) { activeId, timetables, allActivities ->
                val activeTimetable = if (activeId != null && activeId > 0) timetables.find { it.id == activeId } else null
                val todayActivities = if (activeTimetable != null) {
                    allActivities
                        .filter { it.dayOfWeek == todayDayOfWeek() }
                        .sortedBy { it.startTimeMinutes }
                } else emptyList()
                val now = currentTimeMinutesFromMidnight()
                val currentActivityId = todayActivities
                    .firstOrNull { now >= it.startTimeMinutes && now < it.endTimeMinutes }
                    ?.id
                HomeUiState(
                    activeTimetable = activeTimetable,
                    todayActivities = todayActivities,
                    currentActivityId = currentActivityId,
                    timetables = timetables,
                    activeTimetableId = activeId
                )
            }.collect { _uiState.value = it }
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
