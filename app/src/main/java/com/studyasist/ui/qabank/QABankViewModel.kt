package com.studyasist.ui.qabank

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studyasist.data.local.entity.QA
import com.studyasist.data.repository.QABankRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class QABankUiState(
    val items: List<QA> = emptyList(),
    val filterSubject: String? = null,
    val filterChapter: String? = null,
    val distinctSubjects: List<String> = emptyList(),
    val distinctChapters: List<String> = emptyList(),
    val isLoading: Boolean = false
)

@HiltViewModel
class QABankViewModel @Inject constructor(
    private val qaBankRepository: QABankRepository
) : ViewModel() {

    private val _filterSubject = MutableStateFlow<String?>(null)
    private val _filterChapter = MutableStateFlow<String?>(null)
    private val _distinctSubjects = MutableStateFlow<List<String>>(emptyList())
    private val _distinctChapters = MutableStateFlow<List<String>>(emptyList())

    private val itemsFlow = combine(_filterSubject, _filterChapter) { subj, ch ->
        Pair(subj, ch)
    }.flatMapLatest { (subj, ch) ->
        qaBankRepository.getQABySubjectChapter(subj, ch).map { items ->
            Triple(items, subj, ch)
        }
    }

    private val _uiState = MutableStateFlow(QABankUiState())
    val uiState: StateFlow<QABankUiState> = combine(
        itemsFlow,
        _distinctSubjects,
        _distinctChapters
    ) { triple, subjects, chapters ->
        val (items, subj, ch) = triple
        QABankUiState(
            items = items,
            filterSubject = subj,
            filterChapter = ch,
            distinctSubjects = subjects,
            distinctChapters = chapters,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = QABankUiState()
    )

    init {
        loadDistinctValues()
    }

    private fun loadDistinctValues() {
        viewModelScope.launch {
            _distinctSubjects.value = qaBankRepository.getDistinctSubjects()
            _distinctChapters.value = qaBankRepository.getDistinctChapters()
        }
    }

    fun setFilterSubject(subject: String?) {
        val subj = subject?.takeIf { it.isNotBlank() }
        _filterSubject.value = subj
        viewModelScope.launch {
            _distinctChapters.value = subj?.let {
                qaBankRepository.getDistinctChaptersForSubject(it)
            } ?: qaBankRepository.getDistinctChapters()
        }
    }

    fun setFilterChapter(chapter: String?) {
        _filterChapter.value = chapter?.takeIf { it.isNotBlank() }
    }

    fun deleteQA(id: Long) {
        viewModelScope.launch {
            qaBankRepository.deleteQA(id)
            loadDistinctValues()
        }
    }
}
