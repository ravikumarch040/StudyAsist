package com.studyasist.ui.addrevision

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
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
import com.studyasist.ui.components.NumberPicker

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRevisionScreen(
    viewModel: AddRevisionViewModel,
    onBack: () -> Unit,
    onSaved: () -> Unit,
    onOpenTimetables: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.add_revision_block)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        val modifier = Modifier
            .fillMaxWidth()
            .padding(paddingValues)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)

        val activeTimetableId = uiState.activeTimetableId
        if (activeTimetableId == null || activeTimetableId <= 0) {
            Column(
                modifier = modifier,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    stringResource(R.string.no_active_timetable),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Button(onClick = onOpenTimetables) {
                    Text(stringResource(R.string.timetables))
                }
            }
            return@Scaffold
        }

        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(stringResource(R.string.day), style = MaterialTheme.typography.labelMedium)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for ((day, label) in listOf(1 to "Mon", 2 to "Tue", 3 to "Wed", 4 to "Thu", 5 to "Fri", 6 to "Sat", 7 to "Sun")) {
                    androidx.compose.material3.FilterChip(
                        selected = uiState.dayOfWeek == day,
                        onClick = { viewModel.updateDay(day) },
                        label = { Text(label) }
                    )
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

            uiState.errorMessage?.let { msg ->
                Text(
                    msg,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Button(
                onClick = { viewModel.save(onSaved) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isSaving
            ) {
                Text(if (uiState.isSaving) stringResource(R.string.saving) else stringResource(R.string.save))
            }
        }
    }
}
