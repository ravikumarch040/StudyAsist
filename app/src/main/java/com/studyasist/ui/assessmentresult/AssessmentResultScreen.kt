package com.studyasist.ui.assessmentresult

import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.studyasist.R
import com.studyasist.util.sharePdfAsImage
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssessmentResultScreen(
    viewModel: AssessmentResultViewModel,
    onBack: () -> Unit,
    onRevise: (subject: String?, chapter: String?) -> Unit = { _, _ -> },
    onAddToTimetable: (subject: String, chapter: String?) -> Unit = { _, _ -> },
    onManualReview: () -> Unit = {},
    onRetry: (Long) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    var showExportMenu by remember { mutableStateOf(false) }

    fun shareAsImage() {
        coroutineScope.launch {
            val pdfBytes = viewModel.getExportPdf()
            if (pdfBytes == null || pdfBytes.isEmpty()) return@launch
            sharePdfAsImage(
                context = context,
                pdfBytes = pdfBytes,
                filePrefix = "result_share",
                chooserTitle = context.getString(R.string.export_results)
            )
        }
    }

    fun printResult() {
        coroutineScope.launch {
            val pdfBytes = viewModel.getExportPdf()
            if (pdfBytes == null || pdfBytes.isEmpty()) return@launch
            val printManager = context.getSystemService(android.content.Context.PRINT_SERVICE) as? PrintManager
                ?: return@launch
            val jobName = "${context.getString(R.string.app_name)} - ${context.getString(R.string.result)}"
            val adapter = object : PrintDocumentAdapter() {
                override fun onLayout(
                    oldAttributes: android.print.PrintAttributes?,
                    newAttributes: android.print.PrintAttributes,
                    cancellationSignal: android.os.CancellationSignal?,
                    callback: android.print.PrintDocumentAdapter.LayoutResultCallback?,
                    metadata: android.os.Bundle?
                ) {
                    if (cancellationSignal?.isCanceled == true) {
                        callback?.onLayoutCancelled()
                        return
                    }
                    val info = android.print.PrintDocumentInfo.Builder("result.pdf")
                        .setContentType(android.print.PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                        .setPageCount(1)
                        .build()
                    callback?.onLayoutFinished(info, true)
                }

                override fun onWrite(
                    pages: Array<out android.print.PageRange>,
                    destination: android.os.ParcelFileDescriptor,
                    cancellationSignal: android.os.CancellationSignal,
                    callback: android.print.PrintDocumentAdapter.WriteResultCallback
                ) {
                    try {
                        java.io.FileOutputStream(destination.fileDescriptor).use { it.write(pdfBytes) }
                        callback.onWriteFinished(arrayOf(android.print.PageRange.ALL_PAGES))
                    } catch (e: Exception) {
                        callback.onWriteFailed(e.message)
                    }
                }
            }
            printManager.print(jobName, adapter, PrintAttributes.Builder().build())
        }
    }

    fun launchRetry(onlyWrongPartial: Boolean) {
        coroutineScope.launch {
            val newAssessmentId = viewModel.createRetryAssessment(onlyWrongPartial)
            newAssessmentId?.let { onRetry(it) }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.result)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleFlagForReview() }) {
                        Icon(
                            Icons.Default.Flag,
                            contentDescription = if (uiState.needsManualReview) stringResource(R.string.unflag) else stringResource(R.string.flag_for_review),
                            tint = if (uiState.needsManualReview) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Box {
                        IconButton(
                            onClick = { showExportMenu = true },
                            enabled = !uiState.isLoading
                        ) {
                            Icon(Icons.Default.Share, contentDescription = stringResource(R.string.export_results))
                        }
                        DropdownMenu(
                            expanded = showExportMenu,
                            onDismissRequest = { showExportMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.share_as_image)) },
                                onClick = {
                                    showExportMenu = false
                                    shareAsImage()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.print_results)) },
                                onClick = {
                                    showExportMenu = false
                                    printResult()
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
        if (uiState.isLoading) {
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(stringResource(R.string.loading), style = MaterialTheme.typography.bodyLarge)
            }
            return@Scaffold
        }

        Column(
            Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(
                    Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        stringResource(R.string.percent_format, uiState.percent),
                        style = MaterialTheme.typography.displayMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        stringResource(R.string.score_format, uiState.score, uiState.maxScore),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Row(
                        modifier = Modifier.padding(top = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { launchRetry(onlyWrongPartial = false) }
                        ) {
                            Icon(Icons.Default.Replay, contentDescription = stringResource(R.string.cd_retry), modifier = Modifier.padding(end = 8.dp))
                            Text(stringResource(R.string.retry_all))
                        }
                        if (uiState.details.any { !it.correct }) {
                            Button(
                                onClick = { launchRetry(onlyWrongPartial = true) },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                            ) {
                                Text(stringResource(R.string.retry_weak))
                            }
                        }
                        Button(
                            onClick = {
                                val sc = uiState.subjectChapter
                                onRevise(sc?.subject, sc?.chapter)
                            }
                        ) {
                            Icon(Icons.Default.MenuBook, contentDescription = stringResource(R.string.cd_revise), modifier = Modifier.padding(end = 8.dp))
                            Text(stringResource(R.string.revise))
                        }
                        if (uiState.subjectChapter != null) {
                            val sc = uiState.subjectChapter!!
                            if ((sc.subject ?: "").isNotBlank() || !sc.chapter.isNullOrBlank()) {
                                Button(
                                    onClick = { onAddToTimetable((sc.subject ?: "").ifBlank { "Revision" }, sc.chapter) },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                                ) {
                                    Icon(Icons.Default.Schedule, contentDescription = stringResource(R.string.cd_add_to_timetable), modifier = Modifier.padding(end = 8.dp))
                                    Text(stringResource(R.string.add_to_timetable))
                                }
                            }
                        }
                        if (uiState.needsManualReview) {
                            Button(
                                onClick = onManualReview,
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                            ) {
                                Text(stringResource(R.string.override_score))
                            }
                        }
                    }
                }
            }

            if (uiState.manualFeedback != null && uiState.manualFeedback!!.isNotBlank()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f))
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Text(
                            stringResource(R.string.manual_feedback),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            uiState.manualFeedback!!,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Text(
                stringResource(R.string.details),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            uiState.details.forEachIndexed { index, item ->
                val (icon, iconColor, cardColor) = when {
                    item.correct -> Triple("✓", MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                    item.partialCredit -> Triple("~", MaterialTheme.colorScheme.tertiary, MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f))
                    else -> Triple("✗", MaterialTheme.colorScheme.error, MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f))
                }
                val canRevise = !item.correct && (item.subject != null || item.chapter != null)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = cardColor)
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                icon,
                                style = MaterialTheme.typography.titleMedium,
                                color = iconColor
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    stringResource(R.string.question_score_format, item.questionScore),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    "#${index + 1}",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                        if (item.questionText.isNotBlank()) {
                            Text(
                                item.questionText,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                        if (item.userAnswer != null) {
                            Text(
                                "Your answer: ${item.userAnswer}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                        Text(
                            "Correct: ${item.modelAnswer}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                        if (item.feedback.isNotBlank()) {
                            Text(
                                item.feedback,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                        if (canRevise) {
                            Row(
                                modifier = Modifier.padding(top = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { onRevise(item.subject, item.chapter) },
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                ) {
                                    Icon(Icons.Default.MenuBook, contentDescription = stringResource(R.string.cd_revise), modifier = Modifier.padding(end = 4.dp))
                                    Text(stringResource(R.string.revise))
                                }
                                Button(
                                    onClick = {
                                        val subj = item.subject?.takeIf { it.isNotBlank() } ?: "Revision"
                                        onAddToTimetable(subj, item.chapter)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                ) {
                                    Icon(Icons.Default.Schedule, contentDescription = stringResource(R.string.cd_add_to_timetable), modifier = Modifier.padding(end = 4.dp))
                                    Text(stringResource(R.string.add_to_timetable))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
