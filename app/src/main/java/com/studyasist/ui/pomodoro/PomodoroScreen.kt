package com.studyasist.ui.pomodoro

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.studyasist.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PomodoroScreen(
    viewModel: PomodoroViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.pomodoro)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            val phaseLabel = when (uiState.phase) {
                PomodoroPhase.FOCUS -> stringResource(R.string.pomodoro_focus)
                PomodoroPhase.SHORT_BREAK -> stringResource(R.string.pomodoro_short_break)
                PomodoroPhase.LONG_BREAK -> stringResource(R.string.pomodoro_long_break)
            }

            Text(
                phaseLabel,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(8.dp))

            Text(
                stringResource(R.string.pomodoro_session_format, uiState.currentSession, uiState.totalSessions),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(32.dp))

            val progress by animateFloatAsState(
                targetValue = if (uiState.totalSeconds > 0)
                    1f - (uiState.remainingSeconds.toFloat() / uiState.totalSeconds)
                else 0f,
                animationSpec = tween(300),
                label = "progress"
            )

            val arcColor = when (uiState.phase) {
                PomodoroPhase.FOCUS -> MaterialTheme.colorScheme.primary
                PomodoroPhase.SHORT_BREAK -> MaterialTheme.colorScheme.secondary
                PomodoroPhase.LONG_BREAK -> MaterialTheme.colorScheme.tertiary
            }
            val trackColor = MaterialTheme.colorScheme.surfaceVariant

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(240.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = 12.dp.toPx()
                    val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
                    val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)

                    drawArc(
                        color = trackColor,
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(strokeWidth, cap = StrokeCap.Round)
                    )
                    drawArc(
                        color = arcColor,
                        startAngle = -90f,
                        sweepAngle = 360f * progress,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(strokeWidth, cap = StrokeCap.Round)
                    )
                }

                val minutes = uiState.remainingSeconds / 60
                val seconds = uiState.remainingSeconds % 60
                Text(
                    "%02d:%02d".format(minutes, seconds),
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Spacer(Modifier.height(40.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (uiState.isRunning) {
                    Button(onClick = { viewModel.pause() }) {
                        Text(stringResource(R.string.pomodoro_pause))
                    }
                } else {
                    Button(onClick = { viewModel.start() }) {
                        Text(
                            if (uiState.remainingSeconds == uiState.totalSeconds)
                                stringResource(R.string.pomodoro_start)
                            else stringResource(R.string.pomodoro_resume)
                        )
                    }
                }
                OutlinedButton(onClick = { viewModel.reset() }) {
                    Text(stringResource(R.string.pomodoro_reset))
                }
                OutlinedButton(onClick = { viewModel.skipPhase() }) {
                    Text(stringResource(R.string.pomodoro_skip))
                }
            }

            Spacer(Modifier.height(24.dp))

            val hrs = uiState.todayFocusMinutes / 60
            val mins = uiState.todayFocusMinutes % 60
            val timeStr = if (hrs > 0) "${hrs}h ${mins}m" else "${mins}m"
            Text(
                stringResource(R.string.pomodoro_total_today, timeStr),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
