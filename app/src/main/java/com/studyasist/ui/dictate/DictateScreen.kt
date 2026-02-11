package com.studyasist.ui.dictate

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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.studyasist.R
import com.studyasist.ui.components.ImageCropSelector
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DictateScreen(
    viewModel: DictateViewModel,
    onBack: () -> Unit
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.dictate)) },
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
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
            Text(
                stringResource(R.string.dictate_subtitle),
                style = MaterialTheme.typography.bodyMedium
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        val dir = context.getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES)
                        val file = File(dir, "dictate_${System.currentTimeMillis()}.jpg")
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
                    Icon(Icons.Default.PhotoCamera, null, Modifier.padding(end = 4.dp))
                    Text(stringResource(R.string.take_photo))
                }
                Button(
                    onClick = { galleryLauncher.launch("image/*") },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.PhotoLibrary, null, Modifier.padding(end = 4.dp))
                    Text(stringResource(R.string.upload_image))
                }
            }
            if (uiState.imageUri != null) {
                Card(colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceVariant)) {
                    Text(
                        stringResource(R.string.image_selected),
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
            Text(stringResource(R.string.language_for_reading), style = MaterialTheme.typography.titleSmall)
            LanguageDropdown(
                selectedCode = uiState.selectedLanguageCode,
                onSelected = viewModel::setLanguage,
                options = viewModel.languageOptions
            )
            Button(
                onClick = { viewModel.extractText() },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading && uiState.imageUri != null
            ) {
                Text(if (uiState.isLoading) "Extracting…" else stringResource(R.string.dictate_read_aloud))
            }
            if (uiState.errorMessage != null) {
                Text(uiState.errorMessage!!, color = MaterialTheme.colorScheme.error)
            }
            Text(stringResource(R.string.extracted_text), style = MaterialTheme.typography.titleSmall)
            OutlinedTextField(
                value = uiState.extractedText,
                onValueChange = {},
                readOnly = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                minLines = 4
            )
            if (uiState.extractedText.isNotBlank()) {
                Button(
                    onClick = { if (uiState.isSpeaking) viewModel.stopSpeaking() else viewModel.speak() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (uiState.isSpeaking) stringResource(R.string.stop) else stringResource(R.string.read_aloud))
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
private fun LanguageDropdown(
    selectedCode: String,
    onSelected: (String) -> Unit,
    options: List<Pair<String, String>>
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedLabel = options.find { it.first == selectedCode }?.second ?: selectedCode
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
            for (option in options) {
                DropdownMenuItem(
                    text = { Text(option.second) },
                    onClick = {
                        onSelected(option.first)
                        expanded = false
                    }
                )
            }
        }
    }
}
