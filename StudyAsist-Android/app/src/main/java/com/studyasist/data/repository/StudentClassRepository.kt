package com.studyasist.data.repository

import androidx.datastore.preferences.core.edit
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.studyasist.data.datastore.SettingsDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

data class StudentClassProfile(
    val standard: String = "",
    val board: String = "",
    val school: String? = null,
    val city: String? = null,
    val state: String? = null,
    val subjects: List<String> = emptyList()
)

@Singleton
class StudentClassRepository @Inject constructor(
    private val dataStore: SettingsDataStore,
    private val qaBankRepository: QABankRepository,
    private val gson: Gson
) {
    val profileFlow: Flow<StudentClassProfile> = dataStore.getPreferencesFlow().map { prefs ->
        val subjectsJson = prefs[dataStore.studentSubjects] ?: "[]"
        val subjects = try {
            gson.fromJson<List<String>>(subjectsJson, object : TypeToken<List<String>>() {}.type) ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }
        StudentClassProfile(
            standard = prefs[dataStore.studentStandard] ?: "",
            board = prefs[dataStore.studentBoard] ?: "",
            school = prefs[dataStore.studentSchool]?.takeIf { it.isNotBlank() },
            city = prefs[dataStore.studentCity]?.takeIf { it.isNotBlank() },
            state = prefs[dataStore.studentState]?.takeIf { it.isNotBlank() },
            subjects = subjects
        )
    }

    suspend fun getProfile(): StudentClassProfile = profileFlow.first()

    suspend fun saveProfile(profile: StudentClassProfile) {
        dataStore.dataStore.edit { it ->
            it[dataStore.studentStandard] = profile.standard
            it[dataStore.studentBoard] = profile.board
            it[dataStore.studentSchool] = profile.school ?: ""
            it[dataStore.studentCity] = profile.city ?: ""
            it[dataStore.studentState] = profile.state ?: ""
            it[dataStore.studentSubjects] = gson.toJson(profile.subjects)
        }
    }

    /**
     * Subjects for dropdowns: from student class profile if not empty, else from Q&A bank.
     */
    suspend fun getSubjectsForDropdown(): List<String> {
        val profile = getProfile()
        return if (profile.subjects.isNotEmpty()) {
            profile.subjects.sorted()
        } else {
            qaBankRepository.getDistinctSubjects().sorted()
        }
    }

    fun getSubjectsFlow(): Flow<List<String>> = profileFlow.map { profile ->
        if (profile.subjects.isNotEmpty()) profile.subjects.sorted()
        else emptyList()
    }
}
