package com.studyasist.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.hilt.navigation.compose.hiltViewModel
import com.studyasist.ui.activityedit.ActivityEditScreen
import com.studyasist.ui.activityedit.ActivityEditViewModel
import com.studyasist.ui.home.HomeScreen
import com.studyasist.ui.home.HomeViewModel
import com.studyasist.ui.settings.SettingsScreen
import com.studyasist.ui.settings.SettingsViewModel
import com.studyasist.ui.timetabledetail.TimetableDetailScreen
import com.studyasist.ui.timetabledetail.TimetableDetailViewModel
import com.studyasist.ui.timetablelist.TimetableListScreen
import com.studyasist.ui.timetablelist.TimetableListViewModel

@Composable
fun AppNavGraph(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = NavRoutes.HOME
    ) {
        composable(NavRoutes.HOME) {
            val homeViewModel: HomeViewModel = hiltViewModel()
            val listViewModel: TimetableListViewModel = hiltViewModel()
            HomeScreen(
                viewModel = homeViewModel,
                listViewModel = listViewModel,
                onSettingsClick = { navController.navigate(NavRoutes.SETTINGS) },
                onTimetableClick = { id ->
                    navController.navigate(NavRoutes.timetableDetail(id))
                },
                onAddActivity = { id ->
                    navController.navigate(NavRoutes.activityAdd(id, 1))
                },
                onNavigateAfterCreate = { id ->
                    navController.navigate(NavRoutes.timetableDetail(id)) {
                        popUpTo(NavRoutes.HOME) { inclusive = false }
                    }
                }
            )
        }

        composable(NavRoutes.TIMETABLE_LIST) {
            val viewModel: TimetableListViewModel = hiltViewModel()
            TimetableListScreen(
                viewModel = viewModel,
                onTimetableClick = { id ->
                    navController.navigate(NavRoutes.timetableDetail(id))
                },
                onSettingsClick = { navController.navigate(NavRoutes.SETTINGS) },
                onNavigateAfterCreate = { id ->
                    navController.navigate(NavRoutes.timetableDetail(id)) {
                        popUpTo(NavRoutes.TIMETABLE_LIST) { inclusive = false }
                    }
                },
                showTopBar = true
            )
        }

        composable(
            route = NavRoutes.TIMETABLE_DETAIL,
            arguments = listOf(navArgument("timetableId") { type = NavType.LongType })
        ) {
            val viewModel: TimetableDetailViewModel = hiltViewModel()
            TimetableDetailScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onAddActivity = { tid, day ->
                    navController.navigate(NavRoutes.activityAdd(tid, day))
                },
                onEditActivity = { tid, activityId ->
                    navController.navigate(NavRoutes.activityEdit(tid, activityId))
                }
            )
        }

        composable(
            route = NavRoutes.ACTIVITY_ADD,
            arguments = listOf(
                navArgument("timetableId") { type = NavType.LongType },
                navArgument("dayOfWeek") { type = NavType.IntType }
            )
        ) {
            val viewModel: ActivityEditViewModel = hiltViewModel()
            ActivityEditScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() }
            )
        }

        composable(
            route = "activity_edit/{timetableId}/{activityId}",
            arguments = listOf(
                navArgument("timetableId") { type = NavType.LongType },
                navArgument("activityId") { type = NavType.LongType }
            )
        ) {
            val viewModel: ActivityEditViewModel = hiltViewModel()
            ActivityEditScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() }
            )
        }

        composable(NavRoutes.SETTINGS) {
            val viewModel: SettingsViewModel = hiltViewModel()
            SettingsScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
