package com.example.rider.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.material.icons.rounded.Badge
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.TextFields
import androidx.compose.material.icons.rounded.TwoWheeler
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.rider.BuildConfig
import com.example.rider.data.CashEntryType
import com.example.rider.data.DocStatus
import com.example.rider.data.FLOATING_CASH_LIMIT
import com.example.rider.data.HYDERABAD_ZONES
import com.example.rider.data.INSTANT_PAYOUT_MIN
import com.example.rider.data.KycDoc
import com.example.rider.data.RiderProfile
import com.example.rider.data.RiderSeed
import com.example.rider.data.RiderSettings
import com.example.rider.data.Shift
import com.example.rider.data.ThemeMode
import com.example.rider.data.TripStage
import com.example.rider.ui.RiderUiState
import com.example.ui.components.AmountRow
import com.example.ui.components.BadgeTone
import com.example.ui.components.BrandBlack
import com.example.ui.components.ChoiceChip
import com.example.ui.components.EmptyState
import com.example.ui.components.PrimaryButton
import com.example.ui.components.QkCard
import com.example.ui.components.QkTextField
import com.example.ui.components.ScreenHeader
import com.example.ui.components.SecondaryButton
import com.example.ui.components.SegmentedChoice
import com.example.ui.components.SettingRow
import com.example.ui.components.StatusBadge
import com.example.ui.components.TrendChart
import com.example.ui.components.rupees
import com.example.ui.components.timeAgo
import com.example.ui.theme.QuickKartThemeTokens
import kotlinx.coroutines.delay

// ─────────────────────────────────────────────────────────────────────────────────────────────
// 8. COD Collection & Cash Ledger
// ─────────────────────────────────────────────────────────────────────────────────────────────

@Composable
fun CashLedgerScreen(state: RiderUiState, onBack: () -> Unit, onDeposit: (String) -> Unit) {
    val tokens = QuickKartThemeTokens.colors
    var depositing by remember { mutableStateOf(false) }
    val lastCollected = state.cash.firstOrNull { it.type == CashEntryType.COLLECTED }
    LazyColumn(contentPadding = PaddingValues(bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { ScreenHeader("Cash Ledger", onBack = onBack, subtitle = "Cash on delivery collections") }
        if (lastCollected != null) item {
            QkCard(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text("Last collection", style = MaterialTheme.typography.labelMedium, color = tokens.muted)
                Text(rupees(lastCollected.amount), style = MaterialTheme.typography.headlineSmall, color = tokens.ink, fontWeight = FontWeight.ExtraBold)
                Text("${lastCollected.ref} · ${timeAgo(lastCollected.atMillis, state.nowMillis)}", style = MaterialTheme.typography.labelSmall, color = tokens.muted)
            }
        }
        item {
            QkCard(modifier = Modifier.padding(horizontal = 16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Floating cash limit", style = MaterialTheme.typography.titleSmall, color = tokens.ink, modifier = Modifier.weight(1f))
                    Text("${rupees(state.cashInHand)} / ${rupees(FLOATING_CASH_LIMIT)}", style = MaterialTheme.typography.titleSmall, color = if (state.overCashLimit) tokens.alertRed else tokens.ink)
                }
                CashBar(state.cashInHand)
                if (state.overCashLimit) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Warning, null, tint = tokens.alertRed, modifier = Modifier.size(16.dp)); Spacer(Modifier.width(6.dp))
                        Text("Limit reached — new orders are paused until you deposit.", style = MaterialTheme.typography.labelSmall, color = tokens.alertRed)
                    }
                } else Text("Deposit before you reach the limit to keep receiving orders.", style = MaterialTheme.typography.labelSmall, color = tokens.muted)
            }
        }
        item {
            Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = BrandBlack), modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth()) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Cash in hand", style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.75f))
                    Text(rupees(state.cashInHand), style = MaterialTheme.typography.headlineMedium, color = tokens.brandYellow, fontWeight = FontWeight.ExtraBold)
                    PrimaryButton("Deposit COD via UPI", onClick = { depositing = true }, enabled = state.cashInHand > 0, icon = Icons.Rounded.Payments, testTag = "deposit_cod")
                }
            }
        }
        item { Text("History", style = MaterialTheme.typography.titleMedium, color = tokens.ink, modifier = Modifier.padding(horizontal = 16.dp)) }
        items(state.cash, key = { it.id }) { e ->
            QkCard(modifier = Modifier.padding(horizontal = 16.dp), contentPadding = PaddingValues(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(if (e.type == CashEntryType.COLLECTED) "Collected" else "Deposited", style = MaterialTheme.typography.titleSmall, color = tokens.ink)
                        Text("${e.ref} · ${timeAgo(e.atMillis, state.nowMillis)}", style = MaterialTheme.typography.labelSmall, color = tokens.muted)
                    }
                    Text((if (e.type == CashEntryType.COLLECTED) "+ " else "− ") + rupees(e.amount), style = MaterialTheme.typography.titleSmall,
                        color = if (e.type == CashEntryType.COLLECTED) tokens.ink else tokens.deliveryGreenDeep)
                }
            }
        }
    }
    if (depositing) UpiDepositDialog(amount = state.cashInHand, onDismiss = { depositing = false }, onSuccess = { ref -> depositing = false; onDeposit(ref) })
}

/** Simulated UPI collect flow: choose app / UPI ID → processing → success. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun UpiDepositDialog(amount: Int, onDismiss: () -> Unit, onSuccess: (String) -> Unit) {
    val tokens = QuickKartThemeTokens.colors
    var step by remember { mutableIntStateOf(0) } // 0 choose, 1 processing
    var upiId by remember { mutableStateOf("") }
    var app by remember { mutableStateOf<String?>("Any UPI app") }
    val upiValid = app != null || Regex("^[a-zA-Z0-9.\\-_]{2,}@[a-zA-Z]{2,}$").matches(upiId)
    if (step == 1) LaunchedEffect(Unit) { delay(2_000); onSuccess("${(1000..9999).random()}-${(10..99).random()}") }
    AlertDialog(
        onDismissRequest = { if (step == 0) onDismiss() },
        title = { Text(if (step == 0) "Deposit ${rupees(amount)}" else "Waiting for payment…") },
        text = {
            if (step == 0) Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Pay QuickKart's collection account (merchant: QUICKKART COD). Your cash in hand resets once paid.", style = MaterialTheme.typography.bodySmall, color = tokens.muted)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Any UPI app", "Enter UPI ID").forEach { a -> ChoiceChip(a, (a == "Enter UPI ID" && app == null) || app == a, { app = if (a == "Enter UPI ID") null else a }) }
                }
                if (app == null) QkTextField(upiId, { upiId = it.trim().take(40) }, "Your UPI ID", placeholder = "name@bank", error = if (upiId.isNotEmpty() && !upiValid) "e.g. name@okbank" else null)
            } else Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                CircularProgressIndicator(color = tokens.brandYellow, modifier = Modifier.size(28.dp))
                Text("Approve the ${rupees(amount)} request in your UPI app (simulated).", style = MaterialTheme.typography.bodySmall)
            }
        },
        confirmButton = { if (step == 0) TextButton(enabled = upiValid, onClick = { step = 1 }) { Text("Pay ${rupees(amount)}", color = tokens.ink, fontWeight = FontWeight.Bold) } },
        dismissButton = { if (step == 0) TextButton(onClick = onDismiss) { Text("Cancel", color = tokens.muted) } }
    )
}

// ─────────────────────────────────────────────────────────────────────────────────────────────
// 10. Trip History & Cancellation
// ─────────────────────────────────────────────────────────────────────────────────────────────

enum class TripFilter(val label: String) { ALL("All"), COMPLETED("Completed"), CANCELLED("Cancelled") }

@Composable
fun TripHistoryScreen(state: RiderUiState) {
    val tokens = QuickKartThemeTokens.colors
    var filter by rememberSaveable { mutableStateOf(TripFilter.ALL) }
    val trips = state.history.filter {
        when (filter) {
            TripFilter.ALL -> true
            TripFilter.COMPLETED -> it.stage == TripStage.DELIVERED
            TripFilter.CANCELLED -> it.stage != TripStage.DELIVERED
        }
    }
    Column(Modifier.fillMaxSize()) {
        ScreenHeader("Trip History", onBack = null, subtitle = "${state.history.count { it.stage == TripStage.DELIVERED }} completed · ${state.history.count { it.stage != TripStage.DELIVERED }} cancelled/failed")
        SegmentedChoice(TripFilter.entries, filter, { it.label }, { filter = it }, Modifier.padding(horizontal = 16.dp))
        LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            if (trips.isEmpty()) item { EmptyState(Icons.Rounded.TwoWheeler, "No trips", "Your trips will appear here.") }
            items(trips, key = { it.orderId + it.completedAtMillis }) { t ->
                val done = t.stage == TripStage.DELIVERED
                QkCard(contentPadding = PaddingValues(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(if (done) Icons.Rounded.CheckCircle else Icons.Rounded.Warning, null, tint = if (done) tokens.deliveryGreenDeep else tokens.alertRed, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Order ${t.shortId}", style = MaterialTheme.typography.titleSmall, color = tokens.ink)
                            Text("${t.storeName} → ${t.customerAddress.substringAfterLast(", ")} · ${km(t.totalKm)}", style = MaterialTheme.typography.labelSmall, color = tokens.muted, maxLines = 1)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(rupees(t.totalEarning), style = MaterialTheme.typography.titleSmall, color = tokens.ink)
                            StatusBadge(t.stage.label, if (done) BadgeTone.SUCCESS else BadgeTone.DANGER)
                        }
                    }
                    if (done) {
                        HorizontalDivider(color = tokens.border)
                        AmountRow("Trip pay (base + distance)", rupees(t.basePay + t.distancePay))
                        if (t.incentive > 0) AmountRow("Incentive", rupees(t.incentive))
                        if (t.tip > 0) AmountRow("Customer tip", rupees(t.tip), valueColor = tokens.deliveryGreenDeep)
                    } else {
                        Text("Reason: ${t.cancelReason ?: "—"}" + if (t.totalEarning > 0) " · base pay ${rupees(t.totalEarning)}" else "", style = MaterialTheme.typography.labelSmall, color = tokens.muted)
                    }
                    t.completedAtMillis?.let { Text(timeAgo(it, state.nowMillis), style = MaterialTheme.typography.labelSmall, color = tokens.muted) }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────────────────────
// 11. Earnings & Instant Payout
// ─────────────────────────────────────────────────────────────────────────────────────────────

@Composable
fun EarningsScreen(state: RiderUiState, onInstantPayout: () -> Unit, onOpenCash: () -> Unit) {
    val tokens = QuickKartThemeTokens.colors
    var confirm by remember { mutableStateOf(false) }
    val daily = listOf("Mon" to 520, "Tue" to 610, "Wed" to 480, "Thu" to 700, "Fri" to 640, "Sat" to 860) + ("Today" to state.todayEarnings)
    val remainingTrips = (RiderSeed.WEEKLY_TARGET_TRIPS - state.weekTrips).coerceAtLeast(0)
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        ScreenHeader("Earnings & Payouts", onBack = null)
        Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = BrandBlack), modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth()) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("This week", style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.75f))
                Text(rupees(state.weekTotal), style = MaterialTheme.typography.headlineMedium, color = tokens.brandYellow, fontWeight = FontWeight.ExtraBold)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    listOf("Trips" to state.weekTripPay, "Incentives" to state.weekIncentives, "Tips" to state.weekTips).forEach { (l, v) ->
                        Column(Modifier.weight(1f).background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp)).padding(10.dp)) {
                            Text(rupees(v), style = MaterialTheme.typography.titleSmall, color = Color.White, fontWeight = FontWeight.Bold)
                            Text(l, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f))
                        }
                    }
                }
            }
        }
        QkCard(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text("Daily earnings", style = MaterialTheme.typography.titleSmall, color = tokens.ink)
            TrendChart(daily.map { it.second }, daily.map { it.first.take(3) }, bars = true)
        }
        QkCard(modifier = Modifier.padding(horizontal = 16.dp), color = tokens.brandYellowSoft, borderColor = tokens.brandYellow) {
            Text(if (remainingTrips == 0) "Weekly bonus unlocked: ${rupees(RiderSeed.WEEKLY_TARGET_BONUS)}" else "$remainingTrips more trips for a ${rupees(RiderSeed.WEEKLY_TARGET_BONUS)} weekly bonus",
                style = MaterialTheme.typography.titleSmall, color = tokens.ink)
            Box(Modifier.fillMaxWidth().height(8.dp).background(tokens.surface, RoundedCornerShape(50))) {
                Box(Modifier.fillMaxWidth((state.weekTrips.toFloat() / RiderSeed.WEEKLY_TARGET_TRIPS).coerceIn(0f, 1f)).height(8.dp).background(BrandBlack, RoundedCornerShape(50)))
            }
            Text("${state.weekTrips} / ${RiderSeed.WEEKLY_TARGET_TRIPS} trips · resets Monday", style = MaterialTheme.typography.labelSmall, color = tokens.muted)
        }
        QkCard(modifier = Modifier.padding(horizontal = 16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Available for instant payout", style = MaterialTheme.typography.labelMedium, color = tokens.muted)
                    Text(rupees(state.withdrawable), style = MaterialTheme.typography.headlineSmall, color = tokens.ink, fontWeight = FontWeight.ExtraBold)
                }
                StatusBadge("Weekly payout: Mon", BadgeTone.NEUTRAL)
            }
            PrimaryButton("Instant Payout", onClick = { confirm = true }, enabled = state.canInstantPayout, testTag = "instant_payout")
            Text(
                when {
                    state.profile.status(KycDoc.BANK) != DocStatus.VERIFIED -> "Verify your bank account to withdraw."
                    state.withdrawable < INSTANT_PAYOUT_MIN -> "Minimum ${rupees(INSTANT_PAYOUT_MIN)} to withdraw instantly."
                    state.nowMillis - state.lastInstantPayoutAt < 24 * 3_600_000L -> "One instant payout per day. Next available tomorrow."
                    else -> "Free · arrives in minutes to ${state.profile.bankMasked}."
                }, style = MaterialTheme.typography.labelSmall, color = tokens.muted
            )
        }
        QkCard(onClick = onOpenCash, modifier = Modifier.padding(horizontal = 16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Payments, null, tint = tokens.ink); Spacer(Modifier.width(8.dp))
                Text("COD cash in hand", style = MaterialTheme.typography.titleSmall, color = tokens.ink, modifier = Modifier.weight(1f))
                Text(rupees(state.cashInHand), style = MaterialTheme.typography.titleSmall, color = if (state.overCashLimit) tokens.alertRed else tokens.ink)
            }
            Text("Cash collected is not part of your earnings — deposit it to QuickKart.", style = MaterialTheme.typography.labelSmall, color = tokens.muted)
        }
        Text("Recent transactions", style = MaterialTheme.typography.titleMedium, color = tokens.ink, modifier = Modifier.padding(horizontal = 16.dp))
        val txs = state.payoutsTaken.map { Triple("Instant payout", -it.amount, it.atMillis) } +
            state.history.filter { it.totalEarning > 0 }.take(8).map { Triple("Order ${it.shortId}" + if (it.tip > 0 && it.stage == TripStage.DELIVERED) " (incl. tip)" else "", it.totalEarning, it.completedAtMillis ?: 0L) }
        txs.sortedByDescending { it.third }.take(10).forEach { (label, amt, at) ->
            Row(Modifier.padding(horizontal = 16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(label, style = MaterialTheme.typography.bodyMedium, color = tokens.ink)
                    Text(timeAgo(at, state.nowMillis), style = MaterialTheme.typography.labelSmall, color = tokens.muted)
                }
                Text((if (amt < 0) "− " else "+ ") + rupees(kotlin.math.abs(amt)), style = MaterialTheme.typography.titleSmall, color = if (amt < 0) tokens.ink else tokens.deliveryGreenDeep)
            }
        }
    }
    if (confirm) {
        AlertDialog(
            onDismissRequest = { confirm = false },
            title = { Text("Withdraw ${rupees(state.withdrawable)}?") },
            text = { Text("It will be sent to ${state.profile.bankMasked} (${state.profile.ifsc}). You can make one instant payout per day.") },
            confirmButton = { TextButton(onClick = { confirm = false; onInstantPayout() }) { Text("Withdraw", color = tokens.ink, fontWeight = FontWeight.Bold) } },
            dismissButton = { TextButton(onClick = { confirm = false }) { Text("Cancel", color = tokens.muted) } }
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────────────────────
// 12. Profile, Zones & SOS
// ─────────────────────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RiderProfileScreen(
    state: RiderUiState,
    onUpdateProfile: ((RiderProfile) -> RiderProfile) -> Unit,
    onUpdateSettings: ((RiderSettings) -> RiderSettings) -> Unit,
    onOpenKyc: () -> Unit,
    onSos: () -> Unit,
    onCancelSos: () -> Unit,
    onSignOut: () -> Boolean
) {
    val tokens = QuickKartThemeTokens.colors
    val context = LocalContext.current
    val p = state.profile
    var confirmSos by remember { mutableStateOf(false) }
    var confirmSignOut by remember { mutableStateOf(false) }
    var zoneError by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (state.sosActive) {
            QkCard(color = tokens.alertRed, borderColor = tokens.alertRed) {
                Text("SOS ACTIVE", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.ExtraBold)
                Text("Our safety team has your live location and is calling you.", style = MaterialTheme.typography.bodySmall, color = Color.White)
                TextButton(onClick = onCancelSos) { Text("I'm safe — cancel SOS", color = Color.White, fontWeight = FontWeight.Bold) }
            }
        }
        Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = BrandBlack), modifier = Modifier.fillMaxWidth()) {
            Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(Modifier.size(56.dp).background(tokens.brandYellow, CircleShape), contentAlignment = Alignment.Center) {
                    Text(p.name.take(1).ifBlank { "R" }.uppercase(), style = MaterialTheme.typography.headlineSmall, color = tokens.onBrandYellow, fontWeight = FontWeight.ExtraBold)
                }
                Column(Modifier.weight(1f)) {
                    Text(p.name.ifBlank { "Rider" }, style = MaterialTheme.typography.titleMedium, color = Color.White)
                    Text("${p.vehicleType.label} · ${p.vehicleNumber.ifBlank { "—" }}", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.75f))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Star, null, tint = tokens.brandYellow, modifier = Modifier.size(16.dp))
                        Text(" ${p.rating} rating · ${state.acceptanceRate}% acceptance", style = MaterialTheme.typography.labelMedium, color = Color.White)
                    }
                }
            }
        }
        QkCard {
            Text("Preferred zones (Hyderabad)", style = MaterialTheme.typography.titleSmall, color = tokens.ink)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                HYDERABAD_ZONES.forEach { z ->
                    val on = z in p.preferredZones
                    ChoiceChip(z, on, {
                        if (on && p.preferredZones.size == 1) zoneError = true
                        else { zoneError = false; onUpdateProfile { it.copy(preferredZones = if (on) it.preferredZones - z else it.preferredZones + z) } }
                    })
                }
            }
            Text(if (zoneError) "Keep at least one zone." else "Orders are offered mostly from these zones.", style = MaterialTheme.typography.labelSmall, color = if (zoneError) tokens.alertRed else tokens.muted)
        }
        QkCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Schedule, null, tint = tokens.ink, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(6.dp))
                Text("Shift timings", style = MaterialTheme.typography.titleSmall, color = tokens.ink, modifier = Modifier.weight(1f))
                StatusBadge("Active: ${p.shift.hours}", BadgeTone.SUCCESS)
            }
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Shift.entries.forEach { s -> ChoiceChip(s.label, p.shift == s, { onUpdateProfile { it.copy(shift = s) } }) }
            }
            if (state.activeTrip != null) Text("Shift changes apply after your current trip.", style = MaterialTheme.typography.labelSmall, color = tokens.muted)
        }
        QkCard {
            Text("Account", style = MaterialTheme.typography.titleSmall, color = tokens.ink)
            SettingRow(Icons.Rounded.Badge, "Documents & KYC", "${p.requiredDocs.count { p.status(it) == DocStatus.VERIFIED }}/${p.requiredDocs.size} verified", onClick = onOpenKyc)
            SettingRow(Icons.Rounded.AccountBalance, "Bank account", "${p.bankMasked} · ${p.ifsc.ifBlank { "—" }}", onClick = onOpenKyc)
            SettingRow(Icons.Rounded.TwoWheeler, "Vehicle", "${p.vehicleType.label} ${p.vehicleNumber}", onClick = onOpenKyc)
        }
        QkCard {
            Text("Appearance", style = MaterialTheme.typography.titleSmall, color = tokens.ink)
            Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Rounded.DarkMode, null, tint = tokens.ink, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(6.dp)); Text("Theme", style = MaterialTheme.typography.bodyMedium, color = tokens.ink) }
            SegmentedChoice(ThemeMode.entries, state.settings.themeMode, { it.label }, { m -> onUpdateSettings { it.copy(themeMode = m) } })
            Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Rounded.TextFields, null, tint = tokens.ink, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(6.dp)); Text("Text & display size", style = MaterialTheme.typography.bodyMedium, color = tokens.ink) }
            val presets = listOf(85 to "Compact", 100 to "Default", 115 to "Large", 130 to "Extra large")
            SegmentedChoice(presets.map { it.first }, presets.map { it.first }.minByOrNull { kotlin.math.abs(it - state.settings.zoomPercent) } ?: 100,
                { z -> presets.first { it.first == z }.second }, { z -> onUpdateSettings { it.copy(zoomPercent = z) } })
        }
        // SOS — red is reserved for emergency / destructive actions
        Button(
            onClick = { confirmSos = true },
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = tokens.alertRed, contentColor = Color.White),
            modifier = Modifier.fillMaxWidth().height(56.dp).testTag("sos_button")
        ) {
            Icon(Icons.Rounded.Warning, null); Spacer(Modifier.width(8.dp))
            Text("SOS EMERGENCY", fontWeight = FontWeight.ExtraBold)
        }
        SecondaryButton("Sign out", onClick = { confirmSignOut = true }, destructive = true, icon = Icons.AutoMirrored.Rounded.Logout, modifier = Modifier.fillMaxWidth())
        Text("QuickKart Rider v${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})", style = MaterialTheme.typography.labelSmall, color = tokens.muted, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
    }

    if (confirmSos) {
        AlertDialog(
            onDismissRequest = { confirmSos = false },
            title = { Text("Emergency SOS") },
            text = { Text("We'll share your live location with the QuickKart safety team and call you right away. For police, fire or ambulance, call 112.") },
            confirmButton = { TextButton(onClick = { confirmSos = false; onSos() }) { Text("Send SOS", color = tokens.alertRed, fontWeight = FontWeight.Bold) } },
            dismissButton = {
                Row {
                    TextButton(onClick = { confirmSos = false; runCatching { context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:112"))) } }) { Text("Call 112", color = tokens.ink) }
                    TextButton(onClick = { confirmSos = false }) { Text("Cancel", color = tokens.muted) }
                }
            }
        )
    }
    if (confirmSignOut) {
        AlertDialog(
            onDismissRequest = { confirmSignOut = false },
            title = { Text("Sign out?") },
            text = { Text(if (state.activeTrip != null) "You have an active trip. Finish or cancel it first." else "You'll go offline and need an OTP to sign back in.") },
            confirmButton = { TextButton(onClick = { confirmSignOut = false; onSignOut() }) { Text("Sign out", color = tokens.alertRed) } },
            dismissButton = { TextButton(onClick = { confirmSignOut = false }) { Text("Cancel", color = tokens.ink) } }
        )
    }
}
