package com.studyasist.ui

import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType

/**
 * Delegates to the platform HapticFeedback only when [enabled].
 * Used at app root via CompositionLocalProvider to respect Settings → Accessibility → Haptic feedback.
 */
class ConditionalHapticFeedback(
    private val enabled: Boolean,
    private val delegate: HapticFeedback
) : HapticFeedback {
    override fun performHapticFeedback(feedbackType: HapticFeedbackType) {
        if (enabled) delegate.performHapticFeedback(feedbackType)
    }
}
