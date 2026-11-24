package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.DeliveryDining
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.data.model.OrderRecord
import com.example.ui.theme.QuickKartMetricsTokens
import com.example.ui.theme.QuickKartThemeTokens
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

private data class ConfettiParticle(
    val angleRad: Float,
    val distanceFactor: Float,
    val radiusDp: Float,
    val color: Color
)

@Composable
fun OrderPlacedCelebrationOverlay(
    order: OrderRecord?,
    onFinished: () -> Unit
) {
    val tokens = QuickKartThemeTokens.colors
    val metrics = QuickKartMetricsTokens.metrics

    AnimatedVisibility(
        visible = order != null,
        enter = fadeIn(tween(240)) + scaleIn(
            initialScale = 0.92f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
        ),
        exit = fadeOut(tween(220))
    ) {
        if (order == null) return@AnimatedVisibility

        val badgeScale = remember(order.orderId) { Animatable(0.3f) }
        val checkProgress = remember(order.orderId) { Animatable(0f) }
        val confettiProgress = remember(order.orderId) { Animatable(0f) }
        val scooterProgress = remember(order.orderId) { Animatable(0f) }

        LaunchedEffect(order.orderId) {
            launch {
                badgeScale.animateTo(
                    targetValue = 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    )
                )
            }
            launch {
                delay(120L)
                checkProgress.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(420, easing = FastOutSlowInEasing)
                )
            }
            launch {
                confettiProgress.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(1100, easing = FastOutSlowInEasing)
                )
            }
            launch {
                scooterProgress.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(1750, easing = FastOutSlowInEasing)
                )
            }
            delay(2300L)
            onFinished()
        }

        val rippleTransition = rememberInfiniteTransition(label = "order_ripple")
        val rippleScale by rippleTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.48f,
            animationSpec = infiniteRepeatable(
                animation = tween(950, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "ripple_scale"
        )
        val rippleAlpha by rippleTransition.animateFloat(
            initialValue = 0.45f,
            targetValue = 0f,
            animationSpec = infiniteRepeatable(
                animation = tween(950, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "ripple_alpha"
        )

        val confettiColors = remember(tokens) {
            listOf(
                tokens.brandYellow,
                Color(0xFF4ADE80),
                Color.White,
                Color(0xFFFB923C),
                Color(0xFF38BDF8)
            )
        }
        val particles = remember(confettiColors) {
            List(22) { idx ->
                val angle = (idx * (360f / 22f)) * (Math.PI.toFloat() / 180f)
                ConfettiParticle(
                    angleRad = angle,
                    distanceFactor = 0.62f + ((idx * 17) % 10) / 25f,
                    radiusDp = if (idx % 3 == 0) 6f else 4.2f,
                    color = confettiColors[idx % confettiColors.size]
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(tokens.deliveryGreenDeep)
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(metrics.screenHorizontalPadding + 8.dp)
                .testTag("order_placed_celebration_overlay"),
            contentAlignment = Alignment.Center
        ) {
            // Burst confetti background layer
            Canvas(modifier = Modifier.fillMaxSize()) {
                val cx = size.width / 2f
                val cy = size.height * 0.36f
                val maxSpread = minOf(size.width, size.height) * 0.44f
                val p = confettiProgress.value
                val alpha = (1f - (p * 0.72f)).coerceIn(0f, 1f)

                particles.forEach { particle ->
                    val dist = maxSpread * particle.distanceFactor * p
                    val px = cx + cos(particle.angleRad) * dist
                    val py = cy + sin(particle.angleRad) * dist + (p * p * 36.dp.toPx())
                    drawCircle(
                        color = particle.color.copy(alpha = alpha),
                        radius = particle.radiusDp.dp.toPx() * (1.15f - p * 0.35f),
                        center = Offset(px, py)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .widthIn(max = 520.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                QuickKartLogo(
                    size = metrics.badgeButtonSize,
                    cornerRadius = 12.dp
                )

                // Animated Checkmark + Ripple Badge
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(156.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .scale(rippleScale)
                            .clip(CircleShape)
                            .background(tokens.brandYellow.copy(alpha = rippleAlpha))
                    )
                    Surface(
                        shape = CircleShape,
                        color = tokens.brandYellow,
                        border = BorderStroke(4.dp, Color.White),
                        modifier = Modifier
                            .size(106.dp)
                            .scale(badgeScale.value)
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val checkPath = Path().apply {
                                moveTo(size.width * 0.28f, size.height * 0.52f)
                                lineTo(size.width * 0.44f, size.height * 0.68f)
                                lineTo(size.width * 0.74f, size.height * 0.34f)
                            }
                            val pathMeasure = PathMeasure().apply { setPath(checkPath, false) }
                            val animatedPath = Path()
                            pathMeasure.getSegment(
                                0f,
                                pathMeasure.length * checkProgress.value,
                                animatedPath,
                                true
                            )
                            drawPath(
                                path = animatedPath,
                                color = Color(0xFF141414),
                                style = Stroke(
                                    width = 8.dp.toPx(),
                                    cap = StrokeCap.Round,
                                    join = StrokeJoin.Round
                                )
                            )
                        }
                    }
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        color = Color.White.copy(alpha = 0.18f),
                        shape = RoundedCornerShape(50)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Bolt,
                                contentDescription = null,
                                tint = tokens.brandYellow,
                                modifier = Modifier.size(metrics.iconSmall)
                            )
                            Text(
                                text = "ORDER CONFIRMED • ${order.orderId}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }

                    Text(
                        text = "Arriving in ${order.etaMinutes} minutes!",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "${order.totalItemCount} ${if (order.totalItemCount == 1) "item" else "items"} • ₹${order.totalPaid} • ${order.neighborhoodName}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.9f),
                        textAlign = TextAlign.Center
                    )
                }

                // Animated Scooter Dispatch Track
                Surface(
                    color = Color.Black.copy(alpha = 0.24f),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(36.dp)
                        ) {
                            Canvas(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .align(Alignment.BottomCenter)
                                    .padding(horizontal = 8.dp)
                            ) {
                                drawLine(
                                    color = Color.White.copy(alpha = 0.35f),
                                    start = Offset(0f, size.height / 2f),
                                    end = Offset(size.width, size.height / 2f),
                                    strokeWidth = 3.dp.toPx(),
                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f))
                                )
                            }

                            val travelFraction = scooterProgress.value
                            val bounceY = (sin(travelFraction * Math.PI * 6) * 3f).roundToInt()
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(travelFraction.coerceIn(0.08f, 0.94f))
                                    .align(Alignment.CenterStart)
                            ) {
                                Surface(
                                    color = tokens.brandYellow,
                                    shape = CircleShape,
                                    modifier = Modifier
                                        .size(34.dp)
                                        .align(Alignment.CenterEnd)
                                        .offset { IntOffset(0, bounceY) }
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Rounded.DeliveryDining,
                                            contentDescription = "Dispatching rider",
                                            tint = tokens.onBrandYellow,
                                            modifier = Modifier.size(metrics.iconMedium)
                                        )
                                    }
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Sending order to Dark Store Pod",
                                style = MaterialTheme.typography.labelSmall,
                                color = tokens.brandYellow,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${(scooterProgress.value * 100).roundToInt()}%",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White
                            )
                        }
                    }
                }

                Spacer(Modifier.height(4.dp))

                Button(
                    onClick = onFinished,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = tokens.brandYellow,
                        contentColor = tokens.onBrandYellow
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 50.dp)
                        .testTag("celebration_track_now_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.DeliveryDining,
                        contentDescription = null,
                        modifier = Modifier.size(metrics.iconMedium)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Track Live Order",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}
