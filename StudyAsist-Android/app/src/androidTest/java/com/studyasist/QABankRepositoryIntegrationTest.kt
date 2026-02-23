package com.studyasist

import com.studyasist.data.local.entity.QA
import com.studyasist.data.local.entity.QuestionType
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
 * Integration tests for QABankRepository (TC-QA01 to TC-QA06).
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class QABankRepositoryIntegrationTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var database: com.studyasist.data.local.db.AppDatabase

    @Inject
    lateinit var qaBankRepository: QABankRepository

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @After
    fun teardown() {
        database.close()
    }

    private fun qa(subject: String? = "Math", chapter: String? = "Ch1") = QA(
        questionText = "What is 2+2?",
        answerText = "4",
        questionType = QuestionType.NUMERIC,
        subject = subject,
        chapter = chapter
    )

    @Test
    fun insertQA_returnsId() = runBlocking {
        val id = qaBankRepository.insertQA(qa())
        assertTrue(id > 0)
    }

    @Test
    fun getQABySubjectChapter_filtered() = runBlocking {
        qaBankRepository.insertQA(qa("Math", null))
        qaBankRepository.insertQA(qa("Physics", null))
        val list = qaBankRepository.getQAListBySubjectChapter("Math", null)
        assertTrue(list.any { it.subject == "Math" })
        assertTrue(list.none { it.subject == "Physics" })
    }

    @Test
    fun getDistinctSubjects_returnsList() = runBlocking {
        qaBankRepository.insertQA(qa("Math", "Ch1"))
        qaBankRepository.insertQA(qa("Physics", "Ch1"))
        qaBankRepository.insertQA(qa("Math", "Ch2"))
        val subjects = qaBankRepository.getDistinctSubjects()
        assertTrue(subjects.size >= 2)
        assertTrue(subjects.contains("Math"))
        assertTrue(subjects.contains("Physics"))
    }

    @Test
    fun getDistinctChaptersForSubject() = runBlocking {
        qaBankRepository.insertQA(qa("Math", "Ch1"))
        qaBankRepository.insertQA(qa("Math", "Ch2"))
        val chapters = qaBankRepository.getDistinctChaptersForSubject("Math")
        assertTrue(chapters.size >= 2)
        assertTrue(chapters.contains("Ch1"))
        assertTrue(chapters.contains("Ch2"))
    }

    @Test
    fun getRandomQA_returnsRequestedCount() = runBlocking {
        repeat(10) { qaBankRepository.insertQA(qa()) }
        val random = qaBankRepository.getRandomQA(null, null, 5)
        assertEquals(5, random.size)
    }

    @Test
    fun countQA_returnsCorrectCount() = runBlocking {
        repeat(7) { qaBankRepository.insertQA(qa("Math", null)) }
        assertEquals(7, qaBankRepository.countQA("Math", null))
    }

    @Test
    fun deleteQA_removed() = runBlocking {
        val id = qaBankRepository.insertQA(qa())
        qaBankRepository.deleteQA(id)
        assertNull(qaBankRepository.getQAById(id))
    }
}
