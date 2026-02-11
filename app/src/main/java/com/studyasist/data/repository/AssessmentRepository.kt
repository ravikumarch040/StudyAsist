package com.studyasist.data.repository

import com.studyasist.data.local.dao.AssessmentDao
import com.studyasist.data.local.dao.AssessmentQuestionDao
import com.studyasist.data.local.dao.QADao
import com.studyasist.data.local.entity.Assessment
import com.studyasist.data.local.entity.AssessmentQuestion
import com.studyasist.data.local.entity.QA
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AssessmentRepository @Inject constructor(
    private val assessmentDao: AssessmentDao,
    private val assessmentQuestionDao: AssessmentQuestionDao,
    private val qaDao: QADao,
    private val goalRepository: com.studyasist.data.repository.GoalRepository
) {

    fun getAllAssessments(): Flow<List<Assessment>> = assessmentDao.getAll()

    fun getAssessmentsByGoal(goalId: Long): Flow<List<Assessment>> = assessmentDao.getByGoalId(goalId)

    fun getAssessmentFlow(id: Long): Flow<Assessment?> = assessmentDao.getByIdFlow(id)

    suspend fun getAssessment(id: Long): Assessment? = assessmentDao.getById(id)

    suspend fun getAssessmentWithQuestions(assessmentId: Long): AssessmentWithQuestions? {
        val assessment = assessmentDao.getById(assessmentId) ?: return null
        val aqList = assessmentQuestionDao.getByAssessmentId(assessmentId)
        val qaIds = aqList.map { it.qaId }
        val qaList = qaDao.getByIds(qaIds)
        val qaMap = qaList.associateBy { it.id }
        val ordered = aqList
            .sortedBy { it.sequence }
            .mapNotNull { aq -> qaMap[aq.qaId]?.let { qa -> AssessmentQuestionWithQA(aq, qa) } }
        return AssessmentWithQuestions(assessment, ordered)
    }

    suspend fun createAssessment(
        title: String,
        goalId: Long?,
        subject: String?,
        chapter: String?,
        totalTimeSeconds: Int,
        randomizeQuestions: Boolean,
        qaIds: List<Long>
    ): Long {
        val assessment = Assessment(
            title = title,
            goalId = goalId,
            subject = subject,
            chapter = chapter,
            totalTimeSeconds = totalTimeSeconds,
            randomizeQuestions = randomizeQuestions
        )
        val assessmentId = assessmentDao.insert(assessment)
        val questions = qaIds.mapIndexed { index, qaId ->
            AssessmentQuestion(
                assessmentId = assessmentId,
                qaId = qaId,
                weight = 1.0f,
                sequence = index
            )
        }
        assessmentQuestionDao.insertAll(questions)
        return assessmentId
    }

    suspend fun createAssessmentFromRandom(
        title: String,
        goalId: Long?,
        subject: String?,
        chapter: String?,
        totalTimeSeconds: Int,
        randomizeQuestions: Boolean,
        count: Int
    ): Long {
        val qaList = qaDao.getRandomBySubjectChapter(subject, chapter, count)
        val qaIds = qaList.map { it.id }
        return createAssessment(
            title = title,
            goalId = goalId,
            subject = subject,
            chapter = chapter,
            totalTimeSeconds = totalTimeSeconds,
            randomizeQuestions = randomizeQuestions,
            qaIds = qaIds
        )
    }

    suspend fun createAssessmentFromGoal(
        title: String,
        goalId: Long,
        totalTimeSeconds: Int,
        randomizeQuestions: Boolean,
        count: Int
    ): Long {
        val items = goalRepository.getGoalItems(goalId)
        val allQas = mutableListOf<QA>()
        val perItem = if (items.isEmpty()) count else (count / items.size).coerceAtLeast(1)
        for (item in items) {
            val chapters = item.chapterList.split(",").map { it.trim() }.filter { it.isNotBlank() }
            val pairs = if (chapters.isEmpty()) listOf(item.subject to null) else chapters.map { item.subject to it }
            for ((subj, ch) in pairs) {
                val qs = qaDao.getRandomBySubjectChapter(subj, ch, perItem)
                allQas.addAll(qs)
            }
        }
        val qaIds = allQas.distinctBy { it.id }.shuffled().take(count).map { it.id }
        return createAssessment(
            title = title,
            goalId = goalId,
            subject = null,
            chapter = null,
            totalTimeSeconds = totalTimeSeconds,
            randomizeQuestions = randomizeQuestions,
            qaIds = qaIds
        )
    }

    suspend fun updateAssessment(assessment: Assessment) {
        assessmentDao.update(assessment)
    }

    suspend fun deleteAssessment(id: Long) {
        assessmentQuestionDao.deleteByAssessmentId(id)
        assessmentDao.deleteById(id)
    }

    suspend fun getQuestionCount(assessmentId: Long): Int =
        assessmentQuestionDao.getByAssessmentId(assessmentId).size

    data class AssessmentWithQuestions(
        val assessment: Assessment,
        val questions: List<AssessmentQuestionWithQA>
    )

    data class AssessmentQuestionWithQA(
        val assessmentQuestion: AssessmentQuestion,
        val qa: com.studyasist.data.local.entity.QA
    )
}
