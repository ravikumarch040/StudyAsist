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
    const val QA_BANK_REVISE = "qa_bank_revise/{subject}/{chapter}"
    const val QA_SCAN = "qa_scan"

    const val ASSESSMENT_CREATE = "assessment_create"
    const val ASSESSMENT_CREATE_FOR_GOAL = "assessment_create/{goalId}"
    const val ASSESSMENT_LIST = "assessment_list"
    const val ASSESSMENT_EDIT = "assessment_edit/{assessmentId}"
    const val ASSESSMENT_RUN = "assessment_run/{assessmentId}"
    const val ASSESSMENT_RESULT = "assessment_result/{attemptId}"
    const val RESULT_LIST = "result_list"
    const val ADD_REVISION = "add_revision/{subject}/{chapter}"

    fun timetableDetail(timetableId: Long) = "timetable_detail/$timetableId"
    fun activityEdit(timetableId: Long, activityId: Long) = "activity_edit/$timetableId/$activityId"
    fun activityAdd(timetableId: Long, dayOfWeek: Int = 1) = "activity_add/$timetableId/$dayOfWeek"
    fun goalDetail(goalId: Long) = "goal_detail/$goalId"
    fun addRevision(subject: String, chapter: String?): String =
        "add_revision/${java.net.URLEncoder.encode(subject, "UTF-8")}/${java.net.URLEncoder.encode(chapter ?: "", "UTF-8")}"
    fun qaBankRevise(subject: String?, chapter: String?): String =
        "qa_bank_revise/${java.net.URLEncoder.encode(subject ?: "", "UTF-8")}/${java.net.URLEncoder.encode(chapter ?: "", "UTF-8")}"
    fun goalEdit(goalId: Long) = "goal_edit/$goalId"
    fun assessmentCreateForGoal(goalId: Long) = "assessment_create/$goalId"
    fun assessmentEdit(assessmentId: Long) = "assessment_edit/$assessmentId"
    fun assessmentRun(assessmentId: Long) = "assessment_run/$assessmentId"
    fun assessmentResult(attemptId: Long) = "assessment_result/$attemptId"
}
