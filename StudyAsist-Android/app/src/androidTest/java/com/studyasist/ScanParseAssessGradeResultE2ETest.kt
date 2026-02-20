package com.studyasist

import com.studyasist.data.grading.ObjectiveGradingService
import com.studyasist.data.local.db.AppDatabase
import com.studyasist.data.local.entity.Assessment
import com.studyasist.data.local.entity.AssessmentQuestion
import com.studyasist.data.local.entity.Attempt
import com.studyasist.data.local.entity.AttemptAnswer
import com.studyasist.data.local.entity.Goal
import com.studyasist.data.local.entity.QA
import com.studyasist.data.local.entity.Result
import com.studyasist.data.qa.HeuristicQaParser
import com.studyasist.data.repository.QABankRepository
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.ext.junit.runners.AndroidJUnit4
import javax.inject.Inject

/**
 * E2E integration test: scan -> parse -> assess -> grade -> result.
 * Covers the full flow from raw text/Q&A extraction through assessment run and grading.
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class ScanParseAssessGradeResultE2ETest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var database: AppDatabase

    @Inject
    lateinit var gradingService: ObjectiveGradingService

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun fullFlow_scanParseAssessGradeResult() = runBlocking {
        // 1. PARSE: Extract Q&A from text (simulates OCR output)
        val rawText = """
            What is the capital of France?
            Answer: Paris

            True or False: The Earth is flat.
            Answer: False

            2 + 2 = ?
            Answer: 4
        """.trimIndent()
        val parsed = HeuristicQaParser.parse(rawText)
        assertTrue("Parser should extract at least one QA", parsed.isNotEmpty())

        // 2. PERSIST: Save goal and Q&A (simulates scan save)
        val goal = Goal(
            id = 0,
            name = "E2E Test Goal",
            description = "",
            examDate = System.currentTimeMillis() + 86400000 * 30,
            createdAt = System.currentTimeMillis(),
            isActive = true
        )
        val goalId = database.goalDao().insert(goal)

        val qaIds = parsed.map { item ->
            val qa = QA(
                id = 0,
                sourceCaptureId = null,
                subject = "General",
                chapter = "Ch1",
                questionText = item.question,
                answerText = item.answer,
                questionType = item.type,
                optionsJson = item.optionsJson,
                metadataJson = null,
                createdAt = System.currentTimeMillis()
            )
            database.qaDao().insert(qa).toLong()
        }

        // 3. CREATE ASSESSMENT
        val assessment = Assessment(
            id = 0,
            title = "E2E Assessment",
            goalId = goalId,
            subject = null,
            chapter = null,
            totalTimeSeconds = 300,
            randomizeQuestions = false,
            createdAt = System.currentTimeMillis()
        )
        val assessmentId = database.assessmentDao().insert(assessment)
        qaIds.forEachIndexed { i, qaId ->
            database.assessmentQuestionDao().insert(
                AssessmentQuestion(
                    id = 0,
                    assessmentId = assessmentId,
                    qaId = qaId,
                    weight = 1f,
                    sequence = i
                )
            )
        }

        // 4. RUN ASSESSMENT: Simulate user answers
        val attempt = Attempt(
            id = 0,
            assessmentId = assessmentId,
            startedAt = System.currentTimeMillis(),
            endedAt = System.currentTimeMillis() + 60000,
            userNotes = null,
            needsManualReview = false
        )
        val attemptId = database.attemptDao().insert(attempt)

        // User answers: Paris (correct), False (correct), 4 (correct)
        val questions = database.assessmentQuestionDao().getByAssessmentId(assessmentId)
        val answers = listOf("Paris", "False", "4")
        questions.forEachIndexed { i, aq ->
            val answerText = if (i < answers.size) answers[i] else ""
            database.attemptAnswerDao().insert(
                AttemptAnswer(
                    id = 0,
                    attemptId = attemptId,
                    qaId = aq.qaId,
                    answerText = answerText,
                    answerImageUri = null,
                    answerVoiceUri = null,
                    submittedAt = System.currentTimeMillis()
                )
            )
        }

        // 5. GRADE: Compute result
        val answers = database.attemptAnswerDao().getByAttemptId(attemptId).map { aa ->
            aa.qaId to aa.answerText
        }
        val qaMap = database.qaDao().getByIds(answers.map { it.first }).associateBy { it.id }
        val result = gradingService.grade(answers, qaMap)
        database.resultDao().insert(
            Result(
                id = 0,
                attemptId = attemptId,
                score = result.score,
                maxScore = result.maxScore,
                percent = result.percent,
                detailsJson = result.detailsJson ?: "[]",
                manualFeedback = null
            )
        )

        // 6. VERIFY RESULT
        val results = database.resultDao().getAll()
        assertEquals(1, results.size)
        assertTrue("Should have full score or high score", results[0].percent >= 90f)
    }
}
