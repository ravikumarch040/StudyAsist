package com.studyasist.ui

import android.content.Intent
import androidx.lifecycle.ViewModel
import com.studyasist.data.repository.SettingsRepository
import com.studyasist.notification.EXTRA_GOAL_ID
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val darkModeFlow: kotlinx.coroutines.flow.Flow<String> = settingsRepository.darkModeFlow
    val themeIdFlow: kotlinx.coroutines.flow.Flow<String> = settingsRepository.themeIdFlow
    val userNameFlow: kotlinx.coroutines.flow.Flow<String> = settingsRepository.userNameFlow
    val profilePicUriFlow: kotlinx.coroutines.flow.Flow<String?> = settingsRepository.profilePicUriFlow

    private val _pendingGoalIdForDeepLink = MutableStateFlow<Long?>(null)
    val pendingGoalIdForDeepLink: StateFlow<Long?> = _pendingGoalIdForDeepLink.asStateFlow()

    fun setPendingGoalIdFromIntent(intent: Intent?) {
        val goalId = intent?.getLongExtra(EXTRA_GOAL_ID, -1L)?.takeIf { it >= 0 }
        _pendingGoalIdForDeepLink.value = goalId
    }

    fun clearPendingGoalId() {
        _pendingGoalIdForDeepLink.value = null
    }
}
