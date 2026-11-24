package com.example.ui.theme

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
    SMALL_COMPACT, // < 360.dp (compact narrow phones)
    COMPACT,       // 360.dp .. 599.dp (standard phones)
    MEDIUM,        // 600.dp .. 839.dp (foldables, large phones in landscape, small tablets)
    EXPANDED       // >= 840.dp (tablets, desktops)
}

@Immutable
data class QuickKartMetrics(
    val sizeClass: DeviceSizeClass,
    val zoomPercent: Int,
    val zoomScale: Float,
    val iconSmall: Dp,
    val iconMedium: Dp,
    val iconLarge: Dp,
    val iconHero: Dp,
    val navIconSize: Dp,
    val badgeButtonSize: Dp,
    val avatarSize: Dp,
    val screenHorizontalPadding: Dp,
    val cardPadding: Dp,
    val itemSpacing: Dp,
    val productArtworkHeight: Dp,
    val gridColumns: Int,
    val useNavRail: Boolean,
    val isNarrowPhone: Boolean
)

val LocalQuickKartMetrics = staticCompositionLocalOf {
    metricsForWidth(400.dp, DEFAULT_SCREEN_ZOOM_PERCENT)
}

fun metricsForWidth(
    maxWidth: Dp,
    screenZoomPercent: Int = DEFAULT_SCREEN_ZOOM_PERCENT
): QuickKartMetrics {
    val clampedZoom = screenZoomPercent.coerceIn(MIN_SCREEN_ZOOM_PERCENT, MAX_SCREEN_ZOOM_PERCENT)
    val scale = clampedZoom / 100f

    val deviceClass = when {
        maxWidth < 360.dp -> DeviceSizeClass.SMALL_COMPACT
        maxWidth < 600.dp -> DeviceSizeClass.COMPACT
        maxWidth < 840.dp -> DeviceSizeClass.MEDIUM
        else -> DeviceSizeClass.EXPANDED
    }

    val base = when (deviceClass) {
        DeviceSizeClass.SMALL_COMPACT -> QuickKartMetrics(
            sizeClass = sizeClassOrDefault(deviceClass),
            zoomPercent = clampedZoom,
            zoomScale = scale,
            iconSmall = 14.dp,
            iconMedium = 18.dp,
            iconLarge = 22.dp,
            iconHero = 28.dp,
            navIconSize = 22.dp,
            badgeButtonSize = 34.dp,
            avatarSize = 46.dp,
            screenHorizontalPadding = 12.dp,
            cardPadding = 10.dp,
            itemSpacing = 10.dp,
            productArtworkHeight = 98.dp,
            gridColumns = 2,
            useNavRail = false,
            isNarrowPhone = true
        )

        DeviceSizeClass.COMPACT -> QuickKartMetrics(
            sizeClass = sizeClassOrDefault(deviceClass),
            zoomPercent = clampedZoom,
            zoomScale = scale,
            iconSmall = 16.dp,
            iconMedium = 20.dp,
            iconLarge = 24.dp,
            iconHero = 32.dp,
            navIconSize = 24.dp,
            badgeButtonSize = 38.dp,
            avatarSize = 54.dp,
            screenHorizontalPadding = 16.dp,
            cardPadding = 14.dp,
            itemSpacing = 12.dp,
            productArtworkHeight = 112.dp,
            gridColumns = 2,
            useNavRail = false,
            isNarrowPhone = false
        )

        DeviceSizeClass.MEDIUM -> QuickKartMetrics(
            sizeClass = sizeClassOrDefault(deviceClass),
            zoomPercent = clampedZoom,
            zoomScale = scale,
            iconSmall = 18.dp,
            iconMedium = 22.dp,
            iconLarge = 26.dp,
            iconHero = 36.dp,
            navIconSize = 26.dp,
            badgeButtonSize = 42.dp,
            avatarSize = 60.dp,
            screenHorizontalPadding = 20.dp,
            cardPadding = 16.dp,
            itemSpacing = 14.dp,
            productArtworkHeight = 128.dp,
            gridColumns = 3,
            useNavRail = false,
            isNarrowPhone = false
        )

        DeviceSizeClass.EXPANDED -> QuickKartMetrics(
            sizeClass = sizeClassOrDefault(deviceClass),
            zoomPercent = clampedZoom,
            zoomScale = scale,
            iconSmall = 18.dp,
            iconMedium = 24.dp,
            iconLarge = 28.dp,
            iconHero = 40.dp,
            navIconSize = 26.dp,
            badgeButtonSize = 44.dp,
            avatarSize = 64.dp,
            screenHorizontalPadding = 24.dp,
            cardPadding = 18.dp,
            itemSpacing = 16.dp,
            productArtworkHeight = 136.dp,
            gridColumns = 4,
            useNavRail = true,
            isNarrowPhone = false
        )
    }

    if (clampedZoom == DEFAULT_SCREEN_ZOOM_PERCENT) return base

    // Effective logical dp width after zoom determines column count & narrow layout adaptation
    val effectiveWidth = (maxWidth.value / scale).dp
    val adaptedGridColumns = when {
        effectiveWidth >= 840.dp -> 4
        effectiveWidth >= 560.dp -> 3
        effectiveWidth < 285.dp -> 1
        else -> 2
    }
    val adaptedNarrow = effectiveWidth < 360.dp
    val adaptedNavRail = deviceClass == DeviceSizeClass.EXPANDED && effectiveWidth >= 600.dp

    // Gently temper padding at high zoom so larger fonts & icons keep maximum horizontal space
    val paddingFactor = if (scale > 1f) {
        (1f - (scale - 1f) * 0.25f).coerceAtLeast(0.88f)
    } else {
        (0.92f + (scale - 0.8f) * 0.4f).coerceIn(0.88f, 1f)
    }
    val artworkFactor = 1f + (scale - 1f) * 0.65f

    return base.copy(
        iconSmall = (base.iconSmall * scale).coerceIn(12.dp, 24.dp),
        iconMedium = (base.iconMedium * scale).coerceIn(16.dp, 30.dp),
        iconLarge = (base.iconLarge * scale).coerceIn(20.dp, 36.dp),
        iconHero = (base.iconHero * scale).coerceIn(24.dp, 48.dp),
        navIconSize = (base.navIconSize * scale).coerceIn(20.dp, 32.dp),
        badgeButtonSize = (base.badgeButtonSize * scale).coerceIn(32.dp, 52.dp),
        avatarSize = (base.avatarSize * scale).coerceIn(42.dp, 76.dp),
        screenHorizontalPadding = (base.screenHorizontalPadding * paddingFactor).coerceIn(10.dp, 26.dp),
        cardPadding = (base.cardPadding * paddingFactor).coerceIn(8.dp, 20.dp),
        itemSpacing = (base.itemSpacing * paddingFactor).coerceIn(8.dp, 18.dp),
        productArtworkHeight = (base.productArtworkHeight * artworkFactor).coerceIn(84.dp, 160.dp),
        gridColumns = adaptedGridColumns,
        useNavRail = adaptedNavRail,
        isNarrowPhone = adaptedNarrow
    )
}

private fun sizeClassOrDefault(sizeClass: DeviceSizeClass): DeviceSizeClass = sizeClass

object QuickKartMetricsTokens {
    val metrics: QuickKartMetrics
        @Composable
        get() = LocalQuickKartMetrics.current
}
