package com.studyasist.data.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

data class SyncUploadRequest(val payload: Map<String, Any?>, val version: Int = 1)
data class SyncDownloadResponse(val payload: Map<String, Any?>, val version: Int)
data class SyncUploadResponse(val ok: Boolean, val id: Long)

interface SyncApi {
    @POST("api/sync/upload")
    suspend fun upload(@Body body: SyncUploadRequest): Response<SyncUploadResponse>

    @GET("api/sync/download")
    suspend fun download(): Response<SyncDownloadResponse>
}
