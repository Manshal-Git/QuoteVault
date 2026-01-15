package com.example.quotevault

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.example.quotevault.navigation.RootNavGraph
import com.example.quotevault.ui.profile.ProfileViewModel
import com.example.quotevault.ui.theme.QuoteVaultTheme
import com.example.quotevault.ui.theme.ThemeOption
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val profileViewModel: ProfileViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val userPreferences = profileViewModel.state.collectAsState().value.userPreferences
            val theme = userPreferences.themeOption
            val isDarkTheme = userPreferences.isDarkMode

            QuoteVaultTheme(
                darkTheme = isDarkTheme,
                themeOption = ThemeOption.valueOf(theme)
            ) {
                val navController = rememberNavController()
                RootNavGraph(navController = navController)
            }
        }

        lifecycleScope.launch {
            profileViewModel.state.collectLatest {
                WindowCompat.getInsetsController(window, window.decorView).apply {
                    isAppearanceLightStatusBars = !it.userPreferences.isDarkMode
                }
            }
        }
    }
}
