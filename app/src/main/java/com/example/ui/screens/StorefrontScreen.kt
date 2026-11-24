package com.example.ui.screens



import androidx.compose.ui.unit.sp

import androidx.compose.foundation.border

import androidx.compose.foundation.layout.offset

import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.KeyboardActions
import com.example.util.itemCountLabel
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.R
import com.example.data.model.Product
import com.example.data.model.ProductCategory
import com.example.ui.QuickKartUiState
import com.example.ui.components.ProductArtwork
import com.example.ui.components.ProductDetailBottomSheet
import com.example.ui.components.QuickKartLogo
import com.example.ui.components.SavedFavoritesBottomSheet
import com.example.ui.theme.QuickKartMetricsTokens
import com.example.ui.theme.QuickKartThemeTokens
import com.example.util.VoiceSearchHelper

@Composable
fun StorefrontScreen(
    uiState: QuickKartUiState,
    onSelectNeighborhood: (String) -> Unit,
    onSelectCategory: (ProductCategory) -> Unit,
    onToggleFavoritesFilter: () -> Unit,
    onSetFavoritesOnly: (Boolean) -> Unit,
    onQuickAddAllFavorites: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onVoiceListeningChange: (Boolean) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onIncrementCart: (String, String) -> Unit,
    onDecrementCart: (String, String) -> Unit,
    onOpenCart: () -> Unit,
    onShowMessage: (String) -> Unit,
    onSearchSubmitted: (String) -> Unit = {},
    onClearRecentSearches: () -> Unit = {},
    onMarkInboxSeen: () -> Unit = {}
) {
    val tokens = QuickKartThemeTokens.colors
    val metrics = QuickKartMetricsTokens.metrics
    val context = LocalContext.current
    var showNeighborhoodMenu by remember { mutableStateOf(false) }
    var searchFocused by remember { mutableStateOf(false) }
    // True while the search box has focus and is empty: shows recent / trending suggestions.
    var showSearchSuggestions by remember { mutableStateOf(false) }
    var showInbox by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    var activeDetailProduct by remember { mutableStateOf<Product?>(null) }
    var showWishlistSheet by remember { mutableStateOf(false) }
    val selectedVariantByProduct = remember { mutableStateMapOf<String, String>() }

    val voiceHelper = remember(context) {
        VoiceSearchHelper(
            context = context,
            onListeningChanged = onVoiceListeningChange,
            onPartialResult = { partial -> onSearchQueryChange(partial) },
            onFinalResult = { finalText ->
                onSearchQueryChange(finalText)
                searchFocused = true
            },
            onErrorMessage = { msg -> onShowMessage(msg) }
        )
    }

    DisposableEffect(voiceHelper) {
        onDispose { voiceHelper.stopListening() }
    }

    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            voiceHelper.startListening()
        } else {
            onShowMessage("Microphone permission denied. You can still type to search.")
        }
    }

    val activeNeighborhood = uiState.selectedNeighborhood ?: uiState.neighborhoods.firstOrNull()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(tokens.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Sticky Top Bar
            Surface(
                color = tokens.surface,
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = metrics.screenHorizontalPadding, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            QuickKartLogo(
                                size = metrics.badgeButtonSize,
                                cornerRadius = 10.dp
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "QuickKart",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = tokens.ink,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Box {
                                    Row(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable { showNeighborhoodMenu = true }
                                            .padding(vertical = 2.dp)
                                            .testTag("neighborhood_dropdown_trigger"),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            // ETA is shown in the "N MIN" pill next to the header icons.
                                            text = activeNeighborhood?.name ?: "Select Area",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = tokens.deliveryGreenDeep,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f, fill = false)
                                        )
                                        Icon(
                                            imageVector = Icons.Rounded.KeyboardArrowDown,
                                            contentDescription = "Change neighborhood",
                                            tint = tokens.ink,
                                            modifier = Modifier.size(metrics.iconSmall)
                                        )
                                    }

                                    DropdownMenu(
                                        expanded = showNeighborhoodMenu,
                                        onDismissRequest = { showNeighborhoodMenu = false }
                                    ) {
                                        uiState.neighborhoods.forEach { n ->
                                            DropdownMenuItem(
                                                text = {
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Column {
                                                            Text(n.name, fontWeight = FontWeight.Bold)
                                                            Text(
                                                                n.city,
                                                                style = MaterialTheme.typography.bodySmall,
                                                                color = tokens.muted
                                                            )
                                                        }
                                                        Spacer(Modifier.width(12.dp))
                                                        Surface(
                                                            color = tokens.deliveryGreenSoft,
                                                            shape = RoundedCornerShape(50)
                                                        ) {
                                                            Text(
                                                                text = "${n.etaMinutes}m",
                                                                style = MaterialTheme.typography.labelSmall,
                                                                color = tokens.deliveryGreenDeep,
                                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                                            )
                                                        }
                                                    }
                                                },
                                                onClick = {
                                                    onSelectNeighborhood(n.id)
                                                    showNeighborhoodMenu = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            // Icons sit flush together as one cluster; small gap before the ETA pill.
                            horizontalArrangement = Arrangement.spacedBy(0.dp)
                        ) {
                            val unread = unreadInboxCount(uiState)
                            HeaderIconWithBadge(
                                icon = Icons.Rounded.Notifications,
                                contentDescription = "Notifications, $unread unread",
                                tint = tokens.ink,
                                count = unread,
                                onClick = { showInbox = true },
                                testTag = "open_inbox_button"
                            )
                            HeaderIconWithBadge(
                                icon = if (uiState.favoriteIds.isNotEmpty()) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                                contentDescription = "Open Saved Favorites Wishlist",
                                tint = if (uiState.favoriteIds.isNotEmpty()) tokens.alertRed else tokens.ink,
                                count = uiState.favoriteIds.size,
                                onClick = { showWishlistSheet = true },
                                testTag = "open_wishlist_button"
                            )
                            Spacer(Modifier.width(4.dp))

                            Surface(
                                color = tokens.brandYellowSoft,
                                shape = RoundedCornerShape(50),
                                border = BorderStroke(1.dp, tokens.brandYellow.copy(alpha = 0.5f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Timer,
                                        contentDescription = null,
                                        tint = tokens.ink,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = "${activeNeighborhood?.etaMinutes ?: 8} MIN",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = tokens.ink,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }
                        }
                    }

                    // Search Bar + Voice Microphone
                    val pulseTransition = rememberInfiniteTransition(label = "mic_pulse")
                    val micScale by pulseTransition.animateFloat(
                        initialValue = 1f,
                        targetValue = 1.18f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(600),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "mic_scale"
                    )

                    OutlinedTextField(
                        value = uiState.searchQuery,
                        onValueChange = {
                            onSearchQueryChange(it)
                            searchFocused = it.isNotBlank()
                            showSearchSuggestions = it.isBlank()
                        },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = {
                            onSearchSubmitted(uiState.searchQuery)
                            showSearchSuggestions = false
                            focusManager.clearFocus()
                        }),
                        placeholder = {
                            Text(
                                text = when {
                                    uiState.isVoiceListening -> "Listening… speak item"
                                    metrics.isNarrowPhone -> "Search milk, bread, eggs…"
                                    else -> "Search A2 milk, avocados, sourdough…"
                                },
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Rounded.Search,
                                contentDescription = "Search",
                                tint = tokens.muted,
                                modifier = Modifier.size(metrics.iconMedium)
                            )
                        },
                        trailingIcon = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (uiState.searchQuery.isNotEmpty()) {
                                    IconButton(
                                        onClick = {
                                            onSearchQueryChange("")
                                            searchFocused = false
                                        },
                                        modifier = Modifier.testTag("clear_search_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Close,
                                            contentDescription = "Clear search",
                                            tint = tokens.muted,
                                            modifier = Modifier.size(metrics.iconMedium)
                                        )
                                    }
                                }
                                IconButton(
                                    onClick = {
                                        if (uiState.isVoiceListening) {
                                            voiceHelper.stopListening()
                                        } else {
                                            val hasAudio = ContextCompat.checkSelfPermission(
                                                context,
                                                Manifest.permission.RECORD_AUDIO
                                            ) == PackageManager.PERMISSION_GRANTED
                                            if (hasAudio) {
                                                voiceHelper.startListening()
                                            } else {
                                                micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                            }
                                        }
                                    },
                                    modifier = Modifier
                                        .scale(if (uiState.isVoiceListening) micScale else 1f)
                                        .testTag("voice_search_button")
                                ) {
                                    Surface(
                                        color = if (uiState.isVoiceListening) tokens.alertRed else tokens.brandYellow,
                                        shape = CircleShape,
                                        modifier = Modifier.size(metrics.badgeButtonSize)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Rounded.Mic,
                                                contentDescription = "Voice search",
                                                tint = if (uiState.isVoiceListening) Color.White else tokens.onBrandYellow,
                                                modifier = Modifier.size(metrics.iconMedium)
                                            )
                                        }
                                    }
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = tokens.surfaceElevated,
                            unfocusedContainerColor = tokens.surfaceElevated,
                            focusedBorderColor = tokens.brandYellow,
                            unfocusedBorderColor = tokens.border
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { state ->
                                showSearchSuggestions = state.isFocused && uiState.searchQuery.isBlank()
                            }
                            .testTag("storefront_search_input")
                    )

                    // Recent & trending searches (focused, empty search box)
                    AnimatedVisibility(visible = showSearchSuggestions && uiState.searchQuery.isBlank()) {
                        SearchSuggestionsPanel(
                            recent = uiState.preferences.recentSearches,
                            onPick = { q ->
                                onSearchQueryChange(q)
                                onSearchSubmitted(q)
                                showSearchSuggestions = false
                                searchFocused = true
                            },
                            onClearRecent = onClearRecentSearches
                        )
                    }

                    // Real-time Autocomplete Dropdown
                    AnimatedVisibility(
                        visible = searchFocused && uiState.searchQuery.isNotBlank()
                    ) {
                        val matchingCats = uiState.matchingCategories
                        val matchingProds = uiState.filteredProducts.take(4)
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = tokens.surface,
                            border = BorderStroke(1.dp, tokens.border),
                            shadowElevation = 6.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(vertical = 6.dp)) {
                                if (matchingCats.isEmpty() && matchingProds.isEmpty()) {
                                    Text(
                                        text = "No matching items for \"${uiState.searchQuery}\"",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = tokens.muted,
                                        modifier = Modifier.padding(14.dp)
                                    )
                                } else {
                                    matchingCats.forEach { cat ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    onSelectCategory(cat)
                                                    onSearchQueryChange("")
                                                    searchFocused = false
                                                }
                                                .padding(horizontal = 14.dp, vertical = 10.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "Category: ${cat.label}",
                                                style = MaterialTheme.typography.labelLarge,
                                                color = tokens.deliveryGreenDeep
                                            )
                                            Text(
                                                text = "Filter",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = tokens.muted
                                            )
                                        }
                                    }
                                    if (matchingCats.isNotEmpty() && matchingProds.isNotEmpty()) {
                                        HorizontalDivider(color = tokens.border)
                                    }
                                    matchingProds.forEach { prod ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    onSearchSubmitted(uiState.searchQuery)
                                                    searchFocused = false
                                                    activeDetailProduct = prod
                                                }
                                                .padding(horizontal = 14.dp, vertical = 10.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = prod.name,
                                                    style = MaterialTheme.typography.titleSmall,
                                                    color = tokens.ink
                                                )
                                                Text(
                                                    text = "${prod.category.label} • ${prod.defaultVariant.label}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = tokens.muted
                                                )
                                            }
                                            Text(
                                                text = "₹${prod.defaultVariant.priceInr}",
                                                style = MaterialTheme.typography.labelLarge,
                                                color = tokens.ink
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Product Feed + Banner + Filters + Buy Again
            BoxWithConstraints(modifier = Modifier.weight(1f)) {
                val columns = metrics.gridColumns
                val bottomBarAllowance = if (uiState.totalCartCount > 0) 92.dp else 16.dp

                LazyVerticalGrid(
                    columns = GridCells.Fixed(columns),
                    contentPadding = PaddingValues(
                        start = metrics.screenHorizontalPadding,
                        end = metrics.screenHorizontalPadding,
                        top = 14.dp,
                        bottom = bottomBarAllowance
                    ),
                    horizontalArrangement = Arrangement.spacedBy(metrics.itemSpacing),
                    verticalArrangement = Arrangement.spacedBy(metrics.itemSpacing),
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Freshness Banner
                    item(span = { GridItemSpan(columns) }) {
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = tokens.surface),
                            border = BorderStroke(1.dp, tokens.border),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(IntrinsicSize.Min)
                                    .heightIn(min = 126.dp)
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.img_storefront_banner),
                                    contentDescription = "QuickKart fresh harvest",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.matchParentSize()
                                )
                                Box(
                                    modifier = Modifier
                                        .matchParentSize()
                                        .background(
                                            Brush.horizontalGradient(
                                                colors = listOf(
                                                    Color(0xFF141414).copy(alpha = 0.86f),
                                                    Color(0xFF141414).copy(alpha = 0.25f)
                                                )
                                            )
                                        )
                                )
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Surface(
                                        color = tokens.brandYellow,
                                        shape = RoundedCornerShape(50)
                                    ) {
                                        Text(
                                            text = "USE CODE QUICK50",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = tokens.onBrandYellow,
                                            fontWeight = FontWeight.ExtraBold,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                        )
                                    }
                                    Column {
                                        Text(
                                            text = "Flat ₹50 Off Fresh Essentials",
                                            style = MaterialTheme.typography.titleLarge,
                                            color = Color.White
                                        )
                                        Text(
                                            text = "Free delivery above ₹199 • Arriving in ${activeNeighborhood?.etaMinutes ?: 8}m",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.White.copy(alpha = 0.85f)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Category & Saved Favorites Quick-Filter Chips
                    item(span = { GridItemSpan(columns) }) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            item(key = "favorites_filter") {
                                val favSelected = uiState.favoritesOnly
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = if (favSelected) tokens.alertRed else tokens.surface,
                                    border = BorderStroke(
                                        1.dp,
                                        if (favSelected) tokens.alertRed else tokens.border
                                    ),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50))
                                        .clickable(onClick = onToggleFavoritesFilter)
                                        .testTag("favorites_filter_chip")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (favSelected || uiState.favoriteIds.isNotEmpty()) {
                                                Icons.Rounded.Favorite
                                            } else {
                                                Icons.Rounded.FavoriteBorder
                                            },
                                            contentDescription = null,
                                            tint = if (favSelected) Color.White else tokens.alertRed,
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Text(
                                            text = "Favorites (${uiState.favoriteIds.size})",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = if (favSelected) Color.White else tokens.ink,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }

                            items(ProductCategory.entries) { category ->
                                val selected = category == uiState.selectedCategory
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = if (selected) tokens.ink else tokens.surface,
                                    border = BorderStroke(
                                        1.dp,
                                        if (selected) tokens.ink else tokens.border
                                    ),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50))
                                        .clickable { onSelectCategory(category) }
                                        .testTag("category_chip_${category.name}")
                                ) {
                                    Text(
                                        text = category.label,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = if (selected) tokens.background else tokens.ink,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Quick-Add All Favorites Banner when Favorites filter is active
                    if (uiState.favoritesOnly) {
                        item(span = { GridItemSpan(columns) }) {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = tokens.brandYellowSoft,
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
                                            text = "Saved Favorites Filter Active",
                                            style = MaterialTheme.typography.labelLarge,
                                            color = tokens.ink,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = if (uiState.favoriteProducts.isEmpty()) {
                                                "No items saved yet — tap hearts to add"
                                            } else {
                                                "${uiState.favoriteProducts.size} saved staples ready for 1-tap add"
                                            },
                                            style = MaterialTheme.typography.bodySmall,
                                            color = tokens.muted
                                        )
                                    }
                                    if (uiState.favoriteProducts.isNotEmpty()) {
                                        Button(
                                            onClick = onQuickAddAllFavorites,
                                            shape = RoundedCornerShape(10.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = tokens.deliveryGreenDeep,
                                                contentColor = Color.White
                                            ),
                                            modifier = Modifier.testTag("storefront_quick_add_favorites_button")
                                        ) {
                                            Text("Quick-Add All", style = MaterialTheme.typography.labelMedium)
                                        }
                                    }
                                    OutlinedButton(
                                        onClick = { showWishlistSheet = true },
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.testTag("storefront_open_wishlist_chip")
                                    ) {
                                        Text("Wishlist", style = MaterialTheme.typography.labelMedium)
                                    }
                                }
                            }
                        }
                    }

                    // Buy Again / Quick Reorder Strip
                    if (uiState.buyAgainItems.isNotEmpty() && uiState.searchQuery.isBlank()) {
                        item(span = { GridItemSpan(columns) }) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Buy Again • Quick Reorder",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = tokens.ink
                                    )
                                    Text(
                                        text = "From past orders",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = tokens.muted
                                    )
                                }
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    items(uiState.buyAgainItems, key = { it.productId }) { pastItem ->
                                        val product = uiState.products.find { it.id == pastItem.productId }
                                        if (product != null) {
                                            Surface(
                                                shape = RoundedCornerShape(16.dp),
                                                color = tokens.surface,
                                                border = BorderStroke(1.dp, tokens.border),
                                                modifier = Modifier
                                                    .widthIn(min = 212.dp, max = 248.dp)
                                                    .clip(RoundedCornerShape(16.dp))
                                                    .clickable { activeDetailProduct = product }
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(10.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                                ) {
                                                    ProductArtwork(
                                                        artworkType = product.artworkType,
                                                        accentColor = product.accentColor,
                                                        modifier = Modifier.size(metrics.avatarSize)
                                                    )
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Text(
                                                            text = product.name,
                                                            style = MaterialTheme.typography.labelMedium,
                                                            color = tokens.ink,
                                                            maxLines = 2
                                                        )
                                                        Text(
                                                            text = "${pastItem.variantLabel} • ₹${pastItem.unitPriceInr}",
                                                            style = MaterialTheme.typography.bodySmall,
                                                            color = tokens.muted
                                                        )
                                                    }
                                                    Surface(
                                                        color = tokens.brandYellow,
                                                        shape = RoundedCornerShape(10.dp),
                                                        modifier = Modifier
                                                            .size(34.dp)
                                                            .clip(RoundedCornerShape(10.dp))
                                                            .clickable {
                                                                onIncrementCart(product.id, pastItem.variantId)
                                                                onShowMessage("Added ${product.name} to cart")
                                                            }
                                                            .testTag("buy_again_add_${product.id}")
                                                    ) {
                                                        Box(contentAlignment = Alignment.Center) {
                                                            Icon(
                                                                imageVector = Icons.Rounded.Add,
                                                                contentDescription = "Quick add ${product.name}",
                                                                tint = tokens.onBrandYellow,
                                                                modifier = Modifier.size(18.dp)
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Section Title
                    item(span = { GridItemSpan(columns) }) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (uiState.favoritesOnly) {
                                    "Saved Favorites • ${uiState.selectedCategory.shortLabel}"
                                } else {
                                    uiState.selectedCategory.label
                                },
                                style = MaterialTheme.typography.titleMedium,
                                color = tokens.ink
                            )
                            Text(
                                text = itemCountLabel(uiState.filteredProducts.size),
                                style = MaterialTheme.typography.bodySmall,
                                color = tokens.muted
                            )
                        }
                    }

                    if (uiState.filteredProducts.isEmpty()) {
                        item(span = { GridItemSpan(columns) }) {
                            Card(
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(containerColor = tokens.surface),
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
                                        imageVector = if (uiState.favoritesOnly) Icons.Rounded.FavoriteBorder else Icons.Rounded.Search,
                                        contentDescription = null,
                                        tint = tokens.muted,
                                        modifier = Modifier.size(metrics.iconHero)
                                    )
                                    Text(
                                        text = if (uiState.favoritesOnly) {
                                            "No saved favorites match"
                                        } else {
                                            "No products match your filter"
                                        },
                                        style = MaterialTheme.typography.titleMedium,
                                        color = tokens.ink,
                                        textAlign = TextAlign.Center
                                    )
                                    Text(
                                        text = if (uiState.favoritesOnly) {
                                            "Tap the heart icon on any product or reset filters to view all groceries."
                                        } else {
                                            "Try clearing your search or switching category filters."
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = tokens.muted,
                                        textAlign = TextAlign.Center
                                    )
                                    Button(
                                        onClick = {
                                            onSetFavoritesOnly(false)
                                            onSelectCategory(ProductCategory.ALL)
                                            onSearchQueryChange("")
                                        },
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = tokens.brandYellow,
                                            contentColor = tokens.onBrandYellow
                                        ),
                                        modifier = Modifier.testTag("reset_storefront_filters_button")
                                    ) {
                                        Text("Show All Essentials", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    } else {
                        // 2-Column Product Grid
                        items(uiState.filteredProducts, key = { it.id }) { product ->
                            val activeVariantId = selectedVariantByProduct[product.id] ?: product.defaultVariant.id
                            val activeVariant = product.variants.find { it.id == activeVariantId }
                                ?: product.defaultVariant
                            val qtyInCart = uiState.cartItems.find {
                                it.product.id == product.id && it.variant.id == activeVariant.id
                            }?.quantity ?: 0
                            val isFavorite = product.id in uiState.favoriteIds

                            ProductGridCard(
                                product = product,
                                selectedVariantId = activeVariant.id,
                                qtyInCart = qtyInCart,
                                isFavorite = isFavorite,
                                onCardClick = { activeDetailProduct = product },
                                onCycleVariant = {
                                    if (product.variants.size > 1) {
                                        val idx = product.variants.indexOfFirst { it.id == activeVariant.id }
                                        val next = product.variants[(idx + 1) % product.variants.size]
                                        selectedVariantByProduct[product.id] = next.id
                                    } else {
                                        activeDetailProduct = product
                                    }
                                },
                                onToggleFavorite = { onToggleFavorite(product.id) },
                                onIncrement = { onIncrementCart(product.id, activeVariant.id) },
                                onDecrement = { onDecrementCart(product.id, activeVariant.id) }
                            )
                        }
                    }
                }
            }
        }

        // Floating "View Cart" Green Bar
        AnimatedVisibility(
            visible = uiState.totalCartCount > 0,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Surface(
                color = tokens.deliveryGreenDeep,
                shape = RoundedCornerShape(18.dp),
                shadowElevation = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .clickable(onClick = onOpenCart)
                    .testTag("floating_view_cart_bar")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 13.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            color = Color.White.copy(alpha = 0.18f),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Rounded.ShoppingBag,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = "${uiState.totalCartCount} ${if (uiState.totalCartCount == 1) "item" else "items"}",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                            Text(
                                text = "₹${uiState.billBreakdown.itemTotal}",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "View Cart",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                            contentDescription = "Go to Cart",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }

    if (showInbox) {
        val inboxItems = remember(uiState.orders, uiState.walletTransactions, uiState.supportTickets) {
            buildInboxItems(uiState)
        }
        // Capture "last seen" when opened so new items stay highlighted until the inbox closes.
        val seenAtOpen = remember { uiState.preferences.inboxLastSeenMillis }
        NotificationInboxDialog(
            items = inboxItems,
            lastSeenMillis = seenAtOpen,
            onDismiss = {
                showInbox = false
                onMarkInboxSeen()
            }
        )
    }

    activeDetailProduct?.let { detailProduct ->
        ProductDetailBottomSheet(
            product = detailProduct,
            initialVariantId = selectedVariantByProduct[detailProduct.id] ?: detailProduct.defaultVariant.id,
            isFavorite = detailProduct.id in uiState.favoriteIds,
            cartItems = uiState.cartItems,
            onDismiss = { activeDetailProduct = null },
            onToggleFavorite = { onToggleFavorite(detailProduct.id) },
            onIncrementCart = onIncrementCart,
            onDecrementCart = onDecrementCart
        )
    }

    if (showWishlistSheet) {
        SavedFavoritesBottomSheet(
            favoriteProducts = uiState.favoriteProducts,
            cartItems = uiState.cartItems,
            totalCartCount = uiState.totalCartCount,
            onDismiss = { showWishlistSheet = false },
            onToggleFavorite = onToggleFavorite,
            onIncrementCart = onIncrementCart,
            onDecrementCart = onDecrementCart,
            onQuickAddAll = {
                onQuickAddAllFavorites()
            },
            onOpenCart = onOpenCart,
            onProductClick = { product ->
                showWishlistSheet = false
                activeDetailProduct = product
            }
        )
    }
}

@Composable
private fun ProductGridCard(
    product: Product,
    selectedVariantId: String,
    qtyInCart: Int,
    isFavorite: Boolean,
    onCardClick: () -> Unit,
    onCycleVariant: () -> Unit,
    onToggleFavorite: () -> Unit,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit
) {
    val tokens = QuickKartThemeTokens.colors
    val metrics = QuickKartMetricsTokens.metrics
    val variant = product.variants.find { it.id == selectedVariantId } ?: product.defaultVariant

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = tokens.surface),
        border = BorderStroke(1.dp, tokens.border),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onCardClick)
            .testTag("product_card_${product.id}")
    ) {
        Column(
            modifier = Modifier.padding(metrics.cardPadding),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(metrics.productArtworkHeight)
            ) {
                ProductArtwork(
                    artworkType = product.artworkType,
                    accentColor = product.accentColor,
                    modifier = Modifier.fillMaxSize()
                )

                // Discount Badge
                if (variant.discountPercent > 0) {
                    Surface(
                        color = tokens.deliveryGreenDeep,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(6.dp)
                    ) {
                        Text(
                            text = "${variant.discountPercent}% OFF",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                // Favorite Toggle
                Surface(
                    color = tokens.surface.copy(alpha = 0.9f),
                    shape = CircleShape,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .size(metrics.badgeButtonSize)
                        .clip(CircleShape)
                        .clickable(onClick = onToggleFavorite)
                        .testTag("fav_toggle_${product.id}")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                            contentDescription = "Favorite ${product.name}",
                            tint = if (isFavorite) tokens.alertRed else tokens.muted,
                            modifier = Modifier.size(metrics.iconSmall)
                        )
                    }
                }

                // 8m badge
                Surface(
                    color = tokens.surface.copy(alpha = 0.92f),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(6.dp)
                ) {
                    Text(
                        text = "⚡ ${product.deliveryMinutes}m",
                        style = MaterialTheme.typography.labelSmall,
                        color = tokens.ink,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = tokens.ink,
                    maxLines = 3,
                    modifier = Modifier.defaultMinSize(minHeight = 38.dp)
                )

                // Pack Variant Pill
                Surface(
                    color = tokens.surfaceElevated,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, tokens.border),
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = onCycleVariant)
                        .testTag("variant_pill_${product.id}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = variant.label,
                            style = MaterialTheme.typography.bodySmall,
                            color = tokens.muted
                        )
                        if (product.variants.size > 1) {
                            Icon(
                                imageVector = Icons.Rounded.KeyboardArrowDown,
                                contentDescription = "Switch pack size",
                                tint = tokens.muted,
                                modifier = Modifier.size(metrics.iconSmall)
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f, fill = false)) {
                    Text(
                        text = "₹${variant.priceInr}",
                        style = MaterialTheme.typography.titleMedium,
                        color = tokens.ink
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

                if (qtyInCart == 0) {
                    Surface(
                        color = tokens.brandYellowSoft,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.5.dp, tokens.brandYellow),
                        modifier = Modifier
                            .defaultMinSize(minHeight = 36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable(onClick = onIncrement)
                            .testTag("add_button_${product.id}")
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.padding(
                                horizontal = if (metrics.isNarrowPhone) 10.dp else 14.dp,
                                vertical = 6.dp
                            )
                        ) {
                            Text(
                                text = "ADD",
                                style = MaterialTheme.typography.labelMedium,
                                color = tokens.ink,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                } else {
                    Surface(
                        color = tokens.deliveryGreenDeep,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.defaultMinSize(minHeight = 36.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(if (metrics.isNarrowPhone) 26.dp else 30.dp)
                                    .clickable(onClick = onDecrement)
                                    .testTag("dec_button_${product.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Remove,
                                    contentDescription = "Remove one",
                                    tint = Color.White,
                                    modifier = Modifier.size(metrics.iconSmall)
                                )
                            }
                            Text(
                                text = qtyInCart.toString(),
                                style = MaterialTheme.typography.labelLarge,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 3.dp)
                            )
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(if (metrics.isNarrowPhone) 26.dp else 30.dp)
                                    .clickable(onClick = onIncrement)
                                    .testTag("inc_button_${product.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Add,
                                    contentDescription = "Add one",
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

/**
 * Header icon (48dp touch target) with a compact count badge on the icon's top-right corner.
 * The badge stays inside the touch target so it is never clipped, and a surface-coloured ring
 * keeps it readable on top of coloured icons (e.g. the red wishlist heart).
 */
@Composable
private fun HeaderIconWithBadge(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    tint: Color,
    count: Int,
    onClick: () -> Unit,
    testTag: String
) {
    val tokens = QuickKartThemeTokens.colors
    val iconSize = QuickKartMetricsTokens.metrics.iconMedium
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(iconSize)
        )
        if (count > 0) {
            Box(
                modifier = Modifier
                    // Centre of the badge on the icon's top-right corner.
                    .offset(x = iconSize / 2, y = -(iconSize / 2))
                    .defaultMinSize(minWidth = 16.dp, minHeight = 16.dp)
                    .background(tokens.alertRed, RoundedCornerShape(50))
                    .border(1.5.dp, tokens.surface, RoundedCornerShape(50))
                    .padding(horizontal = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (count > 9) "9+" else count.toString(),
                    color = Color.White,
                    fontSize = 10.sp,
                    lineHeight = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}