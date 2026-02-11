package com.studyasist.data.local.entity

/**
 * Result of JOIN between results and attempts tables.
 * Used by ResultDao.getAllResultsWithAttempt().
 */
data class ResultWithAttempt(
    val resultId: Long,
    val attemptId: Long,
    val score: Float,
    val maxScore: Float,
    val percent: Float,
    val assessmentId: Long,
    val startedAt: Long
)
