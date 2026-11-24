package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BakeryDining
import androidx.compose.material.icons.rounded.Cookie
import androidx.compose.material.icons.rounded.Eco
import androidx.compose.material.icons.rounded.Egg
import androidx.compose.material.icons.rounded.LocalCafe
import androidx.compose.material.icons.rounded.ShoppingBasket
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.data.model.ProductCategory
import com.example.ui.QuickKartUiState
import com.example.ui.theme.QuickKartMetricsTokens
import com.example.ui.theme.QuickKartThemeTokens
import com.example.util.itemCountLabel

/** Category icon used on the Categories tab (brand: light-yellow tile, black icon). */
fun ProductCategory.icon(): ImageVector = when (this) {
    ProductCategory.ALL -> Icons.Rounded.ShoppingBasket
    ProductCategory.DAIRY_EGGS -> Icons.Rounded.Egg
    ProductCategory.FRESH_PRODUCE -> Icons.Rounded.Eco
    ProductCategory.BAKERY_BREAD -> Icons.Rounded.BakeryDining
    ProductCategory.COLD_BREW_JUICES -> Icons.Rounded.LocalCafe
    ProductCategory.SNACKS_PANTRY -> Icons.Rounded.Cookie
}

/**
 * Categories tab: browse the catalogue by category. Tapping a tile opens Home filtered
 * to that category.
 */
@Composable
fun CategoriesScreen(
    uiState: QuickKartUiState,
    onSelectCategory: (ProductCategory) -> Unit
) {
    val tokens = QuickKartThemeTokens.colors
    val metrics = QuickKartMetricsTokens.metrics
    val columns = if (metrics.gridColumns >= 3) 3 else 2

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(tokens.background),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(columns),
            contentPadding = PaddingValues(metrics.screenHorizontalPadding),
            horizontalArrangement = Arrangement.spacedBy(metrics.itemSpacing),
            verticalArrangement = Arrangement.spacedBy(metrics.itemSpacing),
            modifier = Modifier
                .widthIn(max = 760.dp)
                .fillMaxSize()
                .testTag("categories_screen")
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        "Shop by category",
                        style = MaterialTheme.typography.titleLarge,
                        color = tokens.ink,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Everything delivered in ${uiState.selectedNeighborhood?.etaMinutes ?: 8} minutes",
                        style = MaterialTheme.typography.bodySmall,
                        color = tokens.muted
                    )
                }
            }
            items(ProductCategory.entries, key = { it.name }) { category ->
                val count = if (category == ProductCategory.ALL) {
                    uiState.products.size
                } else {
                    uiState.products.count { it.category == category }
                }
                CategoryTile(
                    category = category,
                    countLabel = itemCountLabel(count),
                    onClick = { onSelectCategory(category) }
                )
            }
        }
    }
}

@Composable
private fun CategoryTile(category: ProductCategory, countLabel: String, onClick: () -> Unit) {
    val tokens = QuickKartThemeTokens.colors
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = tokens.surface,
        border = BorderStroke(1.dp, tokens.border),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .testTag("category_tile_${category.name.lowercase()}")
    ) {
        Column(
            modifier = Modifier.padding(vertical = 18.dp, horizontal = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(color = tokens.brandYellowSoft, shape = CircleShape, modifier = Modifier.size(56.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = category.icon(),
                        contentDescription = null,
                        tint = tokens.ink,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
            Text(
                text = category.label,
                style = MaterialTheme.typography.titleSmall,
                color = tokens.ink,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 2
            )
            Text(countLabel, style = MaterialTheme.typography.labelSmall, color = tokens.muted)
        }
    }
}
