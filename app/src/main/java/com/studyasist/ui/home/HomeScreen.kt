package com.studyasist.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
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
    onNavigateAfterCreate: (Long) -> Unit
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
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (isCurrent) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surface
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
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${formatTimeMinutes(activity.startTimeMinutes)} – ${formatTimeMinutes(activity.endTimeMinutes)} · ${activity.type.name}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
