package com.example.merchant.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.Badge
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.CloudUpload
import androidx.compose.material.icons.rounded.HourglassTop
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material.icons.rounded.Storefront
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.merchant.data.DocStatus
import com.example.merchant.data.MerchantProfile
import com.example.merchant.data.StoreType
import com.example.ui.components.BadgeTone
import com.example.ui.components.BrandBlack
import com.example.ui.components.BrandSwitch
import com.example.ui.components.ChoiceChip
import com.example.ui.components.PrimaryButton
import com.example.ui.components.QkCard
import com.example.ui.components.QkTextField
import com.example.ui.components.ScreenHeader
import com.example.ui.components.SecondaryButton
import com.example.ui.components.StatusBadge
import com.example.ui.components.OtpBoxes
import com.example.ui.components.QuickKartLogo
import com.example.ui.theme.QuickKartThemeTokens
import com.example.ui.theme.zoomScaledControl
import kotlinx.coroutines.delay

// ─────────────────────────────────────────────────────────────────────────────────────────────
// 0. Login (phone + OTP) — same flow as the customer app; demo OTP 1234.
// ─────────────────────────────────────────────────────────────────────────────────────────────

@Composable
fun MerchantLoginScreen(onVerify: (phone: String, otp: String) -> Boolean) {
    val tokens = QuickKartThemeTokens.colors
    var phone by rememberSaveable { mutableStateOf("") }
    var otpSent by rememberSaveable { mutableStateOf(false) }
    var otp by rememberSaveable { mutableStateOf("") }
    var agreed by rememberSaveable { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var resendIn by remember { mutableIntStateOf(0) }
    var wrongAttempts by remember { mutableIntStateOf(0) }
    var lockedFor by remember { mutableIntStateOf(0) }

    LaunchedEffect(resendIn) { if (resendIn > 0) { delay(1_000); resendIn-- } }
    LaunchedEffect(lockedFor) { if (lockedFor > 0) { delay(1_000); lockedFor-- } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(tokens.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            QuickKartLogo(size = 44.dp, cornerRadius = 12.dp)
            Column(Modifier.weight(1f)) {
                Text("QuickKart", style = MaterialTheme.typography.titleLarge, color = tokens.ink)
                Text("Merchant Partner", style = MaterialTheme.typography.bodySmall, color = tokens.muted)
            }
            StatusBadge("PARTNER", BadgeTone.DARK)
        }

        // Hero: black card with yellow accent (brand template)
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = BrandBlack),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Surface(color = tokens.brandYellow, shape = RoundedCornerShape(50)) {
                    Row(Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Storefront, null, tint = tokens.onBrandYellow, modifier = Modifier.size(15.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("FOR KIRANA, GROCERY & RESTAURANTS", style = MaterialTheme.typography.labelMedium, color = tokens.onBrandYellow, fontWeight = FontWeight.ExtraBold)
                    }
                }
                Text("Sell to your neighbourhood\nin minutes, not days.", style = MaterialTheme.typography.headlineSmall, color = Color.White)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    HeroStat("Orders in", "10 min", Modifier.weight(1f))
                    HeroStat("Payouts", "Weekly", Modifier.weight(1f))
                    HeroStat("Commission", "12%", Modifier.weight(1f))
                }
            }
        }

        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = tokens.surface),
            border = BorderStroke(1.dp, tokens.border),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(if (otpSent) "Verify OTP" else "Sign in to your store", style = MaterialTheme.typography.titleLarge, color = tokens.ink)
                QkTextField(
                    value = phone,
                    onValueChange = { phone = it.filter(Char::isDigit).take(10); error = null },
                    label = "10-digit Mobile Number",
                    prefix = "+91 ",
                    leadingIcon = Icons.Rounded.PhoneAndroid,
                    keyboardType = KeyboardType.Phone,
                    enabled = !otpSent,
                    testTag = "merchant_phone_input"
                )
                AnimatedVisibility(visible = otpSent) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Enter the 4-digit code sent to +91 $phone (demo: 1234)", style = MaterialTheme.typography.bodySmall, color = tokens.muted)
                        OtpBoxes(value = otp, onValueChange = { otp = it; error = null }, enabled = lockedFor == 0)
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            TextButton(onClick = { otpSent = false; otp = ""; error = null }) { Text("Change number", color = tokens.ink) }
                            TextButton(enabled = resendIn == 0, onClick = { resendIn = 30; otp = ""; error = null }) {
                                Text(if (resendIn > 0) "Resend in 0:${resendIn.toString().padStart(2, '0')}" else "Resend OTP", color = if (resendIn > 0) tokens.muted else tokens.ink)
                            }
                        }
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = agreed,
                        onCheckedChange = { agreed = it },
                        colors = CheckboxDefaults.colors(checkedColor = tokens.brandYellow, checkmarkColor = tokens.onBrandYellow),
                        modifier = Modifier.zoomScaledControl()
                    )
                    Text("I agree to the Merchant Terms and Privacy Policy", style = MaterialTheme.typography.bodySmall, color = tokens.muted)
                }
                if (lockedFor > 0) {
                    Text("Too many wrong attempts. Try again in $lockedFor s.", style = MaterialTheme.typography.bodySmall, color = tokens.alertRed)
                } else if (error != null) {
                    Text(error!!, style = MaterialTheme.typography.bodySmall, color = tokens.alertRed)
                }
                PrimaryButton(
                    text = if (otpSent) "Verify & Continue" else "Send OTP",
                    icon = if (otpSent) Icons.Rounded.CheckCircle else null,
                    enabled = agreed && lockedFor == 0,
                    testTag = "merchant_login_button",
                    onClick = {
                        if (!otpSent) {
                            if (phone.length != 10 || phone.first() !in '6'..'9') {
                                error = "Enter a valid 10-digit Indian mobile number."
                            } else { otpSent = true; resendIn = 30 }
                        } else if (otp.length != 4) {
                            error = "Enter the 4-digit OTP."
                        } else if (!onVerify(phone, otp)) {
                            wrongAttempts++
                            otp = ""
                            if (wrongAttempts >= 3) { lockedFor = 30; wrongAttempts = 0 } else error = "Incorrect OTP. ${3 - wrongAttempts} attempt(s) left."
                        }
                    }
                )
                if (!agreed) Text("Accept the terms to continue.", style = MaterialTheme.typography.labelSmall, color = tokens.muted)
            }
        }
        Text(
            "New store? Sign in with your number and we'll guide you through store verification (FSSAI, GSTIN & bank).",
            style = MaterialTheme.typography.bodySmall,
            color = tokens.muted,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun HeroStat(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier.background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp)).padding(10.dp)) {
        Text(value, style = MaterialTheme.typography.titleMedium, color = QuickKartThemeTokens.colors.brandYellow, fontWeight = FontWeight.ExtraBold)
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.75f))
    }
}

// ─────────────────────────────────────────────────────────────────────────────────────────────
// 1. Store KYC & Onboarding — 3 steps with validation, simulated document verification.
// ─────────────────────────────────────────────────────────────────────────────────────────────

private val FSSAI_REGEX = Regex("^[0-9]{14}$")
private val GSTIN_REGEX = Regex("^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z][1-9A-Z]Z[0-9A-Z]$")
private val IFSC_REGEX = Regex("^[A-Z]{4}0[A-Z0-9]{6}$")

@Composable
fun StoreKycScreen(
    profile: MerchantProfile,
    storeOpen: Boolean,
    onUpdate: ((MerchantProfile) -> MerchantProfile) -> Unit,
    onUpload: (isFssai: Boolean) -> Unit,
    onSetStoreOpen: (Boolean) -> Unit,
    onSubmit: () -> Unit,
    onBack: (() -> Unit)?,
    onSignOut: () -> Unit
) {
    val tokens = QuickKartThemeTokens.colors
    var step by rememberSaveable { mutableIntStateOf(0) }
    var showErrors by remember { mutableStateOf(false) }
    var confirmAccount by rememberSaveable { mutableStateOf(profile.accountNumber) }

    val nameErr = if (profile.storeName.trim().length < 3) "Enter your store name" else null
    val ownerErr = if (profile.ownerName.trim().length < 2) "Enter the owner's name" else null
    val addrErr = if (profile.address.trim().length < 10) "Enter the full store address" else null
    val fssaiErr = if (!FSSAI_REGEX.matches(profile.fssaiNumber)) "FSSAI licence is 14 digits" else null
    val gstErr = if (!GSTIN_REGEX.matches(profile.gstin)) "GSTIN is 15 characters, e.g. 36ABCDE1234F1Z5" else null
    val acctErr = if (profile.accountNumber.length !in 9..18) "Account number is 9–18 digits" else null
    val confirmErr = if (confirmAccount != profile.accountNumber) "Account numbers don't match" else null
    val ifscErr = if (!IFSC_REGEX.matches(profile.ifsc)) "IFSC is 11 characters, e.g. HDFC0001234" else null
    val bankErr = if (profile.bankName.trim().length < 3) "Enter the bank name" else null

    val docsUploaded = profile.fssaiStatus != DocStatus.NOT_UPLOADED && profile.gstinStatus != DocStatus.NOT_UPLOADED
    val stepValid = when (step) {
        0 -> listOf(nameErr, ownerErr, addrErr).all { it == null }
        1 -> fssaiErr == null && gstErr == null && docsUploaded
        else -> listOf(acctErr, confirmErr, ifscErr, bankErr).all { it == null }
    }

    Column(
        modifier = Modifier.fillMaxSize().background(tokens.background).statusBarsPadding().navigationBarsPadding().imePadding()
    ) {
        ScreenHeader(
            title = "Store Onboarding & KYC",
            subtitle = "Step ${step + 1} of 3 · ${listOf("Store details", "Licences", "Bank account")[step]}",
            onBack = when {
                step > 0 -> ({ step--; showErrors = false })
                else -> onBack
            },
            actions = { if (onBack == null) TextButton(onClick = onSignOut) { Text("Sign out", color = tokens.muted) } }
        )
        // Progress segments
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            repeat(3) { i ->
                Box(Modifier.weight(1f).height(5.dp).background(if (i <= step) tokens.brandYellow else tokens.border, RoundedCornerShape(50)))
            }
        }
        Column(
            modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(16.dp).widthIn(max = 640.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            when (step) {
                0 -> {
                    KycIntro(Icons.Rounded.Storefront, "Tell us about your store", "Customers see the store name and address on their order.")
                    QkTextField(profile.storeName, { v -> onUpdate { it.copy(storeName = v.take(40)) } }, "Store name", error = nameErr.takeIf { showErrors }, testTag = "kyc_store_name")
                    QkTextField(profile.ownerName, { v -> onUpdate { it.copy(ownerName = v.take(40)) } }, "Owner name", error = ownerErr.takeIf { showErrors })
                    QkTextField("+91 ${profile.phone}", {}, "Owner contact (verified)", enabled = false)
                    Text("Store type", style = MaterialTheme.typography.titleSmall, color = tokens.ink)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        StoreType.entries.forEach { t -> ChoiceChip(t.label, profile.storeType == t, { onUpdate { it.copy(storeType = t) } }) }
                    }
                    QkTextField(profile.address, { v -> onUpdate { it.copy(address = v.take(160)) } }, "Store address", singleLine = false, minLines = 2, error = addrErr.takeIf { showErrors }, supporting = "Shop no., building, street, area, Hyderabad")
                }
                1 -> {
                    KycIntro(Icons.Rounded.Badge, "Licences", "Food businesses need a valid FSSAI licence. GSTIN is required for tax invoices.")
                    QkTextField(
                        profile.fssaiNumber,
                        { v -> onUpdate { it.copy(fssaiNumber = v.filter(Char::isDigit).take(14), fssaiStatus = if (v != it.fssaiNumber) DocStatus.NOT_UPLOADED else it.fssaiStatus) } },
                        "FSSAI licence number", keyboardType = KeyboardType.Number, error = fssaiErr.takeIf { showErrors || profile.fssaiNumber.length == 14 }
                    )
                    DocUploadRow("FSSAI licence certificate", profile.fssaiStatus, enabled = fssaiErr == null) { onUpload(true) }
                    QkTextField(
                        profile.gstin,
                        { v -> onUpdate { it.copy(gstin = v.uppercase().filter(Char::isLetterOrDigit).take(15), gstinStatus = if (v.uppercase() != it.gstin) DocStatus.NOT_UPLOADED else it.gstinStatus) } },
                        "GSTIN", error = gstErr.takeIf { showErrors || profile.gstin.length == 15 }
                    )
                    DocUploadRow("GST registration certificate", profile.gstinStatus, enabled = gstErr == null) { onUpload(false) }
                    if (showErrors && !docsUploaded) Text("Upload both certificates to continue.", style = MaterialTheme.typography.bodySmall, color = tokens.alertRed)
                }
                else -> {
                    KycIntro(Icons.Rounded.AccountBalance, "Bank account for payouts", "Weekly settlements are sent to this account.")
                    QkTextField(profile.bankName, { v -> onUpdate { it.copy(bankName = v.take(40)) } }, "Bank name", error = bankErr.takeIf { showErrors })
                    QkTextField(profile.accountNumber, { v -> onUpdate { it.copy(accountNumber = v.filter(Char::isDigit).take(18)) } }, "Account number", keyboardType = KeyboardType.NumberPassword, error = acctErr.takeIf { showErrors })
                    QkTextField(confirmAccount, { confirmAccount = it.filter(Char::isDigit).take(18) }, "Confirm account number", keyboardType = KeyboardType.Number, error = confirmErr.takeIf { showErrors || confirmAccount.length >= 9 })
                    QkTextField(profile.ifsc, { v -> onUpdate { it.copy(ifsc = v.uppercase().filter(Char::isLetterOrDigit).take(11)) } }, "IFSC code", error = ifscErr.takeIf { showErrors || profile.ifsc.length == 11 })
                    QkCard {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text("Store status", style = MaterialTheme.typography.titleSmall, color = tokens.ink)
                                Text(if (storeOpen) "Open — you'll receive orders once verified" else "Closed — no new orders", style = MaterialTheme.typography.bodySmall, color = tokens.muted)
                            }
                            BrandSwitch(checked = storeOpen, onCheckedChange = onSetStoreOpen)
                        }
                    }
                }
            }
            TextButton(onClick = {
                onUpdate {
                    it.copy(
                        storeName = it.storeName.ifBlank { "Sri Lakshmi Kirana" }, ownerName = it.ownerName.ifBlank { "Ramesh K." },
                        address = it.address.ifBlank { "Shop 12, Ayyappa Society Main Rd, Madhapur, Hyderabad 500081" },
                        fssaiNumber = it.fssaiNumber.ifBlank { "13622011000123" }, gstin = it.gstin.ifBlank { "36ABCDE1234F1Z5" },
                        bankName = it.bankName.ifBlank { "State Bank" }, accountNumber = it.accountNumber.ifBlank { "30012345678" }, ifsc = it.ifsc.ifBlank { "SBIN0001234" }
                    )
                }
                if (confirmAccount.isBlank()) confirmAccount = "30012345678"
            }) { Text("Fill sample details (demo)", color = tokens.muted) }
        }
        Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            if (step > 0) SecondaryButton("Back", onClick = { step--; showErrors = false })
            PrimaryButton(
                text = if (step < 2) "Continue" else "Submit for verification",
                modifier = Modifier.weight(1f),
                testTag = "kyc_continue",
                onClick = {
                    if (!stepValid) showErrors = true
                    else if (step < 2) { step++; showErrors = false }
                    else onSubmit()
                }
            )
        }
    }
}

@Composable
private fun KycIntro(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, body: String) {
    val tokens = QuickKartThemeTokens.colors
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(Modifier.size(48.dp).background(tokens.brandYellowSoft, CircleShape), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = tokens.ink)
        }
        Column {
            Text(title, style = MaterialTheme.typography.titleMedium, color = tokens.ink)
            Text(body, style = MaterialTheme.typography.bodySmall, color = tokens.muted)
        }
    }
}

@Composable
private fun DocUploadRow(title: String, status: DocStatus, enabled: Boolean, onUpload: () -> Unit) {
    val tokens = QuickKartThemeTokens.colors
    QkCard(contentPadding = androidx.compose.foundation.layout.PaddingValues(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Icon(
                when (status) { DocStatus.VERIFIED -> Icons.Rounded.CheckCircle; DocStatus.PENDING -> Icons.Rounded.HourglassTop; else -> Icons.Rounded.CloudUpload },
                null,
                tint = if (status == DocStatus.VERIFIED) tokens.deliveryGreenDeep else tokens.ink
            )
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall, color = tokens.ink)
                StatusBadge(
                    status.label,
                    when (status) { DocStatus.VERIFIED -> BadgeTone.SUCCESS; DocStatus.PENDING -> BadgeTone.WARNING; DocStatus.REJECTED -> BadgeTone.DANGER; else -> BadgeTone.NEUTRAL }
                )
            }
            if (status == DocStatus.NOT_UPLOADED || status == DocStatus.REJECTED) {
                Surface(
                    color = if (enabled) tokens.brandYellow else tokens.surfaceElevated,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.clickable(enabled = enabled, onClick = onUpload)
                ) {
                    Text("Upload", style = MaterialTheme.typography.labelLarge, color = if (enabled) tokens.onBrandYellow else tokens.muted, modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp))
                }
            }
        }
        if (!enabled && status == DocStatus.NOT_UPLOADED) {
            Text("Enter a valid number above to upload.", style = MaterialTheme.typography.labelSmall, color = tokens.muted)
        }
    }
}

