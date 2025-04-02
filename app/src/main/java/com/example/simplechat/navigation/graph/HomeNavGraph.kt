package com.example.simplechat.navigation.graph

import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection
import androidx.compose.animation.core.tween
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import androidx.navigation.navigation
import com.example.simplechat.feature.home.view.ChatScreen
import com.example.simplechat.feature.home.view.HomeScreen
import com.example.simplechat.feature.home.viewmodel.HomeViewModel
import com.example.simplechat.navigation.route.NavigationRoute

fun NavGraphBuilder.HomeNavGraph(
    navController: NavHostController,
    parentNavController: NavController,
    modifier: Modifier = Modifier
) = navigation(
    route = NavigationRoute.HOME_NAV_GRAPH.route,
    startDestination = NavigationRoute.HOME_SCREEN.route
) {
    composable(
        route = NavigationRoute.HOME_SCREEN.route,
        arguments = listOf(
            navArgument("friendCode") {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            }
        ),
        deepLinks = listOf(
            navDeepLink { uriPattern = "android-app://com.example.simplechat/share/{friendCode}" }
        ),
        exitTransition = {
            val route = targetState.destination.route!!
            if (route.startsWith(NavigationRoute.CHAT_SCREEN.route)) {
                slideOutOfContainer(SlideDirection.Start, tween(700))
            } else null
        },
        popEnterTransition = { slideIntoContainer(SlideDirection.End, tween(700)) }
    ) { backStackEntry ->
        val viewModel = hiltViewModel<HomeViewModel>()
        if (viewModel.isLoggedIn) {
            backStackEntry.arguments?.getString("friendCode")?.let {
                viewModel.addFriend(it)
            }

            HomeScreen(
                viewModel = viewModel,
                onNavigateChat = { friendId ->
                    navController.navigate("${NavigationRoute.CHAT_SCREEN.route}/$friendId")
                },
                modifier = modifier
            )
        } else {
            parentNavController.navigate(NavigationRoute.AUTHENTICATION_NAV_GRAPH.route) {
                popUpTo(NavigationRoute.BOTTOM_BAR_HOST_SCREEN.route) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    composable(
        route = "${NavigationRoute.CHAT_SCREEN.route}/{friendId}",
        arguments = listOf(navArgument("friendId") { type = NavType.IntType }),
        enterTransition = { slideIntoContainer(SlideDirection.Start, tween(700)) },
        exitTransition = { slideOutOfContainer(SlideDirection.End, tween(700)) }
    ) {
        ChatScreen(onNavigateBack = { navController.popBackStack() }, modifier = modifier)
    }
}
