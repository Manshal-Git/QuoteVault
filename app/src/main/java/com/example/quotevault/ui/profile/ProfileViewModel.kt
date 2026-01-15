package com.example.quotevault.ui.profile

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quotevault.ui.auth.FakeAuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: ProfileRepository,
    private val authRepository: FakeAuthRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileState())
    val state: StateFlow<ProfileState> = _state.asStateFlow()

    init {
        loadUserPreferences()
    }

    fun handleIntent(intent: ProfileIntent) {
        when (intent) {
            is ProfileIntent.UpdateDarkMode -> updateDarkMode(intent.isDarkMode)
            is ProfileIntent.UpdateFontSize -> updateFontSize(intent.fontSize)
            is ProfileIntent.UpdateThemeOption -> updateThemeOption(intent.themeOption)
            is ProfileIntent.UpdateDailyQuoteNotifications -> updateDailyQuoteNotifications(intent.enabled)
            is ProfileIntent.UpdateNotificationTime -> updateNotificationTime(intent.hour, intent.minute)
            is ProfileIntent.SignOut -> signOut()
            is ProfileIntent.ClearError -> clearError()
        }
    }
    
    private fun loadUserPreferences() {
        viewModelScope.launch {
            repository.userPreferences.collect { preferences ->
                _state.value = _state.value.copy(
                    userPreferences = preferences,
                    isLoading = false
                )
            }
        }
    }
    
    private fun updateDarkMode(isDarkMode: Boolean) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isSyncing = true, error = null)
            
            repository.updateDarkMode(isDarkMode)
                .onSuccess {
                    _state.value = _state.value.copy(isSyncing = false)
                }
                .onFailure { error ->
                    _state.value = _state.value.copy(
                        isSyncing = false,
                        error = error.message ?: "Failed to update dark mode"
                    )
                }
        }
    }
    
    private fun updateFontSize(fontSize: Float) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isSyncing = true, error = null)
            
            repository.updateFontSize(fontSize)
                .onSuccess {
                    _state.value = _state.value.copy(isSyncing = false)
                }
                .onFailure { error ->
                    _state.value = _state.value.copy(
                        isSyncing = false,
                        error = error.message ?: "Failed to update font size"
                    )
                }
        }
    }
    
    private fun updateThemeOption(themeOption: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isSyncing = true, error = null)
            
            repository.updateThemeOption(themeOption)
                .onSuccess {
                    _state.value = _state.value.copy(isSyncing = false)
                }
                .onFailure { error ->
                    _state.value = _state.value.copy(
                        isSyncing = false,
                        error = error.message ?: "Failed to update theme"
                    )
                }
        }
    }
    
    private fun updateDailyQuoteNotifications(enabled: Boolean) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isSyncing = true, error = null)
            
            repository.updateDailyQuoteNotifications(enabled)
                .onSuccess {
                    _state.value = _state.value.copy(isSyncing = false)
                    val message = if (enabled) {
                        "Daily quote notifications enabled"
                    } else {
                        "Daily quote notifications disabled"
                    }
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                }
                .onFailure { error ->
                    _state.value = _state.value.copy(
                        isSyncing = false,
                        error = error.message ?: "Failed to update notifications"
                    )
                }
        }
    }
    
    private fun updateNotificationTime(hour: Int, minute: Int) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isSyncing = true, error = null)
            
            repository.updateNotificationTime(hour, minute)
                .onSuccess {
                    _state.value = _state.value.copy(isSyncing = false)
                    Toast.makeText(context, "Notification time updated", Toast.LENGTH_SHORT).show()
                }
                .onFailure { error ->
                    _state.value = _state.value.copy(
                        isSyncing = false,
                        error = error.message ?: "Failed to update notification time"
                    )
                }
        }
    }

    private fun signOut() {
        viewModelScope.launch {
            authRepository.signOut().onSuccess {
                Toast.makeText(context, "Signed out", Toast.LENGTH_SHORT).show()
            }.onFailure {
                Toast.makeText(context, "Failed to sign out", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun clearError() {
        _state.value = _state.value.copy(error = null)
    }
}
