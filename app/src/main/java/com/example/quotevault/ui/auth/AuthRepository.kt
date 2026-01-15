package com.example.quotevault.ui.auth

import android.util.Log
import com.example.quotevault.core.auth.SupabaseClient
import io.github.jan.supabase.exceptions.RestException
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "FakeAuthRepository"

@Singleton
class AuthRepository @Inject constructor(
    supabase: SupabaseClient
) {
    private val auth = supabase.client.auth

    suspend fun signIn(email: String, password: String): Result<Unit> {
        return if (email.isNotEmpty() && password.length >= 6) {
            try {
                auth.signInWith(Email) {
                    this.email = email
                    this.password = password
                }
                Result.success(Unit)
            } catch (e: RestException) {
                Result.failure(e)
            }
        } else {
            Result.failure(Exception("Invalid credentials"))
        }
    }

    suspend fun signUp(email: String, password: String): Result<Unit> {
        return if (email.contains("@") && password.length >= 6) {
            try {
                val result = auth.signUpWith(Email) {
                    this.email = email
                    this.password = password
                }
                Log.i(TAG, "signUp: $result")
                Result.success(Unit)
            } catch (e: RestException) {
                Result.failure(e)
            }
        } else {
            Result.failure(Exception("Invalid email or password"))
        }
    }

    suspend fun signOut(): Result<Unit> {
        return try {
            auth.signOut()
            Result.success(Unit)
        } catch (e: RestException) {
            Result.failure(e)
        }
    }
    
    suspend fun resetPassword(email: String): Result<Unit> {
        return if (email.contains("@")) {
            try {
                auth.resetPasswordForEmail(email)
                Result.success(Unit)
            } catch (e: RestException) {
                Result.failure(e)
            }
        } else {
            Result.failure(Exception("Invalid email"))
        }
    }
    
    suspend fun changePassword(currentPassword: String, newPassword: String): Result<Unit> {
        return if (currentPassword.isNotEmpty() && newPassword.length >= 6) {
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
}
