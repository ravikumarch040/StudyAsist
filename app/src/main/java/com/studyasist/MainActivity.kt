package com.studyasist

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.activity.viewModels
import androidx.lifecycle.viewmodel.compose.viewModel
import com.studyasist.ui.MainViewModel
import com.studyasist.ui.navigation.AppNavGraph
import com.studyasist.ui.theme.AppTheme
import com.studyasist.ui.theme.StudyAsistTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val mainViewModel: MainViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        mainViewModel.setPendingGoalIdFromIntent(intent)
        setContent {
            val darkMode by mainViewModel.darkModeFlow.collectAsState(initial = "system")
            val themeId by mainViewModel.themeIdFlow.collectAsState(initial = "MINIMAL_LIGHT")
            val isSystemDark = isSystemInDarkTheme()
            val darkTheme = when (darkMode) {
                "dark" -> true
                "light" -> false
                else -> isSystemDark
            }
            val appTheme = try { AppTheme.valueOf(themeId) } catch (_: Exception) { AppTheme.MINIMAL_LIGHT }
            StudyAsistTheme(appTheme = appTheme, darkTheme = darkTheme) {
                val permissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) { }
                LaunchedEffect(Unit) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavGraph(
                        pendingGoalIdFlow = mainViewModel.pendingGoalIdForDeepLink,
                        onPendingGoalIdConsumed = mainViewModel::clearPendingGoalId,
                        userNameFlow = mainViewModel.userNameFlow,
                        profilePicUriFlow = mainViewModel.profilePicUriFlow
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        mainViewModel.setPendingGoalIdFromIntent(intent)
    }
}
