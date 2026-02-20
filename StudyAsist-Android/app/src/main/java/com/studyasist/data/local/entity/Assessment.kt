package com.studyasist.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "assessments",
    foreignKeys = [
        ForeignKey(
            entity = Goal::class,
            parentColumns = ["id"],
            childColumns = ["goalId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index(value = ["goalId"]), Index(value = ["subject"]), Index(value = ["chapter"])]
)
data class Assessment(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val goalId: Long? = null,
    val subject: String? = null,
    val chapter: String? = null,
    val totalTimeSeconds: Int,
    val randomizeQuestions: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
