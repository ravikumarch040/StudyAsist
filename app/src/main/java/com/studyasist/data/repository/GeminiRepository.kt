package com.studyasist.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.util.Base64
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeminiRepository @Inject constructor() {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    private val jsonType = "application/json; charset=utf-8".toMediaType()

    private val modelId = "gemini-2.5-flash"

    suspend fun generateContent(apiKey: String, prompt: String): Result<String> = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) return@withContext Result.failure(IllegalStateException("API key not set. Add your Gemini API key in Settings."))
        try {
            val body = JSONObject().apply {
                put("contents", org.json.JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", org.json.JSONArray().apply {
                            put(JSONObject().apply { put("text", prompt) })
                        })
                    })
                })
            }.toString()
            val request = Request.Builder()
                .url("https://generativelanguage.googleapis.com/v1beta/models/$modelId:generateContent")
                .header("x-goog-api-key", apiKey)
                .header("Content-Type", "application/json; charset=utf-8")
                .post(body.toRequestBody(jsonType))
                .build()
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""
            if (!response.isSuccessful) {
                val userMessage = parseGeminiError(responseBody) ?: "API error: ${response.code}"
                return@withContext Result.failure(Exception(userMessage))
            }
            val json = JSONObject(responseBody)
            val candidates = json.optJSONArray("candidates")
            val text = candidates?.optJSONObject(0)?.optJSONObject("content")?.optJSONArray("parts")?.optJSONObject(0)?.optString("text")?.trim()
                ?: return@withContext Result.failure(Exception("No response from API"))
            Result.success(text)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Sends image + prompt to Gemini Vision API and returns the text response.
     * Use for extracting Q&A from scanned images (math, multiple questions, etc.).
     */
    suspend fun generateContentFromImage(
        apiKey: String,
        imageBytes: ByteArray,
        mimeType: String = "image/jpeg",
        prompt: String
    ): Result<String> = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) return@withContext Result.failure(IllegalStateException("API key not set. Add your Gemini API key in Settings."))
        try {
            val base64 = Base64.encodeToString(imageBytes, Base64.NO_WRAP)
            val partsArray = org.json.JSONArray().apply {
                put(JSONObject().apply { put("text", prompt) })
                put(JSONObject().apply {
                    put("inlineData", JSONObject().apply {
                        put("mimeType", mimeType)
                        put("data", base64)
                    })
                })
            }
            val body = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", partsArray)
                    })
                })
            }.toString()
            val request = Request.Builder()
                .url("https://generativelanguage.googleapis.com/v1beta/models/$modelId:generateContent")
                .header("x-goog-api-key", apiKey)
                .header("Content-Type", "application/json; charset=utf-8")
                .post(body.toRequestBody(jsonType))
                .build()
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""
            if (!response.isSuccessful) {
                val userMessage = parseGeminiError(responseBody) ?: "API error: ${response.code}"
                return@withContext Result.failure(Exception(userMessage))
            }
            val json = JSONObject(responseBody)
            val candidates = json.optJSONArray("candidates")
            val text = candidates?.optJSONObject(0)?.optJSONObject("content")?.optJSONArray("parts")?.optJSONObject(0)?.optString("text")?.trim()
                ?: return@withContext Result.failure(Exception("No response from API"))
            Result.success(text)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Grades a short/essay answer using LLM. Returns JSON with score (0-1), feedback, gradeLevel.
     */
    suspend fun gradeAnswer(
        apiKey: String,
        question: String,
        modelAnswer: String,
        studentAnswer: String
    ): Result<GradeResult> = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) return@withContext Result.failure(IllegalStateException("API key not set"))
        try {
            val prompt = """
                Grade this student answer. Return ONLY valid JSON, no other text.
                {"score": <0.0-1.0>, "gradeLevel": "full"|"partial"|"wrong", "feedback": "<brief feedback>"}
                Question: $question
                Model answer: $modelAnswer
                Student answer: $studentAnswer
            """.trimIndent()
            generateContent(apiKey, prompt).mapCatching { text ->
                val trimmed = text.trim().removeSurrounding("```json", "```").trim()
                val json = JSONObject(trimmed)
                GradeResult(
                    score = json.optDouble("score", 0.0).toFloat(),
                    gradeLevel = json.optString("gradeLevel", "wrong"),
                    feedback = json.optString("feedback", "")
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    data class GradeResult(
        val score: Float,
        val gradeLevel: String,
        val feedback: String
    )

    private fun parseGeminiError(body: String): String? {
        if (body.isBlank()) return null
        return try {
            val json = JSONObject(body)
            json.optJSONObject("error")?.optString("message")?.takeIf { it.isNotBlank() }
        } catch (_: Exception) {
            null
        }
    }
}
