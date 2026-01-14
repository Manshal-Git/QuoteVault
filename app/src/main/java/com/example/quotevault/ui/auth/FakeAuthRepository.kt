package com.example.quotevault.ui.auth

import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeAuthRepository @Inject constructor() {
    
    suspend fun signIn(email: String, password: String): Result<Unit> {
        delay(1500) // Simulate network delay
        
        return if (email.isNotEmpty() && password.length >= 6) {
            Result.success(Unit)
        } else {
            Result.failure(Exception("Invalid credentials"))
        }
    }
    
    suspend fun signUp(email: String, password: String): Result<Unit> {
        delay(1500) // Simulate network delay
        
        return if (email.contains("@") && password.length >= 6) {
            Result.success(Unit)
        } else {
            Result.failure(Exception("Invalid email or password"))
        }
    }
    
    suspend fun signInWithGoogle(): Result<Unit> {
        delay(1000)
        return Result.success(Unit)
    }
    
    suspend fun signInWithApple(): Result<Unit> {
        delay(1000)
        return Result.success(Unit)
    }
    
    suspend fun resetPassword(email: String): Result<Unit> {
        delay(1000)
        return if (email.contains("@")) {
            Result.success(Unit)
        } else {
            Result.failure(Exception("Invalid email"))
        }
    }
}
