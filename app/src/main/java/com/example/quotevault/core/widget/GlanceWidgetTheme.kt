package com.example.quotevault.core.widget

import androidx.compose.ui.graphics.Color
import androidx.glance.color.ColorProvider

object GlanceWidgetTheme {
    // Gradient colors
    val gradientStart = Color(0xFF6366F1)
    val gradientEnd = Color(0xFF8B5CF6)
    
    // Surface colors
    val surface = ColorProvider(Color(0xFFFFFFFF), Color(0xFF1E293B))
    
    // Text colors
    val textPrimary = ColorProvider(
        day = Color(0xFF0F172A),
        night = Color(0xFFF8FAFC)
    )
    val textSecondary = ColorProvider(
        day = Color(0xFF64748B),
        night = Color(0xFF94A3B8)
    )
    val textOnGradient = ColorProvider(Color.White, Color.White)
    
    // Accent colors
    val accent = ColorProvider(Color(0xFFF59E0B), Color(0xFFFEF3C7))
}
