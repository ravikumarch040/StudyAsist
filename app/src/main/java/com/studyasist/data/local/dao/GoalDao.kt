package com.studyasist.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.studyasist.data.local.entity.Goal
import kotlinx.coroutines.flow.Flow

@Dao
interface GoalDao {

    @Query("SELECT * FROM goals WHERE isActive = 1 ORDER BY examDate ASC")
    fun getAllActive(): Flow<List<Goal>>

    @Query("SELECT * FROM goals ORDER BY examDate ASC")
    fun getAll(): Flow<List<Goal>>

    @Query("SELECT * FROM goals WHERE id = :id")
    suspend fun getById(id: Long): Goal?

    @Query("SELECT * FROM goals WHERE id = :id")
    fun getByIdFlow(id: Long): Flow<Goal?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: Goal): Long

    @Update
    suspend fun update(entity: Goal)

    @Query("DELETE FROM goals WHERE id = :id")
    suspend fun deleteById(id: Long)
}
