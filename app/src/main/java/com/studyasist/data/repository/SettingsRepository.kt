package com.studyasist.data.repository

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.studyasist.data.datastore.SettingsDataStore
import com.studyasist.notification.FOCUS_GUARD_RESTRICTED_PACKAGES
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
    val focusGuardEnabled: Boolean,
    val blockOverlap: Boolean = false,
    val cloudBackupFolderUri: String? = null,
    val cloudBackupTarget: String = "folder",
    val cloudBackupAuto: Boolean = false,
    val useCloudForParsing: Boolean = true,
    val useCloudForGrading: Boolean = true,
    val appLocale: String = "system",
    val examGoalAlertDaysThreshold: Int = 7,
    val examGoalAlertPercentThreshold: Int = 50
) {
    companion object {
        const val DEFAULT_LEAD_MINUTES = 5
        val LEAD_OPTIONS = listOf(0, 5, 10)
        const val DEFAULT_EXAM_ALERT_DAYS = 7
        const val DEFAULT_EXAM_ALERT_PERCENT = 50
        val EXAM_ALERT_DAYS_OPTIONS = listOf(1, 3, 7, 14, 21)
        val EXAM_ALERT_PERCENT_OPTIONS = listOf(25, 50, 75)
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
            focusGuardEnabled = prefs[dataStore.focusGuardEnabled] ?: false,
            blockOverlap = prefs[dataStore.blockOverlap] ?: false,
            cloudBackupFolderUri = prefs[dataStore.cloudBackupFolderUri]?.takeIf { it.isNotEmpty() },
            cloudBackupTarget = prefs[dataStore.cloudBackupTarget]?.takeIf { it.isNotEmpty() } ?: "folder",
            cloudBackupAuto = prefs[dataStore.cloudBackupAuto] ?: false,
            useCloudForParsing = prefs[dataStore.useCloudForParsing] ?: true,
            useCloudForGrading = prefs[dataStore.useCloudForGrading] ?: true,
            appLocale = prefs[dataStore.appLocale] ?: "system",
            examGoalAlertDaysThreshold = prefs[dataStore.examGoalAlertDaysThreshold] ?: AppSettings.DEFAULT_EXAM_ALERT_DAYS,
            examGoalAlertPercentThreshold = prefs[dataStore.examGoalAlertPercentThreshold] ?: AppSettings.DEFAULT_EXAM_ALERT_PERCENT
        )
    }

    suspend fun setDefaultLeadMinutes(minutes: Int) {
        dataStore.dataStore.edit { it[dataStore.defaultLeadMinutes] = minutes }
    }

    suspend fun setVibrationEnabled(enabled: Boolean) {
        dataStore.dataStore.edit { it[dataStore.vibrationEnabled] = enabled }
    }

    val focusGuardEnabledFlow: Flow<Boolean> = settingsFlow.map { it.focusGuardEnabled }

    /** User-added packages only (excludes built-in). */
    val focusGuardRestrictedExtraFlow: Flow<Set<String>> = dataStore.getPreferencesFlow().map { prefs ->
        val extra = prefs[dataStore.focusGuardRestrictedExtra]?.takeIf { it.isNotBlank() } ?: ""
        extra.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toSet()
    }

    /** Effective set of restricted packages = built-in + user-added. */
    val focusGuardRestrictedPackagesFlow: Flow<Set<String>> = dataStore.getPreferencesFlow().map { prefs ->
        val extra = prefs[dataStore.focusGuardRestrictedExtra]?.takeIf { it.isNotBlank() } ?: ""
        val extraSet = extra.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toSet()
        FOCUS_GUARD_RESTRICTED_PACKAGES + extraSet
    }

    suspend fun getEffectiveRestrictedPackages(): Set<String> =
        focusGuardRestrictedPackagesFlow.first()

    suspend fun getFocusGuardRestrictedExtra(): Set<String> {
        val s = dataStore.getPreferencesFlow().map { prefs ->
            prefs[dataStore.focusGuardRestrictedExtra]?.takeIf { it.isNotBlank() } ?: ""
        }.first()
        return s.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toSet()
    }

    suspend fun setFocusGuardRestrictedExtra(packages: Set<String>) {
        dataStore.dataStore.edit {
            it[dataStore.focusGuardRestrictedExtra] = packages.joinToString(",")
        }
    }

    suspend fun addFocusGuardPackage(packageName: String) {
        val current = getFocusGuardRestrictedExtra()
        if (packageName.isNotBlank() && packageName !in current) {
            setFocusGuardRestrictedExtra(current + packageName.trim())
        }
    }

    suspend fun removeFocusGuardPackage(packageName: String) {
        setFocusGuardRestrictedExtra(getFocusGuardRestrictedExtra() - packageName)
    }

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

    suspend fun setBlockOverlap(block: Boolean) {
        dataStore.dataStore.edit { it[dataStore.blockOverlap] = block }
    }

    suspend fun setCloudBackupFolderUri(uri: String?) {
        dataStore.dataStore.edit { it[dataStore.cloudBackupFolderUri] = uri ?: "" }
    }

    suspend fun setCloudBackupTarget(target: String) {
        dataStore.dataStore.edit { it[dataStore.cloudBackupTarget] = target }
    }

    fun getCloudBackupTargetFlow(): Flow<String> = dataStore.getPreferencesFlow().map { prefs ->
        prefs[dataStore.cloudBackupTarget]?.takeIf { it.isNotEmpty() } ?: "folder"
    }

    suspend fun setUseCloudForParsing(enabled: Boolean) {
        dataStore.dataStore.edit { it[dataStore.useCloudForParsing] = enabled }
    }

    suspend fun setUseCloudForGrading(enabled: Boolean) {
        dataStore.dataStore.edit { it[dataStore.useCloudForGrading] = enabled }
    }

    suspend fun setCloudBackupAuto(enabled: Boolean) {
        dataStore.dataStore.edit { it[dataStore.cloudBackupAuto] = enabled }
    }

    val cloudBackupLastSuccessFlow: Flow<Long?> = dataStore.getPreferencesFlow().map { prefs ->
        val ms = prefs[dataStore.cloudBackupLastSuccessMillis] ?: 0L
        if (ms <= 0) null else ms
    }

    suspend fun setCloudBackupLastSuccessMillis(millis: Long) {
        dataStore.dataStore.edit { it[dataStore.cloudBackupLastSuccessMillis] = millis }
    }

    suspend fun getDictateLanguage(): String =
        dataStore.getPreferencesFlow().map { it[dataStore.dictateLanguage] }.first() ?: "en"

    suspend fun setDictateLanguage(code: String) {
        dataStore.dataStore.edit { it[dataStore.dictateLanguage] = code }
    }

    suspend fun getExplainLanguage(): String =
        dataStore.getPreferencesFlow().map { it[dataStore.explainLanguage] }.first() ?: "en"

    suspend fun setExplainLanguage(code: String) {
        dataStore.dataStore.edit { it[dataStore.explainLanguage] = code }
    }

    suspend fun getSolveLanguage(): String =
        dataStore.getPreferencesFlow().map { it[dataStore.solveLanguage] }.first() ?: "en"

    suspend fun setSolveLanguage(code: String) {
        dataStore.dataStore.edit { it[dataStore.solveLanguage] = code }
    }

    val darkModeFlow: Flow<String> = dataStore.getPreferencesFlow().map {
        it[dataStore.darkMode] ?: "system"
    }

    suspend fun getDarkMode(): String =
        dataStore.getPreferencesFlow().map { it[dataStore.darkMode] }.first() ?: "system"

    suspend fun setDarkMode(mode: String) {
        dataStore.dataStore.edit { it[dataStore.darkMode] = mode }
    }

    val appLocaleFlow: Flow<String> = dataStore.getPreferencesFlow().map {
        it[dataStore.appLocale] ?: "system"
    }

    suspend fun setAppLocale(localeTag: String) {
        dataStore.dataStore.edit { it[dataStore.appLocale] = localeTag }
    }

    suspend fun setExamGoalAlertDaysThreshold(days: Int) {
        dataStore.dataStore.edit { it[dataStore.examGoalAlertDaysThreshold] = days }
    }

    suspend fun setExamGoalAlertPercentThreshold(percent: Int) {
        dataStore.dataStore.edit { it[dataStore.examGoalAlertPercentThreshold] = percent }
    }

    val themeIdFlow: Flow<String> = dataStore.getPreferencesFlow().map {
        it[dataStore.themeId] ?: "MINIMAL_LIGHT"
    }

    suspend fun setThemeId(id: String) {
        dataStore.dataStore.edit { it[dataStore.themeId] = id }
    }

    val profilePicUriFlow: Flow<String?> = dataStore.getPreferencesFlow().map {
        it[dataStore.profilePicUri]?.takeIf { uri -> uri.isNotEmpty() }
    }

    suspend fun setProfilePicUri(uri: String?) {
        dataStore.dataStore.edit { it[dataStore.profilePicUri] = uri ?: "" }
    }

    val userNameFlow: Flow<String> = dataStore.getPreferencesFlow().map {
        it[dataStore.userName] ?: ""
    }
}
