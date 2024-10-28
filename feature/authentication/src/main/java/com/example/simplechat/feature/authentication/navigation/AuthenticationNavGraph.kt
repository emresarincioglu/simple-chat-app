package com.example.simplechat.feature.authentication.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
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

        composable(
            route = "${NavRoute.SignupScreen.route}/{email}/{password}",
            arguments = listOf(
                navArgument("email") { type = NavType.StringType },
                navArgument("password") { type = NavType.StringType }
            )
        ) {
            SignupScreen(navController = navController, onNavigateHome = onNavigateHome)
        }

        composable(
            route = "${NavRoute.PasswordResetScreen.route}/{email}",
            arguments = listOf(navArgument("email") { type = NavType.StringType })
        ) {
            PasswordResetScreen(navController = navController)
        }
    }
}
