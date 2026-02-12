package com.studyasist.ui.resultlist

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.studyasist.R
import com.studyasist.data.repository.ResultListItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultListScreen(
    viewModel: ResultListViewModel,
    onBack: () -> Unit,
    onResultClick: (Long) -> Unit,
    onManualReview: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var showExportMenu by remember { mutableStateOf(false) }

    fun shareExportCsv() {
        coroutineScope.launch {
            val csv = viewModel.getExportCsv()
            val file = java.io.File(context.cacheDir, "studyasist_results_${System.currentTimeMillis()}.csv")
            file.writeText(csv)
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, context.getString(R.string.export_results)))
        }
    }

    fun shareExportPdf() {
        coroutineScope.launch {
            val pdfBytes = viewModel.getExportPdf()
            val file = java.io.File(context.cacheDir, "studyasist_results_${System.currentTimeMillis()}.pdf")
            file.writeBytes(pdfBytes)
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, context.getString(R.string.export_results)))
        }
    }

    fun shareExportExcel() {
        coroutineScope.launch {
            val excelBytes = viewModel.getExportExcel()
            val file = java.io.File(context.cacheDir, "studyasist_results_${System.currentTimeMillis()}.xls")
            file.writeBytes(excelBytes)
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/vnd.ms-excel"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, context.getString(R.string.export_results)))
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.results)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                actions = {
                    IconButton(onClick = onManualReview) {
                        Icon(Icons.Default.Assignment, contentDescription = stringResource(R.string.manual_review_list))
                    }
                    Box {
                        IconButton(
                            onClick = { showExportMenu = true },
                            enabled = uiState.items.isNotEmpty()
                        ) {
                            Icon(Icons.Default.Share, contentDescription = stringResource(R.string.export_results))
                        }
                        DropdownMenu(
                            expanded = showExportMenu,
                            onDismissRequest = { showExportMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.export_csv)) },
                                onClick = {
                                    showExportMenu = false
                                    shareExportCsv()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.export_pdf)) },
                                onClick = {
                                    showExportMenu = false
                                    shareExportPdf()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.export_excel)) },
                                onClick = {
                                    showExportMenu = false
                                    shareExportExcel()
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        if (uiState.items.isEmpty() && !uiState.isLoading) {
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "No results yet",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.items, key = { it.resultId }) { item ->
                    ResultCard(
                        assessmentTitle = item.assessmentTitle,
                        attemptLabel = item.attemptLabel,
                        percent = item.percent,
                        onClick = { onResultClick(item.attemptId) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ResultCard(
    assessmentTitle: String,
    attemptLabel: String,
    percent: Float,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                assessmentTitle,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                attemptLabel,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                "%.0f%%".format(percent),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
