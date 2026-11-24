package com.example.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PictureAsPdf
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Work
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.data.model.OrderHistoryTimeFilter
import com.example.data.model.OrderRecord
import com.example.data.model.OrderStage
import com.example.data.model.RestockSuggestion
import com.example.data.model.SavedAddress
import com.example.ui.QuickKartUiState
import com.example.ui.components.AddAddressDialog
import com.example.ui.components.QuickKartLogo
import com.example.ui.components.SmartRestockReminderBanner
import com.example.ui.components.SpendingTrendCard
import com.example.ui.theme.AmberGold
import com.example.ui.theme.QuickKartMetricsTokens
import com.example.ui.theme.QuickKartThemeTokens
import com.example.util.NotificationHelper
import com.example.util.PdfReceiptGenerator
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AccountHistoryScreen(
    uiState: QuickKartUiState,
    onAddRestockItem: (RestockSuggestion) -> Unit,
    onHistorySearchChange: (String) -> Unit,
    onSelectTimeFilter: (OrderHistoryTimeFilter) -> Unit,
    onQuickReorder: (OrderRecord) -> Unit,
    onReorderCheckout: (OrderRecord) -> Unit,
    onToggleTheme: (Boolean) -> Unit,
    onToggleNotifications: (Boolean) -> Unit,
    onSetDefaultAddress: (String) -> Unit,
    onDeleteAddress: (String) -> Unit,
    onAddAddress: (String, String, String, String) -> Unit,
    onSignOut: () -> Unit,
    onShowMessage: (String) -> Unit
) {
    val tokens = QuickKartThemeTokens.colors
    val metrics = QuickKartMetricsTokens.metrics
    val context = LocalContext.current
    var showAddAddressDialog by remember { mutableStateOf(false) }

    val notifPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            NotificationHelper.sendTestNotification(context)
            onShowMessage("Test notification sent!")
        } else {
            onShowMessage("Notification permission denied.")
        }
    }

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
                .fillMaxSize(),
            contentPadding = PaddingValues(metrics.screenHorizontalPadding),
            verticalArrangement = Arrangement.spacedBy(metrics.itemSpacing)
        ) {
            // Profile Header Card
            item {
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF141414)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(metrics.cardPadding + 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(metrics.itemSpacing)
                    ) {
                        Surface(
                            color = tokens.brandYellow,
                            shape = CircleShape,
                            modifier = Modifier.size(metrics.avatarSize)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Rounded.Person,
                                    contentDescription = "User avatar",
                                    tint = tokens.onBrandYellow,
                                    modifier = Modifier.size(metrics.iconLarge)
                                )
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = uiState.preferences.userName,
                                style = MaterialTheme.typography.titleLarge,
                                color = Color.White
                            )
                            Text(
                                text = if (uiState.preferences.isGuest) {
                                    "Guest Account • ${uiState.selectedNeighborhood?.name ?: ""}"
                                } else {
                                    "${uiState.preferences.userPhone} • ${uiState.selectedNeighborhood?.name ?: ""}"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.78f)
                            )
                            Spacer(Modifier.height(8.dp))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Surface(
                                    color = Color.White.copy(alpha = 0.12f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "$deliveredOrdersCount Orders",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                                Surface(
                                    color = tokens.deliveryGreenDeep,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "₹$totalSavings Saved",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }

                        QuickKartLogo(
                            size = metrics.badgeButtonSize,
                            cornerRadius = 10.dp
                        )
                    }
                }
            }

            // Smart Restock Reminder Banner
            item {
                SmartRestockReminderBanner(
                    suggestions = uiState.restockSuggestions,
                    onAddSuggestion = { suggestion ->
                        onAddRestockItem(suggestion)
                    }
                )
            }

            // Spending Trend Card
            item {
                SpendingTrendCard(orders = uiState.orders)
            }

            // Order History Header + Search + Time Filters
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Order History",
                        style = MaterialTheme.typography.titleLarge,
                        color = tokens.ink
                    )

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
                        onDownloadReceipt = {
                            runCatching {
                                val result = PdfReceiptGenerator.generateReceipt(context, order)
                                val viewIntent = PdfReceiptGenerator.openPdfIntent(result.fileUri)
                                val chooser = PdfReceiptGenerator.sharePdfIntent(result.fileUri, order.orderId)
                                val launched = runCatching {
                                    context.startActivity(viewIntent)
                                }.isSuccess
                                if (!launched) {
                                    context.startActivity(chooser)
                                }
                                val msg = if (result.savedToDownloads) {
                                    "Receipt ${result.fileName} saved to Downloads/QuickKart!"
                                } else {
                                    "Receipt ${result.fileName} generated!"
                                }
                                onShowMessage(msg)
                            }.onFailure { err ->
                                onShowMessage("Could not generate PDF: ${err.localizedMessage ?: "Error"}")
                            }
                        }
                    )
                }
            }

            // Settings Section
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = tokens.surface),
                    border = BorderStroke(1.dp, tokens.border),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(metrics.cardPadding),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Settings & Preferences",
                            style = MaterialTheme.typography.titleMedium,
                            color = tokens.ink
                        )

                        // Light / Dark Theme Toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.DarkMode,
                                    contentDescription = null,
                                    tint = tokens.ink,
                                    modifier = Modifier.size(metrics.iconMedium)
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Dark Mode",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = tokens.ink
                                    )
                                    Text(
                                        text = "Switch between crisp Light & Dark UI",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = tokens.muted
                                    )
                                }
                            }
                            Switch(
                                checked = uiState.preferences.isDarkTheme,
                                onCheckedChange = onToggleTheme,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = tokens.onBrandYellow,
                                    checkedTrackColor = tokens.brandYellow
                                ),
                                modifier = Modifier.testTag("settings_theme_switch")
                            )
                        }

                        HorizontalDivider(color = tokens.border)

                        // Notifications Toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Notifications,
                                    contentDescription = null,
                                    tint = tokens.ink,
                                    modifier = Modifier.size(metrics.iconMedium)
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Order Alerts",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = tokens.ink
                                    )
                                    Text(
                                        text = "Live status bar updates for stages",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = tokens.muted
                                    )
                                }
                            }
                            Switch(
                                checked = uiState.preferences.notificationsEnabled,
                                onCheckedChange = onToggleNotifications,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = tokens.deliveryGreenDeep
                                ),
                                modifier = Modifier.testTag("settings_notifications_switch")
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                                    !NotificationHelper.hasNotificationPermission(context)
                                ) {
                                    notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                } else {
                                    NotificationHelper.sendTestNotification(context)
                                    onShowMessage("Test notification sent to status bar!")
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .defaultMinSize(minHeight = 48.dp)
                                .testTag("send_test_notification_button")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.NotificationsActive,
                                contentDescription = null,
                                modifier = Modifier.size(metrics.iconMedium)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Send Test Notification")
                        }
                    }
                }
            }

            // Saved Addresses Manager
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = tokens.surface),
                    border = BorderStroke(1.dp, tokens.border),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(metrics.cardPadding),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Saved Addresses",
                                style = MaterialTheme.typography.titleMedium,
                                color = tokens.ink,
                                modifier = Modifier.weight(1f)
                            )
                            TextButton(
                                onClick = { showAddAddressDialog = true },
                                modifier = Modifier.testTag("account_add_address_button")
                            ) {
                                Icon(
                                    Icons.Rounded.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(metrics.iconSmall)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text("Add")
                            }
                        }

                        uiState.savedAddresses.forEach { addr ->
                            SavedAddressRow(
                                address = addr,
                                canDelete = uiState.savedAddresses.size > 1,
                                onSetDefault = { onSetDefaultAddress(addr.id) },
                                onDelete = { onDeleteAddress(addr.id) }
                            )
                        }
                    }
                }
            }

            // Sign Out Button
            item {
                OutlinedButton(
                    onClick = onSignOut,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, tokens.alertRed.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 50.dp)
                        .testTag("sign_out_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.Logout,
                        contentDescription = null,
                        tint = tokens.alertRed,
                        modifier = Modifier.size(metrics.iconMedium)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Sign Out",
                        style = MaterialTheme.typography.labelLarge,
                        color = tokens.alertRed
                    )
                }
                Spacer(Modifier.height(12.dp))
            }
        }
    }

    if (showAddAddressDialog) {
        AddAddressDialog(
            neighborhoods = uiState.neighborhoods,
            initialNeighborhoodId = uiState.selectedNeighborhood?.id ?: "indiranagar",
            onDismiss = { showAddAddressDialog = false },
            onSave = { label, full, landmark, nId ->
                onAddAddress(label, full, landmark, nId)
                showAddAddressDialog = false
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
                        text = "$dateFormatted • ${order.neighborhoodName}",
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

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                maxItemsInEachRow = if (metrics.isNarrowPhone) 2 else 3
            ) {
                OutlinedButton(
                    onClick = onQuickReorder,
                    shape = RoundedCornerShape(10.dp),
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

@Composable
private fun SavedAddressRow(
    address: SavedAddress,
    canDelete: Boolean,
    onSetDefault: () -> Unit,
    onDelete: () -> Unit
) {
    val tokens = QuickKartThemeTokens.colors
    val metrics = QuickKartMetricsTokens.metrics
    val icon = when (address.label) {
        "Home" -> Icons.Rounded.Home
        "Work" -> Icons.Rounded.Work
        else -> Icons.Rounded.LocationOn
    }

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (address.isDefault) tokens.brandYellowSoft else tokens.surfaceElevated,
        border = BorderStroke(1.dp, if (address.isDefault) tokens.brandYellow else tokens.border),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onSetDefault)
    ) {
        Row(
            modifier = Modifier.padding(metrics.cardPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tokens.ink,
                modifier = Modifier.size(metrics.iconMedium)
            )
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = address.label,
                        style = MaterialTheme.typography.titleSmall,
                        color = tokens.ink
                    )
                    if (address.isDefault) {
                        Icon(
                            imageVector = Icons.Rounded.CheckCircle,
                            contentDescription = "Default",
                            tint = tokens.deliveryGreenDeep,
                            modifier = Modifier.size(metrics.iconSmall)
                        )
                    }
                }
                Text(
                    text = address.fullAddress,
                    style = MaterialTheme.typography.bodySmall,
                    color = tokens.muted
                )
            }
            if (canDelete) {
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Rounded.DeleteOutline,
                        contentDescription = "Delete address",
                        tint = tokens.muted,
                        modifier = Modifier.size(metrics.iconMedium)
                    )
                }
            }
        }
    }
}
