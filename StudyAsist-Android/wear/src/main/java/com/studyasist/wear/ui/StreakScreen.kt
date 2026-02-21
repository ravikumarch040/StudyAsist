package com.studyasist.wear.ui

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.TimeText
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.tasks.await

private const val PATH_STREAK = "/studyasist/streak"
private const val KEY_STREAK_DAYS = "streak_days"

@Composable
fun StreakScreen(
    onBack: () -> Unit,
) {
    var streakDays by remember { mutableStateOf<Int?>(null) }
    val context = LocalContext.current
    val dataClient: DataClient = remember { Wearable.getDataClient(context) }

    LaunchedEffect(Unit) {
        // Initial fetch
        try {
            val uri = Uri.parse("wear://*/$PATH_STREAK")
            val results = dataClient.getDataItems(uri).await()
            val item = results.firstOrNull()
            if (item != null) {
                val dataMapItem = DataMapItem.fromDataItem(item)
                streakDays = dataMapItem.dataMap.getInt(KEY_STREAK_DAYS, 0)
            } else {
                streakDays = 0
            }
            results.release()
        } catch (_: Exception) {
            streakDays = 0
        }

        // Listen for updates from phone
        val listener = DataClient.OnDataChangedListener { events ->
            for (i in 0 until events.count) {
                val event = events.get(i)
                if (event.type == DataEvent.TYPE_CHANGED && event.dataItem.uri.path == PATH_STREAK) {
                    val dataMapItem = DataMapItem.fromDataItem(event.dataItem)
                    streakDays = dataMapItem.dataMap.getInt(KEY_STREAK_DAYS, 0)
                }
            }
        }
        dataClient.addListener(listener)
        try {
            kotlinx.coroutines.awaitCancellation()
        } finally {
            dataClient.removeListener(listener)
        }
    }

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
                text = when (val n = streakDays) {
                    null -> "Sync with phone"
                    else -> "$n days"
                },
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
