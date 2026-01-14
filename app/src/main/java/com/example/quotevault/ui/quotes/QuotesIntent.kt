package com.example.quotevault.ui.quotes

sealed class QuotesIntent {
    object LoadQuotes : QuotesIntent()
    object RefreshQuotes : QuotesIntent()
    data class ToggleFavorite(val quoteId: String) : QuotesIntent()
    data class ShareQuote(val quote: Quote) : QuotesIntent()
    data class SearchQuotes(val query: String) : QuotesIntent()
    data class FilterByCategory(val category: String?) : QuotesIntent()
    object ClearError : QuotesIntent()
}
