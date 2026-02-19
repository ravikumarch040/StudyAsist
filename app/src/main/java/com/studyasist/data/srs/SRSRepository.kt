package com.studyasist.data.srs

import com.studyasist.data.local.dao.QADao
import com.studyasist.data.local.entity.QA
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SRSRepository @Inject constructor(
    private val qaDao: QADao
) {

    suspend fun getDueCards(limit: Int = 20): List<QA> {
        val now = System.currentTimeMillis()
        return qaDao.getDueCards(now, limit)
    }

    suspend fun getDueCount(): Int {
        val now = System.currentTimeMillis()
        return qaDao.getDueCount(now)
    }

    /**
     * Process a review with the SM-2 algorithm.
     * @param qaId The Q&A card ID
     * @param quality 0=Again, 1=Hard, 2=Good, 3=Easy
     */
    suspend fun processReview(qaId: Long, quality: Int) {
        val qa = qaDao.getById(qaId) ?: return
        val result = sm2(quality, qa.easeFactor, qa.srsInterval, qa.repetitions)
        val nextReview = System.currentTimeMillis() + result.interval * 24L * 60L * 60L * 1000L
        qaDao.updateSRS(
            id = qaId,
            easeFactor = result.easeFactor,
            interval = result.interval,
            repetitions = result.repetitions,
            nextReviewDate = nextReview,
            lastReviewDate = System.currentTimeMillis()
        )
    }

    suspend fun getReviewForecast(days: Int): Map<Int, Int> {
        val now = System.currentTimeMillis()
        val dayMs = 24L * 60L * 60L * 1000L
        val forecast = mutableMapOf<Int, Int>()
        for (d in 0 until days) {
            val dayStart = now + d * dayMs
            val dayEnd = dayStart + dayMs
            forecast[d] = qaDao.getDueCountInRange(dayStart, dayEnd)
        }
        return forecast
    }
}
