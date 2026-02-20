package com.studyasist.notification

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FocusGuardHelperTest {

    @Test
    fun restrictedPackages_isNotEmpty() {
        assertFalse(FOCUS_GUARD_RESTRICTED_PACKAGES.isEmpty())
    }

    @Test
    fun restrictedPackages_containsYouTube() {
        assertTrue(FOCUS_GUARD_RESTRICTED_PACKAGES.contains("com.google.android.youtube"))
    }

    @Test
    fun restrictedPackages_containsInstagram() {
        assertTrue(FOCUS_GUARD_RESTRICTED_PACKAGES.contains("com.instagram.android"))
    }

    @Test
    fun restrictedPackages_containsWhatsApp() {
        assertTrue(FOCUS_GUARD_RESTRICTED_PACKAGES.contains("com.whatsapp"))
    }

    @Test
    fun restrictedPackages_containsNetflix() {
        assertTrue(FOCUS_GUARD_RESTRICTED_PACKAGES.contains("com.netflix.mediaclient"))
    }

    @Test
    fun restrictedPackages_containsTikTok() {
        assertTrue(
            FOCUS_GUARD_RESTRICTED_PACKAGES.contains("com.zhiliaoapp.musically") ||
                FOCUS_GUARD_RESTRICTED_PACKAGES.contains("com.ss.android.ugc.trill")
        )
    }

    @Test
    fun restrictedPackages_hasReasonableSize() {
        assertTrue(FOCUS_GUARD_RESTRICTED_PACKAGES.size >= 10)
    }
}
