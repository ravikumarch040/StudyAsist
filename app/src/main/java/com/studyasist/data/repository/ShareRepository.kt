package com.studyasist.data.repository

import android.util.Base64
import com.google.gson.Gson
import com.studyasist.data.local.dao.AssessmentDao
import com.studyasist.data.local.dao.AssessmentQuestionDao
import com.studyasist.data.local.dao.QADao
import javax.inject.Inject
import javax.inject.Singleton

data class ShareableAssessment(
    val title: String,
    val questions: List<ShareableQuestion>
)

data class ShareableQuestion(
    val questionText: String,
    val answerText: String,
    val type: String,
    val options: String?
)

@Singleton
class ShareRepository @Inject constructor(
    private val assessmentDao: AssessmentDao,
    private val assessmentQuestionDao: AssessmentQuestionDao,
    private val qaDao: QADao,
    private val gson: Gson
) {

    suspend fun generateShareCode(assessmentId: Long): String? {
        val assessment = assessmentDao.getById(assessmentId) ?: return null
        val questions = assessmentQuestionDao.getByAssessmentId(assessmentId)
        val qaList = questions.mapNotNull { aq -> qaDao.getById(aq.qaId) }

        val shareable = ShareableAssessment(
            title = assessment.title,
            questions = qaList.map { qa ->
                ShareableQuestion(
                    questionText = qa.questionText,
                    answerText = qa.answerText,
                    type = qa.questionType.name,
                    options = qa.optionsJson
                )
            }
        )

        val json = gson.toJson(shareable)
        return Base64.encodeToString(json.toByteArray(), Base64.URL_SAFE or Base64.NO_WRAP)
    }

    fun decodeShareCode(code: String): ShareableAssessment? {
        return try {
            val json = String(Base64.decode(code, Base64.URL_SAFE or Base64.NO_WRAP))
            gson.fromJson(json, ShareableAssessment::class.java)
        } catch (_: Exception) {
            null
        }
    }
}
