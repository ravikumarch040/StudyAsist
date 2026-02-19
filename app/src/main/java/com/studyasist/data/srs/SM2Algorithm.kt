package com.studyasist.data.srs

import kotlin.math.max

data class SM2Result(
    val easeFactor: Double,
    val interval: Int,
    val repetitions: Int
)

/**
 * SM-2 spaced repetition algorithm.
 * @param quality Rating 0–3: 0=Again, 1=Hard, 2=Good, 3=Easy
 * @param easeFactor Current ease factor (starts at 2.5)
 * @param interval Current interval in days
 * @param repetitions Number of consecutive correct reviews
 */
fun sm2(quality: Int, easeFactor: Double, interval: Int, repetitions: Int): SM2Result {
    val q = quality.coerceIn(0, 3)
    val mappedQ = when (q) {
        0 -> 0
        1 -> 3
        2 -> 4
        3 -> 5
        else -> 4
    }

    return if (mappedQ < 3) {
        SM2Result(
            easeFactor = max(1.3, easeFactor - 0.2),
            interval = 1,
            repetitions = 0
        )
    } else {
        val newEF = max(1.3, easeFactor + (0.1 - (5 - mappedQ) * (0.08 + (5 - mappedQ) * 0.02)))
        val newInterval = when (repetitions) {
            0 -> 1
            1 -> 6
            else -> (interval * newEF).toInt().coerceAtLeast(1)
        }
        SM2Result(
            easeFactor = newEF,
            interval = newInterval,
            repetitions = repetitions + 1
        )
    }
}
