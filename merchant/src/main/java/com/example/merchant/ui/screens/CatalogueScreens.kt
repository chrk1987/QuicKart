package com.example.merchant.ui.screens

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AddAPhoto
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.LocalOffer
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.merchant.data.CatalogueItem
import com.example.merchant.data.OfferType
import com.example.merchant.data.StoreOffer
import com.example.ui.components.BadgeTone
import com.example.ui.components.BrandSwitch
import com.example.ui.components.ChoiceChip
import com.example.ui.components.EmptyState
import com.example.ui.components.PrimaryButton
import com.example.ui.components.QkCard
import com.example.ui.components.QkTextField
import com.example.ui.components.ScreenHeader
import com.example.ui.components.SectionTitle
import com.example.ui.components.StatusBadge
import com.example.ui.components.rupees
import com.example.ui.theme.QuickKartMetricsTokens
import com.example.ui.theme.QuickKartThemeTokens
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// ─────────────────────────────────────────────────────────────────────────────────────────────
// 6. Manage Catalogue & Stock
// ─────────────────────────────────────────────────────────────────────────────────────────────

enum class StockFilter(val label: String) { ALL("All"), IN_STOCK("In stock"), LOW("Low stock"), OUT("Out of stock") }

@Composable
fun CatalogueScreen(
    items: List<CatalogueItem>,
    onBack: (() -> Unit)?,
    onSetStock: (String, Int) -> Unit,
    onEdit: (String) -> Unit,
    onAdd: () -> Unit
) {
    val tokens = QuickKartThemeTokens.colors
    val metrics = QuickKartMetricsTokens.metrics
    var query by rememberSaveable { mutableStateOf("") }
    var filter by rememberSaveable { mutableStateOf(StockFilter.ALL) }
    var restockItem by remember { mutableStateOf<CatalogueItem?>(null) }
    // Remember the last stock when switching an item off so switching it back on restores it.
    val lastStock = remember { mutableMapOf<String, Int>() }

    fun CatalogueItem.matches(f: StockFilter) = when (f) {
        StockFilter.ALL -> true
        StockFilter.IN_STOCK -> stock > CatalogueItem.LOW_STOCK_THRESHOLD
        StockFilter.LOW -> isLowStock
        StockFilter.OUT -> isOutOfStock
    }
    val visible = items.filter { it.matches(filter) && (query.isBlank() || it.name.contains(query.trim(), ignoreCase = true) || it.category.contains(query.trim(), true)) }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            ScreenHeader("Catalogue & Stock", onBack = onBack, subtitle = "${items.size} items · ${items.count { it.isOutOfStock }} out of stock")
            QkTextField(
                value = query, onValueChange = { query = it }, label = "Search items or category", leadingIcon = Icons.Rounded.Search,
                modifier = Modifier.padding(horizontal = metrics.screenHorizontalPadding), testTag = "catalogue_search"
            )
            Spacer(Modifier.height(8.dp))
            LazyRow(contentPadding = PaddingValues(horizontal = metrics.screenHorizontalPadding), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(StockFilter.entries) { f -> ChoiceChip(f.label, f == filter, { filter = f }, count = items.count { it.matches(f) }.takeIf { f != StockFilter.ALL }) }
            }
            LazyColumn(
                contentPadding = PaddingValues(start = metrics.screenHorizontalPadding, end = metrics.screenHorizontalPadding, top = 10.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                if (visible.isEmpty()) {
                    item { EmptyState(Icons.Rounded.Inventory2, if (query.isBlank()) "Nothing here" else "No match for \"$query\"", "Try another filter or add a new item.") }
                }
                items(visible, key = { it.id }) { item ->
                    QkCard(onClick = { onEdit(item.id) }, contentPadding = PaddingValues(12.dp), modifier = Modifier.testTag("catalogue_item_${item.id}")) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            ProductThumb(item, 56.dp)
                            Column(Modifier.weight(1f)) {
                                Text(item.name, style = MaterialTheme.typography.titleSmall, color = if (item.active) tokens.ink else tokens.muted, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text("${item.unit} · ${item.category}", style = MaterialTheme.typography.labelSmall, color = tokens.muted, maxLines = 1)
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(rupees(item.sellingPrice), style = MaterialTheme.typography.titleSmall, color = tokens.ink)
                                    if (item.discountPercent > 0) Text(rupees(item.price), style = MaterialTheme.typography.labelSmall, color = tokens.muted, textDecoration = TextDecoration.LineThrough)
                                    when {
                                        !item.active -> StatusBadge("Hidden", BadgeTone.NEUTRAL)
                                        item.isOutOfStock -> StatusBadge("Out of stock", BadgeTone.DANGER)
                                        item.isLowStock -> StatusBadge("Low · ${item.stock}", BadgeTone.WARNING)
                                        else -> StatusBadge("Stock ${item.stock}", BadgeTone.SUCCESS)
                                    }
                                }
                            }
                            BrandSwitch(
                                checked = item.stock > 0,
                                onCheckedChange = { on ->
                                    if (on) {
                                        val restore = lastStock[item.id]
                                        if (restore != null && restore > 0) onSetStock(item.id, restore) else restockItem = item
                                    } else {
                                        lastStock[item.id] = item.stock
                                        onSetStock(item.id, 0)
                                    }
                                },
                                modifier = Modifier.testTag("stock_switch_${item.id}")
                            )
                        }
                    }
                }
            }
        }
        ExtendedFloatingActionButton(
            onClick = onAdd,
            containerColor = tokens.brandYellow,
            contentColor = tokens.onBrandYellow,
            icon = { Icon(Icons.Rounded.Add, null) },
            text = { Text("Add product", fontWeight = FontWeight.Bold) },
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp).testTag("add_product_fab")
        )
    }

    restockItem?.let { item ->
        var qty by remember(item.id) { mutableStateOf("10") }
        val n = qty.toIntOrNull()
        AlertDialog(
            onDismissRequest = { restockItem = null },
            title = { Text("Back in stock") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("How many ${item.name} (${item.unit}) do you have?", style = MaterialTheme.typography.bodySmall)
                    QkTextField(qty, { qty = it.filter(Char::isDigit).take(4) }, "Quantity", keyboardType = KeyboardType.Number, error = if (n == null || n <= 0) "Enter at least 1" else null)
                }
            },
            confirmButton = { TextButton(enabled = n != null && n > 0, onClick = { onSetStock(item.id, n!!); restockItem = null }) { Text("Update stock", color = tokens.ink) } },
            dismissButton = { TextButton(onClick = { restockItem = null }) { Text("Cancel", color = tokens.muted) } }
        )
    }
}

@Composable
fun ProductThumb(item: CatalogueItem, size: Dp) {
    val tokens = QuickKartThemeTokens.colors
    val bitmap = rememberUriBitmap(item.photoUri)
    Box(Modifier.size(size).clip(RoundedCornerShape(14.dp)).background(tokens.brandYellowSoft), contentAlignment = Alignment.Center) {
        if (bitmap != null) {
            Image(bitmap, contentDescription = item.name, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
        } else {
            Text(item.name.take(1).uppercase(), style = MaterialTheme.typography.headlineSmall, color = tokens.ink, fontWeight = FontWeight.ExtraBold)
        }
    }
}

/** Decodes a gallery photo (downsampled) for previews. Returns null if it can't be read. */
@Composable
fun rememberUriBitmap(uri: String?): ImageBitmap? {
    val context = LocalContext.current
    val state = produceState<ImageBitmap?>(initialValue = null, uri) {
        value = if (uri == null) null else withContext(Dispatchers.IO) {
            runCatching {
                val u = Uri.parse(uri)
                val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                context.contentResolver.openInputStream(u)?.use { BitmapFactory.decodeStream(it, null, bounds) }
                var sample = 1
                while (bounds.outWidth / sample > 600 || bounds.outHeight / sample > 600) sample *= 2
                context.contentResolver.openInputStream(u)?.use {
                    BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = sample })?.asImageBitmap()
                }
            }.getOrNull()
        }
    }
    return state.value
}

// ─────────────────────────────────────────────────────────────────────────────────────────────
// 7. Add / Edit Product
// ─────────────────────────────────────────────────────────────────────────────────────────────

@Composable
fun ProductEditorScreen(
    existing: CatalogueItem?,
    allItems: List<CatalogueItem>,
    onBack: () -> Unit,
    onSave: (CatalogueItem) -> Unit,
    onDelete: (String) -> Boolean
) {
    val tokens = QuickKartThemeTokens.colors
    val context = LocalContext.current
    var name by rememberSaveable { mutableStateOf(existing?.name ?: "") }
    var description by rememberSaveable { mutableStateOf(existing?.description ?: "") }
    var category by rememberSaveable { mutableStateOf(existing?.category ?: "") }
    var unit by rememberSaveable { mutableStateOf(existing?.unit ?: "") }
    var price by rememberSaveable { mutableStateOf(existing?.price?.toString() ?: "") }
    var discount by rememberSaveable { mutableStateOf(existing?.discountPercent?.toString() ?: "0") }
    var stock by rememberSaveable { mutableStateOf(existing?.stock?.toString() ?: "") }
    var active by rememberSaveable { mutableStateOf(existing?.active ?: true) }
    var photo by rememberSaveable { mutableStateOf(existing?.photoUri) }
    var showErrors by remember { mutableStateOf(false) }
    var categoryMenu by remember { mutableStateOf(false) }
    var confirmDiscard by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            runCatching { context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION) }
            photo = uri.toString()
        }
    }

    val priceN = price.toIntOrNull()
    val discountN = discount.toIntOrNull()
    val stockN = stock.toIntOrNull()
    val duplicate = allItems.any { it.id != existing?.id && it.name.equals(name.trim(), ignoreCase = true) && it.unit.equals(unit.trim(), true) }
    val nameErr = when { name.trim().length < 2 -> "Enter the product name"; duplicate -> "This item already exists in your catalogue"; else -> null }
    val catErr = if (category.isBlank()) "Choose a category" else null
    val unitErr = if (unit.isBlank()) "e.g. 1 kg, 500 ml, 6 pcs" else null
    val priceErr = if (priceN == null || priceN !in 1..100000) "Enter a price between ₹1 and ₹1,00,000" else null
    val discErr = if (discountN == null || discountN !in 0..90) "Discount can be 0–90%" else null
    val stockErr = if (stockN == null || stockN !in 0..9999) "Stock can be 0–9999" else null
    val valid = listOf(nameErr, catErr, unitErr, priceErr, discErr, stockErr).all { it == null }

    val draft = CatalogueItem(
        id = existing?.id ?: "p_${System.currentTimeMillis()}",
        name = name.trim(), description = description.trim(), category = category, unit = unit.trim(),
        price = priceN ?: 0, discountPercent = discountN ?: 0, stock = stockN ?: 0, active = active, photoUri = photo
    )
    val dirty = existing?.let { it != draft.copy(id = it.id) } ?: (name.isNotBlank() || price.isNotBlank() || photo != null)

    fun attemptSave() { if (valid) onSave(draft) else showErrors = true }
    BackHandler { if (dirty) confirmDiscard = true else onBack() }

    Column(Modifier.fillMaxSize().imePadding()) {
        ScreenHeader(
            title = if (existing == null) "Add New Product" else "Edit Product",
            onBack = { if (dirty) confirmDiscard = true else onBack() },
            actions = {
                if (existing != null) IconButton(onClick = { confirmDelete = true }) { Icon(Icons.Rounded.DeleteOutline, "Delete product", tint = tokens.alertRed) }
                TextButton(onClick = ::attemptSave, modifier = Modifier.testTag("product_save_top")) { Text("Save", color = tokens.ink, fontWeight = FontWeight.Bold) }
            }
        )
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Photo
            val bitmap = rememberUriBitmap(photo)
            Box(
                Modifier.fillMaxWidth().height(170.dp).clip(RoundedCornerShape(18.dp)).background(tokens.surfaceElevated)
                    .clickable { picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }
                    .testTag("product_photo"),
                contentAlignment = Alignment.Center
            ) {
                if (bitmap != null) {
                    Image(bitmap, null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                    Surface(color = tokens.brandYellow, shape = RoundedCornerShape(50), modifier = Modifier.align(Alignment.BottomEnd).padding(10.dp)) {
                        Text("Change photo", style = MaterialTheme.typography.labelMedium, color = tokens.onBrandYellow, modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp))
                    }
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Box(Modifier.size(52.dp).background(tokens.brandYellowSoft, CircleShape), contentAlignment = Alignment.Center) { Icon(Icons.Rounded.AddAPhoto, null, tint = tokens.ink) }
                        Text("Upload product photo", style = MaterialTheme.typography.titleSmall, color = tokens.ink)
                        Text("Use your own photo of the product", style = MaterialTheme.typography.labelSmall, color = tokens.muted)
                    }
                }
            }
            QkTextField(name, { name = it.take(60) }, "Product name", error = nameErr.takeIf { showErrors || duplicate }, testTag = "product_name")
            QkTextField(description, { description = it.take(200) }, "Description (optional)", singleLine = false, minLines = 2)
            Box {
                QkTextField(category.ifBlank { "" }, {}, "Category", error = catErr.takeIf { showErrors }, enabled = false)
                Box(Modifier.matchParentSize().clickable { categoryMenu = true })
                Icon(Icons.Rounded.ArrowDropDown, null, tint = tokens.ink, modifier = Modifier.align(Alignment.CenterEnd).padding(end = 12.dp))
                DropdownMenu(expanded = categoryMenu, onDismissRequest = { categoryMenu = false }) {
                    CatalogueItem.CATEGORIES.forEach { c -> DropdownMenuItem(text = { Text(c) }, onClick = { category = c; categoryMenu = false }) }
                }
            }
            QkTextField(unit, { unit = it.take(20) }, "Pack size / unit", error = unitErr.takeIf { showErrors }, placeholder = "1 kg")
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                QkTextField(price, { price = it.filter(Char::isDigit).take(6) }, "Price (MRP)", prefix = "₹ ", keyboardType = KeyboardType.Number, error = priceErr.takeIf { showErrors }, modifier = Modifier.weight(1f), testTag = "product_price")
                QkTextField(discount, { discount = it.filter(Char::isDigit).take(2) }, "Discount %", keyboardType = KeyboardType.Number, error = discErr.takeIf { showErrors || (discountN ?: 0) > 90 }, modifier = Modifier.weight(1f))
            }
            if (priceN != null && discountN != null && discErr == null) {
                Text("Customer pays ${rupees(draft.sellingPrice)}" + if (discountN > 0) " (saves ${rupees(priceN - draft.sellingPrice)})" else "", style = MaterialTheme.typography.bodySmall, color = tokens.deliveryGreenDeep)
            }
            QkTextField(stock, { stock = it.filter(Char::isDigit).take(4) }, "Stock quantity", keyboardType = KeyboardType.Number, error = stockErr.takeIf { showErrors }, supporting = if (stockN != null && stockN in 1..CatalogueItem.LOW_STOCK_THRESHOLD) "Low stock — you'll get an alert" else null)
            QkCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Active", style = MaterialTheme.typography.titleSmall, color = tokens.ink)
                        Text(if (active) "Visible to customers" else "Hidden from customers", style = MaterialTheme.typography.bodySmall, color = tokens.muted)
                    }
                    BrandSwitch(checked = active, onCheckedChange = { active = it })
                }
            }
        }
        PrimaryButton(if (existing == null) "Add product" else "Save changes", onClick = ::attemptSave, modifier = Modifier.padding(16.dp), testTag = "product_save")
    }

    if (confirmDiscard) {
        AlertDialog(
            onDismissRequest = { confirmDiscard = false },
            title = { Text("Discard changes?") },
            text = { Text("Your edits to this product will be lost.") },
            confirmButton = { TextButton(onClick = { confirmDiscard = false; onBack() }) { Text("Discard", color = tokens.alertRed) } },
            dismissButton = { TextButton(onClick = { confirmDiscard = false }) { Text("Keep editing", color = tokens.ink) } }
        )
    }
    if (confirmDelete && existing != null) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Delete ${existing.name}?") },
            text = { Text("It will be removed from your catalogue. You can mark it inactive instead to hide it temporarily.") },
            confirmButton = { TextButton(onClick = { confirmDelete = false; if (onDelete(existing.id)) onBack() }) { Text("Delete", color = tokens.alertRed) } },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Cancel", color = tokens.ink) } }
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────────────────────
// 8. Offers & Promotions
// ─────────────────────────────────────────────────────────────────────────────────────────────

private val dateFmt = SimpleDateFormat("d MMM yyyy", Locale.getDefault())
fun formatDate(millis: Long): String = dateFmt.format(Date(millis))

@Composable
fun OffersScreen(
    offers: List<StoreOffer>,
    nowMillis: Long,
    onBack: () -> Unit,
    onToggle: (String, Boolean) -> Unit,
    onCreate: (StoreOffer) -> Unit,
    onDelete: (String) -> Unit
) {
    val tokens = QuickKartThemeTokens.colors
    var creating by remember { mutableStateOf(false) }
    val current = offers.filterNot { it.isExpired(nowMillis) }
    val past = offers.filter { it.isExpired(nowMillis) }
    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            ScreenHeader("Offers & Promotions", onBack = onBack, subtitle = "${current.count { it.active }} live offer(s)")
            LazyColumn(contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (current.isEmpty()) item { EmptyState(Icons.Rounded.LocalOffer, "No offers yet", "Offers help you win more orders in your area.") }
                items(current, key = { it.id }) { offer -> OfferCard(offer, nowMillis, onToggle, onDelete) }
                if (past.isNotEmpty()) {
                    item { SectionTitle("Past / expired") }
                    items(past, key = { it.id }) { offer -> OfferCard(offer, nowMillis, onToggle, onDelete) }
                }
            }
        }
        ExtendedFloatingActionButton(
            onClick = { creating = true },
            containerColor = tokens.brandYellow, contentColor = tokens.onBrandYellow,
            icon = { Icon(Icons.Rounded.Add, null) }, text = { Text("Create offer", fontWeight = FontWeight.Bold) },
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp).testTag("create_offer_fab")
        )
    }
    if (creating) CreateOfferDialog(existing = offers, nowMillis = nowMillis, onDismiss = { creating = false }, onCreate = { o, replaceIds ->
        replaceIds.forEach { onToggle(it, false) }
        onCreate(o); creating = false
    })
}

@Composable
private fun OfferCard(offer: StoreOffer, nowMillis: Long, onToggle: (String, Boolean) -> Unit, onDelete: (String) -> Unit) {
    val tokens = QuickKartThemeTokens.colors
    val expired = offer.isExpired(nowMillis)
    val scheduled = offer.validFromMillis > nowMillis
    val highlight = offer.active && !expired
    QkCard(color = if (highlight) tokens.brandYellowSoft else tokens.surface, borderColor = if (highlight) tokens.brandYellow else tokens.border) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(offer.title, style = MaterialTheme.typography.titleSmall, color = if (expired) tokens.muted else tokens.ink)
                Text("${formatDate(offer.validFromMillis)} – ${formatDate(offer.validToMillis)}", style = MaterialTheme.typography.labelSmall, color = tokens.muted)
                Spacer(Modifier.height(4.dp))
                StatusBadge(
                    when { expired -> "Expired"; !offer.active -> "Paused"; scheduled -> "Scheduled"; else -> "Live" },
                    when { expired -> BadgeTone.NEUTRAL; !offer.active -> BadgeTone.NEUTRAL; scheduled -> BadgeTone.WARNING; else -> BadgeTone.SUCCESS }
                )
            }
            if (expired) {
                IconButton(onClick = { onDelete(offer.id) }) { Icon(Icons.Rounded.DeleteOutline, "Delete offer", tint = tokens.muted) }
            } else {
                BrandSwitch(checked = offer.active, onCheckedChange = { onToggle(offer.id, it) })
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CreateOfferDialog(existing: List<StoreOffer>, nowMillis: Long, onDismiss: () -> Unit, onCreate: (StoreOffer, List<String>) -> Unit) {
    val tokens = QuickKartThemeTokens.colors
    var type by remember { mutableStateOf(OfferType.PERCENT_OFF) }
    var value by remember { mutableStateOf("10") }
    var minOrder by remember { mutableStateOf("199") }
    var category by remember { mutableStateOf<String?>(null) }
    var startOffset by remember { mutableIntStateOf(0) }
    var days by remember { mutableIntStateOf(15) }
    var confirmReplace by remember { mutableStateOf<List<StoreOffer>?>(null) }

    val v = value.toIntOrNull()
    val min = minOrder.toIntOrNull()
    val valueErr = when (type) {
        OfferType.PERCENT_OFF -> if (v == null || v !in 1..90) "1–90%" else null
        OfferType.FLAT_OFF -> if (v == null || v < 10) "At least ₹10" else if (min != null && v >= min) "Must be less than the minimum order" else null
        OfferType.FREE_DELIVERY -> null
    }
    val minErr = if (min == null || min > 5000) "₹0–₹5,000" else if (type == OfferType.FREE_DELIVERY && min < 99) "Free delivery needs a minimum of ₹99" else null
    val start = Calendar.getInstance().apply { timeInMillis = nowMillis; add(Calendar.DAY_OF_YEAR, startOffset); if (startOffset > 0) { set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0) } }.timeInMillis
    val end = start + days * 86_400_000L
    val valid = valueErr == null && minErr == null

    fun build() = StoreOffer("o${System.currentTimeMillis()}", type, if (type == OfferType.FREE_DELIVERY) 0 else v ?: 0, min ?: 0, start, end, true, category.takeIf { type == OfferType.PERCENT_OFF })

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create offer") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OfferType.entries.forEach { t -> ChoiceChip(t.label, t == type, { type = t }) }
                }
                if (type != OfferType.FREE_DELIVERY) {
                    QkTextField(value, { value = it.filter(Char::isDigit).take(4) }, if (type == OfferType.PERCENT_OFF) "Discount %" else "Flat discount", prefix = if (type == OfferType.FLAT_OFF) "₹ " else null, keyboardType = KeyboardType.Number, error = valueErr)
                }
                QkTextField(minOrder, { minOrder = it.filter(Char::isDigit).take(4) }, "Minimum order", prefix = "₹ ", keyboardType = KeyboardType.Number, error = minErr)
                if (type == OfferType.PERCENT_OFF) {
                    Text("Applies to", style = MaterialTheme.typography.labelLarge)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        ChoiceChip("Whole store", category == null, { category = null })
                        listOf("Vegetables", "Fruits", "Snacks", "Dairy & Eggs").forEach { c -> ChoiceChip(c, category == c, { category = c }) }
                    }
                }
                Text("Starts", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ChoiceChip("Today", startOffset == 0, { startOffset = 0 }); ChoiceChip("Tomorrow", startOffset == 1, { startOffset = 1 })
                }
                Text("Runs for", style = MaterialTheme.typography.labelLarge)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(7, 15, 30, 60).forEach { d -> ChoiceChip("$d days", days == d, { days = d }) }
                }
                HorizontalDivider(color = tokens.border)
                Text("Valid ${formatDate(start)} – ${formatDate(end)}", style = MaterialTheme.typography.bodySmall, color = tokens.muted)
                if (valid) Text("Preview: ${build().title}", style = MaterialTheme.typography.titleSmall, color = tokens.ink)
            }
        },
        confirmButton = {
            TextButton(enabled = valid, onClick = {
                val clash = existing.filter { it.active && !it.isExpired(nowMillis) && it.type == type && (type != OfferType.PERCENT_OFF || it.category == category) }
                if (clash.isNotEmpty()) confirmReplace = clash else onCreate(build(), emptyList())
            }) { Text("Create", color = tokens.ink, fontWeight = FontWeight.Bold) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = tokens.muted) } }
    )
    confirmReplace?.let { clash ->
        AlertDialog(
            onDismissRequest = { confirmReplace = null },
            title = { Text("Replace live offer?") },
            text = { Text("\"${clash.first().title}\" is already live. Customers can only use one offer of this type, so it will be paused.") },
            confirmButton = { TextButton(onClick = { onCreate(build(), clash.map { it.id }); confirmReplace = null }) { Text("Replace", color = tokens.ink) } },
            dismissButton = { TextButton(onClick = { confirmReplace = null }) { Text("Cancel", color = tokens.muted) } }
        )
    }
}
