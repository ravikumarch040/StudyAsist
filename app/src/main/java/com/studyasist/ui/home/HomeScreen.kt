package com.studyasist.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.studyasist.R
import com.studyasist.data.local.entity.ActivityEntity
import com.studyasist.ui.components.colorForActivityType
import com.studyasist.ui.timetablelist.TimetableListScreen
import com.studyasist.ui.timetablelist.TimetableListViewModel
import com.studyasist.util.formatTimeMinutes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    listViewModel: TimetableListViewModel,
    onSettingsClick: () -> Unit,
    onTimetableClick: (Long) -> Unit,
    onAddActivity: (Long) -> Unit,
    onNavigateAfterCreate: (Long) -> Unit,
    onDictate: () -> Unit = {},
    onExplain: () -> Unit = {},
    onSolve: () -> Unit = {},
    onExamGoals: () -> Unit = {},
    onQABank: () -> Unit = {},
    onAssessments: () -> Unit = {},
    onResults: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("StudyAsist") },
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.settings))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text(stringResource(R.string.today)) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text(stringResource(R.string.timetables)) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text(stringResource(R.string.study_tools)) }
                )
            }
            LaunchedEffect(selectedTab) {
                if (selectedTab == 2) viewModel.refreshStreak()
            }
            when (selectedTab) {
                0 -> TodayTabContent(
                    activeTimetable = uiState.activeTimetable,
                    todayActivities = uiState.todayActivities,
                    currentActivityId = uiState.currentActivityId,
                    onAddActivity = { uiState.activeTimetable?.id?.let(onAddActivity) }
                )
                1 -> TimetableListScreen(
                    viewModel = listViewModel,
                    onTimetableClick = onTimetableClick,
                    onSettingsClick = onSettingsClick,
                    onNavigateAfterCreate = onNavigateAfterCreate,
                    activeTimetableId = uiState.activeTimetableId,
                    onSetActive = viewModel::setActiveTimetableId,
                    showTopBar = false
                )
                2 -> StudyToolsTabContent(
                    studyStreak = uiState.studyStreak,
                    earnedBadges = uiState.earnedBadges,
                    onDictate = onDictate,
                    onExplain = onExplain,
                    onSolve = onSolve,
                    onExamGoals = onExamGoals,
                    onQABank = onQABank,
                    onAssessments = onAssessments,
                    onResults = onResults
                )
            }
        }
    }
}

@Composable
private fun StudyToolsTabContent(
    studyStreak: Int = 0,
    earnedBadges: List<com.studyasist.data.repository.EarnedBadge> = emptyList(),
    onDictate: () -> Unit,
    onExplain: () -> Unit,
    onSolve: () -> Unit,
    onExamGoals: () -> Unit,
    onQABank: () -> Unit,
    onAssessments: () -> Unit = {},
    onResults: () -> Unit = {}
) {
    LazyColumn(
        Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    stringResource(R.string.study_tools),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (studyStreak > 0) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Default.LocalFireDepartment,
                                contentDescription = stringResource(R.string.cd_streak),
                                tint = MaterialTheme.colorScheme.tertiary
                            )
                            Text(
                                "${studyStreak}${stringResource(R.string.streak_days)}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }
                    }
                    if (earnedBadges.isNotEmpty()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Default.EmojiEvents,
                                contentDescription = stringResource(R.string.cd_badges),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                "${earnedBadges.size} badges",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
        if (earnedBadges.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Text(
                            stringResource(R.string.badges),
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(top = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            earnedBadges.take(6).forEach { badge ->
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        Icons.Default.EmojiEvents,
                                        contentDescription = badge.title,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        badge.title,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        item {
            Card(
                modifier = Modifier.fillMaxWidth().clickable(onClick = onExamGoals),
                colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(stringResource(R.string.exam_goals), style = MaterialTheme.typography.titleSmall)
                    Text(stringResource(R.string.exam_goals_subtitle), style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        item {
            Card(
                modifier = Modifier.fillMaxWidth().clickable(onClick = onQABank),
                colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(stringResource(R.string.qa_bank), style = MaterialTheme.typography.titleSmall)
                    Text(stringResource(R.string.qa_bank_subtitle), style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        item {
            Card(
                modifier = Modifier.fillMaxWidth().clickable(onClick = onAssessments),
                colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(stringResource(R.string.assessments), style = MaterialTheme.typography.titleSmall)
                    Text("Create and take practice tests", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        item {
            Card(
                modifier = Modifier.fillMaxWidth().clickable(onClick = onResults),
                colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(stringResource(R.string.results), style = MaterialTheme.typography.titleSmall)
                    Text("View your attempt history and scores", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        item {
            Card(
                modifier = Modifier.fillMaxWidth().clickable(onClick = onDictate),
                colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(stringResource(R.string.dictate), style = MaterialTheme.typography.titleSmall)
                    Text(stringResource(R.string.dictate_subtitle), style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        item {
            Card(
                modifier = Modifier.fillMaxWidth().clickable(onClick = onExplain),
                colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(stringResource(R.string.explain), style = MaterialTheme.typography.titleSmall)
                    Text(stringResource(R.string.explain_subtitle), style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        item {
            Card(
                modifier = Modifier.fillMaxWidth().clickable(onClick = onSolve),
                colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(stringResource(R.string.solve), style = MaterialTheme.typography.titleSmall)
                    Text(stringResource(R.string.solve_subtitle), style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
private fun TodayTabContent(
    activeTimetable: com.studyasist.data.local.entity.TimetableEntity?,
    todayActivities: List<ActivityEntity>,
    currentActivityId: Long?,
    onAddActivity: () -> Unit
) {
    if (activeTimetable == null) {
        Box(
            Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                stringResource(R.string.no_active_timetable),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }
    if (todayActivities.isEmpty()) {
        Box(
            Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                stringResource(R.string.no_activities_today) + " (${activeTimetable.name})",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(todayActivities, key = { it.id }) { activity ->
            val isCurrent = activity.id == currentActivityId
            val (typeContainerColor, typeContentColor) = MaterialTheme.colorScheme.colorForActivityType(activity.type)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (isCurrent) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        typeContainerColor
                    }
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = if (isCurrent) 4.dp else 1.dp
                )
            ) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    val textColor = if (isCurrent) MaterialTheme.colorScheme.onPrimaryContainer else typeContentColor
                    if (isCurrent) {
                        Text(
                            stringResource(R.string.now),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Text(
                        text = activity.title,
                        style = MaterialTheme.typography.titleSmall,
                        color = textColor
                    )
                    Text(
                        text = "${formatTimeMinutes(activity.startTimeMinutes)} – ${formatTimeMinutes(activity.endTimeMinutes)} · ${activity.type.name}",
                        style = MaterialTheme.typography.bodySmall,
                        color = textColor.copy(alpha = 0.85f)
                    )
                }
            }
        }
    }
}
