package com.studyasist.data.repository

import com.studyasist.data.api.LeaderboardApi
import com.studyasist.data.api.LeaderboardItem
import com.studyasist.data.api.SubmitScoreRequest
import javax.inject.Inject
import javax.inject.Singleton

sealed class LeaderboardResult<out T> {
    data class Success<T>(val data: T) : LeaderboardResult<T>()
    data class Error(val message: String) : LeaderboardResult<Nothing>()
}

@Singleton
class LeaderboardRepository @Inject constructor(
    private val leaderboardApi: LeaderboardApi,
    private val authRepository: AuthRepository
) {

    suspend fun hasToken(): Boolean = authRepository.getAccessToken() != null

    suspend fun submitScore(
        score: Double,
        maxScore: Double,
        assessmentTitle: String? = null,
        goalName: String? = null,
        streakDays: Int = 0
    ): LeaderboardResult<Unit> {
        if (authRepository.getAccessToken() == null) return LeaderboardResult.Error("Not signed in")
        return try {
            val resp = leaderboardApi.submit(
                SubmitScoreRequest(
                    score = score,
                    max_score = maxScore,
                    assessment_title = assessmentTitle,
                    goal_name = goalName,
                    streak_days = streakDays
                )
            )
            if (resp.isSuccessful) LeaderboardResult.Success(Unit)
            else LeaderboardResult.Error(resp.message() ?: "Submit failed")
        } catch (e: Exception) {
            LeaderboardResult.Error(e.message ?: "Network error")
        }
    }

    suspend fun getTop(limit: Int = 50): LeaderboardResult<List<LeaderboardItem>> = try {
        val resp = leaderboardApi.getTop(limit)
        if (resp.isSuccessful) {
            LeaderboardResult.Success(resp.body() ?: emptyList())
        } else {
            LeaderboardResult.Error(resp.message() ?: "Failed to load leaderboard")
        }
    } catch (e: Exception) {
        LeaderboardResult.Error(e.message ?: "Network error")
    }

    suspend fun getMyScores(limit: Int = 20): LeaderboardResult<List<Map<String, Any?>>> {
        if (authRepository.getAccessToken() == null) return LeaderboardResult.Error("Not signed in")
        return try {
            val resp = leaderboardApi.getMyScores(limit)
            if (resp.isSuccessful) {
                LeaderboardResult.Success(resp.body() ?: emptyList())
            } else {
                LeaderboardResult.Error(resp.message() ?: "Failed to load scores")
            }
        } catch (e: Exception) {
            LeaderboardResult.Error(e.message ?: "Network error")
        }
    }
}
