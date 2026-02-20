package com.studyasist.ui.onboarding

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.studyasist.data.cloud.DriveApiBackupProvider
import com.studyasist.data.repository.AuthRepository
import com.studyasist.data.repository.SettingsRepository
import com.studyasist.notification.CloudBackupWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

data class OnboardingResult(
    val userName: String,
    val backupTarget: String,
    val backupAuto: Boolean,
    val signedInWithGoogle: Boolean
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val authRepository: AuthRepository,
    private val settingsRepository: SettingsRepository,
    private val driveApiBackupProvider: DriveApiBackupProvider,
    private val workManager: WorkManager
) : ViewModel() {

    val accountSignedIn: StateFlow<Boolean> = authRepository.accessTokenFlow
        .map { it != null }
        .stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000), false)

    private val _driveSignedIn = MutableStateFlow(driveApiBackupProvider.isSignedIn())
    val driveSignedIn: StateFlow<Boolean> = _driveSignedIn.asStateFlow()

    fun refreshDriveSignInState() {
        _driveSignedIn.value = driveApiBackupProvider.isSignedIn()
    }

    private val _signInResult = MutableStateFlow<String?>(null)
    val signInResult: StateFlow<String?> = _signInResult.asStateFlow()

    fun clearSignInResult() { _signInResult.value = null }

    fun isBackendAuthConfigured(): Boolean = authRepository.isBackendAuthConfigured()

    fun getGoogleSignInIntent(): Intent =
        GoogleSignIn.getClient(context, DriveApiBackupProvider.getSignInOptions()).signInIntent

    fun onGoogleSignInResult(resultCode: Int) {
        refreshDriveSignInState()
        if (resultCode != android.app.Activity.RESULT_OK || !authRepository.isBackendAuthConfigured()) return
        viewModelScope.launch {
            _signInResult.value = null
            val idToken = GoogleSignIn.getLastSignedInAccount(context)?.idToken ?: return@launch
            when (val r = authRepository.loginWithGoogle(idToken)) {
                is com.studyasist.data.repository.AuthResult.Success ->
                    _signInResult.value = "success"
                is com.studyasist.data.repository.AuthResult.Error ->
                    _signInResult.value = r.message
            }
        }
    }

    /** Display name from Google account, or null if not signed in. */
    fun getGoogleDisplayName(): String? =
        GoogleSignIn.getLastSignedInAccount(context)?.displayName?.takeIf { it.isNotBlank() }
            ?: GoogleSignIn.getLastSignedInAccount(context)?.email?.substringBefore('@')?.takeIf { it.isNotBlank() }

    suspend fun completeOnboarding(result: OnboardingResult) {
        if (result.userName.isNotBlank()) {
            settingsRepository.setUserName(result.userName)
        }
        settingsRepository.setCloudBackupTarget(result.backupTarget)
        val effectiveBackupAuto = result.backupAuto && result.backupTarget == "google_drive"
        settingsRepository.setCloudBackupAuto(effectiveBackupAuto)
        if (effectiveBackupAuto) {
            val request = PeriodicWorkRequestBuilder<CloudBackupWorker>(24, TimeUnit.HOURS).build()
            workManager.enqueueUniquePeriodicWork(
                "cloud_backup_auto",
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
        settingsRepository.setOnboardingCompleted(true)
    }
}
