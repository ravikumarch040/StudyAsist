package com.studyasist.data.gamification

import com.studyasist.R

data class BadgeDefinition(
    val id: String,
    val titleResId: Int,
    val descriptionResId: Int
)

object BadgeDefinitions {
    val ALL = listOf(
        BadgeDefinition("first_assessment", R.string.badge_first_step, R.string.badge_first_step_desc),
        BadgeDefinition("streak_7", R.string.badge_week_warrior, R.string.badge_week_warrior_desc),
        BadgeDefinition("streak_30", R.string.badge_monthly_master, R.string.badge_monthly_master_desc),
        BadgeDefinition("perfect_score", R.string.badge_perfect, R.string.badge_perfect_desc),
        BadgeDefinition("assessments_10", R.string.badge_dedicated, R.string.badge_dedicated_desc),
        BadgeDefinition("questions_100", R.string.badge_century, R.string.badge_century_desc)
    )

    fun get(id: String): BadgeDefinition? = ALL.find { it.id == id }
}
