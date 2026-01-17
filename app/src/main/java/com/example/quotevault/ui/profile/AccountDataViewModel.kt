package com.example.quotevault.ui.profile

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quotevault.R
import com.example.quotevault.ui.auth.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AccountDataViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    @ApplicationContext private val context: Context
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
                        error = error.message ?: context.getString(R.string.failed_to_change_password)
                    )
                }
        }
    }
    
    private fun validatePasswords(): Boolean {
        var isValid = true
        
        if (_state.value.currentPassword.isEmpty()) {
            _state.value = _state.value.copy(
                currentPasswordError = context.getString(R.string.current_password_required)
            )
            isValid = false
        }
        
        if (_state.value.newPassword.isEmpty() || _state.value.newPassword.length < 6) {
            _state.value = _state.value.copy(
                newPasswordError = context.getString(R.string.password_min_length)
            )
            isValid = false
        }
        
        if (_state.value.newPassword != _state.value.confirmPassword) {
            _state.value = _state.value.copy(
                confirmPasswordError = context.getString(R.string.passwords_do_not_match)
            )
            isValid = false
        }
        
        return isValid
    }
    
    private fun deleteAccount() {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                error = context.getString(R.string.account_deletion_not_available)
            )
        }
    }
    
    private fun clearError() {
        _state.value = _state.value.copy(error = null)
    }
}
