package com.example.simplechat.feature.authentication.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.example.simplechat.feature.authentication.view.LoginScreen
import com.example.simplechat.feature.authentication.view.PasswordResetScreen
import com.example.simplechat.feature.authentication.view.SignupScreen

fun NavGraphBuilder.AuthenticationNavGraph(
    navController: NavHostController,
    onNavigateHome: () -> Unit
) {
    navigation(
        route = NavRoute.AuthenticationNavGraph.route,
        startDestination = NavRoute.LoginScreen.route
    ) {
        composable(route = NavRoute.LoginScreen.route) {
            LoginScreen(navController = navController, onNavigateHome = onNavigateHome)
        }

        composable(route = NavRoute.SignupScreen.route) {
            SignupScreen(navController = navController, onNavigateHome = onNavigateHome)
        }

        composable(route = NavRoute.PasswordResetScreen.route) {
            PasswordResetScreen(navController = navController)
        }
    }
}
