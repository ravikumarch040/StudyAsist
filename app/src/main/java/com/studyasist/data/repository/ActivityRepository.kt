package com.studyasist.data.repository

import com.studyasist.data.local.dao.ActivityDao
import com.studyasist.data.local.entity.ActivityEntity
import com.studyasist.data.local.entity.ActivityType
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ActivityRepository @Inject constructor(
    private val activityDao: ActivityDao
) {

    fun getActivitiesForTimetable(timetableId: Long): Flow<List<ActivityEntity>> =
        activityDao.getAllForTimetable(timetableId)

    suspend fun getActivitiesForDay(timetableId: Long, dayOfWeek: Int): List<ActivityEntity> =
        activityDao.getByTimetableAndDay(timetableId, dayOfWeek)

    suspend fun getActivity(id: Long): ActivityEntity? = activityDao.getById(id)

    suspend fun hasOverlap(
        timetableId: Long,
        dayOfWeek: Int,
        startMinutes: Int,
        endMinutes: Int,
        excludeActivityId: Long = 0L
    ): List<ActivityEntity> =
        activityDao.getOverlapping(timetableId, dayOfWeek, startMinutes, endMinutes, excludeActivityId)

    suspend fun insertActivity(entity: ActivityEntity): Long = activityDao.insert(entity)

    suspend fun updateActivity(entity: ActivityEntity) = activityDao.update(entity)

    suspend fun deleteActivity(id: Long) = activityDao.deleteById(id)

    suspend fun copyDayToDay(timetableId: Long, fromDay: Int, toDay: Int) {
        if (fromDay == toDay) return
        val activities = activityDao.getByTimetableAndDay(timetableId, fromDay)
        activities.forEach { act ->
            activityDao.insert(
                act.copy(
                    id = 0,
                    dayOfWeek = toDay
                )
            )
        }
    }
}
