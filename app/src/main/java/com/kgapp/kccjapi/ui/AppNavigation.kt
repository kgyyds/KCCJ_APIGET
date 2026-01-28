package com.kgapp.kccjapi.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.kgapp.kccjapi.data.network.NetworkModule
import com.kgapp.kccjapi.data.repo.ScoreRepository
import com.kgapp.kccjapi.data.store.UserPreferences
import com.kgapp.kccjapi.ui.screen.ExamDetailScreen
import com.kgapp.kccjapi.ui.screen.ExamListScreen
import com.kgapp.kccjapi.ui.screen.HeaderSettingsScreen
import com.kgapp.kccjapi.ui.screen.LoginScreen
import com.kgapp.kccjapi.ui.state.ExamDetailViewModel
import com.kgapp.kccjapi.ui.state.ExamListViewModel
import com.kgapp.kccjapi.ui.state.HeaderSettingsViewModel
import com.kgapp.kccjapi.ui.state.LoginViewModel
import com.kgapp.kccjapi.ui.state.ViewModelFactory

sealed class Routes(val route: String) {
    data object Login : Routes("login")
    data object HeaderSettings : Routes("headers")
    data object ExamList : Routes("exams/{username}") {
        fun create(username: String) = "exams/${username}"
    }
    data object ExamDetail : Routes("exam_detail/{examId}/{examName}") {
        fun create(examId: String, examName: String) = "exam_detail/${examId}/${examName}"
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val api = NetworkModule.createApi(context)
    val repository = ScoreRepository(api)
    val preferences = UserPreferences(context)

    NavHost(navController = navController, startDestination = Routes.Login.route) {
        composable(Routes.Login.route) {
            val viewModel: LoginViewModel = viewModel(
                factory = ViewModelFactory {
                    LoginViewModel(repository, preferences)
                }
            )
            LoginScreen(
                viewModel = viewModel,
                onOpenHeaders = { navController.navigate(Routes.HeaderSettings.route) },
                onLoginSuccess = { username ->
                    navController.navigate(Routes.ExamList.create(username)) {
                        popUpTo(Routes.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.HeaderSettings.route) {
            val viewModel: HeaderSettingsViewModel = viewModel(
                factory = ViewModelFactory {
                    HeaderSettingsViewModel(preferences)
                }
            )
            HeaderSettingsScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            Routes.ExamList.route,
            arguments = listOf(navArgument("username") { type = NavType.StringType })
        ) { backStackEntry ->
            val username = backStackEntry.arguments?.getString("username").orEmpty()
            val viewModel: ExamListViewModel = viewModel(
                factory = ViewModelFactory {
                    ExamListViewModel(repository, preferences, username)
                }
            )
            ExamListScreen(
                viewModel = viewModel,
                onExamClick = { exam ->
                    navController.navigate(Routes.ExamDetail.create(exam.id, exam.exam))
                },
                onOpenHeaders = { navController.navigate(Routes.HeaderSettings.route) }
            )
        }

        composable(
            Routes.ExamDetail.route,
            arguments = listOf(
                navArgument("examId") { type = NavType.StringType },
                navArgument("examName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val examId = backStackEntry.arguments?.getString("examId").orEmpty()
            val examName = backStackEntry.arguments?.getString("examName").orEmpty()
            val viewModel: ExamDetailViewModel = viewModel(
                factory = ViewModelFactory {
                    ExamDetailViewModel(repository, examId)
                }
            )
            ExamDetailScreen(
                viewModel = viewModel,
                examName = examName,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
