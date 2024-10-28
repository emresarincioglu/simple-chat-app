package com.example.simplechat.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.example.simplechat.R
import com.example.simplechat.feature.home.navigation.NavRoute as HomeNavRoute

enum class BottomBarRoute(
    val route: String,
    @StringRes val title: Int,
    @DrawableRes val icon: Int,
    @StringRes val contentDescription: Int
) {
    HOME(
        route = HomeNavRoute.HomeNavGraph.route,
        title = R.string.screen_home_title,
        icon = R.drawable.ic_home_24,
        contentDescription = R.string.btn_nav_home_cont_desc
    ),
    REQUESTS(
        // TODO: Replace with 'RequestsNavRoute.RequestsNavGraph.route'
        route = "requests_nav_graph",
        title = R.string.screen_requests_title,
        icon = R.drawable.ic_notification_24,
        contentDescription = R.string.btn_nav_requests_cont_desc
    ),
    PROFILE(
        // TODO: Replace with 'ProfileNavRoute.ProfileNavGraph.route'
        route = "profile_nav_graph",
        title = R.string.screen_profile_title,
        icon = R.drawable.ic_person_24,
        contentDescription = R.string.btn_nav_profile_cont_desc
    )
}