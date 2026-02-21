package com.studyasist.ui.pdfimport

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studyasist.data.qa.HeuristicQaParser
import com.studyasist.data.qa.PdfExtractor
import com.studyasist.ui.qascan.EditableQARow
import com.studyasist.util.extractTextFromBitmap
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PdfImportUiState(
    val pdfUri: Uri? = null,
    val pageCount: Int = 0,
    val selectedPages: Set<Int> = emptySet(),
    val isLoading: Boolean = false,
    val progressCurrent: Int = 0,
    val progressTotal: Int = 0,
    val parsedRows: List<EditableQARow> = emptyList(),
    val errorMessage: String? = null,
    val importComplete: Boolean = false
)

@HiltViewModel
class PdfImportViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val pdfExtractor: PdfExtractor,
    private val pendingImportHolder: PendingPdfImportHolder
) : ViewModel() {

    private val _uiState = MutableStateFlow(PdfImportUiState())
    val uiState: StateFlow<PdfImportUiState> = _uiState.asStateFlow()

    fun setPdfUri(uri: Uri?) {
        if (uri == null) {
            _uiState.update { PdfImportUiState() }
            return
        }
        viewModelScope.launch {
            val count = pdfExtractor.getPageCount(context, uri)
            _uiState.update {
                it.copy(
                    pdfUri = uri,
                    pageCount = count,
                    selectedPages = (0 until count).toSet(),
                    errorMessage = null
                )
            }
        }
    }

    fun togglePage(index: Int) {
        _uiState.update { state ->
            val newSet = state.selectedPages.toMutableSet()
            if (newSet.contains(index)) newSet.remove(index) else newSet.add(index)
            state.copy(selectedPages = newSet)
        }
    }

    fun selectAllPages() {
        _uiState.update { state ->
            state.copy(selectedPages = (0 until state.pageCount).toSet())
        }
    }

    fun clearSelection() {
        _uiState.update { it.copy(selectedPages = emptySet()) }
    }

    fun processAndImport() {
        val uri = _uiState.value.pdfUri ?: return
        val indices = _uiState.value.selectedPages.sorted()
        if (indices.isEmpty()) {
            _uiState.update { it.copy(errorMessage = context.getString(com.studyasist.R.string.select_pages_hint)) }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(isLoading = true, errorMessage = null, progressCurrent = 0, progressTotal = indices.size)
            }

            val allRows = mutableListOf<EditableQARow>()
            var current = 0

            try {
                val pages = pdfExtractor.extractPages(context, uri)
                for (i in indices) {
                    if (i in pages.indices) {
                        _uiState.update { it.copy(progressCurrent = current + 1) }
                        val page = pages[i]
                        val ocrResult = extractTextFromBitmap(page.bitmap)
                        page.bitmap.recycle()
                        ocrResult.getOrNull()?.let { text ->
                            val parsed = HeuristicQaParser.parse(text).map { p ->
                                EditableQARow(
                                    question = p.question,
                                    answer = p.answer,
                                    type = p.type
                                )
                            }
                            allRows.addAll(parsed)
                        }
                    }
                    current++
                }

                pendingImportHolder.set(
                    if (allRows.isEmpty()) listOf(EditableQARow("", "", com.studyasist.data.local.entity.QuestionType.SHORT))
                    else allRows
                )
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        parsedRows = allRows,
                        importComplete = true
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: context.getString(com.studyasist.R.string.err_extraction_failed)
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
