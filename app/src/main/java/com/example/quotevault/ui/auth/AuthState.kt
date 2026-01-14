package com.example.quotevault.ui.auth

data class AuthState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSignedIn: Boolean = false,
    val emailError: String? = null,
    val passwordError: String? = null,
    val isSignInMode: Boolean = true
)
