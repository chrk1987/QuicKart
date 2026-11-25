package com.example.rider.ui.screens

import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Navigation
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.FastForward
import androidx.compose.material.icons.rounded.HourglassTop
import androidx.compose.material.icons.rounded.PinDrop
import androidx.compose.material.icons.rounded.ReportProblem
import androidx.compose.material.icons.rounded.Storefront
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.rider.data.CANCEL_REASONS
import com.example.rider.data.FAILED_REASONS
import com.example.rider.data.PaymentMode
import com.example.rider.data.StoreReadiness
import com.example.rider.data.Trip
import com.example.rider.data.TripStage
import com.example.rider.ui.components.PinKind
import com.example.rider.ui.components.RiderMap
import com.example.rider.ui.components.SwipeToConfirm
import com.example.rider.ui.components.dial
import com.example.rider.ui.components.openNavigation
import com.example.rider.data.GeoPoint
import com.example.ui.components.AmountRow
import com.example.ui.components.BadgeTone
import com.example.ui.components.BrandBlack
import com.example.ui.components.ChoiceChip
import com.example.ui.components.OtpBoxes
import com.example.ui.components.PrimaryButton
import com.example.ui.components.QkCard
import com.example.ui.components.QkTextField
import com.example.ui.components.ScreenHeader
import com.example.ui.components.SecondaryButton
import com.example.ui.components.StatusBadge
import com.example.ui.components.rupees
import com.example.ui.theme.QuickKartThemeTokens
import com.example.ui.theme.zoomScaledControl

/** Hosts the active trip and shows the screen for its current stage. */
@Composable
fun ActiveTripScreen(
    trip: Trip,
    rider: GeoPoint,
    onBack: () -> Unit,
    onArrivedStore: () -> Unit,
    onTogglePicked: (String) -> Unit,
    onConfirmPickup: () -> Unit,
    onArrivedCustomer: () -> Unit,
    onCalledCustomer: () -> Unit,
    onPhotoTaken: () -> Unit,
    onCollectCash: () -> Unit,
    onComplete: (String?) -> Boolean,
    onCancel: (String) -> Unit,
    onReportIssue: () -> Unit,
    onSkip: () -> Unit
) {
    var showCancel by remember { mutableStateOf(false) }
    val canCancel = trip.stage == TripStage.TO_STORE || trip.stage == TripStage.AT_STORE
    Column(Modifier.fillMaxSize()) {
        ScreenHeader(
            title = "Order ${trip.shortId}",
            subtitle = trip.stage.label,
            onBack = onBack,
            actions = {
                if (canCancel) TextButton(onClick = { showCancel = true }, modifier = Modifier.testTag("cancel_trip")) { Text("Cancel", color = QuickKartThemeTokens.colors.alertRed) }
                else TextButton(onClick = onReportIssue) { Text("Report issue", color = QuickKartThemeTokens.colors.ink) }
            }
        )
        when (trip.stage) {
            TripStage.TO_STORE -> PickupNavigation(trip, rider, onArrivedStore, onSkip)
            TripStage.AT_STORE -> PickupConfirmation(trip, onTogglePicked, onConfirmPickup, onCancel = { showCancel = true })
            TripStage.TO_CUSTOMER -> DropOffNavigation(trip, rider, onArrivedCustomer, onCalledCustomer, onReportIssue, onSkip)
            TripStage.AT_CUSTOMER -> ProofOfDelivery(trip, onPhotoTaken, onCollectCash, onComplete, onCalledCustomer, onReportIssue)
            else -> {}
        }
    }
    if (showCancel) CancelAssignmentDialog(onDismiss = { showCancel = false }, onConfirm = { showCancel = false; onCancel(it) }, preselect = if (trip.readiness == StoreReadiness.PREPARING && trip.stage == TripStage.AT_STORE) "Long wait at store" else null)
}

// 4. Pickup navigation & arrival
@Composable
private fun PickupNavigation(trip: Trip, rider: GeoPoint, onArrived: () -> Unit, onSkip: () -> Unit) {
    val tokens = QuickKartThemeTokens.colors
    val context = LocalContext.current
    val remaining = trip.pickupKm * (1 - trip.legProgress)
    Box(Modifier.fillMaxWidth().height(300.dp)) {
        RiderMap(rider = rider, routeFrom = trip.origin, routeTo = trip.store, destinationKind = PinKind.STORE, modifier = Modifier.fillMaxSize())
        EtaPill(remaining, Modifier.align(Alignment.TopEnd).padding(10.dp))
    }
    Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        QkCard {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                IconCircle(Icons.Rounded.Storefront)
                Column(Modifier.weight(1f)) {
                    Text(trip.storeName, style = MaterialTheme.typography.titleSmall, color = tokens.ink)
                    Text(trip.storeAddress, style = MaterialTheme.typography.bodySmall, color = tokens.muted)
                }
                CallButton("Call store") { dial(context, trip.storePhone) }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Order readiness", style = MaterialTheme.typography.bodyMedium, color = tokens.ink, modifier = Modifier.weight(1f))
                StatusBadge(trip.readiness.label, if (trip.readiness == StoreReadiness.READY) BadgeTone.SUCCESS else BadgeTone.WARNING)
            }
            SecondaryButton("Navigate", onClick = { openNavigation(context, trip.store, trip.storeName) }, icon = Icons.Rounded.Navigation, modifier = Modifier.fillMaxWidth())
        }
        SwipeToConfirm("Arrived at store", enabled = trip.legProgress >= 0.9f, onConfirm = onArrived, testTag = "arrived_store")
        if (trip.legProgress < 0.9f) Text("Unlocks when you're within 200 m of the store.", style = MaterialTheme.typography.labelSmall, color = tokens.muted)
        DemoSkip(onSkip)
    }
}

// 5. Store pickup confirmation
@Composable
private fun PickupConfirmation(trip: Trip, onToggle: (String) -> Unit, onConfirm: () -> Unit, onCancel: () -> Unit) {
    val tokens = QuickKartThemeTokens.colors
    val context = LocalContext.current
    val allPicked = trip.pickedItems.size >= trip.items.size
    val ready = trip.readiness == StoreReadiness.READY
    Column(Modifier.fillMaxSize()) {
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            QkCard(color = if (ready) tokens.brandYellowSoft else tokens.surface, borderColor = if (ready) tokens.brandYellow else tokens.border) {
                Text("Pickup at", style = MaterialTheme.typography.labelMedium, color = tokens.muted)
                Text(trip.storeName, style = MaterialTheme.typography.titleLarge, color = tokens.ink)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatusBadge("Order ${trip.shortId}", BadgeTone.DARK)
                    StatusBadge(if (ready) "Packed & ready" else "Store is packing", if (ready) BadgeTone.SUCCESS else BadgeTone.WARNING)
                }
                if (!ready) Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.HourglassTop, null, tint = tokens.muted, modifier = Modifier.size(16.dp)); Spacer(Modifier.width(4.dp))
                    Text("Waiting too long? Cancel with reason \"Long wait at store\".", style = MaterialTheme.typography.labelSmall, color = tokens.muted)
                }
            }
            QkCard {
                Text("Items to collect", style = MaterialTheme.typography.titleSmall, color = tokens.ink)
                trip.items.forEach { item ->
                    val picked = item.name in trip.pickedItems
                    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).clickable { onToggle(item.name) }, verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(picked, { onToggle(item.name) }, colors = CheckboxDefaults.colors(checkedColor = tokens.brandYellow, checkmarkColor = tokens.onBrandYellow), modifier = Modifier.zoomScaledControl())
                        Text(item.name, style = MaterialTheme.typography.bodyMedium, color = tokens.ink, modifier = Modifier.weight(1f))
                        Text("× ${item.quantity}", style = MaterialTheme.typography.titleSmall, color = tokens.ink)
                    }
                }
                HorizontalDivider(color = tokens.border)
                AmountRow("Total items", "${trip.itemCount}", bold = true)
                if (trip.paymentMode == PaymentMode.COD) Text("Cash on delivery · collect ${rupees(trip.orderAmount)}", style = MaterialTheme.typography.labelSmall, color = tokens.muted)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SecondaryButton("Cancel order", onClick = onCancel, destructive = true, icon = Icons.Rounded.Close, modifier = Modifier.weight(1f))
                SecondaryButton("Call store", onClick = { dial(context, trip.storePhone) }, icon = Icons.Rounded.Call, modifier = Modifier.weight(1f))
            }
        }
        Column(Modifier.padding(16.dp)) {
            PrimaryButton("Confirm pickup", onClick = onConfirm, enabled = allPicked && ready, icon = Icons.Rounded.CheckCircle, testTag = "confirm_pickup")
            if (!allPicked) Text("Tick each item as you check it.", style = MaterialTheme.typography.labelSmall, color = tokens.muted, modifier = Modifier.padding(top = 6.dp))
        }
    }
}

// 6. Drop-off navigation
@Composable
private fun DropOffNavigation(trip: Trip, rider: GeoPoint, onArrived: () -> Unit, onCalled: () -> Unit, onReport: () -> Unit, onSkip: () -> Unit) {
    val tokens = QuickKartThemeTokens.colors
    val context = LocalContext.current
    val remaining = trip.dropKm * (1 - trip.legProgress)
    Box(Modifier.fillMaxWidth().height(300.dp)) {
        RiderMap(rider = rider, routeFrom = trip.store, routeTo = trip.customer, destinationKind = PinKind.CUSTOMER, modifier = Modifier.fillMaxSize())
        EtaPill(remaining, Modifier.align(Alignment.TopEnd).padding(10.dp))
    }
    Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        QkCard {
            Text("Delivery details", style = MaterialTheme.typography.titleSmall, color = tokens.ink)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                IconCircle(Icons.Rounded.PinDrop)
                Column(Modifier.weight(1f)) {
                    Text(trip.customerName, style = MaterialTheme.typography.titleSmall, color = tokens.ink)
                    Text(trip.customerAddress, style = MaterialTheme.typography.bodySmall, color = tokens.muted)
                }
                CallButton("Call customer") { onCalled(); dial(context, trip.customerPhone) }
            }
            trip.note?.let { Text("Note: $it", style = MaterialTheme.typography.bodySmall, color = tokens.ink) }
            Text("Calls are connected through a masked number to protect privacy.", style = MaterialTheme.typography.labelSmall, color = tokens.muted)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            SecondaryButton("Report issue", onClick = onReport, icon = Icons.Rounded.ReportProblem, modifier = Modifier.weight(1f))
            PrimaryButton("Navigate", onClick = { openNavigation(context, trip.customer, trip.customerName) }, icon = Icons.Rounded.Navigation, modifier = Modifier.weight(1f))
        }
        SwipeToConfirm("Arrived at customer", enabled = trip.legProgress >= 0.9f, onConfirm = onArrived, testTag = "arrived_customer")
        DemoSkip(onSkip)
    }
}

// 7 + 8. Proof of delivery (OTP / photo) with COD collection
@Composable
private fun ProofOfDelivery(trip: Trip, onPhotoTaken: () -> Unit, onCollectCash: () -> Unit, onComplete: (String?) -> Boolean, onCalled: () -> Unit, onReport: () -> Unit) {
    val tokens = QuickKartThemeTokens.colors
    val context = LocalContext.current
    var otp by rememberSaveable { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var wrong by remember { mutableIntStateOf(0) }
    var photo by remember { mutableStateOf<Bitmap?>(null) }
    var received by rememberSaveable { mutableStateOf("") }
    val camera = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bmp -> if (bmp != null) { photo = bmp; onPhotoTaken() } }
    val cod = trip.paymentMode == PaymentMode.COD
    val receivedN = received.toIntOrNull()

    Column(Modifier.fillMaxSize().imePadding()) {
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            QkCard {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.size(44.dp).background(tokens.brandYellow, CircleShape), contentAlignment = Alignment.Center) {
                        Text(trip.customerName.take(1), style = MaterialTheme.typography.titleMedium, color = tokens.onBrandYellow, fontWeight = FontWeight.ExtraBold)
                    }
                    Column(Modifier.weight(1f)) {
                        Text(trip.customerName, style = MaterialTheme.typography.titleSmall, color = tokens.ink)
                        Text("Order ${trip.shortId} · ${trip.itemCount} items", style = MaterialTheme.typography.bodySmall, color = tokens.muted)
                    }
                    CallButton("Call customer") { onCalled(); dial(context, trip.customerPhone) }
                }
            }
            if (cod) {
                QkCard(color = if (trip.cashCollected) tokens.deliveryGreenSoft else tokens.brandYellowSoft, borderColor = if (trip.cashCollected) tokens.deliveryGreen else tokens.brandYellow) {
                    Text("Collect cash", style = MaterialTheme.typography.labelMedium, color = tokens.muted)
                    Text(rupees(trip.orderAmount), style = MaterialTheme.typography.headlineMedium, color = tokens.ink, fontWeight = FontWeight.ExtraBold)
                    if (!trip.cashCollected) {
                        QkTextField(received, { received = it.filter(Char::isDigit).take(5) }, "Cash received from customer", prefix = "₹ ", keyboardType = KeyboardType.Number,
                            error = if (receivedN != null && receivedN < trip.orderAmount) "Short by ${rupees(trip.orderAmount - receivedN)}" else null)
                        if (receivedN != null && receivedN > trip.orderAmount) Text("Return change: ${rupees(receivedN - trip.orderAmount)}", style = MaterialTheme.typography.titleSmall, color = tokens.ink)
                        PrimaryButton("Cash collected", onClick = onCollectCash, enabled = receivedN != null && receivedN >= trip.orderAmount, testTag = "cash_collected")
                        Text("Customer wants to pay by UPI instead? Ask them to pay in the QuickKart app, then mark collected.", style = MaterialTheme.typography.labelSmall, color = tokens.muted)
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Rounded.CheckCircle, null, tint = tokens.deliveryGreenDeep); Spacer(Modifier.width(6.dp)); Text("Cash collected · added to cash in hand", style = MaterialTheme.typography.bodySmall, color = tokens.deliveryGreenDeep) }
                    }
                }
            }
            QkCard {
                Text("Customer OTP verification", style = MaterialTheme.typography.titleSmall, color = tokens.ink)
                Text("Ask the customer for the 4-digit OTP shown in their app.", style = MaterialTheme.typography.bodySmall, color = tokens.muted)
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { OtpBoxes(otp, { otp = it; error = null }) }
                error?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = tokens.alertRed) }
                Text("Demo: the customer's OTP is ${trip.customerOtp}", style = MaterialTheme.typography.labelSmall, color = tokens.muted)
            }
            QkCard {
                Text("Or capture photo proof", style = MaterialTheme.typography.titleSmall, color = tokens.ink)
                Text(if (wrong >= 2) "OTP not working? Take a photo of the parcel at the door with the house number visible." else "Use when the customer can't share the OTP (e.g. left at the door as instructed).",
                    style = MaterialTheme.typography.bodySmall, color = tokens.muted)
                if (photo != null) {
                    Image(photo!!.asImageBitmap(), "Proof photo", contentScale = ContentScale.Crop, modifier = Modifier.fillMaxWidth().height(160.dp).clip(RoundedCornerShape(14.dp)))
                }
                SecondaryButton(if (photo == null) "Take photo" else "Retake photo", onClick = { runCatching { camera.launch(null) } }, icon = Icons.Rounded.CameraAlt, modifier = Modifier.fillMaxWidth())
            }
            TextButton(onClick = onReport) { Icon(Icons.Rounded.ReportProblem, null, tint = tokens.ink); Spacer(Modifier.width(4.dp)); Text("Can't deliver? Report a failed delivery", color = tokens.ink) }
        }
        val readyToComplete = (otp.length == 4 || trip.proofPhotoTaken) && (!cod || trip.cashCollected)
        PrimaryButton("Complete delivery", enabled = readyToComplete, modifier = Modifier.padding(16.dp), testTag = "complete_delivery", onClick = {
            val ok = onComplete(otp.takeIf { it.length == 4 })
            if (!ok) { wrong++; otp = ""; error = if (trip.proofPhotoTaken) null else "Incorrect OTP. Check with the customer or take a photo proof." }
        })
    }
}

// 9. Failed delivery exception
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FailedDeliveryScreen(trip: Trip, onBack: () -> Unit, onSubmit: (String, String) -> Unit, onCall: () -> Unit) {
    val tokens = QuickKartThemeTokens.colors
    val context = LocalContext.current
    var reason by rememberSaveable { mutableStateOf<String?>(null) }
    var comment by rememberSaveable { mutableStateOf("") }
    val needsCall = reason == "Customer unreachable" || reason == "Customer unavailable"
    val needsComment = reason == "Other" || reason == "Wrong address"
    val valid = reason != null && (!needsCall || trip.calledCustomer) && (!needsComment || comment.trim().length >= 10)
    Column(Modifier.fillMaxSize().imePadding()) {
        ScreenHeader("Failed Delivery", onBack = onBack, subtitle = "Order ${trip.shortId} · ${trip.customerName}")
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Why couldn't the delivery be completed?", style = MaterialTheme.typography.titleMedium, color = tokens.ink)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                FAILED_REASONS.forEach { r -> ChoiceChip(r, reason == r, { reason = r }) }
            }
            if (needsCall) {
                QkCard(color = if (trip.calledCustomer) tokens.deliveryGreenSoft else tokens.brandYellowSoft, borderColor = if (trip.calledCustomer) tokens.deliveryGreen else tokens.brandYellow) {
                    Text(if (trip.calledCustomer) "Call attempt recorded" else "Call the customer at least once before reporting", style = MaterialTheme.typography.titleSmall, color = tokens.ink)
                    if (!trip.calledCustomer) SecondaryButton("Call customer", onClick = { onCall(); dial(context, trip.customerPhone) }, icon = Icons.Rounded.Call, modifier = Modifier.fillMaxWidth())
                }
            }
            QkTextField(comment, { comment = it.take(300) }, if (needsComment) "Comments (required)" else "Comments (optional)", singleLine = false, minLines = 3,
                error = if (needsComment && comment.isNotEmpty() && comment.trim().length < 10) "Add a few more details" else null)
            QkCard {
                Text("What happens next", style = MaterialTheme.typography.titleSmall, color = tokens.ink)
                Text("Return the order to ${trip.storeName}. You still earn the base pay of ${rupees(trip.basePay)}." + if (trip.paymentMode == PaymentMode.COD) " No cash is collected." else "",
                    style = MaterialTheme.typography.bodySmall, color = tokens.muted)
            }
        }
        PrimaryButton("Submit report", onClick = { onSubmit(reason!!, comment) }, enabled = valid, modifier = Modifier.padding(16.dp), testTag = "submit_failed")
    }
}

// 10 (modal). Cancel assignment with a reason
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CancelAssignmentDialog(onDismiss: () -> Unit, onConfirm: (String) -> Unit, preselect: String? = null) {
    val tokens = QuickKartThemeTokens.colors
    var reason by remember { mutableStateOf(preselect) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Cancel assignment?") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("The order is reassigned to another rider. Frequent cancellations affect your rating.", style = MaterialTheme.typography.bodySmall, color = tokens.muted)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    CANCEL_REASONS.forEach { r -> ChoiceChip(r, reason == r, { reason = r }) }
                }
            }
        },
        confirmButton = { TextButton(enabled = reason != null, onClick = { onConfirm(reason!!) }) { Text("Confirm cancellation", color = tokens.alertRed) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Keep trip", color = tokens.ink) } }
    )
}

@Composable
private fun EtaPill(remainingKm: Double, modifier: Modifier) {
    val tokens = QuickKartThemeTokens.colors
    val mins = (remainingKm / 0.4).toInt().coerceAtLeast(if (remainingKm > 0.05) 1 else 0) // ~24 km/h in city traffic
    Surface(color = BrandBlack, shape = RoundedCornerShape(14.dp), modifier = modifier) {
        Column(Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
            Text(if (mins == 0) "Arrived" else "$mins MIN", style = MaterialTheme.typography.titleSmall, color = tokens.brandYellow, fontWeight = FontWeight.ExtraBold)
            Text(km(remainingKm), style = MaterialTheme.typography.labelSmall, color = androidx.compose.ui.graphics.Color.White)
        }
    }
}

@Composable
private fun IconCircle(icon: androidx.compose.ui.graphics.vector.ImageVector) {
    val tokens = QuickKartThemeTokens.colors
    Box(Modifier.size(44.dp).background(tokens.brandYellowSoft, CircleShape), contentAlignment = Alignment.Center) { Icon(icon, null, tint = tokens.ink) }
}

@Composable
private fun CallButton(label: String, onClick: () -> Unit) {
    val tokens = QuickKartThemeTokens.colors
    IconButton(onClick = onClick, modifier = Modifier.background(tokens.brandYellow, CircleShape)) { Icon(Icons.Rounded.Call, label, tint = tokens.onBrandYellow) }
}

@Composable
private fun DemoSkip(onSkip: () -> Unit) {
    TextButton(onClick = onSkip) {
        Icon(Icons.Rounded.FastForward, null, tint = QuickKartThemeTokens.colors.muted); Spacer(Modifier.width(4.dp))
        Text("Skip ahead (demo)", color = QuickKartThemeTokens.colors.muted)
    }
}
