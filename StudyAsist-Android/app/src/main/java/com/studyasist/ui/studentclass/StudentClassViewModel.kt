package com.studyasist.ui.studentclass

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studyasist.data.repository.StudentClassProfile
import com.studyasist.data.repository.StudentClassRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StudentClassUiState(
    val standard: String = "",
    val board: String = "",
    val school: String = "",
    val city: String = "",
    val state: String = "",
    val subjects: List<String> = emptyList(),
    val newSubjectInput: String = "",
    val isLoading: Boolean = false,
    val saveMessage: String? = null
)

val STANDARD_OPTIONS = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12")
val BOARD_OPTIONS = listOf(
    "CBSE", "ICSE", "State Board", "IB", "IGCSE",
    "Maharashtra", "Karnataka", "Tamil Nadu", "West Bengal", "Gujarat", "Rajasthan", "Other"
)

@HiltViewModel
class StudentClassViewModel @Inject constructor(
    private val studentClassRepository: StudentClassRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StudentClassUiState())
    val uiState: StateFlow<StudentClassUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val profile = studentClassRepository.getProfile()
            _uiState.update {
                it.copy(
                    standard = profile.standard,
                    board = profile.board,
                    school = profile.school ?: "",
                    city = profile.city ?: "",
                    state = profile.state ?: "",
                    subjects = profile.subjects
                )
            }
        }
    }

    fun setStandard(s: String) { _uiState.update { it.copy(standard = s) } }
    fun setBoard(s: String) { _uiState.update { it.copy(board = s) } }
    fun setSchool(s: String) { _uiState.update { it.copy(school = s) } }
    fun setCity(s: String) { _uiState.update { it.copy(city = s) } }
    fun setState(s: String) { _uiState.update { it.copy(state = s) } }
    fun setNewSubjectInput(s: String) { _uiState.update { it.copy(newSubjectInput = s) } }

    fun addSubject() {
        val input = _uiState.value.newSubjectInput.trim()
        if (input.isNotBlank() && input !in _uiState.value.subjects) {
            _uiState.update {
                it.copy(
                    subjects = it.subjects + input,
                    newSubjectInput = ""
                )
            }
        }
    }

    fun removeSubject(subject: String) {
        _uiState.update {
            it.copy(subjects = it.subjects - subject)
        }
    }

    fun clearSaveMessage() {
        _uiState.update { it.copy(saveMessage = null) }
    }

    fun save() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, saveMessage = null) }
            val s = _uiState.value
            studentClassRepository.saveProfile(
                StudentClassProfile(
                    standard = s.standard,
                    board = s.board,
                    school = s.school.takeIf { it.isNotBlank() },
                    city = s.city.takeIf { it.isNotBlank() },
                    state = s.state.takeIf { it.isNotBlank() },
                    subjects = s.subjects
                )
            )
            _uiState.update {
                it.copy(
                    isLoading = false,
                    saveMessage = "Saved"
                )
            }
        }
    }
}
