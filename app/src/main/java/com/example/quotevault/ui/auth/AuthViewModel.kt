package com.example.quotevault.ui.auth

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quotevault.R
import com.example.quotevault.data.OfflineAuthCache
import com.example.quotevault.utils.NetworkConnectivityManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: AuthRepository,
    private val networkManager: NetworkConnectivityManager,
    private val offlineAuthCache: OfflineAuthCache
) : ViewModel() {
    
    private val _state = MutableStateFlow(AuthState())
    val state: StateFlow<AuthState> = _state.asStateFlow()

    init {
        observeConnectivity()
        checkPendingAuth()
    }

    fun handleIntent(intent: AuthIntent) {
        when (intent) {
            is AuthIntent.EmailChanged -> updateEmail(intent.email)
            is AuthIntent.PasswordChanged -> updatePassword(intent.password)
            is AuthIntent.CallToActionClicked -> onCallToActionClicked()
            is AuthIntent.ForgotPasswordClicked -> forgotPassword()
            is AuthIntent.ClearError -> clearError()
            is AuthIntent.SignInOptionClicked -> setSignInMode(true)
            is AuthIntent.SignUpOptionClicked -> setSignInMode(false)
            is AuthIntent.RetryConnection -> retryConnection()
            is AuthIntent.ContinueOffline -> continueOffline()
            is AuthIntent.ProcessPendingAuth -> processPendingAuth()
        }
    }

    private fun observeConnectivity() {
        viewModelScope.launch {
            networkManager.connectivityFlow().collect { isConnected ->
                _state.update { currentState ->
                    currentState.copy(
                        isConnected = isConnected,
                        offlineMessage = if (!isConnected) {
                            "No internet connection. Some features may be limited."
                        } else null
                    )
                }
                
                // Auto-process pending auth when connection is restored
                if (isConnected && _state.value.hasPendingAuth) {
                    processPendingAuth()
                }
            }
        }
    }
    
    private fun checkPendingAuth() {
        viewModelScope.launch {
            val pendingAuth = offlineAuthCache.getPendingAuth()
            _state.update { it.copy(hasPendingAuth = pendingAuth != null) }
        }
    }

    private fun onCallToActionClicked() {
        if (_state.value.isSignInMode) {
            signIn()
        } else {
            signUp()
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

    private fun setSignInMode(value: Boolean) {
        _state.update {
            it.copy(
                isSignInMode = value
            )
        }
    }
    
    private fun signIn() {
        if (!validateInputs()) return
        
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            
            repository.signIn(_state.value.email, _state.value.password)
                .onSuccess { result ->
                    when (result) {
                        is AuthResult.Success -> {
                            showToast(context.getString(R.string.sign_in_successful))
                            _state.value = _state.value.copy(
                                isLoading = false,
                                isSignedIn = true,
                                hasPendingAuth = false
                            )
                        }
                        is AuthResult.OfflinePending -> {
                            _state.value = _state.value.copy(
                                isLoading = false,
                                offlineMessage = result.message,
                                hasPendingAuth = true
                            )
                        }
                    }
                }
                .onFailure { error ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = error.message ?: context.getString(R.string.sign_in_failed)
                    )
                }
        }
    }
    
    private fun signUp() {
        if (!validateInputs()) return
        
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            
            repository.signUp(_state.value.email, _state.value.password)
                .onSuccess { result ->
                    when (result) {
                        is AuthResult.Success -> {
                            showToast(context.getString(R.string.sign_up_successful))
                            _state.value = _state.value.copy(
                                isLoading = false,
                                isSignedIn = true,
                                hasPendingAuth = false
                            )
                        }
                        is AuthResult.OfflinePending -> {
                            _state.value = _state.value.copy(
                                isLoading = false,
                                offlineMessage = result.message,
                                hasPendingAuth = true
                            )
                        }
                    }
                }
                .onFailure { error ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = error.message ?: context.getString(R.string.sign_up_failed)
                    )
                }
        }
    }
    
    private fun forgotPassword() {
        if (_state.value.email.isEmpty()) {
            _state.value = _state.value.copy(
                emailError = context.getString(R.string.please_enter_email)
            )
            return
        }
        
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            
            repository.resetPassword(_state.value.email)
                .onSuccess { result ->
                    when (result) {
                        is AuthResult.Success -> {
                            _state.value = _state.value.copy(
                                isLoading = false,
                                error = context.getString(R.string.password_reset_email_sent)
                            )
                        }
                        is AuthResult.OfflinePending -> {
                            _state.value = _state.value.copy(
                                isLoading = false,
                                offlineMessage = result.message
                            )
                        }
                    }
                }
                .onFailure { error ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = error.message ?: context.getString(R.string.failed_to_send_reset_email)
                    )
                }
        }
    }
    
    private fun retryConnection() {
        if (networkManager.isConnected()) {
            _state.update { 
                it.copy(
                    offlineMessage = null,
                    error = null
                ) 
            }
            if (_state.value.hasPendingAuth) {
                processPendingAuth()
            }
        } else {
            _state.update { 
                it.copy(
                    offlineMessage = "Still no internet connection. Please check your network settings."
                ) 
            }
        }
    }
    
    private fun continueOffline() {
        viewModelScope.launch {
            if (repository.canContinueOffline()) {
                repository.enableOfflineMode()
                    .onSuccess { email ->
                        _state.update { 
                            it.copy(
                                isSignedIn = true,
                                isOfflineMode = true,
                                email = email,
                                offlineMessage = "Continuing in offline mode with previous account: $email"
                            ) 
                        }
                        showToast("Continuing offline with $email")
                    }
                    .onFailure {
                        _state.update { 
                            it.copy(
                                error = "Cannot continue offline. Please sign in when connected to internet."
                            ) 
                        }
                    }
            } else {
                _state.update { 
                    it.copy(
                        error = "Cannot continue offline. Please sign in when connected to internet."
                    ) 
                }
            }
        }
    }
    
    private fun processPendingAuth() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            
            repository.processPendingAuth()
                .onSuccess { result ->
                    when (result) {
                        is AuthResult.Success -> {
                            showToast("Authentication completed successfully!")
                            _state.update { 
                                it.copy(
                                    isLoading = false,
                                    isSignedIn = true,
                                    hasPendingAuth = false,
                                    offlineMessage = null
                                ) 
                            }
                        }
                        is AuthResult.OfflinePending -> {
                            _state.update { 
                                it.copy(
                                    isLoading = false,
                                    offlineMessage = result.message
                                ) 
                            }
                        }
                    }
                }
                .onFailure { error ->
                    _state.update { 
                        it.copy(
                            isLoading = false,
                            error = "Failed to process pending authentication: ${error.message}"
                        ) 
                    }
                }
        }
    }
    
    private fun validateInputs(): Boolean {
        var isValid = true
        
        if (_state.value.email.isEmpty() || !_state.value.email.contains("@")) {
            _state.value = _state.value.copy(emailError = context.getString(R.string.please_enter_valid_email))
            isValid = false
        }
        
        if (_state.value.password.isEmpty() || _state.value.password.length < 6) {
            _state.value = _state.value.copy(passwordError = context.getString(R.string.password_min_length))
            isValid = false
        }
        
        return isValid
    }
    
    private fun clearError() {
        _state.value = _state.value.copy(
            error = null,
            offlineMessage = null
        )
    }

    private fun showToast(msg: String) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    }
}
