package com.example.quotevault.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.example.quotevault.ui.theme.Dimensions
import com.example.quotevault.ui.theme.Error
import com.example.quotevault.ui.theme.PlayfairDisplay
import com.example.quotevault.ui.theme.QuoteVaultTheme

/**
 * CategoryChip displays a category label with consistent styling.
 * 
 * Features:
 * - Uppercase text transformation
 * - Primary container background color
 * - Label typography with letter spacing
 * - Rounded corners with compact padding
 * 
 * @param category The category name to display
 * @param modifier Optional modifier for customization
 */
@Composable
fun CategoryChip(
    category: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = category.uppercase(),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onPrimaryContainer,
        modifier = modifier
            .clip(RoundedCornerShape(Dimensions.radiusSM))
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(
                horizontal = Dimensions.spaceSM,
                vertical = Dimensions.spaceXS
            )
    )
}

/**
 * QuoteCard displays a quote with author, category, and action buttons.
 * 
 * Features:
 * - Category chip at the top
 * - Quote text in headline typography
 * - Author name with attribution dash
 * - Favorite button that toggles between filled and outlined states
 * - Share button
 * - Clickable card surface
 * - Rounded corners and elevation
 * 
 * @param quote The quote text to display
 * @param author The author name
 * @param category The category label
 * @param isFavorite Whether the quote is marked as favorite
 * @param onFavoriteClick Callback when favorite button is clicked
 * @param onShareClick Callback when share button is clicked
 * @param onCardClick Callback when the card itself is clicked
 * @param modifier Optional modifier for customization
 */
@Composable
fun QuoteCard(
    quote: String,
    author: String,
    category: String,
    isFavorite: Boolean = false,
    onFavoriteClick: () -> Unit = {},
    onShareClick: () -> Unit = {},
    onCardClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .dropShadow(
                shape = RoundedCornerShape(Dimensions.radiusMD),
                shadow = Shadow(
                    radius = 3.dp,
                    spread = 1.dp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                    offset = DpOffset(0.dp, 1.dp)
                )
            )
            .background(
                color = MaterialTheme.colorScheme.surface,
                RoundedCornerShape(Dimensions.radiusMD)
            )
            .padding(Dimensions.spaceMD)
            .clickable(onClick = onCardClick),
        verticalArrangement = Arrangement.spacedBy(Dimensions.spaceSM)
    ) {
        // Category chip at top
        CategoryChip(category = category)

        Spacer(Modifier.height(Dimensions.spaceSM))

        // Quote text with headline typography
        Text(
            text = quote,
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onSurface
        )

        // Author with attribution dash
        Text(
            text = "— $author",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Action buttons row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Favorite button with toggle states
            IconButton(onClick = onFavoriteClick) {
                Icon(
                    imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                    tint = if (isFavorite) Error else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Share button
            IconButton(onClick = onShareClick) {
                Icon(
                    imageVector = Icons.Filled.Share,
                    contentDescription = "Share quote",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@PreviewLightDark()
@Composable
fun PreviewQuoteCard() {
    // Replace 'YourAppTheme' with your actual MaterialTheme wrapper
    QuoteVaultTheme {
        QuoteCard(
            quote = "Believe you can and you're halfway there.",
            author = "Theodore Roosevelt",
            category = "Motivation",
            isFavorite = true,
            onFavoriteClick = { /* Handle click */ },
            onShareClick = { /* Handle share */ },
            onCardClick = { /* Handle navigation */ }
        )
    }
}

/**
 * FeaturedQuoteCard displays a highlighted quote with gradient background.
 * 
 * Features:
 * - Gradient background from indigo to purple
 * - White text for contrast
 * - Center-aligned quote and author
 * - Display typography for quote text
 * - Extra padding for visual emphasis
 * 
 * @param quote The quote text to display
 * @param author The author name
 * @param modifier Optional modifier for customization
 */
@Composable
fun FeaturedQuoteCard(
    quote: String,
    author: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Dimensions.radiusXL))
            .background(
                brush = androidx.compose.ui.graphics.Brush.linearGradient(
                    colors = listOf(
                        com.example.quotevault.ui.theme.GradientStart,
                        com.example.quotevault.ui.theme.GradientEnd
                    )
                )
            )
            .padding(Dimensions.spaceXL),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Dimensions.spaceMD)
    ) {
        // Quote text with display typography
        Text(
            text = quote,
            style = MaterialTheme.typography.displayLarge,
            color = androidx.compose.ui.graphics.Color.White,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        
        // Author name
        Text(
            text = "— $author",
            style = MaterialTheme.typography.bodyLarge,
            color = androidx.compose.ui.graphics.Color.White,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@PreviewLightDark
@Composable
fun PreviewFeaturedQuoteCard() {
    QuoteVaultTheme {
        FeaturedQuoteCard(
            quote = "The only way to do great work is to love what you do.",
            author = "Steve Jobs"
        )
    }
}

/**
 * QuotesSearchBar provides text input for searching quotes.
 * 
 * Features:
 * - Single-line text input
 * - Placeholder text "Search quotes..."
 * - Borderless design with surface variant background
 * - Triggers callback on text change
 * 
 * @param query The current search query text
 * @param onQueryChange Callback invoked when the query text changes
 * @param modifier Optional modifier for customization
 */
@Composable
fun QuotesSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Dimensions.radiusMD)),
        placeholder = {
            Text(
                text = "Search quotes...",
                style = MaterialTheme.typography.bodyLarge
            )
        },
        singleLine = true,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent
        ),
        leadingIcon = {
            Icon(
                imageVector = Icons.Rounded.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(
                    onClick = {
                        onQueryChange("")
                    }
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Clear,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    )
}

/**
 * PrimaryButton provides a styled action button.
 * 
 * Features:
 * - Optional leading icon
 * - Fixed 48dp height for accessibility
 * - Primary color background
 * - Medium border radius
 * 
 * @param text The button label text
 * @param onClick Callback invoked when the button is clicked
 * @param modifier Optional modifier for customization
 * @param icon Optional icon to display before the text
 */
@Composable
fun PrimaryButton(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .padding(horizontal = Dimensions.spaceMD)
            .height(48.dp),
        shape = RoundedCornerShape(Dimensions.radiusMD),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary
        ),
        enabled = enabled,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(Dimensions.spaceSM),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Optional leading icon
            icon?.let {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
            
            // Button text
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}
