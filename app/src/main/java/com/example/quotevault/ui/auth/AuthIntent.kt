package com.example.quotevault.ui.auth

sealed class AuthIntent {
    data class EmailChanged(val email: String) : AuthIntent()
    data class PasswordChanged(val password: String) : AuthIntent()
    object CallToActionClicked : AuthIntent()
    object SignInOptionClicked : AuthIntent()
    object SignUpOptionClicked : AuthIntent()
    object GoogleSignInClicked : AuthIntent()
    object AppleSignInClicked : AuthIntent()
    object ForgotPasswordClicked : AuthIntent()
    object ClearError : AuthIntent()
}
