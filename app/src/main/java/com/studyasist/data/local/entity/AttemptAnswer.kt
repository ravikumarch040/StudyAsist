package com.studyasist.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "attempt_answers",
    foreignKeys = [
        ForeignKey(
            entity = Attempt::class,
            parentColumns = ["id"],
            childColumns = ["attemptId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["attemptId"])]
)
data class AttemptAnswer(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val attemptId: Long,
    val qaId: Long,
    val answerText: String? = null,
    val answerImageUri: String? = null,
    val answerVoiceUri: String? = null,
    val submittedAt: Long
)
