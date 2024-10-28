package com.example.simplechat.feature.authentication.view

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController


@Composable
fun PasswordResetScreen(navController: NavHostController, modifier: Modifier = Modifier) {
    // TODO: Design password reset screen
    Scaffold(modifier = modifier) { paddings ->
        Text("Password Reset", modifier = Modifier.padding(paddings))
    }
}