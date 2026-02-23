package com.studyasist

import com.studyasist.data.repository.GoalRepository.GoalItemInput
import com.studyasist.data.repository.GoalRepository
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.ext.junit.runners.AndroidJUnit4
import javax.inject.Inject

/**
 * Integration tests for GoalRepository (TC-GR01, TC-GR02, TC-GR03, TC-GR05).
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class GoalRepositoryIntegrationTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var goalRepository: GoalRepository

    @Inject
    lateinit var database: com.studyasist.data.local.db.AppDatabase

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun createGoal_andGetById() = runBlocking {
        val examDate = System.currentTimeMillis() + 86400000 * 30
        val goalId = goalRepository.createGoal(
            name = "Test Goal",
            description = "Desc",
            examDate = examDate,
            items = listOf(GoalItemInput(subject = "Math", chapterList = "Ch1,Ch2", targetHours = 10))
        )
        assertTrue(goalId > 0)
        val goal = goalRepository.getGoal(goalId)
        assertNotNull(goal)
        assertEquals("Test Goal", goal!!.name)
        assertEquals(examDate, goal.examDate)
    }

    @Test
    fun getActiveGoals_returnsCreatedGoals() = runBlocking {
        val future = System.currentTimeMillis() + 86400000 * 30
        goalRepository.createGoal("Goal1", "", future, emptyList())
        goalRepository.createGoal("Goal2", "", future, emptyList())
        val active = goalRepository.getActiveGoalsOnce()
        assertTrue(active.size >= 2)
        assertTrue(active.any { it.name == "Goal1" })
        assertTrue(active.any { it.name == "Goal2" })
    }

    @Test
    fun addGoalItem() = runBlocking {
        val goalId = goalRepository.createGoal("G", "", System.currentTimeMillis() + 86400000, emptyList())
        goalRepository.addGoalItem(goalId, "Physics", "Mechanics", null)
        val items = goalRepository.getGoalItems(goalId)
        assertEquals(1, items.size)
        assertEquals("Physics", items[0].subject)
        assertTrue(items[0].chapterList.contains("Mechanics"))
    }
}
