package com.studyasist.data.gamification

data class BadgeDefinition(
    val id: String,
    val title: String,
    val description: String
)

object BadgeDefinitions {
    val ALL = listOf(
        BadgeDefinition("first_assessment", "First Step", "Complete your first assessment"),
        BadgeDefinition("streak_7", "Week Warrior", "Maintain a 7-day study streak"),
        BadgeDefinition("streak_30", "Monthly Master", "Maintain a 30-day study streak"),
        BadgeDefinition("perfect_score", "Perfect", "Get 100% on an assessment"),
        BadgeDefinition("assessments_10", "Dedicated", "Complete 10 assessments"),
        BadgeDefinition("questions_100", "Century", "Practice 100 questions")
    )

    fun get(id: String): BadgeDefinition? = ALL.find { it.id == id }
}
