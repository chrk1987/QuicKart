package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.TrendingDown
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.data.model.OrderRecord
import com.example.data.model.OrderStage
import com.example.data.model.RestockSuggestion
import com.example.ui.theme.DeviceSizeClass
import com.example.ui.theme.QuickKartMetricsTokens
import com.example.ui.theme.QuickKartThemeTokens
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun SmartRestockReminderBanner(
    suggestions: List<RestockSuggestion>,
    onAddSuggestion: (RestockSuggestion) -> Unit
) {
    if (suggestions.isEmpty()) return
    val tokens = QuickKartThemeTokens.colors
    val metrics = QuickKartMetricsTokens.metrics

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = tokens.brandYellowSoft),
        border = BorderStroke(1.dp, tokens.brandYellow),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("smart_restock_banner")
    ) {
        Column(
            modifier = Modifier.padding(metrics.cardPadding),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    color = tokens.brandYellow,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.size(metrics.badgeButtonSize)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.AutoAwesome,
                            contentDescription = null,
                            tint = tokens.onBrandYellow,
                            modifier = Modifier.size(metrics.iconMedium)
                        )
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Smart Restock Reminder",
                        style = MaterialTheme.typography.titleMedium,
                        color = tokens.ink
                    )
                    Text(
                        text = "Running low based on your order cadence",
                        style = MaterialTheme.typography.bodySmall,
                        color = tokens.muted
                    )
                }
            }

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(end = 4.dp)
            ) {
                items(suggestions, key = { it.product.id }) { suggestion ->
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = tokens.surface,
                        border = BorderStroke(1.dp, tokens.border),
                        modifier = Modifier.widthIn(min = 236.dp, max = 268.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            ProductArtwork(
                                artworkType = suggestion.product.artworkType,
                                accentColor = suggestion.product.accentColor,
                                modifier = Modifier.size(metrics.avatarSize)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = suggestion.product.name,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = tokens.ink,
                                    maxLines = 2
                                )
                                Text(
                                    text = "Bought ${suggestion.orderCount}x • every ~${suggestion.avgIntervalDays}d",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = tokens.muted
                                )
                                Text(
                                    text = "Last: ${suggestion.daysSinceLastOrder}d ago • ₹${suggestion.product.defaultVariant.priceInr}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = tokens.deliveryGreenDeep
                                )
                            }
                            Surface(
                                color = tokens.deliveryGreenDeep,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .size(metrics.badgeButtonSize)
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { onAddSuggestion(suggestion) }
                                    .testTag("restock_add_${suggestion.product.id}")
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Rounded.Add,
                                        contentDescription = "Add ${suggestion.product.name} to cart",
                                        tint = Color.White,
                                        modifier = Modifier.size(metrics.iconMedium)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SpendingTrendCard(orders: List<OrderRecord>) {
    val tokens = QuickKartThemeTokens.colors
    val metrics = QuickKartMetricsTokens.metrics
    val recentOrders = orders
        .filter { it.stage == OrderStage.DELIVERED }
        .sortedBy { it.createdAtMillis }
        .takeLast(6)

    val totals = recentOrders.map { it.totalPaid }
    val avgSpend = if (totals.isNotEmpty()) totals.average().roundToInt() else 0
    val totalSpend = totals.sum()
    val pctChange = if (totals.size >= 4) {
        val mid = totals.size / 2
        val olderAvg = totals.take(mid).average()
        val recentAvg = totals.drop(mid).average()
        if (olderAvg > 0) (((recentAvg - olderAvg) / olderAvg) * 100.0).roundToInt() else 0
    } else {
        0
    }
    val dateFormat = SimpleDateFormat("dd MMM", Locale.getDefault())

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = tokens.surface),
        border = BorderStroke(1.dp, tokens.border),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("spending_trend_card")
    ) {
        Column(
            modifier = Modifier.padding(metrics.cardPadding),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Spending Trend",
                        style = MaterialTheme.typography.titleMedium,
                        color = tokens.ink
                    )
                    Text(
                        text = "Last ${recentOrders.size} delivered orders • Total ₹$totalSpend",
                        style = MaterialTheme.typography.bodySmall,
                        color = tokens.muted
                    )
                }

                Surface(
                    color = if (pctChange <= 0) tokens.deliveryGreenSoft else tokens.brandYellowSoft,
                    shape = RoundedCornerShape(50)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = if (pctChange >= 0) Icons.AutoMirrored.Rounded.TrendingUp else Icons.AutoMirrored.Rounded.TrendingDown,
                            contentDescription = null,
                            tint = if (pctChange <= 0) tokens.deliveryGreenDeep else tokens.ink,
                            modifier = Modifier.size(metrics.iconSmall)
                        )
                        Text(
                            text = "${if (pctChange >= 0) "+" else ""}$pctChange%",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (pctChange <= 0) tokens.deliveryGreenDeep else tokens.ink,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "₹$avgSpend",
                    style = MaterialTheme.typography.headlineMedium,
                    color = tokens.ink
                )
                Text(
                    text = "avg. order",
                    style = MaterialTheme.typography.bodySmall,
                    color = tokens.muted,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            if (recentOrders.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(tokens.surfaceElevated),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Complete your first order to see spending trends",
                        style = MaterialTheme.typography.bodySmall,
                        color = tokens.muted
                    )
                }
            } else {
                val barColor = tokens.brandYellow
                val barLatestColor = tokens.deliveryGreen
                val gridLineColor = tokens.border

                val chartHeight = when (metrics.sizeClass) {
                    DeviceSizeClass.SMALL_COMPACT -> 96.dp
                    DeviceSizeClass.COMPACT -> 110.dp
                    DeviceSizeClass.MEDIUM -> 122.dp
                    DeviceSizeClass.EXPANDED -> 132.dp
                }

                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(chartHeight)
                ) {
                    val w = size.width
                    val h = size.height
                    val maxVal = (totals.maxOrNull() ?: 1).coerceAtLeast(1).toFloat()
                    val count = totals.size
                    val slotWidth = w / count
                    val barWidth = (slotWidth * 0.48f).coerceAtMost(42f)

                    // Horizontal reference lines
                    drawLine(gridLineColor, Offset(0f, h * 0.25f), Offset(w, h * 0.25f), strokeWidth = 1.5f)
                    drawLine(gridLineColor, Offset(0f, h * 0.60f), Offset(w, h * 0.60f), strokeWidth = 1.5f)
                    drawLine(gridLineColor, Offset(0f, h - 2f), Offset(w, h - 2f), strokeWidth = 2f)

                    val trendPath = Path()
                    totals.forEachIndexed { idx, value ->
                        val ratio = (value / maxVal).coerceIn(0.12f, 1f)
                        val barHeight = (h - 14f) * ratio
                        val centerX = slotWidth * idx + slotWidth / 2f
                        val topY = h - barHeight - 2f

                        drawRoundRect(
                            color = if (idx == totals.lastIndex) barLatestColor else barColor,
                            topLeft = Offset(centerX - barWidth / 2f, topY),
                            size = Size(barWidth, barHeight),
                            cornerRadius = CornerRadius(10f, 10f)
                        )

                        if (idx == 0) {
                            trendPath.moveTo(centerX, topY)
                        } else {
                            trendPath.lineTo(centerX, topY)
                        }
                    }

                    drawPath(
                        path = trendPath,
                        color = barLatestColor.copy(alpha = 0.65f),
                        style = Stroke(width = 3.5f, cap = StrokeCap.Round)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    recentOrders.forEach { ord ->
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "₹${ord.totalPaid}",
                                style = MaterialTheme.typography.labelSmall,
                                color = tokens.ink,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = dateFormat.format(Date(ord.createdAtMillis)),
                                style = MaterialTheme.typography.labelSmall,
                                color = tokens.muted,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}
