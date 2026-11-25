package com.example.merchant.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DeliveryDining
import androidx.compose.material.icons.rounded.HourglassTop
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.LocalOffer
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.Insights
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.SupportAgent
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.merchant.data.CatalogueItem
import com.example.merchant.data.OrderLine
import com.example.merchant.data.OrderStatus
import com.example.merchant.data.PaymentMode
import com.example.merchant.data.RiderStage
import com.example.merchant.data.StoreOrder
import com.example.merchant.ui.MerchantUiState
import com.example.merchant.ui.MerchantViewModel
import com.example.ui.components.AmountRow
import com.example.ui.components.BadgeTone
import com.example.ui.components.BrandBlack
import com.example.ui.components.BrandSwitch
import com.example.ui.components.ChoiceChip
import com.example.ui.components.EmptyState
import com.example.ui.components.PrimaryButton
import com.example.ui.components.QkCard
import com.example.ui.components.QkTextField
import com.example.ui.components.ScreenHeader
import com.example.ui.components.SecondaryButton
import com.example.ui.components.SectionTitle
import com.example.ui.components.StatusBadge
import com.example.ui.components.rupees
import com.example.ui.components.timeAgo
import com.example.ui.components.QuickKartLogo
import com.example.ui.theme.QuickKartMetricsTokens
import com.example.ui.theme.QuickKartThemeTokens
import com.example.ui.theme.zoomScaledControl

fun statusTone(status: OrderStatus): BadgeTone = when (status) {
    OrderStatus.NEW -> BadgeTone.BRAND
    OrderStatus.PREPARING -> BadgeTone.WARNING
    OrderStatus.READY -> BadgeTone.DARK
    OrderStatus.HANDED_OVER -> BadgeTone.NEUTRAL
    OrderStatus.DELIVERED -> BadgeTone.SUCCESS
    OrderStatus.REJECTED, OrderStatus.MISSED, OrderStatus.CANCELLED -> BadgeTone.DANGER
}

// ─────────────────────────────────────────────────────────────────────────────────────────────
// 2. Merchant Dashboard
// ─────────────────────────────────────────────────────────────────────────────────────────────

@Composable
fun DashboardScreen(
    state: MerchantUiState,
    onToggleOpen: (Boolean) -> Unit,
    onOpenAlerts: () -> Unit,
    onOpenOrders: (filter: OrdersFilter) -> Unit,
    onOpenOrder: (String) -> Unit,
    onOpenKyc: () -> Unit,
    onOpenTile: (route: String) -> Unit,
    onSimulateOrder: () -> Unit
) {
    val tokens = QuickKartThemeTokens.colors
    val metrics = QuickKartMetricsTokens.metrics
    var confirmClose by remember { mutableStateOf(false) }
    LazyColumn(
        contentPadding = PaddingValues(horizontal = metrics.screenHorizontalPadding, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxSize().testTag("merchant_dashboard")
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                QuickKartLogo(size = 44.dp, cornerRadius = 12.dp)
                Column(Modifier.weight(1f)) {
                    Text(state.profile.storeName.ifBlank { "Your store" }, style = MaterialTheme.typography.titleLarge, color = tokens.ink, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text("Welcome, ${state.profile.ownerName.ifBlank { "Partner" }}", style = MaterialTheme.typography.bodySmall, color = tokens.muted)
                }
                Box {
                    IconButton(onClick = onOpenAlerts, modifier = Modifier.testTag("open_alerts")) {
                        Icon(Icons.Rounded.Notifications, "Alerts, ${state.unreadAlerts} unread", tint = tokens.ink)
                    }
                    if (state.unreadAlerts > 0) {
                        Box(
                            Modifier.align(Alignment.TopEnd).padding(top = 6.dp, end = 6.dp).size(16.dp).background(tokens.ink, CircleShape),
                            contentAlignment = Alignment.Center
                        ) { Text(if (state.unreadAlerts > 9) "9+" else "${state.unreadAlerts}", color = tokens.background, style = MaterialTheme.typography.labelSmall) }
                    }
                }
            }
        }
        item {
            // Store open / closed master toggle
            QkCard(color = if (state.settings.isOpen) tokens.brandYellowSoft else tokens.surface, borderColor = if (state.settings.isOpen) tokens.brandYellow else tokens.border) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(if (state.settings.isOpen) "Store is OPEN" else "Store is CLOSED", style = MaterialTheme.typography.titleSmall, color = tokens.ink, fontWeight = FontWeight.ExtraBold)
                        Text(
                            when {
                                !state.profile.kycVerified -> "Orders start after verification"
                                state.settings.isOpen && !state.isWithinHours -> "Outside operating hours — not receiving orders"
                                state.settings.isOpen -> "Receiving orders · ${state.settings.serviceRadiusKm.toInt()} km radius"
                                else -> "Customers can't order from you right now"
                            },
                            style = MaterialTheme.typography.bodySmall, color = tokens.muted
                        )
                    }
                    BrandSwitch(
                        checked = state.settings.isOpen,
                        onCheckedChange = { open -> if (!open && state.activeOrders.isNotEmpty()) confirmClose = true else onToggleOpen(open) },
                        modifier = Modifier.testTag("store_open_switch")
                    )
                }
            }
        }
        if (!state.profile.kycVerified) {
            item {
                QkCard(onClick = onOpenKyc, color = tokens.surface) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Icon(Icons.Rounded.HourglassTop, null, tint = tokens.ink)
                        Column(Modifier.weight(1f)) {
                            Text(if (state.profile.kycSubmitted) "Verification in progress" else "Complete store verification", style = MaterialTheme.typography.titleSmall, color = tokens.ink)
                            Text("FSSAI ${state.profile.fssaiStatus.label.lowercase()} · GSTIN ${state.profile.gstinStatus.label.lowercase()}", style = MaterialTheme.typography.bodySmall, color = tokens.muted)
                        }
                        StatusBadge("Review", BadgeTone.BRAND)
                    }
                }
            }
        }
        item {
            // Today's performance — black hero card with yellow figures
            Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = BrandBlack), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Today's Performance", style = MaterialTheme.typography.titleMedium, color = Color.White)
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        PerfStat("Orders", state.todayOrderCount.toString(), Modifier.weight(1f))
                        PerfStat("Revenue", rupees(state.todayRevenue), Modifier.weight(1.3f))
                        PerfStat("Avg prep", if (state.avgPrepMinutes > 0) "${state.avgPrepMinutes}m" else "—", Modifier.weight(1f))
                    }
                }
            }
        }
        item {
            SectionTitle("Active orders") {
                TextButton(onClick = { onOpenOrders(OrdersFilter.ACTIVE) }) { Text("View all", color = tokens.ink) }
            }
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                listOf(
                    Triple("New", OrderStatus.NEW, OrdersFilter.NEW),
                    Triple("Preparing", OrderStatus.PREPARING, OrdersFilter.PREPARING),
                    Triple("Ready", OrderStatus.READY, OrdersFilter.READY)
                ).forEach { (label, status, filter) ->
                    val count = state.orders.count { it.status == status }
                    QkCard(onClick = { onOpenOrders(filter) }, modifier = Modifier.weight(1f), contentPadding = PaddingValues(12.dp),
                        color = if (status == OrderStatus.NEW && count > 0) tokens.brandYellow else tokens.surface,
                        borderColor = if (status == OrderStatus.NEW && count > 0) tokens.brandYellow else tokens.border) {
                        Text(count.toString(), style = MaterialTheme.typography.headlineSmall, color = tokens.ink, fontWeight = FontWeight.ExtraBold)
                        Text(label, style = MaterialTheme.typography.labelMedium, color = if (status == OrderStatus.NEW && count > 0) tokens.onBrandYellow else tokens.muted)
                    }
                }
            }
        }
        val active = state.activeOrders.take(3)
        if (active.isEmpty()) {
            item {
                EmptyState(
                    Icons.AutoMirrored.Rounded.ReceiptLong, "No active orders",
                    if (state.isAcceptingOrders) "New orders will pop up here with a sound." else "Open your store to start receiving orders.",
                    action = {
                        TextButton(onClick = onSimulateOrder, modifier = Modifier.testTag("simulate_order")) {
                            Icon(Icons.Rounded.Bolt, null, tint = tokens.ink); Spacer(Modifier.width(4.dp)); Text("Simulate a new order (demo)", color = tokens.ink)
                        }
                    }
                )
            }
        } else {
            items(active, key = { it.id }) { order -> OrderCard(order, state.nowMillis) { onOpenOrder(order.id) } }
            item {
                TextButton(onClick = onSimulateOrder, modifier = Modifier.testTag("simulate_order")) {
                    Icon(Icons.Rounded.Bolt, null, tint = tokens.muted); Spacer(Modifier.width(4.dp)); Text("Simulate a new order (demo)", color = tokens.muted)
                }
            }
        }
        item { SectionTitle("Manage") }
        item {
            val tiles = listOf(
                Triple("Catalogue", Icons.Rounded.Inventory2, ROUTE_CATALOGUE),
                Triple("Offers", Icons.Rounded.LocalOffer, ROUTE_OFFERS),
                Triple("Settlements", Icons.Rounded.Payments, ROUTE_SETTLEMENTS),
                Triple("Reports", Icons.Rounded.Insights, ROUTE_REPORTS),
                Triple("Alerts & Zone", Icons.Rounded.Notifications, ROUTE_ALERTS),
                Triple("Support", Icons.Rounded.SupportAgent, ROUTE_SUPPORT)
            )
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                tiles.chunked(3).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        row.forEach { (label, icon, route) ->
                            QkCard(onClick = { onOpenTile(route) }, modifier = Modifier.weight(1f).testTag("tile_$route"), contentPadding = PaddingValues(vertical = 14.dp, horizontal = 8.dp)) {
                                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                    Box(Modifier.size(44.dp).background(tokens.brandYellowSoft, CircleShape), contentAlignment = Alignment.Center) {
                                        Icon(icon, null, tint = tokens.ink)
                                    }
                                }
                                Text(label, style = MaterialTheme.typography.labelLarge, color = tokens.ink, modifier = Modifier.fillMaxWidth(), textAlign = androidx.compose.ui.text.style.TextAlign.Center, maxLines = 1)
                            }
                        }
                    }
                }
            }
        }
    }
    if (confirmClose) {
        AlertDialog(
            onDismissRequest = { confirmClose = false },
            title = { Text("Close store?") },
            text = { Text("You have ${state.activeOrders.size} active order(s). They will continue, but no new orders will come in.") },
            confirmButton = { TextButton(onClick = { confirmClose = false; onToggleOpen(false) }) { Text("Close store", color = tokens.alertRed) } },
            dismissButton = { TextButton(onClick = { confirmClose = false }) { Text("Keep open", color = tokens.ink) } }
        )
    }
}

@Composable
private fun PerfStat(label: String, value: String, modifier: Modifier) {
    Column(modifier.background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp)).padding(12.dp)) {
        Text(value, style = MaterialTheme.typography.titleLarge, color = QuickKartThemeTokens.colors.brandYellow, fontWeight = FontWeight.ExtraBold, maxLines = 1)
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.75f))
    }
}

@Composable
fun OrderCard(order: StoreOrder, nowMillis: Long, onClick: () -> Unit) {
    val tokens = QuickKartThemeTokens.colors
    QkCard(onClick = onClick, modifier = Modifier.testTag("order_card_${order.id}"),
        color = if (order.status == OrderStatus.NEW) tokens.brandYellowSoft else tokens.surface,
        borderColor = if (order.status == OrderStatus.NEW) tokens.brandYellow else tokens.border) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Order ${order.shortId}", style = MaterialTheme.typography.titleSmall, color = tokens.ink, modifier = Modifier.weight(1f))
            StatusBadge(order.status.label, statusTone(order.status))
        }
        Text("${order.customerName} · ${order.customerArea} · ${timeAgo(order.placedAtMillis, nowMillis)}", style = MaterialTheme.typography.bodySmall, color = tokens.muted)
        Text(order.lines.joinToString { "${it.quantity}× ${it.name}" }, style = MaterialTheme.typography.bodySmall, color = tokens.ink, maxLines = 2, overflow = TextOverflow.Ellipsis)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(rupees(order.payableTotal), style = MaterialTheme.typography.titleSmall, color = tokens.ink, modifier = Modifier.weight(1f))
            if (order.paymentMode == PaymentMode.COD) StatusBadge("COD", BadgeTone.NEUTRAL)
            if (order.isPartial) { Spacer(Modifier.width(6.dp)); StatusBadge("Partial", BadgeTone.WARNING) }
            if (order.status == OrderStatus.NEW) {
                Spacer(Modifier.width(6.dp))
                val left = ((MerchantViewModel.ACCEPT_WINDOW_MS - (nowMillis - order.placedAtMillis)) / 1000).coerceAtLeast(0)
                StatusBadge("Respond in ${left}s", BadgeTone.DANGER)
            }
            order.rider?.takeIf { order.status == OrderStatus.READY }?.let {
                Spacer(Modifier.width(6.dp))
                StatusBadge(if (it.stage == RiderStage.AT_STORE) "Rider at store" else it.stage.label, if (it.stage == RiderStage.AT_STORE) BadgeTone.SUCCESS else BadgeTone.NEUTRAL)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────────────────────
// Orders tab
// ─────────────────────────────────────────────────────────────────────────────────────────────

enum class OrdersFilter(val label: String) { ACTIVE("Active"), NEW("New"), PREPARING("Preparing"), READY("Ready"), PAST("Past") }

private fun StoreOrder.matches(f: OrdersFilter) = when (f) {
    OrdersFilter.ACTIVE -> isActive
    OrdersFilter.NEW -> status == OrderStatus.NEW
    OrdersFilter.PREPARING -> status == OrderStatus.PREPARING
    OrdersFilter.READY -> status == OrderStatus.READY
    OrdersFilter.PAST -> !isActive
}

@Composable
fun OrdersScreen(state: MerchantUiState, filter: OrdersFilter, onFilter: (OrdersFilter) -> Unit, onOpenOrder: (String) -> Unit, onSimulateOrder: () -> Unit) {
    val tokens = QuickKartThemeTokens.colors
    val metrics = QuickKartMetricsTokens.metrics
    val list = state.orders.filter { it.matches(filter) }.sortedByDescending { it.placedAtMillis }
    Column(Modifier.fillMaxSize()) {
        ScreenHeader("Orders", onBack = null, subtitle = "${state.activeOrders.size} active · ${state.todayOrderCount} today")
        LazyRow(contentPadding = PaddingValues(horizontal = metrics.screenHorizontalPadding), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(OrdersFilter.entries) { f -> ChoiceChip(f.label, f == filter, { onFilter(f) }, count = state.orders.count { it.matches(f) }.takeIf { f != OrdersFilter.PAST }) }
        }
        LazyColumn(
            contentPadding = PaddingValues(metrics.screenHorizontalPadding),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f)
        ) {
            if (list.isEmpty()) {
                item {
                    EmptyState(Icons.AutoMirrored.Rounded.ReceiptLong, "No ${filter.label.lowercase()} orders", "Orders will show up here as customers place them.",
                        action = { TextButton(onClick = onSimulateOrder) { Text("Simulate a new order (demo)", color = tokens.ink) } })
                }
            }
            items(list, key = { it.id }) { order -> OrderCard(order, state.nowMillis) { onOpenOrder(order.id) } }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────────────────────
// 3. Incoming Order Alert (full screen)
// ─────────────────────────────────────────────────────────────────────────────────────────────

private val REJECT_REASONS = listOf("Items out of stock", "Store closing soon", "Too busy right now", "Can't deliver to this area", "Other")

@Composable
fun IncomingOrderDialog(
    order: StoreOrder,
    nowMillis: Long,
    onAccept: (prepMinutes: Int) -> Unit,
    onPartial: (prepMinutes: Int, unavailableIds: Set<String>) -> Unit,
    onReject: (reason: String) -> Unit,
    onLater: () -> Unit
) {
    val tokens = QuickKartThemeTokens.colors
    var prepIndex by rememberSaveable { mutableFloatStateOf(1f) } // 10/15/20/25/30
    val prep = PREP_STEPS[prepIndex.toInt()]
    val unavailable = remember { mutableStateListOf<String>() }
    var showReject by remember { mutableStateOf(false) }
    val secondsLeft = ((MerchantViewModel.ACCEPT_WINDOW_MS - (nowMillis - order.placedAtMillis)) / 1000).coerceAtLeast(0)

    Dialog(onDismissRequest = onLater, properties = DialogProperties(usePlatformDefaultWidth = false, dismissOnClickOutside = false)) {
        Surface(color = tokens.background, modifier = Modifier.fillMaxSize().testTag("incoming_order_dialog")) {
            Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
                // Header strip in brand yellow
                Row(
                    Modifier.fillMaxWidth().background(tokens.brandYellow).padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("NEW ORDER", style = MaterialTheme.typography.labelMedium, color = tokens.onBrandYellow, fontWeight = FontWeight.ExtraBold)
                        Text("Order ${order.shortId}", style = MaterialTheme.typography.headlineSmall, color = tokens.onBrandYellow)
                    }
                    Box(contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            progress = { secondsLeft / 60f },
                            color = BrandBlack,
                            trackColor = BrandBlack.copy(alpha = 0.15f),
                            strokeWidth = 5.dp,
                            modifier = Modifier.size(58.dp)
                        )
                        Text("${secondsLeft}s", style = MaterialTheme.typography.titleSmall, color = tokens.onBrandYellow, fontWeight = FontWeight.ExtraBold)
                    }
                }
                Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    QkCard {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(order.customerName, style = MaterialTheme.typography.titleMedium, color = tokens.ink)
                                Text("${order.customerArea} · ${order.itemCount} items", style = MaterialTheme.typography.bodySmall, color = tokens.muted)
                            }
                            StatusBadge(order.paymentMode.label, if (order.paymentMode == PaymentMode.COD) BadgeTone.NEUTRAL else BadgeTone.SUCCESS)
                        }
                        order.note?.let { Text("Note: $it", style = MaterialTheme.typography.bodySmall, color = tokens.ink) }
                    }
                    QkCard {
                        Text("Items", style = MaterialTheme.typography.titleSmall, color = tokens.ink)
                        order.lines.forEach { line ->
                            val off = line.productId in unavailable
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("${line.quantity}×", style = MaterialTheme.typography.titleSmall, color = tokens.ink, modifier = Modifier.width(34.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(line.name, style = MaterialTheme.typography.bodyMedium, color = if (off) tokens.muted else tokens.ink, maxLines = 2)
                                    Text(if (off) "Marked unavailable" else rupees(line.lineTotal), style = MaterialTheme.typography.labelSmall, color = if (off) tokens.alertRed else tokens.muted)
                                }
                                Text("Unavailable", style = MaterialTheme.typography.labelSmall, color = tokens.muted)
                                Spacer(Modifier.width(6.dp))
                                BrandSwitch(checked = off, onCheckedChange = { if (it) unavailable.add(line.productId) else unavailable.remove(line.productId) })
                            }
                        }
                        HorizontalDivider(color = tokens.border)
                        AmountRow("Order total", rupees(order.payableTotal), bold = true)
                    }
                    QkCard {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Preparation time", style = MaterialTheme.typography.titleSmall, color = tokens.ink, modifier = Modifier.weight(1f))
                            StatusBadge("$prep min", BadgeTone.DARK)
                        }
                        Slider(
                            value = prepIndex,
                            onValueChange = { prepIndex = it },
                            valueRange = 0f..(PREP_STEPS.size - 1).toFloat(),
                            steps = PREP_STEPS.size - 2,
                            colors = SliderDefaults.colors(thumbColor = BrandBlack, activeTrackColor = tokens.brandYellow, inactiveTrackColor = tokens.border, activeTickColor = BrandBlack, inactiveTickColor = tokens.muted),
                            modifier = Modifier.testTag("prep_slider")
                        )
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            PREP_STEPS.forEach { Text("${it}m", style = MaterialTheme.typography.labelSmall, color = tokens.muted) }
                        }
                    }
                    if (unavailable.size == order.lines.size) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Rounded.Warning, null, tint = tokens.alertRed, modifier = Modifier.size(18.dp))
                            Text("All items are unavailable — reject the order instead.", style = MaterialTheme.typography.bodySmall, color = tokens.alertRed)
                        }
                    }
                    TextButton(onClick = onLater, modifier = Modifier.align(Alignment.CenterHorizontally)) { Text("Decide later (timer keeps running)", color = tokens.muted) }
                }
                Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    SecondaryButton("Reject", onClick = { showReject = true }, destructive = true, modifier = Modifier.weight(1f))
                    PrimaryButton(
                        text = if (unavailable.isEmpty()) "Accept" else "Review partial",
                        enabled = unavailable.size < order.lines.size,
                        modifier = Modifier.weight(1.4f),
                        testTag = "accept_order",
                        onClick = { if (unavailable.isEmpty()) onAccept(prep) else onPartial(prep, unavailable.toSet()) }
                    )
                }
            }
        }
    }
    if (showReject) RejectReasonDialog(onDismiss = { showReject = false }, onConfirm = { showReject = false; onReject(it) })
}

val PREP_STEPS = listOf(10, 15, 20, 25, 30)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RejectReasonDialog(onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    val tokens = QuickKartThemeTokens.colors
    var reason by remember { mutableStateOf<String?>(null) }
    var other by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Reject order?") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("The customer gets a full refund. Frequent rejections lower your store ranking.", style = MaterialTheme.typography.bodySmall, color = tokens.muted)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    REJECT_REASONS.forEach { r -> ChoiceChip(r, reason == r, { reason = r }) }
                }
                if (reason == "Other") QkTextField(other, { other = it.take(80) }, "Reason")
            }
        },
        confirmButton = {
            TextButton(
                enabled = reason != null && (reason != "Other" || other.isNotBlank()),
                onClick = { onConfirm(if (reason == "Other") other.trim() else reason!!) }
            ) { Text("Reject order", color = tokens.alertRed) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = tokens.ink) } }
    )
}

// ─────────────────────────────────────────────────────────────────────────────────────────────
// 4. Partial Fulfilment & Refund
// ─────────────────────────────────────────────────────────────────────────────────────────────

@Composable
fun PartialFulfilmentScreen(
    order: StoreOrder,
    catalogue: List<CatalogueItem>,
    initiallyUnavailable: Set<String>,
    prepMinutes: Int,
    onBack: () -> Unit,
    onConfirm: (List<OrderLine>) -> Unit,
    onReject: (String) -> Unit
) {
    val tokens = QuickKartThemeTokens.colors
    // Working copy: fulfilled qty + substitute per line
    val lines = remember(order.id) {
        mutableStateListOf(*order.lines.map { if (it.productId in initiallyUnavailable) it.copy(fulfilledQuantity = 0) else it }.toTypedArray())
    }
    var showReject by remember { mutableStateOf(false) }
    val refund = lines.sumOf { it.refundAmount }
    val itemTotal = order.itemTotal
    val newTotal = itemTotal - refund + order.deliveryFee
    val nothingLeft = lines.all { it.fulfilledQuantity == 0 && it.substituteName == null }

    Column(Modifier.fillMaxSize().background(tokens.background)) {
        ScreenHeader("Partial Fulfilment", onBack = onBack, subtitle = "Order ${order.shortId} · ${order.customerName}")
        LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item {
                Text("Mark what you can't supply. Reduce quantity, or propose a similar item. The difference is refunded to the customer's ${order.paymentMode.label} automatically.",
                    style = MaterialTheme.typography.bodySmall, color = tokens.muted)
            }
            items(lines.size) { index ->
                val line = lines[index]
                val outOfStock = line.fulfilledQuantity == 0
                QkCard(color = if (line.isShort) tokens.brandYellowSoft else tokens.surface, borderColor = if (line.isShort) tokens.brandYellow else tokens.border) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = outOfStock,
                            onCheckedChange = { checked -> lines[index] = line.copy(fulfilledQuantity = if (checked) 0 else line.quantity, substituteName = if (checked) line.substituteName else null) },
                            colors = CheckboxDefaults.colors(checkedColor = tokens.brandYellow, checkmarkColor = tokens.onBrandYellow),
                            modifier = Modifier.zoomScaledControl()
                        )
                        Column(Modifier.weight(1f)) {
                            Text(line.name, style = MaterialTheme.typography.titleSmall, color = tokens.ink)
                            Text("Ordered ${line.quantity} × ${rupees(line.unitPrice)}" + if (outOfStock) " · Out of stock" else "", style = MaterialTheme.typography.labelSmall, color = if (outOfStock) tokens.alertRed else tokens.muted)
                        }
                        if (line.refundAmount > 0) StatusBadge("Refund ${rupees(line.refundAmount)}", BadgeTone.DANGER)
                    }
                    if (line.quantity > 1) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Can supply", style = MaterialTheme.typography.bodySmall, color = tokens.muted, modifier = Modifier.weight(1f))
                            QtyStepper(line.fulfilledQuantity, 0, line.quantity) { q -> lines[index] = line.copy(fulfilledQuantity = q, substituteName = if (q == line.quantity) null else line.substituteName) }
                        }
                    }
                    if (line.isShort) {
                        val options = substitutesFor(line, catalogue)
                        var menu by remember { mutableStateOf(false) }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.SwapHoriz, null, tint = tokens.ink, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Column(Modifier.weight(1f)) {
                                Text("Propose substitute", style = MaterialTheme.typography.bodyMedium, color = tokens.ink)
                                Text(line.substituteName ?: if (options.isEmpty()) "No similar item in stock" else "Customer pays the same price", style = MaterialTheme.typography.labelSmall, color = tokens.muted)
                            }
                            Box {
                                BrandSwitch(
                                    checked = line.substituteName != null,
                                    enabled = options.isNotEmpty(),
                                    onCheckedChange = { on -> if (on) menu = true else lines[index] = line.copy(substituteName = null) }
                                )
                                DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                                    options.forEach { opt ->
                                        DropdownMenuItem(text = { Text("${opt.name} (${opt.unit})") }, onClick = { menu = false; lines[index] = line.copy(substituteName = "${opt.name} (${opt.unit})") })
                                    }
                                }
                            }
                        }
                    }
                }
            }
            item {
                QkCard {
                    Text("Updated bill", style = MaterialTheme.typography.titleSmall, color = tokens.ink)
                    AmountRow("Item total", rupees(itemTotal))
                    AmountRow("Refund to customer", "− ${rupees(refund)}", valueColor = tokens.alertRed)
                    AmountRow("Delivery fee", if (order.deliveryFee == 0) "Free" else rupees(order.deliveryFee))
                    HorizontalDivider(color = tokens.border)
                    AmountRow("New total", rupees(newTotal), bold = true)
                    if (order.paymentMode == PaymentMode.COD) Text("COD: the rider collects ${rupees(newTotal)} instead of ${rupees(order.payableTotal)}.", style = MaterialTheme.typography.labelSmall, color = tokens.muted)
                }
            }
            if (nothingLeft) {
                item { Text("Nothing left to deliver. Reject the order so the customer gets a full refund.", style = MaterialTheme.typography.bodySmall, color = tokens.alertRed) }
            }
        }
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            PrimaryButton(
                "Confirm partial order · $prepMinutes min",
                onClick = { onConfirm(lines.toList()) },
                enabled = !nothingLeft && lines.any { it.isShort },
                testTag = "confirm_partial"
            )
            if (nothingLeft) SecondaryButton("Reject order", onClick = { showReject = true }, destructive = true, modifier = Modifier.fillMaxWidth())
        }
    }
    if (showReject) RejectReasonDialog(onDismiss = { showReject = false }, onConfirm = { showReject = false; onReject(it) })
}

private fun substitutesFor(line: OrderLine, catalogue: List<CatalogueItem>): List<CatalogueItem> {
    val original = catalogue.firstOrNull { it.id == line.productId }
    return catalogue.filter { it.id != line.productId && it.active && it.stock > 0 && (original == null || it.category == original.category) }.take(5)
}

@Composable
fun QtyStepper(value: Int, min: Int, max: Int, onChange: (Int) -> Unit) {
    val tokens = QuickKartThemeTokens.colors
    Surface(color = tokens.brandYellow, shape = RoundedCornerShape(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { if (value > min) onChange(value - 1) }, enabled = value > min, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Rounded.Remove, "Decrease", tint = tokens.onBrandYellow)
            }
            Text("$value", style = MaterialTheme.typography.titleSmall, color = tokens.onBrandYellow, modifier = Modifier.width(24.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            IconButton(onClick = { if (value < max) onChange(value + 1) }, enabled = value < max, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Rounded.Add, "Increase", tint = tokens.onBrandYellow)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────────────────────
// 5. Order detail: Packing & Rider Handover
// ─────────────────────────────────────────────────────────────────────────────────────────────

@Composable
fun OrderDetailScreen(
    order: StoreOrder,
    nowMillis: Long,
    onBack: () -> Unit,
    onRespond: () -> Unit,
    onTogglePacked: (String) -> Unit,
    onBillAttached: (Boolean) -> Unit,
    onBagSealed: (Boolean) -> Unit,
    onMarkReady: () -> Unit,
    onHandover: () -> Unit,
    onCustomerCancel: () -> Unit,
    onRaiseTicket: () -> Unit
) {
    val tokens = QuickKartThemeTokens.colors
    val context = LocalContext.current
    val packingMode = order.status == OrderStatus.PREPARING || order.status == OrderStatus.READY
    val allPacked = order.lines.all { it.productId in order.packedLineIds } && order.billAttached && order.bagSealed
    val rider = order.rider
    val readyBy = order.acceptedAtMillis?.let { it + order.prepMinutes * 60_000L }

    Column(Modifier.fillMaxSize().background(tokens.background)) {
        ScreenHeader("Order ${order.shortId}", onBack = onBack, subtitle = "${order.customerName} · ${order.customerArea}")
        LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                QkCard(color = if (order.status == OrderStatus.READY) tokens.brandYellowSoft else tokens.surface, borderColor = if (order.status == OrderStatus.READY) tokens.brandYellow else tokens.border) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("Order status", style = MaterialTheme.typography.labelMedium, color = tokens.muted)
                            Text(order.status.label, style = MaterialTheme.typography.titleLarge, color = tokens.ink)
                        }
                        StatusBadge(order.paymentMode.label, if (order.paymentMode == PaymentMode.COD) BadgeTone.NEUTRAL else BadgeTone.SUCCESS)
                    }
                    if (order.status == OrderStatus.PREPARING && readyBy != null) {
                        val minsLeft = ((readyBy - nowMillis) / 60_000).toInt()
                        Text(if (minsLeft >= 0) "Pack within $minsLeft min (prep time ${order.prepMinutes} min)" else "Running ${-minsLeft} min late — mark ready as soon as possible",
                            style = MaterialTheme.typography.bodySmall, color = if (minsLeft >= 0) tokens.muted else tokens.alertRed)
                    }
                    order.rejectReason?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = tokens.alertRed) }
                    if (order.paymentMode == PaymentMode.COD && order.isActive) Text("Rider collects ${rupees(order.payableTotal)} cash from the customer.", style = MaterialTheme.typography.labelSmall, color = tokens.muted)
                }
            }
            if (rider != null && order.status in setOf(OrderStatus.READY, OrderStatus.HANDED_OVER, OrderStatus.PREPARING)) {
                item {
                    QkCard {
                        Text("Assigned rider", style = MaterialTheme.typography.titleSmall, color = tokens.ink)
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Box(Modifier.size(44.dp).background(tokens.brandYellowSoft, CircleShape), contentAlignment = Alignment.Center) { Icon(Icons.Rounded.DeliveryDining, null, tint = tokens.ink) }
                            Column(Modifier.weight(1f)) {
                                Text(rider.name, style = MaterialTheme.typography.titleSmall, color = tokens.ink)
                                Text(
                                    when (rider.stage) {
                                        RiderStage.ASSIGNING -> "Looking for the nearest rider…"
                                        RiderStage.ON_THE_WAY -> "${rider.vehicle} · arriving in ${rider.etaMinutes} min"
                                        RiderStage.AT_STORE -> "${rider.vehicle} · at your store"
                                    },
                                    style = MaterialTheme.typography.bodySmall, color = tokens.muted
                                )
                            }
                            if (rider.phone.isNotBlank()) {
                                IconButton(
                                    onClick = { runCatching { context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${rider.phone}"))) } },
                                    modifier = Modifier.background(tokens.brandYellow, CircleShape)
                                ) { Icon(Icons.Rounded.Call, "Call rider", tint = tokens.onBrandYellow) }
                            }
                        }
                    }
                }
            }
            item {
                QkCard {
                    Text(if (packingMode) "Items to pack" else "Items", style = MaterialTheme.typography.titleSmall, color = tokens.ink)
                    order.lines.forEach { line ->
                        val packed = line.productId in order.packedLineIds
                        Row(
                            Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).then(if (packingMode) Modifier.clickable { onTogglePacked(line.productId) } else Modifier),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (packingMode) {
                                Checkbox(checked = packed, onCheckedChange = { onTogglePacked(line.productId) },
                                    colors = CheckboxDefaults.colors(checkedColor = tokens.brandYellow, checkmarkColor = tokens.onBrandYellow), modifier = Modifier.zoomScaledControl())
                            }
                            Column(Modifier.weight(1f)) {
                                Text("${line.fulfilledQuantity}× ${line.substituteName ?: line.name}", style = MaterialTheme.typography.bodyMedium, color = if (line.fulfilledQuantity == 0) tokens.muted else tokens.ink)
                                when {
                                    line.substituteName != null -> Text("Substitute for ${line.name}", style = MaterialTheme.typography.labelSmall, color = tokens.muted)
                                    line.isShort -> Text("Short by ${line.quantity - line.fulfilledQuantity} · refunded ${rupees(line.refundAmount)}", style = MaterialTheme.typography.labelSmall, color = tokens.alertRed)
                                }
                            }
                            Text(rupees(line.fulfilledTotal), style = MaterialTheme.typography.bodyMedium, color = tokens.ink)
                        }
                    }
                    if (packingMode) {
                        HorizontalDivider(color = tokens.border)
                        CheckRow("Bill / invoice attached", order.billAttached, onBillAttached)
                        CheckRow("Bag sealed with QuickKart tape", order.bagSealed, onBagSealed)
                    }
                    HorizontalDivider(color = tokens.border)
                    if (order.refundTotal > 0) AmountRow("Refunded", "− ${rupees(order.refundTotal)}", valueColor = tokens.alertRed)
                    AmountRow("Total", rupees(order.payableTotal), bold = true)
                    order.customerRating?.let { AmountRow("Customer rating", "★".repeat(it)) }
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = onRaiseTicket) { Icon(Icons.Rounded.SupportAgent, null, tint = tokens.ink); Spacer(Modifier.width(4.dp)); Text("Report an issue", color = tokens.ink) }
                    if (order.status == OrderStatus.PREPARING || order.status == OrderStatus.NEW) {
                        TextButton(onClick = onCustomerCancel) { Text("Customer cancels (demo)", color = tokens.muted) }
                    }
                }
            }
        }
        Column(Modifier.padding(16.dp)) {
            when (order.status) {
                OrderStatus.NEW -> PrimaryButton("Respond to order", onClick = onRespond, icon = Icons.Rounded.Bolt)
                OrderStatus.PREPARING -> {
                    PrimaryButton("Mark ready for pickup", onClick = onMarkReady, enabled = allPacked, icon = Icons.Rounded.CheckCircle, testTag = "mark_ready")
                    if (!allPacked) Text("Tick every item, the bill and the sealed bag first.", style = MaterialTheme.typography.labelSmall, color = tokens.muted, modifier = Modifier.padding(top = 6.dp))
                }
                OrderStatus.READY -> {
                    val atStore = rider?.stage == RiderStage.AT_STORE
                    PrimaryButton("Confirm handover", onClick = onHandover, enabled = atStore && allPacked, icon = Icons.Rounded.DeliveryDining, testTag = "confirm_handover")
                    if (!atStore) Text("Enabled when the rider reaches your store.", style = MaterialTheme.typography.labelSmall, color = tokens.muted, modifier = Modifier.padding(top = 6.dp))
                }
                else -> {}
            }
        }
    }
}

@Composable
private fun CheckRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    val tokens = QuickKartThemeTokens.colors
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).clickable { onChange(!checked) }, verticalAlignment = Alignment.CenterVertically) {
        Checkbox(checked = checked, onCheckedChange = onChange, colors = CheckboxDefaults.colors(checkedColor = tokens.brandYellow, checkmarkColor = tokens.onBrandYellow), modifier = Modifier.zoomScaledControl())
        Text(label, style = MaterialTheme.typography.bodyMedium, color = tokens.ink)
    }
}
