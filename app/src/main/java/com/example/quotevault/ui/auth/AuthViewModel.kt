package com.example.quotevault.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: FakeAuthRepository
) : ViewModel() {
    
    private val _state = MutableStateFlow(AuthState())
    val state: StateFlow<AuthState> = _state.asStateFlow()
    
    fun handleIntent(intent: AuthIntent) {
        when (intent) {
            is AuthIntent.EmailChanged -> updateEmail(intent.email)
            is AuthIntent.PasswordChanged -> updatePassword(intent.password)
            is AuthIntent.SignInClicked -> signIn()
            is AuthIntent.SignUpClicked -> signUp()
            is AuthIntent.GoogleSignInClicked -> signInWithGoogle()
            is AuthIntent.AppleSignInClicked -> signInWithApple()
            is AuthIntent.ForgotPasswordClicked -> forgotPassword()
            is AuthIntent.ClearError -> clearError()
        }
    }
    
    private fun updateEmail(email: String) {
        _state.value = _state.value.copy(
            email = email,
            emailError = null
        )
    }
    
    private fun updatePassword(password: String) {
        _state.value = _state.value.copy(
            password = password,
            passwordError = null
        )
    }
    
    private fun signIn() {
        if (!validateInputs()) return
        
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            
            repository.signIn(_state.value.email, _state.value.password)
                .onSuccess {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        isSignedIn = true
                    )
                }
                .onFailure { error ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = error.message ?: "Sign in failed"
                    )
                }
        }
    }
    
    private fun signUp() {
        if (!validateInputs()) return
        
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            
            repository.signUp(_state.value.email, _state.value.password)
                .onSuccess {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        isSignedIn = true
                    )
                }
                .onFailure { error ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = error.message ?: "Sign up failed"
                    )
                }
        }
    }
    
    private fun signInWithGoogle() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            
            repository.signInWithGoogle()
                .onSuccess {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        isSignedIn = true
                    )
                }
                .onFailure { error ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = error.message ?: "Google sign in failed"
                    )
                }
        }
    }
    
    private fun signInWithApple() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            
            repository.signInWithApple()
                .onSuccess {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        isSignedIn = true
                    )
                }
                .onFailure { error ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = error.message ?: "Apple sign in failed"
                    )
                }
        }
    }
    
    private fun forgotPassword() {
        if (_state.value.email.isEmpty()) {
            _state.value = _state.value.copy(
                emailError = "Please enter your email"
            )
            return
        }
        
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            
            repository.resetPassword(_state.value.email)
                .onSuccess {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = "Password reset email sent"
                    )
                }
                .onFailure { error ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to send reset email"
                    )
                }
        }
    }
    
    private fun validateInputs(): Boolean {
        var isValid = true
        
        if (_state.value.email.isEmpty() || !_state.value.email.contains("@")) {
            _state.value = _state.value.copy(emailError = "Please enter a valid email")
            isValid = false
        }
        
        if (_state.value.password.isEmpty() || _state.value.password.length < 6) {
            _state.value = _state.value.copy(passwordError = "Password must be at least 6 characters")
            isValid = false
        }
        
        return isValid
    }
    
    private fun clearError() {
        _state.value = _state.value.copy(error = null)
    }
}
