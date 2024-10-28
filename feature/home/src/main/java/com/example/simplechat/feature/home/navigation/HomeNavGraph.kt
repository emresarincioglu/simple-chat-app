package com.example.simplechat.feature.home.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.example.simplechat.feature.home.view.HomeScreen

fun NavGraphBuilder.HomeNavGraph(navController: NavHostController) {
    navigation(
        route = NavRoute.HomeNavGraph.route,
        startDestination = NavRoute.HomeScreen.route
    ) {
        composable(route = NavRoute.HomeScreen.route) {
            HomeScreen(navController = navController)
        }
    }
}
