package com.vcc.aprender.playenglish.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * High-contrast, friendly palette for early learning (PT-PT).
 * Semantic colours are supplied by MaterialTheme for accessibility.
 */
object PlayEnglishColors {
    val Navy = Color(0xFF182E52)
    val RoyalBlue = Color(0xFF244DA0)
    val Cream = Color(0xFFFFFBF4)
    val Sky = Color(0xFFE5F0FF)
    val Mint = Color(0xFFDDF6EB)
    val Peach = Color(0xFFFFE7D4)
    val Yellow = Color(0xFFFFF1BF)
    val Green = Color(0xFF1E604B)
    val Red = Color(0xFFB42318)
}

private val ChildColorScheme = lightColorScheme(
    primary = PlayEnglishColors.RoyalBlue,
    onPrimary = Color.White,
    primaryContainer = PlayEnglishColors.Sky,
    onPrimaryContainer = PlayEnglishColors.Navy,
    secondary = PlayEnglishColors.Green,
    onSecondary = Color.White,
    secondaryContainer = PlayEnglishColors.Mint,
    onSecondaryContainer = PlayEnglishColors.Navy,
    background = PlayEnglishColors.Cream,
    onBackground = PlayEnglishColors.Navy,
    surface = Color.White,
    onSurface = PlayEnglishColors.Navy,
    error = PlayEnglishColors.Red,
    onError = Color.White
)

private val ChildTypography = Typography(
    headlineMedium = TextStyle(
        fontSize = 30.sp, lineHeight = 38.sp, fontWeight = FontWeight.ExtraBold
    ),
    headlineSmall = TextStyle(
        fontSize = 25.sp, lineHeight = 32.sp, fontWeight = FontWeight.Bold
    ),
    titleLarge = TextStyle(
        fontSize = 22.sp, lineHeight = 29.sp, fontWeight = FontWeight.Bold
    ),
    titleMedium = TextStyle(
        fontSize = 19.sp, lineHeight = 26.sp, fontWeight = FontWeight.SemiBold
    ),
    bodyLarge = TextStyle(
        fontSize = 18.sp, lineHeight = 27.sp
    ),
    bodyMedium = TextStyle(
        fontSize = 16.sp, lineHeight = 24.sp
    ),
    labelLarge = TextStyle(
        fontSize = 16.sp, lineHeight = 23.sp, fontWeight = FontWeight.SemiBold
    )
)

private val ChildShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp)
)

@Composable
fun PlayEnglishTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = ChildColorScheme,
        typography = ChildTypography,
        shapes = ChildShapes,
        content = content
    )
}
