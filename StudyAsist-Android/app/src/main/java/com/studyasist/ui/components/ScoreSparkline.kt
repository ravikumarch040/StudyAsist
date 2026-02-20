package com.studyasist.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

/**
 * Draws a simple line sparkline of score percentages over time.
 * @param scores List of percentages (0–100), in chronological order (oldest first).
 * @param color Line color.
 * @param modifier Modifier for the composable.
 */
@Composable
fun ScoreSparkline(
    scores: List<Float>,
    color: Color,
    modifier: Modifier = Modifier
) {
    if (scores.isEmpty()) return

    val strokeWidth = 2.dp
    val padding = 4.dp

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
    ) {
        val width = size.width - padding.toPx() * 2
        val height = size.height - padding.toPx() * 2
        val left = padding.toPx()
        val bottom = size.height - padding.toPx()

        val minScore = scores.minOrNull()?.coerceAtLeast(0f) ?: 0f
        val maxScore = scores.maxOrNull()?.coerceAtMost(100f) ?: 100f
        val range = (maxScore - minScore).coerceAtLeast(4f)

        val points = scores.mapIndexed { index, score ->
            val x = left + (width * index / (scores.size - 1).coerceAtLeast(1))
            val y = bottom - (height * (score - minScore) / range)
            Offset(x, y.coerceIn(padding.toPx(), bottom))
        }

        if (points.size >= 2) {
            val path = Path().apply {
                moveTo(points.first().x, points.first().y)
                for (i in 1 until points.size) {
                    lineTo(points[i].x, points[i].y)
                }
            }
            drawPath(
                path = path,
                color = color,
                style = Stroke(
                    width = strokeWidth.toPx(),
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )
        } else if (points.size == 1) {
            drawCircle(
                color = color,
                radius = strokeWidth.toPx() * 2,
                center = points.first()
            )
        }
    }
}
