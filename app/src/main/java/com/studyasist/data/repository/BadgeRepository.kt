package com.studyasist.data.repository

import com.studyasist.data.gamification.BadgeDefinitions
import com.studyasist.data.local.dao.AttemptDao
import com.studyasist.data.local.dao.BadgeDao
import com.studyasist.data.local.dao.ResultDao
import com.studyasist.data.local.entity.BadgeEarned
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BadgeRepository @Inject constructor(
    private val badgeDao: BadgeDao,
    private val attemptDao: AttemptDao,
    private val attemptAnswerDao: com.studyasist.data.local.dao.AttemptAnswerDao,
    private val resultDao: ResultDao
) {

    fun getEarnedBadgesFlow(): Flow<List<EarnedBadge>> =
        badgeDao.getAllEarnedFlow().map { list ->
            list.mapNotNull { earned ->
                BadgeDefinitions.get(earned.badgeId)?.let { def ->
                    EarnedBadge(
                        id = def.id,
                        titleResId = def.titleResId,
                        descriptionResId = def.descriptionResId,
                        earnedAt = earned.earnedAt
                    )
                }
            }
        }

    suspend fun getEarnedBadgesOnce(): List<EarnedBadge> =
        badgeDao.getAllEarnedOnce().mapNotNull { earned ->
            BadgeDefinitions.get(earned.badgeId)?.let { def ->
                EarnedBadge(
                    id = def.id,
                    titleResId = def.titleResId,
                    descriptionResId = def.descriptionResId,
                    earnedAt = earned.earnedAt
                )
            }
        }

    /**
     * Check and award badges after an assessment is completed.
     */
    suspend fun checkAndAwardAfterAttempt(attemptId: Long) {
        val result = resultDao.getByAttemptId(attemptId) ?: return
        val earnedIds = badgeDao.getEarnedBadgeIds().toSet()

        if (!earnedIds.contains("first_assessment")) {
            awardBadge("first_assessment")
        }
        if (result.percent >= 100f && !earnedIds.contains("perfect_score")) {
            awardBadge("perfect_score")
        }
        val totalAttempts = attemptDao.getAll().count { it.endedAt != null }
        if (totalAttempts >= 10 && !earnedIds.contains("assessments_10")) {
            awardBadge("assessments_10")
        }
        checkAndAwardQuestionBadge()
    }

    /**
     * Check streak-based badges. Call when streak is computed.
     */
    suspend fun checkAndAwardStreakBadges(currentStreak: Int) {
        val earnedIds = badgeDao.getEarnedBadgeIds().toSet()
        if (currentStreak >= 7 && !earnedIds.contains("streak_7")) {
            awardBadge("streak_7")
        }
        if (currentStreak >= 30 && !earnedIds.contains("streak_30")) {
            awardBadge("streak_30")
        }
    }

    /**
     * Check question-count badge. Call after attempt or when viewing badges.
     */
    suspend fun checkAndAwardQuestionBadge() {
        val allAttempts = attemptDao.getAll().filter { it.endedAt != null }
        if (allAttempts.isEmpty()) return
        val attemptIds = allAttempts.map { it.id }
        val answers = attemptAnswerDao.getByAttemptIds(attemptIds)
        val uniqueCount = answers.map { it.qaId }.toSet().size
        if (uniqueCount >= 100) {
            val earnedIds = badgeDao.getEarnedBadgeIds().toSet()
            if (!earnedIds.contains("questions_100")) {
                awardBadge("questions_100")
            }
        }
    }

    private suspend fun awardBadge(badgeId: String) {
        badgeDao.insert(BadgeEarned(badgeId = badgeId))
    }
}
