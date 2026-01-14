package com.example.quotevault.ui.quotes

data class QuotesState(
    val quotes: List<Quote> = emptyList(),
    val filteredQuotes: List<Quote> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isRefreshing: Boolean = false,
    val searchQuery: String = "",
    val selectedCategory: String? = null,
    val availableCategories: List<String> = emptyList()
)

data class Quote(
    val id: String,
    val text: String,
    val author: String,
    val category: String,
    val isFavorite: Boolean = false
)
