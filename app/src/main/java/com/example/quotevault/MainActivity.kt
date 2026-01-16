package com.example.quotevault

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.example.quotevault.core.auth.SupabaseClient
import com.example.quotevault.navigation.RootNavGraph
import com.example.quotevault.navigation.Screen
import com.example.quotevault.ui.profile.ProfileViewModel
import com.example.quotevault.ui.theme.QuoteVaultTheme
import com.example.quotevault.ui.theme.ThemeOption
import dagger.hilt.android.AndroidEntryPoint
import io.github.jan.supabase.gotrue.SessionStatus
import io.github.jan.supabase.gotrue.auth
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val profileViewModel: ProfileViewModel by viewModels()

    @Inject
    lateinit var supabase: SupabaseClient
    
    private var isAuthCheckComplete by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        
        // Keep splash screen visible until auth check is complete
        splashScreen.setKeepOnScreenCondition {
            !isAuthCheckComplete
        }
        
        enableEdgeToEdge()
        
        setContent {
            val userPreferences = profileViewModel.state.collectAsState().value.userPreferences
            val theme = userPreferences.themeOption
            val isDarkTheme = userPreferences.isDarkMode

            QuoteVaultTheme(
                darkTheme = isDarkTheme,
                themeOption = ThemeOption.valueOf(theme)
            ) {
                var currentDestination by rememberSaveable {
                    mutableStateOf(Screen.Auth.route)
                }
                
                LaunchedEffect(Unit) {
                    supabase.client.auth.sessionStatus.collect { status ->
                        currentDestination = when (status) {
                            is SessionStatus.Authenticated -> {
                                Screen.Home.route
                            }
                            is SessionStatus.NotAuthenticated -> {
                                Screen.Auth.route
                            }
                            else -> currentDestination
                        }
                        // Mark auth check as complete after first status update
                        if (!isAuthCheckComplete) {
                            isAuthCheckComplete = true
                        }
                    }
                }
                
                val navController = rememberNavController()
                if (isAuthCheckComplete) {
                    RootNavGraph(
                        navController = navController,
                        startDestination = currentDestination
                    )
                }
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
