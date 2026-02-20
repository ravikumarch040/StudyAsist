package com.studyasist.data.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

data class SubmitScoreRequest(
    val score: Double,
    val max_score: Double,
    val assessment_title: String? = null,
    val goal_name: String? = null,
    val streak_days: Int = 0
)
data class LeaderboardItem(
    val rank: Int,
    val user_name: String,
    val score: Double,
    val max_score: Double,
    val percentage: Double,
    val streak_days: Int
)
data class SubmitScoreResponse(val ok: Boolean, val id: Long)

interface LeaderboardApi {
    @POST("api/leaderboard/submit")
    suspend fun submit(@Body body: SubmitScoreRequest): Response<SubmitScoreResponse>

    @GET("api/leaderboard/top")
    suspend fun getTop(@Query("limit") limit: Int = 50): Response<List<LeaderboardItem>>

    @GET("api/leaderboard/me")
    suspend fun getMyScores(@Query("limit") limit: Int = 20): Response<List<Map<String, Any?>>>
}
