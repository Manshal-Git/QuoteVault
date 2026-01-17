package com.example.quotevault.core.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.*
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.color.ColorProvider
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import com.example.quotevault.MainActivity
import com.example.quotevault.R
import com.example.quotevault.core.auth.SupabaseClient
import com.example.quotevault.ui.quotes.Quote
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class DailyQuoteWidget : GlanceAppWidget() {
    
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        // Get the daily quote data and user preferences
        val quote = getQuoteOfTheDay(context)
        val fontSizeScale = getFontSizeScale(context)
        
        provideContent {
            GlanceTheme {
                QuoteOfTheDayWidget(
                    quote = quote.text,
                    author = quote.author,
                    category = quote.category.uppercase(),
                    date = LocalDate.now().format(
                        DateTimeFormatter.ofPattern("EEEE, MMM d", Locale.getDefault())
                    ),
                    fontSizeScale = fontSizeScale
                )
            }
        }
    }
    
    private suspend fun getQuoteOfTheDay(context: Context): Quote {
        return try {
            // Access Hilt dependencies through EntryPoint
            val entryPoint = EntryPointAccessors.fromApplication(
                context.applicationContext,
                WidgetEntryPoint::class.java
            )
            val dataSource = WidgetDataSource(entryPoint.supabaseClient())
            dataSource.getQuoteOfTheDay()
        } catch (e: Exception) {
            // Fallback quote if everything fails
            Quote(
                id = "fallback",
                text = context.getString(R.string.widget_fallback_quote),
                author = context.getString(R.string.widget_fallback_author),
                category = "Motivation"
            )
        }
    }
    
    private suspend fun getFontSizeScale(context: Context): Float {
        return try {
            val entryPoint = EntryPointAccessors.fromApplication(
                context.applicationContext,
                WidgetEntryPoint::class.java
            )
            val userPreferences = entryPoint.userPreferencesDataStore()
            // Get the first value from the flow
            userPreferences.userPreferences.first().fontSize
        } catch (e: Exception) {
            1.0f // Default font size scale
        }
    }
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface WidgetEntryPoint {
    fun supabaseClient(): SupabaseClient
    fun userPreferencesDataStore(): com.example.quotevault.data.UserPreferencesDataStore
}

@Composable
fun QuoteOfTheDayWidget(
    quote: String,
    author: String,
    category: String = "INSPIRATION",
    date: String,
    fontSizeScale: Float = 1.0f
) {
    // Main container with gradient background
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ImageProvider(R.drawable.widget_gradient_bg))
            .cornerRadius(24.dp)
            .padding(0.dp)
            .clickable(actionStartActivity<MainActivity>()),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .padding(bottom = 20.dp, top = 10.dp)
            ,
            verticalAlignment = Alignment.Vertical.CenterVertically,
            horizontalAlignment = Alignment.Horizontal.CenterHorizontally
        ) {
            // Top section - Category badge and date
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Horizontal.Start,
                verticalAlignment = Alignment.Vertical.Top
            ) {
                Column(
                    modifier = GlanceModifier.defaultWeight(),
                    verticalAlignment = Alignment.Vertical.Top
                ) {
                    CategoryBadge(category = category)
                    
                    Spacer(modifier = GlanceModifier.height(4.dp))
                    
                    // Date display
                    Text(
                        text = date,
                        modifier = GlanceModifier.padding(start = 2.dp),
                        style = TextStyle(
                            color = GlanceWidgetTheme.textOnGradient,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Normal
                        )
                    )
                }

                // Quote icon/decoration
                Image(
                    provider = ImageProvider(R.drawable.ic_quote_foreground),
                    contentDescription = "Quote",
                    modifier = GlanceModifier.size(72.dp),
                    contentScale = ContentScale.Fit,
                    colorFilter = ColorFilter.tint(GlanceWidgetTheme.textOnGradient)
                )
            }

            // Quote text - centered and prominent with font size scaling
            Text(
                text = "\"$quote\"",
                style = TextStyle(
                    color = GlanceWidgetTheme.textOnGradient,
                    fontSize = (18.sp.value * fontSizeScale).sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                ),
                modifier = GlanceModifier.fillMaxWidth()
            )

            Spacer(modifier = GlanceModifier.height(12.dp))

            // Author attribution with font size scaling
            Text(
                text = "— $author",
                style = TextStyle(
                    color = GlanceWidgetTheme.textOnGradient,
                    fontSize = (14.sp.value * fontSizeScale).sp,
                    fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.Center
                ),
                modifier = GlanceModifier.fillMaxWidth()
            )

            Spacer(modifier = GlanceModifier.height(20.dp))
        }
    }
}


@Composable
fun CategoryBadge(
    category: String,
    useGradient: Boolean = true
) {
    Box(
        modifier = GlanceModifier
            .background(
                ColorProvider(Color(0x40FFFFFF), Color(0x40FFFFFF))
            )
            .cornerRadius(8.dp)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = category,
            style = TextStyle(
                color = GlanceWidgetTheme.textOnGradient,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        )
    }
}
