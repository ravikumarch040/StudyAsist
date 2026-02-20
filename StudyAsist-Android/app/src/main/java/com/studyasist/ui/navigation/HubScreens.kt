package com.studyasist.ui.navigation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.EmojiObjects
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.studyasist.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyHubScreen(
    onDictate: () -> Unit,
    onExplain: () -> Unit,
    onSolve: () -> Unit,
    onPomodoro: () -> Unit,
    onTutor: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.nav_study)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 16.dp)
        ) {
            item {
                Text(
                    stringResource(R.string.study_tools),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            item { HubMenuItem(Icons.Default.Mic, stringResource(R.string.dictate), stringResource(R.string.dictate_subtitle), onDictate) }
            item { HubMenuItem(Icons.Default.AutoStories, stringResource(R.string.explain), stringResource(R.string.explain_subtitle), onExplain) }
            item { HubMenuItem(Icons.Default.EmojiObjects, stringResource(R.string.solve), stringResource(R.string.solve_subtitle), onSolve) }
            item { HubMenuItem(Icons.Default.Timer, stringResource(R.string.pomodoro), stringResource(R.string.pomodoro_focus), onPomodoro) }
            item { HubMenuItem(Icons.Default.Chat, stringResource(R.string.ai_tutor), stringResource(R.string.ai_tutor_hint), onTutor) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalsHubScreen(
    onGoals: () -> Unit,
    onQABank: () -> Unit,
    onAssessments: () -> Unit,
    onResults: () -> Unit,
    onDailyReview: () -> Unit,
    onFlashcards: () -> Unit,
    onManualReview: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.nav_goals)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 16.dp)
        ) {
            item { HubMenuItem(Icons.Default.TrackChanges, stringResource(R.string.goals), stringResource(R.string.exam_goals_subtitle), onGoals) }
            item { HubMenuItem(Icons.Default.Quiz, stringResource(R.string.qa_bank), stringResource(R.string.qa_bank_subtitle), onQABank) }
            item { HubMenuItem(Icons.Default.Assessment, stringResource(R.string.assessments), stringResource(R.string.create_practice_tests), onAssessments) }
            item { HubMenuItem(Icons.Default.Star, stringResource(R.string.results), stringResource(R.string.view_attempt_history), onResults) }
            item { HubMenuItem(Icons.Default.Style, stringResource(R.string.daily_review), stringResource(R.string.flashcards), onDailyReview) }
            item { HubMenuItem(Icons.Default.Style, stringResource(R.string.flashcards), stringResource(R.string.quick_review), onFlashcards) }
            item { HubMenuItem(Icons.Default.Star, stringResource(R.string.manual_review), stringResource(R.string.manual_review_list), onManualReview) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreHubScreen(
    onSettings: () -> Unit,
    onLeaderboard: () -> Unit,
    onOnlineResources: () -> Unit = {},
    onDownloadedDocs: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.nav_more)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 16.dp)
        ) {
            item { HubMenuItem(Icons.Default.EmojiEvents, stringResource(R.string.leaderboard), stringResource(R.string.leaderboard_subtitle), onLeaderboard) }
            item { HubMenuItem(Icons.Default.Download, stringResource(R.string.online_resources), stringResource(R.string.online_resources_hint), onOnlineResources) }
            item { HubMenuItem(Icons.Default.FolderOpen, stringResource(R.string.downloaded_docs), stringResource(R.string.downloaded_docs_hint), onDownloadedDocs) }
            item { HubMenuItem(Icons.Default.Settings, stringResource(R.string.settings), "", onSettings) }
        }
    }
}

@Composable
private fun HubMenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(28.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                if (subtitle.isNotBlank()) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaceholderScreen(title: String, onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
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
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Coming Soon",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
