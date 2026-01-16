package com.example.quotevault.ui.quotes

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.quotevault.R
import com.example.quotevault.ui.components.*
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuotesDiscoveryScreen(
    modifier: Modifier = Modifier,
    viewModel: QuotesViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var quoteToShare by rememberSaveable { mutableStateOf<Quote?>(null) }
    var shareMessage by rememberSaveable { mutableStateOf<String?>(null) }
    var showQuoteOfTheDay by rememberSaveable { mutableStateOf(true) }
    
    // Track scroll state for FAB expansion
    val listState = rememberLazyListState()
    var isFabExpanded by remember { mutableStateOf(true) }
    
    // Detect scrolling and collapse/expand FAB
    LaunchedEffect(listState.isScrollInProgress) {
        if (listState.isScrollInProgress) {
            isFabExpanded = false
        } else {
            // Wait a bit after scroll stops before expanding
            delay(150)
            if (!listState.isScrollInProgress) {
                isFabExpanded = true
            }
        }
    }
    
    Box(modifier = modifier.fillMaxSize()) {
        // Main Content - Only show when Quote of the Day is dismissed
        AnimatedVisibility(
            visible = !showQuoteOfTheDay || state.quoteOfTheDay == null,
            enter = fadeIn() + slideInVertically(),
            exit = fadeOut() + slideOutVertically()
        ) {
            PullToRefreshBox(
                isRefreshing = state.isRefreshing,
                onRefresh = {
                    viewModel.handleIntent(QuotesIntent.RefreshQuotes)
                },
                modifier = Modifier.fillMaxSize()
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
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
                                            text = stringResource(R.string.all_categories),
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
                                            stringResource(R.string.no_quotes_found)
                                        } else {
                                            stringResource(R.string.no_quotes_available)
                                        },
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                    
                                    if (state.searchQuery.isNotEmpty() || state.selectedCategory != null) {
                                        Text(
                                            text = stringResource(R.string.adjust_search_filters),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                        
                        else -> {
                            LazyColumn(
                                state = listState,
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
                                            viewModel.handleIntent(QuotesIntent.OpenCollectionSheet(quote.id))
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
        }
        
        // Quote of the Day Overlay
        AnimatedVisibility(
            visible = showQuoteOfTheDay && state.quoteOfTheDay != null,
            enter = fadeIn() + slideInVertically(),
            exit = fadeOut() + slideOutVertically(),
            modifier = Modifier.fillMaxSize()
        ) {
            state.quoteOfTheDay?.let { qotd ->
                QuoteOfTheDayOverlay(
                    quote = qotd,
                    onDismiss = { showQuoteOfTheDay = false },
                    onAddToCollection = {
                        viewModel.handleIntent(QuotesIntent.OpenCollectionSheet(qotd.id))
                    },
                    onShare = {
                        quoteToShare = qotd
                    }
                )
            }
        }
        
        // FAB to show Quote of the Day again - Collapses when scrolling
        if (!showQuoteOfTheDay && state.quoteOfTheDay != null) {
            FloatingActionButton(
                onClick = { showQuoteOfTheDay = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                AnimatedContent(
                    targetState = isFabExpanded,
                    transitionSpec = {
                        fadeIn() + expandHorizontally() togetherWith 
                        fadeOut() + shrinkHorizontally()
                    },
                    label = "FAB expansion"
                ) { expanded ->
                    if (expanded) {
                        // Expanded state with icon and text
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Quote of the Day"
                            )
                            Text(
                                text = stringResource(R.string.quote_of_the_day),
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    } else {
                        // Collapsed state with icon only
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Quote of the Day",
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }
        }
        
        // Error SnackBar
        if (state.error != null && !showQuoteOfTheDay) {
            Snackbar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                action = {
                    TextButton(onClick = { viewModel.handleIntent(QuotesIntent.ClearError) }) {
                        Text(stringResource(R.string.dismiss))
                    }
                }
            ) {
                Text(state.error!!)
            }
        }
        
        // Share Success Message
        if (!showQuoteOfTheDay) {
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
    
    // Collection Bottom Sheet
    if (state.showCollectionSheet && state.selectedQuoteForCollection != null) {
        val selectedQuoteId = state.selectedQuoteForCollection!!
        val selectedCollectionIds = state.collections
            .filter { it.quoteIds.contains(selectedQuoteId) }
            .map { it.id }
            .toSet()
        
        AddToCollectionBottomSheet(
            quoteId = selectedQuoteId,
            collections = state.collections,
            selectedCollectionIds = selectedCollectionIds,
            onCollectionToggle = { collectionId ->
                viewModel.handleIntent(
                    QuotesIntent.ToggleQuoteInCollection(selectedQuoteId, collectionId)
                )
            },
            onCreateCollection = { name, description ->
                viewModel.handleIntent(QuotesIntent.CreateCollection(name, description))
            },
            onDismiss = {
                viewModel.handleIntent(QuotesIntent.CloseCollectionSheet)
            }
        )
    }
}

@Composable
private fun QuoteOfTheDayOverlay(
    quote: Quote,
    onDismiss: () -> Unit,
    onAddToCollection: () -> Unit,
    onShare: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            shape = MaterialTheme.shapes.large
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header with close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.quote_of_the_day_emoji),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Dismiss",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                // Featured Quote Card
                FeaturedQuoteCard(
                    quote = quote.text,
                    author = quote.author,
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            onAddToCollection()
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(R.string.save))
                    }
                    
                    Button(
                        onClick = {
                            onShare()
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(R.string.share))
                    }
                }
                
                // Dismiss Button
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.continue_to_quotes))
                }
            }
        }
    }
}
