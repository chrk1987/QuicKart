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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.FormatSize
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LocalGroceryStore
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.Remove
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.OrderStage
import com.example.data.model.SavedAddress
import com.example.ui.QuickKartUiState
import com.example.ui.components.AddAddressDialog
import com.example.ui.components.QuickKartLogo
import com.example.ui.theme.DEFAULT_SCREEN_ZOOM_PERCENT
import com.example.ui.theme.MAX_SCREEN_ZOOM_PERCENT
import com.example.ui.theme.MIN_SCREEN_ZOOM_PERCENT
import com.example.ui.theme.QuickKartMetricsTokens
import com.example.ui.theme.QuickKartThemeTokens
import com.example.util.NotificationHelper
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AccountHistoryScreen(
    uiState: QuickKartUiState,
    onToggleTheme: (Boolean) -> Unit,
    onToggleNotifications: (Boolean) -> Unit,
    onScreenZoomChange: (Int) -> Unit,
    onSetDefaultAddress: (String) -> Unit,
    onDeleteAddress: (String) -> Unit,
    onAddAddress: (
        label: String,
        fullAddress: String,
        landmark: String,
        neighborhoodId: String,
        area: String,
        city: String,
        state: String,
        postalCode: String,
        country: String,
        latitude: Double?,
        longitude: Double?
    ) -> Unit,
    onAddWalletMoney: (Int) -> Unit = {},
    onUpdateProfile: (firstName: String, lastName: String, email: String) -> Unit = { _, _, _ -> },
    onOpenOrderHistory: () -> Unit = {},
    onOpenSupportHub: () -> Unit,
    onSignOut: () -> Unit,
    onShowMessage: (String) -> Unit
) {
    val tokens = QuickKartThemeTokens.colors
    val metrics = QuickKartMetricsTokens.metrics
    val context = LocalContext.current
    var showAddAddressDialog by remember { mutableStateOf(false) }
    var showWalletSheet by remember { mutableStateOf(false) }
    var showEditProfileDialog by remember { mutableStateOf(false) }

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
                .fillMaxSize()
                .testTag("account_management_screen"),
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
                                    listOf(
                                        uiState.preferences.userPhone,
                                        uiState.preferences.userEmail,
                                        uiState.selectedNeighborhood?.name
                                    ).filter { !it.isNullOrBlank() }.joinToString(" • ")
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
                                Surface(
                                    color = tokens.brandYellow,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { showEditProfileDialog = true }
                                        .testTag("account_edit_profile_chip")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Edit,
                                            contentDescription = "Edit details",
                                            tint = tokens.onBrandYellow,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Text(
                                            text = "Edit Profile",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = tokens.onBrandYellow,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
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

            // QuickKart Wallet Card
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = tokens.surface),
                    border = BorderStroke(1.5.dp, tokens.deliveryGreen.copy(alpha = 0.6f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("account_wallet_card")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(metrics.cardPadding),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Surface(
                                    color = tokens.deliveryGreenSoft,
                                    shape = CircleShape,
                                    modifier = Modifier.size(46.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Rounded.AccountBalanceWallet,
                                            contentDescription = "Wallet",
                                            tint = tokens.deliveryGreenDeep,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                                Column {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = "QuickKart Wallet",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = tokens.ink,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Surface(
                                            color = tokens.brandYellowSoft,
                                            shape = RoundedCornerShape(50)
                                        ) {
                                            Text(
                                                text = "1-Tap Pay",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = tokens.ink,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = "Instant checkout & zero refund delays",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = tokens.muted
                                    )
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Balance",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = tokens.muted
                                )
                                Text(
                                    text = "₹${uiState.walletBalance}",
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = tokens.deliveryGreenDeep,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }

                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(100, 200, 500, 1000).forEach { amt ->
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = tokens.surfaceElevated,
                                    border = BorderStroke(1.dp, tokens.border),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50))
                                        .clickable {
                                            onAddWalletMoney(amt)
                                            onShowMessage("Added ₹$amt to QuickKart Wallet!")
                                        }
                                        .testTag("quick_add_wallet_$amt")
                                ) {
                                    Text(
                                        text = "+₹$amt",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = tokens.ink,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { showWalletSheet = true },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = tokens.deliveryGreenDeep,
                                    contentColor = Color.White
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .defaultMinSize(minHeight = 48.dp)
                                    .testTag("account_wallet_add_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text("Add Money", style = MaterialTheme.typography.labelMedium)
                            }

                            OutlinedButton(
                                onClick = { showWalletSheet = true },
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, tokens.border),
                                modifier = Modifier
                                    .weight(1f)
                                    .defaultMinSize(minHeight = 48.dp)
                                    .testTag("account_wallet_passbook_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.ReceiptLong,
                                    contentDescription = null,
                                    tint = tokens.ink,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = "Passbook",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = tokens.ink
                                )
                            }
                        }
                    }
                }
            }

            // Saved Addresses Manager under Account Management
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = tokens.surface),
                    border = BorderStroke(1.dp, tokens.border),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("saved_addresses_card")
                ) {
                    Column(
                        modifier = Modifier.padding(metrics.cardPadding),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Saved Addresses",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = tokens.ink,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Delivery locations for rapid 8-minute delivery",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = tokens.muted
                                )
                            }
                            Spacer(Modifier.width(8.dp))
                            Button(
                                onClick = { showAddAddressDialog = true },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = tokens.brandYellow,
                                    contentColor = tokens.onBrandYellow
                                ),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                                modifier = Modifier
                                    .defaultMinSize(minHeight = 48.dp)
                                    .testTag("account_add_address_button")
                            ) {
                                Icon(
                                    Icons.Rounded.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text("Add Address", style = MaterialTheme.typography.labelSmall)
                            }
                        }

                        if (uiState.savedAddresses.isEmpty()) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = tokens.surfaceElevated,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "No saved addresses yet. Tap '+ Add Address' to drop a map pin.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = tokens.muted,
                                    modifier = Modifier.padding(14.dp)
                                )
                            }
                        } else {
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
            }

            // Order History Tab Shortcut Card
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = tokens.surface),
                    border = BorderStroke(1.dp, tokens.border),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .clickable(onClick = onOpenOrderHistory)
                        .testTag("account_orders_shortcut")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(metrics.cardPadding),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            color = tokens.brandYellowSoft,
                            shape = CircleShape,
                            modifier = Modifier.size(maxOf(46.dp, metrics.badgeButtonSize))
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Rounded.ReceiptLong,
                                    contentDescription = "Order History",
                                    tint = tokens.ink,
                                    modifier = Modifier.size(metrics.iconMedium)
                                )
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Order History & Reorder",
                                style = MaterialTheme.typography.titleSmall,
                                color = tokens.ink,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "$deliveredOrdersCount past orders • Invoices, reorder & restock",
                                style = MaterialTheme.typography.bodySmall,
                                color = tokens.muted
                            )
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                            contentDescription = "Open Orders tab",
                            tint = tokens.muted
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

                        ScreenZoomControlSection(
                            zoomPercent = uiState.preferences.screenZoomPercent,
                            gridColumns = metrics.gridColumns,
                            onZoomChange = onScreenZoomChange
                        )

                        HorizontalDivider(color = tokens.border)

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

    if (showWalletSheet) {
        WalletSheet(
            balanceInr = uiState.walletBalance,
            transactions = uiState.walletTransactions,
            onDismiss = { showWalletSheet = false },
            onAddMoney = { amount ->
                onAddWalletMoney(amount)
                onShowMessage("Added ₹$amount to QuickKart Wallet!")
            }
        )
    }

    if (showEditProfileDialog) {
        val nameParts = uiState.preferences.userName.trim().split(" ")
        val fName = nameParts.firstOrNull() ?: ""
        val lName = nameParts.drop(1).joinToString(" ")
        AccountSetupDialog(
            initialPhone = uiState.preferences.userPhone,
            initialFirstName = fName,
            initialLastName = lName,
            initialEmail = uiState.preferences.userEmail,
            title = "Account Details",
            subtitle = "Update minimum required details for account management (First name is mandatory).",
            onDismiss = { showEditProfileDialog = false },
            onCompleteProfile = { firstName, lastName, email ->
                onUpdateProfile(firstName, lastName, email)
                showEditProfileDialog = false
                onShowMessage("Account profile updated successfully!")
            }
        )
    }

    if (showAddAddressDialog) {
        AddAddressDialog(
            neighborhoods = uiState.neighborhoods,
            initialNeighborhoodId = uiState.selectedNeighborhood?.id ?: "indiranagar",
            onDismiss = { showAddAddressDialog = false },
            onSave = { label, full, landmark, nId, area, city, state, postal, country, lat, lng ->
                onAddAddress(label, full, landmark, nId, area, city, state, postal, country, lat, lng)
                showAddAddressDialog = false
                onShowMessage("Saved $label address to your account!")
            }
        )
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
            .testTag("saved_address_row_${address.id}")
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
                if (address.formattedLocationLine.isNotBlank()) {
                    Text(
                        text = address.formattedLocationLine,
                        style = MaterialTheme.typography.labelSmall,
                        color = tokens.ink.copy(alpha = 0.8f)
                    )
                }
                address.formattedCoordinates?.let { coords ->
                    Text(
                        text = "Pin: $coords • ${address.country}",
                        style = MaterialTheme.typography.labelSmall,
                        color = tokens.deliveryGreenDeep
                    )
                }
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
