package com.example.quotevault.ui.quotes

import com.example.quotevault.data.Collection

data class QuotesState(
    val quotes: List<Quote> = emptyList(),
    val filteredQuotes: List<Quote> = emptyList(),
    val visibleQuotes: List<Quote> = emptyList(),
    val quoteOfTheDay: Quote? = null,
    val isLoadingQuoteOfTheDay: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isRefreshing: Boolean = false,
    val searchQuery: String = "",
    val selectedCategory: String? = null,
    val availableCategories: List<String> = emptyList(),
    val showCollectionSheet: Boolean = false,
    val selectedQuoteForCollection: String? = null,
    val collections: List<Collection> = emptyList(),
    val fontSizeScale: Float = 1.0f,
    val isConnected: Boolean = true,
    val isOfflineMode: Boolean = false,
    val offlineMessage: String? = null,
    val hasOfflineData: Boolean = false,
    val isLoadingMore: Boolean = false,
    val currentPage: Int = 0,
    val hasMorePages: Boolean = false
)

data class Quote(
    val id: String,
    val text: String,
    val author: String,
    val category: String,
    val isFavorite: Boolean = false
)
