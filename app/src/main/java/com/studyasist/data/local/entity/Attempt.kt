package com.studyasist.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "attempts",
    foreignKeys = [
        ForeignKey(
            entity = Assessment::class,
            parentColumns = ["id"],
            childColumns = ["assessmentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["assessmentId"])]
)
data class Attempt(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val assessmentId: Long,
    val startedAt: Long,
    val endedAt: Long? = null,
    val userNotes: String? = null,
    val needsManualReview: Boolean = false
)
