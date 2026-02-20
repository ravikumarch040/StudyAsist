package com.studyasist.wear.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.TimeText

@Composable
fun StreakScreen(
    onBack: () -> Unit,
) {
    // TODO: Sync streak via Wearable Data Layer from phone
    val streakDays = remember { 0 }

    AppScaffold(
        timeText = { TimeText() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Streak",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "$streakDays days",
                style = MaterialTheme.typography.displaySmall
            )
            Button(
                onClick = onBack,
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text("Back")
            }
        }
    }
}
