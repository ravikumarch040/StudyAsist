package com.studyasist.notification

import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.studyasist.data.local.dao.ResultDao
import com.studyasist.data.local.entity.Result as ResultEntity
import com.studyasist.data.repository.GeminiRepository
import com.studyasist.data.repository.SettingsRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

/**
 * Worker for deferred subjective grading using LLM when online.
 * Finds results with partial/wrong short/essay answers and re-grades them via Gemini.
 */
@HiltWorker
class DeferredGradingWorker @AssistedInject constructor(
    @Assisted context: android.content.Context,
    @Assisted params: WorkerParameters,
    private val resultDao: ResultDao,
    private val geminiRepository: GeminiRepository,
    private val settingsRepository: SettingsRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val settings = settingsRepository.settingsFlow.first()
            if (!settings.useCloudForGrading) {
                Log.d(TAG, "Deferred grading: AI grading disabled in settings, skipping")
                return Result.success()
            }
            val apiKey = settings.geminiApiKey
            if (apiKey.isBlank()) {
                Log.d(TAG, "Deferred grading: no API key, skipping")
                return Result.success()
            }
            val allResults = resultDao.getAllResultsWithAttempt()
            var updated = 0
            for (row in allResults) {
                val resultEntity = resultDao.getByAttemptId(row.attemptId) ?: continue
                if (!hasSubjectiveItemsForDeferredGrading(resultEntity.detailsJson)) continue
                val newResult = regradeWithLlm(resultEntity, apiKey) ?: continue
                resultDao.update(newResult)
                updated++
            }
            Log.d(TAG, "Deferred grading: processed ${allResults.size} results, updated $updated")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Deferred grading worker failed", e)
            Result.failure()
        }
    }

    private suspend fun regradeWithLlm(resultEntity: ResultEntity, apiKey: String): ResultEntity? {
        return try {
            val arr = org.json.JSONArray(resultEntity.detailsJson)
            var newScore = 0f
            val maxScore = arr.length().toFloat()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val gradeLevel = obj.optString("gradeLevel", "").lowercase()
                val questionText = obj.optString("questionText", "").take(500)
                val modelAnswer = obj.optString("modelAnswer", "").take(500)
                val userAnswer = obj.optString("userAnswer", "").take(500)
                if ((gradeLevel != "partial" && gradeLevel != "wrong") || questionText.isBlank()) {
                    newScore += when (gradeLevel) {
                        "full" -> 1f
                        "partial" -> 0.5f
                        else -> 0f
                    }
                    continue
                }
                val llmResult = geminiRepository.gradeAnswer(
                    apiKey, questionText, modelAnswer, userAnswer
                ).getOrNull() ?: continue
                val itemScore = when (llmResult.gradeLevel.lowercase()) {
                    "full" -> 1f
                    "partial" -> llmResult.score.coerceIn(0.4f, 0.9f)
                    else -> llmResult.score.coerceIn(0f, 0.3f)
                }
                newScore += itemScore
                obj.put("gradeLevel", llmResult.gradeLevel.lowercase())
                obj.put("feedback", llmResult.feedback)
                obj.put("partialScore", itemScore)
            }
            val percent = if (maxScore > 0) (newScore / maxScore) * 100f else 0f
            resultEntity.copy(
                score = newScore,
                maxScore = maxScore,
                percent = percent,
                detailsJson = arr.toString()
            )
        } catch (e: Exception) {
            Log.e(TAG, "Regrade failed for attempt ${resultEntity.attemptId}", e)
            null
        }
    }

    private fun hasSubjectiveItemsForDeferredGrading(detailsJson: String): Boolean {
        return try {
            val arr = org.json.JSONArray(detailsJson)
            (0 until arr.length()).any { i ->
                val obj = arr.getJSONObject(i)
                val gradeLevel = obj.optString("gradeLevel", "").lowercase()
                (gradeLevel == "partial" || gradeLevel == "wrong")
            }
        } catch (_: Exception) {
            false
        }
    }

    companion object {
        private const val TAG = "DeferredGradingWorker"
    }
}
