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

class QuoteWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            GlanceTheme {
                QuoteOfTheDayWidget(
                    quote = "The only way to do great work is to love what you do.",
                    author = "Steve Jobs",
                    category = "MOTIVATION"
                )
            }
        }
    }
}

@Composable
fun QuoteOfTheDayWidget(
    quote: String,
    author: String,
    category: String = "INSPIRATION"
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
                .padding(20.dp),
            verticalAlignment = Alignment.Vertical.CenterVertically,
            horizontalAlignment = Alignment.Horizontal.CenterHorizontally
        ) {
            // Top section - Category badge
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Horizontal.Start
            ) {
                CategoryBadge(category = category)
                // Quote icon/decoration

                Spacer(modifier = GlanceModifier.defaultWeight())

                Image(
                    provider = ImageProvider(R.drawable.ic_quote_foreground),
                    contentDescription = "Quote",
                    modifier = GlanceModifier.size(72.dp),
                    contentScale = ContentScale.Fit,
                    colorFilter = ColorFilter.tint(GlanceWidgetTheme.textOnGradient)
                )
            }

            // Quote text - centered and prominent
            Text(
                text = "\"$quote\"",
                style = TextStyle(
                    color = GlanceWidgetTheme.textOnGradient,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                ),
                modifier = GlanceModifier.fillMaxWidth()
            )

            Spacer(modifier = GlanceModifier.height(12.dp))

            // Author attribution
            Text(
                text = "— $author",
                style = TextStyle(
                    color = GlanceWidgetTheme.textOnGradient,
                    fontSize = 14.sp,
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
