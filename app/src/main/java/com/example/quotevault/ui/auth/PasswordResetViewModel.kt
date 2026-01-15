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
class PasswordResetViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {
    
    private val _state = MutableStateFlow(PasswordResetState())
    val state: StateFlow<PasswordResetState> = _state.asStateFlow()
    
    fun handleIntent(intent: PasswordResetIntent) {
        when (intent) {
            is PasswordResetIntent.EmailChanged -> updateEmail(intent.email)
            is PasswordResetIntent.SendResetLink -> sendResetLink()
            is PasswordResetIntent.ClearError -> clearError()
        }
    }
    
    private fun updateEmail(email: String) {
        _state.value = _state.value.copy(
            email = email,
            emailError = null
        )
    }
    
    private fun sendResetLink() {
        if (!validateEmail()) return
        
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            
            repository.resetPassword(_state.value.email)
                .onSuccess {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        isResetEmailSent = true
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
    
    private fun validateEmail(): Boolean {
        if (_state.value.email.isEmpty() || !_state.value.email.contains("@")) {
            _state.value = _state.value.copy(emailError = "Please enter a valid email")
            return false
        }
        return true
    }
    
    private fun clearError() {
        _state.value = _state.value.copy(error = null)
    }
}
