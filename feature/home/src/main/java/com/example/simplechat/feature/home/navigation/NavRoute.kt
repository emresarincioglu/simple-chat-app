package com.example.simplechat.feature.home.navigation

sealed class NavRoute(val route : String) {
    // Screens
    data object HomeScreen : NavRoute("home_screen")

    // Graphs
    data object HomeNavGraph : NavRoute("home_nav_graph")
}