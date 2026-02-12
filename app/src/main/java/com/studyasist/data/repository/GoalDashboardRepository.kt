package com.studyasist.data.repository

import com.studyasist.data.local.dao.AssessmentDao
import com.studyasist.data.local.dao.AttemptAnswerDao
import com.studyasist.data.local.dao.AttemptDao
import com.studyasist.data.local.dao.QADao
import com.studyasist.data.local.dao.ResultDao
import com.studyasist.util.daysUntil
import javax.inject.Inject
import javax.inject.Singleton

enum class TrackStatus { ON_TRACK, BEHIND, NOT_ENOUGH_DATA, COMPLETE, EXAM_PASSED }

data class TrackPrediction(
    val status: TrackStatus,
    val projectedPercent: Float? = null,
    val deficitPercent: Float? = null
)

data class GoalDashboardMetrics(
    val totalQuestions: Int,
    val questionsPracticed: Int,
    val percentComplete: Float,
    val recentAttempts: List<RecentAttemptSummary>,
    val subjectProgress: List<SubjectChapterProgress> = emptyList(),
    val trackPrediction: TrackPrediction = TrackPrediction(TrackStatus.NOT_ENOUGH_DATA)
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
        val goal = goalRepository.getGoal(goalId) ?: return GoalDashboardMetrics(
            totalQuestions = 0, questionsPracticed = 0, percentComplete = 0f,
            recentAttempts = emptyList(), subjectProgress = emptyList()
        )
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

        val trackPrediction = computeTrackPrediction(
            goal = goal,
            percentComplete = percentComplete,
            assessments = assessments,
            attemptDao = attemptDao
        )

        return GoalDashboardMetrics(
            totalQuestions = totalQuestions,
            questionsPracticed = qaAttempted,
            percentComplete = percentComplete,
            recentAttempts = recentAttempts,
            subjectProgress = subjectProgress,
            trackPrediction = trackPrediction
        )
    }

    private suspend fun computeTrackPrediction(
        goal: com.studyasist.data.local.entity.Goal,
        percentComplete: Float,
        assessments: List<com.studyasist.data.local.entity.Assessment>,
        attemptDao: AttemptDao
    ): TrackPrediction {
        val daysRemaining = daysUntil(goal.examDate).toInt()
        if (daysRemaining <= 0) return TrackPrediction(TrackStatus.EXAM_PASSED)
        if (percentComplete >= 100f) return TrackPrediction(TrackStatus.COMPLETE)

        val allAttempts = assessments.flatMap { attemptDao.getByAssessmentIdOnce(it.id) }
        if (allAttempts.isEmpty()) return TrackPrediction(TrackStatus.NOT_ENOUGH_DATA)
        val firstActivityMillis = allAttempts.minOfOrNull { it.startedAt } ?: goal.createdAt
        val now = System.currentTimeMillis()
        val daysSinceStart = ((now - firstActivityMillis) / (24 * 60 * 60 * 1000L)).coerceAtLeast(0)

        if (daysSinceStart < 1) return TrackPrediction(TrackStatus.NOT_ENOUGH_DATA)

        val velocity = percentComplete / daysSinceStart
        val projectedPercent = (percentComplete + velocity * daysRemaining).coerceIn(0f, 100f)

        return if (projectedPercent >= 100f) {
            TrackPrediction(TrackStatus.ON_TRACK, projectedPercent = projectedPercent)
        } else {
            TrackPrediction(
                TrackStatus.BEHIND,
                projectedPercent = projectedPercent,
                deficitPercent = 100f - projectedPercent
            )
        }
    }
}
