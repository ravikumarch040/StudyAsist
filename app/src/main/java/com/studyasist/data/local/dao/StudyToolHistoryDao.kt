package com.studyasist.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.studyasist.data.local.entity.StudyToolHistoryEntity

@Dao
interface StudyToolHistoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: StudyToolHistoryEntity): Long

    @Query("SELECT * FROM study_tool_history WHERE toolType = :toolType ORDER BY usedAt DESC LIMIT :limit")
    suspend fun getRecentByTool(toolType: String, limit: Int = 10): List<StudyToolHistoryEntity>

    @Query("SELECT * FROM study_tool_history")
    suspend fun getAll(): List<StudyToolHistoryEntity>

    @Query("DELETE FROM study_tool_history")
    suspend fun deleteAll()
}
