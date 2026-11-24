package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.Neighborhood
import com.example.ui.components.QuickKartLogo
import com.example.ui.theme.QuickKartMetricsTokens
import com.example.ui.theme.QuickKartThemeTokens

@Composable
fun LoginScreen(
    neighborhoods: List<Neighborhood>,
    selectedNeighborhoodId: String,
    isDarkTheme: Boolean,
    onToggleTheme: (Boolean) -> Unit,
    onSelectNeighborhood: (String) -> Unit,
    onSignInWithPhone: (phone: String, neighborhoodId: String, firstName: String, lastName: String, email: String) -> Unit,
    onContinueAsGuest: (String) -> Unit
) {
    val tokens = QuickKartThemeTokens.colors
    val metrics = QuickKartMetricsTokens.metrics
    var phoneNumber by remember { mutableStateOf("9845077218") }
    var otpSent by remember { mutableStateOf(false) }
    var otpCode by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var showProfileSetup by remember { mutableStateOf(false) }
    var showGoogleFlow by remember { mutableStateOf(false) }
    var googleProfile by remember { mutableStateOf<GoogleAccountProfile?>(null) }
    var googleVerifiedPhone by remember { mutableStateOf("") }

    val activeNeighborhood = neighborhoods.find { it.id == selectedNeighborhoodId }
        ?: neighborhoods.first()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(tokens.background)
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 560.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = metrics.screenHorizontalPadding, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Top Brand Row + Theme Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    QuickKartLogo(
                        size = metrics.badgeButtonSize + 4.dp,
                        cornerRadius = 12.dp
                    )
                    Column {
                        Text(
                            text = "QuickKart",
                            style = MaterialTheme.typography.headlineSmall,
                            color = tokens.ink
                        )
                        Text(
                            text = "Hyperlocal Groceries",
                            style = MaterialTheme.typography.bodySmall,
                            color = tokens.muted
                        )
                    }
                }

                IconButton(
                    onClick = { onToggleTheme(!isDarkTheme) },
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("login_theme_toggle")
                ) {
                    Surface(
                        color = tokens.surface,
                        shape = CircleShape,
                        border = BorderStroke(1.dp, tokens.border),
                        modifier = Modifier.size(metrics.badgeButtonSize)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (isDarkTheme) Icons.Rounded.LightMode else Icons.Rounded.DarkMode,
                                contentDescription = "Toggle Light or Dark mode",
                                tint = tokens.ink,
                                modifier = Modifier.size(metrics.iconMedium)
                            )
                        }
                    }
                }
            }

            // Hero Showcase Card with 8-Minute Delivery Branding
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF151A22)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min)
                        .heightIn(min = 210.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_login_hero),
                        contentDescription = "Fresh groceries in 8 minutes",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.matchParentSize()
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Black.copy(alpha = 0.15f),
                                        Color(0xFF0D1015).copy(alpha = 0.88f)
                                    )
                                )
                            )
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(18.dp)
                    ) {
                        Surface(
                            color = tokens.brandYellow,
                            shape = RoundedCornerShape(50)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Timer,
                                    contentDescription = null,
                                    tint = tokens.onBrandYellow,
                                    modifier = Modifier.size(15.dp)
                                )
                                Text(
                                    text = "8-MINUTE DELIVERY",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = tokens.onBrandYellow,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "Fresh groceries,\nfrom pod to door in ${activeNeighborhood.etaMinutes} mins.",
                                style = MaterialTheme.typography.headlineSmall,
                                color = Color.White
                            )
                            Text(
                                text = "Dairy, sourdough, cold brew & farm produce stocked nearby.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }

            // Neighborhood Selector
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.LocationOn,
                        contentDescription = null,
                        tint = tokens.deliveryGreen,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Select your delivery neighborhood",
                        style = MaterialTheme.typography.titleMedium,
                        color = tokens.ink
                    )
                }

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(end = 8.dp)
                ) {
                    items(neighborhoods, key = { it.id }) { item ->
                        val isSelected = item.id == activeNeighborhood.id
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (isSelected) tokens.brandYellow else tokens.surface,
                            border = BorderStroke(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) tokens.brandYellow else tokens.border
                            ),
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { onSelectNeighborhood(item.id) }
                                .testTag("neighborhood_chip_${item.id}")
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = item.name,
                                        style = MaterialTheme.typography.labelLarge,
                                        color = if (isSelected) tokens.onBrandYellow else tokens.ink
                                    )
                                    Surface(
                                        color = if (isSelected) Color(0xFF141414) else tokens.deliveryGreenSoft,
                                        shape = RoundedCornerShape(50)
                                    ) {
                                        Text(
                                            text = "${item.etaMinutes}m",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (isSelected) tokens.brandYellow else tokens.deliveryGreenDeep,
                                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = item.city,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isSelected) tokens.onBrandYellow.copy(alpha = 0.78f) else tokens.muted
                                )
                            }
                        }
                    }
                }
            }

            // Phone + OTP Card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = tokens.surface),
                border = BorderStroke(1.dp, tokens.border),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = if (otpSent) "Verify OTP" else "Sign in with Phone",
                        style = MaterialTheme.typography.titleLarge,
                        color = tokens.ink
                    )

                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = { input ->
                            phoneNumber = input.filter { it.isDigit() }.take(10)
                            errorMsg = null
                        },
                        enabled = !otpSent,
                        label = { Text("10-digit Mobile Number") },
                        prefix = { Text("+91 ", fontWeight = FontWeight.Bold) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Rounded.PhoneAndroid,
                                contentDescription = null
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = tokens.ink,
                            unfocusedBorderColor = tokens.border
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("login_phone_input")
                    )

                    AnimatedVisibility(visible = otpSent) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = otpCode,
                                onValueChange = { input ->
                                    otpCode = input.filter { it.isDigit() }.take(4)
                                    errorMsg = null
                                },
                                label = { Text("4-digit OTP (Use 1234)") },
                                placeholder = { Text("1234") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("login_otp_input")
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Demo OTP sent to +91 $phoneNumber",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = tokens.deliveryGreen
                                )
                                TextButton(
                                    onClick = {
                                        otpSent = false
                                        otpCode = ""
                                    }
                                ) {
                                    Text("Change")
                                }
                            }
                        }
                    }

                    if (errorMsg != null) {
                        Text(
                            text = errorMsg!!,
                            style = MaterialTheme.typography.bodySmall,
                            color = tokens.alertRed
                        )
                    }

                    Button(
                        onClick = {
                            if (!otpSent) {
                                if (phoneNumber.length == 10) {
                                    otpSent = true
                                    otpCode = "1234"
                                    errorMsg = null
                                } else {
                                    errorMsg = "Please enter a valid 10-digit mobile number."
                                }
                            } else {
                                if (otpCode.length == 4) {
                                    showProfileSetup = true
                                } else {
                                    errorMsg = "Enter the 4-digit OTP (e.g. 1234)."
                                }
                            }
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = tokens.brandYellow,
                            contentColor = tokens.onBrandYellow
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("login_primary_button")
                    ) {
                        if (otpSent) {
                            Icon(
                                imageVector = Icons.Rounded.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                        }
                        Text(
                            text = if (otpSent) "Verify & Complete Profile" else "Send OTP",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }

            // Sign in with Google (design prototype)
            SignInOptionDivider()
            ContinueWithGoogleButton(onClick = { showGoogleFlow = true })

            // One-tap Guest Sign In
            OutlinedButton(
                onClick = { onContinueAsGuest(activeNeighborhood.id) },
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, tokens.border),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("continue_as_guest_button")
            ) {
                Text(
                    text = "Continue as Guest",
                    style = MaterialTheme.typography.labelLarge,
                    color = tokens.ink
                )
            }
        }
    }

    if (showProfileSetup) {
        AccountSetupDialog(
            initialPhone = "+91 $phoneNumber",
            initialFirstName = "",
            initialLastName = "",
            initialEmail = "",
            title = "Account Details",
            subtitle = "Post-OTP verification: enter your minimum required details. Only First Name is mandatory; Last Name and Email are optional.",
            onDismiss = { showProfileSetup = false },
            onCompleteProfile = { firstName, lastName, email ->
                showProfileSetup = false
                onSignInWithPhone("+91 $phoneNumber", activeNeighborhood.id, firstName, lastName, email)
            }
        )
    }

    if (showGoogleFlow) {
        GoogleSignInFlow(
            onDismiss = { showGoogleFlow = false },
            onSignedIn = { profile, verifiedPhone ->
                showGoogleFlow = false
                googleVerifiedPhone = verifiedPhone
                googleProfile = profile
            }
        )
    }

    // Final confirmation reuses the same Account Details step as phone sign-in,
    // prefilled from the Google profile.
    googleProfile?.let { profile ->
        AccountSetupDialog(
            initialPhone = googleVerifiedPhone,
            initialFirstName = profile.firstName,
            initialLastName = profile.lastName,
            initialEmail = profile.email,
            title = "Confirm your details",
            subtitle = "We've filled these in from your Google account. Edit anything before you continue.",
            onDismiss = { googleProfile = null },
            onCompleteProfile = { firstName, lastName, email ->
                googleProfile = null
                onSignInWithPhone(googleVerifiedPhone, activeNeighborhood.id, firstName, lastName, email)
            }
        )
    }
}
