package com.example.simplechat.feature.authentication.view

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController


@Composable
fun SignupScreen(
    navController: NavHostController,
    onNavigateHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    // TODO: Design signup screen
    Scaffold(modifier = modifier) { paddings ->
        Text("Signup", modifier = Modifier.padding(paddings))
    }
}