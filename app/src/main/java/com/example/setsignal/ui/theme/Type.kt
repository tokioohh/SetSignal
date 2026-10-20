package com.example.setsignal.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material3.ColorScheme
import androidx.wear.compose.material3.Shapes
import androidx.wear.compose.material3.Typography

val SetSignalColorScheme = ColorScheme(
    primary = Lime,
    primaryDim = LimeDim,
    primaryContainer = LimeContainer,
    onPrimary = Ink,
    onPrimaryContainer = Lime,
    secondary = Teal,
    secondaryDim = TealDim,
    secondaryContainer = TealContainer,
    onSecondary = Ink,
    onSecondaryContainer = Teal,
    tertiary = Ember,
    tertiaryDim = Color(0xFFB77A38),
    tertiaryContainer = EmberContainer,
    onTertiary = Ink,
    onTertiaryContainer = Ember,
    surfaceContainerLow = Carbon,
    surfaceContainer = Graphite,
    surfaceContainerHigh = GraphiteHigh,
    onSurface = WarmWhite,
    onSurfaceVariant = Mist,
    outline = OutlineDark,
    outlineVariant = OutlineSubtle,
    background = Ink,
    onBackground = WarmWhite,
    error = Coral,
    errorDim = CoralDim,
    errorContainer = CoralContainer,
    onError = Ink,
    onErrorContainer = Color(0xFFFFD9D4),
)

private val BaseTypography = Typography()

val SetSignalTypography = Typography(
    defaultFontFamily = FontFamily.SansSerif,
    displayLarge = BaseTypography.displayLarge.copy(
        fontWeight = FontWeight.Bold,
        letterSpacing = (-1.5).sp,
    ),
    displayMedium = BaseTypography.displayMedium.copy(
        fontWeight = FontWeight.Bold,
        letterSpacing = (-1).sp,
    ),
    displaySmall = BaseTypography.displaySmall.copy(fontWeight = FontWeight.SemiBold),
    titleLarge = BaseTypography.titleLarge.copy(
        fontWeight = FontWeight.SemiBold,
        letterSpacing = (-0.4).sp,
    ),
    titleMedium = BaseTypography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
    titleSmall = BaseTypography.titleSmall.copy(fontWeight = FontWeight.Medium),
    labelLarge = BaseTypography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
    labelMedium = BaseTypography.labelMedium.copy(fontWeight = FontWeight.Medium),
    labelSmall = BaseTypography.labelSmall.copy(
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 1.1.sp,
    ),
    bodyLarge = BaseTypography.bodyLarge,
    bodyMedium = BaseTypography.bodyMedium,
    bodySmall = BaseTypography.bodySmall,
    bodyExtraSmall = BaseTypography.bodyExtraSmall,
    numeralExtraLarge = BaseTypography.numeralExtraLarge.copy(
        fontWeight = FontWeight.Bold,
        letterSpacing = (-2).sp,
    ),
    numeralLarge = BaseTypography.numeralLarge.copy(
        fontWeight = FontWeight.Bold,
        letterSpacing = (-1.5).sp,
    ),
    numeralMedium = BaseTypography.numeralMedium.copy(
        fontWeight = FontWeight.Bold,
        letterSpacing = (-0.5).sp,
    ),
    numeralSmall = BaseTypography.numeralSmall.copy(fontWeight = FontWeight.SemiBold),
    numeralExtraSmall = BaseTypography.numeralExtraSmall.copy(fontWeight = FontWeight.Medium),
)

val SetSignalShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(26.dp),
    extraLarge = RoundedCornerShape(36.dp),
)
