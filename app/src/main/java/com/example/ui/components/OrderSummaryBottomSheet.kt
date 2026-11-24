package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.SupportAgent
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.OrderLineItem
import com.example.data.model.OrderRecord
import com.example.data.model.OrderStage
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.QuickKartMetricsTokens
import com.example.ui.theme.QuickKartThemeTokens
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun OrderSummaryBottomSheet(
    order: OrderRecord,
    onDismiss: () -> Unit,
    onDownloadReceipt: (() -> Unit)? = null,
    onRateOrder: (() -> Unit)? = null,
    onNeedHelp: (() -> Unit)? = null
) {
    val tokens = QuickKartThemeTokens.colors
    val metrics = QuickKartMetricsTokens.metrics
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scrollState = rememberScrollState()

    val minutes = order.remainingSeconds / 60
    val seconds = order.remainingSeconds % 60
    val formattedCountdown = String.format(Locale.US, "%02d:%02d", minutes, seconds)
    val etaMinsHeadline = ((order.remainingSeconds + 59) / 60).coerceAtLeast(1)

    val arrivalClockLabel = remember(order.remainingSeconds / 30, order.stage) {
        if (order.stage == OrderStage.DELIVERED) {
            "Arrived at door"
        } else {
            val etaMillis = System.currentTimeMillis() + order.remainingSeconds * 1000L
            val formatter = SimpleDateFormat("h:mm a", Locale.getDefault())
            "Est. by ${formatter.format(Date(etaMillis))}"
        }
    }

    val stageProgress by animateFloatAsState(
        targetValue = when (order.stage) {
            OrderStage.CONFIRMED -> 0.18f
            OrderStage.PACKING -> 0.45f
            OrderStage.OUT_FOR_DELIVERY -> 0.78f
            OrderStage.DELIVERED -> 1f
        },
        animationSpec = tween(550, easing = FastOutSlowInEasing),
        label = "sheet_stage_progress"
    )

    val pulseTransition = rememberInfiniteTransition(label = "sheet_live_pulse")
    val pulseAlpha by pulseTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(750),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sheet_dot_alpha"
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = tokens.surface,
        contentColor = tokens.ink
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .verticalScroll(scrollState)
                .padding(horizontal = metrics.screenHorizontalPadding + 2.dp)
                .padding(bottom = 24.dp)
                .testTag("order_summary_bottom_sheet"),
            verticalArrangement = Arrangement.spacedBy(metrics.itemSpacing)
        ) {
            // Sheet Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                QuickKartLogo(
                    size = metrics.badgeButtonSize,
                    cornerRadius = 10.dp
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Live Order Summary",
                        style = MaterialTheme.typography.titleLarge,
                        color = tokens.ink,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${order.orderId} • ${order.totalItemCount} ${if (order.totalItemCount == 1) "item" else "items"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = tokens.muted
                    )
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("order_summary_sheet_close_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Close order summary",
                        tint = tokens.ink,
                        modifier = Modifier.size(metrics.iconMedium)
                    )
                }
            }

            // Real-Time Estimated Delivery Time Card
            Surface(
                color = tokens.deliveryGreenDeep,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(metrics.cardPadding),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                if (order.stage != OrderStage.DELIVERED) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(tokens.brandYellow.copy(alpha = pulseAlpha))
                                    )
                                }
                                Text(
                                    text = order.stage.title.uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = tokens.brandYellow,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                            Text(
                                text = when (order.stage) {
                                    OrderStage.DELIVERED -> "Delivered safely"
                                    OrderStage.OUT_FOR_DELIVERY -> "Arriving in $etaMinsHeadline min"
                                    else -> "Estimated in $etaMinsHeadline min"
                                },
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Schedule,
                                    contentDescription = null,
                                    tint = Color.White.copy(alpha = 0.88f),
                                    modifier = Modifier.size(metrics.iconSmall)
                                )
                                Text(
                                    text = "$arrivalClockLabel • ${order.stage.subtitle}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.88f)
                                )
                            }
                        }

                        Surface(
                            color = Color.Black.copy(alpha = 0.25f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = if (order.stage == OrderStage.DELIVERED) "DONE" else "ETA",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = tokens.brandYellow,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (order.stage == OrderStage.DELIVERED) "00:00" else formattedCountdown,
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = ((if (metrics.isNarrowPhone) 18f else 20f) * metrics.zoomScale).sp,
                                    color = Color.White,
                                    modifier = Modifier.testTag("order_summary_sheet_eta")
                                )
                            }
                        }
                    }

                    // Live stage progress fill
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(50))
                            .background(Color.White.copy(alpha = 0.22f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(stageProgress)
                                .height(6.dp)
                                .clip(RoundedCornerShape(50))
                                .background(tokens.brandYellow)
                        )
                    }
                }
            }

            // Delivery Destination & Instructions Strip
            Surface(
                color = tokens.surfaceElevated,
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, tokens.border),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Rounded.LocationOn,
                        contentDescription = null,
                        tint = tokens.deliveryGreenDeep,
                        modifier = Modifier.size(metrics.iconMedium)
                    )
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Text(
                            text = "Delivering to ${order.deliveryAddressLabel}",
                            style = MaterialTheme.typography.labelLarge,
                            color = tokens.ink,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = order.deliveryAddressFull,
                            style = MaterialTheme.typography.bodySmall,
                            color = tokens.muted
                        )
                        Text(
                            text = "Window: ${order.deliveryWindowLabel}",
                            style = MaterialTheme.typography.labelSmall,
                            color = tokens.deliveryGreenDeep,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (order.deliveryInstructions.isNotEmpty() || order.riderNote.isNotBlank()) {
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.padding(top = 4.dp)
                            ) {
                                order.deliveryInstructions.forEach { tag ->
                                    Surface(
                                        color = tokens.brandYellowSoft,
                                        shape = RoundedCornerShape(50)
                                    ) {
                                        Text(
                                            text = tag,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = tokens.ink,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                                if (order.riderNote.isNotBlank()) {
                                    Text(
                                        text = "\"${order.riderNote}\"",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = tokens.ink
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Items List Card
            Surface(
                color = tokens.surfaceElevated,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, tokens.border),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(metrics.cardPadding),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ShoppingBag,
                            contentDescription = null,
                            tint = tokens.ink,
                            modifier = Modifier.size(metrics.iconSmall)
                        )
                        Text(
                            text = "Items (${order.totalItemCount})",
                            style = MaterialTheme.typography.titleSmall,
                            color = tokens.ink,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    HorizontalDivider(color = tokens.border.copy(alpha = 0.7f))

                    order.items.forEachIndexed { idx, lineItem ->
                        SheetOrderLineItemRow(item = lineItem)
                        if (idx < order.items.lastIndex) {
                            HorizontalDivider(color = tokens.border.copy(alpha = 0.45f))
                        }
                    }
                }
            }

            // Subtotal & Bill Details Card
            Surface(
                color = tokens.surfaceElevated,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, tokens.border),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(metrics.cardPadding),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ReceiptLong,
                            contentDescription = null,
                            tint = tokens.ink,
                            modifier = Modifier.size(metrics.iconSmall)
                        )
                        Text(
                            text = "Bill Summary",
                            style = MaterialTheme.typography.titleSmall,
                            color = tokens.ink,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    HorizontalDivider(color = tokens.border.copy(alpha = 0.7f))

                    BillSummaryRow(
                        label = "Items Subtotal",
                        value = "₹${order.itemTotal}",
                        valueTestTag = "order_summary_sheet_subtotal"
                    )
                    if (order.mrpSavings > 0) {
                        BillSummaryRow(
                            label = "MRP Discount Savings",
                            value = "-₹${order.mrpSavings}",
                            valueColor = tokens.deliveryGreenDeep
                        )
                    }
                    BillSummaryRow(
                        label = "Delivery Fee",
                        value = if (order.deliveryFee == 0) "FREE" else "₹${order.deliveryFee}",
                        valueColor = if (order.deliveryFee == 0) tokens.deliveryGreenDeep else tokens.ink
                    )
                    BillSummaryRow(
                        label = "Handling Charge",
                        value = "₹${order.handlingCharge}"
                    )
                    if (order.promoDiscount > 0) {
                        BillSummaryRow(
                            label = "Promo Savings",
                            value = "-₹${order.promoDiscount}",
                            valueColor = tokens.deliveryGreenDeep
                        )
                    }
                    if (order.tipAmount > 0) {
                        BillSummaryRow(
                            label = "Rider Tip",
                            value = "₹${order.tipAmount}"
                        )
                    }

                    HorizontalDivider(color = tokens.border)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Grand Total",
                                style = MaterialTheme.typography.titleMedium,
                                color = tokens.ink,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Paid via ${order.paymentMethod.label}",
                                style = MaterialTheme.typography.bodySmall,
                                color = tokens.muted
                            )
                        }
                        Text(
                            text = "₹${order.totalPaid}",
                            style = MaterialTheme.typography.titleLarge,
                            color = tokens.ink,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }

            if (order.stage == OrderStage.DELIVERED && onRateOrder != null) {
                OutlinedButton(
                    onClick = onRateOrder,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, tokens.brandYellow),
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 48.dp)
                        .testTag("sheet_rate_order_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Star,
                        contentDescription = null,
                        tint = Color(0xFFF59E0B),
                        modifier = Modifier.size(metrics.iconMedium)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = if (order.userRating != null) {
                            "Your Rating: ${order.userRating} ★ • Update Feedback"
                        } else {
                            "Rate & Review Delivery"
                        },
                        style = MaterialTheme.typography.labelLarge,
                        color = tokens.ink,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (order.stage == OrderStage.DELIVERED && onDownloadReceipt != null) {
                OutlinedButton(
                    onClick = onDownloadReceipt,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, tokens.deliveryGreenDeep),
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 48.dp)
                        .testTag("sheet_download_receipt_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Download,
                        contentDescription = null,
                        tint = tokens.deliveryGreenDeep,
                        modifier = Modifier.size(metrics.iconMedium)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "Download PDF Receipt",
                        style = MaterialTheme.typography.labelLarge,
                        color = tokens.deliveryGreenDeep,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (onNeedHelp != null) {
                OutlinedButton(
                    onClick = onNeedHelp,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, tokens.border),
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 48.dp)
                        .testTag("sheet_help_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.SupportAgent,
                        contentDescription = null,
                        tint = tokens.ink,
                        modifier = Modifier.size(metrics.iconMedium)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "Need Help / Report Issue",
                        style = MaterialTheme.typography.labelLarge,
                        color = tokens.ink,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = tokens.brandYellow,
                    contentColor = tokens.onBrandYellow
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 48.dp)
            ) {
                Text(
                    text = "Back to Live Map",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun LiveOrderSummarySheetTriggerBar(
    order: OrderRecord,
    onOpenSheet: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tokens = QuickKartThemeTokens.colors
    val metrics = QuickKartMetricsTokens.metrics
    val minutes = order.remainingSeconds / 60
    val seconds = order.remainingSeconds % 60
    val etaLabel = if (order.stage == OrderStage.DELIVERED) {
        "Delivered"
    } else {
        String.format(Locale.US, "ETA %02d:%02d", minutes, seconds)
    }

    Surface(
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        color = tokens.surface,
        border = BorderStroke(1.dp, tokens.border),
        modifier = modifier
            .fillMaxWidth()
            .shadow(10.dp, RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            .clickable(onClick = onOpenSheet)
            .testTag("order_summary_sheet_trigger")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = metrics.screenHorizontalPadding, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Subtle bottom-sheet grabber handle
            Box(
                modifier = Modifier
                    .width(36.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(50))
                    .background(tokens.border)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 44.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    color = tokens.brandYellowSoft,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.ReceiptLong,
                            contentDescription = "Open order summary bottom sheet",
                            tint = tokens.ink,
                            modifier = Modifier.size(metrics.iconMedium)
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${order.totalItemCount} ${if (order.totalItemCount == 1) "item" else "items"} • Subtotal ₹${order.itemTotal}",
                        style = MaterialTheme.typography.titleSmall,
                        color = tokens.ink,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "$etaLabel • Tap for items & bill breakdown",
                        style = MaterialTheme.typography.bodySmall,
                        color = tokens.deliveryGreenDeep,
                        fontWeight = FontWeight.Medium
                    )
                }

                Surface(
                    color = tokens.deliveryGreenDeep,
                    shape = RoundedCornerShape(50)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Text(
                            text = "Summary",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(
                            imageVector = Icons.Rounded.KeyboardArrowUp,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(metrics.iconSmall)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SheetOrderLineItemRow(item: OrderLineItem) {
    val tokens = QuickKartThemeTokens.colors
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            color = tokens.brandYellowSoft,
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, tokens.brandYellow)
        ) {
            Text(
                text = "${item.quantity}×",
                style = MaterialTheme.typography.labelMedium,
                color = tokens.ink,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.productName,
                style = MaterialTheme.typography.bodyMedium,
                color = tokens.ink,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "${item.variantLabel} • ₹${item.unitPriceInr} each",
                style = MaterialTheme.typography.bodySmall,
                color = tokens.muted
            )
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "₹${item.lineTotal}",
                style = MaterialTheme.typography.bodyMedium,
                color = tokens.ink,
                fontWeight = FontWeight.Bold
            )
            val mrpTotal = item.unitMrpInr * item.quantity
            if (mrpTotal > item.lineTotal) {
                Text(
                    text = "₹$mrpTotal",
                    style = MaterialTheme.typography.labelSmall,
                    color = tokens.muted,
                    textDecoration = TextDecoration.LineThrough
                )
            }
        }
    }
}

@Composable
private fun BillSummaryRow(
    label: String,
    value: String,
    valueColor: Color = QuickKartThemeTokens.colors.ink,
    valueTestTag: String? = null
) {
    val tokens = QuickKartThemeTokens.colors
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = tokens.muted
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = valueColor,
            fontWeight = FontWeight.SemiBold,
            modifier = if (valueTestTag != null) Modifier.testTag(valueTestTag) else Modifier
        )
    }
}
