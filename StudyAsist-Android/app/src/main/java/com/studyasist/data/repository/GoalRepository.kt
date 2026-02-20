package com.studyasist.data.repository

import com.studyasist.data.local.dao.GoalDao
import com.studyasist.data.local.dao.GoalItemDao
import com.studyasist.data.local.entity.Goal
import com.studyasist.data.local.entity.GoalItem
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoalRepository @Inject constructor(
    private val goalDao: GoalDao,
    private val goalItemDao: GoalItemDao
) {

    fun getAllActiveGoals(): Flow<List<Goal>> = goalDao.getAllActive()

    suspend fun getActiveGoalsOnce(): List<Goal> = goalDao.getAllActiveOnce()

    fun getAllGoals(): Flow<List<Goal>> = goalDao.getAll()

    fun getGoalFlow(id: Long): Flow<Goal?> = goalDao.getByIdFlow(id)

    suspend fun getGoal(id: Long): Goal? = goalDao.getById(id)

    fun getGoalItemsFlow(goalId: Long): Flow<List<GoalItem>> = goalItemDao.getByGoalId(goalId)

    suspend fun getGoalItems(goalId: Long): List<GoalItem> = goalItemDao.getByGoalIdOnce(goalId)

    suspend fun createGoal(
        name: String,
        description: String?,
        examDate: Long,
        items: List<GoalItemInput>
    ): Long {
        val goal = Goal(
            name = name,
            description = description,
            examDate = examDate,
            isActive = true
        )
        val goalId = goalDao.insert(goal)
        if (items.isNotEmpty()) {
            val entities = items.map { input ->
                GoalItem(
                    goalId = goalId,
                    subject = input.subject,
                    chapterList = input.chapterList,
                    targetHours = input.targetHours
                )
            }
            goalItemDao.insertAll(entities)
        }
        return goalId
    }

    suspend fun updateGoal(goal: Goal) {
        goalDao.update(goal)
    }

    suspend fun updateGoalItems(goalId: Long, items: List<GoalItemInput>) {
        goalItemDao.deleteByGoalId(goalId)
        if (items.isNotEmpty()) {
            val entities = items.map { input ->
                GoalItem(
                    goalId = goalId,
                    subject = input.subject,
                    chapterList = input.chapterList,
                    targetHours = input.targetHours
                )
            }
            goalItemDao.insertAll(entities)
        }
    }

    suspend fun addGoalItem(goalId: Long, subject: String, chapterList: String, targetHours: Int?) {
        goalItemDao.insert(
            GoalItem(
                goalId = goalId,
                subject = subject,
                chapterList = chapterList,
                targetHours = targetHours
            )
        )
    }

    suspend fun deleteGoal(id: Long) {
        goalItemDao.deleteByGoalId(id)
        goalDao.deleteById(id)
    }

    data class GoalItemInput(
        val subject: String,
        val chapterList: String,
        val targetHours: Int? = null
    )
}
