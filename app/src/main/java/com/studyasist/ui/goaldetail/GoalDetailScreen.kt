package com.studyasist.ui.goaldetail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.studyasist.R
import com.studyasist.data.repository.RecentAttemptSummary
import com.studyasist.data.repository.SuggestedPracticeArea
import com.studyasist.data.repository.SubjectChapterProgress
import com.studyasist.data.repository.TrackPrediction
import com.studyasist.data.repository.TrackStatus
import com.studyasist.ui.components.ActivityHeatmap
import com.studyasist.ui.components.ScoreSparkline
import com.studyasist.util.formatExamDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalDetailScreen(
    viewModel: GoalDetailViewModel,
    onBack: () -> Unit,
    onEditGoal: (Long) -> Unit,
    onCreateAssessment: (Long) -> Unit = {},
    onViewAssessments: () -> Unit = {},
    onViewResults: () -> Unit = {},
    onResultClick: (Long) -> Unit = {},
    onPracticeTopic: (subject: String?, chapter: String?) -> Unit = { _, _ -> },
    onAddToTimetable: (subject: String, chapter: String?) -> Unit = { _, _ -> }
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.goal?.name ?: "") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                actions = {
                    if (uiState.goal != null) {
                        IconButton(onClick = { onEditGoal(uiState.goal!!.id) }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        if (uiState.goal == null && !uiState.isLoading) {
            Text(
                "Goal not found",
                Modifier.padding(paddingValues).padding(16.dp),
                style = MaterialTheme.typography.bodyLarge
            )
            return@Scaffold
        }
        val goal = uiState.goal ?: return@Scaffold
        Column(
            Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        stringResource(R.string.days_remaining, uiState.daysRemaining.toInt()),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        formatExamDate(goal.examDate),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    goal.description?.takeIf { it.isNotBlank() }?.let { desc ->
                        Text(
                            desc,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }
            Button(
                onClick = { onCreateAssessment(goal.id) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = stringResource(R.string.cd_create_assessment), modifier = Modifier.padding(end = 8.dp))
                Text(stringResource(R.string.create_assessment))
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onViewAssessments,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.assessments))
                }
                Button(
                    onClick = onViewResults,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.results))
                }
            }
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        "${uiState.questionsPracticed} / ${uiState.totalQuestions} questions practiced",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        "%.0f%% complete".format(uiState.percentComplete),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
            uiState.trackPrediction?.let { prediction ->
                TrackPredictionCard(prediction = prediction)
            }
            if (uiState.suggestedPractice.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(MaterialTheme.colorScheme.tertiaryContainer)
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Text(
                            stringResource(R.string.suggested_practice),
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Text(
                            stringResource(R.string.suggested_practice_hint),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                        uiState.suggestedPractice.forEach { area ->
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                            ) {
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        area.subject,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer
                                    )
                                    Text(
                                        area.chapter ?: stringResource(R.string.filter_all_subjects),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
                                    )
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    IconButton(
                                        onClick = { onAddToTimetable(area.subject, area.chapter) },
                                        modifier = Modifier.padding(0.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.CalendarMonth,
                                            contentDescription = stringResource(R.string.add_to_timetable)
                                        )
                                    }
                                    Button(
                                        onClick = { onPracticeTopic(area.subject, area.chapter) },
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                    ) {
                                        Icon(Icons.Default.MenuBook, contentDescription = stringResource(R.string.cd_revise), modifier = Modifier.padding(end = 4.dp))
                                        Text(stringResource(R.string.revise))
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if (uiState.activityByDay.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface)
                ) {
                    ActivityHeatmap(
                        activityByDay = uiState.activityByDay,
                        weeks = 12,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
            if (uiState.recentAttempts.size >= 2) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface)
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Text(
                            stringResource(R.string.score_trend),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        ScoreSparkline(
                            scores = uiState.recentAttempts.reversed().map { it.percent },
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }
            if (uiState.recentAttempts.isNotEmpty()) {
                Text(
                    "Recent attempts",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                uiState.recentAttempts.forEach { attempt: RecentAttemptSummary ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = { onResultClick(attempt.attemptId) }),
                        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(
                                    attempt.assessmentTitle,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    attempt.attemptLabel,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                "%.0f%%".format(attempt.percent),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
            Text(
                stringResource(R.string.subject),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            uiState.subjectProgress.forEach { progress ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface)
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                progress.subject,
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                "${progress.practiced} / ${progress.total} (%.0f%%)".format(progress.percent),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            progress.chapterLabel,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        LinearProgressIndicator(
                            progress = { (progress.percent / 100f).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                        progress.targetHours?.let { hours ->
                            Text(
                                "Target: $hours h",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TrackPredictionCard(prediction: TrackPrediction) {
    val titleRes = when (prediction.status) {
        TrackStatus.ON_TRACK -> R.string.track_on_track
        TrackStatus.BEHIND -> R.string.track_behind
        TrackStatus.COMPLETE -> R.string.track_complete
        TrackStatus.EXAM_PASSED -> R.string.track_exam_passed
        TrackStatus.NOT_ENOUGH_DATA -> R.string.track_not_enough_data
    }
    val subText = when (prediction.status) {
        TrackStatus.BEHIND -> prediction.projectedPercent?.let { p ->
            prediction.deficitPercent?.let { d ->
                stringResource(R.string.track_behind_message, p, d)
            }
        }
        else -> null
    }
    val icon = when (prediction.status) {
        TrackStatus.ON_TRACK, TrackStatus.COMPLETE, TrackStatus.EXAM_PASSED -> Icons.Default.CheckCircle
        TrackStatus.BEHIND -> Icons.Default.Warning
        TrackStatus.NOT_ENOUGH_DATA -> Icons.Default.Schedule
    }
    val containerColor = when (prediction.status) {
        TrackStatus.ON_TRACK, TrackStatus.COMPLETE -> MaterialTheme.colorScheme.primaryContainer
        TrackStatus.BEHIND -> MaterialTheme.colorScheme.errorContainer
        TrackStatus.EXAM_PASSED, TrackStatus.NOT_ENOUGH_DATA -> MaterialTheme.colorScheme.surfaceVariant
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor)
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = stringResource(
                    when (prediction.status) {
                        TrackStatus.ON_TRACK -> R.string.cd_on_track
                        TrackStatus.BEHIND -> R.string.cd_behind
                        TrackStatus.COMPLETE -> R.string.track_complete
                        TrackStatus.EXAM_PASSED -> R.string.track_exam_passed
                        TrackStatus.NOT_ENOUGH_DATA -> R.string.track_not_enough_data
                    }
                ),
                tint = when (prediction.status) {
                    TrackStatus.ON_TRACK, TrackStatus.COMPLETE -> MaterialTheme.colorScheme.primary
                    TrackStatus.BEHIND -> MaterialTheme.colorScheme.onErrorContainer
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
            Column {
                Text(
                    stringResource(titleRes),
                    style = MaterialTheme.typography.titleMedium,
                    color = when (prediction.status) {
                        TrackStatus.BEHIND -> MaterialTheme.colorScheme.onErrorContainer
                        else -> MaterialTheme.colorScheme.onSurface
                    }
                )
                subText?.let { text ->
                    Text(
                        text,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}
