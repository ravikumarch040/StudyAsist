package com.studyasist.wear

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.studyasist.wear.ui.PomodoroScreen
import com.studyasist.wear.ui.StreakScreen
import com.studyasist.wear.ui.theme.WearTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WearTheme {
                WearApp()
            }
        }
    }
}

@Composable
private fun WearApp() {
    var showStreak by remember { mutableStateOf(false) }
    when {
        showStreak -> StreakScreen(onBack = { showStreak = false })
        else -> PomodoroWithStreakEntry(onOpenStreak = { showStreak = true })
    }
}

@Composable
private fun PomodoroWithStreakEntry(onOpenStreak: () -> Unit) {
    PomodoroScreen(onOpenStreak = onOpenStreak)
}
