package com.example.quotevault.ui.profile

sealed class AccountDataIntent {
    data class CurrentPasswordChanged(val password: String) : AccountDataIntent()
    data class NewPasswordChanged(val password: String) : AccountDataIntent()
    data class ConfirmPasswordChanged(val password: String) : AccountDataIntent()
    object ChangePassword : AccountDataIntent()
    object DeleteAccount : AccountDataIntent()
    object ClearError : AccountDataIntent()
}
