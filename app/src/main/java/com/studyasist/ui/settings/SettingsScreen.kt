package com.studyasist.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.material3.OutlinedTextField
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.studyasist.data.repository.AppSettings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit
) {
    val settings by viewModel.settings.collectAsState(
        initial = AppSettings(AppSettings.DEFAULT_LEAD_MINUTES, true, true, "")
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Notifications", style = MaterialTheme.typography.titleMedium)
            Text(
                "Default reminder (minutes before activity)",
                style = MaterialTheme.typography.bodySmall
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AppSettings.LEAD_OPTIONS.forEach { mins ->
                    androidx.compose.material3.FilterChip(
                        selected = settings.defaultLeadMinutes == mins,
                        onClick = { viewModel.setDefaultLeadMinutes(mins) },
                        label = { Text("$mins min") }
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Sound")
                Switch(
                    checked = settings.soundEnabled,
                    onCheckedChange = viewModel::setSoundEnabled
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Vibration")
                Switch(
                    checked = settings.vibrationEnabled,
                    onCheckedChange = viewModel::setVibrationEnabled
                )
            }
            Text("Alarm sound (text-to-speech)", style = MaterialTheme.typography.titleMedium)
            Text(
                "Custom message spoken when a reminder fires. Leave empty to use system alarm sound.",
                style = MaterialTheme.typography.bodySmall
            )
            OutlinedTextField(
                value = settings.alarmTtsMessage,
                onValueChange = viewModel::setAlarmTtsMessage,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("e.g. Wake up buddy. Its study time.") },
                singleLine = false,
                maxLines = 3
            )
        }
    }
}
