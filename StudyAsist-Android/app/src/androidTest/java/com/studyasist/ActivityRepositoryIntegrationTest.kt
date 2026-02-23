package com.studyasist

import com.studyasist.data.local.entity.ActivityEntity
import com.studyasist.data.local.entity.ActivityType
import com.studyasist.data.local.entity.TimetableEntity
import com.studyasist.data.local.entity.WeekType
import com.studyasist.data.repository.ActivityRepository
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.ext.junit.runners.AndroidJUnit4
import javax.inject.Inject

/**
 * Integration tests for ActivityRepository (TC-AC01 to TC-AC07).
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class ActivityRepositoryIntegrationTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var database: com.studyasist.data.local.db.AppDatabase

    @Inject
    lateinit var activityRepository: ActivityRepository

    private var timetableId: Long = 0L

    @Before
    fun setup() = runBlocking {
        hiltRule.inject()
        val now = System.currentTimeMillis()
        timetableId = database.timetableDao().insert(
            TimetableEntity(id = 0, name = "Test", weekType = WeekType.MON_SUN, startDate = null, createdAt = now, updatedAt = now)
        )
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun insertActivity_returnsId() = runBlocking {
        val id = activityRepository.insertActivity(activity(1, 540, 600))
        assertTrue(id > 0)
    }

    @Test
    fun getActivitiesForDay_returnsList() = runBlocking {
        activityRepository.insertActivity(activity(1, 540, 600))
        activityRepository.insertActivity(activity(1, 600, 660))
        val acts = activityRepository.getActivitiesForDay(timetableId, 1)
        assertEquals(2, acts.size)
    }

    @Test
    fun updateActivity_persisted() = runBlocking {
        val id = activityRepository.insertActivity(activity(1, 540, 600))
        val act = activityRepository.getActivity(id)!!
        activityRepository.updateActivity(act.copy(title = "Updated"))
        assertEquals("Updated", activityRepository.getActivity(id)!!.title)
    }

    @Test
    fun deleteActivity_removed() = runBlocking {
        val id = activityRepository.insertActivity(activity(1, 540, 600))
        activityRepository.deleteActivity(id)
        assertNull(activityRepository.getActivity(id))
    }

    private fun activity(day: Int, startMin: Int, endMin: Int) = ActivityEntity(
        id = 0,
        timetableId = timetableId,
        dayOfWeek = day,
        startTimeMinutes = startMin,
        endTimeMinutes = endMin,
        title = "Activity",
        type = ActivityType.STUDY,
        note = null,
        notifyEnabled = false,
        notifyLeadMinutes = 5,
        useSpeechSound = false,
        alarmTtsMessage = null,
        sortOrder = 0
    )

    @Test
    fun hasOverlap_noOverlap() = runBlocking {
        activityRepository.insertActivity(activity(1, 540, 600)) // 9:00-10:00
        val overlapping = activityRepository.hasOverlap(timetableId, 1, 600, 660, 0L) // 10:00-11:00
        assertTrue(overlapping.isEmpty())
    }

    @Test
    fun hasOverlap_overlaps() = runBlocking {
        val id = activityRepository.insertActivity(activity(1, 540, 600)) // 9:00-10:00
        val overlapping = activityRepository.hasOverlap(timetableId, 1, 570, 630, 0L) // 9:30-10:30
        assertFalse(overlapping.isEmpty())
        assertEquals(1, overlapping.size)
        assertEquals(id, overlapping[0].id)
    }

    @Test
    fun hasOverlap_excludeSelf() = runBlocking {
        val id = activityRepository.insertActivity(activity(1, 540, 600))
        val overlapping = activityRepository.hasOverlap(timetableId, 1, 540, 600, excludeActivityId = id)
        assertTrue(overlapping.isEmpty())
    }

    @Test
    fun copyDayToDay() = runBlocking {
        activityRepository.insertActivity(activity(1, 540, 600))
        activityRepository.insertActivity(activity(1, 600, 660))
        activityRepository.copyDayToDay(timetableId, 1, 2)
        val monActs = activityRepository.getActivitiesForDay(timetableId, 1)
        val tueActs = activityRepository.getActivitiesForDay(timetableId, 2)
        assertEquals(2, monActs.size)
        assertEquals(2, tueActs.size)
        assertEquals(2, tueActs[0].dayOfWeek)
    }
}
