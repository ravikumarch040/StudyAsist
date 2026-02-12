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
    val recentAttempts: List<RecentAttemptSummary>
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
        val qaAttempted = if (assessments.isEmpty()) 0 else {
            val attemptIds = assessments.flatMap { attemptDao.getByAssessmentIdOnce(it.id).map { a -> a.id } }
            if (attemptIds.isEmpty()) 0 else {
                val answers = attemptAnswerDao.getByAttemptIds(attemptIds)
                answers.map { it.qaId }.toSet().size
            }
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

        return GoalDashboardMetrics(
            totalQuestions = totalQuestions,
            questionsPracticed = qaAttempted,
            percentComplete = percentComplete,
            recentAttempts = recentAttempts
        )
    }
}
