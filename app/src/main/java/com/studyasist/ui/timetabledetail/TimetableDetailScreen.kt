package com.studyasist.ui.timetabledetail

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
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
import com.studyasist.data.local.entity.ActivityType
import com.studyasist.util.formatTimeMinutes

private const val SLOT_MINUTES = 30

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimetableDetailScreen(
    viewModel: TimetableDetailViewModel,
    onBack: () -> Unit,
    onAddActivity: (Long, Int) -> Unit,
    onEditActivity: (Long, Long) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val timetable = uiState.timetable ?: return
    val timetableId = timetable.id
    var selectedViewTab by remember { mutableIntStateOf(0) }
    val days = listOf(1 to "Mon", 2 to "Tue", 3 to "Wed", 4 to "Thu", 5 to "Fri", 6 to "Sat", 7 to "Sun")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(timetable.name) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                actions = {
                    IconButton(onClick = { onAddActivity(timetableId, uiState.selectedDay) }) {
                        Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_activity))
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
            TabRow(selectedTabIndex = selectedViewTab) {
                Tab(selected = selectedViewTab == 0, onClick = { selectedViewTab = 0 }, text = { Text(stringResource(R.string.day_view)) })
                Tab(selected = selectedViewTab == 1, onClick = { selectedViewTab = 1 }, text = { Text(stringResource(R.string.week_view)) })
            }
            Text(stringResource(R.string.filter), style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
            Row(
                Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                androidx.compose.material3.FilterChip(
                    selected = uiState.filterType == null,
                    onClick = { viewModel.setFilterType(null) },
                    label = { Text(stringResource(R.string.filter_all)) }
                )
                for (type in ActivityType.entries) {
                    androidx.compose.material3.FilterChip(
                        selected = uiState.filterType == type,
                        onClick = { viewModel.setFilterType(type) },
                        label = { Text(type.name) }
                    )
                }
            }
            when (selectedViewTab) {
                0 -> {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        for ((day, label) in days) {
                            val selected = uiState.selectedDay == day
                            androidx.compose.material3.FilterChip(
                                selected = selected,
                                onClick = { viewModel.setSelectedDay(day) },
                                label = { Text(label) }
                            )
                        }
                    }
                    val filtered = if (uiState.filterType == null) uiState.activities else uiState.activities.filter { it.type == uiState.filterType }
                    val dayActivities = filtered.filter { it.dayOfWeek == uiState.selectedDay }
                    if (dayActivities.isEmpty()) {
                        Box(
                            Modifier.fillMaxSize().padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                stringResource(R.string.no_activities_day),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        LazyColumn(
                            Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(dayActivities, key = { it.id }) { activity ->
                                ActivityCard(activity = activity, onClick = { onEditActivity(timetableId, activity.id) })
                            }
                        }
                    }
                }
                1 -> WeekView(
                    activities = if (uiState.filterType == null) uiState.activities else uiState.activities.filter { it.type == uiState.filterType },
                    onActivityClick = { onEditActivity(timetableId, it.id) }
                )
            }
        }
    }
}

@Composable
private fun WeekView(
    activities: List<ActivityEntity>,
    onActivityClick: (ActivityEntity) -> Unit
) {
    if (activities.isEmpty()) {
        Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
            Text(stringResource(R.string.no_activities_week), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }
    val minMinutes = activities.minOf { it.startTimeMinutes }.let { (it / SLOT_MINUTES) * SLOT_MINUTES }
    val maxMinutes = activities.maxOf { it.endTimeMinutes }.let { ((it + SLOT_MINUTES - 1) / SLOT_MINUTES) * SLOT_MINUTES }
    val slotCount = ((maxMinutes - minMinutes) / SLOT_MINUTES).coerceAtLeast(1)
    val dayLabels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(8.dp)) {
        item {
            Row(Modifier.fillMaxWidth()) {
                Box(Modifier.widthIn(min = 48.dp).padding(4.dp)) {}
                dayLabels.forEach { label ->
                    Box(
                        Modifier
                            .weight(1f)
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(label, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
        items(slotCount) { rowIndex ->
            val slotStart = minMinutes + rowIndex * SLOT_MINUTES
            Row(Modifier.fillMaxWidth().height(44.dp)) {
                Box(
                    Modifier
                        .widthIn(min = 48.dp)
                        .padding(4.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        formatTimeMinutes(slotStart),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                for (day in 1..7) {
                    val slotEnd = slotStart + SLOT_MINUTES
                    val cellActivities = activities.filter { it.dayOfWeek == day && it.startTimeMinutes < slotEnd && it.endTimeMinutes > slotStart }
                    Box(
                        Modifier
                            .weight(1f)
                            .padding(2.dp)
                            .fillMaxSize()
                            .then(
                                if (cellActivities.isNotEmpty()) Modifier.clickable { onActivityClick(cellActivities.first()) }
                                else Modifier
                            )
                    ) {
                        if (cellActivities.isNotEmpty()) {
                            Card(
                                modifier = Modifier.fillMaxSize(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                            ) {
                                Box(Modifier.fillMaxSize().padding(4.dp)) {
                                    Text(
                                        cellActivities.first().title,
                                        style = MaterialTheme.typography.labelSmall,
                                        maxLines = 2
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ActivityCard(
    activity: ActivityEntity,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
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
                activity.note?.let { note ->
                    if (note.isNotBlank()) {
                        Text(text = note, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}
