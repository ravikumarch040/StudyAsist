package com.studyasist

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.gson.Gson
import com.studyasist.data.local.db.AppDatabase
import com.studyasist.data.local.entity.Assessment
import com.studyasist.data.local.entity.AssessmentQuestion
import com.studyasist.data.local.entity.Attempt
import com.studyasist.data.local.entity.AttemptAnswer
import com.studyasist.data.local.entity.Goal
import com.studyasist.data.local.entity.GoalItem
import com.studyasist.data.local.entity.QA
import com.studyasist.data.local.entity.Result
import com.studyasist.data.local.entity.StudyToolHistoryEntity
import com.studyasist.data.local.entity.TimetableEntity
import com.studyasist.data.local.entity.WeekType
import com.studyasist.data.repository.BackupRepository
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Integration test: full flow of backup export and restore.
 * Verifies that data round-trips correctly through JSON backup.
 */
@RunWith(AndroidJUnit4::class)
class BackupRestoreIntegrationTest {

    private lateinit var database: AppDatabase
    private lateinit var backupRepository: BackupRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .fallbackToDestructiveMigration()
            .build()
        backupRepository = BackupRepository(
            context = context,
            timetableDao = database.timetableDao(),
            activityDao = database.activityDao(),
            goalDao = database.goalDao(),
            goalItemDao = database.goalItemDao(),
            qaDao = database.qaDao(),
            assessmentDao = database.assessmentDao(),
            assessmentQuestionDao = database.assessmentQuestionDao(),
            attemptDao = database.attemptDao(),
            attemptAnswerDao = database.attemptAnswerDao(),
            resultDao = database.resultDao(),
            studyToolHistoryDao = database.studyToolHistoryDao(),
            database = database,
            gson = Gson()
        )
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun backupExportAndRestore_preservesData() = runBlocking {
        // 1. Seed data: timetable, goal, QA, assessment, attempt, result, study tool history
        val timetable = TimetableEntity(
            id = 0,
            name = "Test Timetable",
            weekType = WeekType.ONE_WEEK,
            startDate = System.currentTimeMillis(),
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        val timetableId = database.timetableDao().insert(timetable)

        val goal = Goal(
            id = 0,
            name = "Test Goal",
            description = "Desc",
            examDate = System.currentTimeMillis() + 86400000 * 30,
            createdAt = System.currentTimeMillis(),
            isActive = true
        )
        val goalId = database.goalDao().insert(goal)

        val qa = QA(
            id = 0,
            sourceCaptureId = null,
            subject = "Math",
            chapter = "Ch1",
            questionText = "What is 2+2?",
            answerText = "4",
            questionType = com.studyasist.data.local.entity.QuestionType.SHORT,
            optionsJson = null,
            metadataJson = null,
            createdAt = System.currentTimeMillis()
        )
        val qaId = database.qaDao().insert(qa)

        val assessment = Assessment(
            id = 0,
            title = "Test Assessment",
            goalId = goalId,
            subject = "Math",
            chapter = "Ch1",
            totalTimeSeconds = 600,
            randomizeQuestions = true,
            createdAt = System.currentTimeMillis()
        )
        val assessmentId = database.assessmentDao().insert(assessment)

        database.assessmentQuestionDao().insert(
            AssessmentQuestion(
                id = 0,
                assessmentId = assessmentId,
                qaId = qaId,
                weight = 1f,
                sequence = 0
            )
        )

        val attempt = Attempt(
            id = 0,
            assessmentId = assessmentId,
            startedAt = System.currentTimeMillis(),
            endedAt = System.currentTimeMillis() + 60000,
            userNotes = null,
            needsManualReview = false
        )
        val attemptId = database.attemptDao().insert(attempt)

        database.attemptAnswerDao().insert(
            AttemptAnswer(
                id = 0,
                attemptId = attemptId,
                qaId = qaId,
                answerText = "4",
                answerImageUri = null,
                answerVoiceUri = null,
                submittedAt = System.currentTimeMillis()
            )
        )

        database.resultDao().insert(
            Result(
                id = 0,
                attemptId = attemptId,
                score = 1f,
                maxScore = 1f,
                percent = 100f,
                detailsJson = "[]",
                manualFeedback = null
            )
        )

        database.studyToolHistoryDao().insert(
            StudyToolHistoryEntity(toolType = "explain", inputText = "Explain photosynthesis", usedAt = System.currentTimeMillis())
        )

        // 2. Export to JSON
        val json = backupRepository.exportToJson()
        assertTrue(json.isNotBlank())
        assertTrue(json.contains("Test Timetable"))
        assertTrue(json.contains("Test Goal"))
        assertTrue(json.contains("Explain photosynthesis"))

        // 3. Clear DB and restore from JSON
        database.resultDao().deleteAll()
        database.attemptAnswerDao().deleteAll()
        database.attemptDao().deleteAll()
        database.assessmentQuestionDao().deleteAll()
        database.assessmentDao().deleteAll()
        database.goalItemDao().deleteAll()
        database.goalDao().deleteAll()
        database.activityDao().deleteAll()
        database.timetableDao().deleteAll()
        database.qaDao().deleteAll()
        database.studyToolHistoryDao().deleteAll()

        val importResult = backupRepository.importFromJson(json)
        assertTrue(importResult.isSuccess)

        // 4. Verify restored data
        val timetables = database.timetableDao().getAllOnce()
        assertEquals(1, timetables.size)
        assertEquals("Test Timetable", timetables[0].name)

        val goals = database.goalDao().getAllOnce()
        assertEquals(1, goals.size)
        assertEquals("Test Goal", goals[0].name)

        val qaList = database.qaDao().getAllOnce()
        assertEquals(1, qaList.size)
        assertEquals("What is 2+2?", qaList[0].questionText)

        val assessments = database.assessmentDao().getAllOnce()
        assertEquals(1, assessments.size)
        assertEquals("Test Assessment", assessments[0].title)

        val attempts = database.attemptDao().getAll()
        assertEquals(1, attempts.size)

        val results = database.resultDao().getAll()
        assertEquals(1, results.size)
        assertEquals(100f, results[0].percent, 0.01f)

        val history = database.studyToolHistoryDao().getRecentByTool("explain")
        assertEquals(1, history.size)
        assertEquals("Explain photosynthesis", history[0].inputText)
    }
}
