package com.studyasist

import com.studyasist.data.local.entity.ActivityEntity
import com.studyasist.data.local.entity.ActivityType
import com.studyasist.data.local.entity.TimetableEntity
import com.studyasist.data.local.entity.WeekType
import com.studyasist.data.repository.TimetableRepository
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.ext.junit.runners.AndroidJUnit4
import javax.inject.Inject

/**
 * Integration tests for TimetableRepository (TC-TT01 to TC-TT07).
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class TimetableRepositoryIntegrationTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var database: com.studyasist.data.local.db.AppDatabase

    @Inject
    lateinit var timetableRepository: TimetableRepository

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun createTimetable_returnsId_andExists() = runBlocking {
        val id = timetableRepository.createTimetable("Test", WeekType.MON_SUN)
        assertTrue(id > 0)
        val tt = timetableRepository.getTimetable(id)
        assertNotNull(tt)
        assertEquals("Test", tt!!.name)
    }

    @Test
    fun getAllTimetables_emitsCreated() = runBlocking {
        timetableRepository.createTimetable("T1", WeekType.MON_SUN)
        timetableRepository.createTimetable("T2", WeekType.MON_SUN)
        val list = timetableRepository.getAllTimetables().first()
        assertTrue(list.size >= 2)
        assertTrue(list.any { it.name == "T1" })
        assertTrue(list.any { it.name == "T2" })
    }

    @Test
    fun getTimetable_byId_exists() = runBlocking {
        val id = timetableRepository.createTimetable("MyTT", WeekType.MON_SUN)
        val tt = timetableRepository.getTimetable(id)
        assertNotNull(tt)
        assertEquals("MyTT", tt!!.name)
    }

    @Test
    fun getTimetable_nonExistent_returnsNull() = runBlocking {
        val tt = timetableRepository.getTimetable(99999)
        assertNull(tt)
    }

    @Test
    fun updateTimetable_persisted() = runBlocking {
        val id = timetableRepository.createTimetable("Original", WeekType.MON_SUN)
        val tt = timetableRepository.getTimetable(id)!!
        timetableRepository.updateTimetable(tt.copy(name = "Updated"))
        val updated = timetableRepository.getTimetable(id)
        assertEquals("Updated", updated!!.name)
    }

    @Test
    fun deleteTimetable_removesAndCascades() = runBlocking {
        val id = timetableRepository.createTimetable("ToDelete", WeekType.MON_SUN)
        database.activityDao().insert(
            ActivityEntity(0, id, 1, 540, 600, "A", ActivityType.STUDY, null, false, 5, false, null, 0)
        )
        timetableRepository.deleteTimetable(id)
        assertNull(timetableRepository.getTimetable(id))
        assertEquals(0, database.activityDao().getByTimetableAndDay(id, 1).size)
    }

    @Test
    fun duplicateTimetable_copiesActivities() = runBlocking {
        val srcId = timetableRepository.createTimetable("Source", WeekType.MON_SUN)
        database.activityDao().insert(
            ActivityEntity(0, srcId, 1, 540, 600, "Study", ActivityType.STUDY, null, false, 5, false, null, 0)
        )
        val copyId = timetableRepository.duplicateTimetable(srcId, "Copy")
        assertTrue(copyId > 0)
        val copyActs = database.activityDao().getByTimetableAndDay(copyId, 1)
        assertEquals(1, copyActs.size)
        assertEquals("Study", copyActs[0].title)
    }
}
