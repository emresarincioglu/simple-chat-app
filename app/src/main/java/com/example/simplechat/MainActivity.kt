package com.example.simplechat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.simplechat.core.ui.theme.SimpleChatTheme
import com.example.simplechat.view.MainScreen
import com.example.simplechat.viewmodel.SplashViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel by viewModels<SplashViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        splashScreen.setKeepOnScreenCondition { !viewModel.isAppInitializedStream.value }

        setContent {
            SimpleChatTheme {
                val isInitialized by viewModel.isAppInitializedStream.collectAsStateWithLifecycle()
                if (isInitialized) {
                    MainScreen()
                }
            }
        }
    }
}
