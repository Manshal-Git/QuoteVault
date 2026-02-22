package com.example.quotevault.ui.quotes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quotevault.data.CollectionsDataSource
import com.example.quotevault.data.UserPreferencesDataStore
import com.example.quotevault.utils.NetworkConnectivityManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val PAGE_SIZE = 25

@HiltViewModel
class QuotesViewModel @Inject constructor(
    private val repository: QuotesRepository,
    private val collectionsDataSource: CollectionsDataSource,
    private val userPreferencesDataStore: UserPreferencesDataStore,
    private val networkManager: NetworkConnectivityManager
) : ViewModel() {

    private val _state = MutableStateFlow(QuotesState())
    val state: StateFlow<QuotesState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            collectionsDataSource.loadCollections()
        }
        observeConnectivity()
        observeCollections()
        observeUserPreferences()
        checkOfflineData()
        handleIntent(QuotesIntent.LoadQuotes)
        handleIntent(QuotesIntent.LoadQuoteOfTheDay)
    }
    
    private fun observeConnectivity() {
        viewModelScope.launch {
            networkManager.connectivityFlow().collect { isConnected ->
                val wasOffline = !_state.value.isConnected
                
                _state.value = _state.value.copy(
                    isConnected = isConnected,
                    isOfflineMode = !isConnected,
                    offlineMessage = if (!isConnected) {
                        if (_state.value.hasOfflineData) {
                            "You're offline. Showing cached quotes."
                        } else {
                            "You're offline and no cached quotes are available."
                        }
                    } else null
                )
                
                // Auto-refresh when connection is restored
                if (isConnected && wasOffline) {
                    _state.value = _state.value.copy(
                        offlineMessage = "Connection restored. Refreshing quotes..."
                    )
                    handleIntent(QuotesIntent.RefreshQuotes)
                    handleIntent(QuotesIntent.LoadQuoteOfTheDay)
                }
            }
        }
    }
    
    private fun checkOfflineData() {
        viewModelScope.launch {
            val hasOfflineData = repository.hasOfflineData()
            _state.value = _state.value.copy(hasOfflineData = hasOfflineData)
        }
    }
    
    private fun observeCollections() {
        viewModelScope.launch {
            collectionsDataSource.collections.collect { collectionsMap ->
                _state.value = _state.value.copy(
                    collections = collectionsMap.values.sortedWith(
                        compareByDescending<com.example.quotevault.data.Collection> { it.isDefault }
                            .thenBy { it.name }
                    )
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

    fun handleIntent(intent: QuotesIntent) {
        when (intent) {
            is QuotesIntent.LoadQuotes -> loadQuotes()
            is QuotesIntent.LoadQuoteOfTheDay -> loadQuoteOfTheDay()
            is QuotesIntent.RefreshQuotes -> refreshQuotes()
            is QuotesIntent.ToggleFavorite -> toggleFavorite(intent.quoteId)
            is QuotesIntent.OpenCollectionSheet -> openCollectionSheet(intent.quoteId)
            is QuotesIntent.ToggleQuoteInCollection -> toggleQuoteInCollection(intent.quoteId, intent.collectionId)
            is QuotesIntent.CreateCollection -> createCollection(intent.name, intent.description)
            is QuotesIntent.ShareQuote -> shareQuote(intent.quote)
            is QuotesIntent.SearchQuotes -> searchQuotes(intent.query)
            is QuotesIntent.FilterByCategory -> filterByCategory(intent.category)
            is QuotesIntent.LoadNextPage -> loadNextPage()
            is QuotesIntent.CloseCollectionSheet -> closeCollectionSheet()
            is QuotesIntent.ClearError -> clearError()
            is QuotesIntent.RetryConnection -> retryConnection()
            is QuotesIntent.LoadOfflineData -> loadOfflineData()
        }
    }
    
    private fun openCollectionSheet(quoteId: String) {
        _state.value = _state.value.copy(
            showCollectionSheet = true,
            selectedQuoteForCollection = quoteId
        )
    }
    
    private fun closeCollectionSheet() {
        _state.value = _state.value.copy(
            showCollectionSheet = false,
            selectedQuoteForCollection = null
        )
    }
    
    private fun toggleQuoteInCollection(quoteId: String, collectionId: String) {
        viewModelScope.launch {
            collectionsDataSource.toggleQuoteInCollection(quoteId, collectionId)
            // Update quote favorite status if it's the default collection
            if (collectionId == com.example.quotevault.data.Collection.DEFAULT_COLLECTION_ID) {
                val updatedQuotes = _state.value.quotes.map { quote ->
                    if (quote.id == quoteId) {
                        quote.copy(isFavorite = collectionsDataSource.isQuoteInCollection(quoteId, collectionId))
                    } else {
                        quote
                    }
                }
                _state.value = _state.value.copy(quotes = updatedQuotes)
                applyFilters()
            }
        }
    }
    
    private fun createCollection(name: String, description: String) {
        viewModelScope.launch {
            collectionsDataSource.createCollection(name, description)
        }
    }
    
    private fun loadQuoteOfTheDay() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoadingQuoteOfTheDay = true)
            
            repository.getQuoteOfTheDay()
                .onSuccess { quote ->
                    _state.value = _state.value.copy(
                        quoteOfTheDay = quote,
                        isLoadingQuoteOfTheDay = false
                    )
                }
                .onFailure { error ->
                    _state.value = _state.value.copy(
                        isLoadingQuoteOfTheDay = false,
                        error = error.message ?: "Failed to load quote of the day"
                    )
                }
        }
    }

    private fun loadQuotes() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)

            repository.getQuotes()
                .onSuccess { quotes ->
                    val categories = quotes.map { it.category }.distinct().sorted()
                    _state.value = _state.value.copy(
                        quotes = quotes,
                        availableCategories = categories,
                        isLoading = false,
                        hasOfflineData = quotes.isNotEmpty()
                    )
                    applyFilters()
                    
                    // Clear offline message if we successfully loaded quotes
                    if (_state.value.isConnected) {
                        _state.value = _state.value.copy(offlineMessage = null)
                    }
                }
                .onFailure { error ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to load quotes"
                    )
                    
                    // Try to load offline data if available
                    if (!_state.value.isConnected) {
                        loadOfflineData()
                    }
                }
        }
    }
    
    private fun loadOfflineData() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            
            repository.getOfflineQuotes()
                .onSuccess { quotes ->
                    val categories = quotes.map { it.category }.distinct().sorted()
                    _state.value = _state.value.copy(
                        quotes = quotes,
                        availableCategories = categories,
                        isLoading = false,
                        hasOfflineData = true,
                        offlineMessage = "Showing cached quotes from your last online session."
                    )
                    applyFilters()
                }
                .onFailure { error ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = error.message ?: "No offline quotes available",
                        hasOfflineData = false,
                        offlineMessage = "No cached quotes available. Please connect to internet to download quotes."
                    )
                }
        }
    }

    private fun refreshQuotes() {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                isRefreshing = true,
                error = null
            )

            repository.refreshQuotes()
                .onSuccess { quotes ->
                    val categories = quotes.map { it.category }.distinct().sorted()
                    _state.value = _state.value.copy(
                        quotes = quotes,
                        availableCategories = categories,
                        isRefreshing = false,
                        hasOfflineData = true,
                        offlineMessage = null
                    )
                    applyFilters()
                }
                .onFailure { error ->
                    _state.value = _state.value.copy(
                        isRefreshing = false,
                        error = error.message ?: "Failed to refresh quotes"
                    )
                }
        }
    }

    private fun toggleFavorite(quoteId: String) {
        viewModelScope.launch {
            // Toggle in default favorites collection
            collectionsDataSource.toggleQuoteInCollection(
                quoteId, 
                com.example.quotevault.data.Collection.DEFAULT_COLLECTION_ID
            )
            
            // Optimistically update UI
            val updatedQuotes = _state.value.quotes.map { quote ->
                if (quote.id == quoteId) {
                    quote.copy(isFavorite = !quote.isFavorite)
                } else {
                    quote
                }
            }
            _state.value = _state.value.copy(quotes = updatedQuotes)
            applyFilters()

            repository.toggleFavorite(quoteId)
                .onFailure { error ->
                    // Revert on failure
                    val revertedQuotes = _state.value.quotes.map { quote ->
                        if (quote.id == quoteId) {
                            quote.copy(isFavorite = !quote.isFavorite)
                        } else {
                            quote
                        }
                    }
                    _state.value = _state.value.copy(
                        quotes = revertedQuotes,
                        error = error.message ?: "Failed to update favorite"
                    )
                    applyFilters()
                }
        }
    }

    private fun shareQuote(quote: Quote) {
        // This would typically trigger a share intent
        // For now, we'll just log it or handle it in the UI layer
    }

    private fun searchQuotes(query: String) {
        _state.value = _state.value.copy(searchQuery = query)
        applyFilters()
    }

    private fun filterByCategory(category: String?) {
        _state.value = _state.value.copy(selectedCategory = category)
        applyFilters()
    }

    private fun applyFilters() {
        val query = _state.value.searchQuery.lowercase()
        val category = _state.value.selectedCategory

        val filtered = _state.value.quotes.filter { quote ->
            val matchesSearch = query.isEmpty() ||
                    quote.text.lowercase().contains(query) ||
                    quote.author.lowercase().contains(query)

            val matchesCategory = category == null || quote.category == category

            matchesSearch && matchesCategory
        }

        val firstPage = filtered.take(PAGE_SIZE)
        _state.value = _state.value.copy(
            filteredQuotes = filtered,
            visibleQuotes = firstPage,
            currentPage = if (firstPage.isEmpty()) 0 else 1,
            hasMorePages = filtered.size > firstPage.size,
            isLoadingMore = false
        )
    }

    private fun loadNextPage() {
        val state = _state.value
        if (state.isLoadingMore || !state.hasMorePages) return

        val nextPage = state.currentPage + 1
        val nextVisibleCount = nextPage * PAGE_SIZE
        val nextVisible = state.filteredQuotes.take(nextVisibleCount)

        _state.value = state.copy(
            currentPage = nextPage,
            visibleQuotes = nextVisible,
            hasMorePages = nextVisible.size < state.filteredQuotes.size
        )
    }
    
    private fun retryConnection() {
        if (networkManager.isConnected()) {
            _state.value = _state.value.copy(
                offlineMessage = "Connection restored. Refreshing quotes...",
                error = null
            )
            handleIntent(QuotesIntent.RefreshQuotes)
            handleIntent(QuotesIntent.LoadQuoteOfTheDay)
        } else {
            _state.value = _state.value.copy(
                offlineMessage = "Still no internet connection. Please check your network settings."
            )
        }
    }
    
    private fun clearError() {
        _state.value = _state.value.copy(
            error = null,
            offlineMessage = if (!_state.value.isConnected && _state.value.hasOfflineData) {
                "You're offline. Showing cached quotes."
            } else null
        )
    }
}
