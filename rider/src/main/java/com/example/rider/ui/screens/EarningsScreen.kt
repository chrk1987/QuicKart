package com.example.rider.ui.screens

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.CurrencyRupee
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.rider.data.model.COD_FLOATING_LIMIT_INR
import com.example.rider.data.model.PayoutTxType
import com.example.rider.data.model.RiderPaymentMode
import com.example.rider.data.model.defaultIncentiveTiers
import com.example.rider.ui.RiderUiState
import com.example.rider.ui.theme.JetBrainsMonoFontFamily
import com.example.rider.ui.theme.RiderMetricsTokens
import com.example.rider.ui.theme.RiderThemeTokens
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun EarningsScreen(
    state: RiderUiState,
    onWithdrawBalance: () -> Unit,
    onSettleCodCash: () -> Unit
) {
    val tokens = RiderThemeTokens.colors
    val metrics = RiderMetricsTokens.metrics
    val earnings = state.earnings
    val tiers = defaultIncentiveTiers()
    val timeFmt = SimpleDateFormat("hh:mm a", Locale.getDefault())

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
            // 1. Wallet Balance & Instant UPI Withdraw Card
            item {
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = tokens.deliveryGreenDeep,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "WITHDRAWABLE WALLET",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                                Text(
                                    text = "₹${earnings.availableWalletInr}",
                                    style = MaterialTheme.typography.displaySmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                            Surface(
                                color = Color.White.copy(alpha = 0.16f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalAlignment = Alignment.End
                                ) {
                                    Text(
                                        text = "Today Gross",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White.copy(alpha = 0.85f)
                                    )
                                    Text(
                                        text = "₹${earnings.totalEarnedInr}",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = tokens.brandYellow,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Button(
                            onClick = onWithdrawBalance,
                            enabled = earnings.availableWalletInr > 0,
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = tokens.brandYellow,
                                contentColor = tokens.onBrandYellow
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("withdraw_wallet_button")
                        ) {
                            Icon(Icons.Rounded.AccountBalance, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = if (earnings.availableWalletInr > 0) {
                                    "Instant UPI Withdraw ₹${earnings.availableWalletInr}"
                                } else {
                                    "Complete a drop to withdraw"
                                },
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }
            }

            // 2. Earnings Breakdown
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = tokens.surface),
                    border = BorderStroke(1.dp, tokens.border),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Payout Breakdown (${earnings.completedDrops} drops)",
                            style = MaterialTheme.typography.titleMedium,
                            color = tokens.ink,
                            fontWeight = FontWeight.Bold
                        )
                        BreakdownRow("Base + Distance Pay", "₹${earnings.baseAndDistanceInr}")
                        BreakdownRow("Peak Surge Bonus", "₹${earnings.surgeEarnedInr}")
                        BreakdownRow("Customer Tips (100% yours)", "₹${earnings.tipsEarnedInr}")
                        BreakdownRow("10-Min On-Time Bonus", "₹${earnings.onTimeBonusInr}")
                        BreakdownRow("Milestone Incentives Unlocked", "₹${earnings.unlockedIncentiveInr}")
                        HorizontalDivider(color = tokens.border)
                        BreakdownRow("Total Earned Today", "₹${earnings.totalEarnedInr}", bold = true)
                    }
                }
            }

            // 3. COD Cash-In-Hand Ledger Card
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = tokens.surface),
                    border = BorderStroke(
                        1.dp,
                        if (earnings.isCodLimitExceeded) tokens.alertRed else tokens.border
                    ),
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
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Payments,
                                    contentDescription = null,
                                    tint = tokens.promoOrange
                                )
                                Text(
                                    text = "COD Floating Cash",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = tokens.ink,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = "₹${earnings.codCashInHandInr} / ₹$COD_FLOATING_LIMIT_INR",
                                style = MaterialTheme.typography.titleSmall,
                                fontFamily = JetBrainsMonoFontFamily,
                                color = if (earnings.isCodLimitExceeded) tokens.alertRed else tokens.ink,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        LinearProgressIndicator(
                            progress = {
                                (earnings.codCashInHandInr.toFloat() / COD_FLOATING_LIMIT_INR.toFloat())
                                    .coerceIn(0f, 1f)
                            },
                            color = if (earnings.isCodLimitExceeded) tokens.alertRed else tokens.promoOrange,
                            trackColor = tokens.surfaceElevated,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                        )

                        OutlinedButton(
                            onClick = onSettleCodCash,
                            enabled = earnings.codCashInHandInr > 0,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("settle_cod_button")
                        ) {
                            Icon(Icons.Rounded.CurrencyRupee, contentDescription = null)
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = if (earnings.codCashInHandInr > 0) {
                                    "Deposit ₹${earnings.codCashInHandInr} at ${state.currentHub.podName}"
                                } else {
                                    "All COD Cash Settled"
                                }
                            )
                        }
                    }
                }
            }

            // 4. Daily Incentive Tiers
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
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Rounded.EmojiEvents, contentDescription = null, tint = tokens.deliveryGreenDeep)
                            Text(
                                text = "Daily Milestone Incentives",
                                style = MaterialTheme.typography.titleMedium,
                                color = tokens.ink,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        tiers.forEach { tier ->
                            val unlocked = earnings.completedDrops >= tier.targetDrops
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (unlocked) tokens.deliveryGreenSoft else tokens.surfaceElevated,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (unlocked) Icons.Rounded.CheckCircle else Icons.Rounded.Bolt,
                                            contentDescription = null,
                                            tint = if (unlocked) tokens.deliveryGreenDeep else tokens.muted,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Column {
                                            Text(
                                                text = "${tier.label} (${tier.targetDrops} drops)",
                                                style = MaterialTheme.typography.titleSmall,
                                                color = tokens.ink,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = if (unlocked) "Unlocked!" else "${tier.targetDrops - earnings.completedDrops} drops away",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = if (unlocked) tokens.deliveryGreenDeep else tokens.muted
                                            )
                                        }
                                    }
                                    Text(
                                        text = "+₹${tier.rewardInr}",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = if (unlocked) tokens.deliveryGreenDeep else tokens.ink,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 5. Completed Trips History
            item {
                Text(
                    text = "Completed Drops",
                    style = MaterialTheme.typography.titleMedium,
                    color = tokens.ink,
                    fontWeight = FontWeight.Bold
                )
            }
            items(state.completedTrips, key = { it.orderId }) { trip ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = tokens.surface),
                    border = BorderStroke(1.dp, tokens.border),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${trip.orderId} • ${trip.customerName}",
                                style = MaterialTheme.typography.titleSmall,
                                color = tokens.ink,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${trip.distanceKm} km • ${trip.paymentMode.shortLabel} • " +
                                    timeFmt.format(Date(trip.deliveredAtMillis ?: trip.createdAtMillis)),
                                style = MaterialTheme.typography.bodySmall,
                                color = tokens.muted
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "+₹${trip.totalRiderPayoutInr}",
                                style = MaterialTheme.typography.titleMedium,
                                color = tokens.deliveryGreenDeep,
                                fontWeight = FontWeight.ExtraBold
                            )
                            if (trip.paymentMode == RiderPaymentMode.COD) {
                                Text(
                                    text = "COD ₹${trip.collectAmountInr}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = tokens.promoOrange
                                )
                            }
                        }
                    }
                }
            }

            // 6. Bank & COD Ledger Transactions
            if (state.transactions.isNotEmpty()) {
                item {
                    Text(
                        text = "Payout & COD Ledger",
                        style = MaterialTheme.typography.titleMedium,
                        color = tokens.ink,
                        fontWeight = FontWeight.Bold
                    )
                }
                items(state.transactions, key = { it.id }) { tx ->
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = tokens.surface,
                        border = BorderStroke(1.dp, tokens.border),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = tx.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = tokens.ink,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "${tx.type.label} • ${timeFmt.format(Date(tx.createdAtMillis))}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = tokens.muted
                                )
                            }
                            Text(
                                text = "₹${tx.amountInr}",
                                style = MaterialTheme.typography.titleSmall,
                                color = if (tx.type == PayoutTxType.COD_DEPOSIT) tokens.promoOrange else tokens.deliveryGreenDeep,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BreakdownRow(label: String, amount: String, bold: Boolean = false) {
    val tokens = RiderThemeTokens.colors
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = if (bold) MaterialTheme.typography.titleSmall else MaterialTheme.typography.bodyMedium,
            color = if (bold) tokens.ink else tokens.muted,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal
        )
        Text(
            text = amount,
            style = if (bold) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium,
            color = if (bold) tokens.deliveryGreenDeep else tokens.ink,
            fontWeight = if (bold) FontWeight.ExtraBold else FontWeight.SemiBold
        )
    }
}
