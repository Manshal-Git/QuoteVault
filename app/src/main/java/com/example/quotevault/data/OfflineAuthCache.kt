package com.example.quotevault.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.offlineAuthDataStore: DataStore<Preferences> by preferencesDataStore(name = "offline_auth")

@Singleton
class OfflineAuthCache @Inject constructor(
    private val context: Context
) {
    
    private object PreferencesKeys {
        val PENDING_AUTH_EMAIL = stringPreferencesKey("pending_auth_email")
        val PENDING_AUTH_PASSWORD = stringPreferencesKey("pending_auth_password")
        val PENDING_AUTH_TYPE = stringPreferencesKey("pending_auth_type") // "sign_in" or "sign_up"
        val LAST_SUCCESSFUL_EMAIL = stringPreferencesKey("last_successful_email")
        val IS_OFFLINE_MODE = booleanPreferencesKey("is_offline_mode")
    }
    
    /**
     * Store pending authentication request for when connection is restored
     */
    suspend fun storePendingAuth(email: String, password: String, authType: AuthType) {
        context.offlineAuthDataStore.edit { preferences ->
            preferences[PreferencesKeys.PENDING_AUTH_EMAIL] = email
            preferences[PreferencesKeys.PENDING_AUTH_PASSWORD] = password
            preferences[PreferencesKeys.PENDING_AUTH_TYPE] = authType.name
        }
    }
    
    /**
     * Get pending authentication request
     */
    suspend fun getPendingAuth(): PendingAuth? {
        val preferences = context.offlineAuthDataStore.data.first()
        val email = preferences[PreferencesKeys.PENDING_AUTH_EMAIL]
        val password = preferences[PreferencesKeys.PENDING_AUTH_PASSWORD]
        val authType = preferences[PreferencesKeys.PENDING_AUTH_TYPE]
        
        return if (email != null && password != null && authType != null) {
            PendingAuth(
                email = email,
                password = password,
                authType = AuthType.valueOf(authType)
            )
        } else {
            null
        }
    }
    
    /**
     * Clear pending authentication request
     */
    suspend fun clearPendingAuth() {
        context.offlineAuthDataStore.edit { preferences ->
            preferences.remove(PreferencesKeys.PENDING_AUTH_EMAIL)
            preferences.remove(PreferencesKeys.PENDING_AUTH_PASSWORD)
            preferences.remove(PreferencesKeys.PENDING_AUTH_TYPE)
        }
    }
    
    /**
     * Store last successful authentication email for offline mode
     */
    suspend fun storeLastSuccessfulAuth(email: String) {
        context.offlineAuthDataStore.edit { preferences ->
            preferences[PreferencesKeys.LAST_SUCCESSFUL_EMAIL] = email
        }
    }
    
    /**
     * Get last successful authentication email
     */
    fun getLastSuccessfulAuth(): Flow<String?> {
        return context.offlineAuthDataStore.data.map { preferences ->
            preferences[PreferencesKeys.LAST_SUCCESSFUL_EMAIL]
        }
    }
    
    /**
     * Set offline mode status
     */
    suspend fun setOfflineMode(isOffline: Boolean) {
        context.offlineAuthDataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_OFFLINE_MODE] = isOffline
        }
    }
    
    /**
     * Check if app is in offline mode
     */
    fun isOfflineMode(): Flow<Boolean> {
        return context.offlineAuthDataStore.data.map { preferences ->
            preferences[PreferencesKeys.IS_OFFLINE_MODE] ?: false
        }
    }
}

data class PendingAuth(
    val email: String,
    val password: String,
    val authType: AuthType
)

enum class AuthType {
    SIGN_IN,
    SIGN_UP
}