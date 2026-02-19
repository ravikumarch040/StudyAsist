package com.studyasist.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import kotlin.math.sin
import kotlin.random.Random

data class ConfettiParticle(
    val x: Float,
    val y: Float,
    val speed: Float,
    val angle: Float,
    val color: Color,
    val size: Float
)

@Composable
fun ConfettiEffect(
    trigger: Boolean,
    modifier: Modifier = Modifier
) {
    if (!trigger) return

    val progress = remember { Animatable(0f) }
    val particles = remember {
        List(60) {
            ConfettiParticle(
                x = Random.nextFloat(),
                y = -Random.nextFloat() * 0.3f,
                speed = 0.5f + Random.nextFloat() * 1.5f,
                angle = Random.nextFloat() * 360f,
                color = listOf(
                    Color(0xFFFFD700), Color(0xFFFF6B6B), Color(0xFF4D96FF),
                    Color(0xFF00B37E), Color(0xFFE040FB), Color(0xFFFFA000)
                ).random(),
                size = 6f + Random.nextFloat() * 8f
            )
        }
    }

    LaunchedEffect(trigger) {
        progress.animateTo(1f, tween(2500, easing = LinearEasing))
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val t = progress.value
        particles.forEach { p ->
            val px = p.x * size.width + sin(t * p.angle * 0.02f) * 40f
            val py = p.y * size.height + t * size.height * p.speed
            val alpha = (1f - t).coerceIn(0f, 1f)
            if (py < size.height && py > 0) {
                drawRect(
                    color = p.color.copy(alpha = alpha),
                    topLeft = Offset(px, py),
                    size = Size(p.size, p.size * 0.6f)
                )
            }
        }
    }
}
