package com.example.quotevault.ui.favourites

import com.example.quotevault.ui.quotes.Quote

data class FavouritesState(
    val favoriteQuotes: List<Quote> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
