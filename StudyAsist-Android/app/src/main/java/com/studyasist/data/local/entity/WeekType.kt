package com.studyasist.data.local.entity

import androidx.room.TypeConverter

enum class WeekType {
    MON_SUN,           // Mon–Sun single grid
    MON_SAT_PLUS_SUNDAY // Mon–Sat + separate Sunday (same 7-column grid, Sunday last)
}

class WeekTypeConverter {
    @TypeConverter
    fun from(value: WeekType): String = value.name

    @TypeConverter
    fun to(value: String): WeekType = WeekType.valueOf(value)
}
