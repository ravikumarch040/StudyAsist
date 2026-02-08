package com.studyasist.util

fun formatTimeMinutes(minutesFromMidnight: Int): String {
    val h = minutesFromMidnight / 60
    val m = minutesFromMidnight % 60
    return "%d:%02d".format(h, m)
}

fun timeToMinutes(hour: Int, minute: Int): Int = hour * 60 + minute
