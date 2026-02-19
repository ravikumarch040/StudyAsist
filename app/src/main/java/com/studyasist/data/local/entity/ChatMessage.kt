package com.studyasist.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val role: String,
    val content: String,
    val subject: String? = null,
    val chapter: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
