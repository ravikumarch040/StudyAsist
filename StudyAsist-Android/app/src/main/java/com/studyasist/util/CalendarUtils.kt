package com.studyasist.util

import java.util.Calendar

/**
 * Our app uses 1 = Monday, 7 = Sunday.
 * Java Calendar uses 1 = Sunday, 2 = Monday, ..., 7 = Saturday.
 */
fun todayDayOfWeek(): Int {
    val cal = Calendar.getInstance()
    val javaDay = cal.get(Calendar.DAY_OF_WEEK)
    return (javaDay + 5) % 7 + 1
}

fun currentTimeMinutesFromMidnight(): Int {
    val cal = Calendar.getInstance()
    return cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)
}
