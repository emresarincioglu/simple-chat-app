package com.example.simplechat.navigation.route

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.simplechat.R

enum class BottomBarRoute(
    val route: String,
    @StringRes val title: Int,
    val icon: ImageVector,
    @StringRes val contentDescription: Int
) {
    HOME(
        route = NavigationRoute.HOME_NAV_GRAPH.route,
        title = R.string.screen_home_title,
        icon = Icons.Filled.Home,
        contentDescription = R.string.btn_nav_home_cont_desc
    ),
    PROFILE(
        route = NavigationRoute.PROFILE_NAV_GRAPH.route,
        title = R.string.screen_profile_title,
        icon = Icons.Filled.Person,
        contentDescription = R.string.btn_nav_profile_cont_desc
    )
}