package com.example.simplechat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.simplechat.core.ui.theme.SimpleChatTheme
import com.example.simplechat.navigation.navhost.MainNavHost
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            SimpleChatTheme {
                val navController = rememberNavController()
                MainNavHost(navController = navController, modifier = Modifier.fillMaxSize())
            }
        }
    }
}
