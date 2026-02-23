package com.studyasist.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

class TimeUtilsTest {

    @Test
    fun formatTimeMinutes_zero() {
        assertEquals("0:00", formatTimeMinutes(0))
    }

    @Test
    fun formatTimeMinutes_hourOnly() {
        assertEquals("6:00", formatTimeMinutes(360))
    }

    @Test
    fun formatTimeMinutes_withMinutes() {
        assertEquals("14:30", formatTimeMinutes(870))
    }

    @Test
    fun formatTimeMinutes_padsMinutes() {
        assertEquals("9:05", formatTimeMinutes(545))
    }

    @Test
    fun timeToMinutes() {
        assertEquals(0, timeToMinutes(0, 0))
        assertEquals(90, timeToMinutes(1, 30))
        assertEquals(1439, timeToMinutes(23, 59))
    }

    @Test
    fun daysUntil_future() {
        val futureEpoch = System.currentTimeMillis() + 3 * 24 * 60 * 60 * 1000
        assertEquals(3, daysUntil(futureEpoch))
    }

    @Test
    fun daysUntil_past_returnsZero() {
        val pastEpoch = System.currentTimeMillis() - 5 * 24 * 60 * 60 * 1000
        assertEquals(0, daysUntil(pastEpoch))
    }

    @Test
    fun formatRelativeTimeAgo_justNow() {
        val now = System.currentTimeMillis()
        assertEquals("Just now", formatRelativeTimeAgo(now))
    }

    @Test
    fun formatRelativeTimeAgo_minutesAgo() {
        val thirtySecAgo = System.currentTimeMillis() - 30 * 1000
        assertEquals("Just now", formatRelativeTimeAgo(thirtySecAgo))
        val twoMinAgo = System.currentTimeMillis() - 2 * 60 * 1000
        assertEquals("2 min ago", formatRelativeTimeAgo(twoMinAgo))
    }

    @Test
    fun formatRelativeTimeAgo_hoursAgo() {
        val threeHrAgo = System.currentTimeMillis() - 3 * 60 * 60 * 1000
        assertEquals("3 hr ago", formatRelativeTimeAgo(threeHrAgo))
    }

    @Test
    fun formatRelativeTimeAgo_daysAgo() {
        val twoDaysAgo = System.currentTimeMillis() - 2 * 24 * 60 * 60 * 1000
        assertEquals("2 days ago", formatRelativeTimeAgo(twoDaysAgo))
    }

    @Test
    fun formatRelativeTimeAgo_futureReturnsDate() {
        val futureEpoch = System.currentTimeMillis() + 24 * 60 * 60 * 1000
        val result = formatRelativeTimeAgo(futureEpoch)
        assert(result.matches(Regex("""[A-Za-z]{3} \d{1,2}""")))
    }

    @Test
    fun formatExamDate_validEpoch() {
        val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        cal.set(2024, Calendar.JUNE, 15, 0, 0, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val epoch = cal.timeInMillis
        val result = formatExamDate(epoch)
        assertFalse(result.isBlank())
        assertTrue(result.contains("2024"))
    }

    @Test
    fun formatExamDate_edgeDates_yearBoundary() {
        val utc = TimeZone.getTimeZone("UTC")
        val dec31_2024 = Calendar.getInstance(utc).apply {
            set(2024, Calendar.DECEMBER, 31, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        val jan1_2025 = Calendar.getInstance(utc).apply {
            set(2025, Calendar.JANUARY, 1, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        assertTrue(formatExamDate(dec31_2024).contains("2024"))
        assertTrue(formatExamDate(jan1_2025).contains("2025"))
    }
}
