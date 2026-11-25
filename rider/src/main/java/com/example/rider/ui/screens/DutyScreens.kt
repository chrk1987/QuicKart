package com.example.rider.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.DeliveryDining
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.PinDrop
import androidx.compose.material.icons.rounded.Storefront
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.rider.data.ASSIGNMENT_WINDOW_MS
import com.example.rider.data.FLOATING_CASH_LIMIT
import com.example.rider.data.PaymentMode
import com.example.rider.data.TripOffer
import com.example.rider.ui.RiderUiState
import com.example.rider.ui.components.RiderMap
import com.example.ui.components.BadgeTone
import com.example.ui.components.BrandBlack
import com.example.ui.components.BrandSwitch
import com.example.ui.components.PrimaryButton
import com.example.ui.components.QkCard
import com.example.ui.components.SecondaryButton
import com.example.ui.components.StatusBadge
import com.example.ui.components.rupees
import com.example.ui.theme.QuickKartThemeTokens
import java.util.Locale

fun km(v: Double) = String.format(Locale.US, "%.1f km", v)

// ─────────────────────────────────────────────────────────────────────────────────────────────
// 2. Go Online & Zone Map (Home)
// ─────────────────────────────────────────────────────────────────────────────────────────────

@Composable
fun RiderHomeScreen(
    state: RiderUiState,
    onToggleOnline: (Boolean) -> Unit,
    onOpenTrip: () -> Unit,
    onOpenCash: () -> Unit,
    onOpenKyc: () -> Unit,
    onRequestOffer: () -> Unit
) {
    val tokens = QuickKartThemeTokens.colors
    val trip = state.activeTrip
    Column(Modifier.fillMaxSize()) {
        // Header: zone + status
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(Modifier.size(44.dp).background(tokens.brandYellow, CircleShape), contentAlignment = Alignment.Center) {
                Text(state.profile.name.take(1).ifBlank { "R" }.uppercase(), style = MaterialTheme.typography.titleMedium, color = tokens.onBrandYellow, fontWeight = FontWeight.ExtraBold)
            }
            Column(Modifier.weight(1f)) {
                Text("Hi, ${state.profile.name.substringBefore(" ").ifBlank { "Partner" }}", style = MaterialTheme.typography.titleLarge, color = tokens.ink)
                Text("Hyderabad · ${state.profile.preferredZones.joinToString().ifBlank { "No zone" }}", style = MaterialTheme.typography.bodySmall, color = tokens.muted, maxLines = 1)
            }
            Icon(Icons.Rounded.Notifications, null, tint = tokens.ink)
        }
        Box(Modifier.weight(1f).fillMaxWidth()) {
            RiderMap(
                rider = state.position,
                routeFrom = trip?.let { if (it.stage.ordinal <= 1) it.origin else it.store },
                routeTo = trip?.let { if (it.stage.ordinal <= 1) it.store else it.customer },
                modifier = Modifier.fillMaxSize()
            )
            if (state.isOnline && trip == null && state.offer == null) {
                Surface(color = BrandBlack, shape = RoundedCornerShape(50), modifier = Modifier.align(Alignment.TopCenter).padding(top = 12.dp)) {
                    Row(Modifier.padding(horizontal = 14.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(color = tokens.brandYellow, strokeWidth = 2.dp, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Finding orders near you…", style = MaterialTheme.typography.labelLarge, color = Color.White)
                    }
                }
            }
        }
        Column(
            Modifier.fillMaxWidth().background(tokens.background).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (!state.profile.kycVerified) {
                QkCard(onClick = onOpenKyc) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Icon(Icons.Rounded.Warning, null, tint = tokens.alertRed)
                        Text("Complete KYC to go online", style = MaterialTheme.typography.titleSmall, color = tokens.ink, modifier = Modifier.weight(1f))
                        StatusBadge("Open", BadgeTone.BRAND)
                    }
                }
            }
            if (trip != null) {
                QkCard(onClick = onOpenTrip, color = tokens.brandYellowSoft, borderColor = tokens.brandYellow, modifier = Modifier.testTag("active_trip_card")) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.DeliveryDining, null, tint = tokens.ink)
                        Spacer(Modifier.width(8.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Order ${trip.shortId} · ${trip.stage.label}", style = MaterialTheme.typography.titleSmall, color = tokens.ink)
                            Text(if (trip.stage.ordinal <= 1) trip.storeName else trip.customerName, style = MaterialTheme.typography.bodySmall, color = tokens.muted)
                        }
                        StatusBadge("Continue", BadgeTone.DARK)
                    }
                }
            }
            // Online / offline toggle
            QkCard(color = if (state.isOnline) tokens.brandYellow else tokens.surface, borderColor = if (state.isOnline) tokens.brandYellow else tokens.border) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(if (state.isOnline) "You are Online" else "You are Offline", style = MaterialTheme.typography.titleMedium, color = if (state.isOnline) tokens.onBrandYellow else tokens.ink, fontWeight = FontWeight.ExtraBold)
                        Text(
                            when {
                                state.overCashLimit -> "Cash limit reached — deposit to receive orders"
                                state.isOnline -> "Shift ${state.profile.shift.hours} · acceptance ${state.acceptanceRate}%"
                                else -> "Go online to start receiving orders"
                            },
                            style = MaterialTheme.typography.bodySmall, color = if (state.isOnline) tokens.onBrandYellow.copy(alpha = 0.8f) else tokens.muted
                        )
                    }
                    BrandSwitch(state.isOnline, onToggleOnline, modifier = Modifier.testTag("online_switch"), onBrandSurface = state.isOnline)
                }
            }
            Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = BrandBlack), modifier = Modifier.fillMaxWidth()) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Today's earnings", style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.75f))
                        Text(rupees(state.todayEarnings), style = MaterialTheme.typography.headlineMedium, color = tokens.brandYellow, fontWeight = FontWeight.ExtraBold)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Active trips  ${if (trip != null) 1 else 0}", style = MaterialTheme.typography.labelLarge, color = Color.White)
                        Text("Completed  ${state.todayCompleted}", style = MaterialTheme.typography.labelLarge, color = Color.White)
                    }
                }
            }
            QkCard(onClick = onOpenCash, contentPadding = PaddingValues(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Payments, null, tint = tokens.ink)
                    Spacer(Modifier.width(8.dp))
                    Text("Cash in hand", style = MaterialTheme.typography.titleSmall, color = tokens.ink, modifier = Modifier.weight(1f))
                    Text("${rupees(state.cashInHand)} / ${rupees(FLOATING_CASH_LIMIT)}", style = MaterialTheme.typography.titleSmall, color = if (state.overCashLimit) tokens.alertRed else tokens.ink)
                }
                CashBar(state.cashInHand)
            }
            if (state.isOnline && trip == null && state.offer == null) {
                TextButton(onClick = onRequestOffer, modifier = Modifier.testTag("request_offer")) {
                    Icon(Icons.Rounded.Bolt, null, tint = tokens.muted); Spacer(Modifier.width(4.dp)); Text("Get an order now (demo)", color = tokens.muted)
                }
            }
        }
    }
}

@Composable
fun CashBar(cash: Int) {
    val tokens = QuickKartThemeTokens.colors
    val frac = (cash.toFloat() / FLOATING_CASH_LIMIT).coerceIn(0f, 1f)
    Box(Modifier.fillMaxWidth().height(8.dp).background(tokens.surfaceElevated, RoundedCornerShape(50))) {
        Box(Modifier.fillMaxWidth(frac).height(8.dp).background(if (frac >= 1f) tokens.alertRed else if (frac > 0.75f) tokens.brandYellow else tokens.ink, RoundedCornerShape(50)))
    }
}

// ─────────────────────────────────────────────────────────────────────────────────────────────
// 3. New Assignment Alert
// ─────────────────────────────────────────────────────────────────────────────────────────────

@Composable
fun AssignmentDialog(offer: TripOffer, nowMillis: Long, onAccept: () -> Unit, onReject: () -> Unit) {
    val tokens = QuickKartThemeTokens.colors
    val t = offer.trip
    val left = ((offer.expiresAtMillis - nowMillis) / 1000).coerceAtLeast(0)
    Dialog(onDismissRequest = {}, properties = DialogProperties(usePlatformDefaultWidth = false, dismissOnBackPress = false, dismissOnClickOutside = false)) {
        Box(Modifier.fillMaxSize().navigationBarsPadding(), contentAlignment = Alignment.BottomCenter) {
            Surface(shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp), color = tokens.surface, modifier = Modifier.fillMaxWidth().testTag("assignment_dialog")) {
                Column {
                    Row(Modifier.fillMaxWidth().background(tokens.brandYellow).padding(horizontal = 18.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("NEW ASSIGNMENT", style = MaterialTheme.typography.labelMedium, color = tokens.onBrandYellow, fontWeight = FontWeight.ExtraBold)
                            Text("Earn ${rupees(t.payout + t.tip)}", style = MaterialTheme.typography.headlineSmall, color = tokens.onBrandYellow)
                        }
                        Box(contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(progress = { left * 1000f / ASSIGNMENT_WINDOW_MS }, color = BrandBlack, trackColor = BrandBlack.copy(alpha = 0.15f), strokeWidth = 5.dp, modifier = Modifier.size(56.dp))
                            Text("${left}s", style = MaterialTheme.typography.titleSmall, color = tokens.onBrandYellow, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                    Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        LegRow(Icons.Rounded.Storefront, "Pickup · ${km(t.pickupKm)}", t.storeName, t.storeAddress)
                        LegRow(Icons.Rounded.PinDrop, "Drop-off · ${km(t.dropKm)}", t.customerAddress.substringAfterLast(", "), "${t.itemCount} items")
                        HorizontalDivider(color = tokens.border)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            StatusBadge("Order ${t.shortId}", BadgeTone.NEUTRAL)
                            StatusBadge(t.paymentMode.label, if (t.paymentMode == PaymentMode.COD) BadgeTone.WARNING else BadgeTone.SUCCESS)
                            if (t.tip > 0) StatusBadge("+${rupees(t.tip)} tip", BadgeTone.SUCCESS)
                            if (t.incentive > 0) StatusBadge("+${rupees(t.incentive)} bonus", BadgeTone.BRAND)
                        }
                        Text("Payout: base ${rupees(t.basePay)} + distance ${rupees(t.distancePay)}${if (t.incentive > 0) " + bonus ${rupees(t.incentive)}" else ""}", style = MaterialTheme.typography.labelSmall, color = tokens.muted)
                        if (t.paymentMode == PaymentMode.COD) Text("Collect ${rupees(t.orderAmount)} cash from the customer.", style = MaterialTheme.typography.labelSmall, color = tokens.muted)
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            SecondaryButton("Reject", onClick = onReject, modifier = Modifier.weight(1f))
                            PrimaryButton("Accept (${rupees(t.payout + t.tip)})", onClick = onAccept, modifier = Modifier.weight(1.6f), testTag = "accept_assignment")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LegRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, title: String, sub: String) {
    val tokens = QuickKartThemeTokens.colors
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(Modifier.size(40.dp).background(tokens.brandYellowSoft, CircleShape), contentAlignment = Alignment.Center) { Icon(icon, null, tint = tokens.ink) }
        Column {
            Text(label, style = MaterialTheme.typography.labelMedium, color = tokens.muted)
            Text(title, style = MaterialTheme.typography.titleSmall, color = tokens.ink)
            Text(sub, style = MaterialTheme.typography.labelSmall, color = tokens.muted)
        }
    }
}
