package com.kgapp.kccjapi

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.kgapp.kccjapi.ui.screen.AboutScreen
import com.kgapp.kccjapi.ui.screen.ExactQueryScreen
import com.kgapp.kccjapi.ui.screen.FuzzyQueryScreen
import com.kgapp.kccjapi.ui.screen.MenuScreen

object Routes {
    const val MENU = "menu"
    const val EXACT = "exact"
    const val FUZZY = "fuzzy"
    const val ABOUT = "about"
}

@Composable
fun AppNav() {
    val nav = rememberNavController()

    NavHost(navController = nav, startDestination = Routes.MENU) {
        composable(Routes.MENU) {
            MenuScreen(
                onExact = { nav.navigate(Routes.EXACT) },
                onFuzzy = { nav.navigate(Routes.FUZZY) },
                onAbout = { nav.navigate(Routes.ABOUT) }
            )
        }
        composable(Routes.EXACT) { ExactQueryScreen(onBack = { nav.popBackStack() }) }
        composable(Routes.FUZZY) { FuzzyQueryScreen(onBack = { nav.popBackStack() }) }
        composable(Routes.ABOUT) { AboutScreen(onBack = { nav.popBackStack() }) }
    }
}