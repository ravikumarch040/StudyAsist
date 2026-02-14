package com.studyasist.data.repository

data class EarnedBadge(
    val id: String,
    val titleResId: Int,
    val descriptionResId: Int,
    val earnedAt: Long
)
