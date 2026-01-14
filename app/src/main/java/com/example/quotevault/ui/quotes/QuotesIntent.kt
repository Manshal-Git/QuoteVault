package com.example.quotevault.ui.quotes

sealed class QuotesIntent {
    object LoadQuotes : QuotesIntent()
    object RefreshQuotes : QuotesIntent()
    data class ToggleFavorite(val quoteId: String) : QuotesIntent()
    data class ShareQuote(val quote: Quote) : QuotesIntent()
    object ClearError : QuotesIntent()
}
