package com.studyasist.ui.activityedit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studyasist.data.local.entity.ActivityEntity
import com.studyasist.data.local.entity.ActivityType
import com.studyasist.data.repository.ActivityRepository
import com.studyasist.data.repository.SettingsRepository
import com.studyasist.data.repository.TimetableRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ActivityEditUiState(
    val timetableId: Long = 0L,
    val activityId: Long = 0L,
    val isEdit: Boolean = false,
    val dayOfWeek: Int = 1,
    val startHour: Int = 6,
    val startMinute: Int = 0,
    val endHour: Int = 7,
    val endMinute: Int = 0,
    val title: String = "",
    val type: ActivityType = ActivityType.STUDY,
    val note: String = "",
    val notifyEnabled: Boolean = false,
    val notifyLeadMinutes: Int = 5,
    val overlapWarning: List<ActivityEntity> = emptyList(),
    val showOverlapDialog: Boolean = false,
    val isSaving: Boolean = false
) {
    val startTimeMinutes: Int get() = startHour * 60 + startMinute
    val endTimeMinutes: Int get() = endHour * 60 + endMinute
}

@HiltViewModel
class ActivityEditViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val activityRepository: ActivityRepository,
    private val timetableRepository: TimetableRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val timetableId: Long = checkNotNull(savedStateHandle["timetableId"]) { "timetableId required" }
    private val activityId: Long = savedStateHandle.get<Long>("activityId") ?: 0L
    private val initialDayOfWeek: Int = savedStateHandle.get<Int>("dayOfWeek") ?: 1

    private val _uiState = MutableStateFlow(
        ActivityEditUiState(
            timetableId = timetableId,
            activityId = activityId,
            isEdit = activityId != 0L,
            dayOfWeek = initialDayOfWeek
        )
    )
    val uiState: StateFlow<ActivityEditUiState> = _uiState.asStateFlow()

    init {
        if (activityId != 0L) {
            viewModelScope.launch {
                activityRepository.getActivity(activityId)?.let { act ->
                    _uiState.update {
                        it.copy(
                            dayOfWeek = act.dayOfWeek,
                            startHour = act.startTimeMinutes / 60,
                            startMinute = act.startTimeMinutes % 60,
                            endHour = act.endTimeMinutes / 60,
                            endMinute = act.endTimeMinutes % 60,
                            title = act.title,
                            type = act.type,
                            note = act.note ?: "",
                            notifyEnabled = act.notifyEnabled,
                            notifyLeadMinutes = act.notifyLeadMinutes
                        )
                    }
                }
            }
        } else {
            viewModelScope.launch {
                val settings = settingsRepository.settingsFlow.first()
                val dayActivities = activityRepository.getActivitiesForDay(timetableId, initialDayOfWeek)
                    .sortedBy { it.endTimeMinutes }
                val lastEnd = dayActivities.maxByOrNull { it.endTimeMinutes }
                val (defaultStartH, defaultStartM) = if (lastEnd == null) {
                    5 to 0
                } else {
                    lastEnd.endTimeMinutes / 60 to lastEnd.endTimeMinutes % 60
                }
                val defaultEndH = (defaultStartH + 1).coerceAtMost(23)
                val defaultEndM = defaultStartM
                _uiState.update {
                    it.copy(
                        dayOfWeek = initialDayOfWeek,
                        startHour = defaultStartH,
                        startMinute = defaultStartM,
                        endHour = defaultEndH,
                        endMinute = defaultEndM,
                        notifyLeadMinutes = settings.defaultLeadMinutes
                    )
                }
            }
        }
    }

    fun updateDay(day: Int) { _uiState.update { it.copy(dayOfWeek = day) } }
    fun updateStartTime(h: Int, m: Int) { _uiState.update { it.copy(startHour = h, startMinute = m) } }
    fun updateEndTime(h: Int, m: Int) { _uiState.update { it.copy(endHour = h, endMinute = m) } }
    fun updateTitle(s: String) { _uiState.update { it.copy(title = s) } }
    fun updateType(t: ActivityType) { _uiState.update { it.copy(type = t) } }
    fun updateNote(s: String) { _uiState.update { it.copy(note = s) } }
    fun updateNotifyEnabled(b: Boolean) { _uiState.update { it.copy(notifyEnabled = b) } }
    fun updateNotifyLeadMinutes(m: Int) { _uiState.update { it.copy(notifyLeadMinutes = m) } }
    fun dismissOverlapDialog() { _uiState.update { it.copy(showOverlapDialog = false) } }

    fun copyScheduleFromDay(sourceDay: Int, onDone: () -> Unit) {
        if (sourceDay == _uiState.value.dayOfWeek) return
        viewModelScope.launch {
            activityRepository.copyDayToDay(timetableId, sourceDay, _uiState.value.dayOfWeek)
            onDone()
        }
    }

    fun save(saveAnyway: Boolean = false, onSaved: () -> Unit) {
        val state = _uiState.value
        val title = state.title.trim()
        if (title.isBlank()) return
        if (state.startTimeMinutes >= state.endTimeMinutes) return

        viewModelScope.launch {
            val overlapping = activityRepository.hasOverlap(
                timetableId = timetableId,
                dayOfWeek = state.dayOfWeek,
                startMinutes = state.startTimeMinutes,
                endMinutes = state.endTimeMinutes,
                excludeActivityId = state.activityId
            )
            if (overlapping.isNotEmpty() && !saveAnyway) {
                _uiState.update {
                    it.copy(overlapWarning = overlapping, showOverlapDialog = true)
                }
                return@launch
            }

            _uiState.update { it.copy(isSaving = true) }
            val entity = ActivityEntity(
                id = state.activityId,
                timetableId = timetableId,
                dayOfWeek = state.dayOfWeek,
                startTimeMinutes = state.startTimeMinutes,
                endTimeMinutes = state.endTimeMinutes,
                title = title,
                type = state.type,
                note = state.note.ifBlank { null },
                notifyEnabled = state.notifyEnabled,
                notifyLeadMinutes = state.notifyLeadMinutes,
                sortOrder = 0
            )
            if (state.isEdit) {
                activityRepository.updateActivity(entity)
            } else {
                activityRepository.insertActivity(entity)
            }
            _uiState.update { it.copy(isSaving = false) }
            onSaved()
        }
    }
}
