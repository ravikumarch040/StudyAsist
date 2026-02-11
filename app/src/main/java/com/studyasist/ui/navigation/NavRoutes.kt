package com.studyasist.ui.navigation

object NavRoutes {
    const val HOME = "home"
    const val TIMETABLE_LIST = "timetable_list"
    const val TIMETABLE_DETAIL = "timetable_detail/{timetableId}"
    const val ACTIVITY_EDIT = "activity_edit/{timetableId}/{activityId}"
    const val ACTIVITY_ADD = "activity_add/{timetableId}/{dayOfWeek}"
    const val SETTINGS = "settings"
    const val DICTATE = "dictate"
    const val EXPLAIN = "explain"
    const val SOLVE = "solve"

    const val GOAL_LIST = "goal_list"
    const val GOAL_DETAIL = "goal_detail/{goalId}"
    const val GOAL_ADD = "goal_add"
    const val GOAL_EDIT = "goal_edit/{goalId}"

    const val QA_BANK = "qa_bank"
    const val QA_SCAN = "qa_scan"

    fun timetableDetail(timetableId: Long) = "timetable_detail/$timetableId"
    fun activityEdit(timetableId: Long, activityId: Long) = "activity_edit/$timetableId/$activityId"
    fun activityAdd(timetableId: Long, dayOfWeek: Int = 1) = "activity_add/$timetableId/$dayOfWeek"
    fun goalDetail(goalId: Long) = "goal_detail/$goalId"
    fun goalEdit(goalId: Long) = "goal_edit/$goalId"
}
