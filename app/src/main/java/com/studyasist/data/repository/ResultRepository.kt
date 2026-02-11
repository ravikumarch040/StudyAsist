package com.studyasist.data.repository

import com.studyasist.data.local.dao.ResultDao
import com.studyasist.data.local.entity.Result
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ResultRepository @Inject constructor(
    private val resultDao: ResultDao
) {

    suspend fun getResult(attemptId: Long): Result? = resultDao.getByAttemptId(attemptId)

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
}
