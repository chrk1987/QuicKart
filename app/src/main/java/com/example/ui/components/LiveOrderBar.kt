package com.example.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.DeliveryDining
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.OrderRecord
import com.example.data.model.OrderStage
import com.example.ui.theme.QuickKartThemeTokens
import com.example.util.etaMinutesLabel

/**
 * Floating live-order bar shown above the bottom tabs while an order is active
 * (replaces the old Track tab that appeared/disappeared). Brand black pill with a
 * yellow rider badge; tap opens Track.
 */
@Composable
fun LiveOrderBar(order: OrderRecord, onOpenTracking: () -> Unit, modifier: Modifier = Modifier) {
    val tokens = QuickKartThemeTokens.colors
    val headline = when (order.stage) {
        OrderStage.CONFIRMED -> "Order confirmed"
        OrderStage.PACKING -> "Packing your order"
        OrderStage.OUT_FOR_DELIVERY -> "Arriving in ${etaMinutesLabel(order.remainingSeconds)}"
        OrderStage.DELIVERED -> "Delivered"
    }
    val subline = when (order.stage) {
        OrderStage.OUT_FOR_DELIVERY -> "${order.riderName} is on the way"
        OrderStage.DELIVERED -> "Tap to rate your order"
        else -> "Arriving in ${etaMinutesLabel(order.remainingSeconds)} • ${order.orderId}"
    }
    val pulse = rememberInfiniteTransition(label = "live_order_pulse")
    val dotAlpha by pulse.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse),
        label = "live_order_dot"
    )

    Surface(
        color = Color(0xFF141414), // brand black, like the profile header
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 6.dp,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onOpenTracking)
            .semantics { contentDescription = "$headline. Open live tracking" }
            .testTag("live_order_bar")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box {
                Surface(color = tokens.brandYellow, shape = CircleShape, modifier = Modifier.size(40.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Rounded.DeliveryDining,
                            contentDescription = null,
                            tint = tokens.onBrandYellow,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
                // Live status dot (green = live delivery status).
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 2.dp, y = (-2).dp)
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF141414))
                        .padding(2.dp)
                        .clip(CircleShape)
                        .alpha(dotAlpha)
                        .background(tokens.deliveryGreen)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    headline,
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    subline,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.72f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Track", style = MaterialTheme.typography.labelLarge, color = tokens.brandYellow, fontWeight = FontWeight.Bold)
                Icon(
                    Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                    contentDescription = null,
                    tint = tokens.brandYellow
                )
            }
        }
    }
}
