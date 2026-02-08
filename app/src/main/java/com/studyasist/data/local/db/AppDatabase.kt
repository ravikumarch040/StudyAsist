package com.studyasist.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.studyasist.data.local.dao.ActivityDao
import com.studyasist.data.local.dao.TimetableDao
import com.studyasist.data.local.entity.ActivityEntity
import com.studyasist.data.local.entity.ActivityTypeConverter
import com.studyasist.data.local.entity.TimetableEntity
import com.studyasist.data.local.entity.WeekTypeConverter

@Database(
    entities = [TimetableEntity::class, ActivityEntity::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(WeekTypeConverter::class, ActivityTypeConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun timetableDao(): TimetableDao
    abstract fun activityDao(): ActivityDao
}
