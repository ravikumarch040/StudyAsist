package com.studyasist.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "timetables")
data class TimetableEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val weekType: WeekType,
    val startDate: Long? = null,
    val createdAt: Long,
    val updatedAt: Long
)
