package com.studyasist.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.hilt.navigation.compose.hiltViewModel
import com.studyasist.R
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
import com.studyasist.ui.dictate.DictateScreen
import com.studyasist.ui.dictate.DictateViewModel
import com.studyasist.ui.explain.ExplainScreen
import com.studyasist.ui.explain.ExplainViewModel
import com.studyasist.ui.solve.SolveScreen
import com.studyasist.ui.solve.SolveViewModel
import com.studyasist.ui.goallist.GoalListScreen
import com.studyasist.ui.goallist.GoalListViewModel
import com.studyasist.ui.goaldetail.GoalDetailScreen
import com.studyasist.ui.goaldetail.GoalDetailViewModel
import com.studyasist.ui.goaledit.GoalEditScreen
import com.studyasist.ui.goaledit.GoalEditViewModel
import com.studyasist.ui.qabank.QABankScreen
import com.studyasist.ui.qabank.QABankViewModel
import com.studyasist.ui.qascan.QAScanScreen
import com.studyasist.ui.qascan.QAScanViewModel
import com.studyasist.ui.addrevision.AddRevisionScreen
import com.studyasist.ui.addrevision.AddRevisionViewModel
import com.studyasist.ui.assessmentcreate.AssessmentCreateScreen
import com.studyasist.ui.assessmentcreate.AssessmentCreateViewModel
import com.studyasist.ui.assessmentrun.AssessmentRunScreen
import com.studyasist.ui.assessmentrun.AssessmentRunViewModel
import com.studyasist.ui.assessmentresult.AssessmentResultScreen
import com.studyasist.ui.assessmentresult.AssessmentResultViewModel
import com.studyasist.ui.assessmentlist.AssessmentListScreen
import com.studyasist.ui.assessmentlist.AssessmentListViewModel
import com.studyasist.ui.assessmentedit.AssessmentEditScreen
import com.studyasist.ui.assessmentedit.AssessmentEditViewModel
import com.studyasist.ui.resultlist.ResultListScreen
import com.studyasist.ui.resultlist.ResultListViewModel
import com.studyasist.ui.manualreview.ManualReviewListScreen
import com.studyasist.ui.manualreview.ManualReviewListViewModel
import com.studyasist.ui.manualreview.ManualOverrideScreen
import com.studyasist.ui.manualreview.ManualOverrideViewModel
import com.studyasist.ui.leaderboard.LeaderboardScreen
import com.studyasist.ui.leaderboard.LeaderboardViewModel

sealed class BottomNavItem(
    val route: String,
    val labelRes: Int,
    val icon: ImageVector
) {
    data object Home : BottomNavItem(NavRoutes.HOME, R.string.nav_home, Icons.Default.Dashboard)
    data object Timetable : BottomNavItem(NavRoutes.TIMETABLE_LIST, R.string.nav_timetable, Icons.Default.CalendarMonth)
    data object Study : BottomNavItem(NavRoutes.STUDY_HUB, R.string.nav_study, Icons.Default.AutoStories)
    data object Goals : BottomNavItem(NavRoutes.GOAL_LIST, R.string.nav_goals, Icons.Default.TrackChanges)
    data object More : BottomNavItem(NavRoutes.MORE_HUB, R.string.nav_more, Icons.Default.MoreHoriz)
}

private val bottomNavItems = listOf(
    BottomNavItem.Home,
    BottomNavItem.Timetable,
    BottomNavItem.Study,
    BottomNavItem.Goals,
    BottomNavItem.More
)

private val bottomNavRoutes = bottomNavItems.map { it.route }.toSet()

private val hideBottomBarRoutes = setOf(
    "activity_edit/{timetableId}/{activityId}",
    NavRoutes.ACTIVITY_ADD,
    NavRoutes.ASSESSMENT_RUN,
    NavRoutes.QA_SCAN,
    NavRoutes.USER_GUIDE
)

@Composable
fun AppNavGraph(
    navController: NavHostController = rememberNavController(),
    pendingGoalIdFlow: StateFlow<Long?>? = null,
    onPendingGoalIdConsumed: () -> Unit = {},
    userNameFlow: Flow<String> = flowOf(""),
    profilePicUriFlow: Flow<String?> = flowOf(null)
) {
    val pendingGoalId by (pendingGoalIdFlow ?: flowOf<Long?>(null)).collectAsState(initial = null)
    LaunchedEffect(pendingGoalId) {
        pendingGoalId?.let { goalId ->
            navController.navigate(NavRoutes.goalDetail(goalId)) {
                popUpTo(NavRoutes.HOME) { inclusive = false }
            }
            onPendingGoalIdConsumed()
        }
    }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = currentRoute !in hideBottomBarRoutes

    Scaffold(
        bottomBar = {
            AnimatedVisibility(
                visible = showBottomBar,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it })
            ) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        val selected = currentRoute == item.route ||
                            (item == BottomNavItem.Goals && currentRoute?.startsWith("goal_") == true) ||
                            (item == BottomNavItem.Timetable && currentRoute?.startsWith("timetable_") == true)
                        NavigationBarItem(
                            icon = { Icon(item.icon, contentDescription = stringResource(item.labelRes)) },
                            label = { Text(stringResource(item.labelRes)) },
                            selected = selected,
                            onClick = {
                                if (currentRoute != item.route) {
                                    navController.navigate(item.route) {
                                        popUpTo(NavRoutes.HOME) { inclusive = false }
                                        launchSingleTop = true
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = NavRoutes.HOME,
            modifier = Modifier.padding(PaddingValues(bottom = paddingValues.calculateBottomPadding()))
        ) {
            composable(NavRoutes.HOME) {
                val homeViewModel: HomeViewModel = hiltViewModel()
                HomeScreen(
                    viewModel = homeViewModel,
                    onTimetableClick = { id ->
                        navController.navigate(NavRoutes.timetableDetail(id))
                    },
                    onDictate = { navController.navigate(NavRoutes.DICTATE) },
                    onExplain = { navController.navigate(NavRoutes.EXPLAIN) },
                    onSolve = { navController.navigate(NavRoutes.SOLVE) },
                    onExamGoals = { navController.navigate(NavRoutes.GOAL_LIST) },
                    onQABank = { navController.navigate(NavRoutes.QA_BANK) },
                    onAssessments = { navController.navigate(NavRoutes.ASSESSMENT_LIST) },
                    onResults = { navController.navigate(NavRoutes.RESULT_LIST) },
                    onResultClick = { attemptId ->
                        navController.navigate(NavRoutes.assessmentResult(attemptId))
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
                    showTopBar = true,
                    onBack = { navController.popBackStack() }
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
                    onBack = { navController.popBackStack() },
                    onUserGuide = { navController.navigate(NavRoutes.USER_GUIDE) }
                )
            }

            composable(NavRoutes.USER_GUIDE) {
                com.studyasist.ui.userguide.UserGuideScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            // Study Hub - consolidates study tools
            composable(NavRoutes.STUDY_HUB) {
                StudyHubScreen(
                    onDictate = { navController.navigate(NavRoutes.DICTATE) },
                    onExplain = { navController.navigate(NavRoutes.EXPLAIN) },
                    onSolve = { navController.navigate(NavRoutes.SOLVE) },
                    onQABank = { navController.navigate(NavRoutes.QA_BANK) },
                    onAssessments = { navController.navigate(NavRoutes.ASSESSMENT_LIST) },
                    onResults = { navController.navigate(NavRoutes.RESULT_LIST) },
                    onDailyReview = { navController.navigate(NavRoutes.DAILY_REVIEW) },
                    onFlashcards = { navController.navigate(NavRoutes.FLASHCARD) },
                    onPomodoro = { navController.navigate(NavRoutes.POMODORO) }
                )
            }

            // More Hub - settings, results, etc.
            composable(NavRoutes.MORE_HUB) {
                MoreHubScreen(
                    onSettings = { navController.navigate(NavRoutes.SETTINGS) },
                    onResults = { navController.navigate(NavRoutes.RESULT_LIST) },
                    onManualReview = { navController.navigate(NavRoutes.MANUAL_REVIEW_LIST) },
                    onLeaderboard = { navController.navigate(NavRoutes.LEADERBOARD) },
                    onQABank = { navController.navigate(NavRoutes.QA_BANK) },
                    onAssessments = { navController.navigate(NavRoutes.ASSESSMENT_LIST) }
                )
            }

            composable(NavRoutes.LEADERBOARD) {
                val viewModel: LeaderboardViewModel = hiltViewModel()
                LeaderboardScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(NavRoutes.DICTATE) {
                val viewModel: DictateViewModel = hiltViewModel()
                DictateScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(NavRoutes.EXPLAIN) {
                val viewModel: ExplainViewModel = hiltViewModel()
                ExplainScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(NavRoutes.SOLVE) {
                val viewModel: SolveViewModel = hiltViewModel()
                SolveScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(NavRoutes.GOAL_LIST) {
                val viewModel: GoalListViewModel = hiltViewModel()
                GoalListScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onAddGoal = { navController.navigate(NavRoutes.GOAL_ADD) },
                    onGoalClick = { id -> navController.navigate(NavRoutes.goalDetail(id)) },
                    onQABank = { navController.navigate(NavRoutes.QA_BANK) }
                )
            }

            composable(
                route = NavRoutes.GOAL_DETAIL,
                arguments = listOf(navArgument("goalId") { type = NavType.LongType })
            ) {
                val viewModel: GoalDetailViewModel = hiltViewModel()
                GoalDetailScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onEditGoal = { id -> navController.navigate(NavRoutes.goalEdit(id)) },
                    onCreateAssessment = { goalId -> navController.navigate(NavRoutes.assessmentCreateForGoal(goalId)) },
                    onViewAssessments = { navController.navigate(NavRoutes.ASSESSMENT_LIST) },
                    onViewResults = { navController.navigate(NavRoutes.RESULT_LIST) },
                    onResultClick = { attemptId -> navController.navigate(NavRoutes.assessmentResult(attemptId)) },
                    onPracticeTopic = { subject, chapter ->
                        navController.navigate(NavRoutes.qaBankRevise(subject, chapter))
                    },
                    onAddToTimetable = { subject, chapter ->
                        navController.navigate(NavRoutes.addRevision(subject, chapter))
                    },
                    onQuickPractice = { assessmentId ->
                        navController.navigate(NavRoutes.assessmentRun(assessmentId))
                    }
                )
            }

            composable(NavRoutes.GOAL_ADD) {
                val viewModel: GoalEditViewModel = hiltViewModel()
                GoalEditScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onSaved = { id ->
                        navController.navigate(NavRoutes.goalDetail(id)) {
                            popUpTo(NavRoutes.GOAL_LIST) { inclusive = false }
                        }
                    }
                )
            }

            composable(
                route = NavRoutes.GOAL_EDIT,
                arguments = listOf(navArgument("goalId") { type = NavType.LongType })
            ) {
                val viewModel: GoalEditViewModel = hiltViewModel()
                GoalEditScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onSaved = { id ->
                        navController.navigate(NavRoutes.goalDetail(id)) {
                            popUpTo(NavRoutes.GOAL_LIST) { inclusive = false }
                        }
                    }
                )
            }

            composable(NavRoutes.QA_BANK) {
                val viewModel: QABankViewModel = hiltViewModel()
                QABankScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onScanClick = { navController.navigate(NavRoutes.QA_SCAN) },
                    onCreateAssessment = { navController.navigate(NavRoutes.ASSESSMENT_CREATE) },
                    onViewAssessments = { navController.navigate(NavRoutes.ASSESSMENT_LIST) },
                    onViewResults = { navController.navigate(NavRoutes.RESULT_LIST) }
                )
            }

            composable(
                route = NavRoutes.QA_BANK_REVISE,
                arguments = listOf(
                    navArgument("subject") { type = NavType.StringType; defaultValue = "" },
                    navArgument("chapter") { type = NavType.StringType; defaultValue = "" }
                )
            ) {
                val viewModel: QABankViewModel = hiltViewModel()
                QABankScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onScanClick = { navController.navigate(NavRoutes.QA_SCAN) },
                    onCreateAssessment = { navController.navigate(NavRoutes.ASSESSMENT_CREATE) },
                    onViewAssessments = { navController.navigate(NavRoutes.ASSESSMENT_LIST) },
                    onViewResults = { navController.navigate(NavRoutes.RESULT_LIST) }
                )
            }

            composable(NavRoutes.QA_SCAN) {
                val viewModel: QAScanViewModel = hiltViewModel()
                QAScanScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onSaved = { navController.popBackStack() }
                )
            }

            composable(
                route = NavRoutes.ADD_REVISION,
                arguments = listOf(
                    navArgument("subject") { type = NavType.StringType; defaultValue = "" },
                    navArgument("chapter") { type = NavType.StringType; defaultValue = "" }
                )
            ) {
                val viewModel: AddRevisionViewModel = hiltViewModel()
                AddRevisionScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onSaved = { navController.popBackStack() },
                    onOpenTimetables = {
                        navController.popBackStack()
                        navController.navigate(NavRoutes.TIMETABLE_LIST)
                    }
                )
            }

            composable(NavRoutes.ASSESSMENT_CREATE) {
                val viewModel: AssessmentCreateViewModel = hiltViewModel()
                AssessmentCreateScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onCreated = { id ->
                        navController.popBackStack()
                        navController.navigate(NavRoutes.assessmentRun(id))
                    }
                )
            }

            composable(
                route = NavRoutes.ASSESSMENT_CREATE_FOR_GOAL,
                arguments = listOf(navArgument("goalId") { type = NavType.LongType })
            ) {
                val viewModel: AssessmentCreateViewModel = hiltViewModel()
                AssessmentCreateScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onCreated = { id ->
                        navController.popBackStack()
                        navController.navigate(NavRoutes.assessmentRun(id))
                    }
                )
            }

            composable(
                route = NavRoutes.ASSESSMENT_RUN,
                arguments = listOf(navArgument("assessmentId") { type = NavType.LongType })
            ) {
                val viewModel: AssessmentRunViewModel = hiltViewModel()
                AssessmentRunScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onSubmitted = { attemptId ->
                        navController.popBackStack()
                        navController.navigate(NavRoutes.assessmentResult(attemptId))
                    }
                )
            }

            composable(
                route = NavRoutes.ASSESSMENT_RESULT,
                arguments = listOf(navArgument("attemptId") { type = NavType.LongType })
            ) { backStackEntry ->
                val attemptId = backStackEntry.arguments?.getLong("attemptId") ?: 0L
                val viewModel: AssessmentResultViewModel = hiltViewModel()
                AssessmentResultScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onRevise = { subject, chapter ->
                        if (subject.isNullOrBlank() && chapter.isNullOrBlank()) {
                            navController.navigate(NavRoutes.QA_BANK) {
                                popUpTo(NavRoutes.ASSESSMENT_RESULT) { inclusive = false }
                            }
                        } else {
                            navController.navigate(NavRoutes.qaBankRevise(subject, chapter)) {
                                popUpTo(NavRoutes.ASSESSMENT_RESULT) { inclusive = false }
                            }
                        }
                    },
                    onAddToTimetable = { subject, chapter ->
                        navController.navigate(NavRoutes.addRevision(subject, chapter))
                    },
                    onManualReview = { navController.navigate(NavRoutes.manualOverride(attemptId)) },
                    onRetry = { newAssessmentId ->
                        navController.popBackStack()
                        navController.navigate(NavRoutes.assessmentRun(newAssessmentId))
                    }
                )
            }

            composable(NavRoutes.ASSESSMENT_LIST) {
                val viewModel: AssessmentListViewModel = hiltViewModel()
                AssessmentListScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onAssessmentClick = { id -> navController.navigate(NavRoutes.assessmentRun(id)) },
                    onEditAssessment = { id -> navController.navigate(NavRoutes.assessmentEdit(id)) },
                    onCreateAssessment = { navController.navigate(NavRoutes.ASSESSMENT_CREATE) }
                )
            }

            composable(
                route = NavRoutes.ASSESSMENT_EDIT,
                arguments = listOf(navArgument("assessmentId") { type = NavType.LongType })
            ) {
                val viewModel: AssessmentEditViewModel = hiltViewModel()
                AssessmentEditScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onSaved = { navController.popBackStack() }
                )
            }

            composable(NavRoutes.RESULT_LIST) {
                val viewModel: ResultListViewModel = hiltViewModel()
                ResultListScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onResultClick = { attemptId -> navController.navigate(NavRoutes.assessmentResult(attemptId)) },
                    onManualReview = { navController.navigate(NavRoutes.MANUAL_REVIEW_LIST) }
                )
            }

            composable(NavRoutes.MANUAL_REVIEW_LIST) {
                val viewModel: ManualReviewListViewModel = hiltViewModel()
                ManualReviewListScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onItemClick = { attemptId ->
                        navController.navigate(NavRoutes.manualOverride(attemptId))
                    }
                )
            }

            composable(
                route = NavRoutes.MANUAL_OVERRIDE,
                arguments = listOf(navArgument("attemptId") { type = NavType.LongType })
            ) {
                val viewModel: ManualOverrideViewModel = hiltViewModel()
                ManualOverrideScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onSaved = { navController.popBackStack() }
                )
            }

            composable(NavRoutes.DAILY_REVIEW) {
                val viewModel: com.studyasist.ui.review.DailyReviewViewModel = hiltViewModel()
                com.studyasist.ui.review.DailyReviewScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(NavRoutes.FLASHCARD) {
                val viewModel: com.studyasist.ui.flashcard.FlashcardViewModel = hiltViewModel()
                com.studyasist.ui.flashcard.FlashcardScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(NavRoutes.POMODORO) {
                val viewModel: com.studyasist.ui.pomodoro.PomodoroViewModel = hiltViewModel()
                com.studyasist.ui.pomodoro.PomodoroScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(NavRoutes.TUTOR_CHAT) {
                val viewModel: com.studyasist.ui.tutor.TutorChatViewModel = hiltViewModel()
                com.studyasist.ui.tutor.TutorChatScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(NavRoutes.STUDY_PLAN) {
                val viewModel: com.studyasist.ui.studyplan.StudyPlanViewModel = hiltViewModel()
                com.studyasist.ui.studyplan.StudyPlanScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
