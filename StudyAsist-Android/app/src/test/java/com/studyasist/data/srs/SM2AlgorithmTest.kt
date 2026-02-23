package com.studyasist.data.srs

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SM2AlgorithmTest {

    @Test
    fun `quality 0 Again - interval 1 reps 0 EF decreased`() {
        val result = sm2(quality = 0, easeFactor = 2.5, interval = 6, repetitions = 3)
        assertEquals(1, result.interval)
        assertEquals(0, result.repetitions)
        assertTrue(result.easeFactor < 2.5)
        assertEquals(2.3, result.easeFactor, 0.001)
    }

    @Test
    fun `quality 1 Hard - passes with reduced EF and increased interval`() {
        val result = sm2(quality = 1, easeFactor = 2.5, interval = 6, repetitions = 2)
        assertEquals(3, result.repetitions)
        assertTrue(result.easeFactor < 2.5)
        assertTrue(result.interval > 1)
    }

    @Test
    fun `quality 2 Good first review - interval 1 reps 1`() {
        val result = sm2(quality = 2, easeFactor = 2.5, interval = 0, repetitions = 0)
        assertEquals(1, result.interval)
        assertEquals(1, result.repetitions)
        assertEquals(2.5, result.easeFactor, 0.001)
    }

    @Test
    fun `quality 2 Good second review - interval 6 reps 2`() {
        val result = sm2(quality = 2, easeFactor = 2.6, interval = 1, repetitions = 1)
        assertEquals(6, result.interval)
        assertEquals(2, result.repetitions)
        assertEquals(2.6, result.easeFactor, 0.001)
    }

    @Test
    fun `quality 3 Easy first review - interval 1 reps 1`() {
        val result = sm2(quality = 3, easeFactor = 2.5, interval = 0, repetitions = 0)
        assertEquals(1, result.interval)
        assertEquals(1, result.repetitions)
        assertTrue(result.easeFactor > 2.5)
        assertEquals(2.6, result.easeFactor, 0.001)
    }

    @Test
    fun `quality out of range coerced to 3`() {
        val result = sm2(quality = 5, easeFactor = 2.5, interval = 0, repetitions = 0)
        assertEquals(1, result.interval)
        assertEquals(1, result.repetitions)
        assertEquals(2.6, result.easeFactor, 0.001)
    }

    @Test
    fun `EF never below 1_3 - many Again ratings`() {
        var ef = 2.5
        repeat(10) {
            val result = sm2(quality = 0, easeFactor = ef, interval = 6, repetitions = 3)
            ef = result.easeFactor
            assertTrue("EF must be >= 1.3 after iteration $it", result.easeFactor >= 1.3)
        }
        assertEquals(1.3, ef, 0.001)
    }

    @Test
    fun `interval never below 1`() {
        val result = sm2(quality = 2, easeFactor = 0.5, interval = 0, repetitions = 0)
        assertEquals(1, result.interval)
    }
}
