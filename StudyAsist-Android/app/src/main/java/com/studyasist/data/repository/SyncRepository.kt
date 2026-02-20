package com.studyasist.data.repository

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.studyasist.data.api.SyncApi
import com.studyasist.data.api.SyncUploadRequest
import javax.inject.Inject
import javax.inject.Singleton

sealed class SyncResult {
    data object Success : SyncResult()
    data class Error(val message: String) : SyncResult()
}

@Singleton
class SyncRepository @Inject constructor(
    private val syncApi: SyncApi,
    private val backupRepository: BackupRepository,
    private val authRepository: AuthRepository,
    private val gson: Gson
) {

    suspend fun hasToken(): Boolean = authRepository.getAccessToken() != null

    /** Upload local data to backend. Requires auth. */
    suspend fun upload(): SyncResult {
        if (authRepository.getAccessToken() == null) return SyncResult.Error("Not signed in")
        return try {
            val json = backupRepository.exportToJson()
            @Suppress("UNCHECKED_CAST")
            val payload = gson.fromJson<Map<String, Any?>>(json, object : TypeToken<Map<String, Any?>>() {}.type)
                ?: emptyMap()
            val resp = syncApi.upload(SyncUploadRequest(payload = payload, version = 2))
            if (resp.isSuccessful) SyncResult.Success
            else SyncResult.Error(resp.message() ?: "Upload failed")
        } catch (e: Exception) {
            SyncResult.Error(e.message ?: "Network error")
        }
    }

    /** Download data from backend and restore locally. Requires auth. */
    suspend fun download(): SyncResult {
        if (authRepository.getAccessToken() == null) return SyncResult.Error("Not signed in")
        return try {
            val resp = syncApi.download()
            if (!resp.isSuccessful) return SyncResult.Error(resp.message() ?: "Download failed")
            val body = resp.body() ?: return SyncResult.Error("Empty response")
            if (body.payload.isEmpty()) return SyncResult.Success
            val json = gson.toJson(body.payload)
            backupRepository.importFromJson(json).fold(
                onSuccess = { SyncResult.Success },
                onFailure = { SyncResult.Error(it.message ?: "Restore failed") }
            )
        } catch (e: Exception) {
            SyncResult.Error(e.message ?: "Network error")
        }
    }
}
