package com.example.merchant.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.DeliveryDining
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Insights
import androidx.compose.material.icons.rounded.Inventory
import androidx.compose.material.icons.rounded.LocalOffer
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Storefront
import androidx.compose.material.icons.rounded.SupportAgent
import androidx.compose.material.icons.rounded.TextFields
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.merchant.BuildConfig
import com.example.merchant.data.AlertType
import com.example.merchant.data.MerchantAlert
import com.example.merchant.data.MerchantProfile
import com.example.merchant.data.OrderStatus
import com.example.merchant.data.PLATFORM_COMMISSION_PERCENT
import com.example.merchant.data.PayoutStatus
import com.example.merchant.data.ReportRange
import com.example.merchant.data.MerchantSeed
import com.example.merchant.data.StoreSettings
import com.example.merchant.data.SupportTicket
import com.example.merchant.data.ThemeMode
import com.example.merchant.data.TicketStatus
import com.example.merchant.ui.MerchantUiState
import com.example.ui.components.AmountRow
import com.example.ui.components.TrendChart
import com.example.ui.components.BadgeTone
import com.example.ui.components.BrandBlack
import com.example.ui.components.BrandSwitch
import com.example.ui.components.ChoiceChip
import com.example.ui.components.EmptyState
import com.example.ui.components.PrimaryButton
import com.example.ui.components.QkCard
import com.example.ui.components.QkTextField
import com.example.ui.components.ScreenHeader
import com.example.ui.components.SectionTitle
import com.example.ui.components.SegmentedChoice
import com.example.ui.components.SettingRow
import com.example.ui.components.StatusBadge
import com.example.ui.components.minuteOfDayLabel
import com.example.ui.components.rupees
import com.example.ui.components.timeAgo
import com.example.ui.theme.QuickKartThemeTokens

// ─────────────────────────────────────────────────────────────────────────────────────────────
// 9. Settlements & Ledger
// ─────────────────────────────────────────────────────────────────────────────────────────────

@Composable
fun SettlementsScreen(state: MerchantUiState, onBack: () -> Unit, onRaiseTicket: () -> Unit) {
    val tokens = QuickKartThemeTokens.colors
    val nextPayout = state.payouts.firstOrNull { it.status != PayoutStatus.PAID }
    LazyColumn(contentPadding = PaddingValues(bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { ScreenHeader("Settlements & Ledger", onBack = onBack, subtitle = "Weekly payouts · $PLATFORM_COMMISSION_PERCENT% platform commission") }
        item {
            Row(Modifier.padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                QkCard(modifier = Modifier.weight(1f), contentPadding = PaddingValues(14.dp)) {
                    Text("Gross sales", style = MaterialTheme.typography.labelMedium, color = tokens.muted)
                    Text(rupees(state.cycleGross), style = MaterialTheme.typography.titleLarge, color = tokens.ink, fontWeight = FontWeight.ExtraBold)
                }
                QkCard(modifier = Modifier.weight(1f), contentPadding = PaddingValues(14.dp)) {
                    Text("Commission", style = MaterialTheme.typography.labelMedium, color = tokens.muted)
                    Text("− ${rupees(state.cycleCommission)}", style = MaterialTheme.typography.titleLarge, color = tokens.ink, fontWeight = FontWeight.ExtraBold)
                }
            }
        }
        item {
            Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = BrandBlack), modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth()) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Net payable this cycle", style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.75f))
                    Text(rupees(state.cycleNet), style = MaterialTheme.typography.headlineMedium, color = tokens.brandYellow, fontWeight = FontWeight.ExtraBold)
                    Text(
                        nextPayout?.let { "Next payout ${formatDate(it.dateMillis)} to ${state.profile.bankName.ifBlank { "your bank" }} ${state.profile.accountMasked}" } ?: "No pending payouts",
                        style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.8f)
                    )
                    Text("Schedule: every Monday for the previous Mon–Sun (T+1).", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.6f))
                }
            }
        }
        if (state.profile.accountNumber.isBlank()) {
            item {
                QkCard(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Rounded.Warning, null, tint = tokens.alertRed)
                        Text("Add a bank account in Account → Bank details to receive payouts.", style = MaterialTheme.typography.bodySmall, color = tokens.ink)
                    }
                }
            }
        }
        item {
            QkCard(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text("Net sales · last 4 weeks", style = MaterialTheme.typography.titleSmall, color = tokens.ink)
                val series = state.payouts.reversed()
                TrendChart(series.map { it.amount }, series.map { it.periodLabel.substringBefore(" –").substringBefore(" ") })
            }
        }
        item { SectionTitle("Payout history", Modifier.padding(horizontal = 16.dp)) }
        items(state.payouts, key = { it.id }) { p ->
            QkCard(modifier = Modifier.padding(horizontal = 16.dp), contentPadding = PaddingValues(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(p.periodLabel, style = MaterialTheme.typography.titleSmall, color = tokens.ink)
                        Text(p.utr?.let { "UTR $it · ${formatDate(p.dateMillis)}" } ?: "Expected ${formatDate(p.dateMillis)}", style = MaterialTheme.typography.labelSmall, color = tokens.muted)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(rupees(p.amount), style = MaterialTheme.typography.titleSmall, color = tokens.ink)
                        StatusBadge(p.status.label, if (p.status == PayoutStatus.PAID) BadgeTone.SUCCESS else BadgeTone.WARNING)
                    }
                }
                AmountRow("Gross ${rupees(p.grossSales)}", "Commission − ${rupees(p.commission)}")
            }
        }
        item {
            TextButton(onClick = onRaiseTicket, modifier = Modifier.padding(horizontal = 8.dp)) {
                Icon(Icons.Rounded.SupportAgent, null, tint = tokens.ink); Spacer(Modifier.width(4.dp)); Text("Payout looks wrong? Raise a ticket", color = tokens.ink)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────────────────────
// 10. Sales Reports & Analytics
// ─────────────────────────────────────────────────────────────────────────────────────────────

@Composable
fun ReportsScreen(state: MerchantUiState, onBack: () -> Unit) {
    val tokens = QuickKartThemeTokens.colors
    var range by rememberSaveable { mutableStateOf(ReportRange.WEEK) }
    val series = MerchantSeed.revenueSeries(range)
    val revenue = series.sumOf { it.second }
    val orders = series.sumOf { it.third }
    val closed = state.orders.filter { it.status in setOf(OrderStatus.DELIVERED, OrderStatus.REJECTED, OrderStatus.MISSED, OrderStatus.CANCELLED) }
    val cancelRate = if (closed.isEmpty()) 0.0 else closed.count { it.status != OrderStatus.DELIVERED } * 100.0 / closed.size
    val ratings = state.orders.mapNotNull { it.customerRating }
    val avgRating = if (ratings.isEmpty()) 0.0 else ratings.average()
    val topProducts = state.orders.filter { it.status != OrderStatus.REJECTED && it.status != OrderStatus.MISSED }
        .flatMap { it.lines }.groupBy { it.name }.mapValues { (_, l) -> l.sumOf { it.fulfilledQuantity } }
        .entries.sortedByDescending { it.value }.take(5)

    LazyColumn(contentPadding = PaddingValues(bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { ScreenHeader("Sales Reports & Analytics", onBack = onBack) }
        item { SegmentedChoice(ReportRange.entries, range, { it.label }, { range = it }, Modifier.padding(horizontal = 16.dp)) }
        item {
            QkCard(modifier = Modifier.padding(horizontal = 16.dp)) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Column(Modifier.weight(1f)) {
                        Text("Revenue", style = MaterialTheme.typography.labelMedium, color = tokens.muted)
                        Text(rupees(revenue), style = MaterialTheme.typography.headlineSmall, color = tokens.ink, fontWeight = FontWeight.ExtraBold)
                    }
                    Text("$orders orders · AOV ${rupees(if (orders == 0) 0 else revenue / orders)}", style = MaterialTheme.typography.labelMedium, color = tokens.muted)
                }
                TrendChart(series.map { it.second }, series.map { it.first }, bars = range == ReportRange.DAY)
            }
        }
        item {
            Row(Modifier.padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                QkCard(modifier = Modifier.weight(1f), contentPadding = PaddingValues(14.dp)) {
                    Text("Cancellation rate", style = MaterialTheme.typography.labelMedium, color = tokens.muted)
                    Text(String.format(java.util.Locale.US, "%.1f%%", cancelRate), style = MaterialTheme.typography.titleLarge, color = if (cancelRate > 10) tokens.alertRed else tokens.ink, fontWeight = FontWeight.ExtraBold)
                    Text("Includes rejected & missed", style = MaterialTheme.typography.labelSmall, color = tokens.muted)
                }
                QkCard(modifier = Modifier.weight(1f), contentPadding = PaddingValues(14.dp), color = tokens.brandYellowSoft, borderColor = tokens.brandYellow) {
                    Text("Customer rating", style = MaterialTheme.typography.labelMedium, color = tokens.muted)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(String.format(java.util.Locale.US, "%.1f", avgRating), style = MaterialTheme.typography.titleLarge, color = tokens.ink, fontWeight = FontWeight.ExtraBold)
                        Icon(Icons.Rounded.Star, null, tint = Color(0xFFF5A623), modifier = Modifier.size(20.dp))
                    }
                    Text("${ratings.size} ratings", style = MaterialTheme.typography.labelSmall, color = tokens.muted)
                }
            }
        }
        item {
            QkCard(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text("Top-selling products", style = MaterialTheme.typography.titleSmall, color = tokens.ink)
                if (topProducts.isEmpty()) Text("No sales yet.", style = MaterialTheme.typography.bodySmall, color = tokens.muted)
                topProducts.forEachIndexed { i, (name, qty) ->
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(Modifier.size(28.dp).background(if (i == 0) tokens.brandYellow else tokens.surfaceElevated, CircleShape), contentAlignment = Alignment.Center) {
                            Text("${i + 1}", style = MaterialTheme.typography.labelLarge, color = tokens.ink)
                        }
                        Text(name, style = MaterialTheme.typography.bodyMedium, color = tokens.ink, modifier = Modifier.weight(1f), maxLines = 1)
                        Text("$qty units", style = MaterialTheme.typography.labelMedium, color = tokens.muted)
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────────────────────
// 11. Notifications & Zone Config
// ─────────────────────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertsZoneScreen(
    state: MerchantUiState,
    onBack: () -> Unit,
    onOpenAlert: (MerchantAlert) -> Unit,
    onMarkAllRead: () -> Unit,
    onUpdateSettings: ((StoreSettings) -> StoreSettings) -> Unit
) {
    val tokens = QuickKartThemeTokens.colors
    var radius by remember(state.settings.serviceRadiusKm) { mutableFloatStateOf(state.settings.serviceRadiusKm) }
    var editingOpen by remember { mutableStateOf<Boolean?>(null) } // true = opening time, false = closing time

    LazyColumn(contentPadding = PaddingValues(bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            ScreenHeader("Notifications & Zone", onBack = onBack, actions = {
                if (state.unreadAlerts > 0) TextButton(onClick = onMarkAllRead) { Text("Mark all read", color = tokens.ink) }
            })
        }
        item {
            QkCard(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text("Store serviceability", style = MaterialTheme.typography.titleSmall, color = tokens.ink)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.LocationOn, null, tint = tokens.ink, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Delivery radius", style = MaterialTheme.typography.bodyMedium, color = tokens.ink, modifier = Modifier.weight(1f))
                    StatusBadge(String.format(java.util.Locale.US, "%.1f km", radius), BadgeTone.DARK)
                }
                Slider(
                    value = radius, onValueChange = { radius = (it * 2).toInt() / 2f }, onValueChangeFinished = { onUpdateSettings { s -> s.copy(serviceRadiusKm = radius) } },
                    valueRange = 1f..8f,
                    colors = SliderDefaults.colors(thumbColor = BrandBlack, activeTrackColor = tokens.brandYellow, inactiveTrackColor = tokens.border),
                    modifier = Modifier.testTag("radius_slider")
                )
                Text(
                    if (radius > 5f) "Longer distances may increase delivery time beyond 15 min." else "About ${(radius * 2.5).toInt() + 5} min average delivery time.",
                    style = MaterialTheme.typography.labelSmall, color = if (radius > 5f) tokens.alertRed else tokens.muted
                )
                HorizontalDivider(color = tokens.border)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Schedule, null, tint = tokens.ink, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Operating hours", style = MaterialTheme.typography.bodyMedium, color = tokens.ink, modifier = Modifier.weight(1f))
                    StatusBadge(if (state.isWithinHours) "Open now" else "Closed now", if (state.isWithinHours) BadgeTone.SUCCESS else BadgeTone.NEUTRAL)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    TimeBox("Opens", minuteOfDayLabel(state.settings.openMinuteOfDay), Modifier.weight(1f)) { editingOpen = true }
                    TimeBox("Closes", minuteOfDayLabel(state.settings.closeMinuteOfDay), Modifier.weight(1f)) { editingOpen = false }
                }
                if (state.settings.closeMinuteOfDay < state.settings.openMinuteOfDay) {
                    Text("Overnight hours: closes the next day.", style = MaterialTheme.typography.labelSmall, color = tokens.muted)
                }
            }
        }
        item { SectionTitle("Alerts", Modifier.padding(horizontal = 16.dp)) }
        if (state.alerts.isEmpty()) item { EmptyState(Icons.Rounded.Notifications, "No alerts", "Rider arrivals, low stock and payouts show up here.") }
        items(state.alerts, key = { it.id }) { alert ->
            QkCard(onClick = { onOpenAlert(alert) }, modifier = Modifier.padding(horizontal = 16.dp), contentPadding = PaddingValues(14.dp),
                color = if (alert.read) tokens.surface else tokens.brandYellowSoft, borderColor = if (alert.read) tokens.border else tokens.brandYellow) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.size(40.dp).background(if (alert.read) tokens.surfaceElevated else tokens.brandYellow, CircleShape), contentAlignment = Alignment.Center) {
                        Icon(
                            when (alert.type) {
                                AlertType.NEW_ORDER -> Icons.Rounded.Bolt
                                AlertType.RIDER_ARRIVED -> Icons.Rounded.DeliveryDining
                                AlertType.LOW_STOCK -> Icons.Rounded.Inventory
                                AlertType.PAYOUT -> Icons.Rounded.Payments
                                AlertType.ORDER_CANCELLED -> Icons.Rounded.Cancel
                                AlertType.SYSTEM -> Icons.Rounded.NotificationsActive
                            }, null, tint = tokens.ink
                        )
                    }
                    Column(Modifier.weight(1f)) {
                        Text(alert.title, style = MaterialTheme.typography.titleSmall, color = tokens.ink)
                        Text(alert.body, style = MaterialTheme.typography.bodySmall, color = tokens.muted)
                    }
                    Text(timeAgo(alert.atMillis, state.nowMillis), style = MaterialTheme.typography.labelSmall, color = tokens.muted)
                }
            }
        }
    }

    editingOpen?.let { isOpen ->
        val initial = if (isOpen) state.settings.openMinuteOfDay else state.settings.closeMinuteOfDay
        val picker = rememberTimePickerState(initialHour = initial / 60, initialMinute = initial % 60, is24Hour = false)
        AlertDialog(
            onDismissRequest = { editingOpen = null },
            title = { Text(if (isOpen) "Opening time" else "Closing time") },
            text = {
                TimePicker(
                    state = picker,
                    colors = TimePickerDefaults.colors(
                        clockDialSelectedContentColor = tokens.onBrandYellow, selectorColor = tokens.brandYellow,
                        timeSelectorSelectedContainerColor = tokens.brandYellow, timeSelectorSelectedContentColor = tokens.onBrandYellow,
                        periodSelectorSelectedContainerColor = tokens.brandYellow, periodSelectorSelectedContentColor = tokens.onBrandYellow
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val minute = picker.hour * 60 + picker.minute
                    val other = if (isOpen) state.settings.closeMinuteOfDay else state.settings.openMinuteOfDay
                    if (minute != other) onUpdateSettings { if (isOpen) it.copy(openMinuteOfDay = minute) else it.copy(closeMinuteOfDay = minute) }
                    editingOpen = null
                }) { Text("Save", color = tokens.ink) }
            },
            dismissButton = { TextButton(onClick = { editingOpen = null }) { Text("Cancel", color = tokens.muted) } }
        )
    }
}

@Composable
private fun TimeBox(label: String, value: String, modifier: Modifier, onClick: () -> Unit) {
    val tokens = QuickKartThemeTokens.colors
    QkCard(onClick = onClick, modifier = modifier, contentPadding = PaddingValues(12.dp), color = tokens.surfaceElevated) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = tokens.muted)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(value, style = MaterialTheme.typography.titleMedium, color = tokens.ink, modifier = Modifier.weight(1f))
            Icon(Icons.Rounded.Edit, "Edit $label", tint = tokens.ink, modifier = Modifier.size(16.dp))
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────────────────────
// 12. Store Settings & Support (Account tab)
// ─────────────────────────────────────────────────────────────────────────────────────────────

@Composable
fun AccountScreen(
    state: MerchantUiState,
    onEditStore: () -> Unit,
    onUpdateProfile: ((MerchantProfile) -> MerchantProfile) -> Unit,
    onUpdateSettings: ((StoreSettings) -> StoreSettings) -> Unit,
    onOpen: (route: String) -> Unit,
    onOpenSupport: () -> Unit,
    onSignOut: () -> Unit
) {
    val tokens = QuickKartThemeTokens.colors
    var editBank by remember { mutableStateOf(false) }
    var confirmSignOut by remember { mutableStateOf(false) }
    val p = state.profile

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Store profile card (black, like the customer profile card)
        Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = BrandBlack), modifier = Modifier.fillMaxWidth()) {
            Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(Modifier.size(52.dp).background(tokens.brandYellow, CircleShape), contentAlignment = Alignment.Center) {
                    Icon(Icons.Rounded.Storefront, null, tint = tokens.onBrandYellow)
                }
                Column(Modifier.weight(1f)) {
                    Text(p.storeName.ifBlank { "Your store" }, style = MaterialTheme.typography.titleMedium, color = Color.White)
                    Text("${p.ownerName.ifBlank { "Owner" }} · +91 ${p.phone}", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.75f))
                    Spacer(Modifier.height(4.dp))
                    StatusBadge(if (p.kycVerified) "KYC verified" else if (p.kycSubmitted) "KYC under review" else "KYC pending", if (p.kycVerified) BadgeTone.SUCCESS else BadgeTone.BRAND)
                }
                Surface(color = tokens.brandYellow, shape = CircleShape, modifier = Modifier.size(36.dp)) {
                    androidx.compose.material3.IconButton(onClick = onEditStore) { Icon(Icons.Rounded.Edit, "Edit store details", tint = tokens.onBrandYellow, modifier = Modifier.size(18.dp)) }
                }
            }
        }

        QkCard {
            Text("Store details", style = MaterialTheme.typography.titleSmall, color = tokens.ink)
            SettingRow(Icons.Rounded.LocationOn, "Address", p.address.ifBlank { "Not added" }, onClick = onEditStore)
            SettingRow(Icons.Rounded.Schedule, "Operating hours", "${minuteOfDayLabel(state.settings.openMinuteOfDay)} – ${minuteOfDayLabel(state.settings.closeMinuteOfDay)} · ${state.settings.serviceRadiusKm} km radius", onClick = { onOpen(ROUTE_ALERTS) })
            SettingRow(Icons.Rounded.Verified, "Licences", "FSSAI ${p.fssaiStatus.label.lowercase()} · GSTIN ${p.gstinStatus.label.lowercase()}", onClick = onEditStore)
            SettingRow(Icons.Rounded.AccountBalance, "Bank details", if (p.accountNumber.isBlank()) "Add an account to receive payouts" else "${p.bankName} ${p.accountMasked} · ${p.ifsc}", onClick = { editBank = true })
        }

        QkCard {
            Text("Business", style = MaterialTheme.typography.titleSmall, color = tokens.ink)
            SettingRow(Icons.Rounded.Payments, "Settlements & ledger", "Net payable ${rupees(state.cycleNet)}", onClick = { onOpen(ROUTE_SETTLEMENTS) })
            SettingRow(Icons.Rounded.Insights, "Sales reports", "Revenue, cancellations, ratings", onClick = { onOpen(ROUTE_REPORTS) })
            SettingRow(Icons.Rounded.LocalOffer, "Offers & promotions", "${state.offers.count { it.active && !it.isExpired(state.nowMillis) }} live", onClick = { onOpen(ROUTE_OFFERS) })
            SettingRow(Icons.Rounded.Notifications, "Notifications & zone", if (state.unreadAlerts > 0) "${state.unreadAlerts} unread" else "All caught up", onClick = { onOpen(ROUTE_ALERTS) })
        }

        QkCard {
            Text("Order preferences", style = MaterialTheme.typography.titleSmall, color = tokens.ink)
            SettingRow(Icons.Rounded.Bolt, "Auto-accept orders", "Accept with 15 min prep when all items are in stock",
                trailing = { BrandSwitch(state.settings.autoAcceptOrders, { v -> onUpdateSettings { it.copy(autoAcceptOrders = v) } }) })
            SettingRow(Icons.Rounded.VolumeUp, "New order sound", "Ring until you respond",
                trailing = { BrandSwitch(state.settings.orderSound, { v -> onUpdateSettings { it.copy(orderSound = v) } }) })
        }

        QkCard {
            Text("Appearance", style = MaterialTheme.typography.titleSmall, color = tokens.ink)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Rounded.DarkMode, null, tint = tokens.ink, modifier = Modifier.size(18.dp)); Text("Theme", style = MaterialTheme.typography.bodyMedium, color = tokens.ink)
            }
            SegmentedChoice(ThemeMode.entries, state.settings.themeMode, { it.label }, { m -> onUpdateSettings { it.copy(themeMode = m) } })
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Rounded.TextFields, null, tint = tokens.ink, modifier = Modifier.size(18.dp)); Text("Text & display size", style = MaterialTheme.typography.bodyMedium, color = tokens.ink)
            }
            val presets = listOf(85 to "Compact", 100 to "Default", 115 to "Large", 130 to "Extra large")
            SegmentedChoice(presets.map { it.first }, presets.map { it.first }.minByOrNull { kotlin.math.abs(it - state.settings.zoomPercent) } ?: 100,
                { z -> presets.first { it.first == z }.second }, { z -> onUpdateSettings { it.copy(zoomPercent = z) } })
        }

        QkCard {
            SectionTitle("Support tickets") { TextButton(onClick = onOpenSupport) { Icon(Icons.Rounded.Add, null, tint = tokens.ink, modifier = Modifier.size(16.dp)); Text("Raise new", color = tokens.ink) } }
            if (state.tickets.isEmpty()) Text("No tickets yet.", style = MaterialTheme.typography.bodySmall, color = tokens.muted)
            state.tickets.take(4).forEach { t -> TicketRow(t, state.nowMillis) }
            if (state.tickets.size > 4) TextButton(onClick = { onOpen(ROUTE_SUPPORT) }) { Text("View all ${state.tickets.size}", color = tokens.ink) }
        }

        SecondaryButtonFull("Sign out", Icons.AutoMirrored.Rounded.Logout) { confirmSignOut = true }
        Text("QuickKart Merchant v${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})", style = MaterialTheme.typography.labelSmall, color = tokens.muted, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
    }

    if (editBank) BankDialog(p, onDismiss = { editBank = false }, onSave = { name, acct, ifsc -> onUpdateProfile { it.copy(bankName = name, accountNumber = acct, ifsc = ifsc) }; editBank = false })
    if (confirmSignOut) {
        AlertDialog(
            onDismissRequest = { confirmSignOut = false },
            title = { Text("Sign out?") },
            text = { Text(if (state.activeOrders.isNotEmpty()) "You have ${state.activeOrders.size} active order(s). Signing out stops new-order alerts on this phone." else "You'll need an OTP to sign back in.") },
            confirmButton = { TextButton(onClick = { confirmSignOut = false; onSignOut() }) { Text("Sign out", color = tokens.alertRed) } },
            dismissButton = { TextButton(onClick = { confirmSignOut = false }) { Text("Cancel", color = tokens.ink) } }
        )
    }
}

@Composable
private fun SecondaryButtonFull(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    com.example.ui.components.SecondaryButton(text, onClick = onClick, destructive = true, icon = icon, modifier = Modifier.fillMaxWidth().testTag("sign_out"))
}

@Composable
fun TicketRow(t: SupportTicket, nowMillis: Long) {
    val tokens = QuickKartThemeTokens.colors
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text("${t.id} · ${t.subject}", style = MaterialTheme.typography.bodyMedium, color = tokens.ink, maxLines = 1)
            Text("${t.category}${t.orderId?.let { " · Order #${it.removePrefix("QK")}" } ?: ""} · ${timeAgo(t.createdAtMillis, nowMillis)}", style = MaterialTheme.typography.labelSmall, color = tokens.muted)
        }
        StatusBadge(t.status.label, when (t.status) { TicketStatus.OPEN -> BadgeTone.BRAND; TicketStatus.IN_PROGRESS -> BadgeTone.WARNING; TicketStatus.RESOLVED -> BadgeTone.SUCCESS })
    }
}

@Composable
private fun BankDialog(p: MerchantProfile, onDismiss: () -> Unit, onSave: (String, String, String) -> Unit) {
    val tokens = QuickKartThemeTokens.colors
    var name by remember { mutableStateOf(p.bankName) }
    var acct by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var ifsc by remember { mutableStateOf(p.ifsc) }
    val acctErr = if (acct.length !in 9..18) "9–18 digits" else null
    val confirmErr = if (confirm != acct) "Doesn't match" else null
    val ifscErr = if (!Regex("^[A-Z]{4}0[A-Z0-9]{6}$").matches(ifsc)) "e.g. HDFC0001234" else null
    val nameErr = if (name.trim().length < 3) "Enter the bank name" else null
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Bank details") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Payouts in progress still go to the old account. Changes apply from the next cycle.", style = MaterialTheme.typography.bodySmall, color = tokens.muted)
                QkTextField(name, { name = it.take(40) }, "Bank name", error = nameErr.takeIf { name.isNotEmpty() })
                QkTextField(acct, { acct = it.filter(Char::isDigit).take(18) }, "New account number", keyboardType = KeyboardType.NumberPassword, error = acctErr.takeIf { acct.isNotEmpty() })
                QkTextField(confirm, { confirm = it.filter(Char::isDigit).take(18) }, "Confirm account number", keyboardType = KeyboardType.Number, error = confirmErr.takeIf { confirm.isNotEmpty() })
                QkTextField(ifsc, { ifsc = it.uppercase().filter(Char::isLetterOrDigit).take(11) }, "IFSC", error = ifscErr.takeIf { ifsc.length == 11 })
            }
        },
        confirmButton = { TextButton(enabled = listOf(acctErr, confirmErr, ifscErr, nameErr).all { it == null }, onClick = { onSave(name.trim(), acct, ifsc) }) { Text("Save", color = tokens.ink) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = tokens.muted) } }
    )
}

// Support centre: ticket list + raise ticket form

private val TICKET_CATEGORIES = listOf("Order issue", "Payout", "Catalogue", "Rider", "App problem", "Other")

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SupportScreen(state: MerchantUiState, preselectedOrderId: String?, startWithForm: Boolean, onBack: () -> Unit, onRaise: (String, String, String, String?) -> Unit) {
    val tokens = QuickKartThemeTokens.colors
    var showForm by rememberSaveable { mutableStateOf(startWithForm) }
    var category by rememberSaveable { mutableStateOf(if (preselectedOrderId != null) "Order issue" else TICKET_CATEGORIES.first()) }
    var orderId by rememberSaveable { mutableStateOf(preselectedOrderId) }
    var subject by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var showErrors by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize()) {
        ScreenHeader(if (showForm) "Raise New Ticket" else "Store Support", onBack = { if (showForm && !startWithForm) showForm = false else onBack() })
        if (!showForm) {
            LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                item {
                    QkCard(color = tokens.brandYellowSoft, borderColor = tokens.brandYellow) {
                        Text("Merchant helpline", style = MaterialTheme.typography.titleSmall, color = tokens.ink)
                        Text("Chat & tickets: 7 AM – 11 PM. Urgent order issues are answered within 15 minutes.", style = MaterialTheme.typography.bodySmall, color = tokens.muted)
                    }
                }
                if (state.tickets.isEmpty()) item { EmptyState(Icons.Rounded.SupportAgent, "No tickets", "Raise a ticket for order, payout or catalogue problems.") }
                items(state.tickets, key = { it.id }) { t -> QkCard(contentPadding = PaddingValues(12.dp)) { TicketRow(t, state.nowMillis); Text(t.description, style = MaterialTheme.typography.bodySmall, color = tokens.muted) } }
            }
            PrimaryButton("Raise new ticket", onClick = { showForm = true }, icon = Icons.Rounded.Add, modifier = Modifier.padding(16.dp), testTag = "raise_ticket")
        } else {
            val subjectErr = if (subject.trim().length < 5) "At least 5 characters" else null
            val descErr = if (description.trim().length < 15) "Describe the issue (15+ characters)" else null
            val recentOrders = state.orders.sortedByDescending { it.placedAtMillis }.take(6)
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Category", style = MaterialTheme.typography.titleSmall, color = tokens.ink)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    TICKET_CATEGORIES.forEach { c -> ChoiceChip(c, c == category, { category = c }) }
                }
                if (category == "Order issue" || category == "Rider") {
                    Text("Related order", style = MaterialTheme.typography.titleSmall, color = tokens.ink)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        ChoiceChip("None", orderId == null, { orderId = null })
                        recentOrders.forEach { o -> ChoiceChip(o.shortId, orderId == o.id, { orderId = o.id }) }
                    }
                }
                QkTextField(subject, { subject = it.take(60) }, "Subject", error = subjectErr.takeIf { showErrors })
                QkTextField(description, { description = it.take(500) }, "Describe the issue", singleLine = false, minLines = 4, error = descErr.takeIf { showErrors }, supporting = "${description.length}/500")
            }
            PrimaryButton("Submit ticket", modifier = Modifier.padding(16.dp), testTag = "submit_ticket", onClick = {
                if (subjectErr != null || descErr != null) showErrors = true
                else { onRaise(category, subject, description, orderId.takeIf { category == "Order issue" || category == "Rider" }); onBack() }
            })
        }
    }
}
