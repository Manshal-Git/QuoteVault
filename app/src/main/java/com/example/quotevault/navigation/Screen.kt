package com.example.quotevault.navigation

sealed class Screen(val route: String) {
    object Auth : Screen("auth")
    object Home : Screen("home")
    data class PasswordReset(val email: String) : Screen("password_reset") {
        companion object {
            const val ROUTE = "password_reset"
        }
    }
}
