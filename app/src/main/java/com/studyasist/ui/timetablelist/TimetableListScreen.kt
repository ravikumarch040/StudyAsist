package com.studyasist.ui.timetablelist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.studyasist.R
import com.studyasist.data.local.entity.TimetableEntity
import com.studyasist.data.local.entity.WeekType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimetableListScreen(
    viewModel: TimetableListViewModel,
    onTimetableClick: (Long) -> Unit,
    onSettingsClick: () -> Unit,
    onNavigateAfterCreate: (Long) -> Unit,
    activeTimetableId: Long? = null,
    onSetActive: (Long) -> Unit = {},
    showTopBar: Boolean = true
) {
    val uiState by viewModel.uiState.collectAsState()
    var showMenuForId by remember { mutableStateOf<Long?>(null) }

    Scaffold(
        topBar = {
            if (showTopBar) {
                TopAppBar(
                    title = { Text(stringResource(R.string.app_name)) },
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
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.openCreateDialog() },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.new_timetable))
            }
        }
    ) { paddingValues ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.timetables.isEmpty()) {
                Text(
                    stringResource(R.string.no_timetables),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(32.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = uiState.timetables,
                        key = { it.id }
                    ) { timetable ->
                        TimetableCard(
                            timetable = timetable,
                            isActive = timetable.id == activeTimetableId,
                            onClick = { onTimetableClick(timetable.id) },
                            onMenuClick = { showMenuForId = timetable.id },
                            onSetActive = { onSetActive(timetable.id); showMenuForId = null },
                            onDuplicate = {
                                viewModel.duplicateTimetable(
                                    timetable.id,
                                    "${timetable.name} (Copy)"
                                ) { onNavigateAfterCreate(it) }
                                showMenuForId = null
                            },
                            onDelete = {
                                viewModel.deleteTimetable(timetable.id)
                                showMenuForId = null
                            },
                            menuExpanded = showMenuForId == timetable.id,
                            onDismissMenu = { showMenuForId = null }
                        )
                    }
                }
            }
        }
    }

    if (uiState.showCreateDialog) {
        CreateTimetableDialog(
            name = uiState.createName,
            weekType = uiState.createWeekType,
            onNameChange = viewModel::updateCreateName,
            onWeekTypeChange = viewModel::updateCreateWeekType,
            onDismiss = viewModel::dismissCreateDialog,
            onCreate = { viewModel.createTimetable(onNavigateAfterCreate) },
            isCreating = uiState.isCreating
        )
    }
}

@Composable
private fun TimetableCard(
    timetable: TimetableEntity,
    isActive: Boolean,
    onClick: () -> Unit,
    onMenuClick: () -> Unit,
    onSetActive: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit,
    menuExpanded: Boolean,
    onDismissMenu: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            androidx.compose.foundation.layout.Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = timetable.name,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (isActive) {
                            androidx.compose.material3.AssistChip(
                                onClick = {},
                                label = { Text(stringResource(R.string.active)) }
                            )
                        }
                    }
                    Text(
                        text = when (timetable.weekType) {
                            WeekType.MON_SUN -> stringResource(R.string.week_type_mon_sun)
                            WeekType.MON_SAT_PLUS_SUNDAY -> stringResource(R.string.week_type_mon_sat_sunday)
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Box {
                    IconButton(onClick = onMenuClick) {
                        Icon(Icons.Default.MoreVert, contentDescription = stringResource(R.string.options))
                    }
                    androidx.compose.material3.DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = onDismissMenu
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.set_active)) },
                            onClick = onSetActive
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.duplicate)) },
                            onClick = onDuplicate
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error) },
                            onClick = onDelete
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CreateTimetableDialog(
    name: String,
    weekType: WeekType,
    onNameChange: (String) -> Unit,
    onWeekTypeChange: (WeekType) -> Unit,
    onDismiss: () -> Unit,
    onCreate: () -> Unit,
    isCreating: Boolean
) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.new_timetable)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                androidx.compose.material3.OutlinedTextField(
                    value = name,
                    onValueChange = onNameChange,
                    label = { Text(stringResource(R.string.name)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                RadioButtonGroup(
                    selected = weekType,
                    onSelected = onWeekTypeChange,
                    options = listOf(
                        WeekType.MON_SUN to R.string.week_type_mon_sun,
                        WeekType.MON_SAT_PLUS_SUNDAY to R.string.week_type_mon_sat_sunday
                    )
                )
            }
        },
        confirmButton = {
            androidx.compose.material3.TextButton(
                onClick = onCreate,
                enabled = name.isNotBlank() && !isCreating
            ) {
                Text(if (isCreating) stringResource(R.string.creating) else stringResource(R.string.create))
            }
        },
        dismissButton = {
            androidx.compose.material3.TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
private fun RadioButtonGroup(
    selected: WeekType,
    onSelected: (WeekType) -> Unit,
    options: List<Pair<WeekType, Int>>
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(stringResource(R.string.week_type), style = MaterialTheme.typography.labelMedium)
        for ((value, labelResId) in options) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { onSelected(value) }
            ) {
                androidx.compose.material3.RadioButton(
                    selected = selected == value,
                    onClick = { onSelected(value) }
                )
                Text(stringResource(labelResId))
            }
        }
    }
}
