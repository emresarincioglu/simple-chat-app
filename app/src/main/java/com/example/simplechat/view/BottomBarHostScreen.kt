package com.example.simplechat.view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.simplechat.navigation.host.BottomBarNavHost
import com.example.simplechat.navigation.route.BottomBarRoute
import com.example.simplechat.navigation.route.NavigationRoute

@Composable
fun BottomBarHostScreen(parentNavController: NavHostController, modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    var showBottomBar by rememberSaveable { mutableStateOf(true) }

    DisposableEffect(navController) {
        val listener = NavController.OnDestinationChangedListener { _, destination, _ ->
            val route = destination.route!!
            showBottomBar = !route.startsWith(NavigationRoute.CHAT_SCREEN.route)
        }

        navController.addOnDestinationChangedListener(listener)
        onDispose { navController.removeOnDestinationChangedListener(listener) }
    }

    Scaffold(
        bottomBar = {
            AnimatedVisibility(
                visible = showBottomBar,
                enter = expandVertically(
                    tween(durationMillis = 300, delayMillis = 100, easing = LinearEasing)
                ),
                exit = shrinkVertically(
                    tween(durationMillis = 150, delayMillis = 150, easing = LinearEasing)
                ) { fullHeight ->
                    fullHeight / 2
                },
                label = "BottomBarVisibility"
            ) {
                BottomBar(navController)
            }
        },
        modifier = modifier
    ) { paddings ->
        BottomBarNavHost(
            navController = navController,
            parentNavController = parentNavController,
            modifier = Modifier.padding(bottom = paddings.calculateBottomPadding())
        )
    }
}

@Composable
private fun BottomBar(navController: NavHostController, modifier: Modifier = Modifier) {
    var selectedRoute by rememberSaveable { mutableIntStateOf(0) }

    NavigationBar(modifier = modifier) {
        BottomBarRoute.entries.forEachIndexed { index, item ->
            NavigationBarItem(
                onClick = {
                    if (selectedRoute == index) return@NavigationBarItem

                    selectedRoute = index
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.id) {
                            saveState = true
                            inclusive = true
                        }

                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = stringResource(item.contentDescription)
                    )
                },
                label = { Text(text = stringResource(item.title)) },
                selected = selectedRoute == index
            )
        }
    }
}