package com.example.rider.ui.screens

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
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeliveryDining
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.Radar
import androidx.compose.material.icons.rounded.Storefront
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.rider.data.model.COD_FLOATING_LIMIT_INR
import com.example.rider.data.model.OFFER_TIMEOUT_SECONDS
import com.example.rider.data.model.RiderPaymentMode
import com.example.rider.data.model.RiderTripOrder
import com.example.rider.ui.RiderUiState
import com.example.rider.ui.theme.JetBrainsMonoFontFamily
import com.example.rider.ui.theme.RiderMetricsTokens
import com.example.rider.ui.theme.RiderThemeTokens

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DutyDashboardScreen(
    state: RiderUiState,
    onToggleOnline: (Boolean) -> Unit,
    onSelectHub: (String) -> Unit,
    onAcceptOffer: (String) -> Unit,
    onDeclineOffer: (String) -> Unit,
    onPingNewOffer: () -> Unit,
    onOpenActiveTrip: () -> Unit,
    onOpenEarnings: () -> Unit
) {
    val tokens = RiderThemeTokens.colors
    val metrics = RiderMetricsTokens.metrics
    val isOnline = state.prefs.isOnline

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(tokens.background),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .widthIn(max = 680.dp)
                .fillMaxSize(),
            contentPadding = PaddingValues(
                horizontal = metrics.screenHorizontalPadding,
                vertical = 14.dp
            ),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Shift Status Top Banner
            item {
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = if (isOnline) tokens.deliveryGreenDeep else tokens.surface,
                    border = BorderStroke(1.dp, if (isOnline) tokens.deliveryGreenDeep else tokens.border),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("duty_status_banner")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .background(
                                    if (isOnline) tokens.brandYellow else tokens.surfaceElevated,
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isOnline) Icons.Rounded.Radar else Icons.Rounded.DeliveryDining,
                                contentDescription = "Shift duty status",
                                tint = if (isOnline) tokens.onBrandYellow else tokens.muted,
                                modifier = Modifier.size(26.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isOnline) "You're Online • On Duty" else "You're Offline",
                                style = MaterialTheme.typography.titleMedium,
                                color = if (isOnline) Color.White else tokens.ink,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = "${state.currentHub.podName} • ${state.currentHub.demandLevel}",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isOnline) Color.White.copy(alpha = 0.88f) else tokens.muted
                            )
                        }

                        Switch(
                            checked = isOnline,
                            onCheckedChange = onToggleOnline,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = tokens.deliveryGreenDeep,
                                checkedTrackColor = tokens.brandYellow
                            ),
                            modifier = Modifier.testTag("duty_online_switch")
                        )
                    }
                }
            }

            // 2. COD Warning if floating cash >= ₹2,000
            if (state.earnings.isCodLimitExceeded) {
                item {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = tokens.alertRed.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, tokens.alertRed.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenEarnings() }
                            .testTag("cod_limit_warning_banner")
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.WarningAmber,
                                contentDescription = "COD limit alert",
                                tint = tokens.alertRed
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "COD Cash Limit Reached (₹${state.earnings.codCashInHandInr}/₹$COD_FLOATING_LIMIT_INR)",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = tokens.alertRed,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Settle cash at ${state.currentHub.podName} kiosk to keep priority dispatch.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = tokens.ink
                                )
                            }
                        }
                    }
                }
            }

            // 3. Today's KPI Summary Strip
            item {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    maxItemsInEachRow = 2
                ) {
                    RiderKpiTile(
                        title = "Today Earned",
                        value = "₹${state.earnings.totalEarnedInr}",
                        subtitle = "${state.earnings.completedDrops} drops done",
                        icon = Icons.Rounded.AccountBalanceWallet,
                        highlightColor = tokens.deliveryGreenDeep,
                        onClick = onOpenEarnings,
                        modifier = Modifier.weight(1f)
                    )
                    RiderKpiTile(
                        title = "COD Cash Held",
                        value = "₹${state.earnings.codCashInHandInr}",
                        subtitle = "SLA ${state.earnings.slaPercent}% on-time",
                        icon = Icons.Rounded.Payments,
                        highlightColor = if (state.earnings.isCodLimitExceeded) tokens.alertRed else tokens.promoOrange,
                        onClick = onOpenEarnings,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // 4. Active Run Shortcut Card OR Incoming Offer Radar Card
            val activeTrip = state.activeTrip
            val pendingOffer = state.pendingOffer

            if (activeTrip != null) {
                item {
                    Card(
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = tokens.brandYellowSoft),
                        border = BorderStroke(1.5.dp, tokens.brandYellow),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("active_run_resume_card")
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    color = tokens.deliveryGreenDeep,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "ACTIVE RUN • ${activeTrip.orderId}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                                Spacer(Modifier.weight(1f))
                                Text(
                                    text = "Earn ₹${activeTrip.totalRiderPayoutInr}",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = tokens.deliveryGreenDeep,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }

                            Text(
                                text = activeTrip.stage.label,
                                style = MaterialTheme.typography.titleLarge,
                                color = tokens.ink
                            )
                            Text(
                                text = "${activeTrip.hubName} → ${activeTrip.customerName} (${activeTrip.deliveryAddressLabel})",
                                style = MaterialTheme.typography.bodyMedium,
                                color = tokens.ink
                            )

                            Button(
                                onClick = onOpenActiveTrip,
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = tokens.deliveryGreenDeep,
                                    contentColor = Color.White
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .testTag("resume_active_trip_button")
                            ) {
                                Text("Continue Delivery Run", style = MaterialTheme.typography.labelLarge)
                                Spacer(Modifier.width(8.dp))
                                Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = null)
                            }
                        }
                    }
                }
            } else if (pendingOffer != null) {
                item {
                    IncomingOfferCard(
                        offer = pendingOffer,
                        secondsRemaining = state.offerSecondsRemaining,
                        onAccept = { onAcceptOffer(pendingOffer.orderId) },
                        onDecline = { onDeclineOffer(pendingOffer.orderId) }
                    )
                }
            } else {
                item {
                    Card(
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = tokens.surface),
                        border = BorderStroke(1.dp, tokens.border),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.NotificationsActive,
                                contentDescription = null,
                                tint = if (isOnline) tokens.deliveryGreenDeep else tokens.muted,
                                modifier = Modifier.size(36.dp)
                            )
                            Text(
                                text = if (isOnline) "Scanning for nearby Dark Store pings..." else "Go Online to receive orders",
                                style = MaterialTheme.typography.titleMedium,
                                color = tokens.ink,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Pod queue around ${state.currentHub.podName} updates in real time.",
                                style = MaterialTheme.typography.bodySmall,
                                color = tokens.muted
                            )
                            Button(
                                onClick = onPingNewOffer,
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = tokens.deliveryGreenDeep,
                                    contentColor = Color.White
                                ),
                                modifier = Modifier
                                    .defaultMinSize(minHeight = 48.dp)
                                    .testTag("ping_new_order_button")
                            ) {
                                Icon(Icons.Rounded.Bolt, contentDescription = null)
                                Spacer(Modifier.width(6.dp))
                                Text("Simulate Incoming Order Ping", style = MaterialTheme.typography.labelLarge)
                            }
                        }
                    }
                }
            }

            // 5. Switch Dark Store Pod Zone
            item {
                Text(
                    text = "Active Dark Store Zone",
                    style = MaterialTheme.typography.titleMedium,
                    color = tokens.ink,
                    fontWeight = FontWeight.Bold
                )
            }
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(state.availableHubs, key = { it.id }) { hub ->
                        val selected = hub.id == state.currentHub.id
                        FilterChip(
                            selected = selected,
                            onClick = { onSelectHub(hub.id) },
                            label = {
                                Text("${hub.neighborhood} (+₹${hub.surgeBonusInr})")
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Rounded.Storefront,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = tokens.deliveryGreenSoft,
                                selectedLabelColor = tokens.deliveryGreenDeep,
                                selectedLeadingIconColor = tokens.deliveryGreenDeep
                            ),
                            modifier = Modifier.testTag("zone_chip_${hub.id}")
                        )
                    }
                }
            }

            // 6. Next Milestone Incentive Preview
            state.earnings.nextIncentiveTier?.let { tier ->
                item {
                    val progress = (state.earnings.completedDrops.toFloat() / tier.targetDrops.toFloat())
                        .coerceIn(0f, 1f)
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
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${tier.label} Bonus • +₹${tier.rewardInr}",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = tokens.ink,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${state.earnings.completedDrops}/${tier.targetDrops} drops",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = tokens.deliveryGreenDeep,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            LinearProgressIndicator(
                                progress = { progress },
                                color = tokens.deliveryGreenDeep,
                                trackColor = tokens.surfaceElevated,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                            )
                            Text(
                                text = "Complete ${tier.targetDrops - state.earnings.completedDrops} more drops today to unlock ₹${tier.rewardInr} bonus.",
                                style = MaterialTheme.typography.bodySmall,
                                color = tokens.muted
                            )
                        }
                    }
                }
            }

            // Extra manual ping trigger even when an offer is active
            if (activeTrip == null && pendingOffer != null) {
                item {
                    OutlinedButton(
                        onClick = onPingNewOffer,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("ping_another_offer_button")
                    ) {
                        Icon(Icons.Rounded.Bolt, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text("Refresh / Ping Next Dispatch Offer")
                    }
                }
            }
        }
    }
}

@Composable
private fun IncomingOfferCard(
    offer: RiderTripOrder,
    secondsRemaining: Int,
    onAccept: () -> Unit,
    onDecline: () -> Unit
) {
    val tokens = RiderThemeTokens.colors
    val progress = (secondsRemaining.toFloat() / OFFER_TIMEOUT_SECONDS.toFloat()).coerceIn(0f, 1f)

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = tokens.surface),
        border = BorderStroke(2.dp, tokens.deliveryGreenDeep),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("incoming_offer_card")
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = tokens.brandYellow,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Rounded.Bolt,
                            contentDescription = null,
                            tint = tokens.onBrandYellow,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "NEW ORDER • ${offer.orderId}",
                            style = MaterialTheme.typography.labelMedium,
                            color = tokens.onBrandYellow,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }

                Surface(
                    color = if (secondsRemaining <= 8) tokens.alertRed.copy(alpha = 0.14f) else tokens.deliveryGreenSoft,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Rounded.Timer,
                            contentDescription = "Offer countdown",
                            tint = if (secondsRemaining <= 8) tokens.alertRed else tokens.deliveryGreenDeep,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "${secondsRemaining}s",
                            style = MaterialTheme.typography.labelMedium,
                            fontFamily = JetBrainsMonoFontFamily,
                            color = if (secondsRemaining <= 8) tokens.alertRed else tokens.deliveryGreenDeep,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            LinearProgressIndicator(
                progress = { progress },
                color = if (secondsRemaining <= 8) tokens.alertRed else tokens.deliveryGreenDeep,
                trackColor = tokens.surfaceElevated,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
            )

            // Estimated Payout Hero Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "ESTIMATED EARNING",
                        style = MaterialTheme.typography.labelSmall,
                        color = tokens.muted
                    )
                    Text(
                        text = "₹${offer.totalRiderPayoutInr}",
                        style = MaterialTheme.typography.displaySmall,
                        color = tokens.deliveryGreenDeep,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${offer.distanceKm} km • ${offer.estimatedMinutes} min",
                        style = MaterialTheme.typography.titleMedium,
                        color = tokens.ink,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Base ₹${offer.basePayInr} + Dist ₹${offer.distancePayInr} + Surge ₹${offer.surgeBonusInr}" +
                            if (offer.tipInr > 0) " + Tip ₹${offer.tipInr}" else "",
                        style = MaterialTheme.typography.labelSmall,
                        color = tokens.muted
                    )
                }
            }

            HorizontalDivider(color = tokens.border)

            // Pickup -> Drop details
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Storefront,
                    contentDescription = null,
                    tint = tokens.ink,
                    modifier = Modifier.size(20.dp)
                )
                Column {
                    Text(
                        text = "PICKUP • ${offer.hubName} (Bay ${offer.pickupBayCode})",
                        style = MaterialTheme.typography.labelMedium,
                        color = tokens.ink,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${offer.totalUnits} items • Bag Seal ${offer.bagSealCode}",
                        style = MaterialTheme.typography.bodySmall,
                        color = tokens.muted
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.LocationOn,
                    contentDescription = null,
                    tint = tokens.deliveryGreenDeep,
                    modifier = Modifier.size(20.dp)
                )
                Column {
                    Text(
                        text = "DROP • ${offer.customerName} (${offer.deliveryAddressLabel})",
                        style = MaterialTheme.typography.labelMedium,
                        color = tokens.ink,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = offer.deliveryAddressFull,
                        style = MaterialTheme.typography.bodySmall,
                        color = tokens.muted
                    )
                }
            }

            Surface(
                color = if (offer.paymentMode == RiderPaymentMode.COD) {
                    tokens.promoOrange.copy(alpha = 0.14f)
                } else {
                    tokens.deliveryGreenSoft
                },
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Inventory2,
                        contentDescription = null,
                        tint = if (offer.paymentMode == RiderPaymentMode.COD) tokens.promoOrange else tokens.deliveryGreenDeep,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = if (offer.paymentMode == RiderPaymentMode.COD) {
                            "Collect ₹${offer.collectAmountInr} Cash on Delivery (COD)"
                        } else {
                            "${offer.paymentMode.label} • No cash to collect"
                        },
                        style = MaterialTheme.typography.labelMedium,
                        color = tokens.ink,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onDecline,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(0.36f)
                        .height(50.dp)
                        .testTag("decline_offer_button")
                ) {
                    Icon(Icons.Rounded.Close, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("Decline", style = MaterialTheme.typography.labelLarge)
                }
                Button(
                    onClick = onAccept,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = tokens.deliveryGreenDeep,
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .weight(0.64f)
                        .height(50.dp)
                        .testTag("accept_offer_button")
                ) {
                    Icon(Icons.Rounded.CheckCircle, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("Accept Order", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

@Composable
private fun RiderKpiTile(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    highlightColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tokens = RiderThemeTokens.colors
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = tokens.surface),
        border = BorderStroke(1.dp, tokens.border),
        modifier = modifier
            .defaultMinSize(minHeight = 94.dp)
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = highlightColor,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = tokens.muted
                )
            }
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                color = tokens.ink,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = highlightColor,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
