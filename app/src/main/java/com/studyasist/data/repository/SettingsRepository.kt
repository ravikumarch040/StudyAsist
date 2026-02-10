package com.studyasist.data.repository

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.studyasist.data.datastore.SettingsDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

data class AppSettings(
    val defaultLeadMinutes: Int,
    val vibrationEnabled: Boolean,
    val userName: String,
    val ttsVoiceName: String?,
    val geminiApiKey: String,
    val focusGuardEnabled: Boolean
) {
    companion object {
        const val DEFAULT_LEAD_MINUTES = 5
        val LEAD_OPTIONS = listOf(0, 5, 10)
    }
}

@Singleton
class SettingsRepository @Inject constructor(
    private val dataStore: SettingsDataStore
) {

    val settingsFlow: Flow<AppSettings> = dataStore.getPreferencesFlow().map { prefs ->
        AppSettings(
            defaultLeadMinutes = prefs[dataStore.defaultLeadMinutes] ?: AppSettings.DEFAULT_LEAD_MINUTES,
            vibrationEnabled = prefs[dataStore.vibrationEnabled] ?: true,
            userName = prefs[dataStore.userName] ?: "",
            ttsVoiceName = prefs[dataStore.ttsVoiceName]?.takeIf { it.isNotEmpty() },
            geminiApiKey = prefs[dataStore.geminiApiKey] ?: "",
            focusGuardEnabled = prefs[dataStore.focusGuardEnabled] ?: false
        )
    }

    suspend fun setDefaultLeadMinutes(minutes: Int) {
        dataStore.dataStore.edit { it[dataStore.defaultLeadMinutes] = minutes }
    }

    suspend fun setVibrationEnabled(enabled: Boolean) {
        dataStore.dataStore.edit { it[dataStore.vibrationEnabled] = enabled }
    }

    val focusGuardEnabledFlow: Flow<Boolean> = settingsFlow.map { it.focusGuardEnabled }

    val activeTimetableIdFlow: Flow<Long?> = dataStore.getPreferencesFlow().map { prefs ->
        val id = prefs[dataStore.activeTimetableId] ?: -1L
        if (id < 0) null else id
    }

    suspend fun setActiveTimetableId(id: Long?) {
        dataStore.dataStore.edit { it[dataStore.activeTimetableId] = id ?: -1L }
    }

    suspend fun setUserName(name: String) {
        dataStore.dataStore.edit { it[dataStore.userName] = name }
    }

    suspend fun setTtsVoiceName(name: String?) {
        dataStore.dataStore.edit { it[dataStore.ttsVoiceName] = name ?: "" }
    }

    suspend fun setGeminiApiKey(key: String) {
        dataStore.dataStore.edit { it[dataStore.geminiApiKey] = key }
    }

    suspend fun setFocusGuardEnabled(enabled: Boolean) {
        dataStore.dataStore.edit { it[dataStore.focusGuardEnabled] = enabled }
    }
}
