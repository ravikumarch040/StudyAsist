package com.studyasist.auth

import android.app.Activity
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.studyasist.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Uses Credential Manager (instead of deprecated Google Sign-In) for backend auth.
 * Returns ID token for authRepository.loginWithGoogle().
 * Drive backup still uses Google Sign-In for OAuth2 access tokens.
 */
object CredentialManagerAuthHelper {

    /**
     * Gets a Google ID token via Credential Manager for backend auth.
     * Requires DRIVE_WEB_CLIENT_ID (web client ID) to be configured.
     * @return ID token string
     * @throws CredentialManagerAuthException on failure or cancellation
     */
    suspend fun getGoogleIdToken(activity: Activity): String = withContext(Dispatchers.Main) {
        val serverClientId = BuildConfig.DRIVE_WEB_CLIENT_ID
        if (serverClientId.isBlank()) {
            throw CredentialManagerAuthException("Backend auth not configured")
        }
        val credentialManager = CredentialManager.create(activity)
        val googleIdOption = GetGoogleIdOption.Builder()
            .setServerClientId(serverClientId)
            .setFilterByAuthorizedAccounts(false)
            .build()
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()
        try {
            val result = credentialManager.getCredential(activity, request)
            val credential = result.credential
            when (credential) {
                is GoogleIdTokenCredential -> credential.idToken
                else -> {
                    if (credential is CustomCredential &&
                        credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                    ) {
                        GoogleIdTokenCredential.createFrom(credential.data).idToken
                    } else {
                        throw CredentialManagerAuthException("Invalid credential type")
                    }
                }
            }
        } catch (e: GoogleIdTokenParsingException) {
            throw CredentialManagerAuthException(e.message ?: "Invalid token")
        } catch (e: Exception) {
            throw CredentialManagerAuthException(e.message ?: "Sign-in failed")
        }
    }
}

class CredentialManagerAuthException(message: String) : Exception(message)
