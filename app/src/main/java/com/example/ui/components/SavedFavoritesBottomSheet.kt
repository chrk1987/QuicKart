package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.FlashOn
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.data.model.CartItem
import com.example.data.model.Product
import com.example.ui.theme.QuickKartMetricsTokens
import com.example.ui.theme.QuickKartThemeTokens

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SavedFavoritesBottomSheet(
    favoriteProducts: List<Product>,
    cartItems: List<CartItem>,
    totalCartCount: Int,
    onDismiss: () -> Unit,
    onToggleFavorite: (String) -> Unit,
    onIncrementCart: (String, String) -> Unit,
    onDecrementCart: (String, String) -> Unit,
    onQuickAddAll: () -> Unit,
    onOpenCart: () -> Unit,
    onProductClick: (Product) -> Unit
) {
    val tokens = QuickKartThemeTokens.colors
    val metrics = QuickKartMetricsTokens.metrics
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val totalBundleInr = favoriteProducts.sumOf { it.defaultVariant.priceInr }

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
                .padding(horizontal = metrics.screenHorizontalPadding + 2.dp)
                .padding(bottom = 24.dp)
                .testTag("saved_favorites_bottom_sheet"),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = tokens.alertRed.copy(alpha = 0.14f),
                    shape = CircleShape,
                    modifier = Modifier.size(metrics.badgeButtonSize)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.Favorite,
                            contentDescription = null,
                            tint = tokens.alertRed,
                            modifier = Modifier.size(metrics.iconMedium)
                        )
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Saved Favorites",
                        style = MaterialTheme.typography.titleLarge,
                        color = tokens.ink,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${favoriteProducts.size} ${if (favoriteProducts.size == 1) "item" else "items"} in your wishlist",
                        style = MaterialTheme.typography.bodySmall,
                        color = tokens.muted
                    )
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("favorites_sheet_close_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Close saved favorites",
                        tint = tokens.ink,
                        modifier = Modifier.size(metrics.iconMedium)
                    )
                }
            }

            if (favoriteProducts.isEmpty()) {
                Surface(
                    color = tokens.surfaceElevated,
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(1.dp, tokens.border),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.FavoriteBorder,
                            contentDescription = null,
                            tint = tokens.muted,
                            modifier = Modifier.size(metrics.iconHero)
                        )
                        Text(
                            text = "Your wishlist is empty",
                            style = MaterialTheme.typography.titleMedium,
                            color = tokens.ink,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Tap the heart icon on any grocery item to save staples here for 1-tap quick add.",
                            style = MaterialTheme.typography.bodySmall,
                            color = tokens.muted,
                            textAlign = TextAlign.Center
                        )
                        Button(
                            onClick = onDismiss,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = tokens.brandYellow,
                                contentColor = tokens.onBrandYellow
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .defaultMinSize(minHeight = 48.dp)
                        ) {
                            Text("Browse Essentials", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                // Quick-Add All Banner
                Surface(
                    color = tokens.brandYellowSoft,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, tokens.brandYellow),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "1-Tap Wishlist Restock",
                                style = MaterialTheme.typography.labelLarge,
                                color = tokens.ink,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${favoriteProducts.size} favorites • ₹$totalBundleInr bundle",
                                style = MaterialTheme.typography.bodySmall,
                                color = tokens.muted
                            )
                        }
                        Button(
                            onClick = onQuickAddAll,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = tokens.deliveryGreenDeep,
                                contentColor = Color.White
                            ),
                            modifier = Modifier
                                .defaultMinSize(minHeight = 44.dp)
                                .testTag("quick_add_all_favorites_button")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.FlashOn,
                                contentDescription = null,
                                modifier = Modifier.size(metrics.iconSmall)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = "Quick-Add All",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Favorites List
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 380.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(favoriteProducts, key = { it.id }) { product ->
                        val variant = product.defaultVariant
                        val qtyInCart = cartItems.find {
                            it.product.id == product.id && it.variant.id == variant.id
                        }?.quantity ?: 0

                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = tokens.surfaceElevated,
                            border = BorderStroke(1.dp, tokens.border),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { onProductClick(product) }
                                .testTag("wishlist_item_${product.id}")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                ProductArtwork(
                                    artworkType = product.artworkType,
                                    accentColor = product.accentColor,
                                    modifier = Modifier.size(metrics.avatarSize)
                                )
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Text(
                                        text = product.name,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = tokens.ink,
                                        maxLines = 2
                                    )
                                    Text(
                                        text = "${variant.label} • ${product.brand}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = tokens.muted
                                    )
                                    FlowRow(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        Text(
                                            text = "₹${variant.priceInr}",
                                            style = MaterialTheme.typography.labelLarge,
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

                                IconButton(
                                    onClick = { onToggleFavorite(product.id) },
                                    modifier = Modifier
                                        .size(48.dp)
                                        .testTag("wishlist_remove_${product.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Favorite,
                                        contentDescription = "Remove ${product.name} from favorites",
                                        tint = tokens.alertRed,
                                        modifier = Modifier.size(metrics.iconMedium)
                                    )
                                }

                                if (qtyInCart == 0) {
                                    OutlinedButton(
                                        onClick = { onIncrementCart(product.id, variant.id) },
                                        shape = RoundedCornerShape(10.dp),
                                        border = BorderStroke(1.5.dp, tokens.brandYellow),
                                        modifier = Modifier
                                            .defaultMinSize(minHeight = 40.dp)
                                            .testTag("wishlist_add_${product.id}")
                                    ) {
                                        Text(
                                            text = "ADD",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = tokens.ink,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    }
                                } else {
                                    Surface(
                                        color = tokens.deliveryGreenDeep,
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            IconButton(
                                                onClick = { onDecrementCart(product.id, variant.id) },
                                                modifier = Modifier
                                                    .size(34.dp)
                                                    .testTag("wishlist_dec_${product.id}")
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Rounded.Remove,
                                                    contentDescription = "Decrease ${product.name}",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(metrics.iconSmall)
                                                )
                                            }
                                            Text(
                                                text = qtyInCart.toString(),
                                                style = MaterialTheme.typography.labelLarge,
                                                color = Color.White,
                                                modifier = Modifier.padding(horizontal = 4.dp)
                                            )
                                            IconButton(
                                                onClick = { onIncrementCart(product.id, variant.id) },
                                                modifier = Modifier
                                                    .size(34.dp)
                                                    .testTag("wishlist_inc_${product.id}")
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Rounded.Add,
                                                    contentDescription = "Increase ${product.name}",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(metrics.iconSmall)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (totalCartCount > 0) {
                    HorizontalDivider(color = tokens.border)
                    Button(
                        onClick = {
                            onDismiss()
                            onOpenCart()
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = tokens.deliveryGreenDeep,
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 48.dp)
                            .testTag("wishlist_view_cart_button")
                    ) {
                        Text(
                            text = "View Cart ($totalCartCount)",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(metrics.iconSmall)
                        )
                    }
                }
            }
        }
    }
}
