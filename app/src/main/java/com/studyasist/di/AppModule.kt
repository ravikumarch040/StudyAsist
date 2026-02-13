package com.studyasist.di

import android.content.Context
import androidx.room.Room
import androidx.work.WorkManager
import com.google.gson.Gson
import com.studyasist.data.local.db.AppDatabase
import com.studyasist.data.local.dao.ActivityDao
import com.studyasist.data.local.dao.AssessmentDao
import com.studyasist.data.local.dao.AssessmentQuestionDao
import com.studyasist.data.local.dao.BadgeDao
import com.studyasist.data.local.dao.AttemptAnswerDao
import com.studyasist.data.local.dao.AttemptDao
import com.studyasist.data.local.dao.GoalDao
import com.studyasist.data.local.dao.GoalItemDao
import com.studyasist.data.local.dao.QADao
import com.studyasist.data.local.dao.ResultDao
import com.studyasist.data.local.dao.StudyToolHistoryDao
import com.studyasist.data.local.dao.TimetableDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager =
        WorkManager.getInstance(context)

    @Provides
    @Singleton
    fun provideGson(): Gson = Gson()

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "studyasist.db")
            .addMigrations(*AppDatabase.migrations())
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    @Singleton
    fun provideTimetableDao(db: AppDatabase): TimetableDao = db.timetableDao()

    @Provides
    @Singleton
    fun provideActivityDao(db: AppDatabase): ActivityDao = db.activityDao()

    @Provides
    @Singleton
    fun provideGoalDao(db: AppDatabase): GoalDao = db.goalDao()

    @Provides
    @Singleton
    fun provideGoalItemDao(db: AppDatabase): GoalItemDao = db.goalItemDao()

    @Provides
    @Singleton
    fun provideQADao(db: AppDatabase): QADao = db.qaDao()

    @Provides
    @Singleton
    fun provideAssessmentDao(db: AppDatabase): AssessmentDao = db.assessmentDao()

    @Provides
    @Singleton
    fun provideAssessmentQuestionDao(db: AppDatabase): AssessmentQuestionDao = db.assessmentQuestionDao()

    @Provides
    @Singleton
    fun provideAttemptDao(db: AppDatabase): AttemptDao = db.attemptDao()

    @Provides
    @Singleton
    fun provideAttemptAnswerDao(db: AppDatabase): AttemptAnswerDao = db.attemptAnswerDao()

    @Provides
    @Singleton
    fun provideResultDao(db: AppDatabase): ResultDao = db.resultDao()

    @Provides
    @Singleton
    fun provideStudyToolHistoryDao(db: AppDatabase): StudyToolHistoryDao = db.studyToolHistoryDao()

    @Provides
    @Singleton
    fun provideBadgeDao(db: AppDatabase): BadgeDao = db.badgeDao()
}
