package com.studyasist.ui.flashcard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studyasist.data.local.entity.QA
import com.studyasist.data.srs.SRSRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FlashcardUiState(
    val cards: List<QA> = emptyList(),
    val currentIndex: Int = 0,
    val isFlipped: Boolean = false,
    val correct: Int = 0,
    val wrong: Int = 0,
    val sessionComplete: Boolean = false,
    val isLoading: Boolean = true
)

@HiltViewModel
class FlashcardViewModel @Inject constructor(
    private val srsRepository: SRSRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FlashcardUiState())
    val uiState: StateFlow<FlashcardUiState> = _uiState.asStateFlow()

    init {
        loadCards()
    }

    private fun loadCards() {
        viewModelScope.launch {
            val cards = srsRepository.getDueCards(10)
            _uiState.value = FlashcardUiState(cards = cards, isLoading = false)
        }
    }

    fun flipCard() {
        _uiState.value = _uiState.value.copy(isFlipped = !_uiState.value.isFlipped)
    }

    fun swipeRight() {
        val state = _uiState.value
        val card = state.cards.getOrNull(state.currentIndex) ?: return
        viewModelScope.launch {
            srsRepository.processReview(card.id, 2)
            advance(correct = true)
        }
    }

    fun swipeLeft() {
        val state = _uiState.value
        val card = state.cards.getOrNull(state.currentIndex) ?: return
        viewModelScope.launch {
            srsRepository.processReview(card.id, 0)
            advance(correct = false)
        }
    }

    fun skip() {
        advance(correct = false, skipped = true)
    }

    private fun advance(correct: Boolean, skipped: Boolean = false) {
        val state = _uiState.value
        val nextIndex = state.currentIndex + 1
        if (nextIndex >= state.cards.size) {
            _uiState.value = state.copy(
                correct = state.correct + if (correct) 1 else 0,
                wrong = state.wrong + if (!correct && !skipped) 1 else 0,
                sessionComplete = true,
                isFlipped = false
            )
        } else {
            _uiState.value = state.copy(
                currentIndex = nextIndex,
                correct = state.correct + if (correct) 1 else 0,
                wrong = state.wrong + if (!correct && !skipped) 1 else 0,
                isFlipped = false
            )
        }
    }
}
