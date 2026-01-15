package com.example.quotevault.ui.profile

sealed class ProfileIntent {
    data class UpdateDarkMode(val isDarkMode: Boolean) : ProfileIntent()
    data class UpdateFontSize(val fontSize: Float) : ProfileIntent()
    data class UpdateThemeOption(val themeOption: String) : ProfileIntent()
    data class UpdateDailyQuoteNotifications(val enabled: Boolean) : ProfileIntent()
    data class UpdateNotificationTime(val hour: Int, val minute: Int) : ProfileIntent()
    object SignOut : ProfileIntent()
    object ClearError : ProfileIntent()
}
