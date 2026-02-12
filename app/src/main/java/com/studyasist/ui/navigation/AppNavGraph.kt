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
                },
                onDictate = { navController.navigate(NavRoutes.DICTATE) },
                onExplain = { navController.navigate(NavRoutes.EXPLAIN) },
                onSolve = { navController.navigate(NavRoutes.SOLVE) },
                onExamGoals = { navController.navigate(NavRoutes.GOAL_LIST) },
                onQABank = { navController.navigate(NavRoutes.QA_BANK) },
                onAssessments = { navController.navigate(NavRoutes.ASSESSMENT_LIST) },
                onResults = { navController.navigate(NavRoutes.RESULT_LIST) }
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
                onResultClick = { attemptId -> navController.navigate(NavRoutes.assessmentResult(attemptId)) }
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

        composable(NavRoutes.QA_SCAN) {
            val viewModel: QAScanViewModel = hiltViewModel()
            QAScanScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() }
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
        ) {
            val viewModel: AssessmentResultViewModel = hiltViewModel()
            AssessmentResultScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(NavRoutes.ASSESSMENT_LIST) {
            val viewModel: AssessmentListViewModel = hiltViewModel()
            AssessmentListScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onAssessmentClick = { id -> navController.navigate(NavRoutes.assessmentRun(id)) },
                onEditAssessment = { id -> navController.navigate(NavRoutes.assessmentEdit(id)) }
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
                onResultClick = { attemptId -> navController.navigate(NavRoutes.assessmentResult(attemptId)) }
            )
        }
    }
}
