package com.studyasist.data.local.entity

import androidx.room.TypeConverter

enum class ActivityType {
    STUDY,
    BREAK,
    SCHOOL,
    TUITION,
    SLEEP
}

class ActivityTypeConverter {
    @TypeConverter
    fun from(value: ActivityType): String = value.name

    @TypeConverter
    fun to(value: String): ActivityType = ActivityType.valueOf(value)
}
