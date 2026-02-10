package com.studyasist.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
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
