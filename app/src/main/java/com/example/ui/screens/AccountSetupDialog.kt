package com.example.ui.screens

import android.util.Patterns
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.QuickKartThemeTokens

@Composable
fun AccountSetupDialog(
    initialPhone: String,
    initialFirstName: String = "",
    initialLastName: String = "",
    initialEmail: String = "",
    title: String = "Account Details",
    subtitle: String = "Enter minimum required details for account management. Only First Name is mandatory.",
    onDismiss: (() -> Unit)? = null,
    onCompleteProfile: (firstName: String, lastName: String, email: String) -> Unit
) {
    val tokens = QuickKartThemeTokens.colors

    var firstName by remember(initialFirstName) { mutableStateOf(initialFirstName) }
    var lastName by remember(initialLastName) { mutableStateOf(initialLastName) }
    var email by remember(initialEmail) { mutableStateOf(initialEmail) }
    var firstNameError by remember { mutableStateOf<String?>(null) }
    var lastNameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = { onDismiss?.invoke() },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = onDismiss != null,
            dismissOnClickOutside = onDismiss != null
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
                    .testTag("account_setup_dialog")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(22.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Surface(
                            color = tokens.brandYellow,
                            shape = CircleShape,
                            modifier = Modifier
                                .size(56.dp)
                                .align(Alignment.Center)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Rounded.Person,
                                    contentDescription = null,
                                    tint = tokens.onBrandYellow,
                                    modifier = Modifier.size(30.dp)
                                )
                            }
                        }

                        if (onDismiss != null) {
                            IconButton(
                                onClick = onDismiss,
                                modifier = Modifier
                                    .size(48.dp)
                                    .align(Alignment.TopEnd)
                                    .testTag("close_account_setup_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Close,
                                    contentDescription = "Close account details",
                                    tint = tokens.muted
                                )
                            }
                        }
                    }

                    Column(
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

                    OutlinedTextField(
                        value = initialPhone,
                        onValueChange = {},
                        enabled = false,
                        label = { Text("Verified Mobile Number") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = firstName,
                        onValueChange = {
                            firstName = it
                            firstNameError = null
                        },
                        label = { Text("First Name * (Required)") },
                        placeholder = { Text("e.g., Aarav") },
                        isError = firstNameError != null,
                        supportingText = {
                            Text(
                                text = firstNameError ?: "Mandatory • Min 2 letters",
                                color = if (firstNameError != null) tokens.alertRed else tokens.muted
                            )
                        },
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words
                        ),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("setup_firstname_input")
                    )

                    OutlinedTextField(
                        value = lastName,
                        onValueChange = {
                            lastName = it
                            lastNameError = null
                        },
                        label = { Text("Last Name (Optional)") },
                        placeholder = { Text("e.g., Sharma") },
                        isError = lastNameError != null,
                        supportingText = if (lastNameError != null) {
                            { Text(lastNameError!!, color = tokens.alertRed) }
                        } else null,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words
                        ),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("setup_lastname_input")
                    )

                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            emailError = null
                        },
                        label = { Text("Email Address (Optional)") },
                        placeholder = { Text("e.g., aarav@example.com") },
                        isError = emailError != null,
                        supportingText = if (emailError != null) {
                            { Text(emailError!!, color = tokens.alertRed) }
                        } else null,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("setup_email_input")
                    )

                    Button(
                        onClick = {
                            val fNameClean = firstName.trim()
                            val lNameClean = lastName.trim()
                            val emailClean = email.trim()

                            var hasError = false
                            if (fNameClean.length < 2 || !fNameClean.all { it.isLetter() || it == ' ' || it == '.' || it == '\'' || it == '-' } || !fNameClean.any { it.isLetter() }) {
                                firstNameError = "Enter a valid first name (at least 2 letters)."
                                hasError = true
                            }
                            if (lNameClean.isNotEmpty() && (!lNameClean.all { it.isLetter() || it == ' ' || it == '.' || it == '\'' || it == '-' } || !lNameClean.any { it.isLetter() })) {
                                lastNameError = "Last name can only contain letters."
                                hasError = true
                            }
                            if (emailClean.isNotEmpty() && !Patterns.EMAIL_ADDRESS.matcher(emailClean).matches()) {
                                emailError = "Enter a valid email address or leave blank."
                                hasError = true
                            }
                            if (!hasError) {
                                onCompleteProfile(fNameClean, lNameClean, emailClean)
                            }
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = tokens.brandYellow,
                            contentColor = tokens.onBrandYellow
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("save_account_profile_button")
                    ) {
                        Icon(Icons.Rounded.CheckCircle, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Save & Continue", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
    }
}
