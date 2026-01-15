package com.example.quotevault.data

data class Collection(
    val id: String,
    val name: String,
    val description: String = "",
    val quoteIds: Set<String> = emptySet(),
    val createdAt: Long = System.currentTimeMillis(),
    val isDefault: Boolean = false
) {
    companion object {
        const val DEFAULT_COLLECTION_ID = "favorites"
        
        fun createDefault() = Collection(
            id = DEFAULT_COLLECTION_ID,
            name = "Favorites",
            description = "Your favorite quotes",
            isDefault = true
        )
    }
}
