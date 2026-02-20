package com.studyasist.data.templates

data class TemplateActivity(
    val dayOfWeek: Int,
    val startHour: Int,
    val startMinute: Int,
    val endHour: Int,
    val endMinute: Int,
    val title: String,
    val type: String,
    val note: String? = null
)

data class TimetableTemplate(
    val name: String,
    val weekType: String = "MON_SUN",
    val activities: List<TemplateActivity>
)

object TimetableTemplates {

    val genericSchoolDay = TimetableTemplate(
        name = "Generic School Day",
        activities = (1..5).flatMap { day ->
            listOf(
                TemplateActivity(day, 6, 0, 6, 30, "Wake up / Get ready", "BREAK"),
                TemplateActivity(day, 7, 0, 13, 0, "School", "SCHOOL"),
                TemplateActivity(day, 13, 0, 14, 0, "Lunch & Rest", "BREAK"),
                TemplateActivity(day, 14, 0, 16, 0, "Study Session 1", "STUDY"),
                TemplateActivity(day, 16, 0, 16, 30, "Snack Break", "BREAK"),
                TemplateActivity(day, 16, 30, 18, 30, "Study Session 2", "STUDY"),
                TemplateActivity(day, 19, 0, 20, 0, "Revision", "STUDY"),
                TemplateActivity(day, 22, 0, 6, 0, "Sleep", "SLEEP")
            )
        }
    )

    val weekendStudy = TimetableTemplate(
        name = "Weekend Study",
        activities = listOf(6, 7).flatMap { day ->
            listOf(
                TemplateActivity(day, 7, 0, 7, 30, "Morning routine", "BREAK"),
                TemplateActivity(day, 8, 0, 10, 0, "Deep Study Block", "STUDY"),
                TemplateActivity(day, 10, 0, 10, 30, "Break", "BREAK"),
                TemplateActivity(day, 10, 30, 12, 30, "Practice & Revision", "STUDY"),
                TemplateActivity(day, 12, 30, 14, 0, "Lunch", "BREAK"),
                TemplateActivity(day, 14, 0, 16, 0, "Study Session", "STUDY"),
                TemplateActivity(day, 16, 0, 17, 0, "Free Time", "BREAK"),
                TemplateActivity(day, 17, 0, 19, 0, "Evening Study", "STUDY"),
                TemplateActivity(day, 22, 0, 7, 0, "Sleep", "SLEEP")
            )
        }
    )

    val examPrep = TimetableTemplate(
        name = "Exam Preparation",
        activities = (1..7).flatMap { day ->
            listOf(
                TemplateActivity(day, 5, 30, 6, 0, "Wake up", "BREAK"),
                TemplateActivity(day, 6, 0, 8, 0, "Early Morning Study", "STUDY"),
                TemplateActivity(day, 8, 0, 8, 30, "Breakfast", "BREAK"),
                TemplateActivity(day, 8, 30, 11, 0, "Study Block 1", "STUDY"),
                TemplateActivity(day, 11, 0, 11, 30, "Short Break", "BREAK"),
                TemplateActivity(day, 11, 30, 13, 30, "Study Block 2", "STUDY"),
                TemplateActivity(day, 13, 30, 14, 30, "Lunch & Rest", "BREAK"),
                TemplateActivity(day, 14, 30, 17, 0, "Practice Tests", "STUDY"),
                TemplateActivity(day, 17, 0, 17, 30, "Break", "BREAK"),
                TemplateActivity(day, 17, 30, 19, 30, "Revision & Review", "STUDY"),
                TemplateActivity(day, 19, 30, 20, 30, "Light Review / Flash Cards", "STUDY"),
                TemplateActivity(day, 22, 0, 5, 30, "Sleep", "SLEEP")
            )
        }
    )

    val all = listOf(genericSchoolDay, weekendStudy, examPrep)
}
