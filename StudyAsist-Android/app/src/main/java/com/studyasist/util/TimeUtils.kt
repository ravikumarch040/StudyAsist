package com.studyasist.util

import android.content.Context
import com.studyasist.R
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

/** Returns a human-readable "X ago" string, or date if older than 7 days. Pass context for localized strings. */
fun formatRelativeTimeAgo(epochMillis: Long, context: Context? = null): String {
    val now = System.currentTimeMillis()
    val diffMs = now - epochMillis
    if (diffMs < 0) return SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(epochMillis))
    val diffSec = diffMs / 1000
    val diffMin = diffSec / 60
    val diffHour = diffMin / 60
    val diffDay = diffHour / 24
    return when {
        diffSec < 60 -> context?.getString(R.string.just_now) ?: "Just now"
        diffMin < 60 -> context?.getString(R.string.min_ago_format, diffMin) ?: "$diffMin min ago"
        diffHour < 24 -> context?.getString(R.string.hr_ago_format, diffHour) ?: "$diffHour hr ago"
        diffDay < 7 -> context?.getString(R.string.days_ago_format, diffDay) ?: "$diffDay days ago"
        else -> SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(epochMillis))
    }
}
