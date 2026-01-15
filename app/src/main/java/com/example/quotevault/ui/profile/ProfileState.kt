package com.example.quotevault.ui.profile

import com.example.quotevault.data.UserPreferences

data class ProfileState(
    val userPreferences: UserPreferences = UserPreferences(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSyncing: Boolean = false
)
