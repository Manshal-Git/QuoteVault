package com.example.quotevault.ui.favourites

import com.example.quotevault.ui.quotes.Quote

data class FavouritesState(
    val favoriteQuotes: List<Quote> = emptyList(),
    val visibleFavoriteQuotes: List<Quote> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val hasMorePages: Boolean = false,
    val currentPage: Int = 0,
    val error: String? = null,
    val fontSizeScale: Float = 1.0f
)
