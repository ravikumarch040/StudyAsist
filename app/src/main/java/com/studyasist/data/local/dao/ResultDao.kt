package com.studyasist.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.studyasist.data.local.entity.Result

@Dao
interface ResultDao {

    @Query("SELECT * FROM results WHERE attemptId = :attemptId LIMIT 1")
    suspend fun getByAttemptId(attemptId: Long): Result?

    @Query("SELECT * FROM results WHERE attemptId IN (:attemptIds)")
    suspend fun getByAttemptIds(attemptIds: List<Long>): List<Result>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: Result): Long
}
