package com.example.ui.screens

import com.example.util.itemCountLabel
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
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
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
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Settings
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.local.SIGN_IN_GOOGLE
import com.example.data.local.SIGN_IN_GUEST
import com.example.data.model.OrderStage
import com.example.data.model.WalletTransaction
import androidx.compose.material.icons.rounded.PhoneAndroid
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
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
    /** Called only after a top-up payment succeeds, with the method used. */
    onAddWalletMoney: (amountInr: Int, methodNote: String) -> Unit = { _, _ -> },
    onUpdateProfile: (firstName: String, lastName: String, email: String) -> Unit = { _, _, _ -> },
    onOpenOrderHistory: () -> Unit = {},
    onOpenSupportHub: () -> Unit,
    onSignOut: () -> Unit,
    onShowMessage: (String) -> Unit,
    /** null = Account list of parent settings; otherwise the section opened in its own screen. */
    openSection: String? = null,
    onOpenSection: (String) -> Unit = {},
    onNavigateBack: () -> Unit = {},
    onLinkGoogleAccount: (googleEmail: String) -> Unit = {},
    onUnlinkGoogleAccount: () -> Unit = {}
) {
    val tokens = QuickKartThemeTokens.colors
    val metrics = QuickKartMetricsTokens.metrics
    val context = LocalContext.current
    var showAddAddressDialog by remember { mutableStateOf(false) }
    var showWalletSheet by remember { mutableStateOf(false) }
    var showTopUpFlow by remember { mutableStateOf(false) }
    var topUpPresetAmount by remember { mutableStateOf<Int?>(null) }
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showLinkGoogleFlow by remember { mutableStateOf(false) }
    var selectedReceipt by remember { mutableStateOf<WalletTransaction?>(null) }

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
            // Section screen header with back navigation
            if (openSection != null) item {
                AccountSubScreenTopBar(
                    title = accountSectionTitle(openSection),
                    onBack = onNavigateBack
                )
            }

            // Profile Header Card
            if (openSection == null) item {
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
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
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
                                    Icon(
                                        imageVector = Icons.Rounded.Edit,
                                        contentDescription = "Edit Profile",
                                        tint = tokens.onBrandYellow,
                                        modifier = Modifier
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                            .size(13.dp)
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

            // Account section: parent settings, each opening its own screen
            if (openSection == null) item {
                Text(
                    text = "Account",
                    style = MaterialTheme.typography.titleMedium,
                    color = tokens.ink,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(top = 4.dp, start = 4.dp)
                        .testTag("account_section_header")
                )
            }

            // Profile
            if (openSection == null || openSection == SECTION_PROFILE) item {
                AccountParentItem(
                    key = SECTION_PROFILE,
                    icon = Icons.Rounded.Person,
                    iconBackground = tokens.brandYellowSoft,
                    iconTint = tokens.ink,
                    title = "Profile",
                    summary = if (uiState.preferences.isGuest) "Guest account" else "Name, phone & email",
                    openSection = openSection,
                    onOpen = { onOpenSection(SECTION_PROFILE) }
                ) {
                    AccountDetailRow("Name", uiState.preferences.userName.ifBlank { "Not set" })
                    AccountDetailRow("Phone", uiState.preferences.userPhone.ifBlank { "Not set" })
                    AccountDetailRow("Email", uiState.preferences.userEmail.ifBlank { "Not set" })
                    AccountDetailRow(
                        "Location",
                        uiState.selectedNeighborhood?.displayTitle ?: "Not selected"
                    )
                    AccountDetailRow(
                        "Account type",
                        if (uiState.preferences.isGuest) "Guest" else "Registered"
                    )
                    AccountDetailRow(
                        "Signed in with",
                        when (uiState.preferences.signInMethod) {
                            SIGN_IN_GOOGLE -> "Google"
                            SIGN_IN_GUEST -> "Guest access"
                            else -> "Phone • OTP verified"
                        }
                    )
                    if (!uiState.preferences.isGuest) {
                        SignInMethodsCard(
                            phone = uiState.preferences.userPhone,
                            googleEmail = uiState.preferences.linkedGoogleEmail,
                            signedInWithGoogle = uiState.preferences.signInMethod == SIGN_IN_GOOGLE,
                            onLinkGoogle = { showLinkGoogleFlow = true },
                            onUnlinkGoogle = {
                                onUnlinkGoogleAccount()
                                onShowMessage("Google account unlinked")
                            }
                        )
                    }
                    OutlinedButton(
                        onClick = { showEditProfileDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, tokens.border),
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 48.dp)
                            .testTag("account_profile_edit_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Edit,
                            contentDescription = null,
                            tint = tokens.ink,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text("Edit Profile", style = MaterialTheme.typography.labelMedium, color = tokens.ink)
                    }
                }
            }

            // Wallet
            if (openSection == null || openSection == SECTION_WALLET) item {
                AccountParentItem(
                    key = SECTION_WALLET,
                    icon = Icons.Rounded.AccountBalanceWallet,
                    iconBackground = tokens.deliveryGreenSoft,
                    iconTint = tokens.deliveryGreenDeep,
                    title = "Wallet",
                    summary = "Balance ₹${uiState.walletBalance}",
                    openSection = openSection,
                    onOpen = { onOpenSection(SECTION_WALLET) }
                ) {
                    AccountDetailRow(
                        label = "Current balance",
                        value = "₹${uiState.walletBalance}",
                        valueColor = tokens.deliveryGreenDeep,
                        emphasize = true
                    )
                    AccountDetailRow("Transactions", "${uiState.walletTransactions.size}")

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
                                        // Opens payment for this amount; nothing is credited yet.
                                        topUpPresetAmount = amt
                                        showTopUpFlow = true
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

                    ChildGroupLabel("Recent transactions")
                    val recentTransactions = uiState.walletTransactions
                        .sortedByDescending { it.createdAtMillis }
                        
                    if (recentTransactions.isEmpty()) {
                        ChildEmptyText("No wallet transactions yet.")
                    } else {
                        recentTransactions.forEach { tx ->
                            AccountChildItem(
                                title = tx.title,
                                subtitle = "${tx.subtitle} • ${formatAccountDate(tx.createdAtMillis)}",
                                trailing = "${if (tx.isCredit) "+" else "-"}₹${tx.amountInr}",
                                trailingColor = if (tx.isCredit) tokens.deliveryGreenDeep else tokens.alertRed,
                                testTag = "account_wallet_tx_${tx.id}",
                                onClick = { selectedReceipt = tx }
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                topUpPresetAmount = null
                                showTopUpFlow = true
                            },
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
                                imageVector = Icons.AutoMirrored.Rounded.ReceiptLong,
                                contentDescription = null,
                                tint = tokens.ink,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text("Passbook", style = MaterialTheme.typography.labelMedium, color = tokens.ink)
                        }
                    }
                }
            }

            // Orders
            if (openSection == null || openSection == SECTION_ORDERS) item {
                val activeOrdersCount = uiState.orders.count { it.isActive }
                AccountParentItem(
                    key = SECTION_ORDERS,
                    icon = Icons.AutoMirrored.Rounded.ReceiptLong,
                    iconBackground = tokens.brandYellowSoft,
                    iconTint = tokens.ink,
                    title = "Orders",
                    summary = "${uiState.orders.size} orders • $activeOrdersCount active",
                    openSection = openSection,
                    onOpen = { onOpenSection(SECTION_ORDERS) },
                    testTag = "account_orders_shortcut"
                ) {
                    AccountDetailRow("Total orders", "${uiState.orders.size}")
                    AccountDetailRow("Delivered", "$deliveredOrdersCount")
                    AccountDetailRow("Active", "$activeOrdersCount")
                    AccountDetailRow(
                        label = "Total savings",
                        value = "₹$totalSavings",
                        valueColor = tokens.deliveryGreenDeep
                    )

                    ChildGroupLabel("Recent orders")
                    val recentOrders = uiState.orders
                        .sortedByDescending { it.createdAtMillis }
                        
                    if (recentOrders.isEmpty()) {
                        ChildEmptyText("No orders yet. Your orders will appear here.")
                    } else {
                        recentOrders.forEach { order ->
                            AccountChildItem(
                                title = "#${order.orderId} • ${order.stage.title}",
                                subtitle = "${formatAccountDate(order.createdAtMillis)} • " +
                                    "${itemCountLabel(order.totalItemCount)} • ${order.deliveryAddressLabel}",
                                trailing = "₹${order.totalPaid}",
                                trailingColor = tokens.ink,
                                testTag = "account_order_${order.orderId}"
                            )
                        }
                    }

                    TextButton(
                        onClick = onOpenOrderHistory,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("account_orders_view_all")
                    ) {
                        Text(
                            text = "Reorder, invoices & restock",
                            color = tokens.deliveryGreenDeep,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Saved Addresses
            if (openSection == null || openSection == SECTION_ADDRESSES) item {
                val defaultAddress = uiState.savedAddresses.firstOrNull { it.isDefault }
                AccountParentItem(
                    key = SECTION_ADDRESSES,
                    icon = Icons.Rounded.LocationOn,
                    iconBackground = tokens.brandYellowSoft,
                    iconTint = tokens.ink,
                    title = "Saved Addresses",
                    summary = "${uiState.savedAddresses.size} saved" +
                        (defaultAddress?.let { " • Default: ${it.label}" } ?: ""),
                    openSection = openSection,
                    onOpen = { onOpenSection(SECTION_ADDRESSES) },
                    testTag = "saved_addresses_card"
                ) {
                    if (uiState.savedAddresses.isEmpty()) {
                        ChildEmptyText("No saved addresses yet. Tap 'Add Address' to drop a map pin.")
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
                    Button(
                        onClick = { showAddAddressDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = tokens.brandYellow,
                            contentColor = tokens.onBrandYellow
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 48.dp)
                            .testTag("account_add_address_button")
                    ) {
                        Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Add Address", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }

            // Help & Support
            if (openSection == null || openSection == SECTION_SUPPORT) item {
                AccountParentItem(
                    key = SECTION_SUPPORT,
                    icon = Icons.Rounded.SupportAgent,
                    iconBackground = tokens.deliveryGreenSoft,
                    iconTint = tokens.deliveryGreenDeep,
                    title = "Help & Support",
                    summary = if (uiState.openSupportTicketCount > 0) {
                        "${uiState.openSupportTicketCount} open tickets"
                    } else {
                        "No open tickets"
                    },
                    openSection = openSection,
                    onOpen = { onOpenSection(SECTION_SUPPORT) },
                    testTag = "account_support_banner"
                ) {
                    AccountDetailRow("Open tickets", "${uiState.openSupportTicketCount}")
                    AccountDetailRow("Total tickets", "${uiState.supportTickets.size}")

                    ChildGroupLabel("Recent tickets")
                    val recentTickets = uiState.supportTickets
                        .sortedByDescending { it.updatedAtMillis }
                        
                    if (recentTickets.isEmpty()) {
                        ChildEmptyText("No support tickets raised.")
                    } else {
                        recentTickets.forEach { ticket ->
                            AccountChildItem(
                                title = ticket.category.label,
                                subtitle = "Order #${ticket.orderId} • ${formatAccountDate(ticket.updatedAtMillis)}",
                                trailing = ticket.status.label,
                                trailingColor = tokens.deliveryGreenDeep,
                                testTag = "account_ticket_${ticket.ticketId}"
                            )
                        }
                    }

                    TextButton(
                        onClick = onOpenSupportHub,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("account_support_get_help")
                    ) {
                        Text(
                            text = "Get Help • Missing item, refund or live chat",
                            color = tokens.deliveryGreenDeep,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Settings & Preferences
            if (openSection == null || openSection == SECTION_SETTINGS) item {
                AccountParentItem(
                    key = SECTION_SETTINGS,
                    icon = Icons.Rounded.Settings,
                    iconBackground = tokens.surfaceElevated,
                    iconTint = tokens.ink,
                    title = "Settings & Preferences",
                    summary = listOf(
                        if (uiState.preferences.isDarkTheme) "Dark mode" else "Light mode",
                        "Zoom ${uiState.preferences.screenZoomPercent}%",
                        if (uiState.preferences.notificationsEnabled) "Alerts on" else "Alerts off"
                    ).joinToString(" • "),
                    openSection = openSection,
                    onOpen = { onOpenSection(SECTION_SETTINGS) }
                ) {
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
                        border = BorderStroke(1.dp, tokens.border),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = tokens.ink),
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

            // Sign Out Button
            if (openSection == null) item {
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
            onAddMoney = onAddWalletMoney
        )
    }

    if (showTopUpFlow) {
        WalletTopUpFlow(
            currentBalanceInr = uiState.walletBalance,
            presetAmountInr = topUpPresetAmount,
            onDismiss = { showTopUpFlow = false },
            onPaymentSuccess = onAddWalletMoney
        )
    }

    if (showLinkGoogleFlow) {
        GoogleSignInFlow(
            requirePhone = false,
            onDismiss = { showLinkGoogleFlow = false },
            onSignedIn = { profile, _ ->
                showLinkGoogleFlow = false
                onLinkGoogleAccount(profile.email)
                onShowMessage("Linked Google account ${profile.email}")
            }
        )
    }

    selectedReceipt?.let { tx ->
        WalletTransactionReceiptDialog(transaction = tx, onDismiss = { selectedReceipt = null })
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

private const val SECTION_PROFILE = "profile"
private const val SECTION_WALLET = "wallet"
private const val SECTION_ORDERS = "orders"
private const val SECTION_ADDRESSES = "addresses"
private const val SECTION_SUPPORT = "support"
private const val SECTION_SETTINGS = "settings"

private fun accountSectionTitle(section: String): String = when (section) {
    SECTION_PROFILE -> "Profile"
    SECTION_WALLET -> "Wallet"
    SECTION_ORDERS -> "Orders"
    SECTION_ADDRESSES -> "Saved Addresses"
    SECTION_SUPPORT -> "Help & Support"
    SECTION_SETTINGS -> "Settings & Preferences"
    else -> "Account"
}

private fun formatAccountDate(millis: Long): String =
    SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(millis))

/**
 * A parent setting in the Account section.
 *
 * On the Account list ([openSection] == null) it renders as a row with a ">" chevron that
 * opens the setting in its own screen. On that screen ([openSection] == [key]) it renders
 * [content] — the current account details for that setting — as child items.
 */
@Composable
private fun AccountParentItem(
    key: String,
    icon: ImageVector,
    iconBackground: Color,
    iconTint: Color,
    title: String,
    summary: String,
    openSection: String?,
    onOpen: () -> Unit,
    testTag: String = "account_section_$key",
    content: @Composable ColumnScope.() -> Unit
) {
    val tokens = QuickKartThemeTokens.colors
    val metrics = QuickKartMetricsTokens.metrics

    if (openSection == key) {
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = tokens.surface),
            border = BorderStroke(1.dp, tokens.border),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("${testTag}_children")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(metrics.cardPadding),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                content = content
            )
        }
        return
    }

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = tokens.surface),
        border = BorderStroke(1.dp, tokens.border),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onOpen)
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(metrics.cardPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                color = iconBackground,
                shape = CircleShape,
                modifier = Modifier.size(maxOf(42.dp, metrics.badgeButtonSize))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(metrics.iconMedium)
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = tokens.ink,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = summary,
                    style = MaterialTheme.typography.bodySmall,
                    color = tokens.muted
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = "Open $title",
                tint = tokens.muted
            )
        }
    }
}

/** Header with a back arrow for screens opened from the Account section. */
@Composable
fun AccountSubScreenTopBar(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tokens = QuickKartThemeTokens.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .testTag("account_sub_screen_top_bar"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier.testTag("account_sub_screen_back")
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = "Back",
                tint = tokens.ink
            )
        }
        if (title.isNotBlank()) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = tokens.ink,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/** A label/value child item showing one current account detail. */
@Composable
private fun AccountDetailRow(
    label: String,
    value: String,
    valueColor: Color = QuickKartThemeTokens.colors.ink,
    emphasize: Boolean = false
) {
    val tokens = QuickKartThemeTokens.colors
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = tokens.muted,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = if (emphasize) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium,
            color = valueColor,
            fontWeight = if (emphasize) FontWeight.ExtraBold else FontWeight.SemiBold,
            textAlign = TextAlign.End
        )
    }
}

/** A record-style child item (an order, a transaction, a ticket). */
@Composable
private fun AccountChildItem(
    title: String,
    subtitle: String,
    trailing: String,
    trailingColor: Color,
    testTag: String,
    onClick: (() -> Unit)? = null
) {
    val tokens = QuickKartThemeTokens.colors
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = tokens.surfaceElevated,
        border = BorderStroke(1.dp, tokens.border),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = tokens.ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = tokens.muted,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                text = trailing,
                style = MaterialTheme.typography.labelLarge,
                color = trailingColor,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/** Shows how the user can sign in (phone, Google) with link / unlink for Google. */
@Composable
private fun SignInMethodsCard(
    phone: String,
    googleEmail: String,
    signedInWithGoogle: Boolean,
    onLinkGoogle: () -> Unit,
    onUnlinkGoogle: () -> Unit
) {
    val tokens = QuickKartThemeTokens.colors
    ChildGroupLabel("Sign-in methods")
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = tokens.surfaceElevated,
        border = BorderStroke(1.dp, tokens.border),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("account_sign_in_methods")
    ) {
        Column {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(Icons.Rounded.PhoneAndroid, contentDescription = null, tint = tokens.ink)
                Column(modifier = Modifier.weight(1f)) {
                    Text("Mobile number", style = MaterialTheme.typography.titleSmall, color = tokens.ink)
                    Text(phone.ifBlank { "Not added" }, style = MaterialTheme.typography.bodySmall, color = tokens.muted)
                }
                if (phone.isNotBlank()) {
                    Icon(
                        Icons.Rounded.CheckCircle,
                        contentDescription = "Verified",
                        tint = tokens.deliveryGreenDeep,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            HorizontalDivider(color = tokens.border)
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                GoogleGLogo(size = 22.dp)
                Column(modifier = Modifier.weight(1f)) {
                    Text("Google", style = MaterialTheme.typography.titleSmall, color = tokens.ink)
                    Text(
                        googleEmail.ifBlank { "Not linked" },
                        style = MaterialTheme.typography.bodySmall,
                        color = tokens.muted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                when {
                    googleEmail.isBlank() -> TextButton(
                        onClick = onLinkGoogle,
                        colors = ButtonDefaults.textButtonColors(contentColor = tokens.deliveryGreenDeep),
                        modifier = Modifier.testTag("account_link_google_button")
                    ) { Text("Link", fontWeight = FontWeight.Bold) }
                    // The account used to sign in can't be unlinked from here.
                    signedInWithGoogle -> Icon(
                        Icons.Rounded.CheckCircle,
                        contentDescription = "Linked",
                        tint = tokens.deliveryGreenDeep,
                        modifier = Modifier.size(18.dp)
                    )
                    else -> TextButton(
                        onClick = onUnlinkGoogle,
                        colors = ButtonDefaults.textButtonColors(contentColor = tokens.alertRed),
                        modifier = Modifier.testTag("account_unlink_google_button")
                    ) { Text("Unlink", fontWeight = FontWeight.Bold) }
                }
            }
        }
    }
}

@Composable
private fun ChildGroupLabel(text: String) {
    Text(
        text = text.uppercase(Locale.getDefault()),
        style = MaterialTheme.typography.labelSmall,
        color = QuickKartThemeTokens.colors.muted,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 4.dp)
    )
}

@Composable
private fun ChildEmptyText(text: String) {
    val tokens = QuickKartThemeTokens.colors
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = tokens.surfaceElevated,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = tokens.muted,
            modifier = Modifier.padding(14.dp)
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
                        colors = ButtonDefaults.textButtonColors(contentColor = tokens.deliveryGreenDeep),
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
