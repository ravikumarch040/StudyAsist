package com.studyasist.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.studyasist.data.local.entity.Assessment
import kotlinx.coroutines.flow.Flow

@Dao
interface AssessmentDao {

    @Query("SELECT * FROM assessments ORDER BY createdAt DESC")
    fun getAll(): Flow<List<Assessment>>

    @Query("SELECT * FROM assessments WHERE goalId = :goalId ORDER BY createdAt DESC")
    fun getByGoalId(goalId: Long): Flow<List<Assessment>>

    @Query("SELECT * FROM assessments WHERE goalId = :goalId ORDER BY createdAt DESC")
    suspend fun getByGoalIdOnce(goalId: Long): List<Assessment>

    @Query("SELECT * FROM assessments WHERE id = :id")
    suspend fun getById(id: Long): Assessment?

    @Query("SELECT * FROM assessments WHERE id = :id")
    fun getByIdFlow(id: Long): Flow<Assessment?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: Assessment): Long

    @Update
    suspend fun update(entity: Assessment)

    @Query("DELETE FROM assessments WHERE id = :id")
    suspend fun deleteById(id: Long)
}
