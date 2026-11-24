package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.data.model.ProductArtworkType
import com.example.ui.theme.QuickKartThemeTokens

@Composable
fun ProductArtwork(
    artworkType: ProductArtworkType,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val tokens = QuickKartThemeTokens.colors
    val bgStart = accentColor.copy(alpha = if (tokens.isDark) 0.22f else 0.14f)
    val bgEnd = accentColor.copy(alpha = if (tokens.isDark) 0.08f else 0.04f)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.radialGradient(listOf(bgStart, bgEnd))),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp)
        ) {
            val w = size.width
            val h = size.height
            val cx = w / 2f
            val cy = h / 2f

            // Subtle soft shadow oval under item
            drawOval(
                color = Color.Black.copy(alpha = if (tokens.isDark) 0.25f else 0.08f),
                topLeft = Offset(w * 0.2f, h * 0.84f),
                size = Size(w * 0.6f, h * 0.1f)
            )

            when (artworkType) {
                ProductArtworkType.MILK_BOTTLE -> {
                    val bottlePath = Path().apply {
                        moveTo(cx - w * 0.12f, h * 0.14f)
                        lineTo(cx + w * 0.12f, h * 0.14f)
                        lineTo(cx + w * 0.12f, h * 0.28f)
                        lineTo(cx + w * 0.24f, h * 0.42f)
                        lineTo(cx + w * 0.24f, h * 0.85f)
                        lineTo(cx - w * 0.24f, h * 0.85f)
                        lineTo(cx - w * 0.24f, h * 0.42f)
                        lineTo(cx - w * 0.12f, h * 0.28f)
                        close()
                    }
                    drawPath(bottlePath, color = Color.White)
                    drawPath(bottlePath, color = accentColor, style = Stroke(width = 4f))
                    drawRoundRect(
                        color = accentColor,
                        topLeft = Offset(cx - w * 0.22f, h * 0.50f),
                        size = Size(w * 0.44f, h * 0.22f),
                        cornerRadius = CornerRadius(8f, 8f)
                    )
                    drawRoundRect(
                        color = Color(0xFFFFD400),
                        topLeft = Offset(cx - w * 0.13f, h * 0.08f),
                        size = Size(w * 0.26f, h * 0.08f),
                        cornerRadius = CornerRadius(6f, 6f)
                    )
                }

                ProductArtworkType.EGGS_CARTON -> {
                    drawRoundRect(
                        color = Color(0xFFD9B99B),
                        topLeft = Offset(w * 0.14f, h * 0.52f),
                        size = Size(w * 0.72f, h * 0.30f),
                        cornerRadius = CornerRadius(14f, 14f)
                    )
                    val positions = listOf(0.28f, 0.50f, 0.72f)
                    positions.forEach { px ->
                        drawOval(
                            color = Color(0xFFC68642),
                            topLeft = Offset(w * px - w * 0.11f, h * 0.28f),
                            size = Size(w * 0.22f, h * 0.30f)
                        )
                        drawOval(
                            color = Color(0xFFE0AC69).copy(alpha = 0.5f),
                            topLeft = Offset(w * px - w * 0.06f, h * 0.32f),
                            size = Size(w * 0.07f, h * 0.10f)
                        )
                    }
                }

                ProductArtworkType.PANEER_BLOCK -> {
                    drawRoundRect(
                        color = Color(0xFFFFFDF7),
                        topLeft = Offset(w * 0.18f, h * 0.28f),
                        size = Size(w * 0.64f, h * 0.48f),
                        cornerRadius = CornerRadius(16f, 16f)
                    )
                    drawRoundRect(
                        color = accentColor.copy(alpha = 0.5f),
                        topLeft = Offset(w * 0.18f, h * 0.28f),
                        size = Size(w * 0.64f, h * 0.48f),
                        cornerRadius = CornerRadius(16f, 16f),
                        style = Stroke(width = 4f)
                    )
                    drawLine(
                        color = Color(0xFFE5E7EB),
                        start = Offset(w * 0.48f, h * 0.28f),
                        end = Offset(w * 0.48f, h * 0.76f),
                        strokeWidth = 3f
                    )
                }

                ProductArtworkType.GREEK_YOGURT -> {
                    drawRoundRect(
                        color = Color.White,
                        topLeft = Offset(w * 0.22f, h * 0.30f),
                        size = Size(w * 0.56f, h * 0.50f),
                        cornerRadius = CornerRadius(18f, 18f)
                    )
                    drawRoundRect(
                        color = accentColor,
                        topLeft = Offset(w * 0.18f, h * 0.22f),
                        size = Size(w * 0.64f, h * 0.12f),
                        cornerRadius = CornerRadius(10f, 10f)
                    )
                    drawCircle(
                        color = Color(0xFFF59E0B),
                        radius = w * 0.12f,
                        center = Offset(cx, h * 0.58f)
                    )
                }

                ProductArtworkType.AVOCADO -> {
                    drawOval(
                        color = Color(0xFF15803D),
                        topLeft = Offset(w * 0.24f, h * 0.18f),
                        size = Size(w * 0.52f, h * 0.66f)
                    )
                    drawOval(
                        color = Color(0xFFBEF264),
                        topLeft = Offset(w * 0.30f, h * 0.24f),
                        size = Size(w * 0.40f, h * 0.54f)
                    )
                    drawCircle(
                        color = Color(0xFF78350F),
                        radius = w * 0.12f,
                        center = Offset(cx, h * 0.58f)
                    )
                }

                ProductArtworkType.BANANAS -> {
                    val banana = Path().apply {
                        moveTo(w * 0.22f, h * 0.35f)
                        quadraticTo(w * 0.48f, h * 0.85f, w * 0.80f, h * 0.52f)
                        quadraticTo(w * 0.52f, h * 0.62f, w * 0.28f, h * 0.24f)
                        close()
                    }
                    drawPath(banana, color = Color(0xFFFACC15))
                    drawPath(banana, color = Color(0xFFCA8A04), style = Stroke(width = 3f))
                }

                ProductArtworkType.BABY_SPINACH -> {
                    listOf(-20f, 0f, 22f).forEachIndexed { idx, dx ->
                        val leafCenter = Offset(cx + dx, h * (0.48f + idx * 0.03f))
                        drawOval(
                            color = if (idx == 1) Color(0xFF22C55E) else Color(0xFF16A34A),
                            topLeft = Offset(leafCenter.x - w * 0.14f, leafCenter.y - h * 0.22f),
                            size = Size(w * 0.28f, h * 0.44f)
                        )
                    }
                }

                ProductArtworkType.TOMATOES -> {
                    drawCircle(Color(0xFFEF4444), radius = w * 0.18f, center = Offset(w * 0.38f, h * 0.56f))
                    drawCircle(Color(0xFFDC2626), radius = w * 0.18f, center = Offset(w * 0.62f, h * 0.58f))
                    drawCircle(Color(0xFFF87171), radius = w * 0.16f, center = Offset(w * 0.50f, h * 0.36f))
                    drawCircle(Color(0xFF22C55E), radius = w * 0.04f, center = Offset(w * 0.50f, h * 0.22f))
                }

                ProductArtworkType.SOURDOUGH -> {
                    drawOval(
                        color = Color(0xFFB45309),
                        topLeft = Offset(w * 0.14f, h * 0.30f),
                        size = Size(w * 0.72f, h * 0.46f)
                    )
                    drawOval(
                        color = Color(0xFFF59E0B),
                        topLeft = Offset(w * 0.20f, h * 0.34f),
                        size = Size(w * 0.60f, h * 0.34f)
                    )
                    drawLine(Color(0xFFFEF3C7), Offset(w * 0.34f, h * 0.44f), Offset(w * 0.46f, h * 0.56f), 5f, StrokeCap.Round)
                    drawLine(Color(0xFFFEF3C7), Offset(w * 0.52f, h * 0.42f), Offset(w * 0.64f, h * 0.54f), 5f, StrokeCap.Round)
                }

                ProductArtworkType.CROISSANT, ProductArtworkType.BRIOCHE -> {
                    drawOval(
                        color = Color(0xFFD97706),
                        topLeft = Offset(w * 0.16f, h * 0.32f),
                        size = Size(w * 0.68f, h * 0.42f)
                    )
                    drawOval(
                        color = Color(0xFFFBBF24),
                        topLeft = Offset(w * 0.24f, h * 0.36f),
                        size = Size(w * 0.52f, h * 0.30f)
                    )
                    drawCircle(Color(0xFFFEF3C7), 4f, Offset(w * 0.42f, h * 0.46f))
                    drawCircle(Color(0xFFFEF3C7), 4f, Offset(w * 0.54f, h * 0.48f))
                    drawCircle(Color(0xFFFEF3C7), 4f, Offset(w * 0.48f, h * 0.54f))
                }

                ProductArtworkType.COLD_BREW -> {
                    drawRoundRect(
                        color = Color(0xFF451A03),
                        topLeft = Offset(w * 0.28f, h * 0.20f),
                        size = Size(w * 0.44f, h * 0.64f),
                        cornerRadius = CornerRadius(14f, 14f)
                    )
                    drawRoundRect(
                        color = Color(0xFFD97706),
                        topLeft = Offset(w * 0.28f, h * 0.38f),
                        size = Size(w * 0.44f, h * 0.24f),
                        cornerRadius = CornerRadius(4f, 4f)
                    )
                    drawRoundRect(
                        color = Color(0xFFE5E7EB),
                        topLeft = Offset(w * 0.30f, h * 0.16f),
                        size = Size(w * 0.40f, h * 0.08f),
                        cornerRadius = CornerRadius(6f, 6f)
                    )
                }

                ProductArtworkType.ORANGE_JUICE, ProductArtworkType.SUGARCANE_JUICE -> {
                    drawRoundRect(
                        color = accentColor,
                        topLeft = Offset(w * 0.28f, h * 0.26f),
                        size = Size(w * 0.44f, h * 0.58f),
                        cornerRadius = CornerRadius(14f, 14f)
                    )
                    drawRoundRect(
                        color = Color.White.copy(alpha = 0.85f),
                        topLeft = Offset(w * 0.32f, h * 0.44f),
                        size = Size(w * 0.36f, h * 0.20f),
                        cornerRadius = CornerRadius(6f, 6f)
                    )
                    drawRoundRect(
                        color = Color(0xFF141414),
                        topLeft = Offset(w * 0.36f, h * 0.16f),
                        size = Size(w * 0.28f, h * 0.10f),
                        cornerRadius = CornerRadius(6f, 6f)
                    )
                }

                ProductArtworkType.KETTLE_CHIPS, ProductArtworkType.MAKHANA -> {
                    drawRoundRect(
                        color = accentColor,
                        topLeft = Offset(w * 0.22f, h * 0.18f),
                        size = Size(w * 0.56f, h * 0.66f),
                        cornerRadius = CornerRadius(12f, 12f)
                    )
                    drawCircle(
                        color = Color(0xFFFFD400),
                        radius = w * 0.16f,
                        center = Offset(cx, cy)
                    )
                }

                ProductArtworkType.DARK_CHOCOLATE -> {
                    drawRoundRect(
                        color = Color(0xFF451A03),
                        topLeft = Offset(w * 0.22f, h * 0.20f),
                        size = Size(w * 0.56f, h * 0.64f),
                        cornerRadius = CornerRadius(10f, 10f)
                    )
                    drawRoundRect(
                        color = Color(0xFFFFD400),
                        topLeft = Offset(w * 0.22f, h * 0.44f),
                        size = Size(w * 0.56f, h * 0.34f),
                        cornerRadius = CornerRadius(8f, 8f)
                    )
                }

                ProductArtworkType.PEANUT_BUTTER -> {
                    drawRoundRect(
                        color = Color(0xFFD97706),
                        topLeft = Offset(w * 0.24f, h * 0.28f),
                        size = Size(w * 0.52f, h * 0.54f),
                        cornerRadius = CornerRadius(16f, 16f)
                    )
                    drawRoundRect(
                        color = Color(0xFF141414),
                        topLeft = Offset(w * 0.28f, h * 0.18f),
                        size = Size(w * 0.44f, h * 0.12f),
                        cornerRadius = CornerRadius(8f, 8f)
                    )
                    drawRoundRect(
                        color = Color(0xFFFEF3C7),
                        topLeft = Offset(w * 0.28f, h * 0.42f),
                        size = Size(w * 0.44f, h * 0.24f),
                        cornerRadius = CornerRadius(6f, 6f)
                    )
                }

                // ── Original flat artwork for the newer categories (no photos / third-party art) ──

                ProductArtworkType.APPLE -> {
                    drawCircle(accentColor, radius = w * 0.26f, center = Offset(cx - w * 0.07f, h * 0.56f))
                    drawCircle(accentColor, radius = w * 0.26f, center = Offset(cx + w * 0.07f, h * 0.56f))
                    drawCircle(Color.White.copy(alpha = 0.35f), radius = w * 0.06f, center = Offset(cx - w * 0.16f, h * 0.46f))
                    drawLine(Color(0xFF78350F), Offset(cx, h * 0.32f), Offset(cx + w * 0.03f, h * 0.18f), 6f, StrokeCap.Round)
                    drawOval(
                        color = Color(0xFF22C55E),
                        topLeft = Offset(cx + w * 0.03f, h * 0.16f),
                        size = Size(w * 0.16f, h * 0.08f)
                    )
                }

                ProductArtworkType.GRAPES -> {
                    val r = w * 0.085f
                    listOf(
                        -1.5f to 0.36f, -0.5f to 0.36f, 0.5f to 0.36f, 1.5f to 0.36f,
                        -1f to 0.50f, 0f to 0.50f, 1f to 0.50f,
                        -0.5f to 0.64f, 0.5f to 0.64f,
                        0f to 0.77f
                    ).forEach { (dx, fy) ->
                        drawCircle(accentColor, radius = r, center = Offset(cx + dx * r * 1.9f, h * fy))
                        drawCircle(Color.White.copy(alpha = 0.35f), radius = r * 0.3f, center = Offset(cx + dx * r * 1.9f - r * 0.3f, h * fy - r * 0.3f))
                    }
                    drawLine(Color(0xFF78350F), Offset(cx, h * 0.27f), Offset(cx + w * 0.06f, h * 0.14f), 5f, StrokeCap.Round)
                }

                ProductArtworkType.POMEGRANATE -> {
                    drawCircle(accentColor, radius = w * 0.28f, center = Offset(cx, h * 0.56f))
                    val crown = Path().apply {
                        moveTo(cx - w * 0.10f, h * 0.30f)
                        lineTo(cx - w * 0.07f, h * 0.20f)
                        lineTo(cx - w * 0.02f, h * 0.27f)
                        lineTo(cx + w * 0.02f, h * 0.19f)
                        lineTo(cx + w * 0.06f, h * 0.27f)
                        lineTo(cx + w * 0.10f, h * 0.20f)
                        lineTo(cx + w * 0.11f, h * 0.30f)
                        close()
                    }
                    drawPath(crown, color = accentColor)
                    drawCircle(Color.White.copy(alpha = 0.3f), radius = w * 0.07f, center = Offset(cx - w * 0.12f, h * 0.46f))
                }

                ProductArtworkType.CHARGER -> {
                    drawRoundRect(
                        color = Color.White,
                        topLeft = Offset(w * 0.26f, h * 0.32f),
                        size = Size(w * 0.48f, h * 0.48f),
                        cornerRadius = CornerRadius(18f, 18f)
                    )
                    drawRoundRect(
                        color = accentColor,
                        topLeft = Offset(w * 0.26f, h * 0.32f),
                        size = Size(w * 0.48f, h * 0.48f),
                        cornerRadius = CornerRadius(18f, 18f),
                        style = Stroke(width = 4f)
                    )
                    drawRoundRect(accentColor, Offset(w * 0.38f, h * 0.18f), Size(w * 0.06f, h * 0.15f), CornerRadius(3f, 3f))
                    drawRoundRect(accentColor, Offset(w * 0.56f, h * 0.18f), Size(w * 0.06f, h * 0.15f), CornerRadius(3f, 3f))
                    drawRoundRect(Color(0xFF141414), Offset(cx - w * 0.08f, h * 0.60f), Size(w * 0.16f, h * 0.06f), CornerRadius(6f, 6f))
                }

                ProductArtworkType.USB_CABLE -> {
                    val coil = Path().apply {
                        moveTo(w * 0.22f, h * 0.30f)
                        cubicTo(w * 0.80f, h * 0.20f, w * 0.20f, h * 0.70f, w * 0.78f, h * 0.70f)
                    }
                    drawPath(coil, color = accentColor, style = Stroke(width = 9f, cap = StrokeCap.Round))
                    drawRoundRect(Color(0xFF141414), Offset(w * 0.12f, h * 0.24f), Size(w * 0.14f, h * 0.12f), CornerRadius(6f, 6f))
                    drawRoundRect(Color(0xFF9CA3AF), Offset(w * 0.06f, h * 0.27f), Size(w * 0.07f, h * 0.06f), CornerRadius(3f, 3f))
                    drawRoundRect(Color(0xFF141414), Offset(w * 0.74f, h * 0.64f), Size(w * 0.14f, h * 0.12f), CornerRadius(6f, 6f))
                    drawRoundRect(Color(0xFF9CA3AF), Offset(w * 0.87f, h * 0.67f), Size(w * 0.07f, h * 0.06f), CornerRadius(3f, 3f))
                }

                ProductArtworkType.EARBUDS -> {
                    drawRoundRect(
                        color = Color.White,
                        topLeft = Offset(w * 0.20f, h * 0.44f),
                        size = Size(w * 0.60f, h * 0.38f),
                        cornerRadius = CornerRadius(40f, 40f)
                    )
                    drawRoundRect(
                        color = accentColor.copy(alpha = 0.25f),
                        topLeft = Offset(w * 0.20f, h * 0.44f),
                        size = Size(w * 0.60f, h * 0.38f),
                        cornerRadius = CornerRadius(40f, 40f),
                        style = Stroke(width = 4f)
                    )
                    drawLine(accentColor.copy(alpha = 0.25f), Offset(w * 0.20f, h * 0.58f), Offset(w * 0.80f, h * 0.58f), 3f)
                    listOf(cx - w * 0.14f, cx + w * 0.14f).forEach { bx ->
                        drawCircle(accentColor, radius = w * 0.09f, center = Offset(bx, h * 0.30f))
                        drawRoundRect(accentColor, Offset(bx - w * 0.03f, h * 0.30f), Size(w * 0.06f, h * 0.16f), CornerRadius(8f, 8f))
                    }
                    drawCircle(Color(0xFF22C55E), radius = w * 0.02f, center = Offset(cx, h * 0.72f))
                }

                ProductArtworkType.BATTERIES -> {
                    listOf(-1.5f, -0.5f, 0.5f, 1.5f).forEach { i ->
                        val left = cx + i * w * 0.15f - w * 0.06f
                        drawRoundRect(Color(0xFF141414), Offset(left, h * 0.26f), Size(w * 0.12f, h * 0.56f), CornerRadius(8f, 8f))
                        drawRoundRect(accentColor, Offset(left, h * 0.26f), Size(w * 0.12f, h * 0.20f), CornerRadius(8f, 8f))
                        drawRoundRect(Color(0xFF9CA3AF), Offset(left + w * 0.035f, h * 0.21f), Size(w * 0.05f, h * 0.06f), CornerRadius(3f, 3f))
                    }
                }

                ProductArtworkType.TOOTHPASTE -> {
                    val tube = Path().apply {
                        moveTo(w * 0.16f, h * 0.42f)
                        lineTo(w * 0.70f, h * 0.36f)
                        lineTo(w * 0.70f, h * 0.70f)
                        lineTo(w * 0.16f, h * 0.64f)
                        close()
                    }
                    drawPath(tube, color = Color.White)
                    drawPath(tube, color = accentColor, style = Stroke(width = 4f))
                    drawRoundRect(accentColor, Offset(w * 0.26f, h * 0.45f), Size(w * 0.34f, h * 0.15f), CornerRadius(6f, 6f))
                    drawRoundRect(accentColor, Offset(w * 0.70f, h * 0.44f), Size(w * 0.16f, h * 0.18f), CornerRadius(6f, 6f))
                }

                ProductArtworkType.PUMP_BOTTLE -> {
                    drawRoundRect(accentColor, Offset(w * 0.28f, h * 0.36f), Size(w * 0.44f, h * 0.48f), CornerRadius(16f, 16f))
                    drawRoundRect(Color.White.copy(alpha = 0.85f), Offset(w * 0.33f, h * 0.50f), Size(w * 0.34f, h * 0.20f), CornerRadius(6f, 6f))
                    drawRoundRect(Color(0xFF141414), Offset(cx - w * 0.05f, h * 0.22f), Size(w * 0.10f, h * 0.15f), CornerRadius(4f, 4f))
                    drawRoundRect(Color(0xFF141414), Offset(cx - w * 0.05f, h * 0.18f), Size(w * 0.22f, h * 0.06f), CornerRadius(4f, 4f))
                }

                ProductArtworkType.ROLL_PACK -> {
                    listOf(cx - w * 0.14f, cx + w * 0.14f).forEach { rx ->
                        drawRoundRect(Color.White, Offset(rx - w * 0.13f, h * 0.24f), Size(w * 0.26f, h * 0.58f), CornerRadius(14f, 14f))
                        drawRoundRect(accentColor.copy(alpha = 0.6f), Offset(rx - w * 0.13f, h * 0.24f), Size(w * 0.26f, h * 0.58f), CornerRadius(14f, 14f), style = Stroke(width = 3f))
                        drawOval(Color(0xFFE5E7EB), Offset(rx - w * 0.13f, h * 0.20f), Size(w * 0.26f, h * 0.10f))
                        drawOval(accentColor.copy(alpha = 0.5f), Offset(rx - w * 0.04f, h * 0.225f), Size(w * 0.08f, h * 0.045f))
                    }
                }

                ProductArtworkType.TRASH_BAGS -> {
                    drawOval(accentColor, Offset(w * 0.18f, h * 0.40f), Size(w * 0.64f, h * 0.30f))
                    drawRoundRect(accentColor, Offset(w * 0.18f, h * 0.38f), Size(w * 0.64f, h * 0.18f), CornerRadius(6f, 6f))
                    drawOval(Color(0xFF374151), Offset(w * 0.40f, h * 0.36f), Size(w * 0.20f, h * 0.30f))
                    drawOval(Color(0xFF9CA3AF), Offset(w * 0.46f, h * 0.44f), Size(w * 0.08f, h * 0.14f))
                    drawRoundRect(Color(0xFFFFD400), Offset(w * 0.22f, h * 0.70f), Size(w * 0.56f, h * 0.06f), CornerRadius(6f, 6f))
                }
            }
        }
    }
}
