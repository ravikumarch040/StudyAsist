package com.studyasist.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class SettingsDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs = context.dataStore

    val defaultLeadMinutes = intPreferencesKey("default_lead_minutes")
    val soundEnabled = booleanPreferencesKey("sound_enabled")
    val vibrationEnabled = booleanPreferencesKey("vibration_enabled")
    val activeTimetableId = longPreferencesKey("active_timetable_id")

    val dataStore: DataStore<Preferences> get() = prefs

    fun getPreferencesFlow(): Flow<Preferences> = prefs.data
}
