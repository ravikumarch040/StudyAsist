package com.studyasist.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.studyasist.data.local.dao.ActivityDao
import com.studyasist.data.local.dao.AssessmentDao
import com.studyasist.data.local.dao.BadgeDao
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
import com.studyasist.data.local.entity.ChatMessage
import com.studyasist.data.local.entity.PomodoroSession
import com.studyasist.data.local.dao.ChatMessageDao
import com.studyasist.data.local.dao.PomodoroDao
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
import com.studyasist.data.local.entity.StudyToolHistoryEntity
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

private val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE attempts ADD COLUMN needsManualReview INTEGER NOT NULL DEFAULT 0")
    }
}

private val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE results ADD COLUMN manualFeedback TEXT")
    }
}

private val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS study_tool_history (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                toolType TEXT NOT NULL,
                inputText TEXT NOT NULL,
                usedAt INTEGER NOT NULL
            )
        """.trimIndent())
        db.execSQL("CREATE INDEX IF NOT EXISTS index_study_tool_history_toolType_usedAt ON study_tool_history(toolType, usedAt)")
    }
}

private val MIGRATION_6_7 = object : Migration(6, 7) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS badges_earned (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                badgeId TEXT NOT NULL,
                earnedAt INTEGER NOT NULL
            )
        """.trimIndent())
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_badges_earned_badgeId ON badges_earned(badgeId)")
    }
}

private val MIGRATION_7_8 = object : Migration(7, 8) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE qa_bank ADD COLUMN easeFactor REAL NOT NULL DEFAULT 2.5")
        db.execSQL("ALTER TABLE qa_bank ADD COLUMN srsInterval INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE qa_bank ADD COLUMN repetitions INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE qa_bank ADD COLUMN nextReviewDate INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE qa_bank ADD COLUMN lastReviewDate INTEGER")
    }
}

private val MIGRATION_8_9 = object : Migration(8, 9) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS pomodoro_sessions (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                startedAt INTEGER NOT NULL,
                endedAt INTEGER,
                durationMinutes INTEGER NOT NULL,
                type TEXT NOT NULL,
                subject TEXT,
                completed INTEGER NOT NULL DEFAULT 0
            )
        """.trimIndent())
    }
}

private val MIGRATION_9_10 = object : Migration(9, 10) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE attempt_answers ADD COLUMN timeSpentSeconds INTEGER NOT NULL DEFAULT 0")
    }
}

private val MIGRATION_10_11 = object : Migration(10, 11) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS chat_messages (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                role TEXT NOT NULL,
                content TEXT NOT NULL,
                subject TEXT,
                chapter TEXT,
                createdAt INTEGER NOT NULL
            )
        """.trimIndent())
    }
}

@Database(
    entities = [
        TimetableEntity::class,
        ActivityEntity::class,
        BadgeEarned::class,
        Goal::class,
        GoalItem::class,
        QA::class,
        Assessment::class,
        AssessmentQuestion::class,
        Attempt::class,
        AttemptAnswer::class,
        Result::class,
        StudyToolHistoryEntity::class,
        PomodoroSession::class,
        ChatMessage::class
    ],
    version = 11,
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
    abstract fun studyToolHistoryDao(): StudyToolHistoryDao
    abstract fun badgeDao(): BadgeDao
    abstract fun pomodoroDao(): PomodoroDao
    abstract fun chatMessageDao(): ChatMessageDao

    companion object {
        fun migrations(): Array<Migration> = arrayOf(
            MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5,
            MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9,
            MIGRATION_9_10, MIGRATION_10_11
        )
    }
}
