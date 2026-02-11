package com.studyasist.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "goals")
data class Goal(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String? = null,
    val examDate: Long,
    val createdAt: Long = System.currentTimeMillis(),
    val isActive: Boolean = true
)
