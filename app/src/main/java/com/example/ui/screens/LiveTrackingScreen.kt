package com.example.ui.screens

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
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
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DeliveryDining
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarOutline
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.SeedData
import com.example.data.model.OrderRecord
import com.example.data.model.OrderStage
import com.example.service.PdfReceiptService
import com.example.ui.components.InteractiveDeliveryRouteMap
import com.example.ui.components.LiveOrderSummarySheetTriggerBar
import com.example.ui.components.OrderSummaryBottomSheet
import com.example.ui.components.QuickKartLogo
import com.example.ui.theme.AmberGold
import com.example.ui.theme.DeviceSizeClass
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.QuickKartMetricsTokens
import com.example.ui.theme.QuickKartThemeTokens
import com.example.util.NotificationHelper
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LiveTrackingScreen(
    order: OrderRecord?,
    notificationsEnabled: Boolean,
    autoSimulating: Boolean,
    onAdvanceStage: (OrderRecord) -> Unit,
    onRevertStage: (OrderRecord) -> Unit,
    onToggleAutoSim: (OrderRecord) -> Unit,
    onSubmitRating: (String, Int, List<String>, String) -> Unit,
    onBrowseStorefront: () -> Unit,
    onShowMessage: (String) -> Unit
) {
    val tokens = QuickKartThemeTokens.colors
    val metrics = QuickKartMetricsTokens.metrics
    val context = LocalContext.current

    var hasNotifPermission by remember {
        mutableStateOf(NotificationHelper.hasNotificationPermission(context))
    }

    val notifPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasNotifPermission = granted
        if (granted) {
            onShowMessage("Live order push alerts enabled!")
        } else {
            onShowMessage("Notification permission denied. Enable in system settings for live alerts.")
        }
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            !NotificationHelper.hasNotificationPermission(context) &&
            notificationsEnabled
        ) {
            notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    if (order == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(tokens.background)
                .padding(metrics.screenHorizontalPadding + 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(metrics.itemSpacing)
            ) {
                QuickKartLogo(
                    size = metrics.avatarSize + 12.dp,
                    cornerRadius = 18.dp
                )
                Text(
                    text = "No active order right now",
                    style = MaterialTheme.typography.titleLarge,
                    color = tokens.ink,
                    textAlign = TextAlign.Center
                )
                Button(
                    onClick = onBrowseStorefront,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = tokens.brandYellow,
                        contentColor = tokens.onBrandYellow
                    )
                ) {
                    Text("Order in 8 Minutes")
                }
            }
        }
        return
    }

    val minutes = order.remainingSeconds / 60
    val seconds = order.remainingSeconds % 60
    val formattedEta = String.format(Locale.US, "%02d:%02d", minutes, seconds)
    var showOrderSummarySheet by remember(order.orderId) { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val receiptService = remember(context) { PdfReceiptService(context) }

    fun saveAndOpenReceipt() {
        coroutineScope.launch {
            receiptService.generateAndSaveReceipt(order)
                .onSuccess { result ->
                    val viewIntent = PdfReceiptService.openPdfIntent(result.fileUri)
                    val chooser = PdfReceiptService.sharePdfIntent(result.fileUri, order.orderId)
                    val launched = runCatching { context.startActivity(viewIntent) }.isSuccess
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
            contentPadding = PaddingValues(
                start = metrics.screenHorizontalPadding,
                end = metrics.screenHorizontalPadding,
                top = metrics.screenHorizontalPadding,
                bottom = metrics.screenHorizontalPadding + 92.dp
            ),
            verticalArrangement = Arrangement.spacedBy(metrics.itemSpacing)
        ) {
            // Blinkit-style Green Live Status & ETA Header
            item {
                val etaMinsHeadline = ((order.remainingSeconds + 59) / 60).coerceAtLeast(1)
                val headerHeadline = when (order.stage) {
                    OrderStage.CONFIRMED -> "Order placed at Pod"
                    OrderStage.PACKING -> "Packing your order"
                    OrderStage.OUT_FOR_DELIVERY -> "Arriving in $etaMinsHeadline min"
                    OrderStage.DELIVERED -> "Order delivered!"
                }
                val statusTag = when (order.stage) {
                    OrderStage.DELIVERED -> "DELIVERED"
                    else -> "ON TIME"
                }

                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = tokens.deliveryGreenDeep
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(metrics.cardPadding + 2.dp),
                        horizontalArrangement = Arrangement.spacedBy(metrics.itemSpacing),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        QuickKartLogo(
                            size = metrics.badgeButtonSize,
                            cornerRadius = 10.dp
                        )
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Surface(
                                    color = tokens.brandYellow,
                                    shape = RoundedCornerShape(50)
                                ) {
                                    Text(
                                        text = statusTag,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = tokens.onBrandYellow,
                                        fontWeight = FontWeight.ExtraBold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                                Surface(
                                    color = Color.White.copy(alpha = 0.18f),
                                    shape = RoundedCornerShape(50)
                                ) {
                                    Text(
                                        text = order.orderId,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }
                            AnimatedContent(
                                targetState = order.stage,
                                transitionSpec = {
                                    (fadeIn(tween(260)) + slideInVertically { it / 3 }) togetherWith
                                        (fadeOut(tween(180)) + slideOutVertically { -it / 3 })
                                },
                                label = "tracking_stage_header_anim"
                            ) { _ ->
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = headerHeadline,
                                        style = MaterialTheme.typography.titleLarge,
                                        color = Color.White,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                    Text(
                                        text = "${order.stage.subtitle} • ${order.neighborhoodName}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White.copy(alpha = 0.88f)
                                    )
                                }
                            }
                        }

                        Surface(
                            color = Color.Black.copy(alpha = 0.24f),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = if (order.stage == OrderStage.DELIVERED) "DONE" else "ETA",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = tokens.brandYellow,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (order.stage == OrderStage.DELIVERED) "00:00" else formattedEta,
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = if (metrics.isNarrowPhone) 22.sp else 26.sp,
                                    color = Color.White,
                                    modifier = Modifier.testTag("live_eta_countdown")
                                )
                            }
                        }
                    }
                }
            }

            // Notification permission banner if not granted on Android 13+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotifPermission) {
                item {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = tokens.brandYellowSoft,
                        border = BorderStroke(1.dp, tokens.brandYellow),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(metrics.cardPadding),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.NotificationsActive,
                                contentDescription = null,
                                tint = tokens.ink,
                                modifier = Modifier.size(metrics.iconMedium)
                            )
                            Text(
                                text = "Allow notifications to get live status bar updates.",
                                style = MaterialTheme.typography.bodySmall,
                                color = tokens.ink,
                                modifier = Modifier.weight(1f)
                            )
                            TextButton(
                                onClick = {
                                    notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                }
                            ) {
                                Text("Enable", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Hero Interactive Real Street Map View (Blinkit-style directly below header)
            item {
                InteractiveDeliveryRouteMap(
                    order = order,
                    onPinInfo = onShowMessage
                )
            }

            // Blinkit-style Delivery Partner Card directly beneath the map
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = tokens.surface),
                    border = BorderStroke(1.dp, tokens.border),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(metrics.cardPadding),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(metrics.itemSpacing)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_delivery_rider),
                            contentDescription = "Delivery rider ${order.riderName}",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(metrics.avatarSize)
                                .clip(CircleShape)
                        )
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "I'm ${order.riderName}, your delivery partner",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = tokens.ink,
                                    fontWeight = FontWeight.Bold
                                )
                                Surface(
                                    color = tokens.brandYellowSoft,
                                    shape = RoundedCornerShape(50)
                                ) {
                                    Text(
                                        text = "${order.riderRating} ★",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = tokens.ink,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = "QuickKart Electric Fleet • ${order.riderPhone}",
                                style = MaterialTheme.typography.bodySmall,
                                color = tokens.muted
                            )
                        }
                        Surface(
                            color = tokens.deliveryGreenSoft,
                            shape = CircleShape,
                            modifier = Modifier
                                .size(maxOf(48.dp, metrics.badgeButtonSize))
                                .clip(CircleShape)
                                .clickable {
                                    val dialIntent = Intent(
                                        Intent.ACTION_DIAL,
                                        Uri.parse("tel:${order.riderPhone}")
                                    )
                                    runCatching { context.startActivity(dialIntent) }
                                        .onFailure { onShowMessage("Dialer unavailable; rider ${order.riderPhone}") }
                                }
                                .testTag("call_rider_button")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Rounded.Call,
                                    contentDescription = "Call rider",
                                    tint = tokens.deliveryGreenDeep,
                                    modifier = Modifier.size(metrics.iconMedium)
                                )
                            }
                        }
                    }
                }
            }

            // Animated 4-Stage Progress Bar
            item {
                AnimatedStageProgressCard(stage = order.stage)
            }

            // Simulate Stage Progression Controls
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = tokens.surface),
                    border = BorderStroke(1.dp, tokens.border),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(metrics.cardPadding),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = "Simulate Live Order Progress",
                                style = MaterialTheme.typography.titleSmall,
                                color = tokens.ink
                            )
                            Text(
                                text = "Triggers real notifications",
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
                                onClick = { onRevertStage(order) },
                                enabled = order.stage.previous() != null,
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .defaultMinSize(minHeight = 46.dp)
                                    .testTag("sim_prev_stage_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                    contentDescription = null,
                                    modifier = Modifier.size(metrics.iconSmall)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text("Prev")
                            }

                            Button(
                                onClick = { onAdvanceStage(order) },
                                enabled = order.stage.next() != null,
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = tokens.brandYellow,
                                    contentColor = tokens.onBrandYellow
                                ),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                                modifier = Modifier
                                    .weight(if (metrics.isNarrowPhone) 1f else 1.25f)
                                    .defaultMinSize(minHeight = 46.dp)
                                    .testTag("sim_next_stage_button")
                            ) {
                                Text("Next Stage")
                                Spacer(Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                                    contentDescription = null,
                                    modifier = Modifier.size(metrics.iconSmall)
                                )
                            }

                            Button(
                                onClick = { onToggleAutoSim(order) },
                                enabled = order.stage != OrderStage.DELIVERED || autoSimulating,
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (autoSimulating) tokens.alertRed else tokens.deliveryGreenDeep,
                                    contentColor = Color.White
                                ),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .defaultMinSize(minHeight = 46.dp)
                                    .testTag("sim_auto_button")
                            ) {
                                Icon(
                                    imageVector = if (autoSimulating) Icons.Rounded.Stop else Icons.Rounded.PlayArrow,
                                    contentDescription = null,
                                    modifier = Modifier.size(metrics.iconSmall)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(if (autoSimulating) "Stop" else "Auto")
                            }
                        }
                    }
                }
            }

            // Post-Delivery Rating Card (shown once Delivered)
            item {
                AnimatedVisibility(visible = order.stage == OrderStage.DELIVERED) {
                    PostDeliveryRatingCard(
                        order = order,
                        onSubmit = { stars, tags, comment ->
                            onSubmitRating(order.orderId, stars, tags, comment)
                            onShowMessage("Thank you for rating your delivery!")
                        }
                    )
                }
            }

            // Order Items & Instructions Summary
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = tokens.surface),
                    border = BorderStroke(1.dp, tokens.border),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(metrics.cardPadding),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Order Summary (${order.totalItemCount} items • ₹${order.totalPaid})",
                                style = MaterialTheme.typography.titleMedium,
                                color = tokens.ink,
                                modifier = Modifier.weight(1f)
                            )
                            TextButton(
                                onClick = { showOrderSummarySheet = true },
                                modifier = Modifier.testTag("open_order_summary_sheet_button")
                            ) {
                                Text("Full Bill", fontWeight = FontWeight.Bold)
                            }
                        }
                        order.items.forEach { item ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Text(
                                    text = "${item.quantity}x ${item.productName} (${item.variantLabel})",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = tokens.ink,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = "₹${item.lineTotal}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = tokens.ink,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                        if (order.deliveryInstructions.isNotEmpty() || order.riderNote.isNotBlank()) {
                            Surface(
                                color = tokens.surfaceElevated,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = "Delivery Note",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = tokens.muted
                                    )
                                    Text(
                                        text = buildList {
                                            if (order.deliveryInstructions.isNotEmpty()) {
                                                add(order.deliveryInstructions.joinToString(" • "))
                                            }
                                            if (order.riderNote.isNotBlank()) add(order.riderNote)
                                        }.joinToString(" — "),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = tokens.ink
                                    )
                                }
                            }
                        }

                        if (PdfReceiptService.canGenerateReceipt(order)) {
                            OutlinedButton(
                                onClick = { saveAndOpenReceipt() },
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, tokens.deliveryGreenDeep),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .defaultMinSize(minHeight = 48.dp)
                                    .testTag("tracking_download_receipt_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Download,
                                    contentDescription = null,
                                    tint = tokens.deliveryGreenDeep,
                                    modifier = Modifier.size(metrics.iconMedium)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = "Download PDF Receipt",
                                    color = tokens.deliveryGreenDeep,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Docked real-time order summary bottom-sheet peek bar
        LiveOrderSummarySheetTriggerBar(
            order = order,
            onOpenSheet = { showOrderSummarySheet = true },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .widthIn(max = 760.dp)
        )
    }

    if (showOrderSummarySheet) {
        OrderSummaryBottomSheet(
            order = order,
            onDismiss = { showOrderSummarySheet = false },
            onDownloadReceipt = if (PdfReceiptService.canGenerateReceipt(order)) {
                { saveAndOpenReceipt() }
            } else {
                null
            }
        )
    }
}

@Composable
private fun AnimatedStageProgressCard(stage: OrderStage) {
    val tokens = QuickKartThemeTokens.colors
    val metrics = QuickKartMetricsTokens.metrics
    val circleSize = when (metrics.sizeClass) {
        DeviceSizeClass.SMALL_COMPACT -> 26.dp
        DeviceSizeClass.COMPACT -> 30.dp
        DeviceSizeClass.MEDIUM -> 32.dp
        DeviceSizeClass.EXPANDED -> 34.dp
    }
    val targetFraction = when (stage) {
        OrderStage.CONFIRMED -> 0.12f
        OrderStage.PACKING -> 0.42f
        OrderStage.OUT_FOR_DELIVERY -> 0.75f
        OrderStage.DELIVERED -> 1.0f
    }
    val progress by animateFloatAsState(
        targetValue = targetFraction,
        animationSpec = tween(650, easing = FastOutSlowInEasing),
        label = "stage_progress"
    )

    val pulseTransition = rememberInfiniteTransition(label = "active_stage_pulse")
    val pulseScale by pulseTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.24f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "stage_dot_scale"
    )

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = tokens.surface),
        border = BorderStroke(1.dp, tokens.border),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(metrics.cardPadding),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(50))
                    .background(tokens.surfaceElevated)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .height(8.dp)
                        .clip(RoundedCornerShape(50))
                        .background(tokens.deliveryGreen)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                OrderStage.entries.forEach { entry ->
                    val completed = entry.stepIndex < stage.stepIndex || stage == OrderStage.DELIVERED
                    val isCurrent = entry == stage && stage != OrderStage.DELIVERED
                    val badgeColor by animateColorAsState(
                        targetValue = when {
                            completed -> tokens.deliveryGreen
                            isCurrent -> tokens.brandYellow
                            else -> tokens.surfaceElevated
                        },
                        animationSpec = tween(350),
                        label = "stage_badge_color"
                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(circleSize)
                                .scale(if (isCurrent) pulseScale else 1f)
                                .clip(CircleShape)
                                .background(badgeColor)
                        ) {
                            when {
                                completed -> {
                                    Icon(
                                        imageVector = Icons.Rounded.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(metrics.iconSmall)
                                    )
                                }
                                isCurrent -> {
                                    val stageIcon = when (entry) {
                                        OrderStage.CONFIRMED -> Icons.Rounded.ReceiptLong
                                        OrderStage.PACKING -> Icons.Rounded.Inventory2
                                        OrderStage.OUT_FOR_DELIVERY -> Icons.Rounded.DeliveryDining
                                        OrderStage.DELIVERED -> Icons.Rounded.Check
                                    }
                                    Icon(
                                        imageVector = stageIcon,
                                        contentDescription = entry.title,
                                        tint = tokens.onBrandYellow,
                                        modifier = Modifier.size(metrics.iconSmall)
                                    )
                                }
                                else -> {
                                    Text(
                                        text = "${entry.stepIndex + 1}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = tokens.muted,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = entry.title,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (completed || isCurrent) tokens.ink else tokens.muted,
                            fontWeight = if (isCurrent || completed) FontWeight.Bold else FontWeight.Normal,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PostDeliveryRatingCard(
    order: OrderRecord,
    onSubmit: (Int, List<String>, String) -> Unit
) {
    val tokens = QuickKartThemeTokens.colors
    val metrics = QuickKartMetricsTokens.metrics
    var selectedStars by remember(order.orderId) { mutableIntStateOf(order.userRating ?: 5) }
    var selectedTags by remember(order.orderId) { mutableStateOf(order.praiseTags.toSet()) }
    var comment by remember(order.orderId) { mutableStateOf(order.feedbackComment) }
    val alreadySubmitted = order.userRating != null

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = tokens.surface),
        border = BorderStroke(1.5.dp, tokens.brandYellow),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("post_delivery_rating_card")
    ) {
        Column(
            modifier = Modifier.padding(metrics.cardPadding),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.CheckCircle,
                    contentDescription = null,
                    tint = tokens.deliveryGreen,
                    modifier = Modifier.size(metrics.iconMedium)
                )
                Text(
                    text = if (alreadySubmitted) "Thanks for your feedback!" else "Rate Your 8-Min Delivery",
                    style = MaterialTheme.typography.titleMedium,
                    color = tokens.ink,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                (1..5).forEach { star ->
                    IconButton(
                        onClick = { if (!alreadySubmitted) selectedStars = star },
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("rating_star_$star")
                    ) {
                        Icon(
                            imageVector = if (star <= selectedStars) Icons.Rounded.Star else Icons.Rounded.StarOutline,
                            contentDescription = "$star stars",
                            tint = if (star <= selectedStars) AmberGold else tokens.muted,
                            modifier = Modifier.size(metrics.iconHero)
                        )
                    }
                }
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SeedData.praiseTags.forEach { tag ->
                    val active = tag in selectedTags
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = if (active) tokens.brandYellow else tokens.surfaceElevated,
                        border = BorderStroke(1.dp, if (active) tokens.brandYellow else tokens.border),
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .clickable(enabled = !alreadySubmitted) {
                                selectedTags = if (active) selectedTags - tag else selectedTags + tag
                            }
                            .testTag("praise_tag_${tag.lowercase().replace(" ", "_")}")
                    ) {
                        Text(
                            text = tag,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (active) tokens.onBrandYellow else tokens.ink,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                        )
                    }
                }
            }

            OutlinedTextField(
                value = comment,
                onValueChange = { if (!alreadySubmitted) comment = it },
                enabled = !alreadySubmitted,
                label = { Text("Optional Feedback") },
                placeholder = { Text("How was the produce freshness and speed?") },
                minLines = 2,
                maxLines = 3,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("rating_comment_input")
            )

            if (!alreadySubmitted) {
                Button(
                    onClick = { onSubmit(selectedStars, selectedTags.toList(), comment) },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = tokens.deliveryGreenDeep,
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("submit_rating_button")
                ) {
                    Text("Submit Rating", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}


