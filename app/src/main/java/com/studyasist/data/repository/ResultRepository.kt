package com.studyasist.data.repository

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

    private fun String.escapeCsv(): String {
        return if (contains(',') || contains('"') || contains('\n')) {
            "\"" + replace("\"", "\"\"") + "\""
        } else this
    }
}
