package com.studyasist.di

import android.content.Context
import androidx.room.Room
import com.studyasist.data.local.db.AppDatabase
import com.studyasist.data.local.dao.ActivityDao
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
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "studyasist.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    @Singleton
    fun provideTimetableDao(db: AppDatabase): TimetableDao = db.timetableDao()

    @Provides
    @Singleton
    fun provideActivityDao(db: AppDatabase): ActivityDao = db.activityDao()
}
