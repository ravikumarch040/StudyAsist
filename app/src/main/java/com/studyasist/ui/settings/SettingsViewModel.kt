@file:Suppress("DEPRECATION") // TODO: Migrate to Credential Manager + AuthorizationClient for Drive scopes

package com.studyasist.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import com.studyasist.data.repository.AppSettings
import com.studyasist.data.repository.GeminiRepository
import com.studyasist.data.repository.SettingsRepository
import com.studyasist.notification.CloudBackupWorker
import com.studyasist.data.cloud.DRIVE_BACKUP_URI_SCHEME
import com.studyasist.data.cloud.DriveApiBackupProvider
import com.studyasist.util.listBackupFilesInFolder
import com.studyasist.util.readDocumentAsText
import com.studyasist.notification.NotificationScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.google.android.gms.auth.api.signin.GoogleSignIn
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settingsRepository: SettingsRepository,
    private val geminiRepository: GeminiRepository,
    private val notificationScheduler: NotificationScheduler,
    private val backupRepository: com.studyasist.data.repository.BackupRepository,
    private val workManager: WorkManager,
    private val driveApiBackupProvider: DriveApiBackupProvider
) : ViewModel() {

    val settings: StateFlow<AppSettings> = settingsRepository.settingsFlow
        .stateIn(
            viewModelScope,
            kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
            AppSettings(AppSettings.DEFAULT_LEAD_MINUTES, true, "", null, "", false, false, null, "folder", false, true, true, "system", AppSettings.DEFAULT_EXAM_ALERT_DAYS, AppSettings.DEFAULT_EXAM_ALERT_PERCENT)
        )

    val cloudBackupLastSuccess: StateFlow<Long?> = settingsRepository.cloudBackupLastSuccessFlow
        .stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000), null)

    val darkMode: StateFlow<String> = settingsRepository.darkModeFlow
        .stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000), "system")

    val appLocale: StateFlow<String> = settingsRepository.appLocaleFlow
        .stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000), "system")

    val themeId: StateFlow<String> = settingsRepository.themeIdFlow
        .stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000), "MINIMAL_LIGHT")

    val profilePicUri: StateFlow<String?> = settingsRepository.profilePicUriFlow
        .stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000), null)

    fun setThemeId(id: String) {
        viewModelScope.launch { settingsRepository.setThemeId(id) }
    }

    fun setProfilePicUri(uri: String?) {
        viewModelScope.launch { settingsRepository.setProfilePicUri(uri) }
    }

    fun setDarkMode(mode: String) {
        viewModelScope.launch { settingsRepository.setDarkMode(mode) }
    }

    fun setAppLocale(localeTag: String) {
        viewModelScope.launch {
            settingsRepository.setAppLocale(localeTag)
            val locales = when (localeTag) {
                "en", "hi", "es", "fr", "de" -> LocaleListCompat.forLanguageTags(localeTag)
                else -> LocaleListCompat.getEmptyLocaleList()
            }
            AppCompatDelegate.setApplicationLocales(locales)
        }
    }

    private val _apiKeyTestMessage = MutableStateFlow<String?>(null)
    val apiKeyTestMessage: StateFlow<String?> = _apiKeyTestMessage.asStateFlow()

    fun setDefaultLeadMinutes(minutes: Int) {
        viewModelScope.launch { settingsRepository.setDefaultLeadMinutes(minutes) }
    }

    fun setExamGoalAlertDaysThreshold(days: Int) {
        viewModelScope.launch { settingsRepository.setExamGoalAlertDaysThreshold(days) }
    }

    fun setExamGoalAlertPercentThreshold(percent: Int) {
        viewModelScope.launch { settingsRepository.setExamGoalAlertPercentThreshold(percent) }
    }

    fun setVibrationEnabled(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setVibrationEnabled(enabled) }
    }

    fun setBlockOverlap(block: Boolean) {
        viewModelScope.launch { settingsRepository.setBlockOverlap(block) }
    }

    fun setFocusGuardEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setFocusGuardEnabled(enabled)
            notificationScheduler.rescheduleAll()
        }
    }

    val focusGuardRestrictedExtra: StateFlow<Set<String>> = settingsRepository.focusGuardRestrictedExtraFlow
        .stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000), emptySet())

    fun addFocusGuardPackage(packageName: String) {
        viewModelScope.launch {
            settingsRepository.addFocusGuardPackage(packageName)
        }
    }

    fun removeFocusGuardPackage(packageName: String) {
        viewModelScope.launch {
            settingsRepository.removeFocusGuardPackage(packageName)
        }
    }

    fun setUserName(name: String) {
        viewModelScope.launch { settingsRepository.setUserName(name) }
    }

    fun setTtsVoiceName(name: String?) {
        viewModelScope.launch { settingsRepository.setTtsVoiceName(name) }
    }

    fun setGeminiApiKey(key: String) {
        viewModelScope.launch {
            settingsRepository.setGeminiApiKey(key)
            _apiKeyTestMessage.value = null
        }
    }

    fun testApiKey() {
        viewModelScope.launch {
            _apiKeyTestMessage.value = context.getString(com.studyasist.R.string.testing)
            val apiKey = settingsRepository.settingsFlow.first().geminiApiKey
            val result = geminiRepository.generateContent(apiKey, "Reply with exactly: OK")
            _apiKeyTestMessage.value = result.fold(
                onSuccess = { context.getString(com.studyasist.R.string.ok_api_key_works) },
                onFailure = { context.getString(com.studyasist.R.string.api_key_failed_format, it.message ?: "") }
            )
        }
    }

    /** Pair of (json, suggestedFilename) for export. */
    private val _exportRequest = MutableStateFlow<Pair<String, String>?>(null)
    val exportRequest: StateFlow<Pair<String, String>?> = _exportRequest.asStateFlow()

    private val _backupImportResult = MutableStateFlow<String?>(null)
    val backupImportResult: StateFlow<String?> = _backupImportResult.asStateFlow()

    fun exportBackup() {
        viewModelScope.launch {
            _exportRequest.value = null
            val json = backupRepository.exportToJson()
            _exportRequest.value = json to "studyasist_backup_${System.currentTimeMillis()}.json"
        }
    }

    fun exportExamData() {
        viewModelScope.launch {
            _exportRequest.value = null
            val json = backupRepository.exportExamDataToJson()
            _exportRequest.value = json to "StudyAsist_ExamData_${System.currentTimeMillis()}.json"
        }
    }

    fun importBackup(json: String) {
        viewModelScope.launch {
            _backupImportResult.value = null
            val result = backupRepository.importFromJson(json)
            _backupImportResult.value = result.fold(
                onSuccess = { context.getString(com.studyasist.R.string.restore_successful) },
                onFailure = { context.getString(com.studyasist.R.string.restore_failed_format, it.message ?: "") }
            )
        }
    }

    fun importExamData(json: String) {
        viewModelScope.launch {
            _backupImportResult.value = null
            val result = backupRepository.importExamDataFromJson(json)
            _backupImportResult.value = result.fold(
                onSuccess = { context.getString(com.studyasist.R.string.restore_successful) },
                onFailure = { context.getString(com.studyasist.R.string.restore_failed_format, it.message ?: "") }
            )
        }
    }

    fun clearBackupExport() {
        _exportRequest.value = null
    }

    fun clearBackupImportResult() {
        _backupImportResult.value = null
    }

    fun setCloudBackupFolder(uri: android.net.Uri?) {
        viewModelScope.launch {
            settingsRepository.setCloudBackupFolderUri(uri?.toString())
        }
    }

    fun setCloudBackupTarget(target: String) {
        viewModelScope.launch {
            settingsRepository.setCloudBackupTarget(target)
        }
    }

    fun setUseCloudForParsing(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setUseCloudForParsing(enabled) }
    }

    fun setUseCloudForGrading(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setUseCloudForGrading(enabled) }
    }

    fun setCloudBackupAuto(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setCloudBackupAuto(enabled)
            if (enabled) {
                val request = PeriodicWorkRequestBuilder<CloudBackupWorker>(24, TimeUnit.HOURS).build()
                workManager.enqueueUniquePeriodicWork(
                    "cloud_backup_auto",
                    ExistingPeriodicWorkPolicy.KEEP,
                    request
                )
            } else {
                workManager.cancelUniqueWork("cloud_backup_auto")
            }
        }
    }

    private val _cloudBackupResult = MutableStateFlow<String?>(null)
    val cloudBackupResult: StateFlow<String?> = _cloudBackupResult.asStateFlow()

    private val _cloudBackupFiles = MutableStateFlow<List<Pair<String, Uri>>>(emptyList())
    val cloudBackupFiles: StateFlow<List<Pair<String, Uri>>> = _cloudBackupFiles.asStateFlow()

    private val _cloudBackupFilesLoading = MutableStateFlow(false)
    val cloudBackupFilesLoading: StateFlow<Boolean> = _cloudBackupFilesLoading.asStateFlow()

    fun loadCloudBackupFiles() {
        viewModelScope.launch {
            _cloudBackupFilesLoading.value = true
            _cloudBackupFiles.value = emptyList()
            val settings = settingsRepository.settingsFlow.first()
            when (settings.cloudBackupTarget) {
                "google_drive" -> {
                    if (driveApiBackupProvider.isSignedIn()) {
                        _cloudBackupFiles.value = driveApiBackupProvider.listBackups()
                    }
                }
                else -> {
                    val uriStr = settings.cloudBackupFolderUri
                    if (!uriStr.isNullOrBlank()) {
                        _cloudBackupFiles.value = listBackupFilesInFolder(context.contentResolver, uriStr)
                    }
                }
            }
            _cloudBackupFilesLoading.value = false
        }
    }

    fun restoreFromCloudBackup(uri: Uri) {
        viewModelScope.launch {
            _backupImportResult.value = null
            val json = if (uri.scheme == DRIVE_BACKUP_URI_SCHEME) {
                driveApiBackupProvider.readBackup(uri)
            } else {
                readDocumentAsText(context.contentResolver, uri)
            } ?: run {
                _backupImportResult.value = context.getString(com.studyasist.R.string.err_could_not_read_file)
                return@launch
            }
            importBackup(json)
        }
    }

    fun clearCloudBackupFiles() {
        _cloudBackupFiles.value = emptyList()
    }

    fun backupToCloud() {
        viewModelScope.launch {
            _cloudBackupResult.value = null
            val settings = settingsRepository.settingsFlow.first()
            when (settings.cloudBackupTarget) {
                "google_drive" -> {
                    if (!driveApiBackupProvider.isSignedIn()) {
                        _cloudBackupResult.value = context.getString(com.studyasist.R.string.err_sign_in_required_for_drive)
                        return@launch
                    }
                }
                else -> {
                    val uriStr = settings.cloudBackupFolderUri
                    if (uriStr.isNullOrBlank()) {
                        _cloudBackupResult.value = context.getString(com.studyasist.R.string.err_set_cloud_backup_folder_first)
                        return@launch
                    }
                }
            }
            val request = OneTimeWorkRequestBuilder<CloudBackupWorker>()
                .setInputData(CloudBackupWorker.manualWorkData())
                .build()
            workManager.enqueue(request)
            _cloudBackupResult.value = context.getString(com.studyasist.R.string.backup_started_in_background)
        }
    }

    fun clearCloudBackupResult() {
        _cloudBackupResult.value = null
    }

    /** Intent to launch Google Sign-In for Drive backup. */
    fun getGoogleSignInIntent(): Intent =
        GoogleSignIn.getClient(context, DriveApiBackupProvider.getSignInOptions()).signInIntent

    private val _driveSignedIn = MutableStateFlow(driveApiBackupProvider.isSignedIn())
    val driveSignedIn: StateFlow<Boolean> = _driveSignedIn.asStateFlow()

    /** Refresh sign-in state (call after sign-in result or when screen appears). */
    fun refreshDriveSignInState() {
        _driveSignedIn.value = driveApiBackupProvider.isSignedIn()
    }

    fun signOutFromDrive() {
        GoogleSignIn.getClient(context, DriveApiBackupProvider.getSignInOptions()).signOut()
            .addOnCompleteListener { _driveSignedIn.value = false }
    }
}
