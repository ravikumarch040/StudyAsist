package com.studyasist.ui.goaledit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studyasist.data.repository.GoalRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GoalItemRow(
    val subject: String = "",
    val chapterList: String = "",
    val targetHours: String = ""
)

data class GoalEditUiState(
    val goalId: Long = 0L,
    val isEdit: Boolean = false,
    val name: String = "",
    val description: String = "",
    val examDateMillis: Long = System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000,
    val items: List<GoalItemRow> = emptyList(),
    val isSaving: Boolean = false,
    val loadError: String? = null
)

@HiltViewModel
class GoalEditViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val goalRepository: GoalRepository
) : ViewModel() {

    private val goalId: Long = savedStateHandle.get<Long>("goalId") ?: 0L

    private val _uiState = MutableStateFlow(
        GoalEditUiState(
            goalId = goalId,
            isEdit = goalId != 0L
        )
    )
    val uiState: StateFlow<GoalEditUiState> = _uiState.asStateFlow()

    init {
        if (goalId != 0L) {
            viewModelScope.launch {
                val goal = goalRepository.getGoal(goalId)
                val goalItems = goalRepository.getGoalItems(goalId)
                if (goal != null) {
                    _uiState.update {
                        it.copy(
                            name = goal.name,
                            description = goal.description.orEmpty(),
                            examDateMillis = goal.examDate,
                            items = goalItems.map { gi ->
                                GoalItemRow(
                                    subject = gi.subject,
                                    chapterList = gi.chapterList,
                                    targetHours = gi.targetHours?.toString() ?: ""
                                )
                            }
                        )
                    }
                } else {
                    _uiState.update { it.copy(loadError = "Goal not found") }
                }
            }
        } else {
            _uiState.update { it.copy(items = listOf(GoalItemRow())) }
        }
    }

    fun updateName(name: String) {
        _uiState.update { it.copy(name = name) }
    }

    fun updateDescription(description: String) {
        _uiState.update { it.copy(description = description) }
    }

    fun updateExamDate(millis: Long) {
        _uiState.update { it.copy(examDateMillis = millis) }
    }

    fun updateItem(index: Int, subject: String, chapterList: String, targetHours: String) {
        _uiState.update { state ->
            val list = state.items.toMutableList()
            if (index in list.indices) {
                list[index] = GoalItemRow(subject = subject, chapterList = chapterList, targetHours = targetHours)
            }
            state.copy(items = list)
        }
    }

    fun addItem() {
        _uiState.update { it.copy(items = it.items + GoalItemRow()) }
    }

    fun removeItem(index: Int) {
        _uiState.update { state ->
            if (index in state.items.indices) {
                state.copy(items = state.items.filterIndexed { i, _ -> i != index })
            } else state
        }
    }

    fun save(onSaved: (Long) -> Unit) {
        val state = _uiState.value
        val name = state.name.trim()
        if (name.isBlank()) return
        val itemInputs = state.items
            .filter { it.subject.isNotBlank() }
            .map {
                GoalRepository.GoalItemInput(
                    subject = it.subject.trim(),
                    chapterList = it.chapterList.trim(),
                    targetHours = it.targetHours.trim().toIntOrNull()
                )
            }
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            try {
                val id = if (state.isEdit) {
                    val existing = goalRepository.getGoal(state.goalId)
                    if (existing != null) {
                        goalRepository.updateGoal(
                            existing.copy(
                                name = name,
                                description = state.description.takeIf { it.isNotBlank() },
                                examDate = state.examDateMillis
                            )
                        )
                    }
                    goalRepository.updateGoalItems(state.goalId, itemInputs)
                    state.goalId
                } else {
                    goalRepository.createGoal(
                        name = name,
                        description = state.description.takeIf { it.isNotBlank() },
                        examDate = state.examDateMillis,
                        items = itemInputs
                    )
                }
                _uiState.update { it.copy(isSaving = false) }
                onSaved(id)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isSaving = false, loadError = e.message ?: "Save failed")
                }
            }
        }
    }
}
