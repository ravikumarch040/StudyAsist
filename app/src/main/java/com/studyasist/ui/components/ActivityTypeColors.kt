package com.studyasist.ui.components

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import com.studyasist.data.local.entity.ActivityType

/**
 * Returns (containerColor, contentColor) for the given activity type.
 * Uses Material 3 color tokens for theme consistency (light/dark).
 */
fun ColorScheme.colorForActivityType(type: ActivityType): Pair<Color, Color> = when (type) {
    ActivityType.STUDY -> primaryContainer to onPrimaryContainer
    ActivityType.BREAK -> tertiaryContainer to onTertiaryContainer
    ActivityType.SCHOOL -> secondaryContainer to onSecondaryContainer
    ActivityType.TUITION -> secondaryContainer to onSecondaryContainer
    ActivityType.SLEEP -> surfaceVariant to onSurfaceVariant
}
