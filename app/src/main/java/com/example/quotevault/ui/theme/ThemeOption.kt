package com.example.quotevault.ui.theme

import androidx.compose.ui.graphics.Color

enum class ThemeOption(
    val displayName: String,
    val description: String,
    val primaryLight: Color,
    val primaryDark: Color,
    val secondaryLight: Color,
    val secondaryDark: Color
) {
    INDIGO(
        displayName = "Indigo",
        description = "Classic purple-blue theme",
        primaryLight = Color(0xFF6366F1),
        primaryDark = Color(0xFF818CF8),
        secondaryLight = Color(0xFFF59E0B),
        secondaryDark = Color(0xFFFBBF24)
    ),
    EMERALD(
        displayName = "Emerald",
        description = "Fresh green theme",
        primaryLight = Color(0xFF10B981),
        primaryDark = Color(0xFF34D399),
        secondaryLight = Color(0xFF8B5CF6),
        secondaryDark = Color(0xFFA78BFA)
    ),
    ROSE(
        displayName = "Rose",
        description = "Elegant pink theme",
        primaryLight = Color(0xFFE11D48),
        primaryDark = Color(0xFFFB7185),
        secondaryLight = Color(0xFF0EA5E9),
        secondaryDark = Color(0xFF38BDF8)
    )
}
