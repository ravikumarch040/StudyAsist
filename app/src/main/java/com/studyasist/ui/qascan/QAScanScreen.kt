package com.studyasist.ui.qascan

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.studyasist.R
import com.studyasist.ui.components.ImageCropSelector
import java.io.File
import com.studyasist.data.local.entity.QuestionType
import com.studyasist.ui.qascan.EditableQARow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QAScanScreen(
    viewModel: QAScanViewModel,
    onBack: () -> Unit,
    onSaved: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var cameraUri by remember { mutableStateOf<Uri?>(null) }
    var pendingCropUri by remember { mutableStateOf<Uri?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) cameraUri?.let { pendingCropUri = it }
    }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) cameraUri?.let { cameraLauncher.launch(it) }
    }
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { pendingCropUri = it }
    }
    val scrollState = rememberScrollState()
    var subjectExpanded by remember { mutableStateOf(false) }
    var chapterExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.scan_qa)) },
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
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
                    .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        val dir = context.getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES)
                        val file = File(dir, "qascan_${System.currentTimeMillis()}.jpg")
                        cameraUri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                            context.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                            permissionLauncher.launch(Manifest.permission.CAMERA)
                        } else {
                            cameraUri?.let { cameraLauncher.launch(it) }
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.PhotoCamera, contentDescription = null, Modifier.padding(end = 4.dp))
                    Text(stringResource(R.string.take_photo))
                }
                Button(
                    onClick = { galleryLauncher.launch("image/*") },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.PhotoLibrary, contentDescription = null, Modifier.padding(end = 4.dp))
                    Text(stringResource(R.string.upload_image))
                }
            }
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Use AI extraction (recommended for math)", style = MaterialTheme.typography.bodyMedium)
                Switch(
                    checked = uiState.useAiExtraction,
                    onCheckedChange = { viewModel.setUseAiExtraction(it) }
                )
            }
            if (uiState.imageUri != null) {
                Text(
                    stringResource(R.string.image_selected),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Button(
                    onClick = { viewModel.extractAndParse() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isLoading
                ) {
                    Text(
                        if (uiState.isLoading) "…" else stringResource(R.string.extract_questions)
                    )
                }
            }
            uiState.errorMessage?.let { msg ->
                Text(msg, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
            }
            if (uiState.parsedRows.isNotEmpty()) {
                Text(
                    stringResource(R.string.subject),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                ExposedDropdownMenuBox(
                    expanded = subjectExpanded,
                    onExpandedChange = { subjectExpanded = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = uiState.subject,
                        onValueChange = { viewModel.updateSubject(it) },
                        label = { Text(stringResource(R.string.subject)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = subjectExpanded) }
                    )
                    ExposedDropdownMenu(
                        expanded = subjectExpanded,
                        onDismissRequest = { subjectExpanded = false }
                    ) {
                        uiState.distinctSubjects
                            .filter { it.contains(uiState.subject, ignoreCase = true) || uiState.subject.isBlank() }
                            .forEach { subject ->
                                DropdownMenuItem(
                                    text = { Text(subject) },
                                    onClick = {
                                        viewModel.updateSubject(subject)
                                        subjectExpanded = false
                                    }
                                )
                            }
                        if (uiState.distinctSubjects.none { it.equals(uiState.subject, ignoreCase = true) } && uiState.subject.isNotBlank()) {
                            DropdownMenuItem(
                                text = { Text("Use \"${uiState.subject}\" (new)") },
                                onClick = {
                                    subjectExpanded = false
                                }
                            )
                        }
                    }
                }
                Text(
                    stringResource(R.string.chapter),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                ExposedDropdownMenuBox(
                    expanded = chapterExpanded,
                    onExpandedChange = { chapterExpanded = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = uiState.chapter,
                        onValueChange = { viewModel.updateChapter(it) },
                        label = { Text(stringResource(R.string.chapter)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = chapterExpanded) }
                    )
                    ExposedDropdownMenu(
                        expanded = chapterExpanded,
                        onDismissRequest = { chapterExpanded = false }
                    ) {
                        uiState.distinctChapters.forEach { chapter ->
                            DropdownMenuItem(
                                text = { Text(chapter) },
                                onClick = {
                                    viewModel.updateChapter(chapter)
                                    chapterExpanded = false
                                }
                            )
                        }
                        DropdownMenuItem(
                            text = { Text("Enter new (type above)") },
                            onClick = { chapterExpanded = false }
                        )
                    }
                }
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        stringResource(R.string.question) + " / " + stringResource(R.string.answer),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Button(
                        onClick = { viewModel.improveWithAi() },
                        enabled = !uiState.isLoading && uiState.imageUri != null
                    ) {
                        Text(stringResource(R.string.improve_with_ai))
                    }
                }
                uiState.parsedRows.forEachIndexed { index, row ->
                    EditableQARowCard(
                        index = index,
                        row = row,
                        onUpdate = { q, a, t -> viewModel.updateRow(index, q, a, t) },
                        onRemove = { viewModel.removeRow(index) }
                    )
                }
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(onClick = { viewModel.addEmptyRow() }) {
                        Icon(Icons.Default.Add, contentDescription = null, Modifier.padding(end = 4.dp))
                        Text(stringResource(R.string.add_row))
                    }
                    Button(
                        onClick = { viewModel.saveToBank(onSaved) },
                        modifier = Modifier.weight(1f),
                        enabled = !uiState.isLoading
                    ) {
                        Text(stringResource(R.string.save_to_bank))
                    }
                }
            }
        }
            if (pendingCropUri != null) {
                ImageCropSelector(
                    imageUri = pendingCropUri,
                    onCropped = { uri ->
                        viewModel.setImageUri(uri)
                        pendingCropUri = null
                    },
                    onCancel = { pendingCropUri = null },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditableQARowCard(
    index: Int,
    row: EditableQARow,
    onUpdate: (String, String, QuestionType) -> Unit,
    onRemove: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "#${index + 1}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                IconButton(onClick = onRemove) {
                    Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.delete))
                }
            }
            OutlinedTextField(
                value = row.question,
                onValueChange = { onUpdate(it, row.answer, row.type) },
                label = { Text(stringResource(R.string.question)) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )
            OutlinedTextField(
                value = row.answer,
                onValueChange = { onUpdate(row.question, it, row.type) },
                label = { Text(stringResource(R.string.answer)) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 1
            )
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = it }
            ) {
                OutlinedTextField(
                    value = row.type.name,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.type)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    QuestionType.entries.forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type.name) },
                            onClick = {
                                onUpdate(row.question, row.answer, type)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}
