package com.example.quotevault.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

@Singleton
class UserPreferencesDataStore @Inject constructor(
    private val context: Context
) {
    private object PreferencesKeys {
        val DARK_MODE = booleanPreferencesKey("dark_mode")
        val FONT_SIZE = floatPreferencesKey("font_size")
        val USER_NAME = stringPreferencesKey("user_name")
        val USER_AVATAR_URL = stringPreferencesKey("user_avatar_url")
        val THEME_OPTION = stringPreferencesKey("theme_option")
    }
    
    val userPreferences: Flow<UserPreferences> = context.dataStore.data
        .map { preferences ->
            UserPreferences(
                isDarkMode = preferences[PreferencesKeys.DARK_MODE] ?: false,
                fontSize = preferences[PreferencesKeys.FONT_SIZE] ?: 1.0f,
                userName = preferences[PreferencesKeys.USER_NAME] ?: "Guest User",
                userAvatarUrl = preferences[PreferencesKeys.USER_AVATAR_URL] ?: "",
                themeOption = preferences[PreferencesKeys.THEME_OPTION] ?: "INDIGO"
            )
        }
    
    suspend fun updateDarkMode(isDarkMode: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DARK_MODE] = isDarkMode
        }
    }
    
    suspend fun updateFontSize(fontSize: Float) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.FONT_SIZE] = fontSize
        }
    }
    
    suspend fun updateUserInfo(name: String, avatarUrl: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.USER_NAME] = name
            preferences[PreferencesKeys.USER_AVATAR_URL] = avatarUrl
        }
    }
    
    suspend fun updateThemeOption(themeOption: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_OPTION] = themeOption
        }
    }
}

data class UserPreferences(
    val isDarkMode: Boolean = false,
    val fontSize: Float = 1.0f,
    val userName: String = "Guest User",
    val userAvatarUrl: String = "",
    val themeOption: String = "INDIGO"
)
