package com.studyasist.ui.addrevision

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.qualifiers.ApplicationContext
import com.studyasist.data.local.entity.ActivityEntity
import com.studyasist.data.local.entity.ActivityType
import com.studyasist.data.repository.ActivityRepository
import com.studyasist.data.repository.SettingsRepository
import com.studyasist.util.todayDayOfWeek
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.net.URLDecoder
import javax.inject.Inject

data class AddRevisionUiState(
    val subject: String = "",
    val chapter: String? = null,
    val title: String = "",
    val activeTimetableId: Long? = null,
    val activeTimetableName: String? = null,
    val dayOfWeek: Int = 1,
    val startHour: Int = 18,
    val startMinute: Int = 0,
    val endHour: Int = 19,
    val endMinute: Int = 0,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class AddRevisionViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    savedStateHandle: SavedStateHandle,
    private val activityRepository: ActivityRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val subject = URLDecoder.decode(
        savedStateHandle.get<String>("subject") ?: "",
        "UTF-8"
    ).takeIf { it.isNotBlank() } ?: ""
    private val chapter = savedStateHandle.get<String>("chapter")?.let {
        URLDecoder.decode(it, "UTF-8").takeIf { s -> s.isNotBlank() }
    }

    private val _uiState = MutableStateFlow(
        AddRevisionUiState(
            subject = subject,
            chapter = chapter,
            title = buildTitle(subject, chapter),
            dayOfWeek = todayDayOfWeek()
        )
    )
    val uiState: StateFlow<AddRevisionUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val activeId = settingsRepository.activeTimetableIdFlow.first()
            val timetableName = if (activeId != null && activeId > 0) {
                // We don't have timetable name in settings; can leave null or fetch from TimetableRepository
                null
            } else null
            _uiState.update {
                it.copy(
                    activeTimetableId = if (activeId != null && activeId > 0) activeId else null,
                    activeTimetableName = timetableName
                )
            }
        }
    }

    private fun buildTitle(subj: String, ch: String?): String {
        return if (ch.isNullOrBlank()) {
            context.getString(com.studyasist.R.string.revise_title_subject, subj)
        } else {
            context.getString(com.studyasist.R.string.revise_title_subject_chapter, subj, ch)
        }
    }

    fun updateDay(day: Int) = _uiState.update { it.copy(dayOfWeek = day) }
    fun updateStartTime(h: Int, m: Int) = _uiState.update { it.copy(startHour = h, startMinute = m) }
    fun updateEndTime(h: Int, m: Int) = _uiState.update { it.copy(endHour = h, endMinute = m) }
    fun updateTitle(s: String) = _uiState.update { it.copy(title = s) }

    fun save(onSaved: () -> Unit) {
        val state = _uiState.value
        val timetableId = state.activeTimetableId
        if (timetableId == null || timetableId <= 0) {
            _uiState.update { it.copy(errorMessage = context.getString(com.studyasist.R.string.err_no_active_timetable)) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            try {
                val startMinutes = state.startHour * 60 + state.startMinute
                val endMinutes = state.endHour * 60 + state.endMinute
                val entity = ActivityEntity(
                    timetableId = timetableId,
                    dayOfWeek = state.dayOfWeek,
                    startTimeMinutes = startMinutes,
                    endTimeMinutes = endMinutes,
                    title = state.title.ifBlank { buildTitle(state.subject, state.chapter) },
                    type = ActivityType.STUDY
                )
                activityRepository.insertActivity(entity)
                _uiState.update { it.copy(isSaving = false, isSaved = true) }
                onSaved()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = e.message ?: context.getString(com.studyasist.R.string.err_failed_to_add)
                    )
                }
            }
        }
    }
}
