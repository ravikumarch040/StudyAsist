package com.studyasist.ui.assessmentcreate

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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

@OptIn(ExperimentalMaterial3Api::class)
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
                Text("By goal", Modifier.padding(end = 16.dp))
                RadioButton(
                    selected = uiState.sourceMode == SourceMode.BY_SUBJECT_CHAPTER,
                    onClick = { viewModel.setSourceMode(SourceMode.BY_SUBJECT_CHAPTER) }
                )
                Text("By subject/chapter")
            }

            if (uiState.sourceMode == SourceMode.BY_GOAL) {
                GoalDropdown(
                    goals = uiState.availableGoals,
                    selectedGoalId = uiState.selectedGoalId,
                    onSelected = { viewModel.setGoalId(it) }
                )
            } else {
                SubjectChapterDropdowns(
                    subjects = uiState.distinctSubjects,
                    chapters = uiState.distinctChapters,
                    subject = uiState.subject,
                    chapter = uiState.chapter,
                    onSubjectChange = { viewModel.setSubject(it) },
                    onChapterChange = { viewModel.setChapter(it) }
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
                OutlinedTextField(
                    value = uiState.questionCount.toString(),
                    onValueChange = { viewModel.setQuestionCount(it.toIntOrNull() ?: 10) },
                    label = { Text(stringResource(R.string.questions)) },
                    modifier = Modifier.weight(1f)
                )
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
                Text("Randomize questions", style = MaterialTheme.typography.bodyMedium)
                Switch(
                    checked = uiState.randomize,
                    onCheckedChange = { viewModel.setRandomize(it) }
                )
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(Modifier.padding(12.dp)) {
                    Text(
                        "${uiState.availableCount} questions available",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
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
                Text(if (uiState.isLoading) "…" else stringResource(R.string.create))
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

    ExposedDropdownMenuBox(
        expanded = subjExpanded,
        onExpandedChange = { subjExpanded = it }
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
        onExpandedChange = { chExpanded = it }
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
