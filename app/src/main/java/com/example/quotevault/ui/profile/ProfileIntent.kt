package com.example.quotevault.ui.profile

sealed class ProfileIntent {
    data class UpdateDarkMode(val isDarkMode: Boolean) : ProfileIntent()
    data class UpdateFontSize(val fontSize: Float) : ProfileIntent()
    object SignOut : ProfileIntent()
    object ClearError : ProfileIntent()
}
