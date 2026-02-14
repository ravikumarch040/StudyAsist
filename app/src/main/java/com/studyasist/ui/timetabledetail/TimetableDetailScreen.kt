package com.studyasist.ui.timetabledetail

import android.content.Intent
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintManager
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.studyasist.R
import com.studyasist.data.local.entity.ActivityEntity
import com.studyasist.data.local.entity.ActivityType
import androidx.core.content.FileProvider
import com.studyasist.ui.components.colorForActivityType
import com.studyasist.util.formatTimeMinutes
import com.studyasist.util.sharePdfAsImage
import kotlinx.coroutines.launch

private const val SLOT_MINUTES = 30

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimetableDetailScreen(
    viewModel: TimetableDetailViewModel,
    onBack: () -> Unit,
    onAddActivity: (Long, Int) -> Unit,
    onEditActivity: (Long, Long) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val timetable = uiState.timetable ?: return
    val timetableId = timetable.id
    var selectedViewTab by remember { mutableIntStateOf(0) }
    var showExportMenu by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val days = listOf(1 to R.string.day_mon, 2 to R.string.day_tue, 3 to R.string.day_wed, 4 to R.string.day_thu, 5 to R.string.day_fri, 6 to R.string.day_sat, 7 to R.string.day_sun)

    fun shareExportCsv() {
        coroutineScope.launch {
            val csv = viewModel.getExportCsv()
            val file = java.io.File(context.cacheDir, "studyasist_timetable_${timetableId}_${System.currentTimeMillis()}.csv")
            file.writeText(csv)
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, context.getString(R.string.export_timetable)))
        }
    }

    fun shareExportPdf() {
        coroutineScope.launch {
            val pdfBytes = viewModel.getExportPdf()
            val file = java.io.File(context.cacheDir, "studyasist_timetable_${timetableId}_${System.currentTimeMillis()}.pdf")
            file.writeBytes(pdfBytes)
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, context.getString(R.string.export_timetable)))
        }
    }

    fun shareExportExcel() {
        coroutineScope.launch {
            val excelBytes = viewModel.getExportExcel()
            val file = java.io.File(context.cacheDir, "studyasist_timetable_${timetableId}_${System.currentTimeMillis()}.xls")
            file.writeBytes(excelBytes)
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/vnd.ms-excel"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, context.getString(R.string.export_timetable)))
        }
    }

    fun shareAsImage() {
        coroutineScope.launch {
            val pdfBytes = viewModel.getExportPdf()
            if (pdfBytes.isEmpty()) return@launch
            sharePdfAsImage(
                context = context,
                pdfBytes = pdfBytes,
                filePrefix = "timetable_share_$timetableId",
                chooserTitle = context.getString(R.string.export_timetable)
            )
        }
    }

    fun printTimetable() {
        coroutineScope.launch {
            val pdfBytes = viewModel.getExportPdf()
            if (pdfBytes.isEmpty()) return@launch
            val printManager = context.getSystemService(android.content.Context.PRINT_SERVICE) as? PrintManager ?: return@launch
            val jobName = "${context.getString(R.string.app_name)} - ${timetable.name}"
            val bytes = pdfBytes
            val adapter = object : PrintDocumentAdapter() {
                override fun onLayout(
                    oldAttributes: PrintAttributes?,
                    newAttributes: PrintAttributes,
                    cancellationSignal: android.os.CancellationSignal?,
                    callback: android.print.PrintDocumentAdapter.LayoutResultCallback?,
                    metadata: android.os.Bundle?
                ) {
                    if (cancellationSignal?.isCanceled == true) {
                        callback?.onLayoutCancelled()
                        return
                    }
                    val info = android.print.PrintDocumentInfo.Builder("timetable.pdf")
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
                        java.io.FileOutputStream(destination.fileDescriptor).use { it.write(bytes) }
                        callback.onWriteFinished(arrayOf(android.print.PageRange.ALL_PAGES))
                    } catch (e: Exception) {
                        callback.onWriteFailed(e.message)
                    }
                }
            }
            printManager.print(jobName, adapter, PrintAttributes.Builder().build())
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(timetable.name) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = { showExportMenu = true }) {
                            Icon(Icons.Default.Share, contentDescription = stringResource(R.string.export_timetable))
                        }
                        androidx.compose.material3.DropdownMenu(
                            expanded = showExportMenu,
                            onDismissRequest = { showExportMenu = false }
                        ) {
                            androidx.compose.material3.DropdownMenuItem(
                                text = { Text(stringResource(R.string.export_csv)) },
                                onClick = { showExportMenu = false; shareExportCsv() }
                            )
                            androidx.compose.material3.DropdownMenuItem(
                                text = { Text(stringResource(R.string.export_pdf)) },
                                onClick = { showExportMenu = false; shareExportPdf() }
                            )
                            androidx.compose.material3.DropdownMenuItem(
                                text = { Text(stringResource(R.string.export_excel)) },
                                onClick = { showExportMenu = false; shareExportExcel() }
                            )
                            androidx.compose.material3.DropdownMenuItem(
                                text = { Text(stringResource(R.string.share_as_image)) },
                                onClick = { showExportMenu = false; shareAsImage() }
                            )
                        }
                    }
                    IconButton(onClick = { printTimetable() }) {
                        Icon(Icons.Default.Print, contentDescription = stringResource(R.string.print_timetable))
                    }
                    IconButton(onClick = { onAddActivity(timetableId, uiState.selectedDay) }) {
                        Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_activity))
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
            Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            TabRow(selectedTabIndex = selectedViewTab) {
                Tab(selected = selectedViewTab == 0, onClick = { selectedViewTab = 0 }, text = { Text(stringResource(R.string.day_view)) })
                Tab(selected = selectedViewTab == 1, onClick = { selectedViewTab = 1 }, text = { Text(stringResource(R.string.week_view)) })
            }
            Text(stringResource(R.string.filter), style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
            Row(
                Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                androidx.compose.material3.FilterChip(
                    selected = uiState.filterType == null,
                    onClick = { viewModel.setFilterType(null) },
                    label = { Text(stringResource(R.string.filter_all)) }
                )
                for (type in ActivityType.entries) {
                    val (containerColor, contentColor) = MaterialTheme.colorScheme.colorForActivityType(type)
                    androidx.compose.material3.FilterChip(
                        selected = uiState.filterType == type,
                        onClick = { viewModel.setFilterType(type) },
                        label = { Text(type.name) },
                        colors = androidx.compose.material3.FilterChipDefaults.filterChipColors(
                            selectedContainerColor = containerColor,
                            selectedLabelColor = contentColor
                        )
                    )
                }
            }
            if (uiState.minutesByType.isNotEmpty()) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        stringResource(R.string.weekly_summary),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    uiState.minutesByType.entries
                        .filter { it.value > 0 }
                        .sortedBy { it.key.name }
                        .forEach { (type, mins) ->
                            val hours = mins / 60
                            val minsPart = mins % 60
                            val (_, contentColor) = MaterialTheme.colorScheme.colorForActivityType(type)
                            val durationStr = when {
                                hours > 0 && minsPart > 0 -> "${hours}h ${minsPart}m"
                                hours > 0 -> "${hours}h"
                                else -> "${minsPart}m"
                            }
                            Text(
                                "${type.name}: $durationStr",
                                style = MaterialTheme.typography.bodySmall,
                                color = contentColor
                            )
                        }
                }
            }
            when (selectedViewTab) {
                0 -> {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        for ((day, labelResId) in days) {
                            val selected = uiState.selectedDay == day
                            androidx.compose.material3.FilterChip(
                                selected = selected,
                                onClick = { viewModel.setSelectedDay(day) },
                                label = { Text(stringResource(labelResId)) }
                            )
                        }
                    }
                    val filtered = if (uiState.filterType == null) uiState.activities else uiState.activities.filter { it.type == uiState.filterType }
                    val dayActivities = filtered.filter { it.dayOfWeek == uiState.selectedDay }
                    if (dayActivities.isEmpty()) {
                        Box(
                            Modifier.fillMaxSize().padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                stringResource(R.string.no_activities_day),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        LazyColumn(
                            Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(dayActivities, key = { it.id }) { activity ->
                                ActivityCard(activity = activity, onClick = { onEditActivity(timetableId, activity.id) })
                            }
                        }
                    }
                }
                1 -> WeekView(
                    activities = if (uiState.filterType == null) uiState.activities else uiState.activities.filter { it.type == uiState.filterType },
                    onActivityClick = { onEditActivity(timetableId, it.id) }
                )
            }
        }
    }
}

@Composable
private fun WeekView(
    activities: List<ActivityEntity>,
    onActivityClick: (ActivityEntity) -> Unit
) {
    if (activities.isEmpty()) {
        Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
            Text(stringResource(R.string.no_activities_week), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }
    val minMinutes = activities.minOf { it.startTimeMinutes }.let { (it / SLOT_MINUTES) * SLOT_MINUTES }
    val maxMinutes = activities.maxOf { it.endTimeMinutes }.let { ((it + SLOT_MINUTES - 1) / SLOT_MINUTES) * SLOT_MINUTES }
    val slotCount = ((maxMinutes - minMinutes) / SLOT_MINUTES).coerceAtLeast(1)
    val dayLabelResIds = listOf(R.string.day_mon, R.string.day_tue, R.string.day_wed, R.string.day_thu, R.string.day_fri, R.string.day_sat, R.string.day_sun)
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(8.dp)) {
        item {
            Row(Modifier.fillMaxWidth()) {
                Box(Modifier.widthIn(min = 48.dp).padding(4.dp)) {}
                dayLabelResIds.forEach { resId ->
                    Box(
                        Modifier
                            .weight(1f)
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(stringResource(resId), style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
        items(slotCount) { rowIndex ->
            val slotStart = minMinutes + rowIndex * SLOT_MINUTES
            Row(Modifier.fillMaxWidth().height(44.dp)) {
                Box(
                    Modifier
                        .widthIn(min = 48.dp)
                        .padding(4.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        formatTimeMinutes(slotStart),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                for (day in 1..7) {
                    val slotEnd = slotStart + SLOT_MINUTES
                    val cellActivities = activities.filter { it.dayOfWeek == day && it.startTimeMinutes < slotEnd && it.endTimeMinutes > slotStart }
                    Box(
                        Modifier
                            .weight(1f)
                            .padding(2.dp)
                            .fillMaxSize()
                            .then(
                                if (cellActivities.isNotEmpty()) Modifier.clickable { onActivityClick(cellActivities.first()) }
                                else Modifier
                            )
                    ) {
                        if (cellActivities.isNotEmpty()) {
                            val act = cellActivities.first()
                            val (containerColor, contentColor) = MaterialTheme.colorScheme.colorForActivityType(act.type)
                            Card(
                                modifier = Modifier.fillMaxSize(),
                                colors = CardDefaults.cardColors(containerColor = containerColor.copy(alpha = 0.7f)),
                                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                            ) {
                                Box(Modifier.fillMaxSize().padding(4.dp)) {
                                    Text(
                                        act.title,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = contentColor,
                                        maxLines = 2
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ActivityCard(
    activity: ActivityEntity,
    onClick: () -> Unit
) {
    val (containerColor, contentColor) = MaterialTheme.colorScheme.colorForActivityType(activity.type)
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Row(
            Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = activity.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = contentColor
                )
                Text(
                    text = "${formatTimeMinutes(activity.startTimeMinutes)} – ${formatTimeMinutes(activity.endTimeMinutes)} · ${activity.type.name}",
                    style = MaterialTheme.typography.bodySmall,
                    color = contentColor.copy(alpha = 0.8f)
                )
                activity.note?.let { note ->
                    if (note.isNotBlank()) {
                        Text(text = note, style = MaterialTheme.typography.bodySmall, color = contentColor.copy(alpha = 0.8f))
                    }
                }
            }
        }
    }
}
