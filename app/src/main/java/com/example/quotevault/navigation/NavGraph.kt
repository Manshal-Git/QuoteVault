package com.example.quotevault.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.quotevault.ui.auth.AuthScreen
import com.example.quotevault.ui.home.MainScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Home.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Auth.route) {
            AuthScreen(
                onNavigateBack = {
                    // Handle back navigation if needed
                },
                onAuthSuccess = {
                    // Navigate to home screen after successful authentication
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Auth.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.Home.route) {
            MainScreen()
        }
    }
}
