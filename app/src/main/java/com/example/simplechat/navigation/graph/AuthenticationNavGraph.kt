package com.example.simplechat.navigation.graph

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.example.simplechat.feature.authentication.view.LoginScreen
import com.example.simplechat.feature.authentication.view.PasswordResetScreen
import com.example.simplechat.feature.authentication.view.SignupScreen
import com.example.simplechat.feature.authentication.viewmodel.LoginViewModel
import com.example.simplechat.feature.authentication.viewmodel.PasswordResetViewModel
import com.example.simplechat.feature.authentication.viewmodel.SignupViewModel
import com.example.simplechat.navigation.route.NavigationRoute

fun NavGraphBuilder.AuthenticationNavGraph(
    onNavigateHome: () -> Unit,
    navController: NavHostController
) = navigation(
    route = NavigationRoute.AUTHENTICATION_NAV_GRAPH.route,
    startDestination = NavigationRoute.LOGIN_SCREEN.route
) {
    composable(route = NavigationRoute.LOGIN_SCREEN.route) {
        val viewModel = hiltViewModel<LoginViewModel>()
        with(navController.currentBackStackEntry!!.savedStateHandle) {
            remove<String>("email")?.let { viewModel.email = it }
            remove<String>("password")?.let { viewModel.password = it }
        }

        LoginScreen(
            viewModel = viewModel,
            onNavigateHome = onNavigateHome,
            onNavigateSignup = {
                navController.navigate(
                    "${NavigationRoute.SIGNUP_SCREEN.route}/${viewModel.email}/${viewModel.password}"
                )
            },
            onNavigatePasswordReset = {
                navController.navigate("${NavigationRoute.PASSWORD_RESET_SCREEN.route}/${viewModel.email}")
            }
        )
    }

    composable(
        route = "${NavigationRoute.SIGNUP_SCREEN.route}/{email}/{password}",
        arguments = listOf(
            navArgument("email") { type = NavType.StringType },
            navArgument("password") { type = NavType.StringType }
        )
    ) {
        val viewModel = hiltViewModel<SignupViewModel>()
        SignupScreen(
            viewModel = viewModel,
            onNavigateHome = onNavigateHome,
            onNavigateBack = {
                with(navController.previousBackStackEntry!!) {
                    savedStateHandle["email"] = viewModel.email
                    savedStateHandle["password"] = viewModel.password
                }
                navController.popBackStack()
            }
        )
    }

    composable(
        route = "${NavigationRoute.PASSWORD_RESET_SCREEN.route}/{email}",
        arguments = listOf(navArgument("email") { type = NavType.StringType })
    ) {
        val viewModel = hiltViewModel<PasswordResetViewModel>()
        PasswordResetScreen(
            onNavigateBack = {
                val savedStateHandle = navController.previousBackStackEntry!!.savedStateHandle
                savedStateHandle["email"] = viewModel.email
                navController.popBackStack()
            }
        )
    }
}
