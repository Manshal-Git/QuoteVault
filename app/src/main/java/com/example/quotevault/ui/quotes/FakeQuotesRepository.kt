package com.example.quotevault.ui.quotes

import com.example.quotevault.data.Collection
import com.example.quotevault.data.CollectionsDataSource
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeQuotesRepository @Inject constructor(
    private val collectionsDataSource: CollectionsDataSource
) {
    
    private val sampleQuotes = listOf(
        Quote(
            id = "1",
            text = "The only way to do great work is to love what you do.",
            author = "Steve Jobs",
            category = "Motivation"
        ),
        Quote(
            id = "2",
            text = "Innovation distinguishes between a leader and a follower.",
            author = "Steve Jobs",
            category = "Leadership"
        ),
        Quote(
            id = "3",
            text = "Life is what happens when you're busy making other plans.",
            author = "John Lennon",
            category = "Life"
        ),
        Quote(
            id = "4",
            text = "The future belongs to those who believe in the beauty of their dreams.",
            author = "Eleanor Roosevelt",
            category = "Dreams"
        ),
        Quote(
            id = "5",
            text = "It is during our darkest moments that we must focus to see the light.",
            author = "Aristotle",
            category = "Inspiration"
        ),
        Quote(
            id = "6",
            text = "The only impossible journey is the one you never begin.",
            author = "Tony Robbins",
            category = "Motivation"
        ),
        Quote(
            id = "7",
            text = "Success is not final, failure is not fatal: it is the courage to continue that counts.",
            author = "Winston Churchill",
            category = "Success"
        ),
        Quote(
            id = "8",
            text = "Believe you can and you're halfway there.",
            author = "Theodore Roosevelt",
            category = "Belief"
        ),
        Quote(
            id = "9",
            text = "The best time to plant a tree was 20 years ago. The second best time is now.",
            author = "Chinese Proverb",
            category = "Wisdom"
        ),
        Quote(
            id = "10",
            text = "Your time is limited, don't waste it living someone else's life.",
            author = "Steve Jobs",
            category = "Life"
        ),
        Quote(
            id = "11",
            text = "The way to get started is to quit talking and begin doing.",
            author = "Walt Disney",
            category = "Action"
        ),
        Quote(
            id = "12",
            text = "Don't let yesterday take up too much of today.",
            author = "Will Rogers",
            category = "Mindfulness"
        ),
        Quote(
            id = "13",
            text = "You learn more from failure than from success. Don't let it stop you.",
            author = "Unknown",
            category = "Failure"
        ),
        Quote(
            id = "14",
            text = "It's not whether you get knocked down, it's whether you get up.",
            author = "Vince Lombardi",
            category = "Resilience"
        ),
        Quote(
            id = "15",
            text = "If you are working on something that you really care about, you don't have to be pushed.",
            author = "Steve Jobs",
            category = "Passion"
        ),
        Quote(
            id = "16",
            text = "People who are crazy enough to think they can change the world, are the ones who do.",
            author = "Rob Siltanen",
            category = "Change"
        ),
        Quote(
            id = "17",
            text = "Failure will never overtake me if my determination to succeed is strong enough.",
            author = "Og Mandino",
            category = "Determination"
        ),
        Quote(
            id = "18",
            text = "We may encounter many defeats but we must not be defeated.",
            author = "Maya Angelou",
            category = "Perseverance"
        ),
        Quote(
            id = "19",
            text = "Knowing is not enough; we must apply. Wishing is not enough; we must do.",
            author = "Johann Wolfgang Von Goethe",
            category = "Action"
        ),
        Quote(
            id = "20",
            text = "Imagine your life is perfect in every respect; what would it look like?",
            author = "Brian Tracy",
            category = "Vision"
        )
    )
    
    /**
     * Get quote of the day - returns a consistent quote for the current day
     * Uses day of year to ensure same quote throughout the day
     */
    suspend fun getQuoteOfTheDay(): Result<Quote> {
        delay(800) // Simulate network delay
        
        return try {
            val calendar = java.util.Calendar.getInstance()
            val dayOfYear = calendar.get(java.util.Calendar.DAY_OF_YEAR)
            val quoteIndex = dayOfYear % sampleQuotes.size
            val quote = sampleQuotes[quoteIndex]
            
            val quoteWithFavorite = quote.copy(
                isFavorite = collectionsDataSource.isQuoteInCollection(
                    quote.id,
                    Collection.DEFAULT_COLLECTION_ID
                )
            )
            
            Result.success(quoteWithFavorite)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getQuotes(): Result<List<Quote>> {
        delay(1000) // Simulate network delay
        
        return try {
            val quotesWithFavorites = sampleQuotes.map { quote ->
                quote.copy(
                    isFavorite = collectionsDataSource.isQuoteInCollection(
                        quote.id, 
                        Collection.DEFAULT_COLLECTION_ID
                    )
                )
            }
            Result.success(quotesWithFavorites.shuffled())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun toggleFavorite(quoteId: String): Result<Boolean> {
        delay(300) // Simulate network delay
        
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
        delay(1500) // Simulate network delay
        return getQuotes()
    }
}
