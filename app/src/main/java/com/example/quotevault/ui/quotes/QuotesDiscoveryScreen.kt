package com.example.quotevault.ui.quotes

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.quotevault.ui.components.QuoteCard
import com.example.quotevault.ui.components.QuotesSearchBar
import com.example.quotevault.ui.components.ShareQuoteDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuotesDiscoveryScreen(
    modifier: Modifier = Modifier,
    viewModel: QuotesViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var quoteToShare by rememberSaveable { mutableStateOf<Quote?>(null) }
    var shareMessage by rememberSaveable { mutableStateOf<String?>(null) }
    
    Box(modifier = modifier.fillMaxSize()) {
        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = {
                viewModel.handleIntent(QuotesIntent.RefreshQuotes)
            },
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                // Search Bar
                QuotesSearchBar(
                    query = state.searchQuery,
                    onQueryChange = { query ->
                        viewModel.handleIntent(QuotesIntent.SearchQuotes(query))
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(top = 16.dp)
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Category Filter Chips
                if (state.availableCategories.isNotEmpty()) {
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // "All" chip
                        item {
                            FilterChip(
                                selected = state.selectedCategory == null,
                                onClick = {
                                    viewModel.handleIntent(QuotesIntent.FilterByCategory(null))
                                },
                                label = {
                                    Text(
                                        text = "All",
                                        style = MaterialTheme.typography.labelLarge
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                )
                            )
                        }
                        
                        // Category chips
                        items(state.availableCategories) { category ->
                            FilterChip(
                                selected = state.selectedCategory == category,
                                onClick = {
                                    viewModel.handleIntent(QuotesIntent.FilterByCategory(category))
                                },
                                label = {
                                    Text(
                                        text = category,
                                        style = MaterialTheme.typography.labelLarge
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                )
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                // Quotes List
                when {
                    state.isLoading && state.quotes.isEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    
                    state.filteredQuotes.isEmpty() && !state.isLoading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = if (state.searchQuery.isNotEmpty() || state.selectedCategory != null) {
                                        "No quotes found"
                                    } else {
                                        "No quotes available"
                                    },
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                
                                if (state.searchQuery.isNotEmpty() || state.selectedCategory != null) {
                                    Text(
                                        text = "Try adjusting your search or filters",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                    
                    else -> {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            contentPadding = PaddingValues(vertical = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            items(
                                items = state.filteredQuotes,
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
                                        quoteToShare = quote
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
        
        // Share Success Message
        shareMessage?.let { msg ->
            Snackbar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                action = {
                    TextButton(onClick = { shareMessage = null }) {
                        Text("Dismiss")
                    }
                }
            ) {
                Text(msg)
            }
        }
    }
    
    // Share Dialog
    quoteToShare?.let { quote ->
        ShareQuoteDialog(
            quote = quote,
            onDismiss = { quoteToShare = null },
            onShareComplete = { message ->
                shareMessage = message
            }
        )
    }
}
