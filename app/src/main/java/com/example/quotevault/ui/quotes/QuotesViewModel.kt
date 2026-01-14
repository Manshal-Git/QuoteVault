package com.example.quotevault.ui.quotes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QuotesViewModel @Inject constructor(
    private val repository: FakeQuotesRepository
) : ViewModel() {

    private val _state = MutableStateFlow(QuotesState())
    val state: StateFlow<QuotesState> = _state.asStateFlow()

    init {
        handleIntent(QuotesIntent.LoadQuotes)
    }

    fun handleIntent(intent: QuotesIntent) {
        when (intent) {
            is QuotesIntent.LoadQuotes -> loadQuotes()
            is QuotesIntent.RefreshQuotes -> refreshQuotes()
            is QuotesIntent.ToggleFavorite -> toggleFavorite(intent.quoteId)
            is QuotesIntent.ShareQuote -> shareQuote(intent.quote)
            is QuotesIntent.SearchQuotes -> searchQuotes(intent.query)
            is QuotesIntent.FilterByCategory -> filterByCategory(intent.category)
            is QuotesIntent.ClearError -> clearError()
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
                        filteredQuotes = quotes,
                        availableCategories = categories,
                        isLoading = false
                    )
                }
                .onFailure { error ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to load quotes"
                    )
                }
        }
    }

    private fun refreshQuotes() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isRefreshing = true, error = null)

            repository.refreshQuotes()
                .onSuccess { quotes ->
                    val categories = quotes.map { it.category }.distinct().sorted()
                    _state.value = _state.value.copy(
                        quotes = quotes,
                        availableCategories = categories,
                        isRefreshing = false
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
            // Optimistically update UI
            val updatedQuotes = _state.value.quotes.map { quote ->
                if (quote.id == quoteId) {
                    quote.copy(isFavorite = !quote.isFavorite)
                } else {
                    quote
                }
            }
            val updatedFilteredQuotes = _state.value.filteredQuotes.map { quote ->
                if (quote.id == quoteId) {
                    quote.copy(isFavorite = !quote.isFavorite)
                } else {
                    quote
                }
            }
            _state.value =
                _state.value.copy(quotes = updatedQuotes, filteredQuotes = updatedFilteredQuotes)

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
                    val revertedFilteredQuotes = _state.value.filteredQuotes.map { quote ->
                        if (quote.id == quoteId) {
                            quote.copy(isFavorite = !quote.isFavorite)
                        } else {
                            quote
                        }
                    }
                    _state.value = _state.value.copy(
                        quotes = revertedQuotes,
                        filteredQuotes = revertedFilteredQuotes,
                        error = error.message ?: "Failed to update favorite"
                    )
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

        _state.value = _state.value.copy(filteredQuotes = filtered)
    }
    
    private fun clearError() {
        _state.value = _state.value.copy(error = null)
    }
}
