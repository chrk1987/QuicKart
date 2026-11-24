package com.example.ui.screens

import com.example.util.itemCountLabel
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LocalOffer
import androidx.compose.material.icons.rounded.LocalShipping
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.QrCode2
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Work
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.SeedData
import com.example.data.model.DeliverySlot
import com.example.data.model.PaymentMethod
import com.example.data.model.SavedAddress
import com.example.ui.QuickKartUiState
import com.example.ui.components.AddAddressDialog
import com.example.ui.components.ProductArtwork
import com.example.ui.components.QuickKartLogo
import com.example.ui.theme.QuickKartMetricsTokens
import com.example.ui.theme.QuickKartThemeTokens

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CartCheckoutScreen(
    uiState: QuickKartUiState,
    onSelectAddress: (SavedAddress) -> Unit,
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
    onIncrementCart: (String, String) -> Unit,
    onDecrementCart: (String, String) -> Unit,
    onRemoveCartItem: (String, String) -> Unit,
    onPromoInputChange: (String) -> Unit,
    onApplyPromo: (String) -> Unit,
    onRemovePromo: () -> Unit,
    onSelectTip: (Int) -> Unit,
    onSelectCustomTipMode: () -> Unit,
    onCustomTipChange: (String) -> Unit,
    onToggleInstruction: (String) -> Unit,
    onRiderNoteChange: (String) -> Unit,
    onSelectPaymentMethod: (PaymentMethod) -> Unit,
    onSelectDeliverySlot: (DeliverySlot) -> Unit,
    /** Called only after a top-up payment succeeds, with the method used. */
    onAddWalletMoney: (amountInr: Int, methodNote: String) -> Unit = { _, _ -> },
    onPlaceOrder: () -> Unit,
    onBrowseStorefront: () -> Unit
) {
    val tokens = QuickKartThemeTokens.colors
    val metrics = QuickKartMetricsTokens.metrics
    var showAddAddressDialog by remember { mutableStateOf(false) }
    // Non-null while the wallet top-up payment flow is open for this amount.
    var topUpAmount by remember { mutableStateOf<Int?>(null) }
    // True while the UPI / Card payment for this order is in progress.
    var showOrderPayment by remember { mutableStateOf(false) }

    if (uiState.cartItems.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(tokens.background)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.widthIn(max = 380.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_empty_cart),
                    contentDescription = "Empty QuickKart basket",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(190.dp)
                        .clip(RoundedCornerShape(28.dp))
                )
                Text(
                    text = "Your basket is empty",
                    style = MaterialTheme.typography.headlineSmall,
                    color = tokens.ink
                )
                Text(
                    text = "Add fresh dairy, artisan bread, produce, or cold brew — delivered in 8 minutes.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = tokens.muted
                )
                Button(
                    onClick = onBrowseStorefront,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = tokens.brandYellow,
                        contentColor = tokens.onBrandYellow
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("empty_cart_browse_button")
                ) {
                    Text("Browse 8-Min Essentials", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
        return
    }

    val bill = uiState.billBreakdown
    val draft = uiState.checkoutDraft
    val walletShortfall = (bill.grandTotal - uiState.walletBalance).coerceAtLeast(0)
    val insufficientWallet = draft.paymentMethod == PaymentMethod.WALLET && walletShortfall > 0
    val animatedProgress by animateFloatAsState(
        targetValue = bill.freeDeliveryProgress,
        animationSpec = tween(450),
        label = "free_delivery_progress"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(tokens.background),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 720.dp)
                .fillMaxSize()
        ) {
            // Sticky Header
            Surface(
                color = tokens.surface,
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = metrics.screenHorizontalPadding, vertical = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    QuickKartLogo(
                        size = metrics.badgeButtonSize,
                        cornerRadius = 10.dp
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Checkout",
                            style = MaterialTheme.typography.headlineSmall,
                            color = tokens.ink
                        )
                        Text(
                            text = if (draft.selectedDeliverySlot.isExpress) {
                                "${itemCountLabel(uiState.totalCartCount)} • ${uiState.selectedNeighborhood?.etaMinutes ?: 8}-min express delivery"
                            } else {
                                "${itemCountLabel(uiState.totalCartCount)} • Scheduled: ${draft.selectedDeliverySlot.displaySummary}"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = tokens.deliveryGreenDeep
                        )
                    }
                    Surface(
                        color = tokens.brandYellowSoft,
                        shape = RoundedCornerShape(50)
                    ) {
                        Text(
                            text = "₹${bill.grandTotal}",
                            style = MaterialTheme.typography.titleMedium,
                            color = tokens.ink,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(metrics.screenHorizontalPadding),
                verticalArrangement = Arrangement.spacedBy(metrics.itemSpacing)
            ) {
                // Free Delivery Progress Card
                item {
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (bill.isFreeDelivery) tokens.deliveryGreenSoft else tokens.surface
                        ),
                        border = BorderStroke(
                            1.dp,
                            if (bill.isFreeDelivery) tokens.deliveryGreen.copy(alpha = 0.4f) else tokens.border
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = if (bill.isFreeDelivery) Icons.Rounded.CheckCircle else Icons.Rounded.LocalShipping,
                                    contentDescription = null,
                                    tint = if (bill.isFreeDelivery) tokens.deliveryGreenDeep else tokens.ink,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = if (bill.isFreeDelivery) {
                                        "FREE delivery unlocked! You saved ₹15"
                                    } else {
                                        "Add ₹${bill.amountForFreeDelivery} more for FREE delivery (₹199 min)"
                                    },
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if (bill.isFreeDelivery) tokens.deliveryGreenDeep else tokens.ink
                                )
                            }
                            LinearProgressIndicator(
                                progress = { animatedProgress },
                                color = tokens.deliveryGreen,
                                trackColor = tokens.border,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(7.dp)
                                    .clip(RoundedCornerShape(50))
                            )
                        }
                    }
                }

                // Saved Address Selector
                item {
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = tokens.surface),
                        border = BorderStroke(1.dp, tokens.border),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Deliver To",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = tokens.ink
                                )
                                TextButton(
                                    onClick = { showAddAddressDialog = true },
                                    colors = ButtonDefaults.textButtonColors(contentColor = tokens.deliveryGreenDeep),
                                    modifier = Modifier.testTag("cart_add_address_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Add,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text("Add New")
                                }
                            }

                            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                items(uiState.savedAddresses, key = { it.id }) { addr ->
                                    val isSelected = addr.id == uiState.selectedAddress?.id
                                    val icon = when (addr.label) {
                                        "Home" -> Icons.Rounded.Home
                                        "Work" -> Icons.Rounded.Work
                                        else -> Icons.Rounded.LocationOn
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = if (isSelected) tokens.brandYellowSoft else tokens.surfaceElevated,
                                        border = BorderStroke(
                                            width = if (isSelected) 1.5.dp else 1.dp,
                                            color = if (isSelected) tokens.brandYellow else tokens.border
                                        ),
                                        modifier = Modifier
                                            .width(if (metrics.isNarrowPhone) 208.dp else 244.dp)
                                            .clip(RoundedCornerShape(14.dp))
                                            .clickable { onSelectAddress(addr) }
                                            .testTag("checkout_address_${addr.id}")
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(12.dp),
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Icon(
                                                    imageVector = icon,
                                                    contentDescription = null,
                                                    tint = tokens.ink,
                                                    modifier = Modifier.size(metrics.iconSmall)
                                                )
                                                Text(
                                                    text = addr.label,
                                                    style = MaterialTheme.typography.labelLarge,
                                                    color = tokens.ink
                                                )
                                            }
                                            Text(
                                                text = addr.fullAddress,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = tokens.muted,
                                                maxLines = 2
                                            )
                                            if (addr.formattedLocationLine.isNotBlank()) {
                                                Text(
                                                    text = addr.formattedLocationLine,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = tokens.ink.copy(alpha = 0.78f),
                                                    maxLines = 1
                                                )
                                            }
                                            addr.formattedCoordinates?.let { coords ->
                                                Text(
                                                    text = "Pin: $coords",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = tokens.deliveryGreenDeep
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Schedule Delivery Window & Time-Slot Picker
                item {
                    DeliveryWindowPickerCard(
                        selectedSlot = draft.selectedDeliverySlot,
                        expressEtaMinutes = uiState.selectedNeighborhood?.etaMinutes ?: 8,
                        onSelectSlot = onSelectDeliverySlot
                    )
                }

                // Cart Items List
                item {
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = tokens.surface),
                        border = BorderStroke(1.dp, tokens.border),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(metrics.cardPadding),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Basket Items",
                                style = MaterialTheme.typography.titleMedium,
                                color = tokens.ink
                            )
                            uiState.cartItems.forEachIndexed { idx, cartItem ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    ProductArtwork(
                                        artworkType = cartItem.product.artworkType,
                                        accentColor = cartItem.product.accentColor,
                                        modifier = Modifier.size(metrics.avatarSize)
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = cartItem.product.name,
                                            style = MaterialTheme.typography.titleSmall,
                                            color = tokens.ink,
                                            maxLines = 2
                                        )
                                        Text(
                                            text = "${cartItem.variant.label} • ₹${cartItem.variant.priceInr}/unit",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = tokens.muted
                                        )
                                    }

                                    // Quantity Stepper
                                    Surface(
                                        color = tokens.deliveryGreenDeep,
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            IconButton(
                                                onClick = { onDecrementCart(cartItem.product.id, cartItem.variant.id) },
                                                modifier = Modifier
                                                    .size(if (metrics.isNarrowPhone) 28.dp else 32.dp)
                                                    .testTag("cart_dec_${cartItem.product.id}")
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Rounded.Remove,
                                                    contentDescription = "Decrease quantity",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(metrics.iconSmall)
                                                )
                                            }
                                            Text(
                                                text = cartItem.quantity.toString(),
                                                style = MaterialTheme.typography.labelLarge,
                                                color = Color.White,
                                                modifier = Modifier.padding(horizontal = 3.dp)
                                            )
                                            IconButton(
                                                onClick = { onIncrementCart(cartItem.product.id, cartItem.variant.id) },
                                                modifier = Modifier
                                                    .size(if (metrics.isNarrowPhone) 28.dp else 32.dp)
                                                    .testTag("cart_inc_${cartItem.product.id}")
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Rounded.Add,
                                                    contentDescription = "Increase quantity",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(metrics.iconSmall)
                                                )
                                            }
                                        }
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "₹${cartItem.lineTotal}",
                                            style = MaterialTheme.typography.titleSmall,
                                            color = tokens.ink
                                        )
                                        IconButton(
                                            onClick = { onRemoveCartItem(cartItem.product.id, cartItem.variant.id) },
                                            modifier = Modifier
                                                .size(28.dp)
                                                .testTag("cart_remove_${cartItem.product.id}")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.DeleteOutline,
                                                contentDescription = "Remove item",
                                                tint = tokens.muted,
                                                modifier = Modifier.size(metrics.iconSmall)
                                            )
                                        }
                                    }
                                }
                                if (idx < uiState.cartItems.lastIndex) {
                                    HorizontalDivider(color = tokens.border)
                                }
                            }
                        }
                    }
                }

                // Promo Code Card
                item {
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = tokens.surface),
                        border = BorderStroke(1.dp, tokens.border),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.LocalOffer,
                                    contentDescription = null,
                                    tint = tokens.promoOrange,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "Promo Code",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = tokens.ink
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = draft.promoInput,
                                    onValueChange = onPromoInputChange,
                                    placeholder = { Text("Enter QUICK50 or FRESH20") },
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("promo_code_input")
                                )
                                if (draft.appliedPromo != null) {
                                    OutlinedButton(
                                        onClick = onRemovePromo,
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.height(52.dp)
                                    ) {
                                        Text("Remove", color = tokens.alertRed)
                                    }
                                } else {
                                    Button(
                                        onClick = { onApplyPromo(draft.promoInput) },
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = tokens.brandYellow,
                                            contentColor = tokens.onBrandYellow
                                        ),
                                        modifier = Modifier
                                            .height(52.dp)
                                            .testTag("apply_promo_button")
                                    ) {
                                        Text("Apply")
                                    }
                                }
                            }

                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(SeedData.availablePromos, key = { it.code }) { promo ->
                                    val applied = draft.appliedPromo?.code == promo.code
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (applied) tokens.deliveryGreenSoft else tokens.surfaceElevated,
                                        border = BorderStroke(
                                            1.dp,
                                            if (applied) tokens.deliveryGreen else tokens.border
                                        ),
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(10.dp))
                                            .clickable { onApplyPromo(promo.code) }
                                    ) {
                                        Text(
                                            text = "${promo.code} • ${promo.title}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (applied) tokens.deliveryGreenDeep else tokens.ink,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                        )
                                    }
                                }
                            }

                            if (draft.promoMessage != null) {
                                Text(
                                    text = draft.promoMessage,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (draft.appliedPromo != null) tokens.deliveryGreenDeep else tokens.alertRed
                                )
                            }
                        }
                    }
                }

                // Rider Tip Card
                item {
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = tokens.surface),
                        border = BorderStroke(1.dp, tokens.border),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "Tip Your Rider",
                                style = MaterialTheme.typography.titleMedium,
                                color = tokens.ink
                            )
                            Text(
                                text = "100% of your tip goes directly to your delivery partner.",
                                style = MaterialTheme.typography.bodySmall,
                                color = tokens.muted
                            )
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(0, 10, 20, 30, 50).forEach { tip ->
                                    val selected = !draft.isCustomTip && draft.selectedTip == tip
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (selected) tokens.brandYellow else tokens.surfaceElevated,
                                        border = BorderStroke(
                                            1.dp,
                                            if (selected) tokens.brandYellow else tokens.border
                                        ),
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .clickable { onSelectTip(tip) }
                                            .testTag("tip_chip_$tip")
                                    ) {
                                        Text(
                                            text = if (tip == 0) "No Tip" else "₹$tip",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = if (selected) tokens.onBrandYellow else tokens.ink,
                                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp)
                                        )
                                    }
                                }
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (draft.isCustomTip) tokens.brandYellow else tokens.surfaceElevated,
                                    border = BorderStroke(
                                        1.dp,
                                        if (draft.isCustomTip) tokens.brandYellow else tokens.border
                                    ),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable(onClick = onSelectCustomTipMode)
                                        .testTag("tip_chip_custom")
                                ) {
                                    Text(
                                        text = "Custom",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = if (draft.isCustomTip) tokens.onBrandYellow else tokens.ink,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp)
                                    )
                                }
                            }
                            if (draft.isCustomTip) {
                                OutlinedTextField(
                                    value = draft.customTipInput,
                                    onValueChange = onCustomTipChange,
                                    label = { Text("Custom Tip (₹)") },
                                    prefix = { Text("₹ ") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("custom_tip_input")
                                )
                            }
                        }
                    }
                }

                // Delivery Instructions & Rider Note
                item {
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = tokens.surface),
                        border = BorderStroke(1.dp, tokens.border),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "Delivery Instructions & Rider Note",
                                style = MaterialTheme.typography.titleMedium,
                                color = tokens.ink
                            )
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                SeedData.deliveryInstructionChips.forEach { chip ->
                                    val selected = chip in draft.selectedInstructions
                                    Surface(
                                        shape = RoundedCornerShape(50),
                                        color = if (selected) tokens.deliveryGreenSoft else tokens.surfaceElevated,
                                        border = BorderStroke(
                                            1.dp,
                                            if (selected) tokens.deliveryGreen else tokens.border
                                        ),
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(50))
                                            .clickable { onToggleInstruction(chip) }
                                            .testTag("instruction_chip_${chip.lowercase().replace(" ", "_")}")
                                    ) {
                                        Text(
                                            text = chip,
                                            style = MaterialTheme.typography.labelMedium,
                                            color = if (selected) tokens.deliveryGreenDeep else tokens.ink,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                        )
                                    }
                                }
                            }
                            OutlinedTextField(
                                value = draft.riderNote,
                                onValueChange = onRiderNoteChange,
                                label = { Text("Custom Delivery Note for Rider") },
                                placeholder = { Text("e.g., Gate code #402, leave beside shoe rack…") },
                                minLines = 3,
                                maxLines = 4,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("rider_note_input")
                            )
                        }
                    }
                }

                // Payment Method Selector
                item {
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = tokens.surface),
                        border = BorderStroke(1.dp, tokens.border),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Payment Method",
                                style = MaterialTheme.typography.titleMedium,
                                color = tokens.ink
                            )
                            PaymentMethod.entries.forEach { method ->
                                val selected = draft.paymentMethod == method
                                val icon = when (method) {
                                    PaymentMethod.WALLET -> Icons.Rounded.AccountBalanceWallet
                                    PaymentMethod.UPI -> Icons.Rounded.QrCode2
                                    PaymentMethod.CARD -> Icons.Rounded.CreditCard
                                    PaymentMethod.COD -> Icons.Rounded.Payments
                                }
                                val subtitle = if (method == PaymentMethod.WALLET) {
                                    "Balance: ₹${uiState.walletBalance} • Instant 1-tap checkout"
                                } else {
                                    method.subtitle
                                }
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (selected) tokens.brandYellowSoft else tokens.surfaceElevated,
                                    border = BorderStroke(
                                        1.dp,
                                        if (selected) tokens.brandYellow else tokens.border
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { onSelectPaymentMethod(method) }
                                        .testTag("payment_method_${method.name}")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = null,
                                            tint = tokens.ink
                                        )
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = method.label,
                                                style = MaterialTheme.typography.titleSmall,
                                                color = tokens.ink
                                            )
                                            Text(
                                                text = subtitle,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = tokens.muted
                                            )
                                        }
                                        RadioButton(
                                            selected = selected,
                                            onClick = { onSelectPaymentMethod(method) },
                                            colors = RadioButtonDefaults.colors(selectedColor = tokens.ink)
                                        )
                                    }
                                }
                            }
                            if (insufficientWallet) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = tokens.alertRed.copy(alpha = 0.1f),
                                    border = BorderStroke(1.dp, tokens.alertRed.copy(alpha = 0.4f)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("wallet_insufficient_banner")
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Need ₹$walletShortfall more in Wallet",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = tokens.alertRed,
                                            fontWeight = FontWeight.SemiBold,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Button(
                                            onClick = { topUpAmount = walletShortfall },
                                            shape = RoundedCornerShape(10.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = tokens.deliveryGreenDeep,
                                                contentColor = Color.White
                                            ),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                            modifier = Modifier.testTag("checkout_topup_wallet_button")
                                        ) {
                                            Text("+ Add ₹$walletShortfall", style = MaterialTheme.typography.labelSmall)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Bill Breakdown
                item {
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = tokens.surface),
                        border = BorderStroke(1.dp, tokens.border),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Bill Breakdown",
                                style = MaterialTheme.typography.titleMedium,
                                color = tokens.ink
                            )
                            BillRow("Item Total", "₹${bill.itemTotal}")
                            if (bill.mrpSavings > 0) {
                                BillRow("MRP Savings", "-₹${bill.mrpSavings}", isGreen = true)
                            }
                            BillRow(
                                "Delivery Fee",
                                if (bill.deliveryFee == 0) "FREE" else "₹${bill.deliveryFee}",
                                isGreen = bill.deliveryFee == 0
                            )
                            BillRow("Handling Charge", "₹${bill.handlingCharge}")
                            if (bill.promoDiscount > 0) {
                                BillRow("Promo Discount", "-₹${bill.promoDiscount}", isGreen = true)
                            }
                            if (bill.tipAmount > 0) {
                                BillRow("Rider Tip", "₹${bill.tipAmount}")
                            }
                            HorizontalDivider(color = tokens.border, modifier = Modifier.padding(vertical = 4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "To Pay",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = tokens.ink
                                )
                                Text(
                                    text = "₹${bill.grandTotal}",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = tokens.ink
                                )
                            }
                        }
                    }
                }
            }

            // Sticky Place Order Footer
            Surface(
                color = tokens.surface,
                shadowElevation = 8.dp,
                border = BorderStroke(1.dp, tokens.border),
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = {
                        // UPI / Card must be paid first; Wallet is debited and COD is collected at the door.
                        when (draft.paymentMethod) {
                            PaymentMethod.UPI, PaymentMethod.CARD -> showOrderPayment = true
                            PaymentMethod.WALLET, PaymentMethod.COD -> onPlaceOrder()
                        }
                    },
                    enabled = !insufficientWallet,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = tokens.deliveryGreenDeep,
                        contentColor = Color.White,
                        disabledContainerColor = tokens.border,
                        disabledContentColor = tokens.muted
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .height(54.dp)
                        .testTag("place_order_button")
                ) {
                    Text(
                        text = if (insufficientWallet) {
                            "Top Up ₹$walletShortfall to Pay with Wallet"
                        } else {
                            "Place Order • ₹${bill.grandTotal}"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    if (showAddAddressDialog) {
        AddAddressDialog(
            neighborhoods = uiState.neighborhoods,
            initialNeighborhoodId = uiState.selectedNeighborhood?.id ?: "indiranagar",
            onDismiss = { showAddAddressDialog = false },
            onSave = { label, full, landmark, nId, area, city, state, postal, country, lat, lng ->
                onAddAddress(label, full, landmark, nId, area, city, state, postal, country, lat, lng)
                showAddAddressDialog = false
            }
        )
    }

    topUpAmount?.let { amount ->
        WalletTopUpFlow(
            currentBalanceInr = uiState.walletBalance,
            presetAmountInr = amount,
            onDismiss = { topUpAmount = null },
            onPaymentSuccess = onAddWalletMoney
        )
    }

    if (showOrderPayment) {
        OrderPaymentFlow(
            amountInr = bill.grandTotal,
            preferCard = draft.paymentMethod == PaymentMethod.CARD,
            onDismiss = { showOrderPayment = false },
            onPaymentSuccess = {
                showOrderPayment = false
                onPlaceOrder()
            }
        )
    }
}

@Composable
private fun BillRow(label: String, value: String, isGreen: Boolean = false) {
    val tokens = QuickKartThemeTokens.colors
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = tokens.muted
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isGreen) FontWeight.Bold else FontWeight.SemiBold,
            color = if (isGreen) tokens.deliveryGreenDeep else tokens.ink
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DeliveryWindowPickerCard(
    selectedSlot: DeliverySlot,
    expressEtaMinutes: Int,
    onSelectSlot: (DeliverySlot) -> Unit
) {
    val tokens = QuickKartThemeTokens.colors
    val metrics = QuickKartMetricsTokens.metrics
    val dayTabs = remember { listOf("Now", "Today", "Tomorrow") }
    var activeDay by remember(selectedSlot.id) { mutableStateOf(selectedSlot.dayLabel) }
    val slotsForDay = remember(activeDay) {
        SeedData.deliverySlots.filter { it.dayLabel == activeDay }
    }

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = tokens.surface),
        border = BorderStroke(1.dp, tokens.border),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("delivery_window_picker_card")
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Schedule,
                        contentDescription = null,
                        tint = tokens.deliveryGreenDeep,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Delivery Window",
                        style = MaterialTheme.typography.titleMedium,
                        color = tokens.ink
                    )
                }
                Surface(
                    color = tokens.deliveryGreenSoft,
                    shape = RoundedCornerShape(50)
                ) {
                    Text(
                        text = if (selectedSlot.isExpress) {
                            "⚡ $expressEtaMinutes-Min Express"
                        } else {
                            selectedSlot.displaySummary
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = tokens.deliveryGreenDeep,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                    )
                }
            }

            // Day Tabs (Now / Today / Tomorrow)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                dayTabs.forEach { day ->
                    val isDaySelected = activeDay == day
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = if (isDaySelected) tokens.ink else tokens.surfaceElevated,
                        border = BorderStroke(
                            1.dp,
                            if (isDaySelected) tokens.ink else tokens.border
                        ),
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .clickable {
                                activeDay = day
                                val firstForDay = SeedData.deliverySlots.firstOrNull { it.dayLabel == day }
                                if (firstForDay != null && selectedSlot.dayLabel != day) {
                                    onSelectSlot(firstForDay)
                                }
                            }
                            .testTag("slot_day_${day.lowercase()}")
                    ) {
                        Text(
                            text = when (day) {
                                "Now" -> "⚡ Instant ($expressEtaMinutes min)"
                                else -> day
                            },
                            style = MaterialTheme.typography.labelMedium,
                            color = if (isDaySelected) tokens.background else tokens.ink,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                        )
                    }
                }
            }

            // Time-Slot Options for Selected Day
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                slotsForDay.forEach { slot ->
                    val isSelected = slot.id == selectedSlot.id
                    val displayWindow = if (slot.isExpress) {
                        "Arrives in ~$expressEtaMinutes mins"
                    } else {
                        slot.windowText
                    }
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSelected) tokens.brandYellowSoft else tokens.surfaceElevated,
                        border = BorderStroke(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = if (isSelected) tokens.brandYellow else tokens.border
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { onSelectSlot(slot) }
                            .testTag("delivery_slot_${slot.id}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = slot.title,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = tokens.ink,
                                        fontWeight = FontWeight.Bold
                                    )
                                    slot.badge?.let { badge ->
                                        Surface(
                                            color = if (slot.isExpress) tokens.deliveryGreenDeep else tokens.brandYellow,
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                text = badge,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = if (slot.isExpress) Color.White else tokens.onBrandYellow,
                                                fontWeight = FontWeight.ExtraBold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                                Text(
                                    text = displayWindow,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = tokens.muted
                                )
                            }

                            RadioButton(
                                selected = isSelected,
                                onClick = { onSelectSlot(slot) },
                                colors = RadioButtonDefaults.colors(selectedColor = tokens.ink)
                            )
                        }
                    }
                }
            }
        }
    }
}
