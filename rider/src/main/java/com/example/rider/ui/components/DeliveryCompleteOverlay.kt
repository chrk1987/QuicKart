package com.example.rider.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.rider.ui.RiderCelebration
import com.example.rider.ui.theme.RiderThemeTokens

@Composable
fun DeliveryCompleteOverlay(
    celebration: RiderCelebration?,
    onFindNextOrder: () -> Unit,
    onDismiss: () -> Unit
) {
    val tokens = RiderThemeTokens.colors

    AnimatedVisibility(
        visible = celebration != null,
        enter = fadeIn() + scaleIn(initialScale = 0.92f),
        exit = fadeOut()
    ) {
        if (celebration != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.56f))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    shape = RoundedCornerShape(26.dp),
                    colors = CardDefaults.cardColors(containerColor = tokens.surface),
                    border = BorderStroke(1.5.dp, tokens.deliveryGreenDeep.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .widthIn(max = 420.dp)
                        .fillMaxWidth()
                        .testTag("delivery_complete_overlay")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .background(tokens.deliveryGreenSoft, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.CheckCircle,
                                contentDescription = "Delivery completed",
                                tint = tokens.deliveryGreenDeep,
                                modifier = Modifier.size(44.dp)
                            )
                        }

                        Text(
                            text = "Drop Completed!",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = tokens.ink
                        )

                        Text(
                            text = "${celebration.orderId} delivered to ${celebration.customerName} in ${celebration.elapsedMinutes} min",
                            style = MaterialTheme.typography.bodyMedium,
                            color = tokens.muted,
                            textAlign = TextAlign.Center
                        )

                        Surface(
                            color = tokens.brandYellowSoft,
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, tokens.brandYellow)
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 24.dp, vertical = 14.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "EARNED ON THIS TRIP",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = tokens.ink,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "+₹${celebration.earnedInr}",
                                    style = MaterialTheme.typography.displaySmall,
                                    color = tokens.deliveryGreenDeep,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }

                        Spacer(Modifier.height(4.dp))

                        Button(
                            onClick = onFindNextOrder,
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = tokens.deliveryGreenDeep,
                                contentColor = Color.White
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("celebration_next_order_button")
                        ) {
                            Icon(Icons.Rounded.Bolt, contentDescription = null)
                            Spacer(Modifier.size(8.dp))
                            Text("Ready for Next Drop", style = MaterialTheme.typography.labelLarge)
                        }

                        Button(
                            onClick = onDismiss,
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = tokens.surfaceElevated,
                                contentColor = tokens.ink
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("celebration_dismiss_button")
                        ) {
                            Text("View Earnings", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }
        }
    }
}
