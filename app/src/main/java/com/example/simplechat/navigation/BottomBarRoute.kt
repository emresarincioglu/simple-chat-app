package com.example.simplechat.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.simplechat.R
import com.example.simplechat.feature.home.navigation.NavRoute as HomeNavRoute

enum class BottomBarRoute(
    val route: String,
    @StringRes val title: Int,
    val icon: ImageVector,
    @StringRes val contentDescription: Int
) {
    HOME(
        route = HomeNavRoute.HomeNavGraph.route,
        title = R.string.screen_home_title,
        icon = Icons.Filled.Home,
        contentDescription = R.string.btn_nav_home_cont_desc
    ),
    REQUESTS(
        // TODO: Replace with 'RequestsNavRoute.RequestsNavGraph.route'
        route = "requests_nav_graph",
        title = R.string.screen_requests_title,
        icon = Icons.Filled.Notifications,
        contentDescription = R.string.btn_nav_requests_cont_desc
    ),
    PROFILE(
        // TODO: Replace with 'ProfileNavRoute.ProfileNavGraph.route'
        route = "profile_nav_graph",
        title = R.string.screen_profile_title,
        icon = Icons.Filled.Person,
        contentDescription = R.string.btn_nav_profile_cont_desc
    )
}