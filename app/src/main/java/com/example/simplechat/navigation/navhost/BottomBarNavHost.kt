package com.example.simplechat.navigation.navhost

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.example.simplechat.feature.home.navigation.HomeNavGraph
import com.example.simplechat.feature.home.navigation.NavRoute as HomeNavRoute

@Composable
fun BottomBarNavHost(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(
        navController = navController,
        startDestination = HomeNavRoute.HomeNavGraph.route,
        modifier = modifier
    ) {
        HomeNavGraph(navController = navController)
    }
}