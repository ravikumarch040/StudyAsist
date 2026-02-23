package com.studyasist

import com.studyasist.data.repository.AssessmentRepository
import com.studyasist.data.repository.AttemptRepository
import com.studyasist.data.repository.ResultRepository
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
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
 * Integration tests for ResultRepository (TC-RS01 to TC-RS07).
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class ResultRepositoryIntegrationTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var database: com.studyasist.data.local.db.AppDatabase

    @Inject
    lateinit var resultRepository: ResultRepository

    @Inject
    lateinit var assessmentRepository: AssessmentRepository

    @Inject
    lateinit var attemptRepository: AttemptRepository

    private var assessmentId: Long = 0L
    private var attemptId: Long = 0L

    @Before
    fun setup() = runBlocking {
        hiltRule.inject()
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
        attemptId = attemptRepository.startAttempt(assessmentId)
        attemptRepository.endAttempt(attemptId)
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun saveResult_stored() = runBlocking {
        val id = resultRepository.saveResult(
            attemptId = attemptId,
            score = 1f,
            maxScore = 1f,
            percent = 100f,
            detailsJson = "[]"
        )
        assertTrue(id > 0)
        val result = resultRepository.getResult(attemptId)
        assertNotNull(result)
        assertEquals(100f, result!!.percent, 0.01f)
    }

    @Test
    fun getResult_returnsSaved() = runBlocking {
        resultRepository.saveResult(attemptId, 0.5f, 1f, 50f, "[]")
        val result = resultRepository.getResult(attemptId)
        assertNotNull(result)
        assertEquals(0.5f, result!!.score, 0.01f)
        assertEquals(50f, result.percent, 0.01f)
    }

    @Test
    fun getTopResultListItems_returnsSorted() = runBlocking {
        resultRepository.saveResult(attemptId, 0.5f, 1f, 50f, "[]")
        val attempt2 = attemptRepository.startAttempt(assessmentId)
        attemptRepository.endAttempt(attempt2)
        resultRepository.saveResult(attempt2, 1f, 1f, 100f, "[]")
        val items = resultRepository.getTopResultListItems(5)
        assertTrue(items.size >= 2)
        assertEquals(100f, items[0].percent, 0.01f)
    }

    @Test
    fun getExportCsv_validContent() = runBlocking {
        resultRepository.saveResult(attemptId, 1f, 1f, 100f, "[]")
        val csv = resultRepository.getExportCsv()
        assertTrue(csv.isNotBlank())
        assertTrue(csv.contains("Assessment") || csv.contains("assessment"))
    }

    @Test
    fun getExportPdf_nonEmptyByteArray() = runBlocking {
        resultRepository.saveResult(attemptId, 1f, 1f, 100f, "[]")
        val pdf = resultRepository.getExportPdf()
        assertTrue(pdf.isNotEmpty())
    }

    @Test
    fun getExportExcel_validBytes() = runBlocking {
        resultRepository.saveResult(attemptId, 1f, 1f, 100f, "[]")
        val excel = resultRepository.getExportExcel()
        assertTrue(excel.isNotEmpty())
        val str = String(excel, Charsets.UTF_8)
        assertTrue(str.contains("Workbook") || str.contains("xml"))
    }

    @Test
    fun getExportPdfForAttempt_returnsPdfBytes() = runBlocking {
        resultRepository.saveResult(attemptId, 1f, 1f, 100f, """[{"questionText":"Q","modelAnswer":"A","correct":true,"gradeLevel":"full"}]""")
        val pdf = resultRepository.getExportPdfForAttempt(attemptId)
        assertNotNull(pdf)
        assertTrue(pdf!!.isNotEmpty())
    }

    @Test
    fun getExportPdfForAttempt_noResult_returnsNull() = runBlocking {
        val attemptNoResult = attemptRepository.startAttempt(assessmentId)
        attemptRepository.endAttempt(attemptNoResult)
        val pdf = resultRepository.getExportPdfForAttempt(attemptNoResult)
        assertNull(pdf)
    }
}
