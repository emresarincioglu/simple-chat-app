package com.example.simplechat.navigation.route

enum class NavigationRoute(val route: String) {
    // Screens
    BOTTOM_BAR_HOST_SCREEN("bottom_bar_host_screen"),
    LOGIN_SCREEN("login_screen"),
    SIGNUP_SCREEN("signup_screen"),
    PASSWORD_RESET_SCREEN("password_reset_screen"),
    HOME_SCREEN("home_screen"),
    CHAT_SCREEN("chat_screen"),
    PROFILE_SCREEN("profile_screen"),

    // Graphs
    AUTHENTICATION_NAV_GRAPH("main_nav_graph"),
    HOME_NAV_GRAPH("home_nav_graph"),
    PROFILE_NAV_GRAPH("profile_nav_graph")
}