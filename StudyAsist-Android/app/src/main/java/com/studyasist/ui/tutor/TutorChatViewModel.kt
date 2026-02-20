package com.studyasist.ui.tutor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studyasist.data.local.dao.ChatMessageDao
import com.studyasist.data.local.entity.ChatMessage
import com.studyasist.data.repository.GeminiRepository
import com.studyasist.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TutorChatViewModel @Inject constructor(
    private val chatMessageDao: ChatMessageDao,
    private val geminiRepository: GeminiRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val messages: StateFlow<List<ChatMessage>> = chatMessageDao.getAllFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun sendMessage(text: String) {
        val trimmed = text.trim()
        if (trimmed.isBlank()) return

        viewModelScope.launch {
            _isLoading.value = true
            try {
                chatMessageDao.insert(
                    ChatMessage(
                        role = "user",
                        content = trimmed,
                        subject = null,
                        chapter = null
                    )
                )
                val apiKey = settingsRepository.settingsFlow.first().geminiApiKey
                val result = geminiRepository.explain(apiKey, trimmed, "en")
                val response = result.getOrElse { it.message ?: "Something went wrong." }
                chatMessageDao.insert(
                    ChatMessage(
                        role = "assistant",
                        content = response,
                        subject = null,
                        chapter = null
                    )
                )
            } finally {
                _isLoading.value = false
            }
        }
    }
}
