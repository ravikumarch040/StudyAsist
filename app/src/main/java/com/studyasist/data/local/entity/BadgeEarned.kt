package com.studyasist.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "badges_earned", indices = [Index(value = ["badgeId"], unique = true)])
data class BadgeEarned(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val badgeId: String,
    val earnedAt: Long = System.currentTimeMillis()
)
