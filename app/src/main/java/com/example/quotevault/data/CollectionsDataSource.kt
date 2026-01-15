package com.example.quotevault.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Single source of truth for collections and quote-collection relationships.
 * Replaces FavouritesDataSource with a more flexible collection-based system.
 */
@Singleton
class CollectionsDataSource @Inject constructor() {
    
    private val _collections = MutableStateFlow<Map<String, Collection>>(
        mapOf(Collection.DEFAULT_COLLECTION_ID to Collection.createDefault())
    )
    val collections: StateFlow<Map<String, Collection>> = _collections.asStateFlow()
    
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
    fun createCollection(name: String, description: String = ""): Collection {
        val newCollection = Collection(
            id = UUID.randomUUID().toString(),
            name = name,
            description = description
        )
        _collections.value = _collections.value + (newCollection.id to newCollection)
        return newCollection
    }
    
    /**
     * Delete a collection (cannot delete default collection)
     */
    fun deleteCollection(collectionId: String): Boolean {
        if (collectionId == Collection.DEFAULT_COLLECTION_ID) {
            return false
        }
        _collections.value = _collections.value - collectionId
        return true
    }
    
    /**
     * Add a quote to a collection
     */
    fun addQuoteToCollection(quoteId: String, collectionId: String) {
        val collection = _collections.value[collectionId] ?: return
        val updatedCollection = collection.copy(
            quoteIds = collection.quoteIds + quoteId
        )
        _collections.value = _collections.value + (collectionId to updatedCollection)
    }
    
    /**
     * Remove a quote from a collection
     */
    fun removeQuoteFromCollection(quoteId: String, collectionId: String) {
        val collection = _collections.value[collectionId] ?: return
        val updatedCollection = collection.copy(
            quoteIds = collection.quoteIds - quoteId
        )
        _collections.value = _collections.value + (collectionId to updatedCollection)
    }
    
    /**
     * Toggle quote in a collection
     * @return true if added, false if removed
     */
    fun toggleQuoteInCollection(quoteId: String, collectionId: String): Boolean {
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
    fun clearCollection(collectionId: String) {
        val collection = _collections.value[collectionId] ?: return
        val updatedCollection = collection.copy(quoteIds = emptySet())
        _collections.value = _collections.value + (collectionId to updatedCollection)
    }
}
