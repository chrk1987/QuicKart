package com.example.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class DeviceSizeClass {
    SMALL_COMPACT, // < 360.dp (compact narrow phones)
    COMPACT,       // 360.dp .. 599.dp (standard phones)
    MEDIUM,        // 600.dp .. 839.dp (foldables, large phones in landscape, small tablets)
    EXPANDED       // >= 840.dp (tablets, desktops)
}

@Immutable
data class QuickKartMetrics(
    val sizeClass: DeviceSizeClass,
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
    metricsForWidth(400.dp)
}

fun metricsForWidth(maxWidth: Dp): QuickKartMetrics {
    val sizeClass = when {
        maxWidth < 360.dp -> DeviceSizeClass.SMALL_COMPACT
        maxWidth < 600.dp -> DeviceSizeClass.COMPACT
        maxWidth < 840.dp -> DeviceSizeClass.MEDIUM
        else -> DeviceSizeClass.EXPANDED
    }

    return when (sizeClass) {
        DeviceSizeClass.SMALL_COMPACT -> QuickKartMetrics(
            sizeClass = sizeClass,
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
            sizeClass = sizeClass,
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
            sizeClass = sizeClass,
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
            sizeClass = sizeClass,
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
}

object QuickKartMetricsTokens {
    val metrics: QuickKartMetrics
        @Composable
        get() = LocalQuickKartMetrics.current
}
