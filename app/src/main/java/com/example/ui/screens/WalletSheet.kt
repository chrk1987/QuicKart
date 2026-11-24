package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Wallet
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.WalletTransaction
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.QuickKartMetricsTokens
import com.example.ui.theme.QuickKartThemeTokens
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun WalletSheet(
    balanceInr: Int,
    transactions: List<WalletTransaction>,
    onDismiss: () -> Unit,
    onAddMoney: (amountInr: Int, methodNote: String) -> Unit
) {
    val tokens = QuickKartThemeTokens.colors
    val timeFmt = SimpleDateFormat("dd MMM • hh:mm a", Locale.getDefault())
    var showAddMoneyDialog by remember { mutableStateOf(false) }
    var filter by remember { mutableStateOf(PassbookFilter.ALL) }
    var selectedReceipt by remember { mutableStateOf<WalletTransaction?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(tokens.background.copy(alpha = 0.95f))
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                shape = RoundedCornerShape(26.dp),
                color = tokens.surface,
                border = BorderStroke(1.dp, tokens.border),
                shadowElevation = 8.dp,
                modifier = Modifier
                    .widthIn(max = 560.dp)
                    .fillMaxWidth()
                    .testTag("wallet_dialog")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                color = tokens.brandYellow,
                                shape = CircleShape,
                                modifier = Modifier.size(44.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Rounded.Wallet,
                                        contentDescription = null,
                                        tint = tokens.onBrandYellow,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = "QuickKart Money",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = tokens.ink
                                )
                                Text(
                                    text = "Instant 1-tap checkout & instant refunds",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = tokens.muted
                                )
                            }
                        }
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.testTag("close_wallet_dialog")
                        ) {
                            Icon(Icons.Rounded.Close, contentDescription = "Close wallet")
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(22.dp),
                        color = tokens.deliveryGreenDeep,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "AVAILABLE WALLET BALANCE",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White.copy(alpha = 0.85f)
                                    )
                                    Text(
                                        text = "₹$balanceInr",
                                        style = MaterialTheme.typography.displaySmall,
                                        color = Color.White,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                                Surface(
                                    color = Color.White.copy(alpha = 0.16f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "Zero Fee",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = tokens.brandYellow,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Button(
                                onClick = { showAddMoneyDialog = true },
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = tokens.brandYellow,
                                    contentColor = tokens.onBrandYellow
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("add_money_to_wallet_button")
                            ) {
                                Icon(Icons.Rounded.Add, contentDescription = null)
                                Spacer(Modifier.width(6.dp))
                                Text("Add Money to Wallet", style = MaterialTheme.typography.labelLarge)
                            }
                        }
                    }

                    Text(
                        text = "Passbook",
                        style = MaterialTheme.typography.titleMedium,
                        color = tokens.ink,
                        fontWeight = FontWeight.Bold
                    )

                    MonthSummaryRow(transactions)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PassbookFilter.entries.forEach { f ->
                            val selected = f == filter
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = if (selected) tokens.ink else tokens.surfaceElevated,
                                border = BorderStroke(1.dp, if (selected) tokens.ink else tokens.border),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .clickable { filter = f }
                                    .testTag("passbook_filter_${f.name.lowercase()}")
                            ) {
                                Text(
                                    text = f.label,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (selected) tokens.background else tokens.ink,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                                )
                            }
                        }
                    }

                    val visible = transactions
                        .filter { filter.matches(it) }
                        .sortedByDescending { it.createdAtMillis }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(vertical = 4.dp)
                    ) {
                        if (visible.isEmpty()) {
                            item {
                                Text(
                                    text = "No ${filter.label.lowercase()} transactions yet.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = tokens.muted,
                                    modifier = Modifier.padding(vertical = 12.dp)
                                )
                            }
                        }
                        items(visible, key = { it.id }) { tx ->
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = tokens.surfaceElevated,
                                border = BorderStroke(1.dp, tokens.border),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable { selectedReceipt = tx }
                                    .testTag("passbook_tx_${tx.id}")
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        modifier = Modifier.weight(1f),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Surface(
                                            color = if (tx.isCredit) tokens.deliveryGreenSoft else tokens.brandYellowSoft,
                                            shape = CircleShape,
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = if (tx.isCredit) Icons.Rounded.ArrowDownward else Icons.Rounded.ArrowUpward,
                                                    contentDescription = null,
                                                    tint = if (tx.isCredit) tokens.deliveryGreenDeep else tokens.ink,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                        Column {
                                            Text(
                                                text = tx.title,
                                                style = MaterialTheme.typography.titleSmall,
                                                color = tokens.ink
                                            )
                                            Text(
                                                text = "${tx.subtitle} • ${timeFmt.format(Date(tx.createdAtMillis))}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = tokens.muted
                                            )
                                        }
                                    }

                                    Text(
                                        text = "${if (tx.isCredit) "+" else "-"}₹${tx.amountInr}",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontFamily = JetBrainsMonoFontFamily,
                                        color = if (tx.isCredit) tokens.deliveryGreenDeep else tokens.ink,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        softWrap = false,
                                        modifier = Modifier.padding(start = 8.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddMoneyDialog) {
        // Money is credited only after the payment flow reports success.
        WalletTopUpFlow(
            currentBalanceInr = balanceInr,
            onDismiss = { showAddMoneyDialog = false },
            onPaymentSuccess = onAddMoney
        )
    }

    selectedReceipt?.let { tx ->
        WalletTransactionReceiptDialog(transaction = tx, onDismiss = { selectedReceipt = null })
    }
}

private enum class PassbookFilter(val label: String) {
    ALL("All"),
    ADDED("Added"),
    SPENT("Spent"),
    REFUNDS("Refunds & rewards");

    fun matches(tx: WalletTransaction): Boolean = when (this) {
        ALL -> true
        ADDED -> tx.isCredit && tx.isTopUp
        SPENT -> !tx.isCredit
        REFUNDS -> tx.isCredit && !tx.isTopUp
    }
}

/** Top-ups are the "Money Added" credits; other credits are refunds, bonuses and cashback. */
private val WalletTransaction.isTopUp: Boolean get() = title == "Money Added"

@Composable
private fun MonthSummaryRow(transactions: List<WalletTransaction>) {
    val tokens = QuickKartThemeTokens.colors
    val monthStart = Calendar.getInstance().apply {
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
    val thisMonth = transactions.filter { it.createdAtMillis >= monthStart }
    val added = thisMonth.filter { it.isCredit }.sumOf { it.amountInr }
    val spent = thisMonth.filter { !it.isCredit }.sumOf { it.amountInr }
    val monthName = SimpleDateFormat("MMMM", Locale.getDefault()).format(Date())

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("passbook_month_summary"),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        SummaryTile("Received in $monthName", "+₹$added", tokens.deliveryGreenDeep, Modifier.weight(1f))
        SummaryTile("Spent in $monthName", "-₹$spent", tokens.ink, Modifier.weight(1f))
    }
}

@Composable
private fun SummaryTile(label: String, value: String, valueColor: Color, modifier: Modifier) {
    val tokens = QuickKartThemeTokens.colors
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = tokens.surfaceElevated,
        border = BorderStroke(1.dp, tokens.border),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = tokens.muted)
            Text(
                value,
                style = MaterialTheme.typography.titleMedium,
                color = valueColor,
                fontFamily = JetBrainsMonoFontFamily,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/** Receipt for one wallet transaction (amount, method/details, date, transaction ID). */
@Composable
fun WalletTransactionReceiptDialog(transaction: WalletTransaction, onDismiss: () -> Unit) {
    val tokens = QuickKartThemeTokens.colors
    val accent = if (transaction.isCredit) tokens.deliveryGreenDeep else tokens.ink
    val dateText = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        .format(Date(transaction.createdAtMillis))
    val type = when {
        !transaction.isCredit -> "Debit • Payment"
        transaction.isTopUp -> "Credit • Wallet top-up"
        else -> "Credit • Refund / reward"
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = tokens.surface),
            border = BorderStroke(1.dp, tokens.border),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("wallet_receipt_dialog")
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    color = if (transaction.isCredit) tokens.deliveryGreenSoft else tokens.brandYellowSoft,
                    shape = CircleShape,
                    modifier = Modifier.size(56.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (transaction.isCredit) Icons.Rounded.ArrowDownward else Icons.Rounded.ArrowUpward,
                            contentDescription = null,
                            tint = accent,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
                Text(
                    text = "${if (transaction.isCredit) "+" else "-"}₹${transaction.amountInr}",
                    style = MaterialTheme.typography.headlineMedium,
                    color = accent,
                    fontFamily = JetBrainsMonoFontFamily,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = transaction.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = tokens.ink,
                    fontWeight = FontWeight.Bold
                )

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = tokens.surfaceElevated,
                    border = BorderStroke(1.dp, tokens.border),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ReceiptLine("Details", transaction.subtitle)
                        ReceiptLine("Type", type)
                        ReceiptLine("Date", dateText)
                        ReceiptLine("Transaction ID", transaction.id, mono = true)
                        ReceiptLine("Status", "Success", valueColor = tokens.deliveryGreenDeep)
                    }
                }

                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = tokens.brandYellow,
                        contentColor = tokens.onBrandYellow
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("wallet_receipt_close")
                ) {
                    Text("Close", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

@Composable
private fun ReceiptLine(
    label: String,
    value: String,
    mono: Boolean = false,
    valueColor: Color = QuickKartThemeTokens.colors.ink
) {
    val tokens = QuickKartThemeTokens.colors
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = tokens.muted)
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = valueColor,
            fontWeight = FontWeight.SemiBold,
            fontFamily = if (mono) JetBrainsMonoFontFamily else null,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f)
        )
    }
}
