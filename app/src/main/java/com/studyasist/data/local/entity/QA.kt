package com.studyasist.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "qa_bank",
    indices = [
        Index(value = ["subject"]),
        Index(value = ["chapter"]),
        Index(value = ["subject", "chapter"])
    ]
)
data class QA(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sourceCaptureId: Long? = null,
    val subject: String? = null,
    val chapter: String? = null,
    val questionText: String,
    val answerText: String,
    val questionType: QuestionType,
    val optionsJson: String? = null,
    val metadataJson: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val easeFactor: Double = 2.5,
    val srsInterval: Int = 0,
    val repetitions: Int = 0,
    val nextReviewDate: Long = 0,
    val lastReviewDate: Long? = null
)
