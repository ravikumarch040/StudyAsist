package com.studyasist.data.network

import com.studyasist.data.datastore.SettingsDataStore
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsAuthTokenProvider @Inject constructor(
    private val dataStore: SettingsDataStore
) : AuthTokenProvider {
    override suspend fun getToken(): String? {
        return dataStore.getPreferencesFlow().first()[dataStore.authAccessToken]?.takeIf { it.isNotBlank() }
    }
}
