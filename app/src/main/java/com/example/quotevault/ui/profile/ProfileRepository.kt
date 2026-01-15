package com.example.quotevault.ui.profile

import com.example.quotevault.data.UserPreferences
import com.example.quotevault.data.UserPreferencesDataStore
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileRepository @Inject constructor(
    private val preferencesDataStore: UserPreferencesDataStore
) {
    
    val userPreferences: Flow<UserPreferences> = preferencesDataStore.userPreferences
    
    suspend fun updateDarkMode(isDarkMode: Boolean): Result<Unit> {
        return try {
            preferencesDataStore.updateDarkMode(isDarkMode)
            // Simulate server sync
            syncToServer("dark_mode", isDarkMode.toString())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updateFontSize(fontSize: Float): Result<Unit> {
        return try {
            preferencesDataStore.updateFontSize(fontSize)
            // Simulate server sync
            syncToServer("font_size", fontSize.toString())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updateUserInfo(name: String, avatarUrl: String): Result<Unit> {
        return try {
            preferencesDataStore.updateUserInfo(name, avatarUrl)
            // Simulate server sync
            syncToServer("user_info", "$name|$avatarUrl")
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private suspend fun syncToServer(key: String, value: String) {
        // Simulate network delay
        delay(500)
        // In real implementation, this would call your API
        println("Syncing to server: $key = $value")
    }
}
