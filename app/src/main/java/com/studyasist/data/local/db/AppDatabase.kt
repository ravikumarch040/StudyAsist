package com.studyasist.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.studyasist.data.local.dao.ActivityDao
import com.studyasist.data.local.dao.TimetableDao
import com.studyasist.data.local.entity.ActivityEntity
import com.studyasist.data.local.entity.ActivityTypeConverter
import com.studyasist.data.local.entity.TimetableEntity
import com.studyasist.data.local.entity.WeekTypeConverter

private val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE activities ADD COLUMN useSpeechSound INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE activities ADD COLUMN alarmTtsMessage TEXT")
    }
}

@Database(
    entities = [TimetableEntity::class, ActivityEntity::class],
    version = 2,
    exportSchema = true
)
@TypeConverters(WeekTypeConverter::class, ActivityTypeConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun timetableDao(): TimetableDao
    abstract fun activityDao(): ActivityDao

    companion object {
        fun migrations(): Array<Migration> = arrayOf(MIGRATION_1_2)
    }
}
