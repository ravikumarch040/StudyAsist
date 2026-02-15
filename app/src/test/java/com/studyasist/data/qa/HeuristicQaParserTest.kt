package com.studyasist.data.qa

import com.studyasist.data.local.entity.QuestionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class HeuristicQaParserTest {

    @Test
    fun `parse returns empty list for blank input`() {
        val result = HeuristicQaParser.parse("")
        assertTrue(result.isEmpty())
    }

    @Test
    fun `parse returns empty list for whitespace only`() {
        val result = HeuristicQaParser.parse("   \n\t  ")
        assertTrue(result.isEmpty())
    }

    @Test
    fun `parse extracts numbered question`() {
        val input = """
            1. What is the capital of France?
            Answer: Paris
        """.trimIndent()
        val result = HeuristicQaParser.parse(input)
        assertEquals(1, result.size)
        assertTrue(result[0].question.contains("capital of France"))
        assertEquals("Paris", result[0].answer)
    }

    @Test
    fun `parse extracts Q numbered format`() {
        val input = """
            Q1. What is 2 + 2?
            Answer: 4
        """.trimIndent()
        val result = HeuristicQaParser.parse(input)
        assertEquals(1, result.size)
        assertTrue(result[0].question.contains("2 + 2"))
        assertEquals("4", result[0].answer)
    }

    @Test
    fun `parse extracts multiple numbered questions`() {
        val input = """
            1. First question?
            Answer: One

            2. Second question?
            Answer: Two
        """.trimIndent()
        val result = HeuristicQaParser.parse(input)
        assertEquals(2, result.size)
        assertTrue(result[0].question.contains("First"))
        assertEquals("One", result[0].answer)
        assertTrue(result[1].question.contains("Second"))
        assertEquals("Two", result[1].answer)
    }

    @Test
    fun `parse detects MCQ type`() {
        val input = """
            1. Choose the correct answer:
            a. Option A
            b. Option B
            c. Option C
            d. Option D
            Answer: C
        """.trimIndent()
        val result = HeuristicQaParser.parse(input)
        assertEquals(1, result.size)
        assertEquals(QuestionType.MCQ, result[0].type)
        assertEquals("C", result[0].answer)
        assertNotNull(result[0].optionsJson)
    }

    @Test
    fun `parse detects true or false type`() {
        val input = """
            1. The sky is blue. True/False
            Answer: True
        """.trimIndent()
        val result = HeuristicQaParser.parse(input)
        assertEquals(1, result.size)
        assertEquals(QuestionType.TRUE_FALSE, result[0].type)
        assertEquals("True", result[0].answer)
    }

    @Test
    fun `parse extracts question and answer split by question mark`() {
        val input = """
            Who wrote Romeo and Juliet? William Shakespeare
        """.trimIndent()
        val result = HeuristicQaParser.parse(input)
        assertEquals(1, result.size)
        assertTrue(result[0].question.contains("Romeo and Juliet"))
        assertTrue(result[0].answer.contains("Shakespeare"))
    }

    @Test
    fun `parse handles OCR normalization`() {
        val input = "1.\u00a0What   is   the   answer?\u00a0\u00a0Answer:\u00a0Forty"
        val result = HeuristicQaParser.parse(input)
        assertEquals(1, result.size)
        assertTrue(result[0].question.contains("answer"))
        assertEquals("Forty", result[0].answer)
    }

    @Test
    fun `parse detects fill in blank type`() {
        val input = """
            1. The capital of France is _______.
            Answer: Paris
        """.trimIndent()
        val result = HeuristicQaParser.parse(input)
        assertEquals(1, result.size)
        assertEquals(QuestionType.FILL_BLANK, result[0].type)
    }

    @Test
    fun `parse handles question word start`() {
        val input = """
            What is photosynthesis?
            Answer: Process by which plants make food
        """.trimIndent()
        val result = HeuristicQaParser.parse(input)
        assertEquals(1, result.size)
        assertTrue(result[0].question.contains("photosynthesis"))
        assertTrue(result[0].answer.contains("plants"))
    }

    @Test
    fun `parse handles parenthesis number format`() {
        val input = """
            (1) Define velocity.
            Answer: Speed with direction
        """.trimIndent()
        val result = HeuristicQaParser.parse(input)
        assertEquals(1, result.size)
        assertTrue(result[0].question.contains("velocity"))
        assertEquals("Speed with direction", result[0].answer)
    }

    @Test
    fun `parse handles Roman numeral format`() {
        val input = """
            I. First point?
            Answer: Alpha

            II. Second point?
            Answer: Beta
        """.trimIndent()
        val result = HeuristicQaParser.parse(input)
        assertEquals(2, result.size)
        assertTrue(result[0].question.contains("First"))
        assertTrue(result[1].question.contains("Second"))
    }

    @Test
    fun `parse handles ans marker`() {
        val input = """
            1. Quick question?
            Ans. Quick answer
        """.trimIndent()
        val result = HeuristicQaParser.parse(input)
        assertEquals(1, result.size)
        assertEquals("Quick answer", result[0].answer)
    }
}
