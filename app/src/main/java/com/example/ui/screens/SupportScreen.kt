package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AddPhotoAlternate
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.HeadsetMic
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarOutline
import androidx.compose.material.icons.rounded.SupportAgent
import androidx.compose.material.icons.rounded.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.local.SeedData
import com.example.data.model.OrderRecord
import com.example.data.model.SupportFaqTopic
import com.example.data.model.SupportIssueCategory
import com.example.data.model.SupportMessage
import com.example.data.model.SupportResolutionOption
import com.example.data.model.SupportTicketRecord
import com.example.data.model.SupportTicketStatus
import com.example.data.model.computeSupportRefundEstimate
import com.example.data.model.orderLineItemKey
import com.example.ui.theme.AmberGold
import com.example.ui.theme.QuickKartMetricsTokens
import com.example.ui.theme.QuickKartThemeTokens
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private enum class SupportViewMode {
    HUB,
    NEW_TICKET,
    TICKET_THREAD
}

@Composable
fun SupportScreen(
    orders: List<OrderRecord>,
    tickets: List<SupportTicketRecord>,
    initialOrderId: String?,
    openWizardInitially: Boolean,
    onCreateTicket: (
        order: OrderRecord,
        category: SupportIssueCategory,
        selectedItemKeys: Set<String>,
        preferredResolution: SupportResolutionOption,
        userNote: String,
        photoUriString: String?,
        onCreated: (SupportTicketRecord) -> Unit
    ) -> Unit,
    onSendReply: (String, String) -> Unit,
    onResolveTicket: (String, String?) -> Unit,
    onReopenTicket: (String) -> Unit,
    onSubmitCsat: (String, Int) -> Unit,
    onNavigateBack: () -> Unit,
    onShowMessage: (String) -> Unit
) {
    val tokens = QuickKartThemeTokens.colors
    val metrics = QuickKartMetricsTokens.metrics

    var viewMode by rememberSaveable {
        mutableStateOf(
            if (openWizardInitially && orders.isNotEmpty()) {
                SupportViewMode.NEW_TICKET
            } else {
                SupportViewMode.HUB
            }
        )
    }
    var activeTicketId by rememberSaveable { mutableStateOf<String?>(null) }
    var wizardOrderId by rememberSaveable { mutableStateOf(initialOrderId ?: orders.firstOrNull()?.orderId) }
    var wizardCategory by rememberSaveable { mutableStateOf(SupportIssueCategory.MISSING_ITEM) }

    LaunchedEffect(initialOrderId, openWizardInitially) {
        if (initialOrderId != null) {
            wizardOrderId = initialOrderId
        }
        if (openWizardInitially && orders.isNotEmpty()) {
            viewMode = SupportViewMode.NEW_TICKET
        }
    }

    val activeTicket = remember(tickets, activeTicketId) {
        activeTicketId?.let { id -> tickets.find { it.ticketId == id } }
    }

    BackHandler {
        when (viewMode) {
            SupportViewMode.HUB -> onNavigateBack()
            SupportViewMode.NEW_TICKET, SupportViewMode.TICKET_THREAD -> viewMode = SupportViewMode.HUB
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(tokens.background)
            .navigationBarsPadding()
    ) {
        // Top Header Bar
        Surface(
            color = tokens.surface,
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = metrics.screenHorizontalPadding, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                IconButton(
                    onClick = {
                        when (viewMode) {
                            SupportViewMode.HUB -> onNavigateBack()
                            SupportViewMode.NEW_TICKET, SupportViewMode.TICKET_THREAD -> {
                                viewMode = SupportViewMode.HUB
                            }
                        }
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("support_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Back",
                        tint = tokens.ink,
                        modifier = Modifier.size(metrics.iconMedium)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = when (viewMode) {
                            SupportViewMode.HUB -> "QuickKart Care"
                            SupportViewMode.NEW_TICKET -> "Report Order Issue"
                            SupportViewMode.TICKET_THREAD -> "Ticket ${activeTicket?.ticketId ?: ""}"
                        },
                        style = MaterialTheme.typography.titleLarge,
                        color = tokens.ink,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = when (viewMode) {
                            SupportViewMode.HUB -> "Instant refunds, 8-min replacements & live desk"
                            SupportViewMode.NEW_TICKET -> "Guided resolution in under 30 seconds"
                            SupportViewMode.TICKET_THREAD -> {
                                activeTicket?.let { "${it.orderId} • ${it.category.label}" }
                                    ?: "Ticket details"
                            }
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = tokens.muted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                val openCount = tickets.count { it.status != SupportTicketStatus.RESOLVED }
                if (viewMode == SupportViewMode.HUB && openCount > 0) {
                    Surface(
                        color = tokens.brandYellowSoft,
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(1.dp, tokens.brandYellow)
                    ) {
                        Text(
                            text = "$openCount Active",
                            style = MaterialTheme.typography.labelSmall,
                            color = tokens.ink,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }
            }
        }

        when (viewMode) {
            SupportViewMode.HUB -> SupportHubContent(
                tickets = tickets,
                faqs = SeedData.supportFaqTopics,
                hasOrders = orders.isNotEmpty(),
                onStartNewTicket = { category ->
                    if (category != null) {
                        wizardCategory = category
                    }
                    if (wizardOrderId == null) {
                        wizardOrderId = orders.firstOrNull()?.orderId
                    }
                    viewMode = SupportViewMode.NEW_TICKET
                },
                onOpenTicket = { ticket ->
                    activeTicketId = ticket.ticketId
                    viewMode = SupportViewMode.TICKET_THREAD
                }
            )

            SupportViewMode.NEW_TICKET -> SupportIssueWizardContent(
                orders = orders,
                initialOrderId = wizardOrderId,
                initialCategory = wizardCategory,
                onSubmit = { order, category, selectedKeys, resolution, note, photoUri ->
                    onCreateTicket(
                        order,
                        category,
                        selectedKeys,
                        resolution,
                        note,
                        photoUri
                    ) { created ->
                        activeTicketId = created.ticketId
                        viewMode = SupportViewMode.TICKET_THREAD
                        val toast = if (created.status == SupportTicketStatus.RESOLVED) {
                            "${created.ticketId} resolved! ${created.resolutionSummary}"
                        } else {
                            "Ticket ${created.ticketId} created — Care Desk reviewing now"
                        }
                        onShowMessage(toast)
                    }
                }
            )

            SupportViewMode.TICKET_THREAD -> {
                val ticket = activeTicket
                if (ticket == null) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Button(onClick = { viewMode = SupportViewMode.HUB }) {
                            Text("Back to Support Hub")
                        }
                    }
                } else {
                    SupportTicketThreadContent(
                        ticket = ticket,
                        onSendReply = { text -> onSendReply(ticket.ticketId, text) },
                        onResolveTicket = {
                            onResolveTicket(ticket.ticketId, null)
                            onShowMessage("Ticket ${ticket.ticketId} marked Resolved")
                        },
                        onReopenTicket = {
                            onReopenTicket(ticket.ticketId)
                            onShowMessage("Ticket ${ticket.ticketId} reopened")
                        },
                        onSubmitCsat = { rating ->
                            onSubmitCsat(ticket.ticketId, rating)
                            onShowMessage("Thanks for rating QuickKart Care!")
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SupportHubContent(
    tickets: List<SupportTicketRecord>,
    faqs: List<SupportFaqTopic>,
    hasOrders: Boolean,
    onStartNewTicket: (SupportIssueCategory?) -> Unit,
    onOpenTicket: (SupportTicketRecord) -> Unit
) {
    val tokens = QuickKartThemeTokens.colors
    val metrics = QuickKartMetricsTokens.metrics
    var expandedFaqId by remember { mutableStateOf<String?>(faqs.firstOrNull()?.id) }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .widthIn(max = 760.dp)
                .fillMaxSize(),
            contentPadding = PaddingValues(metrics.screenHorizontalPadding),
            verticalArrangement = Arrangement.spacedBy(metrics.itemSpacing)
        ) {
            // Blinkit-style Instant Care Promise Hero
            item {
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = tokens.deliveryGreenDeep),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(metrics.cardPadding + 2.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                color = tokens.brandYellow,
                                shape = CircleShape,
                                modifier = Modifier.size(metrics.avatarSize)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Rounded.HeadsetMic,
                                        contentDescription = "QuickKart Care",
                                        tint = tokens.onBrandYellow,
                                        modifier = Modifier.size(metrics.iconLarge)
                                    )
                                }
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Instant Care Promise",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = Color.White,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = "Missing or spoiled items resolved in seconds — wallet credit or 8-min replacement.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.88f)
                                )
                            }
                        }

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            CarePromisePill(
                                icon = Icons.Rounded.Bolt,
                                text = "10-sec instant credit ≤ ₹250"
                            )
                            CarePromisePill(
                                icon = Icons.Rounded.VerifiedUser,
                                text = "Zero-questions perishable care"
                            )
                        }

                        Button(
                            onClick = { onStartNewTicket(null) },
                            enabled = hasOrders,
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = tokens.brandYellow,
                                contentColor = tokens.onBrandYellow
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .defaultMinSize(minHeight = 50.dp)
                                .testTag("new_support_ticket_button")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Add,
                                contentDescription = null,
                                modifier = Modifier.size(metrics.iconMedium)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = if (hasOrders) "Report an Order Issue" else "Place an order first for order help",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            }

            // Recent & Active Support Tickets
            item {
                Text(
                    text = "Your Support Requests (${tickets.size})",
                    style = MaterialTheme.typography.titleMedium,
                    color = tokens.ink
                )
            }

            if (tickets.isEmpty()) {
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
                                .padding(metrics.cardPadding + 6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.SupportAgent,
                                contentDescription = null,
                                tint = tokens.muted,
                                modifier = Modifier.size(metrics.iconHero)
                            )
                            Text(
                                text = "No support tickets yet",
                                style = MaterialTheme.typography.titleSmall,
                                color = tokens.ink
                            )
                            Text(
                                text = "Everything delivered smoothly! Tap above anytime if an order needs attention.",
                                style = MaterialTheme.typography.bodySmall,
                                color = tokens.muted,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(tickets, key = { it.ticketId }) { ticket ->
                    SupportTicketSummaryCard(
                        ticket = ticket,
                        onClick = { onOpenTicket(ticket) }
                    )
                }
            }

            // Blinkit-style Quick Self-Help FAQs
            item {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Quick Help Topics",
                    style = MaterialTheme.typography.titleMedium,
                    color = tokens.ink
                )
            }

            items(faqs, key = { it.id }) { faq ->
                val expanded = expandedFaqId == faq.id
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = tokens.surface),
                    border = BorderStroke(
                        1.dp,
                        if (expanded) tokens.brandYellow else tokens.border
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable {
                            expandedFaqId = if (expanded) null else faq.id
                        }
                        .testTag("support_faq_${faq.id}")
                ) {
                    Column(
                        modifier = Modifier.padding(metrics.cardPadding),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = faq.question,
                                style = MaterialTheme.typography.titleSmall,
                                color = tokens.ink,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                imageVector = if (expanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                                contentDescription = if (expanded) "Collapse" else "Expand",
                                tint = tokens.muted,
                                modifier = Modifier.size(metrics.iconMedium)
                            )
                        }

                        AnimatedVisibility(visible = expanded) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = faq.answer,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = tokens.muted
                                )
                                if (hasOrders) {
                                    TextButton(
                                        onClick = { onStartNewTicket(faq.linkedCategory) },
                                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "Raise ${faq.linkedCategory.label} Request →",
                                            color = tokens.deliveryGreenDeep,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CarePromisePill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String
) {
    val metrics = QuickKartMetricsTokens.metrics
    Surface(
        color = Color.White.copy(alpha = 0.16f),
        shape = RoundedCornerShape(50)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(metrics.iconSmall)
            )
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SupportTicketSummaryCard(
    ticket: SupportTicketRecord,
    onClick: () -> Unit
) {
    val tokens = QuickKartThemeTokens.colors
    val metrics = QuickKartMetricsTokens.metrics
    val updatedFormatted = remember(ticket.updatedAtMillis) {
        SimpleDateFormat("dd MMM • hh:mm a", Locale.getDefault()).format(Date(ticket.updatedAtMillis))
    }
    val statusBg = when (ticket.status) {
        SupportTicketStatus.RESOLVED -> tokens.deliveryGreenSoft
        SupportTicketStatus.UNDER_REVIEW -> tokens.brandYellowSoft
        SupportTicketStatus.OPEN -> tokens.surfaceElevated
    }
    val statusFg = when (ticket.status) {
        SupportTicketStatus.RESOLVED -> tokens.deliveryGreenDeep
        SupportTicketStatus.UNDER_REVIEW -> tokens.ink
        SupportTicketStatus.OPEN -> tokens.ink
    }

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = tokens.surface),
        border = BorderStroke(1.dp, tokens.border),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .testTag("support_ticket_card_${ticket.ticketId}")
    ) {
        Column(
            modifier = Modifier.padding(metrics.cardPadding),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${ticket.ticketId} • ${ticket.orderId}",
                    style = MaterialTheme.typography.titleSmall,
                    color = tokens.ink,
                    modifier = Modifier.weight(1f)
                )
                Surface(
                    color = statusBg,
                    shape = RoundedCornerShape(50)
                ) {
                    Text(
                        text = ticket.status.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = statusFg,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                    )
                }
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Surface(
                    color = tokens.surfaceElevated,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = ticket.category.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = tokens.ink,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                    )
                }
                if (ticket.affectedAmountInr > 0) {
                    Text(
                        text = "Est. ₹${ticket.affectedAmountInr}",
                        style = MaterialTheme.typography.labelSmall,
                        color = tokens.deliveryGreenDeep,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = updatedFormatted,
                    style = MaterialTheme.typography.labelSmall,
                    color = tokens.muted
                )
            }

            Text(
                text = ticket.affectedItemsSummary,
                style = MaterialTheme.typography.bodySmall,
                color = tokens.ink,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = ticket.resolutionSummary,
                style = MaterialTheme.typography.labelMedium,
                color = tokens.deliveryGreenDeep,
                fontWeight = FontWeight.SemiBold
            )

            ticket.messages.lastOrNull()?.let { latest ->
                Surface(
                    color = tokens.surfaceElevated,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = (if (latest.isUser) "You: " else "Care Desk: ") + latest.text,
                        style = MaterialTheme.typography.bodySmall,
                        color = tokens.muted,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(9.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SupportIssueWizardContent(
    orders: List<OrderRecord>,
    initialOrderId: String?,
    initialCategory: SupportIssueCategory,
    onSubmit: (
        order: OrderRecord,
        category: SupportIssueCategory,
        selectedItemKeys: Set<String>,
        preferredResolution: SupportResolutionOption,
        userNote: String,
        photoUriString: String?
    ) -> Unit
) {
    val tokens = QuickKartThemeTokens.colors
    val metrics = QuickKartMetricsTokens.metrics

    var selectedOrderId by rememberSaveable(initialOrderId) {
        mutableStateOf(initialOrderId ?: orders.firstOrNull()?.orderId ?: "")
    }
    val selectedOrder = remember(orders, selectedOrderId) {
        orders.find { it.orderId == selectedOrderId } ?: orders.firstOrNull()
    }
    var selectedCategory by rememberSaveable(initialCategory) {
        mutableStateOf(initialCategory)
    }
    var selectedItemKeys by remember(selectedOrder?.orderId) {
        mutableStateOf(
            selectedOrder?.items?.firstOrNull()?.let { setOf(orderLineItemKey(it)) } ?: emptySet()
        )
    }
    var selectedResolution by rememberSaveable {
        mutableStateOf(SupportResolutionOption.INSTANT_CREDIT)
    }
    var noteText by rememberSaveable { mutableStateOf("") }
    var attachedPhotoUri by rememberSaveable { mutableStateOf<String?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            attachedPhotoUri = uri.toString()
        }
    }

    if (selectedOrder == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No orders available yet.",
                style = MaterialTheme.typography.bodyLarge,
                color = tokens.muted
            )
        }
        return
    }

    val estimatedCredit = computeSupportRefundEstimate(
        order = selectedOrder,
        selectedItemKeys = if (selectedCategory.requiresItemSelection) selectedItemKeys else emptySet(),
        category = selectedCategory
    )
    val needsItemPick = selectedCategory.requiresItemSelection && selectedItemKeys.isEmpty()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .imePadding(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .widthIn(max = 760.dp)
                .fillMaxSize(),
            contentPadding = PaddingValues(metrics.screenHorizontalPadding),
            verticalArrangement = Arrangement.spacedBy(metrics.itemSpacing)
        ) {
            // Step 1: Select Order
            item {
                Text(
                    text = "1. Select Order",
                    style = MaterialTheme.typography.titleMedium,
                    color = tokens.ink
                )
                Spacer(Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(orders, key = { it.orderId }) { order ->
                        val isSelected = order.orderId == selectedOrder.orderId
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (isSelected) tokens.brandYellowSoft else tokens.surface,
                            border = BorderStroke(
                                width = if (isSelected) 1.6.dp else 1.dp,
                                color = if (isSelected) tokens.brandYellow else tokens.border
                            ),
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { selectedOrderId = order.orderId }
                                .testTag("support_order_pick_${order.orderId}")
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Text(
                                    text = order.orderId,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = tokens.ink,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${order.totalItemCount} items • ₹${order.totalPaid}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = tokens.muted
                                )
                                Text(
                                    text = order.stage.title,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (order.isActive) tokens.deliveryGreenDeep else tokens.muted,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }

            // Step 2: Select Issue Category
            item {
                Text(
                    text = "2. What went wrong?",
                    style = MaterialTheme.typography.titleMedium,
                    color = tokens.ink
                )
                Spacer(Modifier.height(8.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SupportIssueCategory.entries.forEach { category ->
                        val selected = category == selectedCategory
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (selected) tokens.brandYellow else tokens.surface,
                            border = BorderStroke(
                                1.dp,
                                if (selected) tokens.brandYellow else tokens.border
                            ),
                            modifier = Modifier
                                .defaultMinSize(minHeight = 46.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    selectedCategory = category
                                    if (!category.requiresItemSelection && selectedResolution == SupportResolutionOption.EXPRESS_REPLACEMENT) {
                                        selectedResolution = SupportResolutionOption.INSTANT_CREDIT
                                    }
                                }
                                .testTag("support_cat_${category.name}")
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(
                                    text = category.label,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if (selected) tokens.onBrandYellow else tokens.ink,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = category.subtitle,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (selected) tokens.onBrandYellow.copy(alpha = 0.85f) else tokens.muted
                                )
                            }
                        }
                    }
                }
            }

            // Step 3: Affected Line Items (for item-level issues)
            if (selectedCategory.requiresItemSelection) {
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
                                    text = "3. Affected Item(s)",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = tokens.ink
                                )
                                Surface(
                                    color = tokens.deliveryGreenSoft,
                                    shape = RoundedCornerShape(50)
                                ) {
                                    Text(
                                        text = "Est. Credit ₹$estimatedCredit",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = tokens.deliveryGreenDeep,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            selectedOrder.items.forEach { item ->
                                val key = orderLineItemKey(item)
                                val checked = key in selectedItemKeys
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable {
                                            selectedItemKeys = if (checked) {
                                                selectedItemKeys - key
                                            } else {
                                                selectedItemKeys + key
                                            }
                                        }
                                        .padding(vertical = 4.dp)
                                        .testTag("support_item_check_${item.productId}"),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Checkbox(
                                        checked = checked,
                                        onCheckedChange = { nowChecked ->
                                            selectedItemKeys = if (nowChecked) {
                                                selectedItemKeys + key
                                            } else {
                                                selectedItemKeys - key
                                            }
                                        },
                                        colors = CheckboxDefaults.colors(
                                            checkedColor = tokens.deliveryGreenDeep,
                                            checkmarkColor = Color.White
                                        )
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "${item.quantity}x ${item.productName}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = tokens.ink,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            text = item.variantLabel,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = tokens.muted
                                        )
                                    }
                                    Text(
                                        text = "₹${item.lineTotal}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = tokens.ink,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Step 4: Details & Optional Zero-Permission Photo Proof
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
                        Text(
                            text = if (selectedCategory.requiresItemSelection) {
                                "4. Details & Optional Photo"
                            } else {
                                "3. Details & Optional Photo"
                            },
                            style = MaterialTheme.typography.titleSmall,
                            color = tokens.ink
                        )

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            selectedCategory.defaultPrompts.forEach { prompt ->
                                val active = noteText == prompt
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = if (active) tokens.brandYellowSoft else tokens.surfaceElevated,
                                    border = BorderStroke(
                                        1.dp,
                                        if (active) tokens.brandYellow else tokens.border
                                    ),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50))
                                        .clickable { noteText = prompt }
                                ) {
                                    Text(
                                        text = prompt,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = tokens.ink,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = noteText,
                            onValueChange = { noteText = it },
                            placeholder = { Text("Describe the issue briefly (optional)") },
                            maxLines = 3,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("support_note_input")
                        )

                        if (attachedPhotoUri == null) {
                            OutlinedButton(
                                onClick = {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .defaultMinSize(minHeight = 48.dp)
                                    .testTag("support_attach_photo_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.AddPhotoAlternate,
                                    contentDescription = "Attach photo proof",
                                    modifier = Modifier.size(metrics.iconMedium)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text("Attach Photo Proof (Optional)")
                            }
                        } else {
                            Surface(
                                color = tokens.deliveryGreenSoft,
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, tokens.deliveryGreen.copy(alpha = 0.4f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.PhotoCamera,
                                        contentDescription = null,
                                        tint = tokens.deliveryGreenDeep,
                                        modifier = Modifier.size(metrics.iconMedium)
                                    )
                                    Text(
                                        text = "Photo proof attached",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = tokens.deliveryGreenDeep,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.weight(1f)
                                    )
                                    IconButton(
                                        onClick = { attachedPhotoUri = null },
                                        modifier = Modifier.size(40.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Close,
                                            contentDescription = "Remove photo",
                                            tint = tokens.deliveryGreenDeep
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Step 5: Preferred Resolution
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
                        Text(
                            text = if (selectedCategory.requiresItemSelection) {
                                "5. Preferred Resolution"
                            } else {
                                "4. Preferred Resolution"
                            },
                            style = MaterialTheme.typography.titleSmall,
                            color = tokens.ink
                        )

                        SupportResolutionOption.entries.forEach { option ->
                            if (option == SupportResolutionOption.EXPRESS_REPLACEMENT &&
                                !selectedCategory.requiresItemSelection
                            ) {
                                return@forEach
                            }
                            val selected = selectedResolution == option
                            val optionTitle = if (option == SupportResolutionOption.INSTANT_CREDIT && estimatedCredit > 0) {
                                "${option.title} (₹$estimatedCredit)"
                            } else {
                                option.title
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
                                    .clickable { selectedResolution = option }
                                    .testTag("support_res_${option.name}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    RadioButton(
                                        selected = selected,
                                        onClick = { selectedResolution = option },
                                        colors = RadioButtonDefaults.colors(
                                            selectedColor = tokens.deliveryGreenDeep
                                        )
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = optionTitle,
                                            style = MaterialTheme.typography.labelLarge,
                                            color = tokens.ink,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = option.subtitle,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = tokens.muted
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Submit CTA
            item {
                if (needsItemPick) {
                    Text(
                        text = "Select at least one affected item above to continue.",
                        style = MaterialTheme.typography.bodySmall,
                        color = tokens.alertRed
                    )
                    Spacer(Modifier.height(6.dp))
                }
                Button(
                    onClick = {
                        onSubmit(
                            selectedOrder,
                            selectedCategory,
                            if (selectedCategory.requiresItemSelection) selectedItemKeys else emptySet(),
                            selectedResolution,
                            noteText,
                            attachedPhotoUri
                        )
                    },
                    enabled = !needsItemPick,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = tokens.deliveryGreenDeep,
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 52.dp)
                        .testTag("submit_support_ticket_button")
                ) {
                    Text(
                        text = "Submit & Get Resolution",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.height(12.dp))
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SupportTicketThreadContent(
    ticket: SupportTicketRecord,
    onSendReply: (String) -> Unit,
    onResolveTicket: () -> Unit,
    onReopenTicket: () -> Unit,
    onSubmitCsat: (Int) -> Unit
) {
    val tokens = QuickKartThemeTokens.colors
    val metrics = QuickKartMetricsTokens.metrics
    var replyDraft by rememberSaveable(ticket.ticketId) { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .widthIn(max = 760.dp)
                .fillMaxWidth(),
            contentPadding = PaddingValues(metrics.screenHorizontalPadding),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 3-Step Ticket Timeline + Summary Card
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
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            SupportTicketStatus.entries.forEach { step ->
                                val done = step.stepIndex <= ticket.status.stepIndex
                                Column(
                                    modifier = Modifier.weight(1f),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (done) tokens.deliveryGreenDeep else tokens.surfaceElevated
                                            )
                                    ) {
                                        if (done) {
                                            Icon(
                                                imageVector = Icons.Rounded.Check,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(metrics.iconSmall)
                                            )
                                        } else {
                                            Text(
                                                text = "${step.stepIndex + 1}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = tokens.muted
                                            )
                                        }
                                    }
                                    Text(
                                        text = step.label,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (done) tokens.ink else tokens.muted,
                                        fontWeight = if (done) FontWeight.Bold else FontWeight.Normal,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }

                        HorizontalDivider(color = tokens.border)

                        Text(
                            text = ticket.affectedItemsSummary,
                            style = MaterialTheme.typography.bodyMedium,
                            color = tokens.ink,
                            fontWeight = FontWeight.SemiBold
                        )

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Surface(
                                color = tokens.deliveryGreenSoft,
                                shape = RoundedCornerShape(50)
                            ) {
                                Text(
                                    text = ticket.resolutionSummary,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = tokens.deliveryGreenDeep,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                            if (!ticket.photoUriString.isNullOrBlank()) {
                                Surface(
                                    color = tokens.brandYellowSoft,
                                    shape = RoundedCornerShape(50)
                                ) {
                                    Text(
                                        text = "Photo Proof Attached",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = tokens.ink,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            if (ticket.status != SupportTicketStatus.RESOLVED) {
                                TextButton(
                                    onClick = onResolveTicket,
                                    modifier = Modifier.testTag("resolve_ticket_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.CheckCircle,
                                        contentDescription = null,
                                        tint = tokens.deliveryGreenDeep,
                                        modifier = Modifier.size(metrics.iconSmall)
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        text = "Mark Resolved",
                                        color = tokens.deliveryGreenDeep,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            } else {
                                TextButton(
                                    onClick = onReopenTicket,
                                    modifier = Modifier.testTag("reopen_ticket_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Refresh,
                                        contentDescription = null,
                                        tint = tokens.ink,
                                        modifier = Modifier.size(metrics.iconSmall)
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        text = "Reopen Ticket",
                                        color = tokens.ink,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Conversation Thread Bubbles
            items(ticket.messages, key = { it.id }) { msg ->
                SupportMessageBubble(message = msg)
            }

            // Post-Resolution CSAT Rating Card
            if (ticket.status == SupportTicketStatus.RESOLVED) {
                item {
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = tokens.surface),
                        border = BorderStroke(1.5.dp, tokens.brandYellow),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("support_csat_card")
                    ) {
                        Column(
                            modifier = Modifier.padding(metrics.cardPadding),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = if (ticket.csatRating != null) {
                                    "You rated QuickKart Care ${ticket.csatRating}/5 ★"
                                } else {
                                    "How did we do on this support request?"
                                },
                                style = MaterialTheme.typography.titleSmall,
                                color = tokens.ink,
                                textAlign = TextAlign.Center
                            )
                            Row(horizontalArrangement = Arrangement.Center) {
                                (1..5).forEach { star ->
                                    val filled = star <= (ticket.csatRating ?: 0)
                                    IconButton(
                                        onClick = { onSubmitCsat(star) },
                                        modifier = Modifier
                                            .size(48.dp)
                                            .testTag("support_csat_star_$star")
                                    ) {
                                        Icon(
                                            imageVector = if (filled) Icons.Rounded.Star else Icons.Rounded.StarOutline,
                                            contentDescription = "Rate support $star stars",
                                            tint = if (filled) AmberGold else tokens.muted,
                                            modifier = Modifier.size(metrics.iconLarge)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Bottom Quick Reply Chips + Input Bar
        Surface(
            color = tokens.surface,
            tonalElevation = 3.dp,
            border = BorderStroke(1.dp, tokens.border),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 760.dp)
                    .fillMaxWidth()
                    .padding(horizontal = metrics.screenHorizontalPadding, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val quickReplies = if (ticket.status == SupportTicketStatus.RESOLVED) {
                    listOf("Still need help", "Request callback")
                } else {
                    listOf(
                        "Switch to Instant Credit",
                        "Send 8-Min Replacement",
                        "Request Specialist Callback",
                        "Issue Resolved"
                    )
                }

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(quickReplies) { quickText ->
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = tokens.brandYellowSoft,
                            border = BorderStroke(1.dp, tokens.brandYellow),
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .clickable { onSendReply(quickText) }
                        ) {
                            Text(
                                text = quickText,
                                style = MaterialTheme.typography.labelSmall,
                                color = tokens.ink,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = replyDraft,
                        onValueChange = { replyDraft = it },
                        placeholder = { Text("Message QuickKart Care Desk...") },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("support_reply_input")
                    )
                    Button(
                        onClick = {
                            if (replyDraft.isNotBlank()) {
                                onSendReply(replyDraft)
                                replyDraft = ""
                            }
                        },
                        enabled = replyDraft.isNotBlank(),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = tokens.deliveryGreenDeep,
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .defaultMinSize(minWidth = 52.dp, minHeight = 52.dp)
                            .testTag("send_support_reply_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.Send,
                            contentDescription = "Send message",
                            modifier = Modifier.size(metrics.iconMedium)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SupportMessageBubble(message: SupportMessage) {
    val tokens = QuickKartThemeTokens.colors
    val timeLabel = remember(message.timestampMillis) {
        SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(message.timestampMillis))
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            color = if (message.isUser) tokens.deliveryGreenDeep else tokens.surface,
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (message.isUser) 16.dp else 4.dp,
                bottomEnd = if (message.isUser) 4.dp else 16.dp
            ),
            border = if (message.isUser) null else BorderStroke(1.dp, tokens.border),
            modifier = Modifier.widthIn(max = 520.dp)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (!message.isUser) {
                    Text(
                        text = "QuickKart Care Desk",
                        style = MaterialTheme.typography.labelSmall,
                        color = tokens.deliveryGreenDeep,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (message.isUser) Color.White else tokens.ink
                )
                Text(
                    text = timeLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (message.isUser) Color.White.copy(alpha = 0.72f) else tokens.muted,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}
