package com.studyasist.data.repository

import android.content.Context
import com.studyasist.R
import dagger.hilt.android.qualifiers.ApplicationContext
import com.studyasist.data.local.db.AppDatabase
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.studyasist.data.local.dao.ActivityDao
import com.studyasist.data.local.dao.BadgeDao
import com.studyasist.data.local.dao.AssessmentDao
import com.studyasist.data.local.dao.AssessmentQuestionDao
import com.studyasist.data.local.dao.AttemptAnswerDao
import com.studyasist.data.local.dao.AttemptDao
import com.studyasist.data.local.dao.GoalDao
import com.studyasist.data.local.dao.GoalItemDao
import com.studyasist.data.local.dao.QADao
import com.studyasist.data.local.dao.ResultDao
import com.studyasist.data.local.dao.StudyToolHistoryDao
import com.studyasist.data.local.dao.TimetableDao
import com.studyasist.data.local.entity.BadgeEarned
import com.studyasist.data.local.entity.ActivityEntity
import com.studyasist.data.local.entity.Assessment
import com.studyasist.data.local.entity.AssessmentQuestion
import com.studyasist.data.local.entity.Attempt
import com.studyasist.data.local.entity.AttemptAnswer
import com.studyasist.data.local.entity.Goal
import com.studyasist.data.local.entity.GoalItem
import com.studyasist.data.local.entity.QA
import com.studyasist.data.local.entity.Result
import com.studyasist.data.local.entity.StudyToolHistoryEntity
import com.studyasist.data.local.entity.TimetableEntity
import com.studyasist.notification.NotificationScheduler
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

private data class BackupSettings(
    val defaultLeadMinutes: Int = 5,
    val vibrationEnabled: Boolean = true,
    val userName: String = "",
    val ttsVoiceName: String? = null,
    val geminiApiKey: String = "",
    val focusGuardEnabled: Boolean = false,
    val focusGuardRestrictedExtra: String = "",
    val blockOverlap: Boolean = false,
    val cloudBackupFolderUri: String? = null,
    val cloudBackupTarget: String? = null,
    val cloudBackupAuto: Boolean = false,
    val useCloudForParsing: Boolean = true,
    val useCloudForGrading: Boolean = true,
    val dictateLanguage: String = "en",
    val explainLanguage: String = "en",
    val solveLanguage: String = "en",
    val darkMode: String = "system",
    val appLocale: String? = null  // Added later; old backups omit this
)

private data class ExamBackupData(
    val version: Int = 1,
    val exportedAt: Long = System.currentTimeMillis(),
    val goals: List<Goal>,
    val goalItems: List<GoalItem>,
    val qaBank: List<QA>,
    val assessments: List<Assessment>,
    val assessmentQuestions: List<AssessmentQuestion>,
    val attempts: List<Attempt>,
    val attemptAnswers: List<AttemptAnswer>,
    val results: List<Result>
)

private data class BackupData(
    val version: Int = 2,
    val exportedAt: Long = System.currentTimeMillis(),
    val timetables: List<TimetableEntity>,
    val activities: List<ActivityEntity>,
    val goals: List<Goal>,
    val goalItems: List<GoalItem>,
    val qaBank: List<QA>,
    val assessments: List<Assessment>,
    val assessmentQuestions: List<AssessmentQuestion>,
    val attempts: List<Attempt>,
    val attemptAnswers: List<AttemptAnswer>,
    val results: List<Result>,
    val studyToolHistory: List<StudyToolHistoryEntity>? = null,
    val badgesEarned: List<BadgeEarned>? = null,
    val settings: BackupSettings? = null
)

@Singleton
class BackupRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val timetableDao: TimetableDao,
    private val activityDao: ActivityDao,
    private val goalDao: GoalDao,
    private val goalItemDao: GoalItemDao,
    private val qaDao: QADao,
    private val assessmentDao: AssessmentDao,
    private val assessmentQuestionDao: AssessmentQuestionDao,
    private val attemptDao: AttemptDao,
    private val attemptAnswerDao: AttemptAnswerDao,
    private val resultDao: ResultDao,
    private val studyToolHistoryDao: StudyToolHistoryDao,
    private val badgeDao: BadgeDao,
    private val database: AppDatabase,
    private val gson: Gson,
    private val settingsRepository: SettingsRepository,
    private val notificationScheduler: NotificationScheduler
) {

    suspend fun exportToJson(): String {
        val appSettings = settingsRepository.settingsFlow.first()
        val focusExtra = settingsRepository.getFocusGuardRestrictedExtra().joinToString(",")
        val dictLang = settingsRepository.getDictateLanguage()
        val explLang = settingsRepository.getExplainLanguage()
        val solvLang = settingsRepository.getSolveLanguage()
        val darkMode = settingsRepository.getDarkMode()
        val appLocale = appSettings.appLocale
        val backupSettings = BackupSettings(
            defaultLeadMinutes = appSettings.defaultLeadMinutes,
            vibrationEnabled = appSettings.vibrationEnabled,
            userName = appSettings.userName,
            ttsVoiceName = appSettings.ttsVoiceName,
            geminiApiKey = appSettings.geminiApiKey,
            focusGuardEnabled = appSettings.focusGuardEnabled,
            focusGuardRestrictedExtra = focusExtra,
            blockOverlap = appSettings.blockOverlap,
            cloudBackupFolderUri = appSettings.cloudBackupFolderUri,
            cloudBackupTarget = appSettings.cloudBackupTarget,
            cloudBackupAuto = appSettings.cloudBackupAuto,
            useCloudForParsing = appSettings.useCloudForParsing,
            useCloudForGrading = appSettings.useCloudForGrading,
            dictateLanguage = dictLang,
            explainLanguage = explLang,
            solveLanguage = solvLang,
            darkMode = darkMode,
            appLocale = appLocale
        )
        val data = BackupData(
            timetables = timetableDao.getAllOnce(),
            activities = activityDao.getAll(),
            goals = goalDao.getAllOnce(),
            goalItems = goalItemDao.getAll(),
            qaBank = qaDao.getAllOnce(),
            assessments = assessmentDao.getAllOnce(),
            assessmentQuestions = assessmentQuestionDao.getAll(),
            attempts = attemptDao.getAll(),
            attemptAnswers = attemptAnswerDao.getAll(),
            results = resultDao.getAll(),
            studyToolHistory = studyToolHistoryDao.getAll(),
            badgesEarned = badgeDao.getAllEarnedOnce(),
            settings = backupSettings
        )
        return gson.toJson(data)
    }

    /** Exports only exam data (goals, QA bank, assessments, attempts, results). */
    suspend fun exportExamDataToJson(): String {
        val data = ExamBackupData(
            goals = goalDao.getAllOnce(),
            goalItems = goalItemDao.getAll(),
            qaBank = qaDao.getAllOnce(),
            assessments = assessmentDao.getAllOnce(),
            assessmentQuestions = assessmentQuestionDao.getAll(),
            attempts = attemptDao.getAll(),
            attemptAnswers = attemptAnswerDao.getAll(),
            results = resultDao.getAll()
        )
        return gson.toJson(data)
    }

    /** Restores exam data only. Replaces all goals, QA bank, assessments, attempts, results. */
    suspend fun importExamDataFromJson(json: String): kotlin.Result<Unit> = runCatching {
        val data = gson.fromJson<ExamBackupData>(json, object : TypeToken<ExamBackupData>() {}.type)
            ?: throw IllegalArgumentException(context.getString(R.string.err_invalid_backup_format))
        val db = database.openHelper.writableDatabase
        db.execSQL("PRAGMA foreign_keys = OFF")
        try {
            db.beginTransaction()
            try {
                resultDao.deleteAll()
                attemptAnswerDao.deleteAll()
                attemptDao.deleteAll()
                assessmentQuestionDao.deleteAll()
                assessmentDao.deleteAll()
                goalItemDao.deleteAll()
                goalDao.deleteAll()
                qaDao.deleteAll()
                data.goals.forEach { goalDao.insert(it) }
                data.goalItems.forEach { goalItemDao.insert(it) }
                data.qaBank.forEach { qaDao.insert(it) }
                data.assessments.forEach { assessmentDao.insert(it) }
                data.assessmentQuestions.forEach { assessmentQuestionDao.insert(it) }
                data.attempts.forEach { attemptDao.insert(it) }
                data.attemptAnswers.forEach { attemptAnswerDao.insert(it) }
                data.results.forEach { resultDao.insert(it) }
                db.setTransactionSuccessful()
            } finally {
                db.endTransaction()
            }
        } finally {
            db.execSQL("PRAGMA foreign_keys = ON")
        }
        notificationScheduler.rescheduleAll()
    }

    suspend fun importFromJson(json: String): kotlin.Result<Unit> = runCatching {
        val data = gson.fromJson<BackupData>(json, object : TypeToken<BackupData>() {}.type)
            ?: throw IllegalArgumentException(context.getString(R.string.err_invalid_backup_format))
        val db = database.openHelper.writableDatabase
        db.execSQL("PRAGMA foreign_keys = OFF")
        try {
            db.beginTransaction()
            try {
                resultDao.deleteAll()
                attemptAnswerDao.deleteAll()
                attemptDao.deleteAll()
                assessmentQuestionDao.deleteAll()
                assessmentDao.deleteAll()
                goalItemDao.deleteAll()
                goalDao.deleteAll()
                activityDao.deleteAll()
                timetableDao.deleteAll()
                qaDao.deleteAll()
                studyToolHistoryDao.deleteAll()
                badgeDao.deleteAll()
                data.timetables.forEach { timetableDao.insert(it) }
                data.activities.forEach { activityDao.insert(it) }
                data.goals.forEach { goalDao.insert(it) }
                data.goalItems.forEach { goalItemDao.insert(it) }
                data.qaBank.forEach { qaDao.insert(it) }
                data.assessments.forEach { assessmentDao.insert(it) }
                data.assessmentQuestions.forEach { assessmentQuestionDao.insert(it) }
                data.attempts.forEach { attemptDao.insert(it) }
                data.attemptAnswers.forEach { attemptAnswerDao.insert(it) }
                data.results.forEach { resultDao.insert(it) }
                (data.studyToolHistory ?: emptyList()).forEach { studyToolHistoryDao.insert(it) }
                (data.badgesEarned ?: emptyList()).forEach { badgeDao.insert(it) }
                db.setTransactionSuccessful()
            } finally {
                db.endTransaction()
            }
        } finally {
            db.execSQL("PRAGMA foreign_keys = ON")
        }
        data.settings?.let { restoreSettings(it) }
    }

    private suspend fun restoreSettings(s: BackupSettings) {
        settingsRepository.setDefaultLeadMinutes(s.defaultLeadMinutes)
        settingsRepository.setVibrationEnabled(s.vibrationEnabled)
        settingsRepository.setUserName(s.userName)
        settingsRepository.setTtsVoiceName(s.ttsVoiceName)
        settingsRepository.setGeminiApiKey(s.geminiApiKey)
        settingsRepository.setFocusGuardEnabled(s.focusGuardEnabled)
        settingsRepository.setFocusGuardRestrictedExtra(
            s.focusGuardRestrictedExtra.split(",").map { it.trim() }.filter { it.isNotBlank() }.toSet()
        )
        settingsRepository.setBlockOverlap(s.blockOverlap)
        settingsRepository.setCloudBackupFolderUri(s.cloudBackupFolderUri)
        s.cloudBackupTarget?.takeIf { it.isNotEmpty() }?.let { settingsRepository.setCloudBackupTarget(it) }
        settingsRepository.setCloudBackupAuto(s.cloudBackupAuto)
        settingsRepository.setUseCloudForParsing(s.useCloudForParsing)
        settingsRepository.setUseCloudForGrading(s.useCloudForGrading)
        settingsRepository.setDictateLanguage(s.dictateLanguage)
        settingsRepository.setExplainLanguage(s.explainLanguage)
        settingsRepository.setSolveLanguage(s.solveLanguage)
        settingsRepository.setDarkMode(s.darkMode)
        settingsRepository.setAppLocale(s.appLocale?.takeIf { it.isNotBlank() } ?: "system")
        notificationScheduler.rescheduleAll()
    }
}
