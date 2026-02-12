package com.studyasist.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.studyasist.data.local.dao.ActivityDao
import com.studyasist.data.local.dao.AssessmentDao
import com.studyasist.data.local.dao.AssessmentQuestionDao
import com.studyasist.data.local.dao.AttemptAnswerDao
import com.studyasist.data.local.dao.AttemptDao
import com.studyasist.data.local.dao.GoalDao
import com.studyasist.data.local.dao.GoalItemDao
import com.studyasist.data.local.dao.QADao
import com.studyasist.data.local.dao.ResultDao
import com.studyasist.data.local.dao.TimetableDao
import com.studyasist.data.local.entity.ActivityEntity
import com.studyasist.data.local.entity.ActivityTypeConverter
import com.studyasist.data.local.entity.Assessment
import com.studyasist.data.local.entity.AssessmentQuestion
import com.studyasist.data.local.entity.Attempt
import com.studyasist.data.local.entity.AttemptAnswer
import com.studyasist.data.local.entity.Goal
import com.studyasist.data.local.entity.GoalItem
import com.studyasist.data.local.entity.QA
import com.studyasist.data.local.entity.QuestionTypeConverter
import com.studyasist.data.local.entity.Result
import com.studyasist.data.local.entity.TimetableEntity
import com.studyasist.data.local.entity.WeekTypeConverter

private val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE activities ADD COLUMN useSpeechSound INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE activities ADD COLUMN alarmTtsMessage TEXT")
    }
}

private val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS goals (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                name TEXT NOT NULL,
                description TEXT,
                examDate INTEGER NOT NULL,
                createdAt INTEGER NOT NULL,
                isActive INTEGER NOT NULL DEFAULT 1
            )
        """.trimIndent())
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS goal_items (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                goalId INTEGER NOT NULL,
                subject TEXT NOT NULL,
                chapterList TEXT NOT NULL,
                targetHours INTEGER,
                FOREIGN KEY(goalId) REFERENCES goals(id) ON DELETE CASCADE
            )
        """.trimIndent())
        db.execSQL("CREATE INDEX IF NOT EXISTS index_goal_items_goalId ON goal_items(goalId)")

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS qa_bank (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                sourceCaptureId INTEGER,
                subject TEXT,
                chapter TEXT,
                questionText TEXT NOT NULL,
                answerText TEXT NOT NULL,
                questionType TEXT NOT NULL,
                optionsJson TEXT,
                metadataJson TEXT,
                createdAt INTEGER NOT NULL
            )
        """.trimIndent())
        db.execSQL("CREATE INDEX IF NOT EXISTS index_qa_bank_subject ON qa_bank(subject)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_qa_bank_chapter ON qa_bank(chapter)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_qa_bank_subject_chapter ON qa_bank(subject, chapter)")

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS assessments (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                title TEXT NOT NULL,
                goalId INTEGER,
                subject TEXT,
                chapter TEXT,
                totalTimeSeconds INTEGER NOT NULL,
                randomizeQuestions INTEGER NOT NULL DEFAULT 1,
                createdAt INTEGER NOT NULL,
                FOREIGN KEY(goalId) REFERENCES goals(id) ON DELETE SET NULL
            )
        """.trimIndent())
        db.execSQL("CREATE INDEX IF NOT EXISTS index_assessments_goalId ON assessments(goalId)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_assessments_subject ON assessments(subject)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_assessments_chapter ON assessments(chapter)")

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS assessment_questions (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                assessmentId INTEGER NOT NULL,
                qaId INTEGER NOT NULL,
                weight REAL NOT NULL DEFAULT 1.0,
                sequence INTEGER NOT NULL,
                FOREIGN KEY(assessmentId) REFERENCES assessments(id) ON DELETE CASCADE,
                FOREIGN KEY(qaId) REFERENCES qa_bank(id) ON DELETE CASCADE
            )
        """.trimIndent())
        db.execSQL("CREATE INDEX IF NOT EXISTS index_assessment_questions_assessmentId ON assessment_questions(assessmentId)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_assessment_questions_qaId ON assessment_questions(qaId)")

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS attempts (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                assessmentId INTEGER NOT NULL,
                startedAt INTEGER NOT NULL,
                endedAt INTEGER,
                userNotes TEXT,
                FOREIGN KEY(assessmentId) REFERENCES assessments(id) ON DELETE CASCADE
            )
        """.trimIndent())
        db.execSQL("CREATE INDEX IF NOT EXISTS index_attempts_assessmentId ON attempts(assessmentId)")

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS attempt_answers (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                attemptId INTEGER NOT NULL,
                qaId INTEGER NOT NULL,
                answerText TEXT,
                answerImageUri TEXT,
                answerVoiceUri TEXT,
                submittedAt INTEGER NOT NULL,
                FOREIGN KEY(attemptId) REFERENCES attempts(id) ON DELETE CASCADE
            )
        """.trimIndent())
        db.execSQL("CREATE INDEX IF NOT EXISTS index_attempt_answers_attemptId ON attempt_answers(attemptId)")

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS results (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                attemptId INTEGER NOT NULL,
                score REAL NOT NULL,
                maxScore REAL NOT NULL,
                percent REAL NOT NULL,
                detailsJson TEXT NOT NULL,
                FOREIGN KEY(attemptId) REFERENCES attempts(id) ON DELETE CASCADE
            )
        """.trimIndent())
        db.execSQL("CREATE INDEX IF NOT EXISTS index_results_attemptId ON results(attemptId)")
    }
}

@Database(
    entities = [
        TimetableEntity::class,
        ActivityEntity::class,
        Goal::class,
        GoalItem::class,
        QA::class,
        Assessment::class,
        AssessmentQuestion::class,
        Attempt::class,
        AttemptAnswer::class,
        Result::class
    ],
    version = 3,
    exportSchema = false
)
@TypeConverters(
    WeekTypeConverter::class,
    ActivityTypeConverter::class,
    QuestionTypeConverter::class
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun timetableDao(): TimetableDao
    abstract fun activityDao(): ActivityDao
    abstract fun goalDao(): GoalDao
    abstract fun goalItemDao(): GoalItemDao
    abstract fun qaDao(): QADao
    abstract fun assessmentDao(): AssessmentDao
    abstract fun assessmentQuestionDao(): AssessmentQuestionDao
    abstract fun attemptDao(): AttemptDao
    abstract fun attemptAnswerDao(): AttemptAnswerDao
    abstract fun resultDao(): ResultDao

    companion object {
        fun migrations(): Array<Migration> = arrayOf(MIGRATION_1_2, MIGRATION_2_3)
    }
}
