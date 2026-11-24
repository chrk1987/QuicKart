package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.SeedData
import com.example.data.model.OrderRecord
import com.example.ui.theme.AmberGold
import com.example.ui.theme.QuickKartMetricsTokens
import com.example.ui.theme.QuickKartThemeTokens

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun OrderFeedbackBottomSheet(
    order: OrderRecord,
    onDismiss: () -> Unit,
    onSubmit: (stars: Int, tags: List<String>, comment: String) -> Unit
) {
    val tokens = QuickKartThemeTokens.colors
    val metrics = QuickKartMetricsTokens.metrics
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scrollState = rememberScrollState()

    var selectedStars by remember(order.orderId) { mutableIntStateOf(order.userRating ?: 5) }
    var selectedTags by remember(order.orderId) { mutableStateOf(order.praiseTags.toSet()) }
    var comment by remember(order.orderId) { mutableStateOf(order.feedbackComment) }
    val isEditing = order.userRating != null

    val availableTags = if (selectedStars >= 4) SeedData.praiseTags else SeedData.issueTags
    val sentimentLabel = feedbackSentimentLabel(selectedStars)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = tokens.surface,
        contentColor = tokens.ink
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .verticalScroll(scrollState)
                .padding(horizontal = metrics.screenHorizontalPadding + 2.dp)
                .padding(bottom = 24.dp)
                .testTag("order_feedback_bottom_sheet"),
            verticalArrangement = Arrangement.spacedBy(metrics.itemSpacing)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                QuickKartLogo(
                    size = metrics.badgeButtonSize,
                    cornerRadius = 10.dp
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isEditing) "Update Order Feedback" else "Rate Your Delivery",
                        style = MaterialTheme.typography.titleLarge,
                        color = tokens.ink,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${order.orderId} • ${order.totalItemCount} items • ₹${order.totalPaid}",
                        style = MaterialTheme.typography.bodySmall,
                        color = tokens.muted
                    )
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("feedback_sheet_close_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Close feedback sheet",
                        tint = tokens.ink,
                        modifier = Modifier.size(metrics.iconMedium)
                    )
                }
            }

            // Rider Attribution Strip
            Surface(
                color = tokens.surfaceElevated,
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, tokens.border),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_delivery_rider),
                        contentDescription = "Delivery rider ${order.riderName}",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Delivered by ${order.riderName}",
                            style = MaterialTheme.typography.titleSmall,
                            color = tokens.ink,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "${order.neighborhoodName} • ${order.etaMinutes} min promise",
                            style = MaterialTheme.typography.bodySmall,
                            color = tokens.muted
                        )
                    }
                    Surface(
                        color = tokens.brandYellowSoft,
                        shape = RoundedCornerShape(50)
                    ) {
                        Text(
                            text = "${order.riderRating} ★",
                            style = MaterialTheme.typography.labelSmall,
                            color = tokens.ink,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            // Star Picker + Sentiment Badge
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    (1..5).forEach { star ->
                        IconButton(
                            onClick = {
                                selectedStars = star
                                val validTags = if (star >= 4) SeedData.praiseTags else SeedData.issueTags
                                selectedTags = selectedTags.intersect(validTags.toSet())
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("feedback_sheet_star_$star")
                        ) {
                            Icon(
                                imageVector = if (star <= selectedStars) {
                                    Icons.Rounded.Star
                                } else {
                                    Icons.Rounded.StarOutline
                                },
                                contentDescription = "$star stars",
                                tint = if (star <= selectedStars) AmberGold else tokens.muted,
                                modifier = Modifier.size(metrics.iconHero)
                            )
                        }
                    }
                }

                Surface(
                    color = if (selectedStars >= 4) tokens.deliveryGreenSoft else tokens.brandYellowSoft,
                    shape = RoundedCornerShape(50)
                ) {
                    Text(
                        text = "$selectedStars / 5 • $sentimentLabel",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (selectedStars >= 4) tokens.deliveryGreenDeep else tokens.ink,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                    )
                }
            }

            HorizontalDivider(color = tokens.border.copy(alpha = 0.7f))

            // Quick Sentiment Tags
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = if (selectedStars >= 4) "What stood out?" else "What could be better?",
                    style = MaterialTheme.typography.titleSmall,
                    color = tokens.ink,
                    fontWeight = FontWeight.Bold
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    availableTags.forEach { tag ->
                        val active = tag in selectedTags
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = if (active) tokens.brandYellow else tokens.surfaceElevated,
                            border = BorderStroke(1.dp, if (active) tokens.brandYellow else tokens.border),
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .clickable {
                                    selectedTags = if (active) selectedTags - tag else selectedTags + tag
                                }
                                .testTag("feedback_sheet_tag_${tag.lowercase().replace(" ", "_")}")
                        ) {
                            Text(
                                text = tag,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (active) tokens.onBrandYellow else tokens.ink,
                                fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }

            OutlinedTextField(
                value = comment,
                onValueChange = { comment = it },
                label = { Text("Additional Feedback (Optional)") },
                placeholder = { Text("Share notes about produce freshness, packaging, or delivery...") },
                minLines = 2,
                maxLines = 4,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("feedback_sheet_comment_input")
            )

            Button(
                onClick = {
                    onSubmit(selectedStars, selectedTags.toList(), comment.trim())
                    onDismiss()
                },
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = tokens.deliveryGreenDeep,
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 50.dp)
                    .testTag("feedback_sheet_submit_button")
            ) {
                Icon(
                    imageVector = Icons.Rounded.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(metrics.iconMedium)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = if (isEditing) "Update Feedback" else "Submit Order Feedback",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

fun feedbackSentimentLabel(stars: Int): String = when (stars) {
    5 -> "Loved it!"
    4 -> "Great delivery"
    3 -> "Good, room to improve"
    2 -> "Below expectations"
    else -> "Needs urgent attention"
}
