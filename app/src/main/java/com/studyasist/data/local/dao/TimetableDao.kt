package com.studyasist.data.local.dao

import com.studyasist.data.local.entity.TimetableEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface TimetableDao {

    @Query("SELECT * FROM timetables ORDER BY updatedAt DESC")
    fun getAll(): Flow<List<TimetableEntity>>

    @Query("SELECT * FROM timetables WHERE id = :id")
    suspend fun getById(id: Long): TimetableEntity?

    @Query("SELECT * FROM timetables WHERE id = :id")
    fun getByIdFlow(id: Long): Flow<TimetableEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: TimetableEntity): Long

    @Update
    suspend fun update(entity: TimetableEntity)

    @Query("DELETE FROM timetables WHERE id = :id")
    suspend fun deleteById(id: Long)
}
