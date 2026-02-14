package com.studyasist.ui

import androidx.lifecycle.ViewModel
import com.studyasist.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val darkModeFlow: kotlinx.coroutines.flow.Flow<String> = settingsRepository.darkModeFlow
}
