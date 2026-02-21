package com.studyasist.ui.pdfimport

import com.studyasist.ui.qascan.EditableQARow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Holds parsed Q&A rows from PDF import for consumption by QAScanScreen.
 * Write here before navigating to QA_SCAN; QAScanViewModel reads and clears on init.
 */
@Singleton
class PendingPdfImportHolder @Inject constructor() {
    var rows: List<EditableQARow>? = null
        private set

    fun set(rows: List<EditableQARow>) {
        this.rows = rows
    }

    fun consume(): List<EditableQARow>? {
        val r = rows
        rows = null
        return r
    }
}
