package com.example.simplechat.navigation.host

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.example.simplechat.navigation.graph.HomeNavGraph
import com.example.simplechat.navigation.route.NavigationRoute

@Composable
fun BottomBarNavHost(
    navController: NavHostController,
    parentNavController: NavController,
    modifier: Modifier = Modifier
) = NavHost(
    navController = navController,
    startDestination = NavigationRoute.HOME_NAV_GRAPH.route,
    modifier = Modifier.fillMaxSize()
) {
    HomeNavGraph(
        navController = navController,
        parentNavController = parentNavController,
        modifier = modifier
    )
}