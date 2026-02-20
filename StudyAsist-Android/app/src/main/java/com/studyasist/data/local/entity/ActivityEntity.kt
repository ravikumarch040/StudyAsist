package com.studyasist.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "activities",
    foreignKeys = [
        ForeignKey(
            entity = TimetableEntity::class,
            parentColumns = ["id"],
            childColumns = ["timetableId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["timetableId", "dayOfWeek"]),
        Index(value = ["timetableId", "dayOfWeek", "startTimeMinutes"])
    ]
)
data class ActivityEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timetableId: Long,
    val dayOfWeek: Int, // 1 = Monday .. 7 = Sunday (Calendar.SUNDAY = 1 in Java is Sunday; we use 1=Mon)
    val startTimeMinutes: Int,
    val endTimeMinutes: Int,
    val title: String,
    val type: ActivityType,
    val note: String? = null,
    val notifyEnabled: Boolean = false,
    val notifyLeadMinutes: Int = 0,
    val useSpeechSound: Boolean = false,
    val alarmTtsMessage: String? = null,
    val sortOrder: Int = 0
)
