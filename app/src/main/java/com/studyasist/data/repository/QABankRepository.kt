package com.studyasist.data.repository

import com.studyasist.data.local.dao.QADao
import com.studyasist.data.local.entity.QA
import com.studyasist.data.local.entity.QuestionType
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QABankRepository @Inject constructor(
    private val qaDao: QADao
) {

    fun getAllQA(): Flow<List<QA>> = qaDao.getAll()

    fun getQABySubjectChapter(subject: String?, chapter: String?): Flow<List<QA>> =
        qaDao.getBySubjectChapter(subject, chapter)

    suspend fun getQAById(id: Long): QA? = qaDao.getById(id)

    suspend fun getQAByIds(ids: List<Long>): List<QA> = qaDao.getByIds(ids)

    suspend fun getRandomQA(subject: String?, chapter: String?, limit: Int): List<QA> =
        qaDao.getRandomBySubjectChapter(subject, chapter, limit)

    suspend fun countQA(subject: String?, chapter: String?): Int =
        qaDao.countBySubjectChapter(subject, chapter)

    suspend fun getDistinctSubjects(): List<String> = qaDao.getDistinctSubjects()

    suspend fun getDistinctChapters(): List<String> = qaDao.getDistinctChapters()

    suspend fun getDistinctChaptersForSubject(subject: String): List<String> =
        qaDao.getDistinctChaptersForSubject(subject)

    suspend fun insertQA(qa: QA): Long = qaDao.insert(qa)

    suspend fun insertQABatch(
        subject: String?,
        chapter: String?,
        items: List<QABankRepository.ParsedQA>,
        sourceCaptureId: Long? = null
    ): List<Long> {
        val now = System.currentTimeMillis()
        val entities = items.map { parsed ->
            QA(
                sourceCaptureId = sourceCaptureId,
                subject = subject,
                chapter = chapter,
                questionText = parsed.question,
                answerText = parsed.answer,
                questionType = parsed.type,
                optionsJson = parsed.optionsJson,
                metadataJson = parsed.metadataJson,
                createdAt = now
            )
        }
        return qaDao.insertAll(entities)
    }

    suspend fun updateQA(qa: QA) {
        qaDao.update(qa)
    }

    suspend fun deleteQA(id: Long) {
        qaDao.deleteById(id)
    }

    data class ParsedQA(
        val question: String,
        val answer: String,
        val type: QuestionType,
        val optionsJson: String? = null,
        val metadataJson: String? = null
    )
}
