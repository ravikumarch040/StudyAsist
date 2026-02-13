package com.studyasist.data.repository

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
import javax.inject.Inject
import javax.inject.Singleton

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
    val badgesEarned: List<BadgeEarned>? = null
)

@Singleton
class BackupRepository @Inject constructor(
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
    private val gson: Gson
) {

    suspend fun exportToJson(): String {
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
            badgesEarned = badgeDao.getAllEarnedOnce()
        )
        return gson.toJson(data)
    }

    suspend fun importFromJson(json: String): kotlin.Result<Unit> = runCatching {
        val data = gson.fromJson<BackupData>(json, object : TypeToken<BackupData>() {}.type)
            ?: throw IllegalArgumentException("Invalid backup format")
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
    }
}
