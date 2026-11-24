package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Eco
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.BuildConfig
import com.example.ui.components.QuickKartLogo
import com.example.ui.theme.QuickKartThemeTokens

/*
 * About & Privacy Policy — DESIGN PROTOTYPE copy.
 * Placeholder text for review; replace with legal-approved content before release.
 */

private const val SUPPORT_EMAIL = "support@quickkart.example"
private const val PRIVACY_EMAIL = "privacy@quickkart.example"
private const val POLICY_LAST_UPDATED = "24 November 2026"

@Composable
fun ColumnScope.AboutQuickKartContent() {
    val tokens = QuickKartThemeTokens.colors

    // Brand header
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("about_header"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        QuickKartLogo(size = 64.dp, cornerRadius = 16.dp)
        Text("QuickKart", style = MaterialTheme.typography.headlineSmall, color = tokens.ink, fontWeight = FontWeight.Bold)
        Text(
            "Hyperlocal groceries, delivered in minutes",
            style = MaterialTheme.typography.bodyMedium,
            color = tokens.muted,
            textAlign = TextAlign.Center
        )
        Surface(shape = RoundedCornerShape(50), color = tokens.brandYellow) {
            Text(
                "Version ${BuildConfig.VERSION_NAME}",
                style = MaterialTheme.typography.labelMedium,
                color = tokens.onBrandYellow,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(horizontal = 12.dp, vertical = 4.dp)
                    .testTag("about_version")
            )
        }
    }

    HorizontalDivider(color = tokens.border)

    LegalHeading("Our mission")
    LegalParagraph(
        "QuickKart brings fresh dairy, bakery, produce and everyday essentials from neighbourhood " +
            "dark stores to your door in minutes — with honest prices and no surprises."
    )

    LegalHeading("Why QuickKart")
    AboutPoint(Icons.Rounded.Bolt, "8-minute delivery", "Dark stores within 2 km of your neighbourhood.")
    AboutPoint(Icons.Rounded.Eco, "Fresh every day", "Produce and dairy restocked daily by local pods.")
    AboutPoint(Icons.Rounded.Verified, "Instant care", "Missing or damaged items refunded to your Wallet in seconds.")
    AboutPoint(Icons.Rounded.LocationOn, "Serving", "Bengaluru, Hyderabad, Mumbai and New Delhi.")

    LegalHeading("Contact us")
    AboutPoint(Icons.Rounded.Email, "Customer support", SUPPORT_EMAIL)

    Text(
        "© 2026 QuickKart. Prototype build for design review.",
        style = MaterialTheme.typography.labelSmall,
        color = tokens.muted,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun ColumnScope.PrivacyPolicyContent() {
    val tokens = QuickKartThemeTokens.colors

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = tokens.brandYellowSoft,
        border = BorderStroke(1.dp, tokens.brandYellow),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("privacy_summary")
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("In short", style = MaterialTheme.typography.titleSmall, color = tokens.ink, fontWeight = FontWeight.Bold)
            Text(
                "We use your details only to deliver your orders and support you. We never sell your data, " +
                    "and we never store your full card number.",
                style = MaterialTheme.typography.bodySmall,
                color = tokens.ink
            )
        }
    }
    Text(
        "Last updated: $POLICY_LAST_UPDATED",
        style = MaterialTheme.typography.labelSmall,
        color = tokens.muted,
        modifier = Modifier.testTag("privacy_last_updated")
    )

    PolicySection(
        "1. Information we collect",
        listOf(
            "Account details: name, mobile number and (optional) email or linked Google account.",
            "Delivery details: saved addresses and the map pin you choose.",
            "Orders: items, amounts, delivery instructions and ratings you give.",
            "Payments: the method used and a transaction reference. For cards we keep only the last 4 digits."
        )
    )
    PolicySection(
        "2. How we use it",
        listOf(
            "To place, deliver and track your orders, and to send order alerts you turn on.",
            "To process refunds, wallet top-ups and support requests.",
            "To personalise Buy Again, restock suggestions and offers."
        )
    )
    PolicySection(
        "3. Location",
        listOf(
            "Used only when you pin an address or tap \"Use GPS\", to find the nearest store.",
            "You can deny location access and type your address instead."
        )
    )
    PolicySection(
        "4. Sharing",
        listOf(
            "Your delivery partner sees your name, address and delivery notes for the active order only.",
            "Payment partners process payments; we do not sell personal data to anyone."
        )
    )
    PolicySection(
        "5. Your choices",
        listOf(
            "Edit your profile, remove saved addresses and saved payment methods at any time in Account.",
            "Turn order alerts on or off in Settings & Preferences.",
            "Request a copy or deletion of your data by contacting us."
        )
    )
    PolicySection(
        "6. Contact",
        listOf("Questions about privacy: $PRIVACY_EMAIL")
    )

    Text(
        "Prototype policy text for design review — not legal advice.",
        style = MaterialTheme.typography.labelSmall,
        color = tokens.muted,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun LegalHeading(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleMedium,
        color = QuickKartThemeTokens.colors.ink,
        fontWeight = FontWeight.Bold
    )
}

@Composable
private fun LegalParagraph(text: String) {
    Text(text, style = MaterialTheme.typography.bodyMedium, color = QuickKartThemeTokens.colors.ink)
}

@Composable
private fun AboutPoint(icon: ImageVector, title: String, body: String) {
    val tokens = QuickKartThemeTokens.colors
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Surface(color = tokens.brandYellowSoft, shape = CircleShape, modifier = Modifier.size(40.dp)) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = tokens.ink, modifier = Modifier.size(20.dp))
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall, color = tokens.ink)
            Text(body, style = MaterialTheme.typography.bodySmall, color = tokens.muted)
        }
    }
}

@Composable
private fun PolicySection(title: String, points: List<String>) {
    val tokens = QuickKartThemeTokens.colors
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(title, style = MaterialTheme.typography.titleSmall, color = tokens.ink, fontWeight = FontWeight.Bold)
        points.forEach { point ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("•", style = MaterialTheme.typography.bodyMedium, color = tokens.ink)
                Text(point, style = MaterialTheme.typography.bodyMedium, color = tokens.ink, modifier = Modifier.weight(1f))
            }
        }
    }
}
