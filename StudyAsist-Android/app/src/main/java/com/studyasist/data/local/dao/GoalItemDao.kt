package com.studyasist.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.studyasist.data.local.entity.GoalItem
import kotlinx.coroutines.flow.Flow

@Dao
interface GoalItemDao {

    @Query("SELECT * FROM goal_items WHERE goalId = :goalId ORDER BY id")
    fun getByGoalId(goalId: Long): Flow<List<GoalItem>>

    @Query("SELECT * FROM goal_items WHERE goalId = :goalId ORDER BY id")
    suspend fun getByGoalIdOnce(goalId: Long): List<GoalItem>

    @Query("SELECT * FROM goal_items WHERE id = :id")
    suspend fun getById(id: Long): GoalItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: GoalItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<GoalItem>)

    @Update
    suspend fun update(entity: GoalItem)

    @Query("DELETE FROM goal_items WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM goal_items WHERE goalId = :goalId")
    suspend fun deleteByGoalId(goalId: Long)

    @Query("SELECT * FROM goal_items")
    suspend fun getAll(): List<GoalItem>

    @Query("DELETE FROM goal_items")
    suspend fun deleteAll()
}
