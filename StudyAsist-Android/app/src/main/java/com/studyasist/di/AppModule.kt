package com.studyasist.di

import android.content.Context
import androidx.room.Room
import androidx.work.WorkManager
import com.google.gson.Gson
import com.studyasist.BuildConfig
import com.studyasist.data.api.AuthApi
import com.studyasist.data.api.LeaderboardApi
import com.studyasist.data.api.ShareApi
import com.studyasist.data.api.SyncApi
import com.studyasist.data.local.db.AppDatabase
import com.studyasist.data.network.AuthTokenProvider
import com.studyasist.data.network.AuthTokenInterceptor
import com.studyasist.data.network.SettingsAuthTokenProvider
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.studyasist.data.local.dao.ActivityDao
import com.studyasist.data.local.dao.AssessmentDao
import com.studyasist.data.local.dao.AssessmentQuestionDao
import com.studyasist.data.local.dao.BadgeDao
import com.studyasist.data.local.dao.ChatMessageDao
import com.studyasist.data.local.dao.PomodoroDao
import com.studyasist.data.local.dao.AttemptAnswerDao
import com.studyasist.data.local.dao.AttemptDao
import com.studyasist.data.local.dao.GoalDao
import com.studyasist.data.local.dao.GoalItemDao
import com.studyasist.data.local.dao.QADao
import com.studyasist.data.local.dao.ResultDao
import com.studyasist.data.local.dao.StudyToolHistoryDao
import com.studyasist.data.local.dao.TimetableDao
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AuthModule {
    @Binds
    @Singleton
    abstract fun bindAuthTokenProvider(impl: SettingsAuthTokenProvider): AuthTokenProvider
}

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
    fun provideOkHttpClient(authInterceptor: AuthTokenInterceptor): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .build()

    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient): Retrofit {
        val baseUrl = BuildConfig.BACKEND_BASE_URL.trimEnd('/') + "/"
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideAuthApi(retrofit: Retrofit): AuthApi = retrofit.create(AuthApi::class.java)

    @Provides
    @Singleton
    fun provideSyncApi(retrofit: Retrofit): SyncApi = retrofit.create(SyncApi::class.java)

    @Provides
    @Singleton
    fun provideLeaderboardApi(retrofit: Retrofit): LeaderboardApi = retrofit.create(LeaderboardApi::class.java)

    @Provides
    @Singleton
    fun provideShareApi(retrofit: Retrofit): ShareApi = retrofit.create(ShareApi::class.java)

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

    @Provides
    @Singleton
    fun providePomodoroDao(db: AppDatabase): PomodoroDao = db.pomodoroDao()

    @Provides
    @Singleton
    fun provideChatMessageDao(db: AppDatabase): ChatMessageDao = db.chatMessageDao()
}
