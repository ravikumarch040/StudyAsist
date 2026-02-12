package com.studyasist.data.repository

import com.studyasist.data.local.dao.AssessmentDao
import com.studyasist.data.local.dao.AttemptAnswerDao
import com.studyasist.data.local.dao.AttemptDao
import com.studyasist.data.local.dao.QADao
import com.studyasist.data.local.dao.ResultDao
import javax.inject.Inject
import javax.inject.Singleton


data class GoalDashboardMetrics(
    val totalQuestions: Int,
    val questionsPracticed: Int,
    val percentComplete: Float,
    val recentAttempts: List<RecentAttemptSummary>,
    val subjectProgress: List<SubjectChapterProgress> = emptyList()
)

data class SubjectChapterProgress(
    val subject: String,
    val chapterLabel: String,
    val practiced: Int,
    val total: Int,
    val percent: Float,
    val targetHours: Int? = null
)

data class RecentAttemptSummary(
    val assessmentTitle: String,
    val attemptLabel: String,
    val percent: Float,
    val attemptId: Long
)

@Singleton
class GoalDashboardRepository @Inject constructor(
    private val goalRepository: com.studyasist.data.repository.GoalRepository,
    private val qaDao: QADao,
    private val assessmentDao: AssessmentDao,
    private val attemptDao: AttemptDao,
    private val attemptAnswerDao: AttemptAnswerDao,
    private val resultDao: ResultDao
) {

    suspend fun getDashboardMetrics(goalId: Long): GoalDashboardMetrics {
        val items = goalRepository.getGoalItems(goalId)
        val totalQuestions = items.sumOf { item ->
            val chapters = item.chapterList.split(",").map { it.trim() }.filter { it.isNotBlank() }
            if (chapters.isEmpty()) {
                qaDao.countBySubjectChapter(item.subject, null)
            } else {
                chapters.sumOf { ch -> qaDao.countBySubjectChapter(item.subject, ch) }
            }
        }

        val assessments = assessmentDao.getByGoalIdOnce(goalId)
        val attemptIds = assessments.flatMap { attemptDao.getByAssessmentIdOnce(it.id).map { a -> a.id } }
        val qaAttempted = if (attemptIds.isEmpty()) 0 else {
            attemptAnswerDao.getByAttemptIds(attemptIds).map { it.qaId }.toSet().size
        }

        val percentComplete = if (totalQuestions > 0) {
            (qaAttempted.toFloat() / totalQuestions * 100f).coerceIn(0f, 100f)
        } else 0f

        val assessmentIds = assessments.map { it.id }
        val recentAttempts = if (assessmentIds.isEmpty()) emptyList() else {
            val resultRows = resultDao.getResultsForAssessments(assessmentIds, 10)
            resultRows.map { row ->
                val assessment = assessmentDao.getById(row.assessmentId)
                val attempts = attemptDao.getByAssessmentIdOnce(row.assessmentId).sortedBy { it.startedAt }
                val attemptNum = attempts.indexOfFirst { it.id == row.attemptId }.let { if (it >= 0) it + 1 else 1 }
                RecentAttemptSummary(
                    assessmentTitle = assessment?.title ?: "Assessment",
                    attemptLabel = "Attempt $attemptNum",
                    percent = row.percent,
                    attemptId = row.attemptId
                )
            }
        }

        val practicedQaIds = if (assessments.isEmpty() || attemptIds.isEmpty()) emptySet() else {
            attemptAnswerDao.getByAttemptIds(attemptIds).map { it.qaId }.toSet()
        }
        val practicedQas = if (practicedQaIds.isEmpty()) emptyList() else qaDao.getByIds(practicedQaIds.toList())
        val practicedBySubjectChapter = practicedQas.groupingBy { it.subject to (it.chapter ?: "") }.eachCount()

        val subjectProgress = items.map { item ->
            val chapters = item.chapterList.split(",").map { it.trim() }.filter { it.isNotBlank() }
            val pairs = if (chapters.isEmpty()) listOf(item.subject to null) else chapters.map { item.subject to it }
            val itemTotal = pairs.sumOf { (subj, ch) -> qaDao.countBySubjectChapter(subj, ch) }
            val itemPracticed = pairs.sumOf { (subj, ch) ->
                if (ch == null) {
                    practicedBySubjectChapter.entries.filter { it.key.first == subj }.sumOf { it.value }
                } else {
                    practicedBySubjectChapter[subj to ch] ?: 0
                }
            }
            val itemPercent = if (itemTotal > 0) (itemPracticed.toFloat() / itemTotal * 100f).coerceIn(0f, 100f) else 0f
            val chapterLabel = if (chapters.isEmpty()) "All" else chapters.joinToString(", ")
            SubjectChapterProgress(
                subject = item.subject,
                chapterLabel = chapterLabel,
                practiced = itemPracticed,
                total = itemTotal,
                percent = itemPercent,
                targetHours = item.targetHours
            )
        }

        return GoalDashboardMetrics(
            totalQuestions = totalQuestions,
            questionsPracticed = qaAttempted,
            percentComplete = percentComplete,
            recentAttempts = recentAttempts,
            subjectProgress = subjectProgress
        )
    }
}
