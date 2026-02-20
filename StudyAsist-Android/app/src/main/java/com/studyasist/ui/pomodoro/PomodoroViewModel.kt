package com.studyasist.ui.pomodoro

import android.os.CountDownTimer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studyasist.data.local.dao.PomodoroDao
import com.studyasist.data.local.entity.PomodoroSession
import com.studyasist.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class PomodoroPhase { FOCUS, SHORT_BREAK, LONG_BREAK }

data class PomodoroUiState(
    val phase: PomodoroPhase = PomodoroPhase.FOCUS,
    val totalSeconds: Int = 25 * 60,
    val remainingSeconds: Int = 25 * 60,
    val isRunning: Boolean = false,
    val currentSession: Int = 1,
    val totalSessions: Int = 4,
    val todayFocusMinutes: Int = 0,
    val subject: String? = null
)

@HiltViewModel
class PomodoroViewModel @Inject constructor(
    private val pomodoroDao: PomodoroDao,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PomodoroUiState())
    val uiState: StateFlow<PomodoroUiState> = _uiState.asStateFlow()

    private var timer: CountDownTimer? = null
    private var sessionStartedAt: Long = 0L

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            val focusMin = settingsRepository.pomodoroFocusMinutesFlow.first()
            val todayStart = java.util.Calendar.getInstance().apply {
                set(java.util.Calendar.HOUR_OF_DAY, 0)
                set(java.util.Calendar.MINUTE, 0)
                set(java.util.Calendar.SECOND, 0)
                set(java.util.Calendar.MILLISECOND, 0)
            }.timeInMillis
            val todayFocus = pomodoroDao.getTotalFocusMinutesSince(todayStart) ?: 0
            _uiState.value = _uiState.value.copy(
                totalSeconds = focusMin * 60,
                remainingSeconds = focusMin * 60,
                todayFocusMinutes = todayFocus
            )
        }
    }

    fun start() {
        val state = _uiState.value
        if (state.isRunning) return
        sessionStartedAt = System.currentTimeMillis()
        _uiState.value = state.copy(isRunning = true)
        timer = object : CountDownTimer(state.remainingSeconds * 1000L, 1000L) {
            override fun onTick(millisUntilFinished: Long) {
                _uiState.value = _uiState.value.copy(
                    remainingSeconds = (millisUntilFinished / 1000).toInt()
                )
            }

            override fun onFinish() {
                onPhaseComplete()
            }
        }.start()
    }

    fun pause() {
        timer?.cancel()
        _uiState.value = _uiState.value.copy(isRunning = false)
    }

    fun reset() {
        timer?.cancel()
        val state = _uiState.value
        _uiState.value = state.copy(
            isRunning = false,
            remainingSeconds = state.totalSeconds
        )
    }

    fun skipPhase() {
        timer?.cancel()
        onPhaseComplete()
    }

    fun setSubject(subject: String?) {
        _uiState.value = _uiState.value.copy(subject = subject)
    }

    private fun onPhaseComplete() {
        val state = _uiState.value
        viewModelScope.launch {
            if (state.phase == PomodoroPhase.FOCUS) {
                pomodoroDao.insert(
                    PomodoroSession(
                        startedAt = sessionStartedAt,
                        endedAt = System.currentTimeMillis(),
                        durationMinutes = state.totalSeconds / 60,
                        type = "focus",
                        subject = state.subject,
                        completed = true
                    )
                )
            }

            val shortBreakMin = settingsRepository.pomodoroShortBreakMinutesFlow.first()
            val longBreakMin = settingsRepository.pomodoroLongBreakMinutesFlow.first()
            val focusMin = settingsRepository.pomodoroFocusMinutesFlow.first()

            val (nextPhase, nextDuration, nextSession) = when {
                state.phase == PomodoroPhase.FOCUS && state.currentSession % 4 == 0 ->
                    Triple(PomodoroPhase.LONG_BREAK, longBreakMin * 60, state.currentSession)
                state.phase == PomodoroPhase.FOCUS ->
                    Triple(PomodoroPhase.SHORT_BREAK, shortBreakMin * 60, state.currentSession)
                else ->
                    Triple(PomodoroPhase.FOCUS, focusMin * 60, state.currentSession + 1)
            }

            val todayStart = java.util.Calendar.getInstance().apply {
                set(java.util.Calendar.HOUR_OF_DAY, 0)
                set(java.util.Calendar.MINUTE, 0)
                set(java.util.Calendar.SECOND, 0)
                set(java.util.Calendar.MILLISECOND, 0)
            }.timeInMillis
            val todayFocus = pomodoroDao.getTotalFocusMinutesSince(todayStart) ?: 0

            _uiState.value = state.copy(
                phase = nextPhase,
                totalSeconds = nextDuration,
                remainingSeconds = nextDuration,
                isRunning = false,
                currentSession = nextSession,
                todayFocusMinutes = todayFocus
            )
        }
    }

    override fun onCleared() {
        timer?.cancel()
        super.onCleared()
    }
}
