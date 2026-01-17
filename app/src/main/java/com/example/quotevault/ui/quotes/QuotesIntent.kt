package com.example.quotevault.ui.quotes

sealed class QuotesIntent {
    object LoadQuotes : QuotesIntent()
    object LoadQuoteOfTheDay : QuotesIntent()
    object RefreshQuotes : QuotesIntent()
    data class ToggleFavorite(val quoteId: String) : QuotesIntent()
    data class OpenCollectionSheet(val quoteId: String) : QuotesIntent()
    data class ToggleQuoteInCollection(val quoteId: String, val collectionId: String) : QuotesIntent()
    data class CreateCollection(val name: String, val description: String) : QuotesIntent()
    data class ShareQuote(val quote: Quote) : QuotesIntent()
    data class SearchQuotes(val query: String) : QuotesIntent()
    data class FilterByCategory(val category: String?) : QuotesIntent()
    object CloseCollectionSheet : QuotesIntent()
    object ClearError : QuotesIntent()
    object RetryConnection : QuotesIntent()
    object LoadOfflineData : QuotesIntent()
}
