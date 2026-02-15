package com.studyasist.util

import com.studyasist.R
import com.studyasist.data.local.entity.ActivityType

fun ActivityType.labelResId(): Int = when (this) {
    ActivityType.STUDY -> R.string.type_study
    ActivityType.BREAK -> R.string.type_break
    ActivityType.SCHOOL -> R.string.type_school
    ActivityType.TUITION -> R.string.type_tuition
    ActivityType.SLEEP -> R.string.type_sleep
}
