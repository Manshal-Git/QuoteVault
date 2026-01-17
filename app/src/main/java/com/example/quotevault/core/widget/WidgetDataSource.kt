package com.example.quotevault.core.widget

import android.util.Log
import com.example.quotevault.core.auth.SupabaseClient
import com.example.quotevault.ui.quotes.Quote
import com.example.quotevault.ui.quotes.QuoteDto
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WidgetDataSource @Inject constructor(
    private val supabaseClient: SupabaseClient
) {
    
    private val database = supabaseClient.client.postgrest
    
    companion object {
        private const val TAG = "WidgetDataSource"
        
        // Fallback quotes in case of network issues
        private val FALLBACK_QUOTES = listOf(
            Quote(
                id = "fallback_1",
                text = "The only way to do great work is to love what you do.",
                author = "Steve Jobs",
                category = "Motivation"
            ),
            Quote(
                id = "fallback_2", 
                text = "Innovation distinguishes between a leader and a follower.",
                author = "Steve Jobs",
                category = "Leadership"
            ),
            Quote(
                id = "fallback_3",
                text = "Life is what happens when you're busy making other plans.",
                author = "John Lennon",
                category = "Life"
            ),
            Quote(
                id = "fallback_4",
                text = "The future belongs to those who believe in the beauty of their dreams.",
                author = "Eleanor Roosevelt",
                category = "Dreams"
            ),
            Quote(
                id = "fallback_5",
                text = "It is during our darkest moments that we must focus to see the light.",
                author = "Aristotle",
                category = "Inspiration"
            )
        )
    }
    
    /**
     * Get quote of the day for widget display
     * Uses day of year to ensure same quote throughout the day
     */
    suspend fun getQuoteOfTheDay(): Quote {
        return withContext(Dispatchers.IO) {
            try {
                // Try to fetch from database first
                val quotes = fetchQuotesFromDatabase()
                
                if (quotes.isNotEmpty()) {
                    val calendar = java.util.Calendar.getInstance()
                    val dayOfYear = calendar.get(java.util.Calendar.DAY_OF_YEAR)
                    val quoteIndex = dayOfYear % quotes.size
                    val quoteDto = quotes[quoteIndex]
                    
                    Quote(
                        id = quoteDto.id,
                        text = quoteDto.text,
                        author = quoteDto.author,
                        category = quoteDto.category,
                        isFavorite = false // Widget doesn't need favorite status
                    )
                } else {
                    // Fallback to hardcoded quotes
                    getFallbackQuoteOfTheDay()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching quote of the day, using fallback", e)
                getFallbackQuoteOfTheDay()
            }
        }
    }
    
    private suspend fun fetchQuotesFromDatabase(): List<QuoteDto> {
        return try {
            database.from("quotes").select().decodeList<QuoteDto>()
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching quotes from database", e)
            emptyList()
        }
    }
    
    private fun getFallbackQuoteOfTheDay(): Quote {
        val calendar = java.util.Calendar.getInstance()
        val dayOfYear = calendar.get(java.util.Calendar.DAY_OF_YEAR)
        val quoteIndex = dayOfYear % FALLBACK_QUOTES.size
        return FALLBACK_QUOTES[quoteIndex]
    }
}