package com.studyasist.ui.activityedit

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import androidx.lifecycle.viewModelScope
import com.studyasist.data.local.entity.ActivityEntity
import com.studyasist.data.local.entity.ActivityType
import com.studyasist.data.repository.ActivityRepository
import com.studyasist.data.repository.SettingsRepository
import com.studyasist.data.repository.TimetableRepository
import com.studyasist.notification.NotificationScheduler
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
    val useSpeechSound: Boolean = false,
    val alarmTtsMessage: String = "",
    val overlapWarning: List<ActivityEntity> = emptyList(),
    val showOverlapDialog: Boolean = false,
    val overlapBlockedMessage: String? = null,
    val isSaving: Boolean = false
) {
    val startTimeMinutes: Int get() = startHour * 60 + startMinute
    val endTimeMinutes: Int get() = endHour * 60 + endMinute
}

@HiltViewModel
class ActivityEditViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    savedStateHandle: SavedStateHandle,
    private val activityRepository: ActivityRepository,
    private val timetableRepository: TimetableRepository,
    private val settingsRepository: SettingsRepository,
    private val notificationScheduler: NotificationScheduler
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
                            notifyLeadMinutes = act.notifyLeadMinutes,
                            useSpeechSound = act.useSpeechSound,
                            alarmTtsMessage = act.alarmTtsMessage ?: ""
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

    fun updateDay(day: Int) { _uiState.update { it.copy(dayOfWeek = day, overlapBlockedMessage = null) } }
    fun updateStartTime(h: Int, m: Int) { _uiState.update { it.copy(startHour = h, startMinute = m, overlapBlockedMessage = null) } }
    fun updateEndTime(h: Int, m: Int) { _uiState.update { it.copy(endHour = h, endMinute = m, overlapBlockedMessage = null) } }
    fun updateTitle(s: String) { _uiState.update { it.copy(title = s) } }
    fun updateType(t: ActivityType) { _uiState.update { it.copy(type = t) } }
    fun updateNote(s: String) { _uiState.update { it.copy(note = s) } }
    fun updateNotifyEnabled(b: Boolean) { _uiState.update { it.copy(notifyEnabled = b) } }
    fun updateNotifyLeadMinutes(m: Int) { _uiState.update { it.copy(notifyLeadMinutes = m) } }
    fun updateUseSpeechSound(use: Boolean) {
        if (!use) {
            _uiState.update { it.copy(useSpeechSound = false) }
            return
        }
        _uiState.update { it.copy(useSpeechSound = true) }
        val state = _uiState.value
        if (state.alarmTtsMessage.isBlank()) {
            viewModelScope.launch {
                val userName = settingsRepository.settingsFlow.first().userName
                val name = userName.ifBlank { context.getString(com.studyasist.R.string.alarm_tts_default_name) }
                val title = state.title.ifBlank { context.getString(com.studyasist.R.string.alarm_tts_default_title) }
                _uiState.update { it.copy(alarmTtsMessage = context.getString(com.studyasist.R.string.alarm_tts_message_format, name, title)) }
            }
        }
    }
    fun updateAlarmTtsMessage(msg: String) { _uiState.update { it.copy(alarmTtsMessage = msg) } }
    fun dismissOverlapDialog() { _uiState.update { it.copy(showOverlapDialog = false) } }
    fun clearOverlapBlocked() { _uiState.update { it.copy(overlapBlockedMessage = null) } }

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
                val blockOverlap = settingsRepository.settingsFlow.first().blockOverlap
                if (blockOverlap) {
                    _uiState.update {
                        it.copy(
                            overlapBlockedMessage = context.getString(com.studyasist.R.string.overlap_blocked_message)
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(overlapWarning = overlapping, showOverlapDialog = true)
                    }
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
                useSpeechSound = state.useSpeechSound,
                alarmTtsMessage = state.alarmTtsMessage.ifBlank { null },
                sortOrder = 0
            )
            val savedId: Long = if (state.isEdit) {
                activityRepository.updateActivity(entity)
                state.activityId
            } else {
                activityRepository.insertActivity(entity)
            }
            val savedEntity = entity.copy(id = savedId)
            notificationScheduler.cancelActivity(savedEntity.id)
            val activeId = settingsRepository.activeTimetableIdFlow.first()
            if (savedEntity.notifyEnabled && activeId == timetableId) {
                val timetable = timetableRepository.getTimetable(timetableId)
                if (timetable != null) {
                    notificationScheduler.scheduleActivity(savedEntity, timetable.name)
                }
            }
            _uiState.update { it.copy(isSaving = false, overlapBlockedMessage = null) }
            onSaved()
        }
    }
}
