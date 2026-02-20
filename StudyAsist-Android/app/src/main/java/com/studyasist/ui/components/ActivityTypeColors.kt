package com.studyasist.ui.components

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import com.studyasist.data.local.entity.ActivityType

object ScoreColors {
    val excellent = Color(0xFFFFD700)
    val good = Color(0xFF4CAF50)
    val average = Color(0xFFFFA000)
    val poor = Color(0xFFE53935)
    val examUrgent = Color(0xFFD32F2F)
}

/**
 * Color psychology-based activity type colors.
 * Returns a Pair of (container color, content color).
 */
fun ColorScheme.colorForActivityType(type: ActivityType?): Pair<Color, Color> = when (type) {
    ActivityType.STUDY -> Color(0xFF1565C0).copy(alpha = 0.12f) to Color(0xFF1565C0)
    ActivityType.BREAK -> Color(0xFF2E7D32).copy(alpha = 0.12f) to Color(0xFF2E7D32)
    ActivityType.SCHOOL -> Color(0xFFF57F17).copy(alpha = 0.12f) to Color(0xFFF57F17)
    ActivityType.TUITION -> Color(0xFFE65100).copy(alpha = 0.12f) to Color(0xFFE65100)
    ActivityType.SLEEP -> Color(0xFF283593).copy(alpha = 0.12f) to Color(0xFF283593)
    null -> surfaceVariant to onSurfaceVariant
}

fun scoreColor(percent: Float): Color = when {
    percent >= 80f -> ScoreColors.excellent
    percent >= 60f -> ScoreColors.good
    percent >= 40f -> ScoreColors.average
    else -> ScoreColors.poor
}
