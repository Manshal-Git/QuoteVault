package com.example.quotevault.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.example.quotevault.ui.quotes.Quote
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

private val Context.offlineQuotesDataStore: DataStore<Preferences> by preferencesDataStore(name = "offline_quotes")

@Serializable
data class CachedQuote(
    val id: String,
    val text: String,
    val author: String,
    val category: String
)

@Singleton
class OfflineQuotesCache @Inject constructor(
    private val context: Context
) {
    
    private object PreferencesKeys {
        val CACHED_QUOTES = stringPreferencesKey("cached_quotes")
        val CACHED_QUOTE_OF_THE_DAY = stringPreferencesKey("cached_quote_of_the_day")
        val LAST_CACHE_UPDATE = longPreferencesKey("last_cache_update")
        val CACHE_VERSION = intPreferencesKey("cache_version")
    }
    
    private val json = Json { ignoreUnknownKeys = true }
    
    /**
     * Cache quotes for offline access
     */
    suspend fun cacheQuotes(quotes: List<Quote>) {
        val cachedQuotes = quotes.map { quote ->
            CachedQuote(
                id = quote.id,
                text = quote.text,
                author = quote.author,
                category = quote.category
            )
        }
        
        context.offlineQuotesDataStore.edit { preferences ->
            preferences[PreferencesKeys.CACHED_QUOTES] = json.encodeToString(cachedQuotes)
            preferences[PreferencesKeys.LAST_CACHE_UPDATE] = System.currentTimeMillis()
            preferences[PreferencesKeys.CACHE_VERSION] = 1
        }
    }
    
    /**
     * Get cached quotes for offline access
     */
    suspend fun getCachedQuotes(): List<Quote> {
        val preferences = context.offlineQuotesDataStore.data.first()
        val cachedQuotesJson = preferences[PreferencesKeys.CACHED_QUOTES]
        
        return if (cachedQuotesJson != null) {
            try {
                val cachedQuotes = json.decodeFromString<List<CachedQuote>>(cachedQuotesJson)
                cachedQuotes.map { cached ->
                    Quote(
                        id = cached.id,
                        text = cached.text,
                        author = cached.author,
                        category = cached.category,
                        isFavorite = false // Will be updated by checking collections
                    )
                }
            } catch (e: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }
    }
    
    /**
     * Cache quote of the day
     */
    suspend fun cacheQuoteOfTheDay(quote: Quote) {
        val cachedQuote = CachedQuote(
            id = quote.id,
            text = quote.text,
            author = quote.author,
            category = quote.category
        )
        
        context.offlineQuotesDataStore.edit { preferences ->
            preferences[PreferencesKeys.CACHED_QUOTE_OF_THE_DAY] = json.encodeToString(cachedQuote)
        }
    }
    
    /**
     * Get cached quote of the day
     */
    suspend fun getCachedQuoteOfTheDay(): Quote? {
        val preferences = context.offlineQuotesDataStore.data.first()
        val cachedQuoteJson = preferences[PreferencesKeys.CACHED_QUOTE_OF_THE_DAY]
        
        return if (cachedQuoteJson != null) {
            try {
                val cachedQuote = json.decodeFromString<CachedQuote>(cachedQuoteJson)
                Quote(
                    id = cachedQuote.id,
                    text = cachedQuote.text,
                    author = cachedQuote.author,
                    category = cachedQuote.category,
                    isFavorite = false // Will be updated by checking collections
                )
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }
    }
    
    /**
     * Check if we have cached data
     */
    fun hasCachedData(): Flow<Boolean> {
        return context.offlineQuotesDataStore.data.map { preferences ->
            preferences[PreferencesKeys.CACHED_QUOTES] != null
        }
    }
    
    /**
     * Get last cache update timestamp
     */
    suspend fun getLastCacheUpdate(): Long {
        val preferences = context.offlineQuotesDataStore.data.first()
        return preferences[PreferencesKeys.LAST_CACHE_UPDATE] ?: 0L
    }
    
    /**
     * Clear all cached data
     */
    suspend fun clearCache() {
        context.offlineQuotesDataStore.edit { preferences ->
            preferences.clear()
        }
    }
    
    /**
     * Check if cache is stale (older than 24 hours)
     */
    suspend fun isCacheStale(): Boolean {
        val lastUpdate = getLastCacheUpdate()
        val now = System.currentTimeMillis()
        val twentyFourHours = 24 * 60 * 60 * 1000L
        return (now - lastUpdate) > twentyFourHours
    }
}