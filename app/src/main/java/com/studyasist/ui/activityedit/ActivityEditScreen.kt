package com.studyasist.ui.activityedit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import com.studyasist.data.local.entity.ActivityType
import com.studyasist.data.repository.AppSettings
import com.studyasist.ui.components.colorForActivityType
import com.studyasist.ui.components.NumberPicker

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityEditScreen(
    viewModel: ActivityEditViewModel,
    onBack: () -> Unit,
    onSaved: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (uiState.isEdit) stringResource(R.string.edit_activity) else stringResource(R.string.add_activity)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
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
            modifier = Modifier
                .fillMaxWidth()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            uiState.overlapBlockedMessage?.let { msg ->
                Text(
                    msg,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
            Text(stringResource(R.string.day), style = MaterialTheme.typography.labelMedium)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for ((day, labelResId) in listOf(1 to R.string.day_mon, 2 to R.string.day_tue, 3 to R.string.day_wed, 4 to R.string.day_thu, 5 to R.string.day_fri, 6 to R.string.day_sat, 7 to R.string.day_sun)) {
                    androidx.compose.material3.FilterChip(
                        selected = uiState.dayOfWeek == day,
                        onClick = { viewModel.updateDay(day) },
                        label = { Text(stringResource(labelResId)) }
                    )
                }
            }

            if (!uiState.isEdit) {
                Text(stringResource(R.string.copy_from_day), style = MaterialTheme.typography.labelMedium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for ((day, labelResId) in listOf(1 to R.string.day_mon, 2 to R.string.day_tue, 3 to R.string.day_wed, 4 to R.string.day_thu, 5 to R.string.day_fri, 6 to R.string.day_sat, 7 to R.string.day_sun)) {
                        if (day != uiState.dayOfWeek) {
                            androidx.compose.material3.FilterChip(
                                selected = false,
                                onClick = { viewModel.copyScheduleFromDay(day, onDone = onSaved) },
                                label = { Text(stringResource(R.string.from_day, stringResource(labelResId))) }
                            )
                        }
                    }
                }
            }

            Text(stringResource(R.string.start_time), style = MaterialTheme.typography.labelMedium)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                    Text(stringResource(R.string.hour), style = MaterialTheme.typography.labelSmall)
                    NumberPicker(
                        value = uiState.startHour.coerceIn(0, 23),
                        onValueChange = { viewModel.updateStartTime(it, uiState.startMinute) },
                        range = 0..23
                    )
                }
                Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                    Text(stringResource(R.string.min_label), style = MaterialTheme.typography.labelSmall)
                    NumberPicker(
                        value = uiState.startMinute.coerceIn(0, 59),
                        onValueChange = { viewModel.updateStartTime(uiState.startHour, it) },
                        range = 0..59
                    )
                }
            }
            Text(stringResource(R.string.end_time), style = MaterialTheme.typography.labelMedium)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                    Text(stringResource(R.string.hour), style = MaterialTheme.typography.labelSmall)
                    NumberPicker(
                        value = uiState.endHour.coerceIn(0, 23),
                        onValueChange = { viewModel.updateEndTime(it, uiState.endMinute) },
                        range = 0..23
                    )
                }
                Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                    Text(stringResource(R.string.min_label), style = MaterialTheme.typography.labelSmall)
                    NumberPicker(
                        value = uiState.endMinute.coerceIn(0, 59),
                        onValueChange = { viewModel.updateEndTime(uiState.endHour, it) },
                        range = 0..59
                    )
                }
            }

            OutlinedTextField(
                value = uiState.title,
                onValueChange = viewModel::updateTitle,
                label = { Text(stringResource(R.string.title)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Text(stringResource(R.string.type), style = MaterialTheme.typography.labelMedium)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (type in ActivityType.entries) {
                    val (containerColor, contentColor) = MaterialTheme.colorScheme.colorForActivityType(type)
                    androidx.compose.material3.FilterChip(
                        selected = uiState.type == type,
                        onClick = { viewModel.updateType(type) },
                        label = { Text(type.name) },
                        colors = androidx.compose.material3.FilterChipDefaults.filterChipColors(
                            selectedContainerColor = containerColor,
                            selectedLabelColor = contentColor
                        )
                    )
                }
            }

            OutlinedTextField(
                value = uiState.note,
                onValueChange = viewModel::updateNote,
                label = { Text(stringResource(R.string.note_optional)) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 1,
                maxLines = 2
            )

            androidx.compose.material3.HorizontalDivider()
            SwitchRow(
                checked = uiState.notifyEnabled,
                onCheckedChange = viewModel::updateNotifyEnabled,
                label = stringResource(R.string.notify_me)
            )
            if (uiState.notifyEnabled) {
                Text(stringResource(R.string.notify_before_minutes), style = MaterialTheme.typography.labelMedium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (mins in AppSettings.LEAD_OPTIONS) {
                        androidx.compose.material3.FilterChip(
                            selected = uiState.notifyLeadMinutes == mins,
                            onClick = { viewModel.updateNotifyLeadMinutes(mins) },
                            label = { Text(stringResource(R.string.mins_min, mins)) }
                        )
                    }
                }
                SwitchRow(
                    checked = uiState.useSpeechSound,
                    onCheckedChange = viewModel::updateUseSpeechSound,
                    label = stringResource(R.string.use_speech_sound)
                )
                if (uiState.useSpeechSound) {
                    OutlinedTextField(
                        value = uiState.alarmTtsMessage,
                        onValueChange = viewModel::updateAlarmTtsMessage,
                        label = { Text(stringResource(R.string.alarm_message_label)) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text(stringResource(R.string.alarm_message_placeholder)) },
                        singleLine = false,
                        maxLines = 3
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                androidx.compose.material3.OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.cancel))
                }
                androidx.compose.material3.Button(
                    onClick = { viewModel.save(onSaved = onSaved) },
                    modifier = Modifier.weight(1f),
                    enabled = uiState.title.isNotBlank() && !uiState.isSaving
                ) {
                    Text(if (uiState.isSaving) stringResource(R.string.saving) else stringResource(R.string.save))
                }
            }
        }
    }

    if (uiState.showOverlapDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = viewModel::dismissOverlapDialog,
            title = { Text(stringResource(R.string.overlapping_activities)) },
            text = {
                Text(stringResource(R.string.overlap_warning))
            },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = { viewModel.save(saveAnyway = true, onSaved = onSaved) }) {
                    Text(stringResource(R.string.save_anyway))
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = viewModel::dismissOverlapDialog) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
private fun SwitchRow(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    label: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
        Text(label)
        androidx.compose.material3.Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
