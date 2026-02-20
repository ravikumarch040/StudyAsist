package com.studyasist.data.repository

import androidx.datastore.preferences.core.edit
import com.studyasist.BuildConfig
import com.studyasist.data.api.AuthApi
import com.studyasist.data.api.IdTokenRequest
import com.studyasist.data.datastore.SettingsDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

sealed class AuthResult {
    data class Success(val email: String) : AuthResult()
    data class Error(val message: String) : AuthResult()
}

@Singleton
class AuthRepository @Inject constructor(
    private val authApi: AuthApi,
    private val dataStore: SettingsDataStore
) {
    val accessTokenFlow: Flow<String?> = dataStore.getPreferencesFlow().map { prefs ->
        prefs[dataStore.authAccessToken]?.takeIf { it.isNotBlank() }
    }

    val userEmailFlow: Flow<String?> = dataStore.getPreferencesFlow().map { prefs ->
        prefs[dataStore.authUserEmail]?.takeIf { it.isNotBlank() }
    }

    suspend fun getAccessToken(): String? = accessTokenFlow.first()

    suspend fun loginWithGoogle(idToken: String): AuthResult {
        return try {
            val resp = authApi.loginGoogle(IdTokenRequest(id_token = idToken))
            if (resp.isSuccessful) {
                val body = resp.body()!!
                dataStore.dataStore.edit { prefs ->
                    prefs[dataStore.authAccessToken] = body.access_token
                    prefs[dataStore.authUserEmail] = "" // Set after /me if needed
                }
                AuthResult.Success("") // Email from Google account
            } else {
                AuthResult.Error(resp.message() ?: "Login failed")
            }
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Network error")
        }
    }

    suspend fun loginWithApple(idToken: String): AuthResult {
        return try {
            val resp = authApi.loginApple(IdTokenRequest(id_token = idToken))
            if (resp.isSuccessful) {
                val body = resp.body()!!
                dataStore.dataStore.edit { prefs ->
                    prefs[dataStore.authAccessToken] = body.access_token
                    prefs[dataStore.authUserEmail] = ""
                }
                AuthResult.Success("")
            } else {
                AuthResult.Error(resp.message() ?: "Login failed")
            }
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Network error")
        }
    }

    suspend fun signOut() {
        dataStore.dataStore.edit { prefs ->
            prefs -= dataStore.authAccessToken
            prefs -= dataStore.authUserEmail
        }
    }

    fun isBackendAuthConfigured(): Boolean =
        BuildConfig.DRIVE_WEB_CLIENT_ID.isNotBlank() && BuildConfig.BACKEND_BASE_URL.isNotBlank()
}
