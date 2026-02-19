package com.studyasist.data.repository

import android.content.Context
import com.studyasist.R
import com.studyasist.data.local.dao.ActivityDao
import com.studyasist.data.local.dao.AssessmentDao
import com.studyasist.data.local.entity.ActivityType
import dagger.hilt.android.qualifiers.ApplicationContext
import com.studyasist.data.local.dao.AttemptAnswerDao
import com.studyasist.data.local.dao.AttemptDao
import com.studyasist.data.local.dao.QADao
import com.studyasist.data.local.dao.ResultDao
import com.studyasist.util.daysUntil
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

enum class TrackStatus { ON_TRACK, BEHIND, NOT_ENOUGH_DATA, COMPLETE, EXAM_PASSED }

data class TrackPrediction(
    val status: TrackStatus,
    val projectedPercent: Float? = null,
    val deficitPercent: Float? = null,
    /** When using target hours: required study hours per day to hit goal. */
    val requiredHoursPerDay: Float? = null,
    /** When using target hours: actual average study hours per day from timetable. */
    val actualHoursPerDay: Float? = null
)

data class SuggestedPracticeArea(
    val subject: String,
    val chapter: String?,
    val weakCount: Int
)

data class GoalDashboardMetrics(
    val totalQuestions: Int,
    val questionsPracticed: Int,
    val percentComplete: Float,
    val recentAttempts: List<RecentAttemptSummary>,
    val subjectProgress: List<SubjectChapterProgress> = emptyList(),
    val trackPrediction: TrackPrediction = TrackPrediction(TrackStatus.NOT_ENOUGH_DATA),
    val suggestedPractice: List<SuggestedPracticeArea> = emptyList(),
    /** Map of day (ms at midnight) -> number of attempts that day, for last N weeks */
    val activityByDay: Map<Long, Int> = emptyMap()
)

data class SubjectChapterProgress(
    val subject: String,
    val chapterLabel: String,
    val practiced: Int,
    val total: Int,
    val percent: Float,
    val targetHours: Int? = null,
    /** Days since last practice (attempt) that included this subject/chapter; null if never practiced */
    val lastPracticedDaysAgo: Int? = null
)

data class RecentAttemptSummary(
    val assessmentTitle: String,
    val attemptLabel: String,
    val percent: Float,
    val attemptId: Long
)

@Singleton
class GoalDashboardRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val goalRepository: com.studyasist.data.repository.GoalRepository,
    private val qaDao: QADao,
    private val assessmentDao: AssessmentDao,
    private val attemptDao: AttemptDao,
    private val attemptAnswerDao: AttemptAnswerDao,
    private val resultDao: ResultDao,
    private val settingsRepository: SettingsRepository,
    private val activityDao: ActivityDao
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
        val resultRows = if (assessmentIds.isEmpty()) emptyList() else resultDao.getResultsForAssessments(assessmentIds, 15)
        val recentAttempts = if (resultRows.isEmpty()) emptyList() else {
            resultRows.take(10).map { row ->
                val assessment = assessmentDao.getById(row.assessmentId)
                val attempts = attemptDao.getByAssessmentIdOnce(row.assessmentId).sortedBy { it.startedAt }
                val attemptNum = attempts.indexOfFirst { it.id == row.attemptId }.let { if (it >= 0) it + 1 else 1 }
                RecentAttemptSummary(
                    assessmentTitle = assessment?.title ?: context.getString(R.string.assessment_fallback),
                    attemptLabel = context.getString(R.string.attempt_label_format, attemptNum),
                    percent = row.percent,
                    attemptId = row.attemptId
                )
            }
        }

        val practicedQaIds = if (assessments.isEmpty() || attemptIds.isEmpty()) emptySet() else {
            attemptAnswerDao.getByAttemptIds(attemptIds).map { it.qaId }.toSet()
        }
        val practicedQas = if (practicedQaIds.isEmpty()) emptyList() else qaDao.getByIds(practicedQaIds.toList())
        val practicedBySubjectChapter = practicedQas.groupingBy { (it.subject ?: "") to (it.chapter ?: "") }.eachCount()
        val qaIdToSubjectChapter = practicedQas.associate { it.id to ((it.subject ?: "") to (it.chapter ?: "")) }
        val allAttempts = assessments.flatMap { attemptDao.getByAssessmentIdOnce(it.id) }
        val attemptIdToStartedAt = allAttempts.associate { it.id to it.startedAt }
        val lastPracticedAtBySubjectChapter = if (attemptIds.isEmpty()) emptyMap() else {
            val attemptAnswers = attemptAnswerDao.getByAttemptIds(attemptIds)
            val map = mutableMapOf<Pair<String, String>, Long>()
            for (aa in attemptAnswers) {
                val pair = qaIdToSubjectChapter[aa.qaId] ?: continue
                val startedAt = attemptIdToStartedAt[aa.attemptId] ?: continue
                val current = map[pair] ?: 0L
                if (startedAt > current) map[pair] = startedAt
            }
            map
        }

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
            val chapterLabel = if (chapters.isEmpty()) context.getString(R.string.chapter_all) else chapters.joinToString(", ")
            val lastPracticedDaysAgo = if (chapters.isEmpty()) {
                lastPracticedAtBySubjectChapter.entries
                    .filter { it.key.first == item.subject }
                    .maxOfOrNull { it.value }
                    ?.let { millis -> ((System.currentTimeMillis() - millis) / (24 * 60 * 60 * 1000L)).toInt() }
            } else {
                chapters.mapNotNull { ch -> lastPracticedAtBySubjectChapter[item.subject to ch] }.maxOrNull()
                    ?.let { millis -> ((System.currentTimeMillis() - millis) / (24 * 60 * 60 * 1000L)).toInt() }
            }
            SubjectChapterProgress(
                subject = item.subject,
                chapterLabel = chapterLabel,
                practiced = itemPracticed,
                total = itemTotal,
                percent = itemPercent,
                targetHours = item.targetHours,
                lastPracticedDaysAgo = lastPracticedDaysAgo
            )
        }

        val trackPrediction = computeTrackPrediction(
            goal = goal,
            items = items,
            percentComplete = percentComplete,
            assessments = assessments,
            attemptDao = attemptDao
        )

        val suggestedPractice = computeSuggestedPractice(resultRows = resultRows, resultDao = resultDao)

        val activityByDay = computeActivityByDay(allAttempts = assessments.flatMap { attemptDao.getByAssessmentIdOnce(it.id) })

        return GoalDashboardMetrics(
            totalQuestions = totalQuestions,
            questionsPracticed = qaAttempted,
            percentComplete = percentComplete,
            recentAttempts = recentAttempts,
            subjectProgress = subjectProgress,
            trackPrediction = trackPrediction,
            suggestedPractice = suggestedPractice,
            activityByDay = activityByDay
        )
    }

    /** Returns map of day (ms at midnight in default timezone) -> attempt count, for last 12 weeks */
    private fun computeActivityByDay(allAttempts: List<com.studyasist.data.local.entity.Attempt>): Map<Long, Int> {
        val cal = java.util.Calendar.getInstance()
        val dayCounts = mutableMapOf<Long, Int>()
        for (attempt in allAttempts) {
            cal.timeInMillis = attempt.startedAt
            cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
            cal.set(java.util.Calendar.MINUTE, 0)
            cal.set(java.util.Calendar.SECOND, 0)
            cal.set(java.util.Calendar.MILLISECOND, 0)
            val day = cal.timeInMillis
            dayCounts[day] = (dayCounts[day] ?: 0) + 1
        }
        return dayCounts
    }

    private suspend fun computeSuggestedPractice(
        resultRows: List<com.studyasist.data.local.entity.ResultWithAttempt>,
        resultDao: ResultDao
    ): List<SuggestedPracticeArea> {
        val weakByArea = mutableMapOf<Pair<String, String?>, Int>()
        for (row in resultRows) {
            val result = resultDao.getByAttemptId(row.attemptId) ?: continue
            parseWeakAreasFromDetails(result.detailsJson).forEach { (subj, ch) ->
                weakByArea[subj to ch] = (weakByArea[subj to ch] ?: 0) + 1
            }
        }
        return weakByArea.entries
            .sortedByDescending { it.value }
            .take(5)
            .map { (pair, count) ->
                SuggestedPracticeArea(
                    subject = pair.first,
                    chapter = pair.second?.takeIf { it.isNotBlank() },
                    weakCount = count
                )
            }
    }

    private fun parseWeakAreasFromDetails(detailsJson: String): List<Pair<String, String?>> {
        return try {
            val arr = org.json.JSONArray(detailsJson)
            (0 until arr.length()).mapNotNull { i ->
                val obj = arr.getJSONObject(i)
                val gradeLevel = obj.optString("gradeLevel", "").lowercase()
                if (gradeLevel != "wrong" && gradeLevel != "partial") return@mapNotNull null
                val subject = obj.optString("subject", "").trim()
                if (subject.isBlank()) return@mapNotNull null
                val chapter = obj.optString("chapter", "").trim().takeIf { it.isNotBlank() }
                subject to chapter
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private suspend fun computeTrackPrediction(
        goal: com.studyasist.data.local.entity.Goal,
        items: List<com.studyasist.data.local.entity.GoalItem>,
        percentComplete: Float,
        assessments: List<com.studyasist.data.local.entity.Assessment>,
        attemptDao: AttemptDao
    ): TrackPrediction {
        val daysRemaining = daysUntil(goal.examDate).toInt()
        if (daysRemaining <= 0) return TrackPrediction(TrackStatus.EXAM_PASSED)
        if (percentComplete >= 100f) return TrackPrediction(TrackStatus.COMPLETE)

        // When goal items have target hours, compare with timetable study hours
        val totalTargetHours = items.sumOf { it.targetHours ?: 0 }
        if (totalTargetHours > 0 && daysRemaining > 0) {
            val activeTimetableId = settingsRepository.activeTimetableIdFlow.first()
            if (activeTimetableId != null && activeTimetableId > 0) {
                val activities = activityDao.getAllForTimetableOnce(activeTimetableId)
                val studyMinutesPerWeek = activities
                    .filter { it.type == ActivityType.STUDY }
                    .sumOf { it.endTimeMinutes - it.startTimeMinutes }
                val actualHoursPerDay = (studyMinutesPerWeek / 60f) / 7f
                val requiredHoursPerDay = totalTargetHours.toFloat() / daysRemaining
                return if (actualHoursPerDay >= requiredHoursPerDay) {
                    TrackPrediction(
                        TrackStatus.ON_TRACK,
                        requiredHoursPerDay = requiredHoursPerDay,
                        actualHoursPerDay = actualHoursPerDay
                    )
                } else {
                    TrackPrediction(
                        TrackStatus.BEHIND,
                        deficitPercent = ((requiredHoursPerDay - actualHoursPerDay) * daysRemaining / totalTargetHours * 100f).coerceIn(0f, 100f),
                        requiredHoursPerDay = requiredHoursPerDay,
                        actualHoursPerDay = actualHoursPerDay
                    )
                }
            }
        }

        // Fallback: velocity-based prediction from assessment attempts
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
