package com.studyasist.data.repository

import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.studyasist.data.local.dao.AttemptDao
import com.studyasist.data.local.dao.ResultDao
import com.studyasist.data.local.dao.AssessmentDao
import com.studyasist.data.local.entity.Result
import javax.inject.Inject
import javax.inject.Singleton

data class SubjectChapter(
    val subject: String?,
    val chapter: String?
)

data class ResultListItem(
    val resultId: Long,
    val attemptId: Long,
    val assessmentId: Long,
    val assessmentTitle: String,
    val attemptLabel: String,
    val percent: Float,
    val score: Float,
    val maxScore: Float
)

@Singleton
class ResultRepository @Inject constructor(
    private val resultDao: ResultDao,
    private val attemptDao: AttemptDao,
    private val assessmentDao: AssessmentDao
) {

    suspend fun getResult(attemptId: Long): Result? = resultDao.getByAttemptId(attemptId)

    suspend fun updateResult(result: Result) = resultDao.update(result)

    suspend fun getAllResultListItems(): List<ResultListItem> {
        val rows = resultDao.getAllResultsWithAttempt()
        return rows.map { row ->
            val assessment = assessmentDao.getById(row.assessmentId)
            val attempts = attemptDao.getByAssessmentIdOnce(row.assessmentId).sortedBy { it.startedAt }
            val attemptIndex = attempts.indexOfFirst { it.id == row.attemptId }
            val attemptNum = if (attemptIndex >= 0) attemptIndex + 1 else 1
            ResultListItem(
                resultId = row.resultId,
                attemptId = row.attemptId,
                assessmentId = row.assessmentId,
                assessmentTitle = assessment?.title ?: "Assessment",
                attemptLabel = "Attempt $attemptNum",
                percent = row.percent,
                score = row.score,
                maxScore = row.maxScore
            )
        }
    }

    suspend fun saveResult(
        attemptId: Long,
        score: Float,
        maxScore: Float,
        percent: Float,
        detailsJson: String
    ): Long {
        val result = Result(
            attemptId = attemptId,
            score = score,
            maxScore = maxScore,
            percent = percent,
            detailsJson = detailsJson
        )
        return resultDao.insert(result)
    }

    suspend fun getResultsForAttempts(attemptIds: List<Long>): List<Result> =
        resultDao.getByAttemptIds(attemptIds)

    suspend fun getSubjectChapterForAttempt(attemptId: Long): SubjectChapter? {
        val attempt = attemptDao.getById(attemptId) ?: return null
        val assessment = assessmentDao.getById(attempt.assessmentId) ?: return null
        return SubjectChapter(
            subject = assessment.subject?.takeIf { it.isNotBlank() },
            chapter = assessment.chapter?.takeIf { it.isNotBlank() }
        )
    }

    /**
     * Returns CSV content of all results for export.
     * Columns: Assessment,Attempt,Date,Score,Max Score,Percent
     */
    suspend fun getExportCsv(): String {
        val rows = resultDao.getAllResultsWithAttempt()
        val header = "Assessment,Attempt,Date,Score,Max Score,Percent"
        val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.US)
        val lines = mutableListOf(header)
        for (row in rows) {
            val assessment = assessmentDao.getById(row.assessmentId)
            val attempts = attemptDao.getByAssessmentIdOnce(row.assessmentId).sortedBy { it.startedAt }
            val attemptNum = attempts.indexOfFirst { it.id == row.attemptId }.let { if (it >= 0) it + 1 else 1 }
            val title = assessment?.title?.escapeCsv() ?: "Assessment"
            val dateStr = dateFormat.format(java.util.Date(row.startedAt))
            lines.add("$title,Attempt $attemptNum,$dateStr,${row.score},${row.maxScore},${row.percent}")
        }
        return lines.joinToString("\n")
    }

    /**
     * Generates a PDF report of all results.
     * Returns the PDF as a ByteArray suitable for writing to a file.
     */
    suspend fun getExportPdf(): ByteArray {
        val rows = resultDao.getAllResultsWithAttempt()
        val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.US)
        val pageWidth = 595
        val pageHeight = 842
        val margin = 40
        val lineHeight = 24
        val titlePaint = Paint().apply {
            textSize = 24f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        val headerPaint = Paint().apply {
            textSize = 12f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        val normalPaint = Paint().apply {
            textSize = 11f
            isAntiAlias = true
        }
        val document = PdfDocument()
        var y = margin
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        var page = document.startPage(pageInfo)
        var canvas = page.canvas
        canvas.drawText("StudyAsist - Results Report", margin.toFloat(), y.toFloat(), titlePaint)
        y += lineHeight * 2
        canvas.drawText("Generated: ${dateFormat.format(java.util.Date())}", margin.toFloat(), y.toFloat(), normalPaint)
        y += lineHeight * 2
        canvas.drawText("Assessment | Attempt | Date | Score | Max | %", margin.toFloat(), y.toFloat(), headerPaint)
        y += lineHeight
        for (row in rows) {
            if (y > pageHeight - margin - lineHeight * 2) {
                document.finishPage(page)
                val newPageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, document.pages.size + 1).create()
                page = document.startPage(newPageInfo)
                canvas = page.canvas
                y = margin
            }
            val assessment = assessmentDao.getById(row.assessmentId)
            val attempts = attemptDao.getByAssessmentIdOnce(row.assessmentId).sortedBy { it.startedAt }
            val attemptNum = attempts.indexOfFirst { it.id == row.attemptId }.let { if (it >= 0) it + 1 else 1 }
            val title = assessment?.title ?: "Assessment"
            val dateStr = dateFormat.format(java.util.Date(row.startedAt))
            val line = "$title | Attempt $attemptNum | $dateStr | ${row.score} | ${row.maxScore} | ${row.percent}%"
            canvas.drawText(line, margin.toFloat(), y.toFloat(), normalPaint)
            y += lineHeight
        }
        document.finishPage(page)
        val output = java.io.ByteArrayOutputStream()
        document.writeTo(output)
        document.close()
        return output.toByteArray()
    }

    /**
     * Generates a PDF report for a single assessment result (one attempt).
     * Includes score, subject/chapter, date, and per-question details.
     */
    suspend fun getExportPdfForAttempt(attemptId: Long): ByteArray? {
        val result = resultDao.getByAttemptId(attemptId) ?: return null
        val attempt = attemptDao.getById(attemptId) ?: return null
        val assessment = assessmentDao.getById(attempt.assessmentId)
        val attempts = attemptDao.getByAssessmentIdOnce(attempt.assessmentId).sortedBy { it.startedAt }
        val attemptNum = attempts.indexOfFirst { it.id == attemptId }.let { if (it >= 0) it + 1 else 1 }
        val title = assessment?.title ?: "Assessment"
        val subject = assessment?.subject?.takeIf { it.isNotBlank() }
        val chapter = assessment?.chapter?.takeIf { it.isNotBlank() }
        val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.US)
        val dateStr = dateFormat.format(java.util.Date(attempt.startedAt))

        val details = parseDetailsForPdf(result.detailsJson)
        val pageWidth = 595
        val pageHeight = 842
        val margin = 40
        val lineHeight = 18
        val titlePaint = Paint().apply {
            textSize = 22f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        val headerPaint = Paint().apply {
            textSize = 12f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        val normalPaint = Paint().apply {
            textSize = 10f
            isAntiAlias = true
        }

        fun drawWrapped(canvas: android.graphics.Canvas, text: String, x: Float, startY: Int, paint: Paint, maxWidth: Int, maxLines: Int = 3): Int {
            var y = startY
            var remaining = text
            var lines = 0
            while (remaining.isNotBlank() && lines < maxLines) {
                val count = paint.breakText(remaining, true, maxWidth.toFloat(), null)
                val line = remaining.take(count)
                canvas.drawText(line, x, y.toFloat(), paint)
                y += lineHeight
                remaining = remaining.drop(count).trimStart()
                lines++
            }
            return y
        }

        val document = PdfDocument()
        var y = margin
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        var page = document.startPage(pageInfo)
        var canvas = page.canvas

        canvas.drawText("$title - Attempt $attemptNum", margin.toFloat(), y.toFloat(), titlePaint)
        y += lineHeight * 2
        canvas.drawText("Score: %.0f / %.0f (%.0f%%)".format(result.score, result.maxScore, result.percent), margin.toFloat(), y.toFloat(), normalPaint)
        y += lineHeight
        canvas.drawText("Date: $dateStr", margin.toFloat(), y.toFloat(), normalPaint)
        y += lineHeight
        if (subject != null || chapter != null) {
            canvas.drawText("Subject: ${subject ?: "-"} | Chapter: ${chapter ?: "-"}", margin.toFloat(), y.toFloat(), normalPaint)
            y += lineHeight
        }
        y += lineHeight

        canvas.drawText("Details", margin.toFloat(), y.toFloat(), headerPaint)
        y += lineHeight

        for ((idx, d) in details.withIndex()) {
            if (y > pageHeight - margin - lineHeight * 6) {
                document.finishPage(page)
                val newPageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, document.pages.size + 1).create()
                page = document.startPage(newPageInfo)
                canvas = page.canvas
                y = margin
            }
            val icon = when { d.correct -> "OK"; d.partialCredit -> "~"; else -> "X" }
            canvas.drawText("#${idx + 1} $icon (${d.questionScore}/1)", margin.toFloat(), y.toFloat(), normalPaint)
            y += lineHeight
            y = drawWrapped(canvas, "Q: ${d.questionText}", margin.toFloat(), y, normalPaint, pageWidth - margin * 2)
            if (d.userAnswer != null) {
                canvas.drawText("Your answer: ${d.userAnswer.take(100)}", margin.toFloat(), y.toFloat(), normalPaint)
                y += lineHeight
            }
            canvas.drawText("Correct: ${d.modelAnswer.take(80)}", margin.toFloat(), y.toFloat(), normalPaint)
            y += lineHeight
            if (d.feedback.isNotBlank()) {
                canvas.drawText("Feedback: ${d.feedback.take(120)}", margin.toFloat(), y.toFloat(), normalPaint)
                y += lineHeight
            }
            y += lineHeight / 2
        }

        document.finishPage(page)
        val output = java.io.ByteArrayOutputStream()
        document.writeTo(output)
        document.close()
        return output.toByteArray()
    }

    private data class PdfDetailItem(val correct: Boolean, val partialCredit: Boolean, val questionScore: Float, val questionText: String, val userAnswer: String?, val modelAnswer: String, val feedback: String)

    private fun parseDetailsForPdf(json: String): List<PdfDetailItem> = try {
        val arr = org.json.JSONArray(json)
        (0 until arr.length()).mapNotNull { i ->
            val obj = arr.getJSONObject(i)
            val gradeLevel = obj.optString("gradeLevel", "").lowercase()
            val correct = obj.optBoolean("correct")
            val partialCredit = gradeLevel == "partial"
            val questionScore = when (gradeLevel) { "full" -> 1f; "partial" -> 0.5f; else -> 0f }
            PdfDetailItem(
                correct = correct,
                partialCredit = partialCredit,
                questionScore = questionScore,
                questionText = obj.optString("questionText", ""),
                userAnswer = obj.optString("userAnswer").takeIf { it.isNotBlank() },
                modelAnswer = obj.optString("modelAnswer", ""),
                feedback = obj.optString("feedback", "")
            )
        }
    } catch (_: Exception) { emptyList() }

    /**
     * Generates an Excel-compatible XML Spreadsheet 2003 file.
     * Saves as .xls for compatibility; Excel and LibreOffice open it natively.
     * Columns: Assessment, Attempt, Date, Score, Max Score, Percent
     */
    suspend fun getExportExcel(): ByteArray {
        val rows = resultDao.getAllResultsWithAttempt()
        val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.US)
        val sb = StringBuilder()
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
        sb.append("<?mso-application progid=\"Excel.Sheet\"?>\n")
        sb.append("<Workbook xmlns=\"urn:schemas-microsoft-com:office:spreadsheet\" xmlns:ss=\"urn:schemas-microsoft-com:office:spreadsheet\">\n")
        sb.append("  <Worksheet ss:Name=\"Results\">\n")
        sb.append("    <Table>\n")
        sb.append("      <Row><Cell><Data ss:Type=\"String\">Assessment</Data></Cell><Cell><Data ss:Type=\"String\">Attempt</Data></Cell><Cell><Data ss:Type=\"String\">Date</Data></Cell><Cell><Data ss:Type=\"String\">Score</Data></Cell><Cell><Data ss:Type=\"String\">Max Score</Data></Cell><Cell><Data ss:Type=\"String\">Percent</Data></Cell></Row>\n")
        for (row in rows) {
            val assessment = assessmentDao.getById(row.assessmentId)
            val attempts = attemptDao.getByAssessmentIdOnce(row.assessmentId).sortedBy { it.startedAt }
            val attemptNum = attempts.indexOfFirst { it.id == row.attemptId }.let { if (it >= 0) it + 1 else 1 }
            val title = assessment?.title?.escapeXml() ?: "Assessment"
            val dateStr = dateFormat.format(java.util.Date(row.startedAt))
            sb.append("      <Row><Cell><Data ss:Type=\"String\">$title</Data></Cell><Cell><Data ss:Type=\"String\">Attempt $attemptNum</Data></Cell><Cell><Data ss:Type=\"String\">$dateStr</Data></Cell><Cell><Data ss:Type=\"Number\">${row.score}</Data></Cell><Cell><Data ss:Type=\"Number\">${row.maxScore}</Data></Cell><Cell><Data ss:Type=\"Number\">${row.percent}</Data></Cell></Row>\n")
        }
        sb.append("    </Table>\n")
        sb.append("  </Worksheet>\n")
        sb.append("</Workbook>")
        return sb.toString().toByteArray(Charsets.UTF_8)
    }

    private fun String.escapeXml(): String {
        return this
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")
    }

    private fun String.escapeCsv(): String {
        return if (contains(',') || contains('"') || contains('\n')) {
            "\"" + replace("\"", "\"\"") + "\""
        } else this
    }
}
