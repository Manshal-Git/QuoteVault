package com.example.quotevault.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Single source of truth for favorite quotes.
 * This data source is shared across all repositories that need to access favorites.
 */
@Singleton
class FavouritesDataSource @Inject constructor() {
    
    private val _favoriteQuoteIds = MutableStateFlow<Set<String>>(emptySet())
    val favoriteQuoteIds: StateFlow<Set<String>> = _favoriteQuoteIds.asStateFlow()
    
    /**
     * Check if a quote is marked as favorite
     */
    fun isFavorite(quoteId: String): Boolean {
        return _favoriteQuoteIds.value.contains(quoteId)
    }
    
    /**
     * Add a quote to favorites
     */
    fun addFavorite(quoteId: String) {
        _favoriteQuoteIds.value = _favoriteQuoteIds.value + quoteId
    }
    
    /**
     * Remove a quote from favorites
     */
    fun removeFavorite(quoteId: String) {
        _favoriteQuoteIds.value = _favoriteQuoteIds.value - quoteId
    }
    
    /**
     * Toggle favorite status for a quote
     * @return true if added to favorites, false if removed
     */
    fun toggleFavorite(quoteId: String): Boolean {
        return if (isFavorite(quoteId)) {
            removeFavorite(quoteId)
            false
        } else {
            addFavorite(quoteId)
            true
        }
    }
    
    /**
     * Get all favorite quote IDs
     */
    fun getAllFavorites(): Set<String> {
        return _favoriteQuoteIds.value
    }
    
    /**
     * Clear all favorites
     */
    fun clearAllFavorites() {
        _favoriteQuoteIds.value = emptySet()
    }
}
