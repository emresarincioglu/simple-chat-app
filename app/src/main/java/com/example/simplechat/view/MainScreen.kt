package com.example.simplechat.view

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.example.simplechat.navigation.host.MainNavHost
import com.example.simplechat.viewmodel.MainViewModel

@Composable
fun MainScreen(viewModel: MainViewModel = hiltViewModel()) {
    val isLoggedIn = rememberSaveable { viewModel.isLoggedIn }

    MainNavHost(
        navController = rememberNavController(),
        isLoggedIn = isLoggedIn,
        modifier = Modifier.fillMaxSize()
    )
}