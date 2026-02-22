package com.example.quotevault.ui.favourites

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.quotevault.R
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
    val listState = rememberLazyListState()

    LaunchedEffect(state.visibleFavoriteQuotes.size, state.favoriteQuotes.size) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1 }
            .collect { lastVisibleIndex ->
                val shouldLoadMore =
                    lastVisibleIndex >= state.visibleFavoriteQuotes.lastIndex - 2 &&
                        state.hasMorePages &&
                        !state.isLoadingMore
                if (shouldLoadMore) {
                    viewModel.handleIntent(FavouritesIntent.LoadNextPage)
                }
            }
    }

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
                    text = stringResource(R.string.favourites),
                    style = MaterialTheme.typography.headlineLarge
                )
                if (state.favoriteQuotes.isNotEmpty()) {
                    IconButton(onClick = { showClearDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = stringResource(R.string.clear_all_favorites_description)
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
                                text = stringResource(R.string.no_favorites_yet),
                                style = MaterialTheme.typography.headlineLarge,
                                color = MaterialTheme.colorScheme.onBackground
                            )

                            Spacer(Modifier.height(16.dp))

                            Text(
                                text = stringResource(R.string.no_favorites_description),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }

                    else -> {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .fillMaxSize(),
                            contentPadding = PaddingValues(vertical = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            stickyHeader {
                                val count = state.favoriteQuotes.size
                                val quotesText = if (count == 1) 
                                    stringResource(R.string.quote_count_singular)
                                else 
                                    stringResource(R.string.quote_count_plural)
                                    
                                Text(
                                    text = "$count $quotesText",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            MaterialTheme.colorScheme.surfaceVariant
                                        )
                                        .padding(vertical = 8.dp)
                                        .padding(start = 16.dp)
                                )
                            }

                            items(
                                items = state.visibleFavoriteQuotes,
                                key = { quote -> quote.id }
                            ) { quote ->
                                QuoteCard(
                                    quote = quote.text,
                                    author = quote.author,
                                    category = quote.category,
                                    isFavorite = true,
                                    fontSizeScale = state.fontSizeScale,
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

                            if (state.hasMorePages) {
                                item(key = "loading_more") {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator()
                                    }
                                }
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
                                Text(stringResource(R.string.dismiss))
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
                        text = stringResource(R.string.clear_all_favorites_title),
                        style = MaterialTheme.typography.headlineLarge
                    )
                },
                text = {
                    Text(
                        text = stringResource(R.string.clear_all_favorites_message, state.favoriteQuotes.size),
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
                            text = stringResource(R.string.clear_all),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showClearDialog = false }) {
                        Text(stringResource(R.string.cancel))
                    }
                }
            )
        }

        // Share Dialog
        quoteToShare?.let { quote ->
            ShareQuoteDialog(
                quote = quote,
                fontSizeScale = state.fontSizeScale,
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
                        Text(stringResource(R.string.dismiss))
                    }
                }
            ) {
                Text(msg)
            }
        }
    }
}
