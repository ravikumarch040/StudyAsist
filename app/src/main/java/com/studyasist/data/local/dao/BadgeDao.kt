package com.studyasist.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.studyasist.data.local.entity.BadgeEarned
import kotlinx.coroutines.flow.Flow

@Dao
interface BadgeDao {

    @Query("SELECT * FROM badges_earned ORDER BY earnedAt DESC")
    fun getAllEarnedFlow(): Flow<List<BadgeEarned>>

    @Query("SELECT * FROM badges_earned ORDER BY earnedAt DESC")
    suspend fun getAllEarnedOnce(): List<BadgeEarned>

    @Query("SELECT badgeId FROM badges_earned")
    suspend fun getEarnedBadgeIds(): List<String>

    @Query("SELECT 1 FROM badges_earned WHERE badgeId = :badgeId LIMIT 1")
    suspend fun hasBadge(badgeId: String): Boolean?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(entity: BadgeEarned): Long
}
