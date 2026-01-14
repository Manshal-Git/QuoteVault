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
            is QuotesIntent.ClearError -> clearError()
        }
    }

    private fun loadQuotes() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)

            repository.getQuotes()
                .onSuccess { quotes ->
                    _state.value = _state.value.copy(
                        quotes = quotes,
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
                    _state.value = _state.value.copy(
                        quotes = quotes,
                        isRefreshing = false
                    )
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
            _state.value = _state.value.copy(quotes = updatedQuotes)

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
                }
        }
    }

    private fun shareQuote(quote: Quote) {
        // This would typically trigger a share intent
        // For now, we'll just log it or handle it in the UI layer
    }

    private fun clearError() {
        _state.value = _state.value.copy(error = null)
    }
}
