package com.studyasist.data.grading

import com.studyasist.data.local.entity.QA
import com.studyasist.data.local.entity.QuestionType
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Objective-only grading for MCQ, True/False, Numeric, Fill-in, Short answer.
 */
@Singleton
class ObjectiveGradingService @Inject constructor() {

    fun grade(
        answers: List<Pair<Long, String?>>,
        qaMap: Map<Long, QA>
    ): GradingResult {
        var score = 0f
        val maxScore = answers.size.toFloat()
        val details = mutableListOf<QuestionGrade>()

        for ((qaId, userAnswer) in answers) {
            val qa = qaMap[qaId] ?: continue
            val (correct, feedback) = gradeOne(qa, userAnswer?.trim()?.takeIf { it.isNotBlank() } ?: "")
            if (correct) score += 1f
            details.add(
                QuestionGrade(
                    qaId = qaId,
                    questionText = qa.questionText.take(100),
                    correct = correct,
                    userAnswer = userAnswer,
                    modelAnswer = qa.answerText,
                    feedback = feedback
                )
            )
        }

        val percent = if (maxScore > 0) (score / maxScore) * 100f else 0f
        val detailsJson = JSONArray().apply {
            details.forEach { d ->
                put(JSONObject().apply {
                    put("qaId", d.qaId)
                    put("questionText", d.questionText)
                    put("correct", d.correct)
                    put("userAnswer", d.userAnswer)
                    put("modelAnswer", d.modelAnswer)
                    put("feedback", d.feedback)
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

    private fun gradeOne(qa: QA, userAnswer: String): Pair<Boolean, String> {
        return when (qa.questionType) {
            QuestionType.MCQ -> gradeMcq(qa, userAnswer)
            QuestionType.TRUE_FALSE -> gradeTrueFalse(qa, userAnswer)
            QuestionType.NUMERIC -> gradeNumeric(qa, userAnswer)
            QuestionType.FILL_BLANK, QuestionType.SHORT -> gradeText(qa, userAnswer)
            else -> gradeText(qa, userAnswer)
        }
    }

    private fun gradeMcq(qa: QA, userAnswer: String): Pair<Boolean, String> {
        val model = qa.answerText.trim()
        val user = userAnswer.trim()
        if (model.equals(user, ignoreCase = true)) return true to "Correct"

        val modelLetter = model.lowercase().take(1)
        val userLetter = user.lowercase().take(1)
        if (modelLetter in listOf("a", "b", "c", "d") && userLetter in listOf("a", "b", "c", "d")) {
            return (modelLetter == userLetter) to if (modelLetter == userLetter) "Correct" else "Incorrect"
        }

        val options = parseOptions(qa.optionsJson)
        if (options.isNotEmpty() && userLetter in listOf("a", "b", "c", "d")) {
            val userIdx = "abcd".indexOf(userLetter)
            val userOpt = options.getOrNull(userIdx)
            if (userOpt != null && (normalize(userOpt) == normalize(model) || userOpt.equals(model, ignoreCase = true))) {
                return true to "Correct"
            }
        }
        if (options.isNotEmpty() && modelLetter in listOf("a", "b", "c", "d")) {
            val modelIdx = "abcd".indexOf(modelLetter)
            val modelOpt = options.getOrNull(modelIdx)
            if (modelOpt != null && (normalize(user) == normalize(modelOpt) || user.equals(modelOpt, ignoreCase = true))) {
                return true to "Correct"
            }
        }

        return false to "Incorrect"
    }

    private fun gradeTrueFalse(qa: QA, userAnswer: String): Pair<Boolean, String> {
        val model = normalize(qa.answerText).lowercase()
        val user = normalize(userAnswer).lowercase()
        val modelBool = model == "true" || model == "t" || model == "yes"
        val userBool = user == "true" || user == "t" || user == "yes" || user == "1"
        val correct = modelBool == userBool
        return (correct) to if (correct) "Correct" else "Incorrect"
    }

    private fun gradeNumeric(qa: QA, userAnswer: String): Pair<Boolean, String> {
        val modelNum = parseNumber(qa.answerText)
        val userNum = parseNumber(userAnswer)
        if (modelNum == null || userNum == null) {
            val norm = normalize(qa.answerText) == normalize(userAnswer)
            return norm to if (norm) "Correct" else "Incorrect"
        }
        val tolerance = 0.01
        val diff = kotlin.math.abs(modelNum - userNum)
        val correct = diff <= tolerance
        return correct to if (correct) "Correct" else "Expected $modelNum"
    }

    private fun gradeText(qa: QA, userAnswer: String): Pair<Boolean, String> {
        val model = normalize(qa.answerText)
        val user = normalize(userAnswer)
        if (model == user) return true to "Correct"
        if (model.equals(user, ignoreCase = true)) return true to "Correct"
        val modelTokens = tokenize(model)
        val userTokens = tokenize(user)
        if (modelTokens.isEmpty()) return (user.isBlank()) to "Correct"
        val overlap = userTokens.intersect(modelTokens.toSet()).size.toFloat()
        val ratio = overlap / modelTokens.size
        val correct = ratio >= 0.85f
        return correct to if (correct) "Correct" else "Expected: ${qa.answerText.take(80)}"
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
        val correct: Boolean,
        val userAnswer: String?,
        val modelAnswer: String,
        val feedback: String
    )
}
