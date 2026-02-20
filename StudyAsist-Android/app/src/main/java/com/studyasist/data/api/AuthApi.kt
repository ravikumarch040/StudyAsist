package com.studyasist.data.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

data class IdTokenRequest(val id_token: String)
data class TokenResponse(val access_token: String, val token_type: String = "bearer")

interface AuthApi {
    @POST("api/auth/google")
    suspend fun loginGoogle(@Body body: IdTokenRequest): Response<TokenResponse>

    @POST("api/auth/apple")
    suspend fun loginApple(@Body body: IdTokenRequest): Response<TokenResponse>
}
