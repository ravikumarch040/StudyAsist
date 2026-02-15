package com.studyasist.ui.assessmentcreate

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.studyasist.R
import com.studyasist.data.local.entity.Goal
import com.studyasist.data.local.entity.QA

@OptIn(ExperimentalMaterial3Api::class)
@Suppress("DEPRECATION")
@Composable
fun AssessmentCreateScreen(
    viewModel: AssessmentCreateViewModel,
    onBack: () -> Unit,
    onCreated: (Long) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.create_assessment)) },
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
        Box(Modifier.fillMaxSize()) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(paddingValues)
                    .verticalScroll(scrollState)
                    .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                stringResource(R.string.source),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = uiState.sourceMode == SourceMode.BY_GOAL,
                    onClick = { viewModel.setSourceMode(SourceMode.BY_GOAL) }
                )
                Text(stringResource(R.string.by_goal), Modifier.padding(end = 12.dp))
                RadioButton(
                    selected = uiState.sourceMode == SourceMode.BY_SUBJECT_CHAPTER,
                    onClick = { viewModel.setSourceMode(SourceMode.BY_SUBJECT_CHAPTER) }
                )
                Text(stringResource(R.string.by_subject_chapter), Modifier.padding(end = 12.dp))
                RadioButton(
                    selected = uiState.sourceMode == SourceMode.MANUAL,
                    onClick = { viewModel.setSourceMode(SourceMode.MANUAL) }
                )
                Text(stringResource(R.string.manual))
            }

            when (uiState.sourceMode) {
                SourceMode.BY_GOAL -> GoalDropdown(
                    goals = uiState.availableGoals,
                    selectedGoalId = uiState.selectedGoalId,
                    onSelected = { viewModel.setGoalId(it) }
                )
                SourceMode.BY_SUBJECT_CHAPTER -> SubjectChapterDropdowns(
                    subjects = uiState.distinctSubjects,
                    chapters = uiState.distinctChapters,
                    subject = uiState.subject,
                    chapter = uiState.chapter,
                    onSubjectChange = { viewModel.setSubject(it) },
                    onChapterChange = { viewModel.setChapter(it) }
                )
                SourceMode.MANUAL -> ManualSelectionSection(
                    selectedCount = uiState.selectedQas.size,
                    onSelectClick = { viewModel.openQaSelector() },
                    onClearClick = { viewModel.clearManualSelection() }
                )
            }

            OutlinedTextField(
                value = uiState.title,
                onValueChange = { viewModel.setTitle(it) },
                label = { Text(stringResource(R.string.assessment_title)) },
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (uiState.sourceMode == SourceMode.MANUAL) {
                    OutlinedTextField(
                        value = uiState.availableCount.toString(),
                        onValueChange = {},
                        label = { Text(stringResource(R.string.questions)) },
                        modifier = Modifier.weight(1f),
                        readOnly = true,
                        enabled = false
                    )
                } else {
                    OutlinedTextField(
                        value = uiState.questionCount.toString(),
                        onValueChange = { viewModel.setQuestionCount(it.toIntOrNull() ?: 10) },
                        label = { Text(stringResource(R.string.questions)) },
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = uiState.timeLimitMinutes.toString(),
                    onValueChange = { viewModel.setTimeLimitMinutes(it.toIntOrNull() ?: 30) },
                    label = { Text(stringResource(R.string.minutes)) },
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(stringResource(R.string.randomize_questions), style = MaterialTheme.typography.bodyMedium)
                Switch(
                    checked = uiState.randomize,
                    onCheckedChange = { viewModel.setRandomize(it) }
                )
            }

            when (uiState.sourceMode) {
                SourceMode.MANUAL -> if (uiState.selectedQas.isNotEmpty()) {
                    SelectedQAPreview(
                        qas = uiState.selectedQas,
                        onEditSelection = { viewModel.openQaSelector() }
                    )
                } else {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            Text(
                                stringResource(R.string.tap_select_questions_hint),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                else -> Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Text(
                            stringResource(R.string.questions_available_format, uiState.availableCount),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            uiState.errorMessage?.let { msg ->
                Text(
                    msg,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Button(
                onClick = { viewModel.createAssessment(onCreated) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading
            ) {
                Text(if (uiState.isLoading) stringResource(R.string.creating) else stringResource(R.string.create))
            }
            }
            if (uiState.showQaSelector) {
                QASelectorOverlay(
                    viewModel = viewModel,
                    onDismiss = { viewModel.closeQaSelector() },
                    onConfirm = { viewModel.confirmQaSelection() }
                )
            }
        }
    }
}

@Composable
private fun ManualSelectionSection(
    selectedCount: Int,
    onSelectClick: () -> Unit,
    onClearClick: () -> Unit
) {
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedButton(onClick = onSelectClick) {
            Text(stringResource(R.string.select_questions))
        }
        if (selectedCount > 0) {
            Text(
                stringResource(R.string.selected_count_format, selectedCount),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
            OutlinedButton(onClick = onClearClick) {
                Text(stringResource(R.string.clear))
            }
        }
    }
}

@Composable
private fun SelectedQAPreview(
    qas: List<QA>,
    onEditSelection: () -> Unit
) {
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
                    stringResource(R.string.questions_selected_format, qas.size),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedButton(onClick = onEditSelection) {
                    Text(stringResource(R.string.change_selection))
                }
            }
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(qas.take(5), key = { it.id }) { qa ->
                    Text(
                        "• ${qa.questionText.take(80)}${if (qa.questionText.length > 80) "…" else ""}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (qas.size > 5) {
                    item {
                        Text(
                            stringResource(R.string.and_more_format, qas.size - 5),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun QASelectorOverlay(
    viewModel: AssessmentCreateViewModel,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.95f))
    ) {
        Column(Modifier.fillMaxSize()) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = stringResource(R.string.cancel))
                }
                Text(
                    stringResource(R.string.select_questions_selected_format, uiState.selectedQaIds.size),
                    style = MaterialTheme.typography.titleMedium
                )
                IconButton(onClick = onConfirm) {
                    Icon(Icons.Default.Check, contentDescription = stringResource(R.string.save))
                }
            }
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SubjectChapterDropdowns(
                    subjects = uiState.distinctSubjects,
                    chapters = uiState.selectorDistinctChapters,
                    subject = uiState.selectorSubject,
                    chapter = uiState.selectorChapter,
                    onSubjectChange = { viewModel.setSelectorSubject(it) },
                    onChapterChange = { viewModel.setSelectorChapter(it) }
                )
            }
            LazyColumn(
                Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (uiState.selectorQas.isEmpty()) {
                    item {
                        Text(
                            stringResource(R.string.no_qa),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(24.dp)
                        )
                    }
                } else {
                    items(uiState.selectorQas, key = { it.id }) { qa ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.toggleQaSelection(qa.id) }
                                .padding(vertical = 8.dp, horizontal = 4.dp)
                                .border(
                                    1.dp,
                                    if (qa.id in uiState.selectedQaIds) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                    MaterialTheme.shapes.medium
                                )
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = qa.id in uiState.selectedQaIds,
                                onCheckedChange = { viewModel.toggleQaSelection(qa.id) }
                            )
                            Column(Modifier.weight(1f)) {
                                Text(
                                    qa.questionText.take(120).let { if (qa.questionText.length > 120) "$it…" else it },
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                qa.subject?.let { subj ->
                                    Text(
                                        subj + (qa.chapter?.let { " · $it" } ?: ""),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GoalDropdown(
    goals: List<Goal>,
    selectedGoalId: Long?,
    onSelected: (Long?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedGoal = goals.find { it.id == selectedGoalId }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = selectedGoal?.name ?: stringResource(R.string.filter_all),
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.goal)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.filter_all)) },
                onClick = {
                    onSelected(null)
                    expanded = false
                }
            )
            goals.forEach { goal ->
                DropdownMenuItem(
                    text = { Text(goal.name) },
                    onClick = {
                        onSelected(goal.id)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SubjectChapterDropdowns(
    subjects: List<String>,
    chapters: List<String>,
    subject: String,
    chapter: String,
    onSubjectChange: (String) -> Unit,
    onChapterChange: (String) -> Unit
) {
    var subjExpanded by remember { mutableStateOf(false) }
    var chExpanded by remember { mutableStateOf(false) }

    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
    ExposedDropdownMenuBox(
        expanded = subjExpanded,
        onExpandedChange = { subjExpanded = it },
        modifier = Modifier.weight(1f)
    ) {
        OutlinedTextField(
            value = subject.ifBlank { stringResource(R.string.filter_all_subjects) },
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.subject)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = subjExpanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )
        ExposedDropdownMenu(
            expanded = subjExpanded,
            onDismissRequest = { subjExpanded = false }
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.filter_all_subjects)) },
                onClick = {
                    onSubjectChange("")
                    subjExpanded = false
                }
            )
            subjects.forEach { s ->
                DropdownMenuItem(
                    text = { Text(s) },
                    onClick = {
                        onSubjectChange(s)
                        subjExpanded = false
                    }
                )
            }
        }
    }

    ExposedDropdownMenuBox(
        expanded = chExpanded,
        onExpandedChange = { chExpanded = it },
        modifier = Modifier.weight(1f)
    ) {
        OutlinedTextField(
            value = chapter.ifBlank { stringResource(R.string.filter_all) },
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.chapter)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = chExpanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )
        ExposedDropdownMenu(
            expanded = chExpanded,
            onDismissRequest = { chExpanded = false }
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.filter_all)) },
                onClick = {
                    onChapterChange("")
                    chExpanded = false
                }
            )
            chapters.forEach { c ->
                DropdownMenuItem(
                    text = { Text(c) },
                    onClick = {
                        onChapterChange(c)
                        chExpanded = false
                    }
                )
            }
        }
    }
    }
}
