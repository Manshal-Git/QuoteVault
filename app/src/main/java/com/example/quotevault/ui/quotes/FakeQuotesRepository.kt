package com.example.quotevault.ui.quotes

import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeQuotesRepository @Inject constructor() {
    
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
    
    private val favoriteQuotes = mutableSetOf<String>()
    
    suspend fun getQuotes(): Result<List<Quote>> {
        delay(1000) // Simulate network delay
        
        return try {
            val quotesWithFavorites = sampleQuotes.map { quote ->
                quote.copy(isFavorite = favoriteQuotes.contains(quote.id))
            }
            Result.success(quotesWithFavorites)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun toggleFavorite(quoteId: String): Result<Boolean> {
        delay(300) // Simulate network delay
        
        return try {
            val isFavorite = if (favoriteQuotes.contains(quoteId)) {
                favoriteQuotes.remove(quoteId)
                false
            } else {
                favoriteQuotes.add(quoteId)
                true
            }
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
