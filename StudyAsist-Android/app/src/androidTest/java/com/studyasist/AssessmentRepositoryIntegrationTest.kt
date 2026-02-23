package com.studyasist

import com.studyasist.data.local.entity.Goal
import com.studyasist.data.local.entity.GoalItem
import com.studyasist.data.local.entity.QA
import com.studyasist.data.local.entity.QuestionType
import com.studyasist.data.repository.AssessmentRepository
import com.studyasist.data.repository.GoalRepository
import com.studyasist.data.repository.QABankRepository
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.ext.junit.runners.AndroidJUnit4
import javax.inject.Inject

/**
 * Integration tests for AssessmentRepository (TC-AS01 to TC-AS07).
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class AssessmentRepositoryIntegrationTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var database: com.studyasist.data.local.db.AppDatabase

    @Inject
    lateinit var assessmentRepository: AssessmentRepository

    @Inject
    lateinit var goalRepository: GoalRepository

    @Inject
    lateinit var qaBankRepository: QABankRepository

    private var goalId: Long = 0L
    private var qaId1: Long = 0L
    private var qaId2: Long = 0L

    @Before
    fun setup() = runBlocking {
        hiltRule.inject()
        val now = System.currentTimeMillis()
        goalId = database.goalDao().insert(
            Goal(name = "Test Goal", description = null, examDate = now + 86400000 * 30, createdAt = now, isActive = true)
        )
        database.goalItemDao().insert(
            GoalItem(goalId = goalId, subject = "Math", chapterList = "Ch1,Ch2", targetHours = 10)
        )
        qaId1 = qaBankRepository.insertQA(qa("Q1"))
        qaId2 = qaBankRepository.insertQA(qa("Q2"))
    }

    @After
    fun teardown() {
        database.close()
    }

    private fun qa(title: String) = QA(
        questionText = "Question $title",
        answerText = "Answer",
        questionType = QuestionType.SHORT,
        subject = "Math",
        chapter = "Ch1"
    )

    @Test
    fun createAssessment_returnsId() = runBlocking {
        val id = assessmentRepository.createAssessment(
            title = "Test Assessment",
            goalId = goalId,
            subject = "Math",
            chapter = "Ch1",
            totalTimeSeconds = 600,
            randomizeQuestions = true,
            qaIds = listOf(qaId1, qaId2)
        )
        assertTrue(id > 0)
    }

    @Test
    fun createAssessmentFromRandom_createsWithRandomQAs() = runBlocking {
        repeat(5) { qaBankRepository.insertQA(qa("R$it")) }
        val id = assessmentRepository.createAssessmentFromRandom(
            title = "Random Assessment",
            goalId = null,
            subject = "Math",
            chapter = null,
            totalTimeSeconds = 600,
            randomizeQuestions = true,
            count = 3
        )
        assertTrue(id > 0)
        val withQ = assessmentRepository.getAssessmentWithQuestions(id)
        assertNotNull(withQ)
        assertTrue(withQ!!.questions.size <= 3)
    }

    @Test
    fun createAssessmentFromGoal_createsFromGoalItems() = runBlocking {
        val id = assessmentRepository.createAssessmentFromGoal(
            title = "Goal Assessment",
            goalId = goalId,
            totalTimeSeconds = 600,
            randomizeQuestions = true,
            count = 2
        )
        assertTrue(id > 0)
        val withQ = assessmentRepository.getAssessmentWithQuestions(id)
        assertNotNull(withQ)
        assertEquals("Goal Assessment", withQ!!.assessment.title)
    }

    @Test
    fun getAssessmentWithQuestions_returnsFullData() = runBlocking {
        val id = assessmentRepository.createAssessment(
            title = "Full Assessment",
            goalId = goalId,
            subject = "Math",
            chapter = "Ch1",
            totalTimeSeconds = 600,
            randomizeQuestions = false,
            qaIds = listOf(qaId1, qaId2)
        )
        val withQ = assessmentRepository.getAssessmentWithQuestions(id)
        assertNotNull(withQ)
        assertEquals("Full Assessment", withQ!!.assessment.title)
        assertEquals(2, withQ.questions.size)
    }

    @Test
    fun updateAssessment_persisted() = runBlocking {
        val id = assessmentRepository.createAssessment(
            title = "Original",
            goalId = goalId,
            subject = null,
            chapter = null,
            totalTimeSeconds = 600,
            randomizeQuestions = true,
            qaIds = listOf(qaId1)
        )
        val assessment = assessmentRepository.getAssessment(id)!!
        assessmentRepository.updateAssessment(assessment.copy(title = "Updated"))
        assertEquals("Updated", assessmentRepository.getAssessment(id)!!.title)
    }

    @Test
    fun deleteAssessment_removedAndCascade() = runBlocking {
        val id = assessmentRepository.createAssessment(
            title = "To Delete",
            goalId = goalId,
            subject = null,
            chapter = null,
            totalTimeSeconds = 600,
            randomizeQuestions = true,
            qaIds = listOf(qaId1)
        )
        assessmentRepository.deleteAssessment(id)
        assertNull(assessmentRepository.getAssessment(id))
    }

    @Test
    fun createRetryAssessment_createsNewFromOriginal() = runBlocking {
        val origId = assessmentRepository.createAssessment(
            title = "Original",
            goalId = goalId,
            subject = "Math",
            chapter = "Ch1",
            totalTimeSeconds = 600,
            randomizeQuestions = true,
            qaIds = listOf(qaId1, qaId2)
        )
        val retryId = assessmentRepository.createRetryAssessment(origId, listOf(qaId1), " (Weak)")
        assertNotNull(retryId)
        assertTrue(retryId!! > 0)
        val retry = assessmentRepository.getAssessment(retryId)
        assertNotNull(retry)
        assertTrue(retry!!.title.endsWith(" (Weak)"))
    }

    @Test
    fun createRetryAssessment_emptyQaIds_returnsNull() = runBlocking {
        val origId = assessmentRepository.createAssessment(
            title = "Original",
            goalId = goalId,
            subject = null,
            chapter = null,
            totalTimeSeconds = 600,
            randomizeQuestions = true,
            qaIds = listOf(qaId1)
        )
        val retryId = assessmentRepository.createRetryAssessment(origId, emptyList())
        assertNull(retryId)
    }
}
