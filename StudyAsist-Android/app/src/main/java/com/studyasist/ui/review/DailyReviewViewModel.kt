package com.studyasist.ui.review

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

data class ReviewUiState(
    val cards: List<QA> = emptyList(),
    val currentIndex: Int = 0,
    val showAnswer: Boolean = false,
    val reviewed: Int = 0,
    val sessionComplete: Boolean = false,
    val isLoading: Boolean = true
)

@HiltViewModel
class DailyReviewViewModel @Inject constructor(
    private val srsRepository: SRSRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReviewUiState())
    val uiState: StateFlow<ReviewUiState> = _uiState.asStateFlow()

    init {
        loadDueCards()
    }

    private fun loadDueCards() {
        viewModelScope.launch {
            val dueCards = srsRepository.getDueCards(30)
            val newCards = if (dueCards.size < 10) {
                srsRepository.run {
                    val dao = Class.forName("com.studyasist.data.local.dao.QADao")
                    emptyList<QA>()
                }
            } else emptyList()
            _uiState.value = ReviewUiState(
                cards = dueCards,
                isLoading = false
            )
        }
    }

    fun toggleAnswer() {
        _uiState.value = _uiState.value.copy(showAnswer = !_uiState.value.showAnswer)
    }

    fun rate(quality: Int) {
        val state = _uiState.value
        val currentCard = state.cards.getOrNull(state.currentIndex) ?: return
        viewModelScope.launch {
            srsRepository.processReview(currentCard.id, quality)
            val nextIndex = state.currentIndex + 1
            if (nextIndex >= state.cards.size) {
                _uiState.value = state.copy(
                    reviewed = state.reviewed + 1,
                    sessionComplete = true,
                    showAnswer = false
                )
            } else {
                _uiState.value = state.copy(
                    currentIndex = nextIndex,
                    reviewed = state.reviewed + 1,
                    showAnswer = false
                )
            }
        }
    }
}
