package com.studyasist.data.repository

import android.util.Base64
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.studyasist.data.api.CreateShareRequest
import com.studyasist.data.api.ShareApi
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
    private val shareApi: ShareApi,
    private val authRepository: AuthRepository,
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

        // When signed in, use backend for shareable links
        if (authRepository.getAccessToken() != null) {
            try {
                @Suppress("UNCHECKED_CAST")
                val assessmentData = gson.fromJson<Map<String, Any?>>(
                    gson.toJson(shareable),
                    object : TypeToken<Map<String, Any?>>() {}.type
                ) ?: emptyMap()
                val resp = shareApi.create(
                    CreateShareRequest(
                        title = shareable.title,
                        assessment_data = assessmentData,
                        expires_hours = 24
                    )
                )
                if (resp.isSuccessful) {
                    resp.body()?.code?.let { return it }
                }
            } catch (_: Exception) { /* fall through to local */ }
        }

        // Local Base64 fallback
        val json = gson.toJson(shareable)
        return Base64.encodeToString(json.toByteArray(), Base64.URL_SAFE or Base64.NO_WRAP)
    }

    /** Resolves share code from backend (8-char alphanumeric) or local Base64. */
    suspend fun decodeShareCode(code: String): ShareableAssessment? {
        // Backend format: short alphanumeric (e.g. 6–12 chars)
        val looksLikeBackend = code.length in 6..12 && code.all { it.isLetterOrDigit() }
        if (looksLikeBackend) {
            try {
                val resp = shareApi.resolve(code)
                if (resp.isSuccessful) {
                    val body = resp.body() ?: return null
                    val data = body.assessment_data
                    if (data.isNotEmpty()) {
                        return gson.fromJson(gson.toJson(data), ShareableAssessment::class.java)
                    }
                }
            } catch (_: Exception) { /* fall through to local */ }
        }

        // Local Base64
        return try {
            val json = String(Base64.decode(code, Base64.URL_SAFE or Base64.NO_WRAP))
            gson.fromJson(json, ShareableAssessment::class.java)
        } catch (_: Exception) {
            null
        }
    }
}
