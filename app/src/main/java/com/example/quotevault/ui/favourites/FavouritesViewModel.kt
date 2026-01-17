package com.example.quotevault.ui.favourites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quotevault.data.CollectionsDataSource
import com.example.quotevault.data.UserPreferencesDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavouritesViewModel @Inject constructor(
    private val repository: FavouritesRepository,
    private val collectionsDataSource: CollectionsDataSource,
    private val userPreferencesDataStore: UserPreferencesDataStore
) : ViewModel() {
    
    private val _state = MutableStateFlow(FavouritesState())
    val state: StateFlow<FavouritesState> = _state.asStateFlow()
    
    init {
        viewModelScope.launch {
            collectionsDataSource.loadCollections()
        }
        handleIntent(FavouritesIntent.LoadFavorites)
        observeFavorites()
        observeUserPreferences()
    }
    
    fun handleIntent(intent: FavouritesIntent) {
        when (intent) {
            is FavouritesIntent.LoadFavorites -> loadFavorites()
            is FavouritesIntent.RemoveFavorite -> removeFavorite(intent.quoteId)
            is FavouritesIntent.ShareQuote -> shareQuote(intent.quote)
            is FavouritesIntent.ClearAllFavorites -> clearAllFavorites()
            is FavouritesIntent.ClearError -> clearError()
        }
    }
    
    private fun observeFavorites() {
        viewModelScope.launch {
            repository.getFavoriteQuotesFlow().collect { favorites ->
                _state.value = _state.value.copy(
                    favoriteQuotes = favorites,
                    isLoading = false
                )
            }
        }
    }
    
    private fun observeUserPreferences() {
        viewModelScope.launch {
            userPreferencesDataStore.userPreferences.collect { preferences ->
                _state.value = _state.value.copy(
                    fontSizeScale = preferences.fontSize
                )
            }
        }
    }
    
    private fun loadFavorites() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            
            repository.getFavoriteQuotes()
                .onSuccess { favorites ->
                    _state.value = _state.value.copy(
                        favoriteQuotes = favorites,
                        isLoading = false
                    )
                }
                .onFailure { error ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to load favorites"
                    )
                }
        }
    }
    
    private fun removeFavorite(quoteId: String) {
        viewModelScope.launch {
            // Optimistically update UI
            val updatedFavorites = _state.value.favoriteQuotes.filter { it.id != quoteId }
            _state.value = _state.value.copy(favoriteQuotes = updatedFavorites)
            
            repository.removeFavorite(quoteId)
                .onFailure { error ->
                    // Revert on failure
                    loadFavorites()
                    _state.value = _state.value.copy(
                        error = error.message ?: "Failed to remove favorite"
                    )
                }
        }
    }
    
    private fun clearAllFavorites() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            
            repository.clearAllFavorites()
                .onSuccess {
                    _state.value = _state.value.copy(
                        favoriteQuotes = emptyList(),
                        isLoading = false
                    )
                }
                .onFailure { error ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to clear favorites"
                    )
                }
        }
    }
    
    private fun shareQuote(quote: com.example.quotevault.ui.quotes.Quote) {
        // This would typically trigger a share intent
        // For now, we'll just log it or handle it in the UI layer
    }
    
    private fun clearError() {
        _state.value = _state.value.copy(error = null)
    }
}
