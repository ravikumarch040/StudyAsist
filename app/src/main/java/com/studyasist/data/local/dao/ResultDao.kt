package com.studyasist.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.studyasist.data.local.entity.Result
import com.studyasist.data.local.entity.ResultWithAttempt

@Dao
interface ResultDao {

    @Query("SELECT * FROM results WHERE attemptId = :attemptId LIMIT 1")
    suspend fun getByAttemptId(attemptId: Long): Result?

    @Query("SELECT * FROM results WHERE attemptId IN (:attemptIds)")
    suspend fun getByAttemptIds(attemptIds: List<Long>): List<Result>

    @Query("""
        SELECT r.id as resultId, r.attemptId, r.score, r.maxScore, r.percent,
               att.assessmentId, att.startedAt
        FROM results r
        JOIN attempts att ON r.attemptId = att.id
        ORDER BY r.id DESC
    """)
    suspend fun getAllResultsWithAttempt(): List<ResultWithAttempt>

    @Query("""
        SELECT r.id as resultId, r.attemptId, r.score, r.maxScore, r.percent,
               att.assessmentId, att.startedAt
        FROM results r
        JOIN attempts att ON r.attemptId = att.id
        WHERE att.assessmentId IN (:assessmentIds)
        ORDER BY r.id DESC
        LIMIT :limit
    """)
    suspend fun getResultsForAssessments(assessmentIds: List<Long>, limit: Int = 10): List<ResultWithAttempt>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: Result): Long

    @Update
    suspend fun update(entity: Result)
}
