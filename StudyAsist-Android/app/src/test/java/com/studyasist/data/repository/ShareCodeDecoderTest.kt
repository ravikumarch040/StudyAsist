package com.studyasist.data.repository

import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Unit tests for share code decoding (TC-SH04, TC-SH05).
 * Tests the local Base64 path used when code is invalid or malformed.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ShareCodeDecoderTest {

    private val gson = Gson()

    @Test
    fun `decode invalid code returns null - TC-SH04`() {
        assertNull(ShareCodeDecoder.decodeLocalShareCode("!!!garbage@@@###", gson))
        assertNull(ShareCodeDecoder.decodeLocalShareCode("not valid base64!!!", gson))
        assertNull(ShareCodeDecoder.decodeLocalShareCode("", gson))
    }

    @Test
    fun `decode malformed Base64 returns null no crash - TC-SH05`() {
        assertNull(ShareCodeDecoder.decodeLocalShareCode("a===b", gson))
        assertNull(ShareCodeDecoder.decodeLocalShareCode("====", gson))
        assertNull(ShareCodeDecoder.decodeLocalShareCode("ab!cd", gson))
    }

    @Test
    fun `decode valid Base64 returns ShareableAssessment`() {
        val shareable = ShareableAssessment(
            title = "Test",
            questions = listOf(
                ShareableQuestion("Q1?", "A1", "MCQ", "[\"A\",\"B\"]")
            )
        )
        val json = gson.toJson(shareable)
        val code = android.util.Base64.encodeToString(
            json.toByteArray(Charsets.UTF_8),
            android.util.Base64.URL_SAFE or android.util.Base64.NO_WRAP
        )
        val result = ShareCodeDecoder.decodeLocalShareCode(code, gson)
        assertNotNull(result)
        assertEquals("Test", result!!.title)
        assertEquals(1, result.questions.size)
        assertEquals("Q1?", result.questions[0].questionText)
    }
}
