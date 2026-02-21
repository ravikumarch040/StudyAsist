package com.studyasist.ai

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Placeholder for on-device Gemini Nano via ML Kit GenAI Prompt API.
 * Currently disabled: genai-prompt requires Kotlin 2.2, which triggers KSP
 * "unexpected jvm signature V" with Hilt. Re-add when Dagger/KSP compatibility is fixed.
 * See docs/PLAN-OFFLINE-AI.md.
 */
@Singleton
class OfflineGeminiProvider @Inject constructor(
    @ApplicationContext private val context: Context
) {

    /**
     * Returns false until genai-prompt is re-enabled.
     */
    suspend fun isAvailable(): Boolean = withContext(Dispatchers.IO) {
        false
    }

    /**
     * Status for UI. Always UNAVAILABLE until genai-prompt is re-enabled.
     */
    suspend fun getStatus(): OfflineStatus = withContext(Dispatchers.IO) {
        OfflineStatus.UNAVAILABLE
    }

    /**
     * No-op; model is not used.
     */
    suspend fun downloadIfNeeded(): Boolean = true

    /**
     * Returns failure; on-device generation is disabled.
     */
    suspend fun generateContent(prompt: String): Result<String> = withContext(Dispatchers.IO) {
        Result.failure(Exception("Offline AI temporarily disabled. Add Gemini API key in Settings."))
    }
}

enum class OfflineStatus {
    AVAILABLE,
    DOWNLOADABLE,
    DOWNLOADING,
    UNAVAILABLE
}
