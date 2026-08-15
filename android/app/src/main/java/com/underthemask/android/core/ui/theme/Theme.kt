package com.underthemask.android.core.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.LocalContentColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object AppColors {
    val Background = Color(0xFF0B0C0E)
    val BackgroundRaised = Color(0xFF101216)
    val Surface = Color(0xFF181A1E)
    val SurfaceSoft = Color(0xFF202329)
    val SurfaceHover = Color(0xFF292C32)
    val Ink = Color(0xFFF4F1E9)
    val InkSoft = Color(0xFFD8D3C8)
    val Muted = Color(0xFFAAABA9)
    val Disabled = Color(0xFF929390)
    val Line = Color(0xFF34363B)
    val LineStrong = Color(0xFF51535A)
    val Gold = Color(0xFFF0B84D)
    val GoldBright = Color(0xFFFFD477)
    val GoldInk = Color(0xFF241A08)
    val Crew = Color(0xFF61B8AE)
    val CrewSoft = Color(0xFF16332F)
    val Impostor = Color(0xFFD96B62)
    val ImpostorSoft = Color(0xFF3B1E1D)
    val Success = Color(0xFF67C28A)
    val Error = Color(0xFFFF9B92)
}

@Immutable
data class AppSpacing(
    val xSmall: androidx.compose.ui.unit.Dp = 4.dp,
    val small: androidx.compose.ui.unit.Dp = 8.dp,
    val medium: androidx.compose.ui.unit.Dp = 12.dp,
    val large: androidx.compose.ui.unit.Dp = 16.dp,
    val xLarge: androidx.compose.ui.unit.Dp = 24.dp,
    val xxLarge: androidx.compose.ui.unit.Dp = 32.dp,
    val hero: androidx.compose.ui.unit.Dp = 48.dp,
)

private val LocalAppSpacing = staticCompositionLocalOf { AppSpacing() }

object AppTheme {
    val spacing: AppSpacing
        @Composable get() = LocalAppSpacing.current
}

private val DarkColors = darkColorScheme(
    primary = AppColors.Gold,
    onPrimary = AppColors.GoldInk,
    primaryContainer = Color(0xFF3A2D15),
    onPrimaryContainer = AppColors.GoldBright,
    secondary = AppColors.Crew,
    onSecondary = Color(0xFF071F1C),
    secondaryContainer = AppColors.CrewSoft,
    onSecondaryContainer = Color(0xFFB7E6DF),
    tertiary = AppColors.GoldBright,
    onTertiary = AppColors.GoldInk,
    background = AppColors.Background,
    onBackground = AppColors.Ink,
    surface = AppColors.Surface,
    onSurface = AppColors.Ink,
    surfaceVariant = AppColors.SurfaceSoft,
    onSurfaceVariant = AppColors.InkSoft,
    surfaceContainerLowest = AppColors.Background,
    surfaceContainerLow = AppColors.BackgroundRaised,
    surfaceContainer = AppColors.Surface,
    surfaceContainerHigh = AppColors.SurfaceSoft,
    surfaceContainerHighest = AppColors.SurfaceHover,
    outline = AppColors.LineStrong,
    outlineVariant = AppColors.Line,
    error = AppColors.Error,
    onError = Color(0xFF2B0807),
    errorContainer = AppColors.ImpostorSoft,
    onErrorContainer = Color(0xFFFFDAD6),
    scrim = Color.Black,
)

private val AppTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Black,
        fontSize = 44.sp,
        lineHeight = 48.sp,
    ),
    displayMedium = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Bold,
        fontSize = 34.sp,
        lineHeight = 39.sp,
    ),
    headlineLarge = TextStyle(fontWeight = FontWeight.Black, fontSize = 30.sp, lineHeight = 35.sp),
    headlineMedium = TextStyle(fontWeight = FontWeight.ExtraBold, fontSize = 25.sp, lineHeight = 31.sp),
    headlineSmall = TextStyle(fontWeight = FontWeight.Bold, fontSize = 21.sp, lineHeight = 27.sp),
    titleLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = 20.sp, lineHeight = 26.sp),
    titleMedium = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 17.sp, lineHeight = 23.sp),
    titleSmall = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 15.sp, lineHeight = 20.sp),
    bodyLarge = TextStyle(fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 21.sp),
    bodySmall = TextStyle(fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 18.sp),
    labelLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = 14.sp, lineHeight = 20.sp),
    labelMedium = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 12.sp, lineHeight = 17.sp),
    labelSmall = TextStyle(fontWeight = FontWeight.Bold, fontSize = 11.sp, lineHeight = 16.sp),
)

private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(10.dp),
    large = RoundedCornerShape(12.dp),
    extraLarge = RoundedCornerShape(16.dp),
)

@Composable
fun UnderTheMaskTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = DarkColors,
        typography = AppTypography,
        shapes = AppShapes,
    ) {
        CompositionLocalProvider(
            LocalContentColor provides MaterialTheme.colorScheme.onBackground,
            content = content,
        )
    }
}
