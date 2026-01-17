package com.example.quotevault.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.example.quotevault.R

enum class ThemeOption(
    val primaryLight: Color,
    val primaryDark: Color,
    val secondaryLight: Color,
    val secondaryDark: Color
) {
    INDIGO(
        primaryLight = Color(0xFF6366F1),
        primaryDark = Color(0xFF818CF8),
        secondaryLight = Color(0xFFF59E0B),
        secondaryDark = Color(0xFFFBBF24)
    ),
    EMERALD(
        primaryLight = Color(0xFF10B981),
        primaryDark = Color(0xFF34D399),
        secondaryLight = Color(0xFF8B5CF6),
        secondaryDark = Color(0xFFA78BFA)
    ),
    ROSE(
        primaryLight = Color(0xFFE11D48),
        primaryDark = Color(0xFFFB7185),
        secondaryLight = Color(0xFF0EA5E9),
        secondaryDark = Color(0xFF38BDF8)
    );
    
    @Composable
    fun getDisplayName(): String {
        return when (this) {
            INDIGO -> stringResource(R.string.theme_indigo)
            EMERALD -> stringResource(R.string.theme_emerald)
            ROSE -> stringResource(R.string.theme_rose)
        }
    }
    
    @Composable
    fun getDescription(): String {
        return when (this) {
            INDIGO -> stringResource(R.string.theme_indigo_description)
            EMERALD -> stringResource(R.string.theme_emerald_description)
            ROSE -> stringResource(R.string.theme_rose_description)
        }
    }
}
