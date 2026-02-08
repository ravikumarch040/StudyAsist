package com.studyasist.ui.timetabledetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studyasist.data.local.entity.ActivityEntity
import com.studyasist.data.local.entity.ActivityType
import com.studyasist.data.local.entity.TimetableEntity
import com.studyasist.data.repository.ActivityRepository
import com.studyasist.data.repository.TimetableRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class TimetableDetailUiState(
    val timetable: TimetableEntity? = null,
    val activities: List<ActivityEntity> = emptyList(),
    val filterType: ActivityType? = null,
    val selectedDay: Int = 1
) {
    val filteredActivities: List<ActivityEntity>
        get() = if (filterType == null) activities
        else activities.filter { it.type == filterType }
}

@HiltViewModel
class TimetableDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val timetableRepository: TimetableRepository,
    private val activityRepository: ActivityRepository
) : ViewModel() {

    private val timetableId: Long = checkNotNull(savedStateHandle["timetableId"]) { "timetableId required" }

    private val _filterType = MutableStateFlow<ActivityType?>(null)
    private val _selectedDay = MutableStateFlow(1)

    val uiState: StateFlow<TimetableDetailUiState> = combine(
        timetableRepository.getTimetableFlow(timetableId),
        activityRepository.getActivitiesForTimetable(timetableId),
        _filterType,
        _selectedDay
    ) { timetable, activities, filterType, selectedDay ->
        TimetableDetailUiState(
            timetable = timetable,
            activities = activities,
            filterType = filterType,
            selectedDay = selectedDay
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TimetableDetailUiState()
    )

    fun setFilterType(type: ActivityType?) {
        _filterType.value = type
    }

    fun setSelectedDay(day: Int) {
        _selectedDay.value = day
    }

    fun getActivitiesForDay(day: Int): List<ActivityEntity> {
        val state = uiState.value
        val list = if (state.filterType == null) state.activities else state.activities.filter { it.type == state.filterType }
        return list.filter { it.dayOfWeek == day }
    }
}
