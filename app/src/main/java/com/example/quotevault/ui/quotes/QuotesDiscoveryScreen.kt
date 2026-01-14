package com.example.quotevault.ui.quotes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.quotevault.ui.components.QuoteCard

@Composable
fun QuotesDiscoveryScreen(
    modifier: Modifier = Modifier,
    viewModel: QuotesViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    
    Box(modifier = modifier.fillMaxSize()) {
        when {
            state.isLoading && state.quotes.isEmpty() -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            state.quotes.isEmpty() -> {
                Text(
                    text = "No quotes available",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.verticalGradient(
                                listOf(
                                    MaterialTheme.colorScheme.primaryContainer,
                                    MaterialTheme.colorScheme.secondaryContainer,
                                )
                            )
                        )
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    items(
                        items = state.quotes,
                        key = { quote -> quote.id }
                    ) { quote ->
                        QuoteCard(
                            quote = quote.text,
                            author = quote.author,
                            category = quote.category,
                            isFavorite = quote.isFavorite,
                            onFavoriteClick = {
                                viewModel.handleIntent(QuotesIntent.ToggleFavorite(quote.id))
                            },
                            onShareClick = {
                                viewModel.handleIntent(QuotesIntent.ShareQuote(quote))
                            },
                            onCardClick = {
                                // Handle card click if needed
                            },
                            modifier = Modifier
                        )
                    }
                }
            }
        }
        
        // Error SnackBar
        if (state.error != null) {
            Snackbar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                action = {
                    TextButton(onClick = { viewModel.handleIntent(QuotesIntent.ClearError) }) {
                        Text("Dismiss")
                    }
                }
            ) {
                Text(state.error!!)
            }
        }
    }
}
