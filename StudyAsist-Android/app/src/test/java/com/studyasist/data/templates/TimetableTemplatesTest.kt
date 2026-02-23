package com.studyasist.data.templates

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for TimetableTemplates (TC-TM01 to TC-TM04).
 */
class TimetableTemplatesTest {

    @Test
    fun genericSchoolDay_fiveDaysEightActivities() {
        val template = TimetableTemplates.genericSchoolDay
        assertEquals("Generic School Day", template.name)
        assertEquals("MON_SUN", template.weekType)
        val days = template.activities.map { it.dayOfWeek }.distinct().sorted()
        assertEquals(5, days.size)
        assertEquals(listOf(1, 2, 3, 4, 5), days)
        val perDay = template.activities.groupBy { it.dayOfWeek }
        assertEquals(8, perDay[1]!!.size)
    }

    @Test
    fun weekendStudy_twoDays() {
        val template = TimetableTemplates.weekendStudy
        assertEquals("Weekend Study", template.name)
        val days = template.activities.map { it.dayOfWeek }.distinct().sorted()
        assertEquals(2, days.size)
        assertTrue(days.contains(6))
        assertTrue(days.contains(7))
    }

    @Test
    fun examPrep_sevenDays() {
        val template = TimetableTemplates.examPrep
        assertEquals("Exam Preparation", template.name)
        val days = template.activities.map { it.dayOfWeek }.distinct().sorted()
        assertEquals(7, days.size)
        assertEquals(listOf(1, 2, 3, 4, 5, 6, 7), days)
    }

    @Test
    fun templateActivity_structureValid() {
        val template = TimetableTemplates.genericSchoolDay
        val first = template.activities.first()
        assertTrue(first.dayOfWeek in 1..7)
        assertTrue(first.startHour in 0..23)
        assertTrue(first.endHour in 0..24)
        assertTrue(first.title.isNotBlank())
        assertTrue(first.type in listOf("BREAK", "SCHOOL", "STUDY", "SLEEP"))
    }
}
