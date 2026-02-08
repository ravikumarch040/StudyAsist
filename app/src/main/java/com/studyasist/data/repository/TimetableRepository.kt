package com.studyasist.data.repository

import com.studyasist.data.local.dao.ActivityDao
import com.studyasist.data.local.dao.TimetableDao
import com.studyasist.data.local.entity.TimetableEntity
import com.studyasist.data.local.entity.WeekType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TimetableRepository @Inject constructor(
    private val timetableDao: TimetableDao,
    private val activityDao: ActivityDao
) {

    fun getAllTimetables(): Flow<List<TimetableEntity>> = timetableDao.getAll()

    fun getTimetableFlow(id: Long): Flow<TimetableEntity?> = timetableDao.getByIdFlow(id)

    suspend fun getTimetable(id: Long): TimetableEntity? = timetableDao.getById(id)

    suspend fun createTimetable(name: String, weekType: WeekType, startDate: Long? = null): Long {
        val now = System.currentTimeMillis()
        val entity = TimetableEntity(
            name = name,
            weekType = weekType,
            startDate = startDate,
            createdAt = now,
            updatedAt = now
        )
        return timetableDao.insert(entity)
    }

    suspend fun updateTimetable(entity: TimetableEntity) {
        timetableDao.update(entity.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun deleteTimetable(id: Long) {
        activityDao.deleteByTimetableId(id)
        timetableDao.deleteById(id)
    }

    suspend fun duplicateTimetable(sourceId: Long, newName: String): Long {
        val source = timetableDao.getById(sourceId) ?: return 0L
        val activities = activityDao.getAllForTimetable(sourceId).first()
        val now = System.currentTimeMillis()
        val newEntity = TimetableEntity(
            name = newName,
            weekType = source.weekType,
            startDate = source.startDate,
            createdAt = now,
            updatedAt = now
        )
        val newId = timetableDao.insert(newEntity)
        activities.forEach { act ->
            activityDao.insert(
                act.copy(id = 0, timetableId = newId)
            )
        }
        return newId
    }

}
