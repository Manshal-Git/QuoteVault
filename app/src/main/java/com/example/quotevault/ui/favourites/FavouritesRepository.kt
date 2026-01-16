package com.example.quotevault.ui.favourites

import android.util.Log
import com.example.quotevault.core.auth.SupabaseClient
import com.example.quotevault.data.Collection
import com.example.quotevault.data.CollectionsDataSource
import com.example.quotevault.ui.quotes.Quote
import com.example.quotevault.ui.quotes.QuoteDto
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "FavouritesRepository"

@Singleton
class FavouritesRepository @Inject constructor(
    private val collectionsDataSource: CollectionsDataSource,
    private val supabaseClient: SupabaseClient
) {
    
    private val database = supabaseClient.client.postgrest
    
    private suspend fun fetchQuotesFromDatabase(): List<QuoteDto> {
        return try {
            database.from("quotes").select().decodeList<QuoteDto>()
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching quotes from database", e)
            emptyList()
        }
    }
    
    private fun QuoteDto.toQuote(isFavorite: Boolean = false): Quote {
        return Quote(
            id = this.id,
            text = this.text,
            author = this.author,
            category = this.category,
            isFavorite = isFavorite
        )
    }
    
    /**
     * Get all favorite quotes as a Flow that updates when favorites change
     */
    fun getFavoriteQuotesFlow(): Flow<List<Quote>> {
        return collectionsDataSource.collections.map { collectionsMap ->
            val favoriteCollection = collectionsMap[Collection.DEFAULT_COLLECTION_ID]
            val favoriteIds = favoriteCollection?.quoteIds ?: emptySet()
            
            if (favoriteIds.isEmpty()) {
                emptyList()
            } else {
                try {
                    val allQuotes = fetchQuotesFromDatabase()
                    allQuotes.filter { quoteDto ->
                        favoriteIds.contains(quoteDto.id)
                    }.map { quoteDto ->
                        quoteDto.toQuote(isFavorite = true)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "getFavoriteQuotesFlow: $e")
                    emptyList()
                }
            }
        }
    }
    
    /**
     * Get favorite quotes (one-time fetch)
     */
    suspend fun getFavoriteQuotes(): Result<List<Quote>> {
        return try {
            val favoriteIds = collectionsDataSource.getFavoriteQuoteIds()
            
            if (favoriteIds.isEmpty()) {
                return Result.success(emptyList())
            }
            
            val allQuotes = fetchQuotesFromDatabase()
            val favorites = allQuotes.filter { quoteDto ->
                favoriteIds.contains(quoteDto.id)
            }.map { quoteDto ->
                quoteDto.toQuote(isFavorite = true)
            }
            
            Result.success(favorites)
        } catch (e: Exception) {
            Log.e(TAG, "getFavoriteQuotes: $e")
            Result.failure(e)
        }
    }
    
    /**
     * Remove a quote from favorites
     */
    suspend fun removeFavorite(quoteId: String): Result<Unit> {
        return try {
            collectionsDataSource.removeQuoteFromCollection(quoteId, Collection.DEFAULT_COLLECTION_ID)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error removing favorite quote $quoteId", e)
            Result.failure(e)
        }
    }
    
    /**
     * Clear all favorites
     */
    suspend fun clearAllFavorites(): Result<Unit> {
        return try {
            collectionsDataSource.clearCollection(Collection.DEFAULT_COLLECTION_ID)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing all favorites", e)
            Result.failure(e)
        }
    }
}
