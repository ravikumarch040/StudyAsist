package com.studyasist.ui.assessmentrun

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.studyasist.util.extractTextFromImage
import dagger.hilt.android.qualifiers.ApplicationContext
import androidx.lifecycle.viewModelScope
import com.studyasist.data.grading.ObjectiveGradingService
import com.studyasist.data.local.entity.QA
import com.studyasist.data.repository.AssessmentRepository
import com.studyasist.data.repository.AttemptRepository
import com.studyasist.data.repository.BadgeRepository
import com.studyasist.data.repository.ResultRepository
import com.studyasist.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.studyasist.util.speakText
import java.util.Locale
import javax.inject.Inject

data class AssessmentRunUiState(
    val assessmentTitle: String = "",
    val totalTimeSeconds: Long = 0,
    val remainingSeconds: Long = 0,
    val questions: List<QuestionWithAnswer> = emptyList(),
    val currentIndex: Int = 0,
    val isStarted: Boolean = false,
    val isSubmitted: Boolean = false,
    val attemptId: Long? = null,
    val resultId: Long? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val isExtractingFromImage: Boolean = false
)

data class QuestionWithAnswer(
    val qa: QA,
    val userAnswer: String = "",
    val answerImageUri: String? = null,
    val answerVoiceUri: String? = null
)

@HiltViewModel
class AssessmentRunViewModel @Inject constructor(
    @ApplicationContext private val context: android.content.Context,
    savedStateHandle: SavedStateHandle,
    private val assessmentRepository: AssessmentRepository,
    private val attemptRepository: AttemptRepository,
    private val resultRepository: ResultRepository,
    private val badgeRepository: BadgeRepository,
    private val settingsRepository: SettingsRepository,
    private val gradingService: ObjectiveGradingService
) : ViewModel() {

    private val assessmentId: Long = checkNotNull(savedStateHandle["assessmentId"]) { "assessmentId required" }

    private val _uiState = MutableStateFlow(AssessmentRunUiState())
    val uiState: StateFlow<AssessmentRunUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null

    init {
        loadAssessment()
    }

    private fun loadAssessment() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val withQuestions = assessmentRepository.getAssessmentWithQuestions(assessmentId)
            if (withQuestions == null) {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = context.getString(com.studyasist.R.string.err_assessment_not_found))
                }
                return@launch
            }
            val questions = withQuestions.questions.map { q ->
                QuestionWithAnswer(qa = q.qa, userAnswer = "")
            }
            val totalTime = withQuestions.assessment.totalTimeSeconds.toLong()
            _uiState.update {
                it.copy(
                    assessmentTitle = withQuestions.assessment.title,
                    totalTimeSeconds = totalTime,
                    remainingSeconds = totalTime,
                    questions = questions,
                    isLoading = false
                )
            }
        }
    }

    fun startAttempt() {
        if (_uiState.value.isStarted) return
        viewModelScope.launch {
            val attemptId = attemptRepository.startAttempt(assessmentId)
            _uiState.update {
                it.copy(isStarted = true, attemptId = attemptId)
            }
            startTimer()
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (isActive) {
                delay(1000)
                _uiState.update { state ->
                    val remaining = (state.remainingSeconds - 1).coerceAtLeast(0)
                    if (remaining == 0L) {
                        timerJob?.cancel()
                        submitAnswers()
                    }
                    state.copy(remainingSeconds = remaining)
                }
            }
        }
    }

    fun updateAnswer(index: Int, answer: String) {
        _uiState.update { state ->
            if (index !in state.questions.indices) return@update state
            val updated = state.questions.toMutableList()
            updated[index] = updated[index].copy(userAnswer = answer)
            state.copy(questions = updated)
        }
    }

    fun setCurrentIndex(index: Int) {
        _uiState.update {
            it.copy(currentIndex = index.coerceIn(0, it.questions.size - 1))
        }
    }

    fun nextQuestion() {
        _uiState.update { state ->
            state.copy(currentIndex = (state.currentIndex + 1).coerceAtMost(state.questions.size - 1))
        }
    }

    fun prevQuestion() {
        _uiState.update { state ->
            state.copy(currentIndex = (state.currentIndex - 1).coerceAtLeast(0))
        }
    }

    fun readQuestionAloud(text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            val voiceName = settingsRepository.settingsFlow.first().ttsVoiceName
            withContext(Dispatchers.Main) {
                speakText(context, text, Locale.getDefault(), voiceName)
            }
        }
    }

    fun extractFromImageAndUpdateAnswer(uri: Uri) {
        val index = _uiState.value.currentIndex
        if (index !in _uiState.value.questions.indices) return
        viewModelScope.launch {
            _uiState.update { it.copy(isExtractingFromImage = true, errorMessage = null) }
            extractTextFromImage(context, uri)
                .onSuccess { text ->
                    updateAnswerWithImageUri(index, text.trim(), uri.toString())
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(errorMessage = e.message ?: context.getString(com.studyasist.R.string.err_could_not_read_image))
                    }
                }
            _uiState.update { it.copy(isExtractingFromImage = false) }
        }
    }

    private fun updateAnswerWithImageUri(index: Int, answer: String, imageUri: String) {
        _uiState.update { state ->
            if (index !in state.questions.indices) return@update state
            val updated = state.questions.toMutableList()
            updated[index] = updated[index].copy(userAnswer = answer, answerImageUri = imageUri)
            state.copy(questions = updated)
        }
    }

    fun submitAnswers() {
        val state = _uiState.value
        if (state.isSubmitted || state.attemptId == null) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            timerJob?.cancel()

            val answers = state.questions.map { q ->
                AttemptRepository.AttemptAnswerInput(
                    qaId = q.qa.id,
                    answerText = q.userAnswer.takeIf { it.isNotBlank() },
                    answerImageUri = q.answerImageUri,
                    answerVoiceUri = q.answerVoiceUri
                )
            }
            attemptRepository.saveAnswers(state.attemptId, answers)
            attemptRepository.endAttempt(state.attemptId)

            val qaMap = state.questions.associate { it.qa.id to it.qa }
            val answerPairs = state.questions.map { it.qa.id to it.userAnswer }
            val result = gradingService.grade(answerPairs, qaMap)

            val resultId = resultRepository.saveResult(
                attemptId = state.attemptId,
                score = result.score,
                maxScore = result.maxScore,
                percent = result.percent,
                detailsJson = result.detailsJson
            )
            badgeRepository.checkAndAwardAfterAttempt(state.attemptId)

            _uiState.update {
                it.copy(
                    isSubmitted = true,
                    resultId = resultId,
                    isLoading = false
                )
            }
        }
    }

    override fun onCleared() {
        timerJob?.cancel()
        super.onCleared()
    }
}
