package com.studyasist.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studyasist.data.repository.AppSettings
import com.studyasist.data.repository.GeminiRepository
import com.studyasist.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val geminiRepository: GeminiRepository
) : ViewModel() {

    val settings: StateFlow<AppSettings> = settingsRepository.settingsFlow
        .stateIn(
            viewModelScope,
            kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
            AppSettings(AppSettings.DEFAULT_LEAD_MINUTES, true, "", null, "")
        )

    private val _apiKeyTestMessage = MutableStateFlow<String?>(null)
    val apiKeyTestMessage: StateFlow<String?> = _apiKeyTestMessage.asStateFlow()

    fun setDefaultLeadMinutes(minutes: Int) {
        viewModelScope.launch { settingsRepository.setDefaultLeadMinutes(minutes) }
    }

    fun setVibrationEnabled(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setVibrationEnabled(enabled) }
    }

    fun setUserName(name: String) {
        viewModelScope.launch { settingsRepository.setUserName(name) }
    }

    fun setTtsVoiceName(name: String?) {
        viewModelScope.launch { settingsRepository.setTtsVoiceName(name) }
    }

    fun setGeminiApiKey(key: String) {
        viewModelScope.launch {
            settingsRepository.setGeminiApiKey(key)
            _apiKeyTestMessage.value = null
        }
    }

    fun testApiKey() {
        viewModelScope.launch {
            _apiKeyTestMessage.value = "Testing…"
            val apiKey = settingsRepository.settingsFlow.first().geminiApiKey
            val result = geminiRepository.generateContent(apiKey, "Reply with exactly: OK")
            _apiKeyTestMessage.value = result.fold(
                onSuccess = { "OK – API key works." },
                onFailure = { "Failed: ${it.message}" }
            )
        }
    }
}
