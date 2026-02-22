package com.example.quotevault.ui.favourites

import com.example.quotevault.ui.quotes.Quote

sealed class FavouritesIntent {
    object LoadFavorites : FavouritesIntent()
    data class RemoveFavorite(val quoteId: String) : FavouritesIntent()
    data class ShareQuote(val quote: Quote) : FavouritesIntent()
    object ClearAllFavorites : FavouritesIntent()
    object LoadNextPage : FavouritesIntent()
    object ClearError : FavouritesIntent()
}
