package com.example.rider.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.BatteryChargingFull
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.DeliveryDining
import androidx.compose.material.icons.rounded.HeadsetMic
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material.icons.rounded.ZoomIn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.rider.data.model.RiderVehicleType
import com.example.rider.ui.RiderUiState
import com.example.rider.ui.theme.DEFAULT_SCREEN_ZOOM_PERCENT
import com.example.rider.ui.theme.JetBrainsMonoFontFamily
import com.example.rider.ui.theme.MAX_SCREEN_ZOOM_PERCENT
import com.example.rider.ui.theme.MIN_SCREEN_ZOOM_PERCENT
import com.example.rider.ui.theme.RiderMetricsTokens
import com.example.rider.ui.theme.RiderThemeTokens
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RiderProfileScreen(
    state: RiderUiState,
    onSelectVehicle: (RiderVehicleType) -> Unit,
    onUpdateChecklist: (helmet: Boolean, bag: Boolean, battery: Boolean) -> Unit,
    onToggleDarkTheme: (Boolean) -> Unit,
    onToggleNotifications: (Boolean) -> Unit,
    onSetScreenZoom: (Int) -> Unit,
    onLogout: () -> Unit,
    onMessage: (String) -> Unit
) {
    val tokens = RiderThemeTokens.colors
    val metrics = RiderMetricsTokens.metrics
    val prefs = state.prefs
    val context = LocalContext.current

    val notifPermLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            onToggleNotifications(true)
            onMessage("Dispatch notifications enabled")
        } else {
            onToggleNotifications(false)
            onMessage("Notification permission denied")
        }
    }

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
            // 1. Fleet Partner Badge Card
            item {
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = tokens.deliveryGreenDeep,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(58.dp)
                                .background(tokens.brandYellow, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.DeliveryDining,
                                contentDescription = "Partner badge",
                                tint = tokens.onBrandYellow,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = prefs.riderName,
                                style = MaterialTheme.typography.titleLarge,
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = "${prefs.fleetId} • ${prefs.riderPhone}",
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = JetBrainsMonoFontFamily,
                                color = Color.White.copy(alpha = 0.88f)
                            )
                            Text(
                                text = "Home Pod: ${state.currentHub.podName}",
                                style = MaterialTheme.typography.labelMedium,
                                color = tokens.brandYellow
                            )
                        }
                    }
                }
            }

            // 2. Pre-Shift Safety & Gear Checklist
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
                            text = "Shift Safety Checklist",
                            style = MaterialTheme.typography.titleMedium,
                            color = tokens.ink,
                            fontWeight = FontWeight.Bold
                        )

                        ChecklistRow(
                            icon = Icons.Rounded.Shield,
                            label = "ISI Helmet & Reflective Jacket",
                            checked = prefs.checklistHelmet,
                            onCheckedChange = {
                                onUpdateChecklist(it, prefs.checklistThermalBag, prefs.checklistBatteryOk)
                            }
                        )
                        ChecklistRow(
                            icon = Icons.Rounded.ShoppingBag,
                            label = "Insulated QuickKart Thermal Bag Clean",
                            checked = prefs.checklistThermalBag,
                            onCheckedChange = {
                                onUpdateChecklist(prefs.checklistHelmet, it, prefs.checklistBatteryOk)
                            }
                        )
                        ChecklistRow(
                            icon = Icons.Rounded.BatteryChargingFull,
                            label = "Vehicle Charge / Fuel & Phone > 50%",
                            checked = prefs.checklistBatteryOk,
                            onCheckedChange = {
                                onUpdateChecklist(prefs.checklistHelmet, prefs.checklistThermalBag, it)
                            }
                        )
                    }
                }
            }

            // 3. Vehicle Selector
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
                            text = "Active Vehicle",
                            style = MaterialTheme.typography.titleMedium,
                            color = tokens.ink,
                            fontWeight = FontWeight.Bold
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            RiderVehicleType.entries.forEach { vehicle ->
                                FilterChip(
                                    selected = prefs.vehicleType == vehicle,
                                    onClick = { onSelectVehicle(vehicle) },
                                    label = { Text(vehicle.label) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = tokens.deliveryGreenSoft,
                                        selectedLabelColor = tokens.deliveryGreenDeep
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // 4. Display, Notifications & Screen Zoom Settings
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = tokens.surface),
                    border = BorderStroke(1.dp, tokens.border),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "App Preferences",
                            style = MaterialTheme.typography.titleMedium,
                            color = tokens.ink,
                            fontWeight = FontWeight.Bold
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Rounded.DarkMode, contentDescription = null, tint = tokens.ink)
                            Spacer(Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Night Shift Dark Mode", style = MaterialTheme.typography.titleSmall, color = tokens.ink)
                                Text("Reduce glare during evening deliveries", style = MaterialTheme.typography.bodySmall, color = tokens.muted)
                            }
                            Switch(
                                checked = prefs.isDarkTheme,
                                onCheckedChange = onToggleDarkTheme,
                                modifier = Modifier.testTag("rider_dark_mode_switch")
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Rounded.NotificationsActive, contentDescription = null, tint = tokens.ink)
                            Spacer(Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Dispatch & Lock-Screen Alerts", style = MaterialTheme.typography.titleSmall, color = tokens.ink)
                                Text("Sound & ongoing trip progress banner", style = MaterialTheme.typography.bodySmall, color = tokens.muted)
                            }
                            Switch(
                                checked = prefs.notificationsEnabled,
                                onCheckedChange = { wantEnable ->
                                    if (!wantEnable) {
                                        onToggleNotifications(false)
                                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                                        ContextCompat.checkSelfPermission(
                                            context,
                                            Manifest.permission.POST_NOTIFICATIONS
                                        ) != PackageManager.PERMISSION_GRANTED
                                    ) {
                                        notifPermLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    } else {
                                        onToggleNotifications(true)
                                    }
                                },
                                modifier = Modifier.testTag("rider_notifications_switch")
                            )
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Rounded.ZoomIn, contentDescription = null, tint = tokens.ink)
                                    Text(
                                        text = "Screen Zoom (${prefs.screenZoomPercent}%)",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = tokens.ink
                                    )
                                }
                                TextButton(
                                    onClick = { onSetScreenZoom(DEFAULT_SCREEN_ZOOM_PERCENT) },
                                    enabled = prefs.screenZoomPercent != DEFAULT_SCREEN_ZOOM_PERCENT
                                ) {
                                    Text("Reset 100%")
                                }
                            }
                            Slider(
                                value = prefs.screenZoomPercent.toFloat(),
                                onValueChange = { raw ->
                                    val snapped = ((raw / 5f).roundToInt() * 5)
                                        .coerceIn(MIN_SCREEN_ZOOM_PERCENT, MAX_SCREEN_ZOOM_PERCENT)
                                    onSetScreenZoom(snapped)
                                },
                                valueRange = MIN_SCREEN_ZOOM_PERCENT.toFloat()..MAX_SCREEN_ZOOM_PERCENT.toFloat(),
                                steps = 9,
                                modifier = Modifier.testTag("rider_screen_zoom_slider")
                            )
                        }
                    }
                }
            }

            // 5. Hub Dispatch Helpline & Sign Out
            item {
                OutlinedButton(
                    onClick = {
                        val dial = Intent(Intent.ACTION_DIAL, Uri.parse("tel:+918040009911"))
                        runCatching { context.startActivity(dial) }
                            .onFailure { onMessage("Pod Captain Helpline: +91 80400 09911") }
                    },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("rider_hub_helpline_button")
                ) {
                    Icon(Icons.Rounded.HeadsetMic, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Call Pod Captain Helpline")
                }
            }

            item {
                Button(
                    onClick = onLogout,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = tokens.alertRed.copy(alpha = 0.14f),
                        contentColor = tokens.alertRed
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("rider_logout_button")
                ) {
                    Icon(Icons.AutoMirrored.Rounded.Logout, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("End Shift & Sign Out", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

@Composable
private fun ChecklistRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val tokens = RiderThemeTokens.colors
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (checked) tokens.deliveryGreenSoft else tokens.surfaceElevated,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (checked) tokens.deliveryGreenDeep else tokens.muted,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = tokens.ink,
                modifier = Modifier.weight(1f)
            )
            Checkbox(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = CheckboxDefaults.colors(checkedColor = tokens.deliveryGreenDeep)
            )
        }
    }
}
