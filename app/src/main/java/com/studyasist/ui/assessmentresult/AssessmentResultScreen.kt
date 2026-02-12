package com.studyasist.ui.assessmentresult

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.studyasist.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssessmentResultScreen(
    viewModel: AssessmentResultViewModel,
    onBack: () -> Unit,
    onRevise: (subject: String?, chapter: String?) -> Unit = { _, _ -> }
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.result)) },
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
                        "%.0f%%".format(uiState.percent),
                        style = MaterialTheme.typography.displayMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        "Score: %.0f / %.0f".format(uiState.score, uiState.maxScore),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Button(
                        onClick = {
                            val sc = uiState.subjectChapter
                            onRevise(sc?.subject, sc?.chapter)
                        },
                        modifier = Modifier.padding(top = 16.dp)
                    ) {
                        Icon(Icons.Default.MenuBook, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                        Text(stringResource(R.string.revise))
                    }
                }
            }

            Text(
                stringResource(R.string.details),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            uiState.details.forEachIndexed { index, item ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (item.correct) {
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        } else {
                            MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                        }
                    )
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                if (item.correct) "✓" else "✗",
                                style = MaterialTheme.typography.titleMedium,
                                color = if (item.correct) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                            )
                            Text(
                                "#${index + 1}",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                        if (item.questionText.isNotBlank()) {
                            Text(
                                item.questionText,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                        if (item.userAnswer != null) {
                            Text(
                                "Your answer: ${item.userAnswer}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            "Correct: ${item.modelAnswer}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
