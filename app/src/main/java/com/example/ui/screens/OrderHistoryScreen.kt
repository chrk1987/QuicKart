package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.PictureAsPdf
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.SupportAgent
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.data.model.OrderHistoryTimeFilter
import com.example.data.model.OrderRecord
import com.example.data.model.OrderStage
import com.example.data.model.RestockSuggestion
import com.example.service.PdfReceiptService
import com.example.ui.QuickKartUiState
import com.example.ui.components.OrderFeedbackBottomSheet
import com.example.ui.components.SmartRestockReminderBanner
import com.example.ui.components.SpendingTrendCard
import com.example.ui.theme.AmberGold
import com.example.ui.theme.QuickKartMetricsTokens
import com.example.ui.theme.QuickKartThemeTokens
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun OrderHistoryScreen(
    uiState: QuickKartUiState,
    onAddRestockItem: (RestockSuggestion) -> Unit,
    onHistorySearchChange: (String) -> Unit,
    onSelectTimeFilter: (OrderHistoryTimeFilter) -> Unit,
    onQuickReorder: (OrderRecord) -> Unit,
    onReorderCheckout: (OrderRecord) -> Unit,
    onSubmitRating: (String, Int, List<String>, String) -> Unit,
    onOpenSupportForOrder: (OrderRecord) -> Unit,
    onShowMessage: (String) -> Unit
) {
    val tokens = QuickKartThemeTokens.colors
    val metrics = QuickKartMetricsTokens.metrics
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val receiptService = remember(context) { PdfReceiptService(context) }
    var feedbackTargetOrder by remember { mutableStateOf<OrderRecord?>(null) }

    val deliveredOrdersCount = uiState.orders.count { it.stage == OrderStage.DELIVERED }
    val totalSavings = uiState.orders.sumOf { it.mrpSavings + it.promoDiscount }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(tokens.background),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .widthIn(max = 760.dp)
                .fillMaxSize()
                .testTag("order_history_screen"),
            contentPadding = PaddingValues(metrics.screenHorizontalPadding),
            verticalArrangement = Arrangement.spacedBy(metrics.itemSpacing)
        ) {
            // Order History Header + Summary Pill + Search + Time Filters
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                color = tokens.brandYellow,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.size(44.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Rounded.ReceiptLong,
                                        contentDescription = "Order History",
                                        tint = tokens.onBrandYellow,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = "Order History",
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = tokens.ink,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = "$deliveredOrdersCount delivered orders • ₹$totalSavings saved",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = tokens.muted
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = uiState.historySearchQuery,
                        onValueChange = onHistorySearchChange,
                        placeholder = { Text("Filter by product or category (e.g. Milk, Bakery)") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Rounded.Search,
                                contentDescription = "Search orders"
                            )
                        },
                        trailingIcon = {
                            if (uiState.historySearchQuery.isNotEmpty()) {
                                IconButton(onClick = { onHistorySearchChange("") }) {
                                    Icon(Icons.Rounded.Close, contentDescription = "Clear")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("history_search_input")
                    )

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(OrderHistoryTimeFilter.entries) { filter ->
                            val selected = filter == uiState.historyTimeFilter
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = if (selected) tokens.brandYellow else tokens.surface,
                                border = BorderStroke(
                                    1.dp,
                                    if (selected) tokens.brandYellow else tokens.border
                                ),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .clickable { onSelectTimeFilter(filter) }
                                    .testTag("history_filter_${filter.name}")
                            ) {
                                Text(
                                    text = filter.label,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (selected) tokens.onBrandYellow else tokens.ink,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Filtered Historical Order Cards
            if (uiState.filteredHistoricalOrders.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = tokens.surface),
                        border = BorderStroke(1.dp, tokens.border),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(metrics.cardPadding + 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.ShoppingBag,
                                contentDescription = null,
                                tint = tokens.muted,
                                modifier = Modifier.size(metrics.iconHero)
                            )
                            Text(
                                text = "No matching past orders",
                                style = MaterialTheme.typography.titleMedium,
                                color = tokens.ink,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "Try clearing your search or switching to All Time.",
                                style = MaterialTheme.typography.bodySmall,
                                color = tokens.muted,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(uiState.filteredHistoricalOrders, key = { it.orderId }) { order ->
                    HistoricalOrderCard(
                        order = order,
                        onQuickReorder = { onQuickReorder(order) },
                        onReorderCheckout = { onReorderCheckout(order) },
                        onRateOrder = { feedbackTargetOrder = order },
                        onNeedHelp = { onOpenSupportForOrder(order) },
                        onDownloadReceipt = {
                            coroutineScope.launch {
                                receiptService.generateAndSaveReceipt(order)
                                    .onSuccess { result ->
                                        val viewIntent = PdfReceiptService.openPdfIntent(result.fileUri)
                                        val chooser = PdfReceiptService.sharePdfIntent(result.fileUri, order.orderId)
                                        val launched = runCatching {
                                            context.startActivity(viewIntent)
                                        }.isSuccess
                                        if (!launched) {
                                            context.startActivity(chooser)
                                        }
                                        val msg = if (result.savedToDownloads) {
                                            "Receipt ${result.fileName} saved to Downloads/QuickKart!"
                                        } else {
                                            "Receipt ${result.fileName} saved!"
                                        }
                                        onShowMessage(msg)
                                    }
                                    .onFailure { err ->
                                        onShowMessage("Could not generate PDF: ${err.localizedMessage ?: "Error"}")
                                    }
                            }
                        }
                    )
                }
            }

            // Smart Restock Reminder Banner
            item {
                SmartRestockReminderBanner(
                    suggestions = uiState.restockSuggestions,
                    onAddSuggestion = onAddRestockItem
                )
            }

            // Spending Trend Card
            item {
                SpendingTrendCard(orders = uiState.orders)
            }
        }
    }

    feedbackTargetOrder?.let { target ->
        OrderFeedbackBottomSheet(
            order = target,
            onDismiss = { feedbackTargetOrder = null },
            onSubmit = { stars, tags, comment ->
                onSubmitRating(target.orderId, stars, tags, comment)
                onShowMessage("Feedback saved for ${target.orderId}!")
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun HistoricalOrderCard(
    order: OrderRecord,
    onQuickReorder: () -> Unit,
    onReorderCheckout: () -> Unit,
    onRateOrder: () -> Unit,
    onNeedHelp: () -> Unit,
    onDownloadReceipt: () -> Unit
) {
    val tokens = QuickKartThemeTokens.colors
    val metrics = QuickKartMetricsTokens.metrics
    val dateFormatted = SimpleDateFormat("dd MMM yyyy • hh:mm a", Locale.getDefault())
        .format(Date(order.createdAtMillis))

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = tokens.surface),
        border = BorderStroke(1.dp, tokens.border),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("history_order_card_${order.orderId}")
    ) {
        Column(
            modifier = Modifier.padding(metrics.cardPadding),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = order.orderId,
                            style = MaterialTheme.typography.titleSmall,
                            color = tokens.ink
                        )
                        Surface(
                            color = tokens.deliveryGreenSoft,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "Delivered",
                                style = MaterialTheme.typography.labelSmall,
                                color = tokens.deliveryGreenDeep,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        if (order.userRating != null) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Star,
                                    contentDescription = null,
                                    tint = AmberGold,
                                    modifier = Modifier.size(metrics.iconSmall)
                                )
                                Text(
                                    text = "${order.userRating}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = tokens.ink
                                )
                            }
                        }
                    }
                    Text(
                        text = "$dateFormatted • ${order.neighborhoodName} • ${order.deliveryWindowLabel}",
                        style = MaterialTheme.typography.bodySmall,
                        color = tokens.muted
                    )
                }

                Text(
                    text = "₹${order.totalPaid}",
                    style = MaterialTheme.typography.titleMedium,
                    color = tokens.ink
                )
            }

            Text(
                text = order.items.joinToString(", ") { "${it.quantity}x ${it.productName}" },
                style = MaterialTheme.typography.bodyMedium,
                color = tokens.ink
            )

            // Order Feedback Tags / Review Strip + Rate Action
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                order.praiseTags.forEach { tag ->
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
                Surface(
                    color = tokens.surfaceElevated,
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(1.dp, tokens.border),
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .clickable(onClick = onRateOrder)
                        .testTag("rate_order_${order.orderId}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Star,
                            contentDescription = null,
                            tint = AmberGold,
                            modifier = Modifier.size(metrics.iconSmall)
                        )
                        Text(
                            text = if (order.userRating != null) "Edit Feedback" else "Rate Order",
                            style = MaterialTheme.typography.labelSmall,
                            color = tokens.ink,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                Surface(
                    color = tokens.surfaceElevated,
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(1.dp, tokens.border),
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .clickable(onClick = onNeedHelp)
                        .testTag("order_help_${order.orderId}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.SupportAgent,
                            contentDescription = "Get help for order ${order.orderId}",
                            tint = tokens.ink,
                            modifier = Modifier.size(metrics.iconSmall)
                        )
                        Text(
                            text = "Help",
                            style = MaterialTheme.typography.labelSmall,
                            color = tokens.ink,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            if (order.feedbackComment.isNotBlank()) {
                Text(
                    text = "\"${order.feedbackComment}\"",
                    style = MaterialTheme.typography.bodySmall,
                    color = tokens.muted
                )
            }

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                maxItemsInEachRow = if (metrics.isNarrowPhone) 2 else 3
            ) {
                OutlinedButton(
                    onClick = onQuickReorder,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = tokens.ink),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                    modifier = Modifier
                        .weight(1f)
                        .defaultMinSize(minHeight = 42.dp)
                        .testTag("quick_reorder_${order.orderId}")
                ) {
                    Icon(
                        Icons.Rounded.Add,
                        contentDescription = null,
                        modifier = Modifier.size(metrics.iconSmall)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text("Quick Add", style = MaterialTheme.typography.labelSmall)
                }

                Button(
                    onClick = onReorderCheckout,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = tokens.brandYellow,
                        contentColor = tokens.onBrandYellow
                    ),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                    modifier = Modifier
                        .weight(1f)
                        .defaultMinSize(minHeight = 42.dp)
                        .testTag("reorder_checkout_${order.orderId}")
                ) {
                    Icon(
                        Icons.Rounded.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(metrics.iconSmall)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text("Reorder", style = MaterialTheme.typography.labelSmall)
                }

                OutlinedButton(
                    onClick = onDownloadReceipt,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = tokens.ink),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                    modifier = Modifier
                        .weight(1f)
                        .defaultMinSize(minHeight = 42.dp)
                        .testTag("download_receipt_${order.orderId}")
                ) {
                    Icon(
                        Icons.Rounded.PictureAsPdf,
                        contentDescription = null,
                        modifier = Modifier.size(metrics.iconSmall)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text("PDF", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}
