package com.example.simplechat.navigation.navhost

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.simplechat.feature.authentication.navigation.AuthenticationNavGraph
import com.example.simplechat.navigation.NavRoute
import com.example.simplechat.view.BottomBarHostScreen
import com.example.simplechat.feature.authentication.navigation.NavRoute as AuthNavRoute

@Composable
fun MainNavHost(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(
        navController = navController,
        // TODO: Start with home screen if user already logged in
        startDestination = AuthNavRoute.AuthenticationNavGraph.route,
        modifier = modifier
    ) {
        AuthenticationNavGraph(
            navController = navController,
            onNavigateHome = {
                navController.navigate(NavRoute.BottomBarHostScreen.route) {
                    popUpTo(AuthNavRoute.AuthenticationNavGraph.route) { inclusive = true }
                }
            }
        )

        composable(route = NavRoute.BottomBarHostScreen.route) {
            BottomBarHostScreen(parentNavController = navController)
        }
    }
}