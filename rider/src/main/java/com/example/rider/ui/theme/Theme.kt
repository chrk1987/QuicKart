package com.example.rider.ui.theme

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
data class RiderColors(
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
    val alertRed: Color,
    val infoBlue: Color
)

private val LightRiderColors = RiderColors(
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
    alertRed = AlertRed,
    infoBlue = InfoBlue
)

private val DarkRiderColors = RiderColors(
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
    deliveryGreenDeep = DeliveryGreenDeep,
    deliveryGreenSoft = DeliveryGreenSoftDark,
    promoOrange = PromoOrange,
    alertRed = AlertRed,
    infoBlue = InfoBlue
)

val LocalRiderColors = staticCompositionLocalOf { LightRiderColors }

private val RiderLightScheme = lightColorScheme(
    primary = DeliveryGreenDeep,
    onPrimary = Color.White,
    primaryContainer = DeliveryGreenSoftLight,
    onPrimaryContainer = DeliveryGreenDeep,
    secondary = ElectricYellow,
    onSecondary = InkBlack,
    secondaryContainer = ElectricYellowSoft,
    onSecondaryContainer = InkBlack,
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

private val RiderDarkScheme = darkColorScheme(
    primary = DeliveryGreen,
    onPrimary = InkBlack,
    primaryContainer = DeliveryGreenSoftDark,
    onPrimaryContainer = DeliveryGreen,
    secondary = ElectricYellow,
    onSecondary = InkBlack,
    secondaryContainer = Color(0xFF3A3000),
    onSecondaryContainer = ElectricYellow,
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

private val RiderShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

object RiderThemeTokens {
    val colors: RiderColors
        @Composable
        get() = LocalRiderColors.current
}

@Composable
fun QuickKartRiderTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    screenZoomPercent: Int = 100,
    content: @Composable () -> Unit
) {
    val customColors = if (darkTheme) DarkRiderColors else LightRiderColors
    val m3Scheme = if (darkTheme) RiderDarkScheme else RiderLightScheme
    val typography = remember(screenZoomPercent) {
        scaledRiderTypography(screenZoomPercent)
    }

    CompositionLocalProvider(LocalRiderColors provides customColors) {
        MaterialTheme(
            colorScheme = m3Scheme,
            typography = typography,
            shapes = RiderShapes,
            content = content
        )
    }
}
