package com.studyasist.auth

import android.content.Intent
import android.net.Uri
import android.util.Base64
import com.studyasist.BuildConfig
import java.security.SecureRandom

/**
 * Helper for Sign in with Apple on Android.
 * Uses web flow: opens browser to Apple auth URL; backend receives callback and redirects
 * to studyasist://apple-signin?id_token=XXX which launches the app.
 *
 * Requires:
 * - APPLE_SERVICE_ID in local.properties (Apple Services ID from Apple Developer)
 * - Backend endpoint that receives Apple's form_post and redirects to studyasist://apple-signin?id_token=XXX
 */
object AppleSignInHelper {

    const val APPLE_SIGNIN_SCHEME = "studyasist"
    const val APPLE_SIGNIN_HOST = "apple-signin"

    fun isConfigured(): Boolean = BuildConfig.APPLE_SERVICE_ID.isNotBlank()

    /**
     * Builds the Apple authorization URL for the web flow.
     * redirect_uri must be your backend's callback URL (e.g. https://api.example.com/auth/apple/callback)
     * that receives Apple's form_post and redirects to studyasist://apple-signin?id_token=XXX
     */
    fun buildAuthUrl(redirectUri: String): String {
        val nonce = generateNonce()
        val state = generateState()
        return "https://appleid.apple.com/auth/authorize" +
            "?client_id=${Uri.encode(BuildConfig.APPLE_SERVICE_ID)}" +
            "&redirect_uri=${Uri.encode(redirectUri)}" +
            "&response_type=code%20id_token" +
            "&scope=name%20email" +
            "&response_mode=form_post" +
            "&state=$state" +
            "&nonce=$nonce"
    }

    /**
     * Redirect URI where Apple sends the form_post. Your backend must have this endpoint
     * configured in Apple Developer and must redirect to studyasist://apple-signin?id_token=XXX
     */
    fun getRedirectUri(): String {
        val base = BuildConfig.BACKEND_BASE_URL.trimEnd('/')
        return "$base/api/auth/apple/callback"
    }

    /**
     * Extracts id_token from the studyasist://apple-signin intent data.
     * Returns null if the intent is not an Apple sign-in callback.
     */
    fun extractIdTokenFromIntent(intent: Intent?): String? {
        val data = intent?.data ?: return null
        if (data.scheme != APPLE_SIGNIN_SCHEME || data.host != APPLE_SIGNIN_HOST) return null
        return data.getQueryParameter("id_token")
    }

    private fun generateNonce(): String {
        val bytes = ByteArray(32)
        SecureRandom().nextBytes(bytes)
        return Base64.encodeToString(bytes, Base64.URL_SAFE or Base64.NO_WRAP)
    }

    private fun generateState(): String {
        val bytes = ByteArray(16)
        SecureRandom().nextBytes(bytes)
        return Base64.encodeToString(bytes, Base64.URL_SAFE or Base64.NO_WRAP)
    }
}
