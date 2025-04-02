package com.example.simplechat.navigation.host

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.simplechat.navigation.graph.AuthenticationNavGraph
import com.example.simplechat.navigation.route.NavigationRoute
import com.example.simplechat.view.BottomBarHostScreen

@Composable
fun MainNavHost(
    navController: NavHostController,
    isLoggedIn: Boolean,
    modifier: Modifier = Modifier
) = NavHost(
    navController = navController,
    startDestination = if (isLoggedIn) {
        NavigationRoute.BOTTOM_BAR_HOST_SCREEN.route
    } else {
        NavigationRoute.AUTHENTICATION_NAV_GRAPH.route
    },
    modifier = modifier
) {
    AuthenticationNavGraph(
        navController = navController,
        onNavigateHome = {
            navController.navigate(NavigationRoute.BOTTOM_BAR_HOST_SCREEN.route) {
                popUpTo(NavigationRoute.AUTHENTICATION_NAV_GRAPH.route) { inclusive = true }
            }
        }
    )

    composable(route = NavigationRoute.BOTTOM_BAR_HOST_SCREEN.route) {
        BottomBarHostScreen(parentNavController = navController)
    }
}