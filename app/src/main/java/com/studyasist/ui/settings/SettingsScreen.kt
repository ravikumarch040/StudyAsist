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
import androidx.compose.material.icons.filled.AutoStories
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
import com.studyasist.ui.components.ProfileAvatar
import com.studyasist.ui.theme.AppTheme
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import com.studyasist.notification.openUsageAccessSettings
import com.studyasist.util.VoiceOption
import com.studyasist.util.formatRelativeTimeAgo
import com.studyasist.util.loadAvailableVoicesIndia
import java.io.InputStreamReader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Suppress("DEPRECATION")
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit,
    onUserGuide: () -> Unit = {}
) {
    val settings by viewModel.settings.collectAsState(
        initial = AppSettings(AppSettings.DEFAULT_LEAD_MINUTES, true, "", null, "", false, false, null, "folder", false, true, true, "system", AppSettings.DEFAULT_EXAM_ALERT_DAYS, AppSettings.DEFAULT_EXAM_ALERT_PERCENT)
    )
    val apiKeyTestMessage by viewModel.apiKeyTestMessage.collectAsState(initial = null)
    val backupImportResult by viewModel.backupImportResult.collectAsState(initial = null)
    val cloudBackupResult by viewModel.cloudBackupResult.collectAsState(initial = null)
    val cloudBackupLastSuccess by viewModel.cloudBackupLastSuccess.collectAsState(initial = null)
    val cloudBackupFiles by viewModel.cloudBackupFiles.collectAsState(initial = emptyList())
    val cloudBackupFilesLoading by viewModel.cloudBackupFilesLoading.collectAsState(initial = false)
    val darkMode by viewModel.darkMode.collectAsState(initial = "system")
    val appLocale by viewModel.appLocale.collectAsState(initial = "system")
    val themeId by viewModel.themeId.collectAsState(initial = "MINIMAL_LIGHT")
    val profilePicUri by viewModel.profilePicUri.collectAsState(initial = null)
    var showRestoreFromFolderDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val driveSignedIn by viewModel.driveSignedIn.collectAsState(initial = false)

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

    val exportRequest by viewModel.exportRequest.collectAsState(initial = null)
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        uri?.let { u ->
            exportRequest?.let { (json, _) ->
                context.contentResolver.openOutputStream(u)?.use { it.write(json.toByteArray()) }
            }
        }
        viewModel.clearBackupExport()
    }
    LaunchedEffect(Unit) { viewModel.refreshDriveSignInState() }
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            viewModel.refreshDriveSignInState()
            viewModel.loadCloudBackupFiles()
        }
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

    val importExamDataLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let { u ->
            context.contentResolver.openInputStream(u)?.use { input ->
                val json = InputStreamReader(input).readText()
                viewModel.importExamData(json)
            }
        }
    }

    LaunchedEffect(exportRequest) {
        exportRequest?.let { (_, filename) ->
            exportLauncher.launch(filename)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings)) },
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
            // Profile section
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ProfileAvatar(
                    userName = settings.userName,
                    profilePicUri = profilePicUri,
                    size = 56.dp,
                    editable = true,
                    onPhotoSelected = { viewModel.setProfilePicUri(it) }
                )
                Column(Modifier.weight(1f)) {
                    Text(stringResource(R.string.profile), style = MaterialTheme.typography.titleSmall)
                    Text(
                        settings.userName.ifBlank { stringResource(R.string.profile_guest) },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Text(stringResource(R.string.user_guide), style = MaterialTheme.typography.titleMedium)
            androidx.compose.material3.Card(
                onClick = onUserGuide,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        stringResource(R.string.user_guide),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        Icons.Filled.AutoStories,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Text(
                stringResource(R.string.user_guide_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(stringResource(R.string.appearance), style = MaterialTheme.typography.titleMedium)

            // Language
            Text(stringResource(R.string.language), style = MaterialTheme.typography.bodySmall)
            Text(
                stringResource(R.string.language_app_summary),
                style = MaterialTheme.typography.bodySmall
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("system" to R.string.language_system, "en" to R.string.lang_english, "hi" to R.string.lang_hindi, "es" to R.string.lang_spanish, "fr" to R.string.lang_french, "de" to R.string.lang_german).forEach { (tag, labelRes) ->
                    FilterChip(
                        selected = appLocale == tag,
                        onClick = { viewModel.setAppLocale(tag) },
                        label = { Text(stringResource(labelRes)) }
                    )
                }
            }

            // Dark mode toggle
            Text(stringResource(R.string.dark_mode_label), style = MaterialTheme.typography.bodySmall)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("system" to R.string.theme_system, "light" to R.string.theme_light, "dark" to R.string.theme_dark).forEach { (mode, labelRes) ->
                    FilterChip(
                        selected = darkMode == mode,
                        onClick = { viewModel.setDarkMode(mode) },
                        label = { Text(stringResource(labelRes)) }
                    )
                }
            }

            // Theme picker grid
            Text(stringResource(R.string.theme), style = MaterialTheme.typography.bodySmall)
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(AppTheme.entries.toList()) { theme ->
                    val isSelected = theme.name == themeId
                    val preview = theme.previewColors
                    Column(
                        modifier = Modifier
                            .clickable { viewModel.setThemeId(theme.name) }
                            .then(
                                if (isSelected) Modifier.border(
                                    2.dp,
                                    MaterialTheme.colorScheme.primary,
                                    RoundedCornerShape(12.dp)
                                ) else Modifier
                            )
                            .padding(4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .height(32.dp)
                        ) {
                            Box(
                                Modifier
                                    .weight(1f)
                                    .height(32.dp)
                                    .background(preview.primary, RoundedCornerShape(topStart = 8.dp))
                            )
                            Box(
                                Modifier
                                    .weight(1f)
                                    .height(32.dp)
                                    .background(preview.surface)
                            )
                            Box(
                                Modifier
                                    .weight(1f)
                                    .height(32.dp)
                                    .background(preview.accent, RoundedCornerShape(topEnd = 8.dp))
                            )
                        }
                        Text(
                            stringResource(theme.displayNameRes),
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(top = 4.dp),
                            maxLines = 1
                        )
                    }
                }
            }
            Text(stringResource(R.string.notifications), style = MaterialTheme.typography.titleMedium)
            Text(
                stringResource(R.string.default_reminder_minutes),
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
                        label = { Text(stringResource(R.string.mins_min, mins)) }
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(stringResource(R.string.vibration))
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
            Text(stringResource(R.string.exam_goal_alert_settings), style = MaterialTheme.typography.titleMedium)
            Text(
                stringResource(R.string.exam_goal_alert_days_hint),
                style = MaterialTheme.typography.bodySmall
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AppSettings.EXAM_ALERT_DAYS_OPTIONS.forEach { days ->
                    androidx.compose.material3.FilterChip(
                        selected = settings.examGoalAlertDaysThreshold == days,
                        onClick = { viewModel.setExamGoalAlertDaysThreshold(days) },
                        label = { Text(if (days == 1) "1 day" else "$days days") }
                    )
                }
            }
            Text(
                stringResource(R.string.exam_goal_alert_percent_hint),
                style = MaterialTheme.typography.bodySmall
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AppSettings.EXAM_ALERT_PERCENT_OPTIONS.forEach { percent ->
                    androidx.compose.material3.FilterChip(
                        selected = settings.examGoalAlertPercentThreshold == percent,
                        onClick = { viewModel.setExamGoalAlertPercentThreshold(percent) },
                        label = { Text("$percent%") }
                    )
                }
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
                        Text(if (showBuiltIn) stringResource(R.string.hide) else stringResource(R.string.view))
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
                        Text(stringResource(R.string.cd_add))
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
            Text(stringResource(R.string.your_name), style = MaterialTheme.typography.titleMedium)
            Text(
                stringResource(R.string.your_name_hint),
                style = MaterialTheme.typography.bodySmall
            )
            OutlinedTextField(
                value = settings.userName,
                onValueChange = viewModel::setUserName,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(stringResource(R.string.your_name)) },
                singleLine = true
            )
            Text(stringResource(R.string.speech_india_voices), style = MaterialTheme.typography.titleMedium)
            Text(
                stringResource(R.string.voice_for_alarms_hint),
                style = MaterialTheme.typography.bodySmall
            )
            VoiceDropdown(
                selectedVoiceName = settings.ttsVoiceName,
                onVoiceSelected = viewModel::setTtsVoiceName
            )
            Text(stringResource(R.string.ai_explain_solve), style = MaterialTheme.typography.titleMedium)
            Text(
                stringResource(R.string.gemini_api_key_hint_long),
                style = MaterialTheme.typography.bodySmall
            )
            OutlinedTextField(
                value = settings.geminiApiKey,
                onValueChange = viewModel::setGeminiApiKey,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(stringResource(R.string.api_key)) },
                singleLine = true
            )
            androidx.compose.material3.Button(
                onClick = { viewModel.testApiKey() },
                modifier = Modifier.fillMaxWidth(),
                enabled = settings.geminiApiKey.isNotBlank()
            ) {
                Text(stringResource(R.string.test_api_key))
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
                        color = if (msg == stringResource(R.string.ok_api_key_works)) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                        maxLines = Int.MAX_VALUE
                    )
                }
            }
            Text(stringResource(R.string.ai_privacy), style = MaterialTheme.typography.titleMedium)
            Text(stringResource(R.string.ai_privacy_hint), style = MaterialTheme.typography.bodySmall)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(stringResource(R.string.use_cloud_for_parsing), modifier = Modifier.weight(1f))
                Switch(
                    checked = settings.useCloudForParsing,
                    onCheckedChange = viewModel::setUseCloudForParsing
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(stringResource(R.string.use_cloud_for_grading), modifier = Modifier.weight(1f))
                Switch(
                    checked = settings.useCloudForGrading,
                    onCheckedChange = viewModel::setUseCloudForGrading
                )
            }
            Text(stringResource(R.string.cloud_backup), style = MaterialTheme.typography.titleMedium)
            Text(stringResource(R.string.cloud_backup_summary), style = MaterialTheme.typography.bodySmall)
            Text(stringResource(R.string.cloud_backup_target), style = MaterialTheme.typography.bodySmall)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("folder" to R.string.cloud_backup_target_folder, "google_drive" to R.string.cloud_backup_target_google_drive).forEach { (target, labelRes) ->
                    androidx.compose.material3.FilterChip(
                        selected = settings.cloudBackupTarget == target,
                        onClick = { viewModel.setCloudBackupTarget(target) },
                        label = { Text(stringResource(labelRes)) }
                    )
                }
            }
            if (settings.cloudBackupTarget == "folder") {
                Text(stringResource(R.string.backup_folder_hint), style = MaterialTheme.typography.bodySmall)
                Button(
                    onClick = { backupFolderLauncher.launch(null) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.set_backup_folder))
                }
                if (!settings.cloudBackupFolderUri.isNullOrBlank()) {
                    Text(
                        stringResource(R.string.folder_set),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            } else {
                if (driveSignedIn) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            stringResource(R.string.signed_in_as, com.google.android.gms.auth.api.signin.GoogleSignIn.getLastSignedInAccount(context)?.email ?: ""),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        androidx.compose.material3.TextButton(
                            onClick = { viewModel.signOutFromDrive() }
                        ) {
                            Text(stringResource(R.string.sign_out))
                        }
                    }
                } else {
                    Button(
                        onClick = { googleSignInLauncher.launch(viewModel.getGoogleSignInIntent()) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.sign_in_with_google))
                    }
                }
            }
            cloudBackupLastSuccess?.let { millis ->
                Text(
                    stringResource(R.string.cloud_backup_last, formatRelativeTimeAgo(millis, context)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            val backupRestoreEnabled = when (settings.cloudBackupTarget) {
                "google_drive" -> driveSignedIn
                else -> !settings.cloudBackupFolderUri.isNullOrBlank()
            }
            Button(
                onClick = { viewModel.backupToCloud() },
                modifier = Modifier.fillMaxWidth(),
                enabled = backupRestoreEnabled
            ) {
                Text(stringResource(R.string.backup_to_cloud))
            }
            Button(
                onClick = {
                    viewModel.loadCloudBackupFiles()
                    showRestoreFromFolderDialog = true
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = backupRestoreEnabled
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
                            Text(stringResource(R.string.loading))
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
                        color = if (msg == stringResource(R.string.backup_started_in_background)) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
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
            Spacer(modifier = Modifier.padding(8.dp))
            Text(stringResource(R.string.exam_data_only), style = MaterialTheme.typography.titleMedium)
            Text(stringResource(R.string.export_exam_data_hint), style = MaterialTheme.typography.bodySmall)
            Button(
                onClick = { viewModel.exportExamData() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.export_exam_data))
            }
            Text(stringResource(R.string.restore_exam_data_hint), style = MaterialTheme.typography.bodySmall)
            Button(
                onClick = { importExamDataLauncher.launch(arrayOf("application/json", "*/*")) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.restore_exam_data))
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
                        color = if (msg == stringResource(R.string.restore_successful)) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                        maxLines = Int.MAX_VALUE
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Suppress("DEPRECATION")
@Composable
private fun VoiceDropdown(
    selectedVoiceName: String?,
    onVoiceSelected: (String?) -> Unit
) {
    val context = LocalContext.current
    val systemDefaultStr = stringResource(R.string.system_default)
    var voices by remember { mutableStateOf<List<VoiceOption>>(emptyList()) }
    var expanded by remember { mutableStateOf(false) }
    var selectedLabel by remember(selectedVoiceName) {
        mutableStateOf(
            if (selectedVoiceName.isNullOrEmpty()) systemDefaultStr
            else voices.find { it.voiceName == selectedVoiceName }?.displayName ?: systemDefaultStr
        )
    }
    LaunchedEffect(Unit) {
        voices = withContext(Dispatchers.Main.immediate) { loadAvailableVoicesIndia(context) }
        selectedLabel = if (selectedVoiceName.isNullOrEmpty()) systemDefaultStr
        else voices.find { it.voiceName == selectedVoiceName }?.displayName ?: systemDefaultStr
    }
    LaunchedEffect(selectedVoiceName, voices) {
        selectedLabel = if (selectedVoiceName.isNullOrEmpty()) systemDefaultStr
        else voices.find { it.voiceName == selectedVoiceName }?.displayName ?: systemDefaultStr
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
                text = { Text(systemDefaultStr) },
                onClick = {
                    onVoiceSelected(null)
                    selectedLabel = systemDefaultStr
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
