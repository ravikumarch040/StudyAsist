package com.studyasist.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "study_tool_history", indices = [Index(value = ["toolType", "usedAt"])])
data class StudyToolHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val toolType: String, // "dictate", "explain", "solve"
    val inputText: String,
    val usedAt: Long
)
