package com.example.ui.screens

import android.util.Patterns
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.PersonAdd
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material.icons.rounded.Shield
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.components.QuickKartLogo
import com.example.ui.theme.QuickKartThemeTokens
import kotlinx.coroutines.delay

/*
 * Sign in with Google — DESIGN PROTOTYPE.
 *
 * Simulates the full Google sign-in journey locally (no Google / Firebase calls):
 * account chooser → consent → signing in → link & verify mobile (OTP 1234) →
 * the existing Account Details dialog prefilled from the Google profile.
 * To go live, replace the demo accounts and the simulated delay with
 * Credential Manager + Firebase Auth; the UI and callbacks can stay as they are.
 */

private val GoogleBlue = Color(0xFF4285F4)
private val GoogleRed = Color(0xFFEA4335)
private val GoogleYellow = Color(0xFFFBBC05)
private val GoogleGreen = Color(0xFF34A853)

/** Profile data returned by the (simulated) Google account. */
data class GoogleAccountProfile(
    val displayName: String,
    val email: String,
    val avatarColor: Color
) {
    val firstName: String get() = displayName.trim().split(" ").firstOrNull().orEmpty()
    val lastName: String get() = displayName.trim().split(" ").drop(1).joinToString(" ")
    val initial: String get() = displayName.trim().firstOrNull()?.uppercase() ?: "G"
}

private val DemoGoogleAccounts = listOf(
    GoogleAccountProfile("Aarav Sharma", "aarav.sharma@gmail.com", Color(0xFF1E88E5)),
    GoogleAccountProfile("Diya Menon", "diya.menon@gmail.com", Color(0xFF8E24AA))
)

private enum class GoogleStep { CHOOSE_ACCOUNT, ADD_ACCOUNT, CONSENT, SIGNING_IN, LINK_PHONE }

/** The multi-colour Google "G" mark, drawn in Compose so no image asset is needed. */
@Composable
fun GoogleGLogo(size: Dp = 20.dp, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(size)) {
        val stroke = this.size.minDimension * 0.22f
        val inset = stroke / 2f
        val arcSize = Size(this.size.width - stroke, this.size.height - stroke)
        val topLeft = Offset(inset, inset)
        fun arc(color: Color, start: Float, sweep: Float) = drawArc(
            color = color,
            startAngle = start,
            sweepAngle = sweep,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = stroke)
        )
        arc(GoogleRed, 205f, 110f)
        arc(GoogleYellow, 150f, 55f)
        arc(GoogleGreen, 40f, 110f)
        arc(GoogleBlue, -15f, 55f)
        // Blue crossbar of the "G"
        drawRect(
            color = GoogleBlue,
            topLeft = Offset(this.size.width / 2f, this.size.height / 2f - stroke / 2f),
            size = Size(this.size.width / 2f - inset / 2f, stroke)
        )
    }
}

/** "Continue with Google" button styled like the app's secondary buttons. */
@Composable
fun ContinueWithGoogleButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val tokens = QuickKartThemeTokens.colors
    OutlinedButton(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, tokens.border),
        colors = ButtonDefaults.outlinedButtonColors(containerColor = tokens.surface),
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .testTag("continue_with_google_button")
    ) {
        GoogleGLogo(size = 20.dp)
        Spacer(Modifier.width(10.dp))
        Text(
            text = "Continue with Google",
            style = MaterialTheme.typography.labelLarge,
            color = tokens.ink
        )
    }
}

/** "──── or ────" separator between sign-in options. */
@Composable
fun SignInOptionDivider(modifier: Modifier = Modifier) {
    val tokens = QuickKartThemeTokens.colors
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        HorizontalDivider(modifier = Modifier.weight(1f), color = tokens.border)
        Text("or", style = MaterialTheme.typography.labelMedium, color = tokens.muted)
        HorizontalDivider(modifier = Modifier.weight(1f), color = tokens.border)
    }
}

/**
 * Full Google sign-in journey. Calls [onSignedIn] with the Google profile and the
 * verified mobile number; the caller then shows the Account Details confirmation.
 */
@Composable
fun GoogleSignInFlow(
    onDismiss: () -> Unit,
    /** false when linking Google to an already signed-in profile (phone is already verified). */
    requirePhone: Boolean = true,
    onSignedIn: (profile: GoogleAccountProfile, verifiedPhone: String) -> Unit
) {
    val tokens = QuickKartThemeTokens.colors
    var step by remember { mutableStateOf(GoogleStep.CHOOSE_ACCOUNT) }
    var selected by remember { mutableStateOf<GoogleAccountProfile?>(null) }

    val busy = step == GoogleStep.SIGNING_IN

    Dialog(
        onDismissRequest = { if (!busy) onDismiss() },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = !busy,
            dismissOnClickOutside = !busy
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(tokens.background.copy(alpha = 0.94f))
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
                    .widthIn(max = 480.dp)
                    .fillMaxWidth()
                    .padding(16.dp)
                    .testTag("google_sign_in_dialog")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(22.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    FlowHeader(
                        showBack = step == GoogleStep.ADD_ACCOUNT || step == GoogleStep.CONSENT,
                        showClose = !busy,
                        onBack = { step = GoogleStep.CHOOSE_ACCOUNT },
                        onClose = onDismiss
                    )

                    when (step) {
                        GoogleStep.CHOOSE_ACCOUNT -> ChooseAccountStep(
                            onPick = { account ->
                                selected = account
                                step = GoogleStep.CONSENT
                            },
                            onUseAnother = { step = GoogleStep.ADD_ACCOUNT }
                        )

                        GoogleStep.ADD_ACCOUNT -> AddAccountStep(
                            onNext = { account ->
                                selected = account
                                step = GoogleStep.CONSENT
                            }
                        )

                        GoogleStep.CONSENT -> ConsentStep(
                            account = selected!!,
                            onCancel = onDismiss,
                            onContinue = { step = GoogleStep.SIGNING_IN }
                        )

                        GoogleStep.SIGNING_IN -> SigningInStep(
                            account = selected!!,
                            onDone = {
                                if (requirePhone) step = GoogleStep.LINK_PHONE
                                else onSignedIn(selected!!, "")
                            }
                        )

                        GoogleStep.LINK_PHONE -> LinkPhoneStep(
                            account = selected!!,
                            onVerified = { phone -> onSignedIn(selected!!, phone) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FlowHeader(
    showBack: Boolean,
    showClose: Boolean,
    onBack: () -> Unit,
    onClose: () -> Unit
) {
    val tokens = QuickKartThemeTokens.colors
    Box(modifier = Modifier.fillMaxWidth()) {
        if (showBack) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(48.dp)
                    .align(Alignment.CenterStart)
                    .testTag("google_flow_back")
            ) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = tokens.muted)
            }
        }
        Row(
            modifier = Modifier.align(Alignment.Center),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                color = Color.White,
                shape = CircleShape,
                border = BorderStroke(1.dp, tokens.border),
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) { GoogleGLogo(size = 22.dp) }
            }
            Text("+", style = MaterialTheme.typography.titleMedium, color = tokens.muted)
            QuickKartLogo(size = 44.dp, cornerRadius = 12.dp)
        }
        if (showClose) {
            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .size(48.dp)
                    .align(Alignment.CenterEnd)
                    .testTag("google_flow_close")
            ) {
                Icon(Icons.Rounded.Close, contentDescription = "Close", tint = tokens.muted)
            }
        }
    }
}

@Composable
private fun StepTitle(title: String, subtitle: String) {
    val tokens = QuickKartThemeTokens.colors
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = tokens.ink,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = tokens.muted,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun AccountAvatar(account: GoogleAccountProfile, size: Dp = 40.dp) {
    Surface(color = account.avatarColor, shape = CircleShape, modifier = Modifier.size(size)) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = account.initial,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun ChooseAccountStep(
    onPick: (GoogleAccountProfile) -> Unit,
    onUseAnother: () -> Unit
) {
    val tokens = QuickKartThemeTokens.colors
    StepTitle(
        title = "Choose an account",
        subtitle = "to continue to QuickKart"
    )

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = tokens.surfaceElevated,
        border = BorderStroke(1.dp, tokens.border),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            DemoGoogleAccounts.forEachIndexed { index, account ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onPick(account) }
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                        .testTag("google_account_$index"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AccountAvatar(account)
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = account.displayName,
                            style = MaterialTheme.typography.titleSmall,
                            color = tokens.ink
                        )
                        Text(
                            text = account.email,
                            style = MaterialTheme.typography.bodySmall,
                            color = tokens.muted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                HorizontalDivider(color = tokens.border)
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onUseAnother)
                    .padding(horizontal = 14.dp, vertical = 14.dp)
                    .testTag("google_use_another_account"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(color = tokens.brandYellowSoft, shape = CircleShape, modifier = Modifier.size(40.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Rounded.PersonAdd, contentDescription = null, tint = tokens.ink)
                    }
                }
                Text(
                    text = "Use another account",
                    style = MaterialTheme.typography.titleSmall,
                    color = tokens.ink
                )
            }
        }
    }

    PrivacyNote()
}

@Composable
private fun AddAccountStep(onNext: (GoogleAccountProfile) -> Unit) {
    val tokens = QuickKartThemeTokens.colors
    var email by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    StepTitle(title = "Sign in", subtitle = "Use your Google Account")

    OutlinedTextField(
        value = email,
        onValueChange = {
            email = it.trim()
            error = null
        },
        label = { Text("Email or phone") },
        leadingIcon = { Icon(Icons.Rounded.Email, contentDescription = null) },
        isError = error != null,
        supportingText = { Text(error ?: "Prototype: any valid Gmail address works", color = if (error != null) tokens.alertRed else tokens.muted) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = tokens.ink,
            unfocusedBorderColor = tokens.border
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("google_email_input")
    )

    PrimaryYellowButton(
        text = "Next",
        testTag = "google_email_next",
        onClick = {
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                error = "Enter a valid email address."
            } else {
                val name = email.substringBefore("@")
                    .split('.', '_', '-')
                    .filter { it.isNotBlank() }
                    .joinToString(" ") { part -> part.replaceFirstChar { it.uppercase() } }
                    .ifBlank { "QuickKart User" }
                onNext(GoogleAccountProfile(name, email, GoogleGreen))
            }
        }
    )
}

@Composable
private fun ConsentStep(
    account: GoogleAccountProfile,
    onCancel: () -> Unit,
    onContinue: () -> Unit
) {
    val tokens = QuickKartThemeTokens.colors
    StepTitle(
        title = "Sign in to QuickKart",
        subtitle = "QuickKart will receive the following from Google"
    )

    // Selected account chip
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Surface(
            shape = RoundedCornerShape(50),
            color = tokens.surfaceElevated,
            border = BorderStroke(1.dp, tokens.border)
        ) {
            Row(
                modifier = Modifier.padding(start = 4.dp, end = 14.dp, top = 4.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AccountAvatar(account, size = 28.dp)
                Text(
                    account.email,
                    style = MaterialTheme.typography.labelMedium,
                    color = tokens.ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = tokens.surfaceElevated,
        border = BorderStroke(1.dp, tokens.border),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ConsentRow(Icons.Rounded.AccountCircle, "Name & profile picture", account.displayName)
            ConsentRow(Icons.Rounded.Email, "Email address", account.email)
            ConsentRow(Icons.Rounded.Lock, "Never shared", "Your Google password stays with Google")
        }
    }

    PrivacyNote()

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        OutlinedButton(
            onClick = onCancel,
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, tokens.border),
            modifier = Modifier
                .weight(1f)
                .height(50.dp)
                .testTag("google_consent_cancel")
        ) {
            Text("Cancel", style = MaterialTheme.typography.labelLarge, color = tokens.ink)
        }
        Button(
            onClick = onContinue,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = tokens.brandYellow,
                contentColor = tokens.onBrandYellow
            ),
            modifier = Modifier
                .weight(1f)
                .height(50.dp)
                .testTag("google_consent_continue")
        ) {
            Text("Continue", style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun ConsentRow(icon: ImageVector, title: String, value: String) {
    val tokens = QuickKartThemeTokens.colors
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(color = tokens.deliveryGreenSoft, shape = CircleShape, modifier = Modifier.size(36.dp)) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = tokens.deliveryGreenDeep, modifier = Modifier.size(20.dp))
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall, color = tokens.ink)
            Text(
                value,
                style = MaterialTheme.typography.bodySmall,
                color = tokens.muted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun SigningInStep(account: GoogleAccountProfile, onDone: () -> Unit) {
    val tokens = QuickKartThemeTokens.colors
    LaunchedEffect(account) {
        delay(1400) // Simulated Google token exchange
        onDone()
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .testTag("google_signing_in"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            CircularProgressIndicator(
                color = tokens.brandYellow,
                strokeWidth = 3.dp,
                modifier = Modifier.size(72.dp)
            )
            AccountAvatar(account, size = 52.dp)
        }
        Text(
            text = "Signing in as ${account.firstName}…",
            style = MaterialTheme.typography.titleMedium,
            color = tokens.ink,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = account.email,
            style = MaterialTheme.typography.bodySmall,
            color = tokens.muted
        )
    }
}

@Composable
private fun LinkPhoneStep(
    account: GoogleAccountProfile,
    onVerified: (String) -> Unit
) {
    val tokens = QuickKartThemeTokens.colors
    var phone by remember { mutableStateOf("") }
    var otpSent by remember { mutableStateOf(false) }
    var otp by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    // Success banner
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = tokens.deliveryGreenSoft,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = tokens.deliveryGreenDeep)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Google account connected",
                    style = MaterialTheme.typography.titleSmall,
                    color = tokens.deliveryGreenDeep,
                    fontWeight = FontWeight.Bold
                )
                Text(account.email, style = MaterialTheme.typography.bodySmall, color = tokens.ink)
            }
        }
    }

    StepTitle(
        title = if (otpSent) "Verify OTP" else "Add your mobile number",
        subtitle = "Needed for delivery updates and rider calls. Google doesn't share your number."
    )

    OutlinedTextField(
        value = phone,
        onValueChange = { input ->
            phone = input.filter { it.isDigit() }.take(10)
            error = null
        },
        enabled = !otpSent,
        label = { Text("10-digit Mobile Number") },
        prefix = { Text("+91 ", fontWeight = FontWeight.Bold) },
        leadingIcon = { Icon(Icons.Rounded.PhoneAndroid, contentDescription = null) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = tokens.ink,
            unfocusedBorderColor = tokens.border
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("google_link_phone_input")
    )

    if (otpSent) {
        OutlinedTextField(
            value = otp,
            onValueChange = { input ->
                otp = input.filter { it.isDigit() }.take(4)
                error = null
            },
            label = { Text("4-digit OTP (Use 1234)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("google_link_otp_input")
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Demo OTP sent to +91 $phone",
                style = MaterialTheme.typography.bodySmall,
                color = tokens.deliveryGreen
            )
            TextButton(onClick = {
                otpSent = false
                otp = ""
            }) { Text("Change") }
        }
    }

    if (error != null) {
        Text(error!!, style = MaterialTheme.typography.bodySmall, color = tokens.alertRed)
    }

    PrimaryYellowButton(
        text = if (otpSent) "Verify & Continue" else "Send OTP",
        testTag = "google_link_primary_button",
        showCheck = otpSent,
        onClick = {
            if (!otpSent) {
                if (phone.length == 10) {
                    otpSent = true
                    otp = "1234"
                } else {
                    error = "Please enter a valid 10-digit mobile number."
                }
            } else {
                if (otp == "1234") {
                    onVerified("+91 $phone")
                } else {
                    error = "Incorrect OTP. Use 1234 for this prototype."
                }
            }
        }
    )
}

@Composable
private fun PrimaryYellowButton(
    text: String,
    testTag: String,
    showCheck: Boolean = false,
    onClick: () -> Unit
) {
    val tokens = QuickKartThemeTokens.colors
    Button(
        onClick = onClick,
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
        if (showCheck) {
            Icon(Icons.Rounded.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
private fun PrivacyNote() {
    val tokens = QuickKartThemeTokens.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp)),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            Icons.Rounded.Shield,
            contentDescription = null,
            tint = tokens.muted,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = "Google will share your name, email address and profile picture with QuickKart. " +
                "See QuickKart's Privacy Policy and Terms of Service.",
            style = MaterialTheme.typography.bodySmall,
            color = tokens.muted
        )
    }
}
