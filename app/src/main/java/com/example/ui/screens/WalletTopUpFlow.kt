package com.example.ui.screens

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.QrCode2
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.QuickKartThemeTokens
import kotlinx.coroutines.delay
import kotlin.random.Random

/*
 * Wallet top-up — DESIGN PROTOTYPE payment flow.
 *
 * Money is credited ONLY after a (simulated) successful payment:
 *   amount → payment method (UPI / Card / Netbanking) → card OTP (1234) →
 *   processing → success (wallet credited) or failure (nothing credited, retry).
 * No real gateway is called. Demo failure triggers: a UPI ID starting with "fail",
 * or a card number ending in 0000.
 * To go live, replace [simulatePaymentSucceeds] + the processing delay with a
 * payment-gateway SDK (e.g. Razorpay / PayU / Juspay) and credit the wallet from
 * the server-verified payment callback, never from the client.
 */

private const val MIN_TOP_UP_INR = 10
private const val MAX_TOP_UP_INR = 10_000

private enum class TopUpStep { AMOUNT, METHOD, CARD_OTP, PROCESSING, SUCCESS, FAILED }
private enum class TopUpMethod(val label: String, val subtitle: String, val icon: ImageVector) {
    UPI("UPI", "GPay, PhonePe, Paytm or any UPI ID", Icons.Rounded.QrCode2),
    CARD("Credit / Debit Card", "Visa, Mastercard, RuPay", Icons.Rounded.CreditCard),
    NETBANKING("Netbanking", "All major Indian banks", Icons.Rounded.AccountBalance)
}

private val UpiApps = listOf("GPay", "PhonePe", "Paytm", "BHIM")
private val Banks = listOf("SBI", "HDFC Bank", "ICICI Bank", "Axis Bank", "Kotak Bank")

/**
 * @param presetAmountInr amount chosen before opening (quick-add chip, checkout shortfall).
 *   When set, the flow starts on the payment-method step; the amount can still be changed.
 * @param onPaymentSuccess called only after payment succeeds, with the amount and a note
 *   describing the method used (stored on the wallet transaction).
 */
@Composable
fun WalletTopUpFlow(
    currentBalanceInr: Int,
    presetAmountInr: Int? = null,
    onDismiss: () -> Unit,
    onPaymentSuccess: (amountInr: Int, methodNote: String) -> Unit
) = PaymentFlowDialog(
    forOrder = false,
    currentBalanceInr = currentBalanceInr,
    presetAmountInr = presetAmountInr?.coerceIn(MIN_TOP_UP_INR, MAX_TOP_UP_INR),
    initialMethod = TopUpMethod.UPI,
    onDismiss = onDismiss,
    onPaymentSuccess = onPaymentSuccess
)

/**
 * Pay for a checkout order (UPI / Card / Netbanking). [onPaymentSuccess] is called only after
 * the (simulated) payment succeeds — the caller places the order then. On failure the user
 * can retry or cancel; the cart is left untouched.
 */
@Composable
fun OrderPaymentFlow(
    amountInr: Int,
    preferCard: Boolean,
    onDismiss: () -> Unit,
    onPaymentSuccess: (methodNote: String) -> Unit
) = PaymentFlowDialog(
    forOrder = true,
    currentBalanceInr = 0,
    presetAmountInr = amountInr,
    initialMethod = if (preferCard) TopUpMethod.CARD else TopUpMethod.UPI,
    onDismiss = onDismiss,
    onPaymentSuccess = { _, note -> onPaymentSuccess(note.replace("Added via", "Paid via")) }
)

@Composable
private fun PaymentFlowDialog(
    forOrder: Boolean,
    currentBalanceInr: Int,
    presetAmountInr: Int?,
    initialMethod: TopUpMethod,
    onDismiss: () -> Unit,
    onPaymentSuccess: (amountInr: Int, methodNote: String) -> Unit
) {
    val tokens = QuickKartThemeTokens.colors
    val startAmount = presetAmountInr
    var step by remember { mutableStateOf(if (startAmount != null) TopUpStep.METHOD else TopUpStep.AMOUNT) }
    var amountText by remember { mutableStateOf((startAmount ?: 500).toString()) }
    val amount = amountText.toIntOrNull() ?: 0

    val saved = LocalSavedPayments.current
    var method by remember { mutableStateOf(initialMethod) }
    // A saved UPI ID is pre-filled; otherwise default to the GPay app.
    var upiApp by remember { mutableStateOf<String?>(if (saved.upiId.isBlank()) "GPay" else null) }
    var upiId by remember { mutableStateOf(saved.upiId) }
    var cardNumber by remember { mutableStateOf("") }
    var cardExpiry by remember { mutableStateOf("") }
    var cardCvv by remember { mutableStateOf("") }
    // With a saved card only the CVV is needed.
    var useSavedCard by remember { mutableStateOf(saved.cardLast4.isNotBlank()) }
    var saveForLater by remember { mutableStateOf(true) }
    var bank by remember { mutableStateOf(Banks[1]) }
    var methodError by remember { mutableStateOf<String?>(null) }
    var txnRef by remember { mutableStateOf("") }

    val cardLast4 = if (useSavedCard) saved.cardLast4 else cardNumber.takeLast(4)
    val methodNote = when (method) {
        TopUpMethod.UPI -> if (upiId.isNotBlank()) "Added via UPI • $upiId" else "Added via UPI • ${upiApp ?: "UPI"}"
        TopUpMethod.CARD -> "Added via Card •••• $cardLast4"
        TopUpMethod.NETBANKING -> "Added via Netbanking • $bank"
    }
    val processingText = when (method) {
        TopUpMethod.UPI -> if (upiId.isNotBlank()) "Approve the request sent to $upiId" else "Approve the payment in ${upiApp ?: "your UPI app"}"
        TopUpMethod.CARD -> "Confirming payment with your bank"
        TopUpMethod.NETBANKING -> "Completing payment with $bank"
    }
    val busy = step == TopUpStep.PROCESSING

    Dialog(
        onDismissRequest = { if (!busy) onDismiss() },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = !busy,
            dismissOnClickOutside = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(tokens.background.copy(alpha = 0.95f))
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding(),
            contentAlignment = Alignment.Center
        ) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = tokens.surface),
                border = BorderStroke(1.dp, tokens.border),
                modifier = Modifier
                    .widthIn(max = 520.dp)
                    .fillMaxWidth()
                    .padding(16.dp)
                    .testTag("wallet_top_up_dialog")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    TopUpHeader(
                        title = when (step) {
                            TopUpStep.AMOUNT -> "Add Money to Wallet"
                            TopUpStep.METHOD -> if (forOrder) "Pay for your Order" else "Choose Payment Method"
                            TopUpStep.CARD_OTP -> "Verify Card Payment"
                            TopUpStep.PROCESSING -> "Processing Payment"
                            TopUpStep.SUCCESS -> "Payment Successful"
                            TopUpStep.FAILED -> "Payment Failed"
                        },
                        showBack = (step == TopUpStep.METHOD && !forOrder) || step == TopUpStep.CARD_OTP,
                        showClose = !busy && step != TopUpStep.SUCCESS,
                        onBack = {
                            methodError = null
                            step = if (step == TopUpStep.CARD_OTP) TopUpStep.METHOD else TopUpStep.AMOUNT
                        },
                        onClose = onDismiss
                    )

                    when (step) {
                        TopUpStep.AMOUNT -> AmountStep(
                            balanceInr = currentBalanceInr,
                            amountText = amountText,
                            onAmountChange = { amountText = it },
                            onContinue = { step = TopUpStep.METHOD }
                        )

                        TopUpStep.METHOD -> {
                            AmountSummaryRow(
                                label = if (forOrder) "Order total" else "Adding to Wallet",
                                amountInr = amount,
                                onChange = if (forOrder) null else { { step = TopUpStep.AMOUNT } }
                            )
                            TopUpMethod.entries.forEach { m ->
                                MethodOption(
                                    method = m,
                                    selected = method == m,
                                    onSelect = {
                                        method = m
                                        methodError = null
                                    }
                                ) {
                                    when (m) {
                                        TopUpMethod.UPI -> UpiDetails(
                                            selectedApp = upiApp,
                                            upiId = upiId,
                                            onSelectApp = {
                                                upiApp = it
                                                upiId = ""
                                                methodError = null
                                            },
                                            onUpiIdChange = {
                                                upiId = it.trim()
                                                upiApp = null
                                                methodError = null
                                            }
                                        )
                                        TopUpMethod.CARD -> if (useSavedCard) {
                                            SavedCardDetails(
                                                last4 = saved.cardLast4,
                                                cvv = cardCvv,
                                                onCvvChange = { cardCvv = it; methodError = null },
                                                onUseDifferentCard = {
                                                    useSavedCard = false
                                                    cardCvv = ""
                                                    methodError = null
                                                }
                                            )
                                        } else {
                                            CardDetails(
                                                number = cardNumber,
                                                expiry = cardExpiry,
                                                cvv = cardCvv,
                                                onNumberChange = { cardNumber = it; methodError = null },
                                                onExpiryChange = { cardExpiry = it; methodError = null },
                                                onCvvChange = { cardCvv = it; methodError = null }
                                            )
                                        }
                                        TopUpMethod.NETBANKING -> BankDetails(
                                            selectedBank = bank,
                                            onSelectBank = { bank = it }
                                        )
                                    }
                                }
                            }
                            if (methodError != null) {
                                Text(methodError!!, style = MaterialTheme.typography.bodySmall, color = tokens.alertRed)
                            }
                            val canSave = (method == TopUpMethod.UPI && upiId.isNotBlank() && upiId != saved.upiId) ||
                                (method == TopUpMethod.CARD && !useSavedCard)
                            if (canSave) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable { saveForLater = !saveForLater }
                                        .testTag("save_payment_method_toggle")
                                ) {
                                    Checkbox(
                                        checked = saveForLater,
                                        onCheckedChange = { saveForLater = it },
                                        colors = CheckboxDefaults.colors(checkedColor = tokens.brandYellow, checkmarkColor = tokens.onBrandYellow)
                                    )
                                    Text(
                                        if (method == TopUpMethod.CARD) "Save card (last 4 digits only) for faster checkout"
                                        else "Save this UPI ID for faster checkout",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = tokens.ink
                                    )
                                }
                            }
                            SecureNote(
                                if (forOrder) "Your order is placed only after payment succeeds"
                                else "Money is added only after payment succeeds"
                            )
                            YellowActionButton(
                                text = "Pay ₹$amount",
                                testTag = "wallet_top_up_pay_button",
                                icon = Icons.Rounded.Lock,
                                onClick = {
                                    methodError = if (method == TopUpMethod.CARD && useSavedCard) {
                                        if (cardCvv.length == 3) null else "Enter the 3-digit CVV."
                                    } else {
                                        validateMethod(method, upiApp, upiId, cardNumber, cardExpiry, cardCvv)
                                    }
                                    if (methodError == null) {
                                        step = if (method == TopUpMethod.CARD) TopUpStep.CARD_OTP else TopUpStep.PROCESSING
                                    }
                                }
                            )
                        }

                        TopUpStep.CARD_OTP -> CardOtpStep(
                            amountInr = amount,
                            cardLast4 = cardLast4,
                            onVerified = { step = TopUpStep.PROCESSING }
                        )

                        TopUpStep.PROCESSING -> ProcessingStep(
                            amountInr = amount,
                            message = processingText,
                            onFinished = {
                                txnRef = "QKP${System.currentTimeMillis().toString().takeLast(8)}${Random.nextInt(10, 99)}"
                                if (simulatePaymentSucceeds(method, upiId, cardLast4)) {
                                    if (saveForLater) {
                                        when {
                                            method == TopUpMethod.UPI && upiId.isNotBlank() -> saved.onSave(upiId, null)
                                            method == TopUpMethod.CARD && !useSavedCard -> saved.onSave(null, cardLast4)
                                        }
                                    }
                                    onPaymentSuccess(amount, methodNote)
                                    // For orders the caller closes this dialog and shows the order celebration.
                                    step = TopUpStep.SUCCESS
                                } else {
                                    step = TopUpStep.FAILED
                                }
                            }
                        )

                        TopUpStep.SUCCESS -> ResultStep(
                            forOrder = forOrder,
                            success = true,
                            amountInr = amount,
                            detail = methodNote.removePrefix("Added via "),
                            txnRef = txnRef,
                            newBalanceInr = currentBalanceInr,
                            primaryText = "Done",
                            onPrimary = onDismiss,
                            onSecondary = null
                        )

                        TopUpStep.FAILED -> ResultStep(
                            forOrder = forOrder,
                            success = false,
                            amountInr = amount,
                            detail = methodNote.removePrefix("Added via "),
                            txnRef = txnRef,
                            newBalanceInr = null,
                            primaryText = "Retry Payment",
                            onPrimary = { step = TopUpStep.METHOD },
                            onSecondary = onDismiss
                        )
                    }
                }
            }
        }
    }
}

private fun validateMethod(
    method: TopUpMethod,
    upiApp: String?,
    upiId: String,
    cardNumber: String,
    cardExpiry: String,
    cardCvv: String
): String? = when (method) {
    TopUpMethod.UPI -> when {
        upiApp == null && upiId.isBlank() -> "Choose a UPI app or enter your UPI ID."
        upiId.isNotBlank() && !Regex("^[A-Za-z0-9._-]{2,}@[A-Za-z]{2,}$").matches(upiId) ->
            "Enter a valid UPI ID, e.g. name@okhdfcbank."
        else -> null
    }
    TopUpMethod.CARD -> {
        val month = cardExpiry.take(2).toIntOrNull()
        when {
            cardNumber.length != 16 -> "Enter the 16-digit card number."
            cardExpiry.length != 4 || month == null || month !in 1..12 -> "Enter expiry as MM/YY."
            cardCvv.length != 3 -> "Enter the 3-digit CVV."
            else -> null
        }
    }
    TopUpMethod.NETBANKING -> null
}

/** Prototype-only outcome. Real integrations must rely on the gateway's verified result. */
private fun simulatePaymentSucceeds(method: TopUpMethod, upiId: String, cardLast4: String): Boolean =
    when (method) {
        TopUpMethod.UPI -> !upiId.lowercase().startsWith("fail")
        TopUpMethod.CARD -> cardLast4 != "0000"
        TopUpMethod.NETBANKING -> true
    }

/**
 * Saved payment methods available to the payment screens (provided by the app shell).
 * Only a UPI ID and a card's last 4 digits are ever stored — never full card data.
 */
data class SavedPayments(
    val upiId: String = "",
    val cardLast4: String = "",
    val onSave: (upiId: String?, cardLast4: String?) -> Unit = { _, _ -> }
)

val LocalSavedPayments = compositionLocalOf { SavedPayments() }

@Composable
private fun SavedCardDetails(
    last4: String,
    cvv: String,
    onCvvChange: (String) -> Unit,
    onUseDifferentCard: () -> Unit
) {
    val tokens = QuickKartThemeTokens.colors
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = tokens.brandYellowSoft,
        border = BorderStroke(1.dp, tokens.brandYellow),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("saved_card_option")
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(Icons.Rounded.CreditCard, contentDescription = null, tint = tokens.ink)
            Column(modifier = Modifier.weight(1f)) {
                Text("Saved card •••• $last4", style = MaterialTheme.typography.titleSmall, color = tokens.ink)
                Text("Enter CVV to pay", style = MaterialTheme.typography.bodySmall, color = tokens.muted)
            }
            Icon(Icons.Rounded.CheckCircle, contentDescription = "Selected", tint = tokens.ink)
        }
    }
    OutlinedTextField(
        value = cvv,
        onValueChange = { onCvvChange(it.filter { ch -> ch.isDigit() }.take(3)) },
        label = { Text("CVV") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("saved_card_cvv")
    )
    androidx.compose.material3.TextButton(
        onClick = onUseDifferentCard,
        colors = ButtonDefaults.textButtonColors(contentColor = tokens.ink),
        modifier = Modifier.testTag("use_different_card_button")
    ) { Text("Use a different card", fontWeight = FontWeight.Bold) }
}

@Composable
private fun TopUpHeader(
    title: String,
    showBack: Boolean,
    showClose: Boolean,
    onBack: () -> Unit,
    onClose: () -> Unit
) {
    val tokens = QuickKartThemeTokens.colors
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (showBack) {
            IconButton(onClick = onBack, modifier = Modifier.testTag("wallet_top_up_back")) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = tokens.ink)
            }
        } else {
            Surface(color = tokens.brandYellow, shape = CircleShape, modifier = Modifier.size(40.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Rounded.AccountBalanceWallet,
                        contentDescription = null,
                        tint = tokens.onBrandYellow,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = tokens.ink,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        if (showClose) {
            IconButton(onClick = onClose, modifier = Modifier.testTag("wallet_top_up_close")) {
                Icon(Icons.Rounded.Close, contentDescription = "Close", tint = tokens.muted)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AmountStep(
    balanceInr: Int,
    amountText: String,
    onAmountChange: (String) -> Unit,
    onContinue: () -> Unit
) {
    val tokens = QuickKartThemeTokens.colors
    val amount = amountText.toIntOrNull() ?: 0
    val error = when {
        amountText.isBlank() -> null
        amount < MIN_TOP_UP_INR -> "Minimum top-up is ₹$MIN_TOP_UP_INR."
        amount > MAX_TOP_UP_INR -> "Maximum top-up is ₹$MAX_TOP_UP_INR per transaction."
        else -> null
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF141414), // brand black card
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "CURRENT BALANCE",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.85f),
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "₹$balanceInr",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }

    OutlinedTextField(
        value = amountText,
        onValueChange = { onAmountChange(it.filter { ch -> ch.isDigit() }.take(5)) },
        label = { Text("Amount (₹)") },
        prefix = { Text("₹ ", fontWeight = FontWeight.Bold) },
        isError = error != null,
        supportingText = {
            Text(
                error ?: "Min ₹$MIN_TOP_UP_INR • Max ₹$MAX_TOP_UP_INR per top-up",
                color = if (error != null) tokens.alertRed else tokens.muted
            )
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = tokens.ink,
            unfocusedBorderColor = tokens.border
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("wallet_top_up_amount_input")
    )

    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        listOf(100, 200, 500, 1000, 2000).forEach { preset ->
            val selected = amount == preset
            Surface(
                shape = RoundedCornerShape(50),
                color = if (selected) tokens.ink else tokens.surfaceElevated,
                border = BorderStroke(1.dp, if (selected) tokens.ink else tokens.border),
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .clickable { onAmountChange(preset.toString()) }
                    .testTag("wallet_top_up_preset_$preset")
            ) {
                Text(
                    text = "₹$preset",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (selected) tokens.background else tokens.ink,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                )
            }
        }
    }

    YellowActionButton(
        text = "Continue to Pay",
        testTag = "wallet_top_up_continue",
        enabled = amount in MIN_TOP_UP_INR..MAX_TOP_UP_INR,
        onClick = onContinue
    )
}

@Composable
private fun AmountSummaryRow(label: String, amountInr: Int, onChange: (() -> Unit)?) {
    val tokens = QuickKartThemeTokens.colors
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = tokens.brandYellowSoft,
        border = BorderStroke(1.dp, tokens.brandYellow),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(label, style = MaterialTheme.typography.labelSmall, color = tokens.muted)
                Text(
                    "₹$amountInr",
                    style = MaterialTheme.typography.titleLarge,
                    color = tokens.ink,
                    fontWeight = FontWeight.ExtraBold
                )
            }
            if (onChange != null) {
                androidx.compose.material3.TextButton(
                    onClick = onChange,
                    modifier = Modifier.testTag("wallet_top_up_change_amount")
                ) {
                    Text("Change", color = tokens.ink, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun MethodOption(
    method: TopUpMethod,
    selected: Boolean,
    onSelect: () -> Unit,
    details: @Composable () -> Unit
) {
    val tokens = QuickKartThemeTokens.colors
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (selected) tokens.surfaceElevated else tokens.surface,
        border = BorderStroke(if (selected) 1.5.dp else 1.dp, if (selected) tokens.ink else tokens.border),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("wallet_top_up_method_${method.name.lowercase()}")
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onSelect)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(color = tokens.brandYellowSoft, shape = CircleShape, modifier = Modifier.size(38.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(method.icon, contentDescription = null, tint = tokens.ink, modifier = Modifier.size(20.dp))
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(method.label, style = MaterialTheme.typography.titleSmall, color = tokens.ink)
                    Text(method.subtitle, style = MaterialTheme.typography.bodySmall, color = tokens.muted)
                }
                RadioButton(
                    selected = selected,
                    onClick = onSelect,
                    colors = RadioButtonDefaults.colors(selectedColor = tokens.ink)
                )
            }
            if (selected) {
                HorizontalDivider(color = tokens.border)
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) { details() }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun UpiDetails(
    selectedApp: String?,
    upiId: String,
    onSelectApp: (String) -> Unit,
    onUpiIdChange: (String) -> Unit
) {
    val tokens = QuickKartThemeTokens.colors
    Text("Pay with UPI app", style = MaterialTheme.typography.labelMedium, color = tokens.muted)
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        UpiApps.forEach { app ->
            SelectChip(
                text = app,
                selected = selectedApp == app,
                testTag = "wallet_top_up_upi_${app.lowercase()}",
                onClick = { onSelectApp(app) }
            )
        }
    }
    Text("or enter UPI ID", style = MaterialTheme.typography.labelMedium, color = tokens.muted)
    OutlinedTextField(
        value = upiId,
        onValueChange = onUpiIdChange,
        placeholder = { Text("name@okhdfcbank") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("wallet_top_up_upi_id_input")
    )
}

@Composable
private fun CardDetails(
    number: String,
    expiry: String,
    cvv: String,
    onNumberChange: (String) -> Unit,
    onExpiryChange: (String) -> Unit,
    onCvvChange: (String) -> Unit
) {
    OutlinedTextField(
        value = number.chunked(4).joinToString(" "),
        onValueChange = { onNumberChange(it.filter { ch -> ch.isDigit() }.take(16)) },
        label = { Text("Card number") },
        placeholder = { Text("1234 5678 9012 3456") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("wallet_top_up_card_number")
    )
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(
            value = if (expiry.length > 2) "${expiry.take(2)}/${expiry.drop(2)}" else expiry,
            onValueChange = { onExpiryChange(it.filter { ch -> ch.isDigit() }.take(4)) },
            label = { Text("Expiry (MM/YY)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .weight(1f)
                .testTag("wallet_top_up_card_expiry")
        )
        OutlinedTextField(
            value = cvv,
            onValueChange = { onCvvChange(it.filter { ch -> ch.isDigit() }.take(3)) },
            label = { Text("CVV") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .weight(1f)
                .testTag("wallet_top_up_card_cvv")
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun BankDetails(selectedBank: String, onSelectBank: (String) -> Unit) {
    val tokens = QuickKartThemeTokens.colors
    Text("Select your bank", style = MaterialTheme.typography.labelMedium, color = tokens.muted)
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Banks.forEach { b ->
            SelectChip(
                text = b,
                selected = selectedBank == b,
                testTag = "wallet_top_up_bank_${b.substringBefore(' ').lowercase()}",
                onClick = { onSelectBank(b) }
            )
        }
    }
}

@Composable
private fun SelectChip(text: String, selected: Boolean, testTag: String, onClick: () -> Unit) {
    val tokens = QuickKartThemeTokens.colors
    Surface(
        shape = RoundedCornerShape(50),
        color = if (selected) tokens.brandYellow else tokens.surface,
        border = BorderStroke(1.dp, if (selected) tokens.brandYellow else tokens.border),
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .clickable(onClick = onClick)
            .testTag(testTag)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) tokens.onBrandYellow else tokens.ink,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
        )
    }
}

@Composable
private fun CardOtpStep(amountInr: Int, cardLast4: String, onVerified: () -> Unit) {
    val tokens = QuickKartThemeTokens.colors
    var otp by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    Text(
        text = "Enter the OTP sent by your bank to confirm ₹$amountInr on card •••• $cardLast4.",
        style = MaterialTheme.typography.bodyMedium,
        color = tokens.ink
    )
    OutlinedTextField(
        value = otp,
        onValueChange = {
            otp = it.filter { ch -> ch.isDigit() }.take(4)
            error = null
        },
        label = { Text("4-digit OTP (Use 1234)") },
        isError = error != null,
        supportingText = error?.let { { Text(it, color = tokens.alertRed) } },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("wallet_top_up_card_otp")
    )
    YellowActionButton(
        text = "Verify & Pay ₹$amountInr",
        testTag = "wallet_top_up_otp_verify",
        icon = Icons.Rounded.CheckCircle,
        onClick = {
            if (otp == "1234") onVerified() else error = "Incorrect OTP. Use 1234 for this prototype."
        }
    )
}

@Composable
private fun ProcessingStep(amountInr: Int, message: String, onFinished: () -> Unit) {
    val tokens = QuickKartThemeTokens.colors
    LaunchedEffect(Unit) {
        delay(2000) // Simulated gateway round-trip
        onFinished()
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
            .testTag("wallet_top_up_processing"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        CircularProgressIndicator(color = tokens.brandYellow, strokeWidth = 4.dp, modifier = Modifier.size(64.dp))
        Text(
            text = "₹$amountInr",
            style = MaterialTheme.typography.headlineSmall,
            color = tokens.ink,
            fontWeight = FontWeight.ExtraBold
        )
        Text(message, style = MaterialTheme.typography.bodyMedium, color = tokens.ink, textAlign = TextAlign.Center)
        Text(
            "Please don't press back or close the app.",
            style = MaterialTheme.typography.bodySmall,
            color = tokens.muted,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ResultStep(
    forOrder: Boolean,
    success: Boolean,
    amountInr: Int,
    detail: String,
    txnRef: String,
    newBalanceInr: Int?,
    primaryText: String,
    onPrimary: () -> Unit,
    onSecondary: (() -> Unit)?
) {
    val tokens = QuickKartThemeTokens.colors
    val accent = if (success) tokens.deliveryGreenDeep else tokens.alertRed
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(if (success) "wallet_top_up_success" else "wallet_top_up_failed"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Surface(
            color = if (success) tokens.deliveryGreenSoft else tokens.alertRed.copy(alpha = 0.1f),
            shape = CircleShape,
            modifier = Modifier.size(72.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = if (success) Icons.Rounded.CheckCircle else Icons.Rounded.ErrorOutline,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(40.dp)
                )
            }
        }
        Text(
            text = when {
                forOrder && success -> "₹$amountInr paid"
                forOrder -> "Payment of ₹$amountInr failed"
                success -> "₹$amountInr added to Wallet"
                else -> "₹$amountInr was not added"
            },
            style = MaterialTheme.typography.titleLarge,
            color = tokens.ink,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center
        )
        Text(
            text = when {
                success -> "Paid via $detail"
                forOrder -> "The payment via $detail was declined, so your order was not placed. " +
                    "No money was deducted and your cart is saved."
                else -> "The payment via $detail was declined. No money was deducted from your account."
            },
            style = MaterialTheme.typography.bodySmall,
            color = tokens.muted,
            textAlign = TextAlign.Center
        )
    }

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = tokens.surfaceElevated,
        border = BorderStroke(1.dp, tokens.border),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            ReceiptRow("Transaction ID", txnRef)
            ReceiptRow("Status", if (success) "Success" else "Failed", accent)
            if (newBalanceInr != null) ReceiptRow("Wallet balance", "₹$newBalanceInr")
        }
    }

    YellowActionButton(text = primaryText, testTag = "wallet_top_up_primary_result", onClick = onPrimary)
    if (onSecondary != null) {
        OutlinedButton(
            onClick = onSecondary,
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, tokens.border),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("wallet_top_up_cancel_result")
        ) {
            Text("Cancel", style = MaterialTheme.typography.labelLarge, color = tokens.ink)
        }
    }
}

@Composable
private fun ReceiptRow(label: String, value: String, valueColor: Color = QuickKartThemeTokens.colors.ink) {
    val tokens = QuickKartThemeTokens.colors
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = tokens.muted, modifier = Modifier.weight(1f))
        Text(
            value,
            style = MaterialTheme.typography.bodySmall,
            color = valueColor,
            fontFamily = JetBrainsMonoFontFamily,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun SecureNote(message: String) {
    val tokens = QuickKartThemeTokens.colors
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Icon(Icons.Rounded.Lock, contentDescription = null, tint = tokens.deliveryGreenDeep, modifier = Modifier.size(14.dp))
        Text(
            "100% secure payment • $message",
            style = MaterialTheme.typography.bodySmall,
            color = tokens.muted
        )
    }
}

@Composable
private fun YellowActionButton(
    text: String,
    testTag: String,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    onClick: () -> Unit
) {
    val tokens = QuickKartThemeTokens.colors
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = tokens.brandYellow,
            contentColor = tokens.onBrandYellow
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .testTag(testTag)
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}
