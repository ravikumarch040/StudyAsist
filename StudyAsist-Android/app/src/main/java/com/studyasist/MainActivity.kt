package com.studyasist

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import com.studyasist.auth.AppleSignInHelper
import com.studyasist.auth.AppleSignInResultHolder
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.activity.viewModels
import com.studyasist.ui.MainViewModel
import com.studyasist.ui.navigation.AppNavGraph
import com.studyasist.ui.onboarding.OnboardingResult
import com.studyasist.ui.onboarding.OnboardingScreen
import com.studyasist.ui.onboarding.OnboardingViewModel
import com.studyasist.ui.ConditionalHapticFeedback
import com.studyasist.ui.theme.AppTheme
import com.studyasist.ui.theme.StudyAsistTheme
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject lateinit var authRepository: com.studyasist.data.repository.AuthRepository
    @Inject lateinit var appleSignInResultHolder: AppleSignInResultHolder

    private val mainViewModel: MainViewModel by viewModels()
    private val onboardingViewModel: OnboardingViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        mainViewModel.setPendingGoalIdFromIntent(intent)
        processAppleSignInIntent(intent)
        setContent {
            val darkMode by mainViewModel.darkModeFlow.collectAsState(initial = "system")
            val themeId by mainViewModel.themeIdFlow.collectAsState(initial = "MINIMAL_LIGHT")
            val hapticEnabled by mainViewModel.hapticEnabledFlow.collectAsState(initial = true)
            val highContrastMode by mainViewModel.highContrastModeFlow.collectAsState(initial = false)
            val colorBlindMode by mainViewModel.colorBlindModeFlow.collectAsState(initial = false)
            val fontScale by mainViewModel.fontScaleFlow.collectAsState(initial = 1.0f)
            val onboardingCompleted by mainViewModel.onboardingCompletedFlow.collectAsState(initial = true)
            val isSystemDark = isSystemInDarkTheme()
            val darkTheme = when (darkMode) {
                "dark" -> true
                "light" -> false
                else -> isSystemDark
            }
            val baseTheme = try { AppTheme.valueOf(themeId) } catch (_: Exception) { AppTheme.MINIMAL_LIGHT }
            val appTheme = if (highContrastMode || colorBlindMode) AppTheme.DARK_HIGH_CONTRAST else baseTheme
            val scope = rememberCoroutineScope()
            var showOnboarding by remember { mutableStateOf(!onboardingCompleted) }

            LaunchedEffect(onboardingCompleted) {
                showOnboarding = !onboardingCompleted
            }

            val defaultDensity = LocalDensity.current
            val platformHaptic = LocalHapticFeedback.current
            CompositionLocalProvider(
                LocalDensity provides Density(density = defaultDensity.density, fontScale = fontScale),
                LocalHapticFeedback provides ConditionalHapticFeedback(hapticEnabled, platformHaptic)
            ) {
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
                    if (showOnboarding) {
                        OnboardingScreen(
                            viewModel = onboardingViewModel,
                            onComplete = { result ->
                                scope.launch {
                                    onboardingViewModel.completeOnboarding(result)
                                    showOnboarding = false
                                }
                            }
                        )
                    } else {
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
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        mainViewModel.setPendingGoalIdFromIntent(intent)
        processAppleSignInIntent(intent)
    }

    private fun processAppleSignInIntent(intent: Intent?) {
        val idToken = AppleSignInHelper.extractIdTokenFromIntent(intent) ?: return
        lifecycleScope.launch {
            when (val r = authRepository.loginWithApple(idToken)) {
                is com.studyasist.data.repository.AuthResult.Success ->
                    appleSignInResultHolder.setResult(getString(R.string.signed_in_success))
                is com.studyasist.data.repository.AuthResult.Error ->
                    appleSignInResultHolder.setResult(r.message)
            }
        }
    }
}
