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
import androidx.compose.material.icons.rounded.FormatSize
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LocalGroceryStore
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PictureAsPdf
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.SupportAgent
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
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.example.service.PdfReceiptService
import com.example.ui.QuickKartUiState
import com.example.ui.components.AddAddressDialog
import com.example.ui.components.OrderFeedbackBottomSheet
import com.example.ui.components.QuickKartLogo
import com.example.ui.components.SmartRestockReminderBanner
import com.example.ui.components.SpendingTrendCard
import com.example.ui.theme.AmberGold
import com.example.ui.theme.DEFAULT_SCREEN_ZOOM_PERCENT
import com.example.ui.theme.MAX_SCREEN_ZOOM_PERCENT
import com.example.ui.theme.MIN_SCREEN_ZOOM_PERCENT
import com.example.ui.theme.QuickKartMetricsTokens
import com.example.ui.theme.QuickKartThemeTokens
import com.example.util.NotificationHelper
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AccountHistoryScreen(
    uiState: QuickKartUiState,
    onAddRestockItem: (RestockSuggestion) -> Unit,
    onHistorySearchChange: (String) -> Unit,
    onSelectTimeFilter: (OrderHistoryTimeFilter) -> Unit,
    onQuickReorder: (OrderRecord) -> Unit,
    onReorderCheckout: (OrderRecord) -> Unit,
    onSubmitRating: (String, Int, List<String>, String) -> Unit,
    onToggleTheme: (Boolean) -> Unit,
    onToggleNotifications: (Boolean) -> Unit,
    onScreenZoomChange: (Int) -> Unit,
    onSetDefaultAddress: (String) -> Unit,
    onDeleteAddress: (String) -> Unit,
    onAddAddress: (String, String, String, String) -> Unit,
    onOpenSupportHub: () -> Unit,
    onOpenSupportForOrder: (OrderRecord) -> Unit,
    onSignOut: () -> Unit,
    onShowMessage: (String) -> Unit
) {
    val tokens = QuickKartThemeTokens.colors
    val metrics = QuickKartMetricsTokens.metrics
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val receiptService = remember(context) { PdfReceiptService(context) }
    var showAddAddressDialog by remember { mutableStateOf(false) }
    var feedbackTargetOrder by remember { mutableStateOf<OrderRecord?>(null) }

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

            // Help & Customer Support Hub Banner
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = tokens.surface),
                    border = BorderStroke(1.dp, tokens.deliveryGreen.copy(alpha = 0.45f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .clickable(onClick = onOpenSupportHub)
                        .testTag("account_support_banner")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(metrics.cardPadding),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            color = tokens.deliveryGreenSoft,
                            shape = CircleShape,
                            modifier = Modifier.size(maxOf(46.dp, metrics.badgeButtonSize))
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Rounded.SupportAgent,
                                    contentDescription = "Customer Support",
                                    tint = tokens.deliveryGreenDeep,
                                    modifier = Modifier.size(metrics.iconMedium)
                                )
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "Help & Customer Support",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = tokens.ink,
                                    fontWeight = FontWeight.Bold
                                )
                                if (uiState.openSupportTicketCount > 0) {
                                    Surface(
                                        color = tokens.brandYellow,
                                        shape = RoundedCornerShape(50)
                                    ) {
                                        Text(
                                            text = "${uiState.openSupportTicketCount} Open",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = tokens.onBrandYellow,
                                            fontWeight = FontWeight.ExtraBold,
                                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                            Text(
                                text = "Missing item, refund, replacement or live chat",
                                style = MaterialTheme.typography.bodySmall,
                                color = tokens.muted
                            )
                        }
                        TextButton(onClick = onOpenSupportHub) {
                            Text(
                                text = "Get Help",
                                color = tokens.deliveryGreenDeep,
                                fontWeight = FontWeight.Bold
                            )
                        }
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

                        // Screen Zoom Slider (default 100%, adaptive font/icon/layout scaling)
                        ScreenZoomControlSection(
                            zoomPercent = uiState.preferences.screenZoomPercent,
                            gridColumns = metrics.gridColumns,
                            onZoomChange = onScreenZoomChange
                        )

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
                            tint = tokens.deliveryGreenDeep,
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ScreenZoomControlSection(
    zoomPercent: Int,
    gridColumns: Int,
    onZoomChange: (Int) -> Unit
) {
    val tokens = QuickKartThemeTokens.colors
    val metrics = QuickKartMetricsTokens.metrics
    var sliderValue by remember { mutableFloatStateOf(zoomPercent.toFloat()) }

    LaunchedEffect(zoomPercent) {
        sliderValue = zoomPercent.toFloat()
    }

    val currentStepPercent = ((sliderValue.roundToInt().coerceIn(MIN_SCREEN_ZOOM_PERCENT, MAX_SCREEN_ZOOM_PERCENT) + 2) / 5) * 5
    val scaleLabel = when {
        currentStepPercent < 100 -> "Compact"
        currentStepPercent == 100 -> "Default"
        currentStepPercent <= 115 -> "Large"
        else -> "Extra Large"
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("screen_zoom_section"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.FormatSize,
                contentDescription = null,
                tint = tokens.ink,
                modifier = Modifier.size(metrics.iconMedium)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Screen Zoom",
                    style = MaterialTheme.typography.titleSmall,
                    color = tokens.ink
                )
                Text(
                    text = "Scales fonts, icons & grid ($gridColumns cols on this display)",
                    style = MaterialTheme.typography.bodySmall,
                    color = tokens.muted
                )
            }
            Surface(
                color = if (currentStepPercent == DEFAULT_SCREEN_ZOOM_PERCENT) {
                    tokens.deliveryGreenSoft
                } else {
                    tokens.brandYellowSoft
                },
                shape = RoundedCornerShape(50),
                border = BorderStroke(
                    1.dp,
                    if (currentStepPercent == DEFAULT_SCREEN_ZOOM_PERCENT) {
                        tokens.deliveryGreen.copy(alpha = 0.45f)
                    } else {
                        tokens.brandYellow
                    }
                )
            ) {
                Text(
                    text = "$currentStepPercent% • $scaleLabel",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (currentStepPercent == DEFAULT_SCREEN_ZOOM_PERCENT) {
                        tokens.deliveryGreenDeep
                    } else {
                        tokens.ink
                    },
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                        .testTag("screen_zoom_badge")
                )
            }
        }

        // Live Responsive Preview Strip
        Surface(
            color = tokens.surfaceElevated,
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, tokens.border),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 9.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    color = tokens.brandYellowSoft,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.size(metrics.badgeButtonSize)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.LocalGroceryStore,
                            contentDescription = null,
                            tint = tokens.ink,
                            modifier = Modifier.size(metrics.iconMedium)
                        )
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Live UI Preview • Fresh A2 Milk",
                        style = MaterialTheme.typography.titleSmall,
                        color = tokens.ink
                    )
                    Text(
                        text = "Icons ${metrics.iconMedium.value.roundToInt()}dp • Fonts scaled to $currentStepPercent%",
                        style = MaterialTheme.typography.bodySmall,
                        color = tokens.muted
                    )
                }
                if (currentStepPercent != DEFAULT_SCREEN_ZOOM_PERCENT) {
                    TextButton(
                        onClick = {
                            sliderValue = DEFAULT_SCREEN_ZOOM_PERCENT.toFloat()
                            onZoomChange(DEFAULT_SCREEN_ZOOM_PERCENT)
                        },
                        modifier = Modifier.testTag("reset_screen_zoom_button")
                    ) {
                        Text("Reset", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Slider with -5% / +5% precision buttons (48dp touch targets)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            IconButton(
                onClick = {
                    val next = (zoomPercent - 5).coerceAtLeast(MIN_SCREEN_ZOOM_PERCENT)
                    sliderValue = next.toFloat()
                    onZoomChange(next)
                },
                enabled = zoomPercent > MIN_SCREEN_ZOOM_PERCENT,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("zoom_decrease_button")
            ) {
                Icon(
                    imageVector = Icons.Rounded.Remove,
                    contentDescription = "Decrease screen zoom",
                    tint = if (zoomPercent > MIN_SCREEN_ZOOM_PERCENT) tokens.ink else tokens.muted,
                    modifier = Modifier.size(metrics.iconMedium)
                )
            }

            Slider(
                value = sliderValue,
                onValueChange = { raw ->
                    sliderValue = raw
                    val snapped = ((raw.roundToInt().coerceIn(MIN_SCREEN_ZOOM_PERCENT, MAX_SCREEN_ZOOM_PERCENT) + 2) / 5) * 5
                    if (snapped != zoomPercent) {
                        onZoomChange(snapped)
                    }
                },
                valueRange = MIN_SCREEN_ZOOM_PERCENT.toFloat()..MAX_SCREEN_ZOOM_PERCENT.toFloat(),
                steps = ((MAX_SCREEN_ZOOM_PERCENT - MIN_SCREEN_ZOOM_PERCENT) / 5) - 1,
                colors = SliderDefaults.colors(
                    thumbColor = tokens.brandYellow,
                    activeTrackColor = tokens.deliveryGreenDeep,
                    inactiveTrackColor = tokens.border
                ),
                modifier = Modifier
                    .weight(1f)
                    .testTag("screen_zoom_slider")
            )

            IconButton(
                onClick = {
                    val next = (zoomPercent + 5).coerceAtMost(MAX_SCREEN_ZOOM_PERCENT)
                    sliderValue = next.toFloat()
                    onZoomChange(next)
                },
                enabled = zoomPercent < MAX_SCREEN_ZOOM_PERCENT,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("zoom_increase_button")
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = "Increase screen zoom",
                    tint = if (zoomPercent < MAX_SCREEN_ZOOM_PERCENT) tokens.ink else tokens.muted,
                    modifier = Modifier.size(metrics.iconMedium)
                )
            }
        }

        // Quick Preset Chips
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(85, 100, 115, 130).forEach { preset ->
                val selected = zoomPercent == preset
                Surface(
                    shape = RoundedCornerShape(50),
                    color = if (selected) tokens.ink else tokens.surfaceElevated,
                    border = BorderStroke(1.dp, if (selected) tokens.ink else tokens.border),
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .clickable {
                            sliderValue = preset.toFloat()
                            onZoomChange(preset)
                        }
                        .testTag("zoom_preset_$preset")
                ) {
                    Text(
                        text = if (preset == 100) "100% Default" else "$preset%",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (selected) tokens.background else tokens.ink,
                        modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}
