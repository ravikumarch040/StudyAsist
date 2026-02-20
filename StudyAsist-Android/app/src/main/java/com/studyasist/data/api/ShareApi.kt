package com.studyasist.data.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

data class CreateShareRequest(
    val title: String,
    val assessment_data: Map<String, Any?>,
    val expires_hours: Int = 24
)
data class ShareApiResponse(val code: String, val expires_at: String)
data class ResolveShareResponse(val title: String, val assessment_data: Map<String, Any?>)

interface ShareApi {
    @POST("api/share/create")
    suspend fun create(@Body body: CreateShareRequest): Response<ShareApiResponse>

    @GET("api/share/resolve/{code}")
    suspend fun resolve(@Path("code") code: String): Response<ResolveShareResponse>
}
