package com.studyasist.ui.assessmentcreate

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studyasist.data.local.entity.Goal
import com.studyasist.data.repository.AssessmentRepository
import com.studyasist.data.repository.GoalRepository
import com.studyasist.data.repository.QABankRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AssessmentCreateUiState(
    val sourceMode: SourceMode = SourceMode.BY_SUBJECT_CHAPTER,
    val selectedGoalId: Long? = null,
    val subject: String = "",
    val chapter: String = "",
    val title: String = "",
    val questionCount: Int = 10,
    val timeLimitMinutes: Int = 30,
    val randomize: Boolean = true,
    val availableGoals: List<Goal> = emptyList(),
    val distinctSubjects: List<String> = emptyList(),
    val distinctChapters: List<String> = emptyList(),
    val availableCount: Int = 0,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val createdAssessmentId: Long? = null,
    // Manual selection
    val showQaSelector: Boolean = false,
    val selectorSubject: String = "",
    val selectorChapter: String = "",
    val selectorQas: List<com.studyasist.data.local.entity.QA> = emptyList(),
    val selectedQaIds: Set<Long> = emptySet(),
    val selectedQas: List<com.studyasist.data.local.entity.QA> = emptyList(),
    val selectorDistinctChapters: List<String> = emptyList()
)

enum class SourceMode { BY_GOAL, BY_SUBJECT_CHAPTER, MANUAL }

@HiltViewModel
class AssessmentCreateViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val assessmentRepository: AssessmentRepository,
    private val goalRepository: GoalRepository,
    private val qaBankRepository: QABankRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AssessmentCreateUiState())
    val uiState: StateFlow<AssessmentCreateUiState> = _uiState.asStateFlow()

    init {
        val goalId = savedStateHandle.get<Long>("goalId")
        if (goalId != null) {
            _uiState.update {
                it.copy(sourceMode = SourceMode.BY_GOAL, selectedGoalId = goalId)
            }
        }
        loadGoals()
        loadDistinctValues()
    }

    fun setSourceMode(mode: SourceMode) {
        _uiState.update {
            it.copy(
                sourceMode = mode,
                errorMessage = null,
                showQaSelector = if (mode == SourceMode.MANUAL) it.showQaSelector else false
            )
        }
        when (mode) {
            SourceMode.BY_GOAL -> loadGoals()
            SourceMode.BY_SUBJECT_CHAPTER -> loadDistinctValues()
            SourceMode.MANUAL -> loadDistinctValues()
        }
        refreshAvailableCount()
    }

    fun setGoalId(goalId: Long?) {
        _uiState.update { it.copy(selectedGoalId = goalId, errorMessage = null) }
        refreshAvailableCount()
    }

    fun setSubject(subject: String) {
        _uiState.update {
            it.copy(subject = subject, chapter = "", errorMessage = null)
        }
        viewModelScope.launch {
            val chapters = if (subject.isNotBlank()) {
                qaBankRepository.getDistinctChaptersForSubject(subject)
            } else {
                qaBankRepository.getDistinctChapters()
            }
            _uiState.update { it.copy(distinctChapters = chapters) }
        }
        refreshAvailableCount()
    }

    fun setChapter(chapter: String) {
        _uiState.update { it.copy(chapter = chapter, errorMessage = null) }
        refreshAvailableCount()
    }

    fun setTitle(title: String) {
        _uiState.update { it.copy(title = title, errorMessage = null) }
    }

    fun setQuestionCount(count: Int) {
        _uiState.update { it.copy(questionCount = count.coerceIn(1, 100), errorMessage = null) }
    }

    fun setTimeLimitMinutes(minutes: Int) {
        _uiState.update { it.copy(timeLimitMinutes = minutes.coerceIn(1, 180), errorMessage = null) }
    }

    fun setRandomize(randomize: Boolean) {
        _uiState.update { it.copy(randomize = randomize) }
    }

    private fun loadGoals() {
        viewModelScope.launch {
            goalRepository.getAllActiveGoals().collect { goals ->
                _uiState.update { it.copy(availableGoals = goals) }
                refreshAvailableCount()
            }
        }
    }

    private fun loadDistinctValues() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    distinctSubjects = qaBankRepository.getDistinctSubjects(),
                    distinctChapters = qaBankRepository.getDistinctChapters()
                )
            }
            refreshAvailableCount()
        }
    }

    private fun refreshAvailableCount() {
        viewModelScope.launch {
            val state = _uiState.value
            val count = when (state.sourceMode) {
                SourceMode.BY_GOAL -> {
                    val goalId = state.selectedGoalId
                    if (goalId == null) 0
                    else {
                        val items = goalRepository.getGoalItems(goalId)
                        items.sumOf { item ->
                            val chapters = item.chapterList.split(",").map { it.trim() }.filter { it.isNotBlank() }
                            if (chapters.isEmpty()) {
                                qaBankRepository.countQA(item.subject, null)
                            } else {
                                chapters.sumOf { ch -> qaBankRepository.countQA(item.subject, ch) }
                            }
                        }
                    }
                }
                SourceMode.BY_SUBJECT_CHAPTER -> {
                    qaBankRepository.countQA(
                        state.subject.takeIf { it.isNotBlank() },
                        state.chapter.takeIf { it.isNotBlank() }
                    )
                }
                SourceMode.MANUAL -> state.selectedQaIds.size
            }
            _uiState.update { it.copy(availableCount = count) }
        }
    }

    fun openQaSelector() {
        viewModelScope.launch {
            val state = _uiState.value
            val chapters = if (state.selectorSubject.isNotBlank()) {
                qaBankRepository.getDistinctChaptersForSubject(state.selectorSubject)
            } else {
                qaBankRepository.getDistinctChapters()
            }
            _uiState.update {
                it.copy(
                    showQaSelector = true,
                    selectorDistinctChapters = chapters
                )
            }
            loadSelectorQas()
        }
    }

    fun closeQaSelector() {
        _uiState.update { it.copy(showQaSelector = false) }
    }

    fun setSelectorSubject(subject: String) {
        _uiState.update {
            it.copy(
                selectorSubject = subject,
                selectorChapter = if (subject == it.selectorSubject) it.selectorChapter else ""
            )
        }
        viewModelScope.launch {
            val chapters = if (subject.isNotBlank()) {
                qaBankRepository.getDistinctChaptersForSubject(subject)
            } else {
                qaBankRepository.getDistinctChapters()
            }
            _uiState.update { it.copy(selectorDistinctChapters = chapters) }
            loadSelectorQas()
        }
    }

    fun setSelectorChapter(chapter: String) {
        _uiState.update { it.copy(selectorChapter = chapter) }
        loadSelectorQas()
    }

    private fun loadSelectorQas() {
        viewModelScope.launch {
            val state = _uiState.value
            val qas = qaBankRepository.getQAListBySubjectChapter(
                state.selectorSubject.takeIf { it.isNotBlank() },
                state.selectorChapter.takeIf { it.isNotBlank() }
            )
            _uiState.update { it.copy(selectorQas = qas) }
        }
    }

    fun toggleQaSelection(qaId: Long) {
        _uiState.update {
            val newSet = if (qaId in it.selectedQaIds) {
                it.selectedQaIds - qaId
            } else {
                it.selectedQaIds + qaId
            }
            it.copy(selectedQaIds = newSet)
        }
        refreshAvailableCount()
    }

    fun confirmQaSelection() {
        viewModelScope.launch {
            val state = _uiState.value
            val qas = if (state.selectedQaIds.isEmpty()) emptyList()
            else qaBankRepository.getQAByIds(state.selectedQaIds.toList())
            _uiState.update {
                it.copy(
                    showQaSelector = false,
                    selectedQas = qas,
                    availableCount = qas.size
                )
            }
        }
    }

    fun clearManualSelection() {
        _uiState.update {
            it.copy(
                selectedQaIds = emptySet(),
                selectedQas = emptyList(),
                availableCount = 0
            )
        }
        refreshAvailableCount()
    }

    fun createAssessment(onCreated: (Long) -> Unit) {
        val state = _uiState.value
        if (state.title.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Enter assessment title") }
            return
        }
        if (state.availableCount == 0) {
            _uiState.update { it.copy(errorMessage = "No questions available for selected criteria") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val id = when (state.sourceMode) {
                    SourceMode.BY_GOAL -> {
                        val goalId = state.selectedGoalId
                            ?: run {
                                _uiState.update { it.copy(isLoading = false, errorMessage = "Select a goal") }
                                return@launch
                            }
                        val count = state.questionCount.coerceAtMost(state.availableCount)
                        assessmentRepository.createAssessmentFromGoal(
                            title = state.title,
                            goalId = goalId,
                            totalTimeSeconds = state.timeLimitMinutes * 60,
                            randomizeQuestions = state.randomize,
                            count = count
                        )
                    }
                    SourceMode.BY_SUBJECT_CHAPTER -> {
                        val count = state.questionCount.coerceAtMost(state.availableCount)
                        assessmentRepository.createAssessmentFromRandom(
                            title = state.title,
                            goalId = null,
                            subject = state.subject.takeIf { it.isNotBlank() },
                            chapter = state.chapter.takeIf { it.isNotBlank() },
                            totalTimeSeconds = state.timeLimitMinutes * 60,
                            randomizeQuestions = state.randomize,
                            count = count
                        )
                    }
                    SourceMode.MANUAL -> {
                        val qaIds = state.selectedQas.map { it.id }
                        if (qaIds.isEmpty()) {
                            _uiState.update { it.copy(isLoading = false, errorMessage = "Select at least one question") }
                            return@launch
                        }
                        assessmentRepository.createAssessment(
                            title = state.title,
                            goalId = null,
                            subject = null,
                            chapter = null,
                            totalTimeSeconds = state.timeLimitMinutes * 60,
                            randomizeQuestions = state.randomize,
                            qaIds = if (state.randomize) qaIds.shuffled() else qaIds
                        )
                    }
                }
                _uiState.update {
                    it.copy(isLoading = false, createdAssessmentId = id, errorMessage = null)
                }
                onCreated(id)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Failed to create assessment"
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
