package com.studyasist.ui.onlineresources

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.studyasist.R
import com.studyasist.ui.studentclass.BOARD_OPTIONS
import com.studyasist.ui.studentclass.STANDARD_OPTIONS

private val RESOURCE_TYPE_OPTIONS = listOf("books", "sample_papers")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnlineResourcesScreen(
    viewModel: OnlineResourcesViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var standardExpanded by remember { mutableStateOf(false) }
    var boardExpanded by remember { mutableStateOf(false) }
    var subjectExpanded by remember { mutableStateOf(false) }
    var resourceTypeExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.online_resources)) },
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                stringResource(R.string.online_resources_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Standard dropdown
            Text(stringResource(R.string.standard), style = MaterialTheme.typography.titleSmall)
            ExposedDropdownMenuBox(expanded = standardExpanded, onExpandedChange = { standardExpanded = it }) {
                OutlinedTextField(
                    value = uiState.standard,
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = standardExpanded) }
                )
                ExposedDropdownMenu(expanded = standardExpanded, onDismissRequest = { standardExpanded = false }) {
                    STANDARD_OPTIONS.forEach { opt ->
                        DropdownMenuItem(
                            text = { Text(opt) },
                            onClick = { viewModel.setStandard(opt); standardExpanded = false }
                        )
                    }
                }
            }

            // Board dropdown
            Text(stringResource(R.string.board), style = MaterialTheme.typography.titleSmall)
            ExposedDropdownMenuBox(expanded = boardExpanded, onExpandedChange = { boardExpanded = it }) {
                OutlinedTextField(
                    value = uiState.board,
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = boardExpanded) }
                )
                ExposedDropdownMenu(expanded = boardExpanded, onDismissRequest = { boardExpanded = false }) {
                    BOARD_OPTIONS.forEach { opt ->
                        DropdownMenuItem(
                            text = { Text(opt) },
                            onClick = { viewModel.setBoard(opt); boardExpanded = false }
                        )
                    }
                }
            }

            // Subject dropdown
            Text(stringResource(R.string.subject), style = MaterialTheme.typography.titleSmall)
            ExposedDropdownMenuBox(expanded = subjectExpanded, onExpandedChange = { subjectExpanded = it }) {
                OutlinedTextField(
                    value = uiState.subject,
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
                    placeholder = { Text(stringResource(R.string.subject)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = subjectExpanded) }
                )
                ExposedDropdownMenu(expanded = subjectExpanded, onDismissRequest = { subjectExpanded = false }) {
                    uiState.subjectOptions.forEach { opt ->
                        DropdownMenuItem(
                            text = { Text(opt) },
                            onClick = { viewModel.setSubject(opt); subjectExpanded = false }
                        )
                    }
                }
            }

            // Resource type dropdown
            Text(stringResource(R.string.type), style = MaterialTheme.typography.titleSmall)
            ExposedDropdownMenuBox(expanded = resourceTypeExpanded, onExpandedChange = { resourceTypeExpanded = it }) {
                OutlinedTextField(
                    value = when (uiState.resourceType) {
                        "books" -> stringResource(R.string.find_books)
                        else -> stringResource(R.string.find_sample_papers)
                    },
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = resourceTypeExpanded) }
                )
                ExposedDropdownMenu(expanded = resourceTypeExpanded, onDismissRequest = { resourceTypeExpanded = false }) {
                    RESOURCE_TYPE_OPTIONS.forEach { opt ->
                        DropdownMenuItem(
                            text = {
                                Text(when (opt) {
                                    "books" -> stringResource(R.string.find_books)
                                    else -> stringResource(R.string.find_sample_papers)
                                })
                            },
                            onClick = {
                                viewModel.setResourceType(opt)
                                resourceTypeExpanded = false
                            }
                        )
                    }
                }
            }

            Button(
                onClick = { viewModel.search() },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(end = 8.dp),
                        strokeWidth = 2.dp
                    )
                }
                Text(if (uiState.isLoading) stringResource(R.string.searching) else stringResource(R.string.search))
            }

            uiState.errorMessage?.let { msg ->
                Text(msg, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
            }

            uiState.downloadMessage?.let { msg ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        msg,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (msg.startsWith("Saved")) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
                    androidx.compose.material3.TextButton(onClick = { viewModel.clearDownloadMessage() }) {
                        Text(stringResource(R.string.dismiss))
                    }
                }
            }

            uiState.results.forEach { item ->
                ResourceResultItem(
                    item = item,
                    onPreview = {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(item.url)))
                    },
                    onDownload = { viewModel.download(item) }
                )
            }
        }
    }
}

@Composable
private fun ResourceResultItem(
    item: OnlineResourceItem,
    onPreview: () -> Unit,
    onDownload: () -> Unit
) {
    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.fillMaxWidth()
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(onClick = onPreview) {
                    Icon(Icons.Default.OpenInBrowser, contentDescription = null, modifier = Modifier.padding(end = 4.dp))
                    Text(stringResource(R.string.preview))
                }
                Button(onClick = onDownload) {
                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.padding(end = 4.dp))
                    Text(stringResource(R.string.download))
                }
            }
        }
    }
}
