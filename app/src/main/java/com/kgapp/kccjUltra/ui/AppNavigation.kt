package com.kgapp.kccjUltra.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.kgapp.kccjUltra.ScoreApp
import com.kgapp.kccjUltra.ui.screen.ExamDetailScreen
import com.kgapp.kccjUltra.ui.screen.ExamListScreen
import com.kgapp.kccjUltra.ui.screen.LoginScreen
import com.kgapp.kccjUltra.ui.screen.StudentDetailScreen
import com.kgapp.kccjUltra.ui.state.ExamDetailViewModel
import com.kgapp.kccjUltra.ui.state.ExamListViewModel
import com.kgapp.kccjUltra.ui.state.LoginViewModel
import com.kgapp.kccjUltra.ui.state.ViewModelFactory

sealed class Routes(val route: String) {
    data object Login : Routes("login")
    data object ExamList : Routes("exams/{username}") {
        fun create(username: String) = "exams/${username}"
    }
    data object ExamDetail : Routes("exam_detail/{examId}/{examName}") {
        fun create(examId: String, examName: String) = "exam_detail/${examId}/${examName}"
    }
    data object StudentDetail : Routes("student_detail/{examId}/{studentNum}") {
        fun create(examId: String, studentNum: String) = "student_detail/${examId}/${studentNum}"
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val container = (context.applicationContext as ScoreApp).container
    val repository = container.repository
    val preferences = container.preferences

    NavHost(navController = navController, startDestination = Routes.Login.route) {
        composable(Routes.Login.route) {
            val viewModel: LoginViewModel = viewModel(
                factory = ViewModelFactory {
                    LoginViewModel(repository, preferences)
                }
            )
            LoginScreen(
                viewModel = viewModel,
                onLoginSuccess = { username ->
                    navController.navigate(Routes.ExamList.create(username)) {
                        popUpTo(Routes.Login.route) { inclusive = true }
                    }
                }
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
                    navController.navigate(Routes.ExamDetail.create(exam.id, exam.title))
                }
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
                onStudentClick = { student ->
                    navController.navigate(Routes.StudentDetail.create(examId, student.studentNum))
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            Routes.StudentDetail.route,
            arguments = listOf(
                navArgument("examId") { type = NavType.StringType },
                navArgument("studentNum") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val studentNum = backStackEntry.arguments?.getString("studentNum").orEmpty()
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry(Routes.ExamDetail.route)
            }
            val examId = parentEntry.arguments?.getString("examId").orEmpty()
            val viewModel: ExamDetailViewModel = viewModel(
                parentEntry,
                factory = ViewModelFactory {
                    ExamDetailViewModel(repository, examId)
                }
            )
            StudentDetailScreen(
                viewModel = viewModel,
                studentNum = studentNum,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
