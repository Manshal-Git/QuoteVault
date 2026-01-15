package com.example.quotevault.ui.auth

sealed class PasswordResetIntent {
    data class EmailChanged(val email: String) : PasswordResetIntent()
    object SendResetLink : PasswordResetIntent()
    object ClearError : PasswordResetIntent()
}
