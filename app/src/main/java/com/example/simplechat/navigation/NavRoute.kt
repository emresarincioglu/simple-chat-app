package com.example.simplechat.navigation

sealed class NavRoute(val route : String) {
    // Screens
    data object BottomBarHostScreen : NavRoute("bottom_bar_host_screen")

    // Graphs
    data object MainNavGraph : NavRoute("main_nav_graph")
}