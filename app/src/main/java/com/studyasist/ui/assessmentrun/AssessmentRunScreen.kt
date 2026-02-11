package com.studyasist.ui.assessmentrun

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.NavigateBefore
import androidx.compose.material.icons.filled.NavigateNext
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.studyasist.R
import com.studyasist.data.local.entity.QuestionType
import com.studyasist.data.local.entity.QA
import org.json.JSONArray

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssessmentRunScreen(
    viewModel: AssessmentRunViewModel,
    onBack: () -> Unit,
    onSubmitted: (Long) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isSubmitted, uiState.attemptId) {
        if (uiState.isSubmitted && uiState.attemptId != null) {
            onSubmitted(uiState.attemptId!!)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(uiState.assessmentTitle.take(30) + if (uiState.assessmentTitle.length > 30) "…" else "")
                        if (uiState.isStarted && !uiState.isSubmitted) {
                            Text(
                                formatTime(uiState.remainingSeconds),
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                },
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
        if (uiState.isLoading) {
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Loading…", style = MaterialTheme.typography.bodyLarge)
            }
            return@Scaffold
        }

        if (uiState.questions.isEmpty()) {
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    uiState.errorMessage ?: "No questions in this assessment",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            return@Scaffold
        }

        if (!uiState.isStarted) {
            StartScreen(
                questionCount = uiState.questions.size,
                timeMinutes = (uiState.totalTimeSeconds / 60).toInt(),
                onStart = { viewModel.startAttempt() },
                onBack = onBack,
                modifier = Modifier.padding(paddingValues)
            )
            return@Scaffold
        }

        val current = uiState.questions.getOrNull(uiState.currentIndex) ?: return@Scaffold
        val scrollState = rememberScrollState()

        Column(
            Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text(
                "Question ${uiState.currentIndex + 1} of ${uiState.questions.size}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Column(
                Modifier
                    .weight(1f)
                    .verticalScroll(scrollState)
            ) {
                QuestionCard(
                    qa = current.qa,
                    userAnswer = current.userAnswer,
                    onAnswerChange = { viewModel.updateAnswer(uiState.currentIndex, it) }
                )
            }
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = { viewModel.prevQuestion() },
                    enabled = uiState.currentIndex > 0
                ) {
                    Icon(Icons.Default.NavigateBefore, contentDescription = "Previous")
                }
                if (uiState.currentIndex < uiState.questions.size - 1) {
                    IconButton(onClick = { viewModel.nextQuestion() }) {
                        Icon(Icons.Default.NavigateNext, contentDescription = "Next")
                    }
                } else {
                    Button(onClick = { viewModel.submitAnswers() }) {
                        Text(stringResource(R.string.submit))
                    }
                }
            }
        }
    }
}

@Composable
private fun StartScreen(
    questionCount: Int,
    timeMinutes: Int,
    onStart: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "$questionCount questions",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            "Time limit: $timeMinutes minutes",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp)
        )
        Button(
            onClick = onStart,
            modifier = Modifier.padding(top = 24.dp)
        ) {
            Text(stringResource(R.string.start))
        }
    }
}

@Composable
private fun QuestionCard(
    qa: QA,
    userAnswer: String,
    onAnswerChange: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                qa.questionText,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            when (qa.questionType) {
                QuestionType.MCQ -> McqOptions(
                    options = parseOptions(qa.optionsJson),
                    modelAnswer = qa.answerText,
                    selected = userAnswer,
                    onSelect = onAnswerChange
                )
                QuestionType.TRUE_FALSE -> TrueFalseOptions(
                    selected = userAnswer,
                    onSelect = onAnswerChange
                )
                else -> OutlinedTextField(
                    value = userAnswer,
                    onValueChange = onAnswerChange,
                    label = { Text(stringResource(R.string.answer)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    maxLines = if (qa.questionType == QuestionType.SHORT || qa.questionType == QuestionType.ESSAY) 4 else 1
                )
            }
        }
    }
}

@Composable
private fun McqOptions(
    options: List<String>,
    modelAnswer: String,
    selected: String,
    onSelect: (String) -> Unit
) {
    if (options.isEmpty()) {
        OutlinedTextField(
            value = selected,
            onValueChange = onSelect,
            label = { Text(stringResource(R.string.answer)) },
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
        )
        return
    }
    val letters = listOf("a", "b", "c", "d")
    Column(Modifier.padding(top = 12.dp)) {
        options.forEachIndexed { index, opt ->
            val letter = letters.getOrNull(index) ?: ""
            Row(
                Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = selected.equals(letter, ignoreCase = true) || selected.equals(opt, ignoreCase = true),
                        onClick = { onSelect(letter) }
                    )
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = selected.equals(letter, ignoreCase = true) || selected.equals(opt, ignoreCase = true),
                    onClick = { onSelect(letter) }
                )
                Text("$letter) $opt", Modifier.padding(start = 8.dp))
            }
        }
    }
}

@Composable
private fun TrueFalseOptions(
    selected: String,
    onSelect: (String) -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(top = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            Modifier
                .selectable(selected = selected.equals("true", ignoreCase = true), onClick = { onSelect("true") }),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = selected.equals("true", ignoreCase = true),
                onClick = { onSelect("true") }
            )
            Text("True", Modifier.padding(start = 8.dp))
        }
        Row(
            Modifier
                .selectable(selected = selected.equals("false", ignoreCase = true), onClick = { onSelect("false") }),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = selected.equals("false", ignoreCase = true),
                onClick = { onSelect("false") }
            )
            Text("False", Modifier.padding(start = 8.dp))
        }
    }
}

private fun parseOptions(json: String?): List<String> {
    if (json.isNullOrBlank()) return emptyList()
    return try {
        val arr = JSONArray(json)
        (0 until arr.length()).mapNotNull { i ->
            when (val v = arr.get(i)) {
                is String -> v
                else -> v.toString()
            }
        }
    } catch (_: Exception) {
        emptyList()
    }
}

private fun formatTime(seconds: Long): String {
    val m = seconds / 60
    val s = seconds % 60
    return "%d:%02d".format(m, s)
}
