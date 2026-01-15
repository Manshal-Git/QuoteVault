package com.example.quotevault.ui.favourites

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
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
import com.example.quotevault.ui.components.ShareQuoteDialog
import com.example.quotevault.ui.quotes.Quote

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavouritesScreen(
    modifier: Modifier = Modifier,
    viewModel: FavouritesViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showClearDialog by rememberSaveable { mutableStateOf(false) }
    var quoteToShare by rememberSaveable { mutableStateOf<Quote?>(null) }
    var shareMessage by rememberSaveable { mutableStateOf<String?>(null) }

    Box(modifier) {
        Column {
            Row(
                Modifier
                    .height(60.dp)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                ,
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Favourites",
                    style = MaterialTheme.typography.headlineLarge
                )
                if (state.favoriteQuotes.isNotEmpty()) {
                    IconButton(onClick = { showClearDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Clear all favorites"
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                when {
                    state.isLoading && state.favoriteQuotes.isEmpty() -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    state.favoriteQuotes.isEmpty() && !state.isLoading -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "No Favorites Yet",
                                style = MaterialTheme.typography.headlineLarge,
                                color = MaterialTheme.colorScheme.onBackground
                            )

                            Spacer(Modifier.height(16.dp))

                            Text(
                                text = "Start adding quotes to your favorites by tapping the heart icon",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }

                    else -> {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize(),
                            contentPadding = PaddingValues(vertical = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            stickyHeader {
                                Text(
                                    text = "${state.favoriteQuotes.size} ${if (state.favoriteQuotes.size == 1) "Quote" else "Quotes"}",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            MaterialTheme.colorScheme.surfaceVariant
                                        )
                                        .padding(8.dp)
                                )
                            }

                            items(
                                items = state.favoriteQuotes,
                                key = { quote -> quote.id }
                            ) { quote ->
                                QuoteCard(
                                    quote = quote.text,
                                    author = quote.author,
                                    category = quote.category,
                                    isFavorite = true,
                                    onFavoriteClick = {
                                        viewModel.handleIntent(FavouritesIntent.RemoveFavorite(quote.id))
                                    },
                                    onShareClick = {
                                        quoteToShare = quote
                                    },
                                    onCardClick = {
                                        // Handle card click if needed
                                    },
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                            }
                        }
                    }
                }
                // Error SnackBar
                state.error?.let { error ->
                    Snackbar(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(16.dp),
                        action = {
                            TextButton(onClick = { viewModel.handleIntent(FavouritesIntent.ClearError) }) {
                                Text("Dismiss")
                            }
                        }
                    ) {
                        Text(error)
                    }
                }
            }
        }
        // Clear All Confirmation Dialog
        if (showClearDialog) {
            AlertDialog(
                onDismissRequest = { showClearDialog = false },
                title = {
                    Text(
                        text = "Clear All Favorites?",
                        style = MaterialTheme.typography.headlineLarge
                    )
                },
                text = {
                    Text(
                        text = "This will remove all ${state.favoriteQuotes.size} quotes from your favorites. This action cannot be undone.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.handleIntent(FavouritesIntent.ClearAllFavorites)
                            showClearDialog = false
                        }
                    ) {
                        Text(
                            text = "Clear All",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showClearDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
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
}
