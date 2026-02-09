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
    val soundEnabled: Boolean,
    val vibrationEnabled: Boolean,
    val alarmTtsMessage: String
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
            soundEnabled = prefs[dataStore.soundEnabled] ?: true,
            vibrationEnabled = prefs[dataStore.vibrationEnabled] ?: true,
            alarmTtsMessage = prefs[dataStore.alarmTtsMessage] ?: ""
        )
    }

    suspend fun setDefaultLeadMinutes(minutes: Int) {
        dataStore.dataStore.edit { it[dataStore.defaultLeadMinutes] = minutes }
    }

    suspend fun setSoundEnabled(enabled: Boolean) {
        dataStore.dataStore.edit { it[dataStore.soundEnabled] = enabled }
    }

    suspend fun setVibrationEnabled(enabled: Boolean) {
        dataStore.dataStore.edit { it[dataStore.vibrationEnabled] = enabled }
    }

    val activeTimetableIdFlow: Flow<Long?> = dataStore.getPreferencesFlow().map { prefs ->
        val id = prefs[dataStore.activeTimetableId] ?: -1L
        if (id < 0) null else id
    }

    suspend fun setActiveTimetableId(id: Long?) {
        dataStore.dataStore.edit { it[dataStore.activeTimetableId] = id ?: -1L }
    }

    suspend fun getAlarmTtsMessage(): String = settingsFlow.first().alarmTtsMessage

    suspend fun setAlarmTtsMessage(message: String) {
        dataStore.dataStore.edit { it[dataStore.alarmTtsMessage] = message }
    }
}
