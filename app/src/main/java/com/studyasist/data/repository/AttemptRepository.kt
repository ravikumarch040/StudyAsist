package com.studyasist.data.repository

import com.studyasist.data.local.dao.AttemptAnswerDao
import com.studyasist.data.local.dao.AttemptDao
import com.studyasist.data.local.entity.Attempt
import com.studyasist.data.local.entity.AttemptAnswer
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AttemptRepository @Inject constructor(
    private val attemptDao: AttemptDao,
    private val attemptAnswerDao: AttemptAnswerDao
) {

    fun getAttemptsByAssessment(assessmentId: Long): Flow<List<Attempt>> =
        attemptDao.getByAssessmentId(assessmentId)

    suspend fun getAttempts(assessmentId: Long): List<Attempt> =
        attemptDao.getByAssessmentIdOnce(assessmentId)

    suspend fun getAttempt(id: Long): Attempt? = attemptDao.getById(id)

    suspend fun startAttempt(assessmentId: Long): Long {
        val attempt = Attempt(
            assessmentId = assessmentId,
            startedAt = System.currentTimeMillis(),
            endedAt = null,
            userNotes = null
        )
        return attemptDao.insert(attempt)
    }

    suspend fun endAttempt(attemptId: Long) {
        val attempt = attemptDao.getById(attemptId) ?: return
        attemptDao.update(
            attempt.copy(endedAt = System.currentTimeMillis())
        )
    }

    suspend fun saveAnswers(attemptId: Long, answers: List<AttemptAnswerInput>) {
        val now = System.currentTimeMillis()
        val entities = answers.map { input ->
            AttemptAnswer(
                attemptId = attemptId,
                qaId = input.qaId,
                answerText = input.answerText,
                answerImageUri = input.answerImageUri,
                answerVoiceUri = input.answerVoiceUri,
                submittedAt = now
            )
        }
        attemptAnswerDao.insertAll(entities)
    }

    suspend fun getAnswers(attemptId: Long): List<AttemptAnswer> =
        attemptAnswerDao.getByAttemptId(attemptId)

    data class AttemptAnswerInput(
        val qaId: Long,
        val answerText: String? = null,
        val answerImageUri: String? = null,
        val answerVoiceUri: String? = null
    )
}
