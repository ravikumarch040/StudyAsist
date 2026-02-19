package com.studyasist.data.grading

import android.content.Context
import com.studyasist.R
import com.studyasist.data.local.entity.QA
import com.studyasist.data.local.entity.QuestionType
import com.studyasist.data.repository.GeminiRepository
import com.studyasist.data.repository.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

enum class GradeLevel { FULL, PARTIAL, WRONG }

@Singleton
class ObjectiveGradingService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val geminiRepository: GeminiRepository,
    private val settingsRepository: SettingsRepository
) {

    suspend fun grade(
        answers: List<Pair<Long, String?>>,
        qaMap: Map<Long, QA>
    ): GradingResult {
        val apiKey = settingsRepository.settingsFlow.first().geminiApiKey
        var score = 0f
        val maxScore = answers.size.toFloat()
        val details = mutableListOf<QuestionGrade>()

        for ((qaId, userAnswer) in answers) {
            val qa = qaMap[qaId] ?: continue
            val (level, feedback) = gradeOne(qa, userAnswer?.trim()?.takeIf { it.isNotBlank() } ?: "", apiKey)
            score += when (level) {
                GradeLevel.FULL -> 1f
                GradeLevel.PARTIAL -> 0.5f
                GradeLevel.WRONG -> 0f
            }
            details.add(
                QuestionGrade(
                    qaId = qaId,
                    questionText = qa.questionText.take(100),
                    gradeLevel = level,
                    userAnswer = userAnswer,
                    modelAnswer = qa.answerText,
                    feedback = feedback,
                    subject = qa.subject,
                    chapter = qa.chapter
                )
            )
        }

        val percent = if (maxScore > 0) (score / maxScore) * 100f else 0f
        val detailsJson = JSONArray().apply {
            details.forEach { d ->
                put(JSONObject().apply {
                    put("qaId", d.qaId)
                    put("questionText", d.questionText)
                    put("correct", d.gradeLevel == GradeLevel.FULL)
                    put("gradeLevel", d.gradeLevel.name.lowercase())
                    put("userAnswer", d.userAnswer)
                    put("modelAnswer", d.modelAnswer)
                    put("feedback", d.feedback)
                    put("subject", d.subject)
                    put("chapter", d.chapter)
                })
            }
        }.toString()

        return GradingResult(
            score = score,
            maxScore = maxScore,
            percent = percent,
            detailsJson = detailsJson
        )
    }

    private suspend fun gradeOne(qa: QA, userAnswer: String, apiKey: String): Pair<GradeLevel, String> {
        return when (qa.questionType) {
            QuestionType.MCQ -> gradeMcq(qa, userAnswer)
            QuestionType.TRUE_FALSE -> gradeTrueFalse(qa, userAnswer)
            QuestionType.NUMERIC -> gradeNumeric(qa, userAnswer, apiKey)
            QuestionType.FILL_BLANK, QuestionType.SHORT, QuestionType.ESSAY -> gradeText(qa, userAnswer)
            else -> gradeText(qa, userAnswer)
        }
    }

    private fun gradeMcq(qa: QA, userAnswer: String): Pair<GradeLevel, String> {
        val model = qa.answerText.trim()
        val user = userAnswer.trim()
        if (model.equals(user, ignoreCase = true)) return GradeLevel.FULL to context.getString(R.string.feedback_correct)

        val modelLetter = model.lowercase().take(1)
        val userLetter = user.lowercase().take(1)
        if (modelLetter in listOf("a", "b", "c", "d") && userLetter in listOf("a", "b", "c", "d")) {
            return if (modelLetter == userLetter) GradeLevel.FULL to context.getString(R.string.feedback_correct) else GradeLevel.WRONG to context.getString(R.string.feedback_incorrect)
        }

        val options = parseOptions(qa.optionsJson)
        if (options.isNotEmpty() && userLetter in listOf("a", "b", "c", "d")) {
            val userIdx = "abcd".indexOf(userLetter)
            val userOpt = options.getOrNull(userIdx)
            if (userOpt != null && (normalize(userOpt) == normalize(model) || userOpt.equals(model, ignoreCase = true))) {
                return GradeLevel.FULL to context.getString(R.string.feedback_correct)
            }
        }
        if (options.isNotEmpty() && modelLetter in listOf("a", "b", "c", "d")) {
            val modelIdx = "abcd".indexOf(modelLetter)
            val modelOpt = options.getOrNull(modelIdx)
            if (modelOpt != null && (normalize(user) == normalize(modelOpt) || user.equals(modelOpt, ignoreCase = true))) {
                return GradeLevel.FULL to context.getString(R.string.feedback_correct)
            }
        }

        return GradeLevel.WRONG to context.getString(R.string.feedback_incorrect)
    }

    private fun gradeTrueFalse(qa: QA, userAnswer: String): Pair<GradeLevel, String> {
        val model = normalize(qa.answerText).lowercase()
        val user = normalize(userAnswer).lowercase()
        val modelBool = model == "true" || model == "t" || model == "yes"
        val userBool = user == "true" || user == "t" || user == "yes" || user == "1"
        val correct = modelBool == userBool
        return if (correct) GradeLevel.FULL to context.getString(R.string.feedback_correct) else GradeLevel.WRONG to context.getString(R.string.feedback_incorrect)
    }

    private suspend fun gradeNumeric(qa: QA, userAnswer: String, apiKey: String): Pair<GradeLevel, String> {
        val modelNum = parseNumber(qa.answerText)
        val userNum = parseNumber(userAnswer)
        if (modelNum != null && userNum != null) {
            val tolerance = 0.01
            val diff = kotlin.math.abs(modelNum - userNum)
            val correct = diff <= tolerance
            return if (correct) GradeLevel.FULL to context.getString(R.string.feedback_correct) else GradeLevel.WRONG to context.getString(R.string.feedback_expected_numeric, modelNum.toString())
        }
        val norm = normalize(qa.answerText) == normalize(userAnswer)
        if (norm) return GradeLevel.FULL to context.getString(R.string.feedback_correct)
        if (apiKey.isNotBlank()) {
            val geminiResult = geminiRepository.checkMathEquivalence(apiKey, qa.answerText, userAnswer).getOrNull()
            if (geminiResult == true) return GradeLevel.FULL to context.getString(R.string.feedback_correct)
        }
        return GradeLevel.WRONG to context.getString(R.string.feedback_expected_numeric, qa.answerText.take(50))
    }

    /** Token overlap thresholds: >=0.86 full credit, 0.68–0.86 partial, <0.68 wrong */
    private fun gradeText(qa: QA, userAnswer: String): Pair<GradeLevel, String> {
        val model = normalize(qa.answerText)
        val user = normalize(userAnswer)
        if (model == user) return GradeLevel.FULL to context.getString(R.string.feedback_correct)
        if (model.equals(user, ignoreCase = true)) return GradeLevel.FULL to context.getString(R.string.feedback_correct)
        val modelTokens = tokenize(model)
        val userTokens = tokenize(user)
        if (modelTokens.isEmpty()) return if (user.isBlank()) GradeLevel.FULL to context.getString(R.string.feedback_correct) else GradeLevel.WRONG to context.getString(R.string.feedback_expected_answer, qa.answerText.take(80))
        val overlap = userTokens.intersect(modelTokens.toSet()).size.toFloat()
        val ratio = overlap / modelTokens.size
        return when {
            ratio >= 0.86f -> GradeLevel.FULL to context.getString(R.string.feedback_correct)
            ratio >= 0.68f -> GradeLevel.PARTIAL to context.getString(R.string.feedback_partial_credit)
            else -> GradeLevel.WRONG to context.getString(R.string.feedback_expected_answer, qa.answerText.take(80))
        }
    }

    private fun normalize(s: String): String =
        s.trim().replace(Regex("\\s+"), " ").lowercase()

    private fun tokenize(s: String): List<String> =
        s.lowercase().split(Regex("\\s+")).filter { it.length > 1 }

    private fun parseNumber(s: String): Double? =
        Regex("[-+]?\\d+\\.?\\d*").find(s.trim())?.value?.toDoubleOrNull()

    private fun parseOptions(json: String?): List<String> {
        if (json.isNullOrBlank()) return emptyList()
        return try {
            val arr = org.json.JSONArray(json)
            (0 until arr.length()).mapNotNull { i ->
                when (val v = arr.get(i)) {
                    is String -> v
                    else -> v.toString()
                }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    data class GradingResult(
        val score: Float,
        val maxScore: Float,
        val percent: Float,
        val detailsJson: String
    )

    private data class QuestionGrade(
        val qaId: Long,
        val questionText: String,
        val gradeLevel: GradeLevel,
        val userAnswer: String?,
        val modelAnswer: String,
        val feedback: String,
        val subject: String?,
        val chapter: String?
    )
}
