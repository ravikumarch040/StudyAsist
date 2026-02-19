package com.studyasist.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.EmojiObjects
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material3.DrawerState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.studyasist.R
import com.studyasist.ui.navigation.NavRoutes
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

data class DrawerItem(
    val route: String,
    val labelRes: Int,
    val icon: ImageVector
)

@Composable
fun AppDrawerContent(
    currentRoute: String?,
    drawerState: DrawerState,
    userName: Flow<String>,
    profilePicUri: Flow<String?>,
    onNavigate: (String) -> Unit
) {
    val scope = rememberCoroutineScope()
    val name by userName.collectAsState(initial = "")
    val picUri by profilePicUri.collectAsState(initial = null)

    ModalDrawerSheet {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ProfileAvatar(
                    userName = name,
                    profilePicUri = picUri,
                    size = 72.dp
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = name.ifBlank { stringResource(R.string.profile_guest) },
                    style = MaterialTheme.typography.titleMedium
                )
            }

            HorizontalDivider(Modifier.padding(vertical = 8.dp))

            val mainItems = listOf(
                DrawerItem(NavRoutes.HOME, R.string.drawer_dashboard, Icons.Default.Dashboard),
                DrawerItem(NavRoutes.TIMETABLE_LIST, R.string.drawer_timetables, Icons.Default.CalendarMonth)
            )
            mainItems.forEach { item ->
                NavigationDrawerItem(
                    icon = { Icon(item.icon, contentDescription = null) },
                    label = { Text(stringResource(item.labelRes)) },
                    selected = currentRoute == item.route,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onNavigate(item.route)
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
            }

            HorizontalDivider(Modifier.padding(vertical = 8.dp))

            Text(
                text = stringResource(R.string.drawer_study_tools),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 28.dp, vertical = 8.dp)
            )

            val studyItems = listOf(
                DrawerItem(NavRoutes.GOAL_LIST, R.string.drawer_exam_goals, Icons.Default.TrackChanges),
                DrawerItem(NavRoutes.QA_BANK, R.string.drawer_qa_bank, Icons.Default.Quiz),
                DrawerItem(NavRoutes.ASSESSMENT_LIST, R.string.drawer_assessments, Icons.Default.Assessment),
                DrawerItem(NavRoutes.RESULT_LIST, R.string.drawer_results, Icons.Default.Star),
                DrawerItem(NavRoutes.DICTATE, R.string.drawer_dictate, Icons.Default.Mic),
                DrawerItem(NavRoutes.EXPLAIN, R.string.drawer_explain, Icons.Default.AutoStories),
                DrawerItem(NavRoutes.SOLVE, R.string.drawer_solve, Icons.Default.EmojiObjects)
            )
            studyItems.forEach { item ->
                NavigationDrawerItem(
                    icon = { Icon(item.icon, contentDescription = null) },
                    label = { Text(stringResource(item.labelRes)) },
                    selected = currentRoute == item.route,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onNavigate(item.route)
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
            }

            HorizontalDivider(Modifier.padding(vertical = 8.dp))

            NavigationDrawerItem(
                icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                label = { Text(stringResource(R.string.settings)) },
                selected = currentRoute == NavRoutes.SETTINGS,
                onClick = {
                    scope.launch { drawerState.close() }
                    onNavigate(NavRoutes.SETTINGS)
                },
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
            )
        }
    }
}
