package com.example.simplechat.feature.authentication.navigation

sealed class NavRoute(val route : String) {
    // Screens
    data object LoginScreen : NavRoute("login_screen")
    data object SignupScreen : NavRoute("signup_screen")
    data object PasswordResetScreen : NavRoute("password_reset_screen")

    // Graphs
    data object AuthenticationNavGraph : NavRoute("authentication_nav_graph")
}