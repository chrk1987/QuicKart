package com.example.rider.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddAPhoto
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DeliveryDining
import androidx.compose.material.icons.rounded.DoneAll
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.ReportProblem
import androidx.compose.material.icons.rounded.Storefront
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.rider.data.model.RiderPaymentMode
import com.example.rider.data.model.RiderTripOrder
import com.example.rider.data.model.RiderTripStage
import com.example.rider.ui.components.RiderStreetMapCard
import com.example.rider.ui.theme.JetBrainsMonoFontFamily
import com.example.rider.ui.theme.RiderMetricsTokens
import com.example.rider.ui.theme.RiderThemeTokens

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ActiveTripScreen(
    activeTrip: RiderTripOrder?,
    otpError: String?,
    onArrivedAtHub: (String) -> Unit,
    onToggleItem: (String, String) -> Unit,
    onPickAllAndSeal: (String) -> Unit,
    onToggleColdSeal: (String) -> Unit,
    onStartOutForDelivery: (String) -> Unit,
    onArrivedAtDoor: (String) -> Unit,
    onAttachPodPhoto: (String, Uri) -> Unit,
    onClearOtpError: () -> Unit,
    onCompleteDelivery: (orderId: String, otp: String, codConfirmed: Boolean) -> Unit,
    onReassignTrip: (orderId: String, reason: String) -> Unit,
    onGoToDuty: () -> Unit,
    onMessage: (String) -> Unit
) {
    val tokens = RiderThemeTokens.colors
    val metrics = RiderMetricsTokens.metrics
    val context = LocalContext.current

    if (activeTrip == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(tokens.background)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = tokens.surface),
                border = BorderStroke(1.dp, tokens.border),
                modifier = Modifier
                    .widthIn(max = 420.dp)
                    .fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.DeliveryDining,
                        contentDescription = null,
                        tint = tokens.deliveryGreenDeep,
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        text = "No Active Trip Right Now",
                        style = MaterialTheme.typography.titleLarge,
                        color = tokens.ink
                    )
                    Text(
                        text = "Accept an incoming Dark Store order from Duty Hub to start your 10-min run.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = tokens.muted
                    )
                    Button(
                        onClick = onGoToDuty,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = tokens.deliveryGreenDeep,
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("go_to_duty_from_empty_trip")
                    ) {
                        Text("Open Duty Hub")
                    }
                }
            }
        }
        return
    }

    var enteredOtp by remember(activeTrip.orderId) { mutableStateOf("") }
    var codConfirmed by remember(activeTrip.orderId) { mutableStateOf(false) }
    var showIssueDialog by remember { mutableStateOf(false) }

    val podPhotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            onAttachPodPhoto(activeTrip.orderId, uri)
        }
    }

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
            // 1. Live Stepper Banner
            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = tokens.deliveryGreenDeep,
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
                                text = "${activeTrip.orderId} • Step ${activeTrip.stage.stepIndex} of 4",
                                style = MaterialTheme.typography.labelLarge,
                                color = tokens.brandYellow
                            )
                            Text(
                                text = "Earn ₹${activeTrip.totalRiderPayoutInr}",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                        Text(
                            text = activeTrip.stage.label,
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = activeTrip.stage.shortActionHint,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.88f)
                        )
                        LinearProgressIndicator(
                            progress = { (activeTrip.stage.stepIndex / 4f).coerceIn(0.15f, 1f) },
                            color = tokens.brandYellow,
                            trackColor = Color.White.copy(alpha = 0.24f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                        )
                    }
                }
            }

            // 2. Street Navigation Map Card
            item {
                RiderStreetMapCard(
                    trip = activeTrip,
                    onMapMessage = onMessage
                )
            }

            // 3. Stage-Specific Execution Card
            item {
                when (activeTrip.stage) {
                    RiderTripStage.HEADING_TO_HUB -> {
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = tokens.surface),
                            border = BorderStroke(1.dp, tokens.border),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Rounded.Storefront, contentDescription = null, tint = tokens.ink)
                                    Text(
                                        text = "${activeTrip.hubName} • Pickup Bay ${activeTrip.pickupBayCode}",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = tokens.ink,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(
                                    text = "Park in EV/2W bay near Shutter #2 and verify Seal ${activeTrip.bagSealCode}.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = tokens.muted
                                )
                                Button(
                                    onClick = { onArrivedAtHub(activeTrip.orderId) },
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = tokens.deliveryGreenDeep,
                                        contentColor = Color.White
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp)
                                        .testTag("arrived_at_hub_button")
                                ) {
                                    Text("Reached Dark Store Pod", style = MaterialTheme.typography.labelLarge)
                                }
                            }
                        }
                    }

                    RiderTripStage.PICKING_AT_POD -> {
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = tokens.surface),
                            border = BorderStroke(1.dp, tokens.border),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "Verify & Seal Bag",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = tokens.ink,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Bay ${activeTrip.pickupBayCode} • Seal ${activeTrip.bagSealCode}",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontFamily = JetBrainsMonoFontFamily,
                                            color = tokens.deliveryGreenDeep
                                        )
                                    }
                                    TextButton(
                                        onClick = { onPickAllAndSeal(activeTrip.orderId) },
                                        modifier = Modifier.testTag("pick_all_seal_button")
                                    ) {
                                        Icon(Icons.Rounded.DoneAll, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("Check All")
                                    }
                                }

                                activeTrip.items.forEach { item ->
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (item.isPicked) tokens.deliveryGreenSoft else tokens.surfaceElevated,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { onToggleItem(activeTrip.orderId, item.productId) }
                                            .testTag("pod_item_${item.productId}")
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Checkbox(
                                                checked = item.isPicked,
                                                onCheckedChange = {
                                                    onToggleItem(activeTrip.orderId, item.productId)
                                                },
                                                colors = CheckboxDefaults.colors(checkedColor = tokens.deliveryGreenDeep)
                                            )
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = "${item.quantity}x ${item.name}",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = tokens.ink,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                                Text(
                                                    text = item.variantLabel,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = tokens.muted
                                                )
                                            }
                                        }
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (activeTrip.podSealed) tokens.brandYellowSoft else tokens.surfaceElevated,
                                    border = BorderStroke(
                                        1.dp,
                                        if (activeTrip.podSealed) tokens.brandYellow else tokens.border
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onToggleColdSeal(activeTrip.orderId) }
                                        .testTag("cold_bag_seal_toggle")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Checkbox(
                                            checked = activeTrip.podSealed,
                                            onCheckedChange = { onToggleColdSeal(activeTrip.orderId) },
                                            colors = CheckboxDefaults.colors(checkedColor = tokens.deliveryGreenDeep)
                                        )
                                        Column {
                                            Text(
                                                text = "Tamper-Evident Thermal Seal Applied",
                                                style = MaterialTheme.typography.titleSmall,
                                                color = tokens.ink,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "Code: ${activeTrip.bagSealCode}",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontFamily = JetBrainsMonoFontFamily,
                                                color = tokens.muted
                                            )
                                        }
                                    }
                                }

                                Button(
                                    onClick = { onStartOutForDelivery(activeTrip.orderId) },
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = tokens.deliveryGreenDeep,
                                        contentColor = Color.White
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp)
                                        .testTag("start_delivery_ride_button")
                                ) {
                                    Text("Picked & Sealed • Start Delivery", style = MaterialTheme.typography.labelLarge)
                                }
                            }
                        }
                    }

                    RiderTripStage.OUT_FOR_DELIVERY -> {
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = tokens.surface),
                            border = BorderStroke(1.dp, tokens.border),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = "Heading to ${activeTrip.customerName}",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = tokens.ink,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = activeTrip.deliveryAddressFull,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = tokens.ink
                                )
                                Button(
                                    onClick = { onArrivedAtDoor(activeTrip.orderId) },
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = tokens.deliveryGreenDeep,
                                        contentColor = Color.White
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp)
                                        .testTag("arrived_at_door_button")
                                ) {
                                    Text("Reached Customer Doorstep", style = MaterialTheme.typography.labelLarge)
                                }
                            }
                        }
                    }

                    RiderTripStage.ARRIVED_AT_DOOR -> {
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = tokens.surface),
                            border = BorderStroke(1.5.dp, tokens.deliveryGreenDeep),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Rounded.Key, contentDescription = null, tint = tokens.deliveryGreenDeep)
                                    Column {
                                        Text(
                                            text = "Handover Verification",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = tokens.ink,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Customer 4-digit PIN (Demo hint: ${activeTrip.deliveryOtp})",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontFamily = JetBrainsMonoFontFamily,
                                            color = tokens.deliveryGreenDeep
                                        )
                                    }
                                }

                                OutlinedTextField(
                                    value = enteredOtp,
                                    onValueChange = { raw ->
                                        val digits = raw.filter { it.isDigit() }.take(4)
                                        enteredOtp = digits
                                        onClearOtpError()
                                    },
                                    label = { Text("Enter 4-Digit Delivery PIN") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    isError = otpError != null,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("delivery_otp_input")
                                )

                                if (otpError != null) {
                                    Text(
                                        text = otpError,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = tokens.alertRed,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                if (activeTrip.paymentMode == RiderPaymentMode.COD) {
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = tokens.promoOrange.copy(alpha = 0.12f),
                                        border = BorderStroke(1.dp, tokens.promoOrange.copy(alpha = 0.5f)),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { codConfirmed = !codConfirmed }
                                            .testTag("cod_collect_checkbox_row")
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Checkbox(
                                                checked = codConfirmed,
                                                onCheckedChange = { codConfirmed = it },
                                                colors = CheckboxDefaults.colors(checkedColor = tokens.deliveryGreenDeep)
                                            )
                                            Column {
                                                Text(
                                                    text = "Collected ₹${activeTrip.collectAmountInr} COD Cash",
                                                    style = MaterialTheme.typography.titleSmall,
                                                    color = tokens.ink,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Text(
                                                    text = "Added to your Hub COD ledger balance",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = tokens.muted
                                                )
                                            }
                                        }
                                    }
                                }

                                OutlinedButton(
                                    onClick = {
                                        podPhotoLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .testTag("attach_pod_photo_button")
                                ) {
                                    Icon(
                                        imageVector = if (activeTrip.podPhotoUri != null) {
                                            Icons.Rounded.Verified
                                        } else {
                                            Icons.Rounded.AddAPhoto
                                        },
                                        contentDescription = null
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        if (activeTrip.podPhotoUri != null) {
                                            "Doorstep Proof Photo Attached"
                                        } else {
                                            "Attach Doorstep Photo (Optional)"
                                        }
                                    )
                                }

                                Button(
                                    onClick = {
                                        onCompleteDelivery(activeTrip.orderId, enteredOtp, codConfirmed)
                                    },
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = tokens.deliveryGreenDeep,
                                        contentColor = Color.White
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(52.dp)
                                        .testTag("complete_delivery_button")
                                ) {
                                    Icon(Icons.Rounded.CheckCircle, contentDescription = null)
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        "Complete Delivery • Earn ₹${activeTrip.totalRiderPayoutInr}",
                                        style = MaterialTheme.typography.labelLarge
                                    )
                                }
                            }
                        }
                    }

                    else -> Unit
                }
            }

            // 4. Customer Details, Delivery Notes & Call Card
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = tokens.surface),
                    border = BorderStroke(1.dp, tokens.border),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = activeTrip.customerName,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = tokens.ink,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${activeTrip.deliveryAddressLabel} • ${activeTrip.deliveryAddressFull}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = tokens.muted
                                )
                            }

                            OutlinedButton(
                                onClick = {
                                    val dial = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${activeTrip.customerPhone}"))
                                    runCatching { context.startActivity(dial) }
                                        .onFailure { onMessage("No phone dialer available") }
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .defaultMinSize(minHeight = 44.dp)
                                    .testTag("call_customer_button")
                            ) {
                                Icon(Icons.Rounded.Call, contentDescription = "Call customer", modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Call")
                            }
                        }

                        if (activeTrip.deliveryInstructions.isNotEmpty()) {
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                activeTrip.deliveryInstructions.forEach { instr ->
                                    Surface(
                                        color = tokens.brandYellowSoft,
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = instr,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = tokens.ink,
                                            fontWeight = FontWeight.SemiBold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }

                        if (activeTrip.riderNote.isNotBlank()) {
                            Surface(
                                color = tokens.surfaceElevated,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Note: ${activeTrip.riderNote}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = tokens.ink,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }

                        HorizontalDivider(color = tokens.border)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Payments,
                                    contentDescription = null,
                                    tint = if (activeTrip.paymentMode == RiderPaymentMode.COD) {
                                        tokens.promoOrange
                                    } else {
                                        tokens.deliveryGreenDeep
                                    },
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = if (activeTrip.paymentMode == RiderPaymentMode.COD) {
                                        "Collect ₹${activeTrip.collectAmountInr} Cash"
                                    } else {
                                        activeTrip.paymentMode.label
                                    },
                                    style = MaterialTheme.typography.labelLarge,
                                    color = tokens.ink
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Rounded.Inventory2, contentDescription = null, tint = tokens.muted, modifier = Modifier.size(16.dp))
                                Text(
                                    text = "${activeTrip.totalUnits} units",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = tokens.muted
                                )
                            }
                        }
                    }
                }
            }

            // 5. Trip Issue / Reassign Escalation
            item {
                OutlinedButton(
                    onClick = { showIssueDialog = true },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("report_trip_issue_button")
                ) {
                    Icon(Icons.Rounded.ReportProblem, contentDescription = null, tint = tokens.alertRed)
                    Spacer(Modifier.width(6.dp))
                    Text("Report Trip Issue / Request Reassign", color = tokens.alertRed)
                }
            }
        }
    }

    if (showIssueDialog) {
        val reasons = listOf(
            "Vehicle breakdown / flat tire",
            "Customer unreachable after 3 calls",
            "Damaged / spilled item at Pod",
            "Unsafe road / heavy weather block"
        )
        AlertDialog(
            onDismissRequest = { showIssueDialog = false },
            title = { Text("Report Trip Issue") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Hub Dispatch will reassign ${activeTrip.orderId} cleanly without penalty for valid alerts.",
                        style = MaterialTheme.typography.bodySmall,
                        color = tokens.muted
                    )
                    reasons.forEach { reason ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = tokens.surfaceElevated,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    showIssueDialog = false
                                    onReassignTrip(activeTrip.orderId, reason)
                                }
                        ) {
                            Text(
                                text = reason,
                                style = MaterialTheme.typography.bodyMedium,
                                color = tokens.ink,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showIssueDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}
