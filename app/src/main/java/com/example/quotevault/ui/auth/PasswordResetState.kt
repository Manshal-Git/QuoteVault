package com.example.quotevault.ui.auth

data class PasswordResetState(
    val email: String = "",
    val emailError: String? = null,
    val isLoading: Boolean = false,
    val isResetEmailSent: Boolean = false,
    val error: String? = null
)
