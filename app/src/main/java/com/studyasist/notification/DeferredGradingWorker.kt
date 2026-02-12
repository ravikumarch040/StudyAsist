package com.studyasist.notification

import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.studyasist.data.local.dao.ResultDao
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * Worker intended for deferred subjective grading when online.
 *
 * Currently a no-op placeholder. When LLM grading backend is added, this worker will:
 * - Find attempts with subjective answers (SHORT/ESSAY) that have partial/wrong grades
 * - Call LLM grading endpoint for improved feedback
 * - Merge results and update Result.detailsJson
 *
 * Constrained to run when network is connected (for future API calls).
 */
@HiltWorker
class DeferredGradingWorker @AssistedInject constructor(
    @Assisted context: android.content.Context,
    @Assisted params: WorkerParameters,
    private val resultDao: ResultDao
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            // Placeholder: when LLM grading is added, query for results with
            // subjective answers needing re-grading and call backend.
            val allResults = resultDao.getAllResultsWithAttempt()
            val pendingCount = allResults.count { row ->
                val result = resultDao.getByAttemptId(row.attemptId)
                result != null && hasSubjectiveItemsForDeferredGrading(result.detailsJson)
            }
            Log.d(TAG, "Deferred grading: checked ${allResults.size} results, $pendingCount could use LLM re-grading (backend not configured)")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Deferred grading worker failed", e)
            Result.failure()
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
