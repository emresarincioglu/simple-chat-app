package com.example.simplechat.navigation.graph

import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.example.simplechat.feature.profile.view.ProfileScreen
import com.example.simplechat.navigation.route.NavigationRoute

fun NavGraphBuilder.ProfileNavGraph(
    parentNavController: NavController, modifier: Modifier = Modifier
) = navigation(
    route = NavigationRoute.PROFILE_NAV_GRAPH.route,
    startDestination = NavigationRoute.PROFILE_SCREEN.route
) {
    composable(route = NavigationRoute.PROFILE_SCREEN.route) {
        ProfileScreen(
            onLogOut = {
                parentNavController.navigate(NavigationRoute.AUTHENTICATION_NAV_GRAPH.route) {
                    popUpTo(NavigationRoute.BOTTOM_BAR_HOST_SCREEN.route) { inclusive = true }
                    launchSingleTop = true
                }
            },
            modifier = modifier
        )
    }
}
