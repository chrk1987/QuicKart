package com.example.rider.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.coerceIn
import androidx.compose.ui.unit.dp

const val MIN_SCREEN_ZOOM_PERCENT = 80
const val DEFAULT_SCREEN_ZOOM_PERCENT = 100
const val MAX_SCREEN_ZOOM_PERCENT = 130

enum class DeviceSizeClass {
    SMALL_COMPACT,
    COMPACT,
    MEDIUM,
    EXPANDED
}

@Immutable
data class RiderMetrics(
    val sizeClass: DeviceSizeClass,
    val zoomPercent: Int,
    val zoomScale: Float,
    val iconSmall: Dp,
    val iconMedium: Dp,
    val iconLarge: Dp,
    val iconHero: Dp,
    val navIconSize: Dp,
    val avatarSize: Dp,
    val screenHorizontalPadding: Dp,
    val cardPadding: Dp,
    val itemSpacing: Dp,
    val useNavRail: Boolean,
    val isNarrowPhone: Boolean
)

val LocalRiderMetrics = staticCompositionLocalOf {
    metricsForWidth(400.dp, DEFAULT_SCREEN_ZOOM_PERCENT)
}

fun metricsForWidth(
    maxWidth: Dp,
    screenZoomPercent: Int = DEFAULT_SCREEN_ZOOM_PERCENT
): RiderMetrics {
    val clampedZoom = screenZoomPercent.coerceIn(MIN_SCREEN_ZOOM_PERCENT, MAX_SCREEN_ZOOM_PERCENT)
    val scale = clampedZoom / 100f

    val deviceClass = when {
        maxWidth < 360.dp -> DeviceSizeClass.SMALL_COMPACT
        maxWidth < 600.dp -> DeviceSizeClass.COMPACT
        maxWidth < 840.dp -> DeviceSizeClass.MEDIUM
        else -> DeviceSizeClass.EXPANDED
    }

    val base = when (deviceClass) {
        DeviceSizeClass.SMALL_COMPACT -> RiderMetrics(
            sizeClass = deviceClass,
            zoomPercent = clampedZoom,
            zoomScale = scale,
            iconSmall = 14.dp,
            iconMedium = 18.dp,
            iconLarge = 22.dp,
            iconHero = 28.dp,
            navIconSize = 22.dp,
            avatarSize = 46.dp,
            screenHorizontalPadding = 12.dp,
            cardPadding = 12.dp,
            itemSpacing = 10.dp,
            useNavRail = false,
            isNarrowPhone = true
        )
        DeviceSizeClass.COMPACT -> RiderMetrics(
            sizeClass = deviceClass,
            zoomPercent = clampedZoom,
            zoomScale = scale,
            iconSmall = 16.dp,
            iconMedium = 20.dp,
            iconLarge = 24.dp,
            iconHero = 32.dp,
            navIconSize = 24.dp,
            avatarSize = 54.dp,
            screenHorizontalPadding = 16.dp,
            cardPadding = 14.dp,
            itemSpacing = 12.dp,
            useNavRail = false,
            isNarrowPhone = false
        )
        DeviceSizeClass.MEDIUM -> RiderMetrics(
            sizeClass = deviceClass,
            zoomPercent = clampedZoom,
            zoomScale = scale,
            iconSmall = 18.dp,
            iconMedium = 22.dp,
            iconLarge = 26.dp,
            iconHero = 36.dp,
            navIconSize = 26.dp,
            avatarSize = 60.dp,
            screenHorizontalPadding = 20.dp,
            cardPadding = 16.dp,
            itemSpacing = 14.dp,
            useNavRail = false,
            isNarrowPhone = false
        )
        DeviceSizeClass.EXPANDED -> RiderMetrics(
            sizeClass = deviceClass,
            zoomPercent = clampedZoom,
            zoomScale = scale,
            iconSmall = 18.dp,
            iconMedium = 24.dp,
            iconLarge = 28.dp,
            iconHero = 40.dp,
            navIconSize = 26.dp,
            avatarSize = 64.dp,
            screenHorizontalPadding = 24.dp,
            cardPadding = 18.dp,
            itemSpacing = 16.dp,
            useNavRail = true,
            isNarrowPhone = false
        )
    }

    if (clampedZoom == DEFAULT_SCREEN_ZOOM_PERCENT) return base

    val effectiveWidth = (maxWidth.value / scale).dp
    val adaptedNarrow = effectiveWidth < 360.dp
    val adaptedNavRail = deviceClass == DeviceSizeClass.EXPANDED && effectiveWidth >= 600.dp
    val paddingFactor = if (scale > 1f) {
        (1f - (scale - 1f) * 0.25f).coerceAtLeast(0.88f)
    } else {
        (0.92f + (scale - 0.8f) * 0.4f).coerceIn(0.88f, 1f)
    }

    return base.copy(
        iconSmall = (base.iconSmall * scale).coerceIn(12.dp, 24.dp),
        iconMedium = (base.iconMedium * scale).coerceIn(16.dp, 30.dp),
        iconLarge = (base.iconLarge * scale).coerceIn(20.dp, 36.dp),
        iconHero = (base.iconHero * scale).coerceIn(24.dp, 48.dp),
        navIconSize = (base.navIconSize * scale).coerceIn(20.dp, 32.dp),
        avatarSize = (base.avatarSize * scale).coerceIn(42.dp, 76.dp),
        screenHorizontalPadding = (base.screenHorizontalPadding * paddingFactor).coerceIn(10.dp, 26.dp),
        cardPadding = (base.cardPadding * paddingFactor).coerceIn(10.dp, 20.dp),
        itemSpacing = (base.itemSpacing * paddingFactor).coerceIn(8.dp, 18.dp),
        useNavRail = adaptedNavRail,
        isNarrowPhone = adaptedNarrow
    )
}

object RiderMetricsTokens {
    val metrics: RiderMetrics
        @Composable
        get() = LocalRiderMetrics.current
}
