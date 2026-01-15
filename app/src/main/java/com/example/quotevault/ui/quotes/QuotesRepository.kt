package com.example.quotevault.ui.quotes

import android.util.Log
import com.example.quotevault.core.auth.SupabaseClient
import com.example.quotevault.data.Collection
import com.example.quotevault.data.CollectionsDataSource
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.Serializable
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class QuoteDto(
    val id: String,
    val text: String,
    val author: String,
    val category: String
)

private const val TAG = "FakeQuotesRepository"

@Singleton
class QuotesRepository @Inject constructor(
    private val collectionsDataSource: CollectionsDataSource,
    supabaseClient: SupabaseClient
) {

    private val database = supabaseClient.client.postgrest
    
    private suspend fun fetchQuotesFromDatabase(): List<QuoteDto> {
        return try {
            database.from("quotes").select().decodeList<QuoteDto>()
        } catch (e: Exception) {
            // Fallback to empty list if database fetch fails
            Log.e(TAG, "fetchQuotesFromDatabase: $e", )
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
     * Get quote of the day - returns a consistent quote for the current day
     * Uses day of year to ensure same quote throughout the day
     */
    suspend fun getQuoteOfTheDay(): Result<Quote> {
        return try {
            val quotes = fetchQuotesFromDatabase()
            
            if (quotes.isEmpty()) {
                return Result.failure(Exception("No quotes available"))
            }
            
            val calendar = java.util.Calendar.getInstance()
            val dayOfYear = calendar.get(java.util.Calendar.DAY_OF_YEAR)
            val quoteIndex = dayOfYear % quotes.size
            val quoteDto = quotes[quoteIndex]
            
            val isFavorite = collectionsDataSource.isQuoteInCollection(
                quoteDto.id,
                Collection.DEFAULT_COLLECTION_ID
            )
            
            val quote = quoteDto.toQuote(isFavorite)
            Result.success(quote)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getQuotes(): Result<List<Quote>> {
        return try {
            val quotes = fetchQuotesFromDatabase()
            
            val quotesWithFavorites = quotes.map { quoteDto ->
                val isFavorite = collectionsDataSource.isQuoteInCollection(
                    quoteDto.id, 
                    Collection.DEFAULT_COLLECTION_ID
                )
                quoteDto.toQuote(isFavorite)
            }
            
            Result.success(quotesWithFavorites.shuffled())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun toggleFavorite(quoteId: String): Result<Boolean> {
        return try {
            val isFavorite = collectionsDataSource.toggleQuoteInCollection(
                quoteId, 
                Collection.DEFAULT_COLLECTION_ID
            )
            Result.success(isFavorite)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun refreshQuotes(): Result<List<Quote>> {
        return getQuotes()
    }
}
