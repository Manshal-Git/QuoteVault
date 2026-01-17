package com.example.quotevault.ui.quotes

import com.example.quotevault.data.Collection

data class QuotesState(
    val quotes: List<Quote> = emptyList(),
    val filteredQuotes: List<Quote> = emptyList(),
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
    val fontSizeScale: Float = 1.0f
)

data class Quote(
    val id: String,
    val text: String,
    val author: String,
    val category: String,
    val isFavorite: Boolean = false
)
