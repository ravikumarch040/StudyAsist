package com.studyasist.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pomodoro_sessions")
data class PomodoroSession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val startedAt: Long,
    val endedAt: Long? = null,
    val durationMinutes: Int,
    val type: String,
    val subject: String? = null,
    val completed: Boolean = false
)
