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
import com.studyasist.notification.NotificationScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val geminiRepository: GeminiRepository,
    private val notificationScheduler: NotificationScheduler,
    private val backupRepository: com.studyasist.data.repository.BackupRepository,
    private val workManager: WorkManager
) : ViewModel() {

    val settings: StateFlow<AppSettings> = settingsRepository.settingsFlow
        .stateIn(
            viewModelScope,
            kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
            AppSettings(AppSettings.DEFAULT_LEAD_MINUTES, true, "", null, "", false, false, null, false)
        )

    val cloudBackupLastSuccess: StateFlow<Long?> = settingsRepository.cloudBackupLastSuccessFlow
        .stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000), null)

    private val _apiKeyTestMessage = MutableStateFlow<String?>(null)
    val apiKeyTestMessage: StateFlow<String?> = _apiKeyTestMessage.asStateFlow()

    fun setDefaultLeadMinutes(minutes: Int) {
        viewModelScope.launch { settingsRepository.setDefaultLeadMinutes(minutes) }
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
            _apiKeyTestMessage.value = "Testing…"
            val apiKey = settingsRepository.settingsFlow.first().geminiApiKey
            val result = geminiRepository.generateContent(apiKey, "Reply with exactly: OK")
            _apiKeyTestMessage.value = result.fold(
                onSuccess = { "OK – API key works." },
                onFailure = { "Failed: ${it.message}" }
            )
        }
    }

    private val _backupExportJson = MutableStateFlow<String?>(null)
    val backupExportJson: StateFlow<String?> = _backupExportJson.asStateFlow()

    private val _backupImportResult = MutableStateFlow<String?>(null)
    val backupImportResult: StateFlow<String?> = _backupImportResult.asStateFlow()

    fun exportBackup() {
        viewModelScope.launch {
            _backupExportJson.value = null
            val json = backupRepository.exportToJson()
            _backupExportJson.value = json
        }
    }

    fun importBackup(json: String) {
        viewModelScope.launch {
            _backupImportResult.value = null
            val result = backupRepository.importFromJson(json)
            _backupImportResult.value = result.fold(
                onSuccess = { "Restore successful." },
                onFailure = { "Restore failed: ${it.message}" }
            )
        }
    }

    fun clearBackupExport() {
        _backupExportJson.value = null
    }

    fun clearBackupImportResult() {
        _backupImportResult.value = null
    }

    fun setCloudBackupFolder(uri: android.net.Uri?) {
        viewModelScope.launch {
            settingsRepository.setCloudBackupFolderUri(uri?.toString())
        }
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

    fun backupToCloud() {
        viewModelScope.launch {
            _cloudBackupResult.value = null
            val uriStr = settingsRepository.settingsFlow.first().cloudBackupFolderUri
            if (uriStr.isNullOrBlank()) {
                _cloudBackupResult.value = "Set cloud backup folder first"
                return@launch
            }
            val request = OneTimeWorkRequestBuilder<CloudBackupWorker>()
                .setInputData(CloudBackupWorker.manualWorkData())
                .build()
            workManager.enqueue(request)
            _cloudBackupResult.value = "Backup started in background"
        }
    }

    fun clearCloudBackupResult() {
        _cloudBackupResult.value = null
    }
}
