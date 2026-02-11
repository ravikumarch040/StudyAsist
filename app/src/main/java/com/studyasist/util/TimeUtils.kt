package com.studyasist.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun formatTimeMinutes(minutesFromMidnight: Int): String {
    val h = minutesFromMidnight / 60
    val m = minutesFromMidnight % 60
    return "%d:%02d".format(h, m)
}

fun timeToMinutes(hour: Int, minute: Int): Int = hour * 60 + minute

fun formatExamDate(epochMillis: Long): String {
    val sdf = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
    return sdf.format(Date(epochMillis))
}

fun daysUntil(epochMillis: Long): Long {
    val now = System.currentTimeMillis()
    val diff = epochMillis - now
    return (diff / (24 * 60 * 60 * 1000)).coerceAtLeast(0)
}
