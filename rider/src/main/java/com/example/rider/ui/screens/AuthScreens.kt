package com.example.rider.ui.screens

import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.Badge
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DeliveryDining
import androidx.compose.material.icons.rounded.Face
import androidx.compose.material.icons.rounded.HourglassTop
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material.icons.rounded.TwoWheeler
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.rider.data.DocStatus
import com.example.rider.data.KycDoc
import com.example.rider.data.RiderProfile
import com.example.rider.data.VehicleType
import com.example.ui.components.BadgeTone
import com.example.ui.components.BrandBlack
import com.example.ui.components.ChoiceChip
import com.example.ui.components.OtpBoxes
import com.example.ui.components.PrimaryButton
import com.example.ui.components.QkCard
import com.example.ui.components.QkTextField
import com.example.ui.components.QuickKartLogo
import com.example.ui.components.ScreenHeader
import com.example.ui.components.StatusBadge
import com.example.ui.theme.QuickKartThemeTokens
import com.example.ui.theme.zoomScaledControl
import kotlinx.coroutines.delay

// ─────────────────────────────────────────────────────────────────────────────────────────────
// 1a. Rider login (phone + OTP, demo OTP 1234)
// ─────────────────────────────────────────────────────────────────────────────────────────────

@Composable
fun RiderLoginScreen(onVerify: (String, String) -> Boolean) {
    val tokens = QuickKartThemeTokens.colors
    var phone by rememberSaveable { mutableStateOf("") }
    var otpSent by rememberSaveable { mutableStateOf(false) }
    var otp by rememberSaveable { mutableStateOf("") }
    var agreed by rememberSaveable { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var resendIn by remember { mutableIntStateOf(0) }
    var wrong by remember { mutableIntStateOf(0) }
    var locked by remember { mutableIntStateOf(0) }
    LaunchedEffect(resendIn) { if (resendIn > 0) { delay(1000); resendIn-- } }
    LaunchedEffect(locked) { if (locked > 0) { delay(1000); locked-- } }

    Column(
        Modifier.fillMaxSize().background(tokens.background).statusBarsPadding().navigationBarsPadding().imePadding()
            .verticalScroll(rememberScrollState()).padding(horizontal = 18.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            QuickKartLogo(size = 44.dp, cornerRadius = 12.dp)
            Column(Modifier.weight(1f)) {
                Text("QuickKart", style = MaterialTheme.typography.titleLarge, color = tokens.ink)
                Text("Delivery Partner", style = MaterialTheme.typography.bodySmall, color = tokens.muted)
            }
            StatusBadge("RIDER", BadgeTone.DARK)
        }
        Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = BrandBlack), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Surface(color = tokens.brandYellow, shape = RoundedCornerShape(50)) {
                    Row(Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.DeliveryDining, null, tint = tokens.onBrandYellow, modifier = Modifier.size(15.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("EARN ON YOUR SCHEDULE", style = MaterialTheme.typography.labelMedium, color = tokens.onBrandYellow, fontWeight = FontWeight.ExtraBold)
                    }
                }
                Text("Short trips in your zone.\nPaid every week, or instantly.", style = MaterialTheme.typography.headlineSmall, color = Color.White)
            }
        }
        Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = tokens.surface), border = BorderStroke(1.dp, tokens.border), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(if (otpSent) "Verify OTP" else "Sign in to ride", style = MaterialTheme.typography.titleLarge, color = tokens.ink)
                QkTextField(phone, { phone = it.filter(Char::isDigit).take(10); error = null }, "10-digit Mobile Number", prefix = "+91 ",
                    leadingIcon = Icons.Rounded.PhoneAndroid, keyboardType = KeyboardType.Phone, enabled = !otpSent, testTag = "rider_phone_input")
                AnimatedVisibility(otpSent) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Code sent to +91 $phone (demo: 1234)", style = MaterialTheme.typography.bodySmall, color = tokens.muted)
                        OtpBoxes(otp, { otp = it; error = null }, enabled = locked == 0)
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            TextButton(onClick = { otpSent = false; otp = "" }) { Text("Change number", color = tokens.ink) }
                            TextButton(enabled = resendIn == 0, onClick = { resendIn = 30; otp = "" }) {
                                Text(if (resendIn > 0) "Resend in 0:${resendIn.toString().padStart(2, '0')}" else "Resend OTP", color = if (resendIn > 0) tokens.muted else tokens.ink)
                            }
                        }
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(agreed, { agreed = it }, colors = CheckboxDefaults.colors(checkedColor = tokens.brandYellow, checkmarkColor = tokens.onBrandYellow), modifier = Modifier.zoomScaledControl())
                    Text("I agree to the Partner Terms and Privacy Policy", style = MaterialTheme.typography.bodySmall, color = tokens.muted)
                }
                when {
                    locked > 0 -> Text("Too many wrong attempts. Try again in $locked s.", style = MaterialTheme.typography.bodySmall, color = tokens.alertRed)
                    error != null -> Text(error!!, style = MaterialTheme.typography.bodySmall, color = tokens.alertRed)
                }
                PrimaryButton(if (otpSent) "Verify & Continue" else "Send OTP", enabled = agreed && locked == 0, testTag = "rider_login_button", onClick = {
                    if (!otpSent) {
                        if (phone.length != 10 || phone.first() !in '6'..'9') error = "Enter a valid 10-digit Indian mobile number." else { otpSent = true; resendIn = 30 }
                    } else if (otp.length != 4) error = "Enter the 4-digit OTP."
                    else if (!onVerify(phone, otp)) {
                        wrong++; otp = ""
                        if (wrong >= 3) { locked = 30; wrong = 0 } else error = "Incorrect OTP. ${3 - wrong} attempt(s) left."
                    }
                })
            }
        }
        Text("New partner? Sign in with your number to start KYC (licence, RC, bank, photo).", style = MaterialTheme.typography.bodySmall, color = tokens.muted, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
    }
}

// ─────────────────────────────────────────────────────────────────────────────────────────────
// 1b. Rider KYC: verification badges for each document
// ─────────────────────────────────────────────────────────────────────────────────────────────

private val DL_REGEX = Regex("^[A-Z]{2}[0-9]{2}[0-9]{4}[0-9]{7}$")
private val RC_REGEX = Regex("^[A-Z]{2}[0-9]{1,2}[A-Z]{1,3}[0-9]{4}$")
private val IFSC_REGEX = Regex("^[A-Z]{4}0[A-Z0-9]{6}$")

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RiderKycScreen(
    profile: RiderProfile,
    onUpdate: ((RiderProfile) -> RiderProfile) -> Unit,
    onSubmitDoc: (KycDoc) -> Unit,
    onContinue: () -> Unit,
    onBack: (() -> Unit)?,
    onSignOut: () -> Unit
) {
    val tokens = QuickKartThemeTokens.colors
    var editing by remember { mutableStateOf<KycDoc?>(null) }
    var selfie by remember { mutableStateOf<Bitmap?>(null) }
    val camera = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bmp ->
        if (bmp != null) { selfie = bmp; onSubmitDoc(KycDoc.PHOTO) }
    }
    val required = profile.requiredDocs
    val allVerified = required.all { profile.status(it) == DocStatus.VERIFIED }
    val nameOk = profile.name.trim().length >= 2

    Column(Modifier.fillMaxSize().background(tokens.background).statusBarsPadding().navigationBarsPadding().imePadding()) {
        ScreenHeader("Partner KYC", onBack = onBack, subtitle = "${required.count { profile.status(it) == DocStatus.VERIFIED }} of ${required.size} verified",
            actions = { if (onBack == null) TextButton(onClick = onSignOut) { Text("Sign out", color = tokens.muted) } })
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            QkTextField(profile.name, { v -> onUpdate { it.copy(name = v.take(40)) } }, "Full name (as on licence)", error = if (!nameOk && profile.name.isNotEmpty()) "Enter your full name" else null, testTag = "rider_name")
            Text("Vehicle", style = MaterialTheme.typography.titleSmall, color = tokens.ink)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                VehicleType.entries.forEach { v -> ChoiceChip(v.label, profile.vehicleType == v, { onUpdate { it.copy(vehicleType = v) } }) }
            }
            if (profile.vehicleType == VehicleType.BICYCLE) Text("Bicycles don't need an RC. Orders are limited to 3 km.", style = MaterialTheme.typography.labelSmall, color = tokens.muted)
            QkCard {
                Text("KYC verification status", style = MaterialTheme.typography.titleSmall, color = tokens.ink)
                required.forEach { doc ->
                    val status = profile.status(doc)
                    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                        .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(Modifier.size(40.dp).background(tokens.brandYellowSoft, CircleShape), contentAlignment = Alignment.Center) {
                            if (doc == KycDoc.PHOTO && selfie != null) Image(selfie!!.asImageBitmap(), null, contentScale = ContentScale.Crop, modifier = Modifier.size(40.dp).clip(CircleShape))
                            else Icon(when (doc) { KycDoc.LICENSE -> Icons.Rounded.Badge; KycDoc.RC -> Icons.Rounded.TwoWheeler; KycDoc.BANK -> Icons.Rounded.AccountBalance; KycDoc.PHOTO -> Icons.Rounded.Face }, null, tint = tokens.ink)
                        }
                        Column(Modifier.weight(1f)) {
                            Text(doc.label, style = MaterialTheme.typography.titleSmall, color = tokens.ink)
                            Text(
                                when (doc) {
                                    KycDoc.LICENSE -> profile.licenseNumber.ifBlank { "Licence number" }
                                    KycDoc.RC -> profile.vehicleNumber.ifBlank { "Registration number" }
                                    KycDoc.BANK -> if (profile.bankAccount.isBlank()) "Account & IFSC" else "${profile.bankMasked} · ${profile.ifsc}"
                                    KycDoc.PHOTO -> "Clear selfie, no helmet or sunglasses"
                                }, style = MaterialTheme.typography.labelSmall, color = tokens.muted
                            )
                        }
                        when (status) {
                            DocStatus.VERIFIED -> StatusBadge("Verified", BadgeTone.SUCCESS)
                            DocStatus.PENDING -> Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Rounded.HourglassTop, null, tint = tokens.muted, modifier = Modifier.size(14.dp)); StatusBadge("Verifying", BadgeTone.WARNING) }
                            else -> Surface(color = tokens.brandYellow, shape = RoundedCornerShape(10.dp), onClick = { if (doc == KycDoc.PHOTO) runCatching { camera.launch(null) } else editing = doc }) {
                                Text(if (status == DocStatus.REJECTED) "Re-upload" else "Add", style = MaterialTheme.typography.labelLarge, color = tokens.onBrandYellow, modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp))
                            }
                        }
                    }
                }
                if (allVerified) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Rounded.CheckCircle, null, tint = tokens.deliveryGreenDeep)
                        Text("All documents verified!", style = MaterialTheme.typography.titleSmall, color = tokens.deliveryGreenDeep)
                    }
                }
            }
            TextButton(onClick = {
                onUpdate { it.copy(name = it.name.ifBlank { "Arjun Reddy" }, licenseNumber = it.licenseNumber.ifBlank { "TS0920190012345" }, vehicleNumber = it.vehicleNumber.ifBlank { "TS09EK4821" }, bankAccount = it.bankAccount.ifBlank { "40012345678" }, ifsc = it.ifsc.ifBlank { "SBIN0001234" }) }
            }) { Text("Fill sample details (demo)", color = tokens.muted) }
        }
        PrimaryButton(
            if (allVerified) "Continue" else "Complete all documents to continue",
            enabled = allVerified && nameOk, onClick = onContinue, modifier = Modifier.padding(16.dp), testTag = "kyc_continue"
        )
    }

    editing?.let { doc ->
        var a by remember(doc) { mutableStateOf(when (doc) { KycDoc.LICENSE -> profile.licenseNumber; KycDoc.RC -> profile.vehicleNumber; else -> profile.bankAccount }) }
        var b by remember(doc) { mutableStateOf(profile.ifsc) }
        var c by remember(doc) { mutableStateOf("") }
        val err: String? = when (doc) {
            KycDoc.LICENSE -> if (!DL_REGEX.matches(a)) "15 characters, e.g. TS0920190012345" else null
            KycDoc.RC -> if (!RC_REGEX.matches(a)) "e.g. TS09EK4821" else null
            KycDoc.BANK -> when { a.length !in 9..18 -> "Account number is 9–18 digits"; c != a -> "Account numbers don't match"; !IFSC_REGEX.matches(b) -> "IFSC, e.g. SBIN0001234"; else -> null }
            KycDoc.PHOTO -> null
        }
        AlertDialog(
            onDismissRequest = { editing = null },
            title = { Text(doc.label) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    when (doc) {
                        KycDoc.LICENSE -> QkTextField(a, { a = it.uppercase().filter(Char::isLetterOrDigit).take(15) }, "Driving licence number", error = err.takeIf { a.length >= 15 })
                        KycDoc.RC -> QkTextField(a, { a = it.uppercase().filter(Char::isLetterOrDigit).take(10) }, "Vehicle registration number", error = err.takeIf { a.length >= 9 })
                        KycDoc.BANK -> {
                            QkTextField(a, { a = it.filter(Char::isDigit).take(18) }, "Account number", keyboardType = KeyboardType.NumberPassword)
                            QkTextField(c, { c = it.filter(Char::isDigit).take(18) }, "Confirm account number", keyboardType = KeyboardType.Number, error = "Account numbers don't match".takeIf { c.isNotEmpty() && c != a })
                            QkTextField(b, { b = it.uppercase().filter(Char::isLetterOrDigit).take(11) }, "IFSC", error = "IFSC, e.g. SBIN0001234".takeIf { b.length == 11 && !IFSC_REGEX.matches(b) })
                        }
                        KycDoc.PHOTO -> {}
                    }
                    Text("We verify documents automatically in a few seconds (prototype).", style = MaterialTheme.typography.labelSmall, color = tokens.muted)
                }
            },
            confirmButton = {
                TextButton(enabled = err == null, onClick = {
                    onUpdate {
                        when (doc) {
                            KycDoc.LICENSE -> it.copy(licenseNumber = a)
                            KycDoc.RC -> it.copy(vehicleNumber = a)
                            KycDoc.BANK -> it.copy(bankAccount = a, ifsc = b)
                            KycDoc.PHOTO -> it
                        }
                    }
                    onSubmitDoc(doc); editing = null
                }) { Text("Submit", color = tokens.ink, fontWeight = FontWeight.Bold) }
            },
            dismissButton = { TextButton(onClick = { editing = null }) { Text("Cancel", color = tokens.muted) } }
        )
    }
}
