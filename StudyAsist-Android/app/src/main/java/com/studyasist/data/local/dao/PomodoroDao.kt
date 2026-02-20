package com.studyasist.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.studyasist.data.local.entity.PomodoroSession
import kotlinx.coroutines.flow.Flow

@Dao
interface PomodoroDao {

    @Query("SELECT * FROM pomodoro_sessions ORDER BY startedAt DESC")
    fun getAllFlow(): Flow<List<PomodoroSession>>

    @Query("SELECT * FROM pomodoro_sessions WHERE completed = 1 AND startedAt >= :since ORDER BY startedAt DESC")
    suspend fun getCompletedSince(since: Long): List<PomodoroSession>

    @Query("SELECT SUM(durationMinutes) FROM pomodoro_sessions WHERE completed = 1 AND type = 'focus' AND startedAt >= :since")
    suspend fun getTotalFocusMinutesSince(since: Long): Int?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: PomodoroSession): Long

    @Update
    suspend fun update(session: PomodoroSession)

    @Query("DELETE FROM pomodoro_sessions")
    suspend fun deleteAll()
}
