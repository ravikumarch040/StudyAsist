package com.studyasist.data.repository

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for LicenseRepository (TC-LC01 to TC-LC03).
 */
class LicenseRepositoryTest {

    private val repo = LicenseRepository()

    @Test
    fun isPremium_defaultTrue() {
        assertTrue(repo.isPremium())
    }

    @Test
    fun maxTimetables_premiumReturnsMaxInt() {
        assertEquals(Int.MAX_VALUE, repo.maxTimetables())
    }

    @Test
    fun maxQAScansPerMonth_premiumReturnsMaxInt() {
        assertEquals(Int.MAX_VALUE, repo.maxQAScansPerMonth())
    }
}
