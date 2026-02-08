package com.studyasist.ui.navigation

object NavRoutes {
    const val HOME = "home"
    const val TIMETABLE_LIST = "timetable_list"
    const val TIMETABLE_DETAIL = "timetable_detail/{timetableId}"
    const val ACTIVITY_EDIT = "activity_edit/{timetableId}/{activityId}"
    const val ACTIVITY_ADD = "activity_add/{timetableId}/{dayOfWeek}"
    const val SETTINGS = "settings"

    fun timetableDetail(timetableId: Long) = "timetable_detail/$timetableId"
    fun activityEdit(timetableId: Long, activityId: Long) = "activity_edit/$timetableId/$activityId"
    fun activityAdd(timetableId: Long, dayOfWeek: Int = 1) = "activity_add/$timetableId/$dayOfWeek"
}
