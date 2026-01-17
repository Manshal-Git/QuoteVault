package com.example.quotevault.ui.quotes

import android.util.Log
import com.example.quotevault.core.auth.SupabaseClient
import com.example.quotevault.data.Collection
import com.example.quotevault.data.CollectionsDataSource
import com.example.quotevault.data.OfflineQuotesCache
import com.example.quotevault.utils.NetworkConnectivityManager
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.Serializable
import java.net.UnknownHostException
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class QuoteDto(
    val id: String,
    val text: String,
    val author: String,
    val category: String
)

private const val TAG = "QuotesRepository"

@Singleton
class QuotesRepository @Inject constructor(
    private val collectionsDataSource: CollectionsDataSource,
    private val networkManager: NetworkConnectivityManager,
    private val offlineQuotesCache: OfflineQuotesCache,
    supabaseClient: SupabaseClient
) {

    private val database = supabaseClient.client.postgrest
    
    private suspend fun fetchQuotesFromDatabase(): List<QuoteDto> {
        return try {
            if (!networkManager.isConnected()) {
                throw UnknownHostException("No internet connection")
            }
            
            val quotes = database.from("quotes").select().decodeList<QuoteDto>()
            Log.d(TAG, "Fetched ${quotes.size} quotes from database")
            quotes
        } catch (e: UnknownHostException) {
            Log.w(TAG, "Network unavailable, will use cached data if available")
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "fetchQuotesFromDatabase error: $e")
            throw e
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
     * Falls back to cached data when offline
     */
    suspend fun getQuoteOfTheDay(): Result<Quote> {
        return try {
            val quotes = if (networkManager.isConnected()) {
                try {
                    fetchQuotesFromDatabase()
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to fetch from database, trying cache", e)
                    val cachedQuotes = offlineQuotesCache.getCachedQuotes()
                    if (cachedQuotes.isEmpty()) {
                        throw Exception("No quotes available offline")
                    }
                    cachedQuotes.map { quote ->
                        QuoteDto(quote.id, quote.text, quote.author, quote.category)
                    }
                }
            } else {
                Log.d(TAG, "Offline mode: using cached quotes for QOTD")
                val cachedQuotes = offlineQuotesCache.getCachedQuotes()
                if (cachedQuotes.isEmpty()) {
                    return Result.failure(Exception("No quotes available offline. Please connect to internet to download quotes."))
                }
                cachedQuotes.map { quote ->
                    QuoteDto(quote.id, quote.text, quote.author, quote.category)
                }
            }
            
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
            
            // Cache the quote of the day if we're online
            if (networkManager.isConnected()) {
                offlineQuotesCache.cacheQuoteOfTheDay(quote)
            }
            
            Result.success(quote)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting quote of the day", e)
            
            // Try to get cached quote of the day as last resort
            val cachedQotd = offlineQuotesCache.getCachedQuoteOfTheDay()
            if (cachedQotd != null) {
                val isFavorite = collectionsDataSource.isQuoteInCollection(
                    cachedQotd.id,
                    Collection.DEFAULT_COLLECTION_ID
                )
                Result.success(cachedQotd.copy(isFavorite = isFavorite))
            } else {
                Result.failure(e)
            }
        }
    }
    
    suspend fun getQuotes(): Result<List<Quote>> {
        return try {
            val quotes = if (networkManager.isConnected()) {
                try {
                    val fetchedQuotes = fetchQuotesFromDatabase()
                    Log.d(TAG, "Successfully fetched ${fetchedQuotes.size} quotes online")
                    
                    // Cache the quotes for offline use
                    val quotesToCache = fetchedQuotes.map { dto ->
                        Quote(dto.id, dto.text, dto.author, dto.category)
                    }
                    offlineQuotesCache.cacheQuotes(quotesToCache)
                    Log.d(TAG, "Cached ${quotesToCache.size} quotes for offline use")
                    
                    fetchedQuotes
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to fetch quotes online, falling back to cache", e)
                    val cachedQuotes = offlineQuotesCache.getCachedQuotes()
                    if (cachedQuotes.isEmpty()) {
                        throw Exception("No quotes available offline")
                    }
                    cachedQuotes.map { quote ->
                        QuoteDto(quote.id, quote.text, quote.author, quote.category)
                    }
                }
            } else {
                Log.d(TAG, "Offline mode: loading cached quotes")
                val cachedQuotes = offlineQuotesCache.getCachedQuotes()
                if (cachedQuotes.isEmpty()) {
                    return Result.failure(Exception("No quotes available offline. Please connect to internet to download quotes."))
                }
                Log.d(TAG, "Loaded ${cachedQuotes.size} cached quotes")
                cachedQuotes.map { quote ->
                    QuoteDto(quote.id, quote.text, quote.author, quote.category)
                }
            }
            
            val quotesWithFavorites = quotes.map { quoteDto ->
                val isFavorite = collectionsDataSource.isQuoteInCollection(
                    quoteDto.id, 
                    Collection.DEFAULT_COLLECTION_ID
                )
                quoteDto.toQuote(isFavorite)
            }
            
            Result.success(quotesWithFavorites.shuffled())
        } catch (e: Exception) {
            Log.e(TAG, "Error getting quotes", e)
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
            Log.e(TAG, "Error toggling favorite for quote $quoteId", e)
            Result.failure(e)
        }
    }
    
    suspend fun refreshQuotes(): Result<List<Quote>> {
        return if (networkManager.isConnected()) {
            Log.d(TAG, "Refreshing quotes from server")
            getQuotes()
        } else {
            Log.d(TAG, "Cannot refresh quotes while offline")
            Result.failure(Exception("Cannot refresh quotes while offline. Please check your internet connection."))
        }
    }
    
    /**
     * Load cached quotes for offline use
     */
    suspend fun getOfflineQuotes(): Result<List<Quote>> {
        return try {
            val cachedQuotes = offlineQuotesCache.getCachedQuotes()
            if (cachedQuotes.isEmpty()) {
                Result.failure(Exception("No offline quotes available"))
            } else {
                val quotesWithFavorites = cachedQuotes.map { quote ->
                    val isFavorite = collectionsDataSource.isQuoteInCollection(
                        quote.id, 
                        Collection.DEFAULT_COLLECTION_ID
                    )
                    quote.copy(isFavorite = isFavorite)
                }
                Log.d(TAG, "Loaded ${quotesWithFavorites.size} offline quotes")
                Result.success(quotesWithFavorites.shuffled())
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading offline quotes", e)
            Result.failure(e)
        }
    }
    
    /**
     * Check if we have offline data available
     */
    suspend fun hasOfflineData(): Boolean {
        return offlineQuotesCache.getCachedQuotes().isNotEmpty()
    }
    
    /**
     * Get cache status information
     */
    suspend fun getCacheInfo(): CacheInfo {
        val hasData = hasOfflineData()
        val lastUpdate = offlineQuotesCache.getLastCacheUpdate()
        val isStale = offlineQuotesCache.isCacheStale()
        val quotesCount = offlineQuotesCache.getCachedQuotes().size
        
        return CacheInfo(
            hasData = hasData,
            lastUpdate = lastUpdate,
            isStale = isStale,
            quotesCount = quotesCount
        )
    }
}

data class CacheInfo(
    val hasData: Boolean,
    val lastUpdate: Long,
    val isStale: Boolean,
    val quotesCount: Int
)
