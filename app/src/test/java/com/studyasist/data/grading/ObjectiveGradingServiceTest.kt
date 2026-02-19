package com.studyasist.data.grading

import com.studyasist.data.datastore.SettingsDataStore
import com.studyasist.data.local.entity.QA
import com.studyasist.data.local.entity.QuestionType
import com.studyasist.data.repository.GeminiRepository
import com.studyasist.data.repository.SettingsRepository
import dagger.hilt.android.testing.HiltTestApplication
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(application = HiltTestApplication::class, sdk = [34])
class ObjectiveGradingServiceTest {

    private lateinit var service: ObjectiveGradingService

    @Before
    fun setUp() {
        val context = org.robolectric.RuntimeEnvironment.getApplication().applicationContext
        val settingsDataStore = SettingsDataStore(context)
        val settingsRepository = SettingsRepository(settingsDataStore)
        val geminiRepository = GeminiRepository()
        service = ObjectiveGradingService(context, geminiRepository, settingsRepository)
    }

    private fun qa(
        id: Long = 1L,
        questionText: String = "Q?",
        answerText: String,
        questionType: QuestionType,
        optionsJson: String? = null
    ) = QA(
        id = id,
        questionText = questionText,
        answerText = answerText,
        questionType = questionType,
        optionsJson = optionsJson
    )

    @Test
    fun `MCQ - exact match gives full credit`() = runBlocking {
        val qa = qa(1L, "Choose one", "Paris", QuestionType.MCQ)
        val result = service.grade(listOf(1L to "Paris"), mapOf(1L to qa))
        assertEquals(1f, result.score, 0.001f)
        assertEquals(1f, result.maxScore, 0.001f)
        assertTrue(result.detailsJson.contains("\"gradeLevel\":\"full\""))
    }

    @Test
    fun `MCQ - option letter match gives full credit`() = runBlocking {
        val qa = qa(1L, "Choose one", "C", QuestionType.MCQ, "[\"A\",\"B\",\"C\",\"D\"]")
        val result = service.grade(listOf(1L to "c"), mapOf(1L to qa))
        assertEquals(1f, result.score, 0.001f)
    }

    @Test
    fun `MCQ - wrong answer gives zero`() = runBlocking {
        val qa = qa(1L, "Choose one", "A", QuestionType.MCQ)
        val result = service.grade(listOf(1L to "B"), mapOf(1L to qa))
        assertEquals(0f, result.score, 0.001f)
    }

    @Test
    fun `TrueFalse - true match gives full credit`() = runBlocking {
        val qa = qa(1L, "T or F?", "True", QuestionType.TRUE_FALSE)
        val result = service.grade(listOf(1L to "true"), mapOf(1L to qa))
        assertEquals(1f, result.score, 0.001f)
    }

    @Test
    fun `TrueFalse - yes matches true`() = runBlocking {
        val qa = qa(1L, "T or F?", "True", QuestionType.TRUE_FALSE)
        val result = service.grade(listOf(1L to "yes"), mapOf(1L to qa))
        assertEquals(1f, result.score, 0.001f)
    }

    @Test
    fun `TrueFalse - false when model is true gives zero`() = runBlocking {
        val qa = qa(1L, "T or F?", "True", QuestionType.TRUE_FALSE)
        val result = service.grade(listOf(1L to "false"), mapOf(1L to qa))
        assertEquals(0f, result.score, 0.001f)
    }

    @Test
    fun `Numeric - exact match gives full credit`() = runBlocking {
        val qa = qa(1L, "What is 2+2?", "4", QuestionType.NUMERIC)
        val result = service.grade(listOf(1L to "4"), mapOf(1L to qa))
        assertEquals(1f, result.score, 0.001f)
    }

    @Test
    fun `Numeric - tolerance within 0_01 gives full credit`() = runBlocking {
        val qa = qa(1L, "Value?", "3.14", QuestionType.NUMERIC)
        val result = service.grade(listOf(1L to "3.14"), mapOf(1L to qa))
        assertEquals(1f, result.score, 0.001f)
    }

    @Test
    fun `Numeric - outside tolerance gives zero`() = runBlocking {
        val qa = qa(1L, "Value?", "3.14", QuestionType.NUMERIC)
        val result = service.grade(listOf(1L to "3.2"), mapOf(1L to qa))
        assertEquals(0f, result.score, 0.001f)
    }

    @Test
    fun `Short - exact match gives full credit`() = runBlocking {
        val qa = qa(1L, "Capital of France?", "Paris", QuestionType.SHORT)
        val result = service.grade(listOf(1L to "Paris"), mapOf(1L to qa))
        assertEquals(1f, result.score, 0.001f)
    }

    @Test
    fun `Short - case insensitive match gives full credit`() = runBlocking {
        val qa = qa(1L, "Capital?", "Paris", QuestionType.SHORT)
        val result = service.grade(listOf(1L to "PARIS"), mapOf(1L to qa))
        assertEquals(1f, result.score, 0.001f)
    }

    @Test
    fun `Short - high token overlap gives full credit`() = runBlocking {
        val qa = qa(1L, "Define?", "photosynthesis is the process by which plants make food", QuestionType.SHORT)
        val result = service.grade(listOf(1L to "photosynthesis process plants make food"), mapOf(1L to qa))
        assertEquals(1f, result.score, 0.001f)
    }

    @Test
    fun `Short - low overlap gives zero`() = runBlocking {
        val qa = qa(1L, "Define?", "Paris is the capital of France", QuestionType.SHORT)
        val result = service.grade(listOf(1L to "London"), mapOf(1L to qa))
        assertEquals(0f, result.score, 0.001f)
    }

    @Test
    fun `Short - medium overlap gives partial credit`() = runBlocking {
        val qa = qa(1L, "Define?", "one two three four five six seven eight nine ten", QuestionType.SHORT)
        val result = service.grade(listOf(1L to "one two three four five six seven"), mapOf(1L to qa))
        assertEquals(0.5f, result.score, 0.001f)
    }

    @Test
    fun `blank answer gives zero`() = runBlocking {
        val qa = qa(1L, "Q?", "Paris", QuestionType.SHORT)
        val result = service.grade(listOf(1L to ""), mapOf(1L to qa))
        assertEquals(0f, result.score, 0.001f)
    }

    @Test
    fun `unknown QA id skips that answer`() = runBlocking {
        val qa = qa(1L, "Q?", "Paris", QuestionType.SHORT)
        val result = service.grade(listOf(1L to "Paris", 999L to "wrong"), mapOf(1L to qa))
        assertEquals(1f, result.score, 0.001f)
        assertEquals(2f, result.maxScore, 0.001f)
    }

    @Test
    fun `multiple questions aggregate score`() = runBlocking {
        val qa1 = qa(1L, "Q1?", "A", QuestionType.MCQ)
        val qa2 = qa(2L, "Q2?", "B", QuestionType.MCQ)
        val result = service.grade(
            listOf(1L to "A", 2L to "wrong"),
            mapOf(1L to qa1, 2L to qa2)
        )
        assertEquals(1f, result.score, 0.001f)
        assertEquals(2f, result.maxScore, 0.001f)
        assertEquals(50f, result.percent, 0.001f)
    }
}
