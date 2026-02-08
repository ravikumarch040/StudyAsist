package com.studyasist.ui.timetablelist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studyasist.data.local.entity.TimetableEntity
import com.studyasist.data.local.entity.WeekType
import com.studyasist.data.repository.TimetableRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TimetableListUiState(
    val timetables: List<TimetableEntity> = emptyList(),
    val showCreateDialog: Boolean = false,
    val createName: String = "",
    val createWeekType: WeekType = WeekType.MON_SUN,
    val isCreating: Boolean = false
)

@HiltViewModel
class TimetableListViewModel @Inject constructor(
    private val timetableRepository: TimetableRepository
) : ViewModel() {

    private val timetablesFlow = timetableRepository.getAllTimetables()
    private val _dialogState = MutableStateFlow(TimetableListUiState())

    val uiState: StateFlow<TimetableListUiState> = combine(
        timetablesFlow,
        _dialogState
    ) { timetables, dialog ->
        TimetableListUiState(
            timetables = timetables,
            showCreateDialog = dialog.showCreateDialog,
            createName = dialog.createName,
            createWeekType = dialog.createWeekType,
            isCreating = dialog.isCreating
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TimetableListUiState()
    )

    fun openCreateDialog() {
        _dialogState.value = _dialogState.value.copy(
            showCreateDialog = true,
            createName = "",
            createWeekType = WeekType.MON_SUN
        )
    }

    fun dismissCreateDialog() {
        _dialogState.value = _dialogState.value.copy(showCreateDialog = false)
    }

    fun updateCreateName(name: String) {
        _dialogState.value = _dialogState.value.copy(createName = name)
    }

    fun updateCreateWeekType(weekType: WeekType) {
        _dialogState.value = _dialogState.value.copy(createWeekType = weekType)
    }

    fun createTimetable(onCreated: (Long) -> Unit) {
        val name = _dialogState.value.createName.trim()
        if (name.isBlank()) return
        viewModelScope.launch {
            _dialogState.value = _dialogState.value.copy(isCreating = true)
            val id = timetableRepository.createTimetable(
                name = name,
                weekType = _dialogState.value.createWeekType
            )
            _dialogState.value = _dialogState.value.copy(
                isCreating = false,
                showCreateDialog = false,
                createName = ""
            )
            onCreated(id)
        }
    }

    fun deleteTimetable(id: Long) {
        viewModelScope.launch {
            timetableRepository.deleteTimetable(id)
        }
    }

    fun duplicateTimetable(sourceId: Long, newName: String, onCreated: (Long) -> Unit) {
        viewModelScope.launch {
            val id = timetableRepository.duplicateTimetable(sourceId, newName)
            if (id != 0L) onCreated(id)
        }
    }
}
