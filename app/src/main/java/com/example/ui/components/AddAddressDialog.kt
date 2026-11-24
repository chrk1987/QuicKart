package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.model.Neighborhood
import com.example.ui.theme.QuickKartThemeTokens

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddAddressDialog(
    neighborhoods: List<Neighborhood>,
    initialNeighborhoodId: String,
    onDismiss: () -> Unit,
    onSave: (label: String, fullAddress: String, landmark: String, neighborhoodId: String) -> Unit
) {
    val tokens = QuickKartThemeTokens.colors
    var selectedLabel by remember { mutableStateOf("Home") }
    var fullAddress by remember { mutableStateOf("") }
    var landmark by remember { mutableStateOf("") }
    var neighborhoodId by remember { mutableStateOf(initialNeighborhoodId) }
    var showError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = tokens.surface,
        title = {
            Text(
                text = "Add Delivery Address",
                style = MaterialTheme.typography.titleLarge,
                color = tokens.ink
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Save As",
                    style = MaterialTheme.typography.labelMedium,
                    color = tokens.muted
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Home", "Work", "Other").forEach { tag ->
                        val active = selectedLabel == tag
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = if (active) tokens.brandYellow else tokens.surfaceElevated,
                            border = BorderStroke(1.dp, if (active) tokens.brandYellow else tokens.border),
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .clickable { selectedLabel = tag }
                        ) {
                            Text(
                                text = tag,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (active) tokens.onBrandYellow else tokens.ink,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                            )
                        }
                    }
                }

                Text(
                    text = "Neighborhood",
                    style = MaterialTheme.typography.labelMedium,
                    color = tokens.muted
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(neighborhoods, key = { it.id }) { n ->
                        val active = neighborhoodId == n.id
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (active) tokens.deliveryGreenSoft else tokens.surfaceElevated,
                            border = BorderStroke(1.dp, if (active) tokens.deliveryGreen else tokens.border),
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { neighborhoodId = n.id }
                        ) {
                            Text(
                                text = "${n.name} (${n.etaMinutes}m)",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (active) tokens.deliveryGreenDeep else tokens.ink,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = fullAddress,
                    onValueChange = {
                        fullAddress = it
                        showError = false
                    },
                    label = { Text("Flat / Building / Street") },
                    placeholder = { Text("e.g. Apt 304, Maple Heights, 100ft Rd") },
                    minLines = 2,
                    maxLines = 3,
                    isError = showError,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_address_street_input")
                )

                OutlinedTextField(
                    value = landmark,
                    onValueChange = { landmark = it },
                    label = { Text("Landmark (Optional)") },
                    placeholder = { Text("e.g. Near Metro Station") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                if (showError) {
                    Text(
                        text = "Please enter your street address.",
                        style = MaterialTheme.typography.bodySmall,
                        color = tokens.alertRed
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (fullAddress.isBlank()) {
                        showError = true
                    } else {
                        onSave(selectedLabel, fullAddress, landmark, neighborhoodId)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = tokens.brandYellow,
                    contentColor = tokens.onBrandYellow
                ),
                modifier = Modifier.testTag("save_address_confirm_button")
            ) {
                Text("Save Address")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = tokens.muted)
            }
        }
    )
}
