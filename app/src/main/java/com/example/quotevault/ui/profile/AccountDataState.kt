package com.example.quotevault.ui.profile

data class AccountDataState(
    val currentPassword: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val currentPasswordError: String? = null,
    val newPasswordError: String? = null,
    val confirmPasswordError: String? = null,
    val isLoading: Boolean = false,
    val isPasswordChanged: Boolean = false,
    val error: String? = null
)
