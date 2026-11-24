package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.LocalOffer
import androidx.compose.material.icons.rounded.LocalShipping
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.SupportAgent
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.OrderStage
import com.example.data.model.Product
import com.example.data.model.PromoCode
import com.example.data.model.SupportTicketStatus
import com.example.ui.QuickKartUiState
import com.example.ui.REFERRAL_REWARD_INR
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.QuickKartThemeTokens
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

// ───────────────────────── Notification inbox ─────────────────────────

enum class InboxKind(val icon: ImageVector) {
    ORDER(Icons.Rounded.LocalShipping),
    WALLET(Icons.Rounded.AccountBalanceWallet),
    SUPPORT(Icons.Rounded.SupportAgent)
}

data class InboxItem(
    val id: String,
    val kind: InboxKind,
    val title: String,
    val body: String,
    val timeMillis: Long
)

/** Inbox is derived from orders, wallet activity and support tickets already in the app. */
fun buildInboxItems(uiState: QuickKartUiState, limit: Int = 30): List<InboxItem> {
    val orders = uiState.orders.map { order ->
        InboxItem(
            id = "order_${order.orderId}",
            kind = InboxKind.ORDER,
            title = when (order.stage) {
                OrderStage.DELIVERED -> "Order #${order.orderId} delivered"
                else -> "Order #${order.orderId} • ${order.stage.title}"
            },
            body = "₹${order.totalPaid} • ${order.deliveryAddressLabel} • ${order.deliveryWindowLabel}",
            timeMillis = order.createdAtMillis
        )
    }
    val wallet = uiState.walletTransactions.filter { it.isCredit }.map { tx ->
        InboxItem(
            id = "wallet_${tx.id}",
            kind = InboxKind.WALLET,
            title = "₹${tx.amountInr} credited • ${tx.title}",
            body = tx.subtitle,
            timeMillis = tx.createdAtMillis
        )
    }
    val tickets = uiState.supportTickets.map { t ->
        InboxItem(
            id = "ticket_${t.ticketId}",
            kind = InboxKind.SUPPORT,
            title = "${t.ticketId} • ${t.status.label}",
            body = if (t.status == SupportTicketStatus.RESOLVED) t.resolutionSummary
            else "${t.category.label} on #${t.orderId}",
            timeMillis = t.updatedAtMillis
        )
    }
    return (orders + wallet + tickets).sortedByDescending { it.timeMillis }.take(limit)
}

fun unreadInboxCount(uiState: QuickKartUiState): Int =
    buildInboxItems(uiState).count { it.timeMillis > uiState.preferences.inboxLastSeenMillis }

@Composable
fun NotificationInboxDialog(
    items: List<InboxItem>,
    lastSeenMillis: Long,
    onDismiss: () -> Unit
) {
    val tokens = QuickKartThemeTokens.colors
    val fmt = remember { SimpleDateFormat("dd MMM • hh:mm a", Locale.getDefault()) }
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(tokens.background.copy(alpha = 0.95f))
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = tokens.surface),
                border = BorderStroke(1.dp, tokens.border),
                modifier = Modifier
                    .widthIn(max = 560.dp)
                    .fillMaxWidth()
                    .testTag("notification_inbox_dialog")
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "Notifications",
                            style = MaterialTheme.typography.titleLarge,
                            color = tokens.ink,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_inbox_button")) {
                            Icon(Icons.Rounded.Close, contentDescription = "Close notifications", tint = tokens.muted)
                        }
                    }
                    if (items.isEmpty()) {
                        Text("You're all caught up.", style = MaterialTheme.typography.bodyMedium, color = tokens.muted)
                    } else {
                        LazyColumn(
                            modifier = Modifier.heightIn(max = 520.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(items, key = { it.id }) { item ->
                                val unread = item.timeMillis > lastSeenMillis
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = if (unread) tokens.brandYellowSoft else tokens.surfaceElevated,
                                    border = BorderStroke(1.dp, if (unread) tokens.brandYellow else tokens.border),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Surface(color = tokens.surface, shape = CircleShape, modifier = Modifier.size(36.dp)) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(item.kind.icon, contentDescription = null, tint = tokens.ink, modifier = Modifier.size(20.dp))
                                            }
                                        }
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(item.title, style = MaterialTheme.typography.titleSmall, color = tokens.ink)
                                            Text(
                                                item.body,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = tokens.muted,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(fmt.format(Date(item.timeMillis)), style = MaterialTheme.typography.labelSmall, color = tokens.muted)
                                        }
                                        if (unread) {
                                            Box(
                                                modifier = Modifier
                                                    .padding(top = 6.dp)
                                                    .size(8.dp)
                                                    .clip(CircleShape)
                                                    .background(tokens.alertRed)
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
}

// ───────────────────────── Recent & trending searches ─────────────────────────

val TrendingSearches = listOf("A2 milk", "Avocado", "Sourdough", "Eggs", "Cold brew", "Paneer")

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SearchSuggestionsPanel(
    recent: List<String>,
    onPick: (String) -> Unit,
    onClearRecent: () -> Unit
) {
    val tokens = QuickKartThemeTokens.colors
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = tokens.surface,
        border = BorderStroke(1.dp, tokens.border),
        shadowElevation = 6.dp,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("search_suggestions_panel")
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            if (recent.isNotEmpty()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.History, contentDescription = null, tint = tokens.muted, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Recent searches", style = MaterialTheme.typography.labelLarge, color = tokens.ink, modifier = Modifier.weight(1f))
                    TextButton(
                        onClick = onClearRecent,
                        colors = ButtonDefaults.textButtonColors(contentColor = tokens.deliveryGreenDeep),
                        modifier = Modifier.testTag("clear_recent_searches")
                    ) { Text("Clear", fontWeight = FontWeight.Bold) }
                }
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    recent.forEach { q -> SuggestionChip(q, "recent_search_$q") { onPick(q) } }
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.AutoMirrored.Rounded.TrendingUp, contentDescription = null, tint = tokens.muted, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Trending near you", style = MaterialTheme.typography.labelLarge, color = tokens.ink)
            }
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                TrendingSearches.forEach { q -> SuggestionChip(q, "trending_search_$q") { onPick(q) } }
            }
        }
    }
}

@Composable
private fun SuggestionChip(text: String, testTag: String, onClick: () -> Unit) {
    val tokens = QuickKartThemeTokens.colors
    Surface(
        shape = RoundedCornerShape(50),
        color = tokens.surfaceElevated,
        border = BorderStroke(1.dp, tokens.border),
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .clickable(onClick = onClick)
            .testTag(testTag)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = tokens.ink,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
        )
    }
}

// ───────────────────────── Product ratings & reviews ─────────────────────────

private data class SampleReview(val name: String, val stars: Int, val text: String, val daysAgo: Int)

private val ReviewPool = listOf(
    SampleReview("Ananya R.", 5, "Fresh and arrived in under 10 minutes. Will reorder!", 2),
    SampleReview("Rohit K.", 4, "Good quality and well packed. Slightly pricey but worth it.", 5),
    SampleReview("Meera S.", 5, "Exactly as described. The delivery partner was very polite.", 8),
    SampleReview("Vikram P.", 4, "Tastes great. Would love a bigger pack size.", 12),
    SampleReview("Sneha T.", 3, "Decent, but one pack was close to its date.", 15),
    SampleReview("Arjun M.", 5, "My weekly staple now — always fresh.", 20)
)

/** Read-only ratings breakdown and sample reviews (prototype data seeded per product). */
@Composable
fun ProductReviewsSection(product: Product) {
    val tokens = QuickKartThemeTokens.colors
    val seed = abs(product.id.hashCode())
    val reviews = remember(product.id) {
        List(3) { i -> ReviewPool[(seed + i * 2) % ReviewPool.size] }
    }
    // Star distribution shaped around the product's average rating.
    val distribution = remember(product.id) {
        val r = product.rating
        val five = ((r - 3f) / 2f).coerceIn(0.2f, 0.85f)
        val four = (1f - five) * 0.6f
        val three = (1f - five - four) * 0.6f
        val two = (1f - five - four - three) * 0.6f
        val one = (1f - five - four - three - two).coerceAtLeast(0f)
        listOf(five, four, three, two, one)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("product_reviews_section"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("Ratings & Reviews", style = MaterialTheme.typography.titleMedium, color = tokens.ink, fontWeight = FontWeight.Bold)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    String.format(Locale.getDefault(), "%.1f", product.rating),
                    style = MaterialTheme.typography.headlineMedium,
                    color = tokens.ink,
                    fontWeight = FontWeight.ExtraBold
                )
                StarRow(product.rating.roundToInt())
                Text("${product.ratingCount} ratings", style = MaterialTheme.typography.labelSmall, color = tokens.muted)
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                distribution.forEachIndexed { index, fraction ->
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("${5 - index}", style = MaterialTheme.typography.labelSmall, color = tokens.muted)
                        LinearProgressIndicator(
                            progress = { fraction },
                            color = tokens.deliveryGreenDeep,
                            trackColor = tokens.surfaceElevated,
                            modifier = Modifier
                                .weight(1f)
                                .height(6.dp)
                                .clip(RoundedCornerShape(50))
                        )
                    }
                }
            }
        }
        reviews.forEach { review ->
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = tokens.surfaceElevated,
                border = BorderStroke(1.dp, tokens.border),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(review.name, style = MaterialTheme.typography.titleSmall, color = tokens.ink, modifier = Modifier.weight(1f))
                        Text("${review.daysAgo}d ago", style = MaterialTheme.typography.labelSmall, color = tokens.muted)
                    }
                    StarRow(review.stars, size = 14)
                    Text(review.text, style = MaterialTheme.typography.bodySmall, color = tokens.ink)
                    Text("Verified purchase", style = MaterialTheme.typography.labelSmall, color = tokens.deliveryGreenDeep)
                }
            }
        }
    }
}

@Composable
private fun StarRow(stars: Int, size: Int = 16) {
    Row {
        repeat(5) { i ->
            Icon(
                Icons.Rounded.Star,
                contentDescription = null,
                tint = if (i < stars) Color(0xFFF59E0B) else QuickKartThemeTokens.colors.border,
                modifier = Modifier.size(size.dp)
            )
        }
    }
}

// ───────────────────────── Offers & referral (Account section content) ─────────────────────────

@Composable
fun ColumnScope.OffersAndRewardsContent(
    promos: List<PromoCode>,
    referralCode: String,
    referralRedeemed: Boolean,
    onRedeemReferral: (String) -> String,
    onShowMessage: (String) -> Unit
) {
    val tokens = QuickKartThemeTokens.colors
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    var redeemInput by remember { mutableStateOf("") }

    // Referral card
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF141414) /* brand black card, like the profile header */,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("referral_card")
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                "Invite friends, get ₹$REFERRAL_REWARD_INR each",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Your friend gets ₹$REFERRAL_REWARD_INR in Wallet on sign-up and you get ₹$REFERRAL_REWARD_INR after their first order.",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.85f)
            )
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color.White.copy(alpha = 0.16f),
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable {
                            clipboard.setText(AnnotatedString(referralCode))
                            onShowMessage("Referral code copied")
                        }
                ) {
                    Text(
                        referralCode,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontFamily = JetBrainsMonoFontFamily,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                            .testTag("referral_code_text")
                    )
                }
                Button(
                    onClick = {
                        val share = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(
                                Intent.EXTRA_TEXT,
                                "Get groceries in 8 minutes on QuickKart! Use my code $referralCode for ₹$REFERRAL_REWARD_INR off."
                            )
                        }
                        context.startActivity(Intent.createChooser(share, "Share referral code"))
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = tokens.brandYellow, contentColor = tokens.onBrandYellow),
                    modifier = Modifier
                        .height(48.dp)
                        .testTag("share_referral_button")
                ) {
                    Icon(Icons.Rounded.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Share")
                }
            }
        }
    }

    // Redeem a friend's code
    if (!referralRedeemed) {
        Text("Have a referral code?", style = MaterialTheme.typography.labelLarge, color = tokens.ink)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(
                value = redeemInput,
                onValueChange = { redeemInput = it.uppercase().filter { ch -> ch.isLetterOrDigit() }.take(12) },
                placeholder = { Text("e.g. QKFRIEND50") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = tokens.ink, unfocusedBorderColor = tokens.border),
                modifier = Modifier
                    .weight(1f)
                    .testTag("referral_redeem_input")
            )
            OutlinedButton(
                onClick = {
                    onShowMessage(onRedeemReferral(redeemInput))
                    redeemInput = ""
                },
                enabled = redeemInput.isNotBlank(),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, tokens.border),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = tokens.ink),
                modifier = Modifier
                    .height(52.dp)
                    .testTag("referral_redeem_button")
            ) { Text("Redeem", fontWeight = FontWeight.Bold) }
        }
    } else {
        Text(
            "Referral reward already redeemed on this account.",
            style = MaterialTheme.typography.bodySmall,
            color = tokens.muted
        )
    }

    // Offers list
    Text("Available offers", style = MaterialTheme.typography.labelLarge, color = tokens.ink)
    promos.forEach { promo ->
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = tokens.surfaceElevated,
            border = BorderStroke(1.dp, tokens.border),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("offer_${promo.code}")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(Icons.Rounded.LocalOffer, contentDescription = null, tint = tokens.promoOrange)
                Column(modifier = Modifier.weight(1f)) {
                    Text(promo.code, style = MaterialTheme.typography.titleSmall, color = tokens.ink, fontFamily = JetBrainsMonoFontFamily, fontWeight = FontWeight.Bold)
                    Text(promo.title, style = MaterialTheme.typography.bodySmall, color = tokens.muted)
                }
                TextButton(
                    onClick = {
                        clipboard.setText(AnnotatedString(promo.code))
                        onShowMessage("${promo.code} copied • apply it at checkout")
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = tokens.ink)
                ) { Text("Copy", fontWeight = FontWeight.Bold) }
            }
        }
    }
    Text(
        "Tip: at checkout tap \"Apply best offer\" and we'll pick the biggest saving for your basket.",
        style = MaterialTheme.typography.bodySmall,
        color = tokens.muted
    )
}
