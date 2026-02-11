package com.studyasist.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.studyasist.data.local.entity.Attempt
import kotlinx.coroutines.flow.Flow

@Dao
interface AttemptDao {

    @Query("SELECT * FROM attempts WHERE id = :id")
    suspend fun getById(id: Long): Attempt?

    @Query("SELECT * FROM attempts WHERE assessmentId = :assessmentId ORDER BY startedAt DESC")
    fun getByAssessmentId(assessmentId: Long): Flow<List<Attempt>>

    @Query("SELECT * FROM attempts WHERE assessmentId = :assessmentId ORDER BY startedAt DESC")
    suspend fun getByAssessmentIdOnce(assessmentId: Long): List<Attempt>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: Attempt): Long

    @Update
    suspend fun update(entity: Attempt)

    @Query("DELETE FROM attempts WHERE id = :id")
    suspend fun deleteById(id: Long)
}
