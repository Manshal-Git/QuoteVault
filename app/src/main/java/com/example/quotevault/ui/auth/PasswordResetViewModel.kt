package com.example.quotevault.ui.auth

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quotevault.R
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PasswordResetViewModel @Inject constructor(
    private val repository: AuthRepository,
    @ApplicationContext private val context: Context
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
                        error = error.message ?: context.getString(R.string.failed_to_send_reset_email)
                    )
                }
        }
    }
    
    private fun validateEmail(): Boolean {
        if (_state.value.email.isEmpty() || !_state.value.email.contains("@")) {
            _state.value = _state.value.copy(emailError = context.getString(R.string.please_enter_valid_email))
            return false
        }
        return true
    }
    
    private fun clearError() {
        _state.value = _state.value.copy(error = null)
    }
}
