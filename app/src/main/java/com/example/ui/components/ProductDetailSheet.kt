package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.data.model.CartItem
import com.example.data.model.Product
import com.example.ui.theme.AmberGold
import com.example.ui.theme.DeviceSizeClass
import com.example.ui.theme.QuickKartMetricsTokens
import com.example.ui.theme.QuickKartThemeTokens

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ProductDetailBottomSheet(
    product: Product,
    initialVariantId: String,
    isFavorite: Boolean,
    cartItems: List<CartItem>,
    onDismiss: () -> Unit,
    onToggleFavorite: () -> Unit,
    onIncrementCart: (String, String) -> Unit,
    onDecrementCart: (String, String) -> Unit
) {
    val tokens = QuickKartThemeTokens.colors
    val metrics = QuickKartMetricsTokens.metrics
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedVariantId by remember(product.id) { mutableStateOf(initialVariantId) }
    val selectedVariant = product.variants.find { it.id == selectedVariantId } ?: product.defaultVariant
    val qtyInCart = cartItems.find {
        it.product.id == product.id && it.variant.id == selectedVariant.id
    }?.quantity ?: 0
    val heroArtworkHeight = when (metrics.sizeClass) {
        DeviceSizeClass.SMALL_COMPACT -> 148.dp
        DeviceSizeClass.COMPACT -> 175.dp
        DeviceSizeClass.MEDIUM -> 200.dp
        DeviceSizeClass.EXPANDED -> 220.dp
    }

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
                .padding(horizontal = metrics.screenHorizontalPadding + 4.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(metrics.itemSpacing)
        ) {
            ProductArtwork(
                artworkType = product.artworkType,
                accentColor = product.accentColor,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(heroArtworkHeight)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            color = tokens.deliveryGreenSoft,
                            shape = RoundedCornerShape(50)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Timer,
                                    contentDescription = null,
                                    tint = tokens.deliveryGreenDeep,
                                    modifier = Modifier.size(metrics.iconSmall)
                                )
                                Text(
                                    text = "${product.deliveryMinutes} MINS",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = tokens.deliveryGreenDeep
                                )
                            }
                        }
                        Text(
                            text = product.brand,
                            style = MaterialTheme.typography.labelMedium,
                            color = tokens.muted
                        )
                    }

                    Text(
                        text = product.name,
                        style = MaterialTheme.typography.headlineSmall,
                        color = tokens.ink
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Star,
                            contentDescription = null,
                            tint = AmberGold,
                            modifier = Modifier.size(metrics.iconSmall)
                        )
                        Text(
                            text = "${product.rating} (${product.ratingCount} ratings)",
                            style = MaterialTheme.typography.bodySmall,
                            color = tokens.muted
                        )
                    }
                }

                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("sheet_favorite_button")
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                        contentDescription = "Toggle favorite",
                        tint = if (isFavorite) tokens.alertRed else tokens.muted,
                        modifier = Modifier.size(metrics.iconLarge)
                    )
                }
            }

            Text(
                text = product.description,
                style = MaterialTheme.typography.bodyMedium,
                color = tokens.muted
            )

            if (product.highlights.isNotEmpty()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    product.highlights.forEach { tag ->
                        Surface(
                            color = tokens.surfaceElevated,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, tokens.border)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.CheckCircle,
                                    contentDescription = null,
                                    tint = tokens.deliveryGreen,
                                    modifier = Modifier.size(metrics.iconSmall)
                                )
                                Text(
                                    text = tag,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = tokens.ink
                                )
                            }
                        }
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Select Pack Size",
                    style = MaterialTheme.typography.titleSmall,
                    color = tokens.ink
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    product.variants.forEach { variant ->
                        val active = variant.id == selectedVariant.id
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (active) tokens.brandYellowSoft else tokens.surfaceElevated,
                            border = BorderStroke(
                                width = if (active) 1.5.dp else 1.dp,
                                color = if (active) tokens.brandYellow else tokens.border
                            ),
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { selectedVariantId = variant.id }
                                .testTag("sheet_variant_${variant.id}")
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(
                                    text = variant.label,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = tokens.ink
                                )
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "₹${variant.priceInr}",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = tokens.ink,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (variant.mrpInr > variant.priceInr) {
                                        Text(
                                            text = "₹${variant.mrpInr}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = tokens.muted,
                                            textDecoration = TextDecoration.LineThrough
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            HorizontalDivider(color = tokens.border)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    FlowRow(
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "₹${selectedVariant.priceInr}",
                            style = MaterialTheme.typography.headlineSmall,
                            color = tokens.ink
                        )
                        if (selectedVariant.discountPercent > 0) {
                            Surface(
                                color = tokens.deliveryGreenSoft,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "${selectedVariant.discountPercent}% OFF",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = tokens.deliveryGreenDeep,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    Text(
                        text = "Inclusive of all taxes",
                        style = MaterialTheme.typography.bodySmall,
                        color = tokens.muted
                    )
                }

                if (qtyInCart == 0) {
                    Button(
                        onClick = { onIncrementCart(product.id, selectedVariant.id) },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = tokens.deliveryGreenDeep,
                            contentColor = androidx.compose.ui.graphics.Color.White
                        ),
                        modifier = Modifier
                            .defaultMinSize(minHeight = 50.dp)
                            .testTag("sheet_add_to_cart")
                    ) {
                        Text(
                            text = "Add to Cart • ₹${selectedVariant.priceInr}",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                } else {
                    Surface(
                        color = tokens.deliveryGreenDeep,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.defaultMinSize(minHeight = 50.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 6.dp)
                        ) {
                            IconButton(
                                onClick = { onDecrementCart(product.id, selectedVariant.id) },
                                modifier = Modifier.size(44.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Remove,
                                    contentDescription = "Decrease quantity",
                                    tint = androidx.compose.ui.graphics.Color.White,
                                    modifier = Modifier.size(metrics.iconMedium)
                                )
                            }
                            Text(
                                text = qtyInCart.toString(),
                                style = MaterialTheme.typography.titleMedium,
                                color = androidx.compose.ui.graphics.Color.White,
                                modifier = Modifier.padding(horizontal = 10.dp)
                            )
                            IconButton(
                                onClick = { onIncrementCart(product.id, selectedVariant.id) },
                                modifier = Modifier.size(44.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Add,
                                    contentDescription = "Increase quantity",
                                    tint = androidx.compose.ui.graphics.Color.White,
                                    modifier = Modifier.size(metrics.iconMedium)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
