package com.example.simplechat.view

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.simplechat.navigation.BottomBarRoute
import com.example.simplechat.navigation.navhost.BottomBarNavHost

@Composable
fun BottomBarHostScreen(parentNavController: NavHostController, modifier: Modifier = Modifier) {
    Scaffold(
        bottomBar = {
            var selectedRoute by rememberSaveable { mutableIntStateOf(0) }

            NavigationBar {
                BottomBarRoute.entries.forEachIndexed { index, item ->
                    NavigationBarItem(
                        onClick = {
                            selectedRoute = index
                            parentNavController.navigate(item.route) {
                                popUpTo(parentNavController.graph.findStartDestination().id) {
                                    saveState = true
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
                        selected = selectedRoute == index,
                        alwaysShowLabel = false
                    )
                }
            }
        },
        modifier = modifier
    ) { paddings ->
        val navController = rememberNavController()
        BottomBarNavHost(navController = navController, modifier = Modifier.padding(paddings))
    }
}