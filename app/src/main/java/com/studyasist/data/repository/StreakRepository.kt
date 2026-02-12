package com.studyasist.data.repository

import com.studyasist.data.local.dao.AttemptDao
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StreakRepository @Inject constructor(
    private val attemptDao: AttemptDao
) {

    /**
     * Returns the current study streak: consecutive days (including today) with at least one completed attempt.
     * Counts backwards from today.
     */
    suspend fun getCurrentStreak(): Int {
        val cal = Calendar.getInstance(Locale.getDefault())
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val dayMs = 24 * 60 * 60 * 1000L
        val timestamps = attemptDao.getCompletedAttemptTimestamps()
        val activeDays = timestamps.map { ts ->
            cal.timeInMillis = ts
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            cal.timeInMillis
        }.toSet()
        if (activeDays.isEmpty()) return 0
        val today = Calendar.getInstance(Locale.getDefault()).apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        var streak = 0
        var check = today
        while (activeDays.contains(check)) {
            streak++
            check -= dayMs
        }
        return streak
    }
}
