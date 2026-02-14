package com.studyasist.ui.settings

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.studyasist.R
import com.studyasist.data.repository.AppSettings
import com.studyasist.notification.FOCUS_GUARD_RESTRICTED_PACKAGES
import com.studyasist.notification.openUsageAccessSettings
import com.studyasist.util.VoiceOption
import com.studyasist.util.formatRelativeTimeAgo
import com.studyasist.util.loadAvailableVoicesIndia
import java.io.InputStreamReader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit
) {
    val settings by viewModel.settings.collectAsState(
        initial = AppSettings(AppSettings.DEFAULT_LEAD_MINUTES, true, "", null, "", false, false, null, false)
    )
    val apiKeyTestMessage by viewModel.apiKeyTestMessage.collectAsState(initial = null)
    val backupExportJson by viewModel.backupExportJson.collectAsState(initial = null)
    val backupImportResult by viewModel.backupImportResult.collectAsState(initial = null)
    val cloudBackupResult by viewModel.cloudBackupResult.collectAsState(initial = null)
    val cloudBackupLastSuccess by viewModel.cloudBackupLastSuccess.collectAsState(initial = null)
    val cloudBackupFiles by viewModel.cloudBackupFiles.collectAsState(initial = emptyList())
    val cloudBackupFilesLoading by viewModel.cloudBackupFilesLoading.collectAsState(initial = false)
    val darkMode by viewModel.darkMode.collectAsState(initial = "system")
    var showRestoreFromFolderDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val backupFolderLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri: Uri? ->
        uri?.let { u ->
            try {
                val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                context.contentResolver.takePersistableUriPermission(u, takeFlags)
                viewModel.setCloudBackupFolder(u)
            } catch (e: Exception) {
                viewModel.setCloudBackupFolder(null)
            }
        }
    }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        uri?.let { u ->
            backupExportJson?.let { json ->
                context.contentResolver.openOutputStream(u)?.use { it.write(json.toByteArray()) }
            }
        }
        viewModel.clearBackupExport()
    }
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let { u ->
            context.contentResolver.openInputStream(u)?.use { input ->
                val json = InputStreamReader(input).readText()
                viewModel.importBackup(json)
            }
        }
    }

    LaunchedEffect(backupExportJson) {
        backupExportJson?.let { json ->
            exportLauncher.launch("studyasist_backup_${System.currentTimeMillis()}.json")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
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
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(stringResource(R.string.appearance), style = MaterialTheme.typography.titleMedium)
            Text(stringResource(R.string.theme), style = MaterialTheme.typography.bodySmall)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("system" to R.string.theme_system, "light" to R.string.theme_light, "dark" to R.string.theme_dark).forEach { (mode, labelRes) ->
                    androidx.compose.material3.FilterChip(
                        selected = darkMode == mode,
                        onClick = { viewModel.setDarkMode(mode) },
                        label = { Text(stringResource(labelRes)) }
                    )
                }
            }
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
                Text("Vibration")
                Switch(
                    checked = settings.vibrationEnabled,
                    onCheckedChange = viewModel::setVibrationEnabled
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(stringResource(R.string.block_overlap))
                Switch(
                    checked = settings.blockOverlap,
                    onCheckedChange = viewModel::setBlockOverlap
                )
            }
            Text(stringResource(R.string.focus_guard), style = MaterialTheme.typography.titleMedium)
            Text(
                stringResource(R.string.focus_guard_summary),
                style = MaterialTheme.typography.bodySmall
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(stringResource(R.string.focus_guard)) // label for switch
                Switch(
                    checked = settings.focusGuardEnabled,
                    onCheckedChange = { enabled ->
                        viewModel.setFocusGuardEnabled(enabled)
                        if (enabled) {
                            openUsageAccessSettings(context)
                        }
                    }
                )
            }
            if (settings.focusGuardEnabled) {
                Text(
                    stringResource(R.string.focus_guard_usage_hint),
                    style = MaterialTheme.typography.bodySmall
                )
                var showBuiltIn by remember { mutableStateOf(false) }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        stringResource(R.string.focus_guard_built_in, FOCUS_GUARD_RESTRICTED_PACKAGES.size),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    androidx.compose.material3.TextButton(
                        onClick = { showBuiltIn = !showBuiltIn }
                    ) {
                        Text(if (showBuiltIn) "Hide" else "View")
                    }
                }
                AnimatedVisibility(
                    visible = showBuiltIn,
                    enter = expandVertically(),
                    exit = shrinkVertically()
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        FOCUS_GUARD_RESTRICTED_PACKAGES.sorted().forEach { pkg ->
                            Text(
                                pkg,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                val customPackages by viewModel.focusGuardRestrictedExtra.collectAsState()
                var addPkgInput by remember { mutableStateOf("") }
                Text(
                    stringResource(R.string.focus_guard_custom_apps),
                    style = MaterialTheme.typography.bodySmall
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = addPkgInput,
                        onValueChange = { addPkgInput = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text(stringResource(R.string.focus_guard_add_app_hint)) },
                        singleLine = true
                    )
                    Button(
                        onClick = {
                            if (addPkgInput.isNotBlank()) {
                                viewModel.addFocusGuardPackage(addPkgInput.trim())
                                addPkgInput = ""
                            }
                        },
                        enabled = addPkgInput.isNotBlank()
                    ) {
                        Text("Add")
                    }
                }
                if (customPackages.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        customPackages.forEach { pkg ->
                            AssistChip(
                                onClick = { viewModel.removeFocusGuardPackage(pkg) },
                                label = { Text(pkg, maxLines = 1) },
                                trailingIcon = {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = stringResource(R.string.delete),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            )
                        }
                    }
                }
            }
            Text("Your name", style = MaterialTheme.typography.titleMedium)
            Text(
                "Used in alarm speech: \"Hey {name}, Its time for {activity}\". Optional.",
                style = MaterialTheme.typography.bodySmall
            )
            OutlinedTextField(
                value = settings.userName,
                onValueChange = viewModel::setUserName,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Your name") },
                singleLine = true
            )
            Text("Speech (India voices)", style = MaterialTheme.typography.titleMedium)
            Text(
                "Voice for alarms and reading aloud. Only India voices are listed.",
                style = MaterialTheme.typography.bodySmall
            )
            VoiceDropdown(
                selectedVoiceName = settings.ttsVoiceName,
                onVoiceSelected = viewModel::setTtsVoiceName
            )
            Text("AI (Explain / Solve)", style = MaterialTheme.typography.titleMedium)
            Text(
                "Gemini API key from Google AI Studio. Required for Explain and Solve.",
                style = MaterialTheme.typography.bodySmall
            )
            OutlinedTextField(
                value = settings.geminiApiKey,
                onValueChange = viewModel::setGeminiApiKey,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("API key") },
                singleLine = true
            )
            androidx.compose.material3.Button(
                onClick = { viewModel.testApiKey() },
                modifier = Modifier.fillMaxWidth(),
                enabled = settings.geminiApiKey.isNotBlank()
            ) {
                Text("Test API key")
            }
            apiKeyTestMessage?.let { msg ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Text(
                        msg,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (msg.startsWith("OK") || msg.contains("success")) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                        maxLines = Int.MAX_VALUE
                    )
                }
            }
            Text(stringResource(R.string.cloud_backup), style = MaterialTheme.typography.titleMedium)
            Text(stringResource(R.string.cloud_backup_summary), style = MaterialTheme.typography.bodySmall)
            Text(stringResource(R.string.backup_folder_hint), style = MaterialTheme.typography.bodySmall)
            Button(
                onClick = { backupFolderLauncher.launch(null) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.set_backup_folder))
            }
            if (!settings.cloudBackupFolderUri.isNullOrBlank()) {
                Text(
                    "Folder set",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            cloudBackupLastSuccess?.let { millis ->
                Text(
                    stringResource(R.string.cloud_backup_last, formatRelativeTimeAgo(millis)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Button(
                onClick = { viewModel.backupToCloud() },
                modifier = Modifier.fillMaxWidth(),
                enabled = !settings.cloudBackupFolderUri.isNullOrBlank()
            ) {
                Text(stringResource(R.string.backup_to_cloud))
            }
            Button(
                onClick = {
                    viewModel.loadCloudBackupFiles()
                    showRestoreFromFolderDialog = true
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !settings.cloudBackupFolderUri.isNullOrBlank()
            ) {
                Text(stringResource(R.string.restore_from_backup_folder))
            }
            if (showRestoreFromFolderDialog) {
                AlertDialog(
                    onDismissRequest = {
                        showRestoreFromFolderDialog = false
                        viewModel.clearCloudBackupFiles()
                    },
                    title = { Text(stringResource(R.string.restore_from_folder_hint)) },
                    text = {
                        if (cloudBackupFilesLoading) {
                            Text("Loading…")
                        } else if (cloudBackupFiles.isEmpty()) {
                            Text(stringResource(R.string.no_backups_in_folder))
                        } else {
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                items(cloudBackupFiles, key = { it.second.toString() }) { (name, uri) ->
                                    Text(
                                        text = name,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                viewModel.restoreFromCloudBackup(uri)
                                                showRestoreFromFolderDialog = false
                                                viewModel.clearCloudBackupFiles()
                                            }
                                            .padding(vertical = 8.dp),
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                            }
                        }
                    },
                    confirmButton = {
                        Button(onClick = {
                            showRestoreFromFolderDialog = false
                            viewModel.clearCloudBackupFiles()
                        }) {
                            Text(stringResource(R.string.cancel))
                        }
                    }
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(stringResource(R.string.auto_daily_backup))
                Switch(
                    checked = settings.cloudBackupAuto,
                    onCheckedChange = viewModel::setCloudBackupAuto
                )
            }
            cloudBackupResult?.let { msg ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Text(
                        msg,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (msg.startsWith("Backup started")) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                        maxLines = Int.MAX_VALUE
                    )
                }
            }
            Spacer(modifier = Modifier.padding(8.dp))
            Text(stringResource(R.string.backup_restore), style = MaterialTheme.typography.titleMedium)
            Text(stringResource(R.string.backup_export_hint), style = MaterialTheme.typography.bodySmall)
            Button(
                onClick = { viewModel.exportBackup() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.backup))
            }
            Text(stringResource(R.string.restore_import_hint), style = MaterialTheme.typography.bodySmall)
            Button(
                onClick = { importLauncher.launch(arrayOf("application/json", "*/*")) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.restore))
            }
            backupImportResult?.let { msg ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Text(
                        msg,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (msg.startsWith("Restore successful")) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                        maxLines = Int.MAX_VALUE
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VoiceDropdown(
    selectedVoiceName: String?,
    onVoiceSelected: (String?) -> Unit
) {
    val context = LocalContext.current
    var voices by remember { mutableStateOf<List<VoiceOption>>(emptyList()) }
    var expanded by remember { mutableStateOf(false) }
    var selectedLabel by remember(selectedVoiceName) {
        mutableStateOf(
            if (selectedVoiceName.isNullOrEmpty()) "System default"
            else voices.find { it.voiceName == selectedVoiceName }?.displayName ?: "System default"
        )
    }
    LaunchedEffect(Unit) {
        voices = withContext(Dispatchers.Main.immediate) { loadAvailableVoicesIndia(context) }
        selectedLabel = if (selectedVoiceName.isNullOrEmpty()) "System default"
        else voices.find { it.voiceName == selectedVoiceName }?.displayName ?: "System default"
    }
    LaunchedEffect(selectedVoiceName, voices) {
        selectedLabel = if (selectedVoiceName.isNullOrEmpty()) "System default"
        else voices.find { it.voiceName == selectedVoiceName }?.displayName ?: "System default"
    }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = selectedLabel,
            onValueChange = {},
            readOnly = true,
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("System default") },
                onClick = {
                    onVoiceSelected(null)
                    selectedLabel = "System default"
                    expanded = false
                }
            )
            voices.forEach { opt ->
                DropdownMenuItem(
                    text = { Text(opt.displayName) },
                    onClick = {
                        onVoiceSelected(opt.voiceName)
                        selectedLabel = opt.displayName
                        expanded = false
                    }
                )
            }
        }
    }
}
