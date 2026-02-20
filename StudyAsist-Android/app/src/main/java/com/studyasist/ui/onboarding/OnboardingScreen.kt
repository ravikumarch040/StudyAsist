package com.studyasist.ui.onboarding

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.studyasist.R
import kotlinx.coroutines.launch

data class OnboardingPage(
    val icon: ImageVector,
    val titleRes: Int,
    val descriptionRes: Int
)

private val featurePages = listOf(
    OnboardingPage(Icons.Default.CalendarMonth, R.string.onboarding_timetable_title, R.string.onboarding_timetable_desc),
    OnboardingPage(Icons.Default.CameraAlt, R.string.onboarding_scan_title, R.string.onboarding_scan_desc),
    OnboardingPage(Icons.Default.Assessment, R.string.onboarding_assess_title, R.string.onboarding_assess_desc),
    OnboardingPage(Icons.Default.TrackChanges, R.string.onboarding_goal_title, R.string.onboarding_goal_desc)
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel,
    onComplete: (OnboardingResult) -> Unit
) {
    val totalPages = featurePages.size + 1 + 1 + 1 // features + account + backup + name

    val pagerState = rememberPagerState(pageCount = { totalPages })
    val scope = rememberCoroutineScope()
    var userName by remember { mutableStateOf("") }
    var backupTarget by remember { mutableStateOf("folder") }
    var backupAuto by remember { mutableStateOf(false) }
    var signInTriggeredFromBackup by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    val accountSignedIn by viewModel.accountSignedIn.collectAsState(initial = false)
    val driveSignedIn by viewModel.driveSignedIn.collectAsState(initial = false)
    val signInResult by viewModel.signInResult.collectAsState(initial = null)

    LaunchedEffect(accountSignedIn) {
        if (accountSignedIn && userName.isBlank()) {
            userName = viewModel.getGoogleDisplayName() ?: ""
        }
    }

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        viewModel.onGoogleSignInResult(result.resultCode)
        if (signInTriggeredFromBackup) {
            if (result.resultCode == android.app.Activity.RESULT_OK) {
                backupTarget = "google_drive"
            }
            signInTriggeredFromBackup = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            val lastPage = totalPages - 1
            if (pagerState.currentPage < lastPage) {
                TextButton(onClick = {
                    val defaultName = if (accountSignedIn) viewModel.getGoogleDisplayName() ?: "" else ""
                    onComplete(OnboardingResult(
                        userName = defaultName.ifBlank { userName },
                        backupTarget = backupTarget,
                        backupAuto = backupAuto,
                        signedInWithGoogle = accountSignedIn
                    ))
                }) {
                    Text(stringResource(R.string.onboarding_skip))
                }
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { page ->
            when {
                page < featurePages.size -> OnboardingPageContent(featurePages[page])
                page == featurePages.size -> OnboardingAccountPage(
                    accountSignedIn = accountSignedIn,
                    signInResult = signInResult,
                    onSignInClick = { googleSignInLauncher.launch(viewModel.getGoogleSignInIntent()) },
                    onClearResult = { viewModel.clearSignInResult() }
                )
                page == featurePages.size + 1 -> {
                    LaunchedEffect(Unit) { viewModel.refreshDriveSignInState() }
                    OnboardingBackupPage(
                        backupTarget = backupTarget,
                        backupAuto = backupAuto,
                        driveSignedIn = driveSignedIn,
                        onTargetChange = { backupTarget = it },
                        onAutoChange = { backupAuto = it },
                        onRequestGoogleSignIn = {
                            signInTriggeredFromBackup = true
                            googleSignInLauncher.launch(viewModel.getGoogleSignInIntent())
                        }
                    )
                }
                else -> NameCollectionPage(
                    userName = userName,
                    onUserNameChange = { userName = it },
                    signedInWithGoogle = accountSignedIn,
                    onDone = { focusManager.clearFocus() }
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            repeat(totalPages) { index ->
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .size(if (pagerState.currentPage == index) 10.dp else 8.dp)
                        .clip(CircleShape)
                        .background(
                            if (pagerState.currentPage == index)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.outlineVariant
                        )
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        val isLastPage = pagerState.currentPage == totalPages - 1
        AnimatedVisibility(
            visible = isLastPage,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Button(
                onClick = {
                    onComplete(OnboardingResult(
                        userName = userName.ifBlank { viewModel.getGoogleDisplayName() ?: "" },
                        backupTarget = backupTarget,
                        backupAuto = backupAuto,
                        signedInWithGoogle = accountSignedIn
                    ))
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text(
                    stringResource(R.string.onboarding_get_started),
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }

        AnimatedVisibility(
            visible = !isLastPage,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Button(
                onClick = {
                    scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text(
                    stringResource(R.string.onboarding_next),
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

@Composable
private fun OnboardingAccountPage(
    accountSignedIn: Boolean,
    signInResult: String?,
    onSignInClick: () -> Unit,
    onClearResult: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier.size(56.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        Spacer(Modifier.height(40.dp))

        Text(
            text = stringResource(R.string.onboarding_account_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.onboarding_account_desc),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(Modifier.height(24.dp))

        if (accountSignedIn) {
            Text(
                stringResource(R.string.signed_in_success),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        } else {
            Button(onClick = onSignInClick, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.sign_in_with_google))
            }
        }

        signInResult?.let { msg ->
            if (msg != "success") {
                Spacer(Modifier.height(8.dp))
                Text(
                    msg,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                TextButton(onClick = onClearResult) {
                    Text(stringResource(R.string.dismiss))
                }
            }
        }
    }
}

@Composable
private fun OnboardingBackupPage(
    backupTarget: String,
    backupAuto: Boolean,
    driveSignedIn: Boolean,
    onTargetChange: (String) -> Unit,
    onAutoChange: (Boolean) -> Unit,
    onRequestGoogleSignIn: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Backup,
                contentDescription = null,
                modifier = Modifier.size(56.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        Spacer(Modifier.height(40.dp))

        Text(
            text = stringResource(R.string.onboarding_backup_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.onboarding_backup_desc),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = backupTarget == "google_drive",
                onClick = {
                    if (driveSignedIn) {
                        onTargetChange("google_drive")
                    } else {
                        onRequestGoogleSignIn()
                    }
                },
                label = { Text(stringResource(R.string.onboarding_backup_google_drive)) }
            )
            FilterChip(
                selected = backupTarget == "folder",
                onClick = { onTargetChange("folder") },
                label = { Text(stringResource(R.string.onboarding_backup_later)) }
            )
        }

        if (backupTarget == "google_drive") {
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(stringResource(R.string.onboarding_backup_auto), modifier = Modifier.weight(1f))
                Switch(
                    checked = backupAuto,
                    onCheckedChange = onAutoChange
                )
            }
        }
    }
}

@Composable
private fun OnboardingPageContent(page: OnboardingPage) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = page.icon,
                contentDescription = null,
                modifier = Modifier.size(56.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        Spacer(Modifier.height(40.dp))

        Text(
            text = stringResource(page.titleRes),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(Modifier.height(16.dp))

        Text(
            text = stringResource(page.descriptionRes),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

@Composable
private fun NameCollectionPage(
    userName: String,
    onUserNameChange: (String) -> Unit,
    signedInWithGoogle: Boolean,
    onDone: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = if (signedInWithGoogle) stringResource(R.string.onboarding_ready) else stringResource(R.string.onboarding_welcome),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(Modifier.height(16.dp))

        Text(
            text = if (signedInWithGoogle) stringResource(R.string.onboarding_ready_desc) else stringResource(R.string.onboarding_enter_name),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = userName,
            onValueChange = onUserNameChange,
            label = { Text(stringResource(R.string.onboarding_name_hint)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { onDone() }),
            modifier = Modifier.fillMaxWidth()
        )
    }
}
