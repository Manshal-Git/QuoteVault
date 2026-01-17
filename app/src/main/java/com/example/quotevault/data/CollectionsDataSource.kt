package com.example.quotevault.data

import android.util.Log
import com.example.quotevault.core.auth.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class CollectionDto(
    val id: String,
    val user_id: String,
    val name: String,
    val description: String? = null,
    val created_at: String
)

@Serializable
data class CollectionQuoteDto(
    val collection_id: String,
    val quote_id: String
)

@Serializable
data class FavouriteDto(
    val user_id: String,
    val quote_id: String,
    val created_at: String
)

private const val COLLECTION_QUOTES_TABLE = "collection_quotes"
private const val COLLECTIONS_TABLE = "collections"

/**
 * Single source of truth for collections and quote-collection relationships.
 * Now uses Supabase for persistence.
 */
@Singleton
class CollectionsDataSource @Inject constructor(
    private val supabaseClient: SupabaseClient
) {
    
    private val database = supabaseClient.client.postgrest
    private val auth = supabaseClient.client.auth
    
    private val _collections = MutableStateFlow<Map<String, Collection>>(emptyMap())
    val collections: StateFlow<Map<String, Collection>> = _collections.asStateFlow()
    
    companion object {
        private const val TAG = "CollectionsDataSource"
    }
    
    private suspend fun getCurrentUserId(): String? {
        return auth.currentUserOrNull()?.id
    }
    
    private suspend fun ensureDefaultCollection() {
        val userId = getCurrentUserId() ?: return
        val defaultCollection = _collections.value[Collection.DEFAULT_COLLECTION_ID]
        
        if (defaultCollection == null) {
            // Create default collection in database
            try {
                val collectionDto = CollectionDto(
                    id = Collection.DEFAULT_COLLECTION_ID,
                    user_id = userId,
                    name = "Favorites",
                    description = "Your favorite quotes",
                    created_at = java.time.Instant.now().toString()
                )
                
                database.from(COLLECTIONS_TABLE).insert(collectionDto)
                
                val defaultCol = Collection.createDefault()
                _collections.value = _collections.value + (Collection.DEFAULT_COLLECTION_ID to defaultCol)
            } catch (e: Exception) {
                Log.e(TAG, "Error creating default collection in database", e)
                // If collection already exists, just add to local state
                val defaultCol = Collection.createDefault()
                _collections.value = _collections.value + (Collection.DEFAULT_COLLECTION_ID to defaultCol)
            }
        }
    }
    
    suspend fun loadCollections() {
        val userId = getCurrentUserId() ?: return
        
        try {
            // Load collections from database
            val collectionsDto = database.from(COLLECTIONS_TABLE)
                .select {
                    filter {
                        eq("user_id", userId)
                    }
                }
                .decodeList<CollectionDto>()
            
            // Load collection-quote relationships
            val collectionQuotes = database.from(COLLECTION_QUOTES_TABLE)
                .select()
                .decodeList<CollectionQuoteDto>()
            
            // Load favorites (for backward compatibility)
            val favourites = database.from(Collection.DEFAULT_COLLECTION_ID)
                .select {
                    filter {
                        eq("user_id", userId)
                    }
                }
                .decodeList<FavouriteDto>()

            // Build collections map
            val collectionsMap = mutableMapOf<String, Collection>()
            
            collectionsDto.forEach { dto ->
                val quoteIds = collectionQuotes
                    .filter { it.collection_id == dto.id }
                    .map { it.quote_id }
                    .toSet()
                
                collectionsMap[dto.id] = Collection(
                    id = dto.id,
                    name = dto.name,
                    description = dto.description ?: "",
                    quoteIds = quoteIds,
                    createdAt = System.currentTimeMillis(), // Could parse created_at if needed
                    isDefault = dto.id == Collection.DEFAULT_COLLECTION_ID
                )
            }
            
            // Add favorites to default collection if it exists
            val defaultCollection = collectionsMap[Collection.DEFAULT_COLLECTION_ID]
            if (defaultCollection != null) {
                val favoriteQuoteIds = favourites.map { it.quote_id }.toSet()
                collectionsMap[Collection.DEFAULT_COLLECTION_ID] = defaultCollection.copy(
                    quoteIds = defaultCollection.quoteIds + favoriteQuoteIds
                )
            } else {
                val favoriteQuoteIds = favourites.map { it.quote_id }.toSet()
                collectionsMap[Collection.DEFAULT_COLLECTION_ID] = Collection.createDefault().copy(
                    quoteIds = favoriteQuoteIds
                )
            }
            
            _collections.value = collectionsMap
            
            // Ensure default collection exists
            ensureDefaultCollection()
            
        } catch (e: Exception) {
            Log.e(TAG, "Error loading collections from database", e)
            // Fallback to default collection only
            ensureDefaultCollection()
        }
    }
    
    /**
     * Get all collections as a list
     */
    fun getAllCollections(): List<Collection> {
        return _collections.value.values.sortedWith(
            compareByDescending<Collection> { it.isDefault }
                .thenBy { it.name }
        )
    }
    
    /**
     * Get a specific collection by ID
     */
    fun getCollection(collectionId: String): Collection? {
        return _collections.value[collectionId]
    }
    
    /**
     * Create a new collection
     */
    suspend fun createCollection(name: String, description: String = ""): Collection? {
        val userId = getCurrentUserId() ?: return null
        
        try {
            val newCollectionId = UUID.randomUUID().toString()
            val collectionDto = CollectionDto(
                id = newCollectionId,
                user_id = userId,
                name = name,
                description = description,
                created_at = java.time.Instant.now().toString()
            )
            
            database.from(COLLECTIONS_TABLE).insert(collectionDto)
            
            val newCollection = Collection(
                id = newCollectionId,
                name = name,
                description = description
            )
            
            _collections.value = _collections.value + (newCollection.id to newCollection)
            return newCollection
        } catch (e: Exception) {
            Log.e(TAG, "Error creating collection: $name", e)
            return null
        }
    }
    
    /**
     * Delete a collection (cannot delete default collection)
     */
    suspend fun deleteCollection(collectionId: String): Boolean {
        if (collectionId == Collection.DEFAULT_COLLECTION_ID) {
            return false
        }
        
        val userId = getCurrentUserId() ?: return false
        
        try {
            // Delete collection-quote relationships first
            database.from(COLLECTION_QUOTES_TABLE)
                .delete {
                    filter {
                        eq("collection_id", collectionId)
                    }
                }
            
            // Delete collection
            database.from(COLLECTIONS_TABLE)
                .delete {
                    filter {
                        eq("id", collectionId)
                        eq("user_id", userId)
                    }
                }
            
            _collections.value = _collections.value - collectionId
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting collection: $collectionId", e)
            return false
        }
    }
    
    /**
     * Add a quote to a collection with upsert behavior (insert if not exists)
     * This is a safer alternative to addQuoteToCollection that handles duplicates gracefully
     */
    suspend fun upsertQuoteToCollection(quoteId: String, collectionId: String) {
        try {
            // Check local state first for performance
            val collection = _collections.value[collectionId]
            if (collection?.quoteIds?.contains(quoteId) == true) {
                return // Already exists locally
            }
            
            if (collectionId == Collection.DEFAULT_COLLECTION_ID) {
                val userId = getCurrentUserId() ?: return
                
                // Use upsert approach: try to insert, ignore if already exists
                try {
                    val favouriteDto = FavouriteDto(
                        user_id = userId,
                        quote_id = quoteId,
                        created_at = java.time.Instant.now().toString()
                    )
                    database.from(Collection.DEFAULT_COLLECTION_ID).insert(favouriteDto)
                } catch (e: Exception) {
                    // If it's a unique constraint violation, that's fine - the record already exists
                    if (e.message?.contains("duplicate key") == true || 
                        e.message?.contains("unique constraint") == true ||
                        e.message?.contains("UNIQUE constraint") == true) {
                        Log.d(TAG, "Quote $quoteId already exists in favorites (expected)")
                    } else {
                        throw e // Re-throw if it's a different error
                    }
                }
            } else {
                // Similar approach for regular collections
                try {
                    val collectionQuoteDto = CollectionQuoteDto(
                        collection_id = collectionId,
                        quote_id = quoteId
                    )
                    database.from(COLLECTION_QUOTES_TABLE).insert(collectionQuoteDto)
                } catch (e: Exception) {
                    if (e.message?.contains("duplicate key") == true || 
                        e.message?.contains("unique constraint") == true ||
                        e.message?.contains("UNIQUE constraint") == true) {
                        Log.d(TAG, "Quote $quoteId already exists in collection $collectionId (expected)")
                    } else {
                        throw e
                    }
                }
            }
            
            // Update local state regardless of database operation result
            val updatedCollection = (collection ?: Collection.createDefault()).copy(
                quoteIds = (collection?.quoteIds ?: emptySet()) + quoteId
            )
            _collections.value = _collections.value + (collectionId to updatedCollection)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error upserting quote $quoteId to collection $collectionId", e)
        }
    }

    /**
     * Add a quote to a collection
     */
    suspend fun addQuoteToCollection(quoteId: String, collectionId: String) {
        // Use the safer upsert method
        upsertQuoteToCollection(quoteId, collectionId)
    }
    
    /**
     * Remove a quote from a collection
     */
    suspend fun removeQuoteFromCollection(quoteId: String, collectionId: String) {
        try {
            if (collectionId == Collection.DEFAULT_COLLECTION_ID) {
                // Remove from favourites table
                val userId = getCurrentUserId() ?: return
                database.from(Collection.DEFAULT_COLLECTION_ID)
                    .delete {
                        filter {
                            eq("user_id", userId)
                            eq("quote_id", quoteId)
                        }
                    }
            } else {
                // Remove from collection_quote table
                database.from(COLLECTION_QUOTES_TABLE)
                    .delete {
                        filter {
                            eq("collection_id", collectionId)
                            eq("quote_id", quoteId)
                        }
                    }
            }
            
            // Update local state
            val collection = _collections.value[collectionId] ?: return
            val updatedCollection = collection.copy(
                quoteIds = collection.quoteIds - quoteId
            )
            _collections.value = _collections.value + (collectionId to updatedCollection)
        } catch (e: Exception) {
            Log.e(TAG, "Error removing quote $quoteId from collection $collectionId", e)
        }
    }
    
    /**
     * Toggle quote in a collection
     * @return true if added, false if removed
     */
    suspend fun toggleQuoteInCollection(quoteId: String, collectionId: String): Boolean {
        // Ensure collection exists, create default if needed
        if (collectionId == Collection.DEFAULT_COLLECTION_ID && _collections.value[collectionId] == null) {
            ensureDefaultCollection()
        }
        
        val collection = _collections.value[collectionId] ?: return false
        return if (collection.quoteIds.contains(quoteId)) {
            removeQuoteFromCollection(quoteId, collectionId)
            false
        } else {
            addQuoteToCollection(quoteId, collectionId)
            true
        }
    }
    
    /**
     * Check if a quote is in a specific collection
     */
    fun isQuoteInCollection(quoteId: String, collectionId: String): Boolean {
        return _collections.value[collectionId]?.quoteIds?.contains(quoteId) ?: false
    }
    
    /**
     * Get all collections that contain a specific quote
     */
    fun getCollectionsForQuote(quoteId: String): List<Collection> {
        return _collections.value.values.filter { collection ->
            collection.quoteIds.contains(quoteId)
        }
    }
    
    /**
     * Check if a quote is in any collection (for backward compatibility with favorites)
     */
    fun isQuoteInAnyCollection(quoteId: String): Boolean {
        return _collections.value.values.any { it.quoteIds.contains(quoteId) }
    }
    
    /**
     * Get all quote IDs from the default favorites collection
     */
    fun getFavoriteQuoteIds(): Set<String> {
        return _collections.value[Collection.DEFAULT_COLLECTION_ID]?.quoteIds ?: emptySet()
    }
    
    /**
     * Clear all quotes from a collection
     */
    suspend fun clearCollection(collectionId: String) {
        val userId = getCurrentUserId() ?: return
        
        try {
            if (collectionId == Collection.DEFAULT_COLLECTION_ID) {
                // Clear favourites table
                database.from(Collection.DEFAULT_COLLECTION_ID)
                    .delete {
                        filter {
                            eq("user_id", userId)
                        }
                    }
            } else {
                // Clear collection_quote table
                database.from(COLLECTION_QUOTES_TABLE)
                    .delete {
                        filter {
                            eq("collection_id", collectionId)
                        }
                    }
            }
            
            // Update local state
            val collection = _collections.value[collectionId] ?: return
            val updatedCollection = collection.copy(quoteIds = emptySet())
            _collections.value = _collections.value + (collectionId to updatedCollection)
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing collection $collectionId", e)
        }
    }
}
