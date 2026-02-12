package com.studyasist.data.local.dao

import com.studyasist.data.local.entity.ActivityEntity
import kotlinx.coroutines.flow.Flow
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface ActivityDao {

    @Query("SELECT * FROM activities WHERE timetableId = :timetableId ORDER BY dayOfWeek, startTimeMinutes, sortOrder")
    fun getAllForTimetable(timetableId: Long): Flow<List<ActivityEntity>>

    @Query("SELECT * FROM activities WHERE timetableId = :timetableId AND dayOfWeek = :dayOfWeek ORDER BY startTimeMinutes, sortOrder")
    suspend fun getByTimetableAndDay(timetableId: Long, dayOfWeek: Int): List<ActivityEntity>

    @Query("SELECT * FROM activities WHERE id = :id")
    suspend fun getById(id: Long): ActivityEntity?

    @Query(
        """
        SELECT * FROM activities 
        WHERE timetableId = :timetableId AND dayOfWeek = :dayOfWeek 
        AND (:excludeId = 0 OR id != :excludeId)
        AND (
            (startTimeMinutes < :endMinutes AND endTimeMinutes > :startMinutes)
        )
        """
    )
    suspend fun getOverlapping(
        timetableId: Long,
        dayOfWeek: Int,
        startMinutes: Int,
        endMinutes: Int,
        excludeId: Long = 0L
    ): List<ActivityEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: ActivityEntity): Long

    @Update
    suspend fun update(entity: ActivityEntity)

    @Query("DELETE FROM activities WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM activities WHERE timetableId = :timetableId")
    suspend fun deleteByTimetableId(timetableId: Long)

    @Query("SELECT * FROM activities")
    suspend fun getAll(): List<ActivityEntity>

    @Query("DELETE FROM activities")
    suspend fun deleteAll()
}
