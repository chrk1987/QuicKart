package com.example.rider.ui.screens

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
import androidx.compose.material.icons.rounded.Badge
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.DeliveryDining
import androidx.compose.material.icons.rounded.DirectionsBike
import androidx.compose.material.icons.rounded.ElectricScooter
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.rounded.Storefront
import androidx.compose.material.icons.rounded.TwoWheeler
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
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
import com.example.rider.data.model.RiderHub
import com.example.rider.data.model.RiderVehicleType
import com.example.rider.ui.theme.RiderMetricsTokens
import com.example.rider.ui.theme.RiderThemeTokens

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RiderAuthScreen(
    hubs: List<RiderHub>,
    initialName: String,
    initialPhone: String,
    initialFleetId: String,
    initialVehicle: RiderVehicleType,
    initialHubId: String,
    onStartShift: (name: String, phone: String, fleetId: String, vehicle: RiderVehicleType, hubId: String) -> Unit
) {
    val tokens = RiderThemeTokens.colors
    val metrics = RiderMetricsTokens.metrics

    var name by remember { mutableStateOf(initialName) }
    var phone by remember { mutableStateOf(initialPhone) }
    var fleetId by remember { mutableStateOf(initialFleetId) }
    var selectedVehicle by remember { mutableStateOf(initialVehicle) }
    var selectedHubId by remember { mutableStateOf(initialHubId) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

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
                .padding(horizontal = metrics.screenHorizontalPadding, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = tokens.deliveryGreenDeep,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(tokens.brandYellow, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.DeliveryDining,
                                contentDescription = "QuickKart Rider",
                                tint = tokens.onBrandYellow,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "QuickKart Fleet",
                                style = MaterialTheme.typography.headlineSmall,
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = "10-Min Hyperlocal Delivery Partner",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White.copy(alpha = 0.88f)
                            )
                        }
                    }

                    Surface(
                        color = Color.White.copy(alpha = 0.14f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Earn ₹25 base + distance + peak surge + 100% tips with instant UPI payout.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = tokens.surface),
                border = BorderStroke(1.dp, tokens.border),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Partner Shift Sign-In",
                        style = MaterialTheme.typography.titleLarge,
                        color = tokens.ink
                    )

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it; errorMessage = null },
                        label = { Text("Partner Full Name") },
                        leadingIcon = { Icon(Icons.Rounded.Person, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("rider_name_input")
                    )

                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it; errorMessage = null },
                        label = { Text("Registered Mobile") },
                        leadingIcon = { Icon(Icons.Rounded.Phone, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("rider_phone_input")
                    )

                    OutlinedTextField(
                        value = fleetId,
                        onValueChange = { fleetId = it; errorMessage = null },
                        label = { Text("Fleet Badge ID") },
                        leadingIcon = { Icon(Icons.Rounded.Badge, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("rider_fleet_input")
                    )

                    Text(
                        text = "Delivery Vehicle",
                        style = MaterialTheme.typography.titleSmall,
                        color = tokens.ink
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        RiderVehicleType.entries.forEach { vehicle ->
                            val selected = selectedVehicle == vehicle
                            val icon = when (vehicle) {
                                RiderVehicleType.ELECTRIC_SCOOTER -> Icons.Rounded.ElectricScooter
                                RiderVehicleType.E_CYCLE -> Icons.Rounded.DirectionsBike
                                RiderVehicleType.MOTORCYCLE -> Icons.Rounded.TwoWheeler
                            }
                            FilterChip(
                                selected = selected,
                                onClick = { selectedVehicle = vehicle },
                                label = { Text(vehicle.label) },
                                leadingIcon = {
                                    Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = tokens.deliveryGreenSoft,
                                    selectedLabelColor = tokens.deliveryGreenDeep,
                                    selectedLeadingIconColor = tokens.deliveryGreenDeep
                                ),
                                modifier = Modifier.testTag("vehicle_chip_${vehicle.name.lowercase()}")
                            )
                        }
                    }

                    Text(
                        text = "Reporting Dark Store Pod",
                        style = MaterialTheme.typography.titleSmall,
                        color = tokens.ink
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        hubs.forEach { hub ->
                            val isSelected = selectedHubId == hub.id
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = if (isSelected) tokens.deliveryGreenSoft else tokens.surfaceElevated,
                                border = BorderStroke(
                                    1.2.dp,
                                    if (isSelected) tokens.deliveryGreenDeep else tokens.border
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedHubId = hub.id }
                                    .testTag("hub_option_${hub.id}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Storefront,
                                        contentDescription = null,
                                        tint = if (isSelected) tokens.deliveryGreenDeep else tokens.muted
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "${hub.podName} • ${hub.neighborhood}",
                                            style = MaterialTheme.typography.titleSmall,
                                            color = tokens.ink,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "${hub.city} • ${hub.demandLevel}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = if (isSelected) tokens.deliveryGreenDeep else tokens.muted
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (errorMessage != null) {
                        Text(
                            text = errorMessage!!,
                            style = MaterialTheme.typography.bodySmall,
                            color = tokens.alertRed,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Button(
                        onClick = {
                            if (name.isBlank() || phone.trim().length < 7 || fleetId.isBlank()) {
                                errorMessage = "Enter your name, mobile number, and fleet ID."
                                return@Button
                            }
                            onStartShift(
                                name.trim(),
                                phone.trim(),
                                fleetId.trim(),
                                selectedVehicle,
                                selectedHubId
                            )
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = tokens.deliveryGreenDeep,
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("rider_start_shift_button")
                    ) {
                        Icon(Icons.Rounded.Bolt, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text("Go Online & Start Shift", style = MaterialTheme.typography.labelLarge)
                    }

                    OutlinedButton(
                        onClick = {
                            onStartShift(
                                "Arjun Kumar",
                                "+91 98452 31090",
                                "QKR-BLR-248",
                                selectedVehicle,
                                selectedHubId
                            )
                        },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("rider_demo_login_button")
                    ) {
                        Text("Quick Fill Demo Partner", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
    }
}
