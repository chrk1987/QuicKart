package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Immutable
data class QuickKartColors(
    val isDark: Boolean,
    val background: Color,
    val surface: Color,
    val surfaceElevated: Color,
    val ink: Color,
    val muted: Color,
    val border: Color,
    val brandYellow: Color,
    val onBrandYellow: Color,
    val brandYellowSoft: Color,
    val deliveryGreen: Color,
    val deliveryGreenDeep: Color,
    val deliveryGreenSoft: Color,
    val promoOrange: Color,
    val alertRed: Color
)

private val LightQuickKartColors = QuickKartColors(
    isDark = false,
    background = LightBackground,
    surface = LightSurface,
    surfaceElevated = LightSurfaceElevated,
    ink = LightInk,
    muted = LightMuted,
    border = LightBorder,
    brandYellow = ElectricYellow,
    onBrandYellow = InkBlack,
    brandYellowSoft = ElectricYellowSoft,
    deliveryGreen = DeliveryGreen,
    deliveryGreenDeep = DeliveryGreenDeep,
    deliveryGreenSoft = DeliveryGreenSoftLight,
    promoOrange = PromoOrange,
    alertRed = AlertRed
)

private val DarkQuickKartColors = QuickKartColors(
    isDark = true,
    background = DarkBackground,
    surface = DarkSurface,
    surfaceElevated = DarkSurfaceElevated,
    ink = DarkText,
    muted = DarkMuted,
    border = DarkBorder,
    brandYellow = ElectricYellow,
    onBrandYellow = InkBlack,
    brandYellowSoft = Color(0xFF332B00),
    deliveryGreen = DeliveryGreen,
    deliveryGreenDeep = DeliveryGreenDeepDark,
    deliveryGreenSoft = DeliveryGreenSoftDark,
    promoOrange = PromoOrange,
    alertRed = AlertRed
)

val LocalQuickKartColors = staticCompositionLocalOf { LightQuickKartColors }

private val QuickKartLightScheme = lightColorScheme(
    // Material defaults (text-field labels/cursor, unstyled text buttons, checkboxes) use `primary`.
    // Yellow is unreadable on white, so light mode uses ink; brand-yellow surfaces set their colors explicitly.
    primary = InkBlack,
    onPrimary = Color.White,
    // Keep the brand's warm yellow tint on elevated surfaces (e.g. the cream bottom bar).
    surfaceTint = ElectricYellow,
    primaryContainer = ElectricYellowSoft,
    onPrimaryContainer = InkBlack,
    secondary = DeliveryGreenDeep,
    onSecondary = Color.White,
    secondaryContainer = DeliveryGreenSoftLight,
    onSecondaryContainer = DeliveryGreenDeep,
    tertiary = PromoOrange,
    onTertiary = Color.White,
    background = LightBackground,
    onBackground = LightInk,
    surface = LightSurface,
    onSurface = LightInk,
    surfaceVariant = LightSurfaceElevated,
    onSurfaceVariant = LightMuted,
    outline = LightBorder,
    error = AlertRed,
    onError = Color.White
)

private val QuickKartDarkScheme = darkColorScheme(
    primary = ElectricYellow,
    onPrimary = InkBlack,
    primaryContainer = Color(0xFF3A3000),
    onPrimaryContainer = ElectricYellow,
    secondary = DeliveryGreen,
    onSecondary = InkBlack,
    secondaryContainer = DeliveryGreenSoftDark,
    onSecondaryContainer = DeliveryGreen,
    tertiary = PromoOrange,
    onTertiary = InkBlack,
    background = DarkBackground,
    onBackground = DarkText,
    surface = DarkSurface,
    onSurface = DarkText,
    surfaceVariant = DarkSurfaceElevated,
    onSurfaceVariant = DarkMuted,
    outline = DarkBorder,
    error = AlertRed,
    onError = Color.White
)

private val QuickKartShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

object QuickKartThemeTokens {
    val colors: QuickKartColors
        @Composable
        get() = LocalQuickKartColors.current
}

@Composable
fun QuickKartTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    screenZoomPercent: Int = 100,
    content: @Composable () -> Unit
) {
    val customColors = if (darkTheme) DarkQuickKartColors else LightQuickKartColors
    val m3Scheme = if (darkTheme) QuickKartDarkScheme else QuickKartLightScheme
    val typography = remember(screenZoomPercent) {
        scaledQuickKartTypography(screenZoomPercent)
    }

    CompositionLocalProvider(LocalQuickKartColors provides customColors) {
        MaterialTheme(
            colorScheme = m3Scheme,
            typography = typography,
            shapes = QuickKartShapes,
            content = content
        )
    }
}
