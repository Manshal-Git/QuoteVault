package com.example.quotevault.ui.auth

import android.util.Log
import com.example.quotevault.core.auth.SupabaseClient
import com.example.quotevault.data.AuthType
import com.example.quotevault.data.OfflineAuthCache
import com.example.quotevault.utils.NetworkConnectivityManager
import io.github.jan.supabase.exceptions.RestException
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import kotlinx.coroutines.flow.first
import java.net.UnknownHostException
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "AuthRepository"

@Singleton
class AuthRepository @Inject constructor(
    supabase: SupabaseClient,
    private val networkManager: NetworkConnectivityManager,
    private val offlineAuthCache: OfflineAuthCache
) {
    private val auth = supabase.client.auth

    suspend fun signIn(email: String, password: String): Result<AuthResult> {
        return if (email.isNotEmpty() && password.length >= 6) {
            if (!networkManager.isConnected()) {
                // Store credentials for later when connection is restored
                offlineAuthCache.storePendingAuth(email, password, AuthType.SIGN_IN)
                return Result.success(AuthResult.OfflinePending("Sign in will be processed when connection is restored"))
            }
            
            try {
                auth.signInWith(Email) {
                    this.email = email
                    this.password = password
                }
                
                // Store successful auth for offline mode
                offlineAuthCache.storeLastSuccessfulAuth(email)
                offlineAuthCache.clearPendingAuth()
                
                Result.success(AuthResult.Success)
            } catch (e: RestException) {
                handleAuthException(e, email, password, AuthType.SIGN_IN)
            } catch (e: UnknownHostException) {
                // Network error - store for retry
                offlineAuthCache.storePendingAuth(email, password, AuthType.SIGN_IN)
                Result.success(AuthResult.OfflinePending("No internet connection. Sign in will be processed when connection is restored"))
            } catch (e: Exception) {
                Log.e(TAG, "signIn error: ${e.message}")
                Result.failure(e)
            }
        } else {
            Result.failure(Exception("Invalid credentials"))
        }
    }

    suspend fun signUp(email: String, password: String): Result<AuthResult> {
        return if (email.contains("@") && password.length >= 6) {
            if (!networkManager.isConnected()) {
                // Store credentials for later when connection is restored
                offlineAuthCache.storePendingAuth(email, password, AuthType.SIGN_UP)
                return Result.success(AuthResult.OfflinePending("Account creation will be processed when connection is restored"))
            }
            
            try {
                val result = auth.signUpWith(Email) {
                    this.email = email
                    this.password = password
                }
                Log.i(TAG, "signUp: $result")
                
                // Store successful auth for offline mode
                offlineAuthCache.storeLastSuccessfulAuth(email)
                offlineAuthCache.clearPendingAuth()
                
                Result.success(AuthResult.Success)
            } catch (e: RestException) {
                handleAuthException(e, email, password, AuthType.SIGN_UP)
            } catch (e: UnknownHostException) {
                // Network error - store for retry
                offlineAuthCache.storePendingAuth(email, password, AuthType.SIGN_UP)
                Result.success(AuthResult.OfflinePending("No internet connection. Account creation will be processed when connection is restored"))
            } catch (e: Exception) {
                Log.e(TAG, "signUp error: ${e.message}")
                Result.failure(e)
            }
        } else {
            Result.failure(Exception("Invalid email or password"))
        }
    }

    suspend fun signOut(): Result<Unit> {
        return try {
            if (networkManager.isConnected()) {
                auth.signOut()
            }
            // Clear offline cache regardless of connection
            offlineAuthCache.clearPendingAuth()
            offlineAuthCache.setOfflineMode(false)
            Result.success(Unit)
        } catch (e: RestException) {
            Result.failure(e)
        }
    }
    
    suspend fun resetPassword(email: String): Result<AuthResult> {
        return if (email.contains("@")) {
            if (!networkManager.isConnected()) {
                return Result.success(AuthResult.OfflinePending("Password reset requires internet connection. Please try again when connected."))
            }
            
            try {
                auth.resetPasswordForEmail(email)
                Result.success(AuthResult.Success)
            } catch (e: RestException) {
                Result.failure(e)
            } catch (e: UnknownHostException) {
                Result.success(AuthResult.OfflinePending("No internet connection. Password reset requires internet connection."))
            }
        } else {
            Result.failure(Exception("Invalid email"))
        }
    }
    
    suspend fun changePassword(currentPassword: String, newPassword: String): Result<Unit> {
        return if (currentPassword.isNotEmpty() && newPassword.length >= 6) {
            if (!networkManager.isConnected()) {
                return Result.failure(Exception("Password change requires internet connection"))
            }
            
            try {
                auth.updateUser {
                    password = newPassword
                }
                Result.success(Unit)
            } catch (e: RestException) {
                Result.failure(e)
            }
        } else {
            Result.failure(Exception("Invalid password"))
        }
    }
    
    /**
     * Process pending authentication when connection is restored
     */
    suspend fun processPendingAuth(): Result<AuthResult> {
        val pendingAuth = offlineAuthCache.getPendingAuth()
        return if (pendingAuth != null && networkManager.isConnected()) {
            when (pendingAuth.authType) {
                AuthType.SIGN_IN -> signIn(pendingAuth.email, pendingAuth.password)
                AuthType.SIGN_UP -> signUp(pendingAuth.email, pendingAuth.password)
            }
        } else {
            Result.failure(Exception("No pending authentication or no connection"))
        }
    }
    
    /**
     * Check if user can continue in offline mode (has previous successful auth)
     */
    suspend fun canContinueOffline(): Boolean {
        val lastSuccessfulAuth = offlineAuthCache.getLastSuccessfulAuth().first()
        return !lastSuccessfulAuth.isNullOrEmpty()
    }
    
    /**
     * Enable offline mode for previously authenticated user
     */
    suspend fun enableOfflineMode(): Result<String> {
        val lastSuccessfulAuth = offlineAuthCache.getLastSuccessfulAuth().first()
        return if (!lastSuccessfulAuth.isNullOrEmpty()) {
            offlineAuthCache.setOfflineMode(true)
            Result.success(lastSuccessfulAuth)
        } else {
            Result.failure(Exception("No previous authentication found"))
        }
    }
    
    private suspend fun handleAuthException(
        e: RestException, 
        email: String, 
        password: String, 
        authType: AuthType
    ): Result<AuthResult> {
        return when {
            e.message?.contains("network", ignoreCase = true) == true -> {
                // Network-related error - store for retry
                offlineAuthCache.storePendingAuth(email, password, authType)
                Result.success(AuthResult.OfflinePending("Connection issue. Request will be processed when connection is restored"))
            }
            else -> Result.failure(e)
        }
    }
}

sealed class AuthResult {
    object Success : AuthResult()
    data class OfflinePending(val message: String) : AuthResult()
}
