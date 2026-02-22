package com.example.quotevault.ui.favourites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quotevault.data.CollectionsDataSource
import com.example.quotevault.data.UserPreferencesDataStore
import com.example.quotevault.ui.quotes.Quote
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
            is FavouritesIntent.LoadNextPage -> loadNextPage()
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
                resetPagination()
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
                    resetPagination()
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
            val updatedFavorites = _state.value.favoriteQuotes.filter { it.id != quoteId }
            _state.value = _state.value.copy(favoriteQuotes = updatedFavorites)
            resetPagination()

            repository.removeFavorite(quoteId)
                .onFailure { error ->
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
                        visibleFavoriteQuotes = emptyList(),
                        isLoading = false,
                        hasMorePages = false,
                        currentPage = 0
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

    private fun shareQuote(quote: Quote) {
        // Handled in UI layer
    }

    private fun resetPagination() {
        val firstPage = _state.value.favoriteQuotes.take(PAGE_SIZE)
        _state.value = _state.value.copy(
            visibleFavoriteQuotes = firstPage,
            currentPage = if (firstPage.isEmpty()) 0 else 1,
            hasMorePages = _state.value.favoriteQuotes.size > firstPage.size,
            isLoadingMore = false
        )
    }

    private fun loadNextPage() {
        val state = _state.value
        if (!state.hasMorePages || state.isLoadingMore) return

        val nextPage = state.currentPage + 1
        val nextVisible = state.favoriteQuotes.take(nextPage * PAGE_SIZE)
        _state.value = state.copy(
            visibleFavoriteQuotes = nextVisible,
            currentPage = nextPage,
            hasMorePages = nextVisible.size < state.favoriteQuotes.size,
            isLoadingMore = false
        )
    }

    private fun clearError() {
        _state.value = _state.value.copy(error = null)
    }

    companion object {
        private const val PAGE_SIZE = 25
    }
}
