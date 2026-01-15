package com.example.quotevault.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quotevault.ui.auth.FakeAuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AccountDataViewModel @Inject constructor(
    private val authRepository: FakeAuthRepository
) : ViewModel() {
    
    private val _state = MutableStateFlow(AccountDataState())
    val state: StateFlow<AccountDataState> = _state.asStateFlow()
    
    fun handleIntent(intent: AccountDataIntent) {
        when (intent) {
            is AccountDataIntent.CurrentPasswordChanged -> updateCurrentPassword(intent.password)
            is AccountDataIntent.NewPasswordChanged -> updateNewPassword(intent.password)
            is AccountDataIntent.ConfirmPasswordChanged -> updateConfirmPassword(intent.password)
            is AccountDataIntent.ChangePassword -> changePassword()
            is AccountDataIntent.DeleteAccount -> deleteAccount()
            is AccountDataIntent.ClearError -> clearError()
        }
    }
    
    private fun updateCurrentPassword(password: String) {
        _state.value = _state.value.copy(
            currentPassword = password,
            currentPasswordError = null,
            isPasswordChanged = false
        )
    }
    
    private fun updateNewPassword(password: String) {
        _state.value = _state.value.copy(
            newPassword = password,
            newPasswordError = null,
            isPasswordChanged = false
        )
    }
    
    private fun updateConfirmPassword(password: String) {
        _state.value = _state.value.copy(
            confirmPassword = password,
            confirmPasswordError = null,
            isPasswordChanged = false
        )
    }
    
    private fun changePassword() {
        if (!validatePasswords()) return
        
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            
            authRepository.changePassword(
                currentPassword = _state.value.currentPassword,
                newPassword = _state.value.newPassword
            )
                .onSuccess {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        isPasswordChanged = true,
                        currentPassword = "",
                        newPassword = "",
                        confirmPassword = ""
                    )
                }
                .onFailure { error ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to change password"
                    )
                }
        }
    }
    
    private fun validatePasswords(): Boolean {
        var isValid = true
        
        if (_state.value.currentPassword.isEmpty()) {
            _state.value = _state.value.copy(
                currentPasswordError = "Current password is required"
            )
            isValid = false
        }
        
        if (_state.value.newPassword.isEmpty() || _state.value.newPassword.length < 6) {
            _state.value = _state.value.copy(
                newPasswordError = "Password must be at least 6 characters"
            )
            isValid = false
        }
        
        if (_state.value.newPassword != _state.value.confirmPassword) {
            _state.value = _state.value.copy(
                confirmPasswordError = "Passwords do not match"
            )
            isValid = false
        }
        
        return isValid
    }
    
    private fun deleteAccount() {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                error = "Account deletion is not available in demo mode"
            )
        }
    }
    
    private fun clearError() {
        _state.value = _state.value.copy(error = null)
    }
}
