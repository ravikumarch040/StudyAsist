package com.studyasist

import com.studyasist.data.local.entity.Assessment
import com.studyasist.data.local.entity.AssessmentQuestion
import com.studyasist.data.repository.AssessmentRepository
import com.studyasist.data.repository.AttemptRepository
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
 * Integration tests for AttemptRepository (TC-AT01 to TC-AT06).
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class AttemptRepositoryIntegrationTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var database: com.studyasist.data.local.db.AppDatabase

    @Inject
    lateinit var attemptRepository: AttemptRepository

    @Inject
    lateinit var assessmentRepository: AssessmentRepository

    private var assessmentId: Long = 0L

    @Before
    fun setup() = runBlocking {
        hiltRule.inject()
        val now = System.currentTimeMillis()
        val qaId = database.qaDao().insert(
            com.studyasist.data.local.entity.QA(
                questionText = "Q",
                answerText = "A",
                questionType = com.studyasist.data.local.entity.QuestionType.SHORT,
                subject = "Math",
                chapter = "Ch1"
            )
        )
        assessmentId = assessmentRepository.createAssessment(
            title = "Test Assessment",
            goalId = null,
            subject = "Math",
            chapter = null,
            totalTimeSeconds = 600,
            randomizeQuestions = true,
            qaIds = listOf(qaId)
        )
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun startAttempt_returnsId() = runBlocking {
        val attemptId = attemptRepository.startAttempt(assessmentId)
        assertTrue(attemptId > 0)
        val attempt = attemptRepository.getAttempt(attemptId)
        assertNotNull(attempt)
        assertNull(attempt!!.endedAt)
    }

    @Test
    fun endAttempt_setsEndedAt() = runBlocking {
        val attemptId = attemptRepository.startAttempt(assessmentId)
        attemptRepository.endAttempt(attemptId)
        val attempt = attemptRepository.getAttempt(attemptId)
        assertNotNull(attempt)
        assertNotNull(attempt!!.endedAt)
    }

    @Test
    fun saveAnswers_stored() = runBlocking {
        val attemptId = attemptRepository.startAttempt(assessmentId)
        val qaId = database.assessmentQuestionDao().getByAssessmentId(assessmentId).first().qaId
        attemptRepository.saveAnswers(
            attemptId,
            listOf(AttemptRepository.AttemptAnswerInput(qaId = qaId, answerText = "42"))
        )
        val answers = attemptRepository.getAnswers(attemptId)
        assertEquals(1, answers.size)
        assertEquals("42", answers[0].answerText)
    }

    @Test
    fun getAnswers_returnsStoredList() = runBlocking {
        val attemptId = attemptRepository.startAttempt(assessmentId)
        val aqList = database.assessmentQuestionDao().getByAssessmentId(assessmentId)
        val inputs = aqList.map { AttemptRepository.AttemptAnswerInput(it.qaId, "Answer for ${it.qaId}") }
        attemptRepository.saveAnswers(attemptId, inputs)
        val answers = attemptRepository.getAnswers(attemptId)
        assertEquals(aqList.size, answers.size)
    }

    @Test
    fun setNeedsManualReview_flagSet() = runBlocking {
        val attemptId = attemptRepository.startAttempt(assessmentId)
        attemptRepository.setNeedsManualReview(attemptId, true)
        val attempt = attemptRepository.getAttempt(attemptId)
        assertNotNull(attempt)
        assertTrue(attempt!!.needsManualReview)
    }

    @Test
    fun getNeedingManualReview_returnsFlaggedAttempts() = runBlocking {
        val attemptId = attemptRepository.startAttempt(assessmentId)
        attemptRepository.setNeedsManualReview(attemptId, true)
        val needing = attemptRepository.getNeedingManualReview()
        assertTrue(needing.any { it.id == attemptId })
    }
}
