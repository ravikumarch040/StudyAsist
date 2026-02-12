package com.studyasist.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.studyasist.data.local.entity.AttemptAnswer

@Dao
interface AttemptAnswerDao {

    @Query("SELECT * FROM attempt_answers WHERE attemptId = :attemptId ORDER BY id")
    suspend fun getByAttemptId(attemptId: Long): List<AttemptAnswer>

    @Query("SELECT * FROM attempt_answers WHERE attemptId IN (:attemptIds)")
    suspend fun getByAttemptIds(attemptIds: List<Long>): List<AttemptAnswer>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: AttemptAnswer): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<AttemptAnswer>)
}
