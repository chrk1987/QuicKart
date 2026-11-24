package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.PreferencesManager
import com.example.data.local.QuickKartDatabase
import com.example.data.local.UserPreferences
import com.example.data.model.BillBreakdown
import com.example.data.model.CartItem
import com.example.data.model.Neighborhood
import com.example.data.model.OrderHistoryTimeFilter
import com.example.data.model.OrderLineItem
import com.example.data.model.OrderRecord
import com.example.data.model.OrderStage
import com.example.data.model.PaymentMethod
import com.example.data.model.Product
import com.example.data.model.ProductCategory
import com.example.data.model.PromoCode
import com.example.data.model.RestockSuggestion
import com.example.data.model.SavedAddress
import com.example.data.repository.QuickKartRepository
import com.example.util.NotificationHelper
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import kotlin.random.Random

enum class BottomTab(val route: String, val label: String) {
    HOME("home", "Home"),
    CART("cart", "Cart"),
    TRACK("track", "Track"),
    ACCOUNT("account", "Account")
}

data class CheckoutDraft(
    val promoInput: String = "",
    val appliedPromo: PromoCode? = null,
    val promoMessage: String? = null,
    val selectedTip: Int = 20,
    val customTipInput: String = "",
    val isCustomTip: Boolean = false,
    val selectedInstructions: Set<String> = setOf("Leave at the door"),
    val riderNote: String = "",
    val paymentMethod: PaymentMethod = PaymentMethod.UPI
)

data class QuickKartUiState(
    val isReady: Boolean = false,
    val preferences: UserPreferences = UserPreferences(),
    val neighborhoods: List<Neighborhood> = emptyList(),
    val selectedNeighborhood: Neighborhood? = null,
    val products: List<Product> = emptyList(),
    val selectedCategory: ProductCategory = ProductCategory.ALL,
    val searchQuery: String = "",
    val isVoiceListening: Boolean = false,
    val favoriteIds: Set<String> = emptySet(),
    val cartItems: List<CartItem> = emptyList(),
    val savedAddresses: List<SavedAddress> = emptyList(),
    val selectedAddress: SavedAddress? = null,
    val orders: List<OrderRecord> = emptyList(),
    val activeOrder: OrderRecord? = null,
    val latestTrackableOrder: OrderRecord? = null,
    val restockSuggestions: List<RestockSuggestion> = emptyList(),
    val buyAgainItems: List<OrderLineItem> = emptyList(),
    val historySearchQuery: String = "",
    val historyTimeFilter: OrderHistoryTimeFilter = OrderHistoryTimeFilter.ALL_TIME,
    val checkoutDraft: CheckoutDraft = CheckoutDraft(),
    val billBreakdown: BillBreakdown = BillBreakdown(0, 0, 0, 0, 0, 0, 0),
    val autoSimulateTracking: Boolean = false
) {
    val totalCartCount: Int get() = cartItems.sumOf { it.quantity }

    val filteredProducts: List<Product>
        get() = products.filter { product ->
            val matchesCat = selectedCategory == ProductCategory.ALL || product.category == selectedCategory
            val q = searchQuery.trim()
            val matchesQuery = q.isEmpty() ||
                product.name.contains(q, ignoreCase = true) ||
                product.brand.contains(q, ignoreCase = true) ||
                product.category.label.contains(q, ignoreCase = true) ||
                product.description.contains(q, ignoreCase = true)
            matchesCat && matchesQuery
        }

    val matchingCategories: List<ProductCategory>
        get() {
            val q = searchQuery.trim()
            if (q.isEmpty()) return emptyList()
            return ProductCategory.entries.filter {
                it != ProductCategory.ALL && it.label.contains(q, ignoreCase = true)
            }
        }

    val filteredHistoricalOrders: List<OrderRecord>
        get() {
            val now = System.currentTimeMillis()
            val cutoffMillis = historyTimeFilter.days?.let { days ->
                now - TimeUnit.DAYS.toMillis(days.toLong())
            }
            val q = historySearchQuery.trim()
            return orders
                .filter { it.stage == OrderStage.DELIVERED }
                .filter { order ->
                    cutoffMillis == null || order.createdAtMillis >= cutoffMillis
                }
                .filter { order ->
                    q.isEmpty() || order.items.any { item ->
                        item.productName.contains(q, ignoreCase = true) ||
                            item.category.label.contains(q, ignoreCase = true)
                    } || order.orderId.contains(q, ignoreCase = true)
                }
        }
}

class QuickKartViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: QuickKartRepository
    private val selectedCategoryFlow = MutableStateFlow(ProductCategory.ALL)
    private val searchQueryFlow = MutableStateFlow("")
    private val isVoiceListeningFlow = MutableStateFlow(false)
    private val historySearchFlow = MutableStateFlow("")
    private val historyTimeFilterFlow = MutableStateFlow(OrderHistoryTimeFilter.ALL_TIME)
    private val checkoutDraftFlow = MutableStateFlow(CheckoutDraft())
    private val autoSimFlow = MutableStateFlow(false)
    private val isReadyFlow = MutableStateFlow(false)
    private val lastActiveOrderIdFlow = MutableStateFlow<String?>(null)

    private var countdownJob: Job? = null
    private var autoSimJob: Job? = null

    init {
        val db = QuickKartDatabase.getInstance(application)
        val prefs = PreferencesManager(application)
        repository = QuickKartRepository(
            favoriteDao = db.favoriteDao(),
            cartDao = db.cartDao(),
            addressDao = db.savedAddressDao(),
            orderDao = db.orderDao(),
            prefsManager = prefs
        )
        NotificationHelper.ensureChannel(application)
        viewModelScope.launch {
            repository.ensureSeeded()
            isReadyFlow.value = true
        }
        startLiveOrderTicker()
    }

    private data class CoreStreams(
        val prefs: UserPreferences,
        val favorites: Set<String>,
        val cart: List<CartItem>,
        val addresses: List<SavedAddress>,
        val orders: List<OrderRecord>
    )

    private data class FilterStreams(
        val category: ProductCategory,
        val search: String,
        val listening: Boolean,
        val historySearch: String,
        val historyFilter: OrderHistoryTimeFilter,
        val checkout: CheckoutDraft,
        val autoSim: Boolean,
        val ready: Boolean,
        val lastTrackedOrderId: String?
    )

    private val coreFlow = combine(
        repository.userPreferences,
        repository.favoriteProductIds,
        repository.cartItems,
        repository.savedAddresses,
        repository.orders
    ) { prefs, favs, cart, addrs, orders ->
        CoreStreams(prefs, favs, cart, addrs, orders)
    }

    private val filterFlow = combine(
        selectedCategoryFlow,
        searchQueryFlow,
        isVoiceListeningFlow,
        historySearchFlow,
        historyTimeFilterFlow
    ) { cat, search, listening, histSearch, histFilter ->
        listOf(cat, search, listening, histSearch, histFilter)
    }.combine(
        combine(
            checkoutDraftFlow,
            autoSimFlow,
            isReadyFlow,
            lastActiveOrderIdFlow
        ) { draft, sim, ready, lastId ->
            listOf(draft, sim, ready, lastId)
        }
    ) { first, second ->
        FilterStreams(
            category = first[0] as ProductCategory,
            search = first[1] as String,
            listening = first[2] as Boolean,
            historySearch = first[3] as String,
            historyFilter = first[4] as OrderHistoryTimeFilter,
            checkout = second[0] as CheckoutDraft,
            autoSim = second[1] as Boolean,
            ready = second[2] as Boolean,
            lastTrackedOrderId = second[3] as String?
        )
    }

    val uiState: StateFlow<QuickKartUiState> = combine(coreFlow, filterFlow) { core, filters ->
        val selectedNeighborhood = repository.neighborhoods.find {
            it.id == core.prefs.selectedNeighborhoodId
        } ?: repository.neighborhoods.first()

        val selectedAddress = core.addresses.find { it.id == core.prefs.selectedAddressId }
            ?: core.addresses.firstOrNull { it.isDefault }
            ?: core.addresses.firstOrNull()

        val activeOrder = core.orders.firstOrNull { it.isActive }
        val latestTrackable = activeOrder
            ?: filters.lastTrackedOrderId?.let { id -> core.orders.find { it.orderId == id } }
            ?: core.orders.firstOrNull()

        val bill = computeBill(core.cart, filters.checkout)
        val restock = repository.computeRestockSuggestions(core.orders)
        val buyAgain = core.orders
            .filter { it.stage == OrderStage.DELIVERED }
            .flatMap { it.items }
            .distinctBy { it.productId }
            .take(8)

        QuickKartUiState(
            isReady = filters.ready,
            preferences = core.prefs,
            neighborhoods = repository.neighborhoods,
            selectedNeighborhood = selectedNeighborhood,
            products = repository.products,
            selectedCategory = filters.category,
            searchQuery = filters.search,
            isVoiceListening = filters.listening,
            favoriteIds = core.favorites,
            cartItems = core.cart,
            savedAddresses = core.addresses,
            selectedAddress = selectedAddress,
            orders = core.orders,
            activeOrder = activeOrder,
            latestTrackableOrder = latestTrackable,
            restockSuggestions = restock,
            buyAgainItems = buyAgain,
            historySearchQuery = filters.historySearch,
            historyTimeFilter = filters.historyFilter,
            checkoutDraft = filters.checkout,
            billBreakdown = bill,
            autoSimulateTracking = filters.autoSim
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = QuickKartUiState()
    )

    private fun computeBill(cart: List<CartItem>, draft: CheckoutDraft): BillBreakdown {
        if (cart.isEmpty()) {
            return BillBreakdown(0, 0, 0, 0, 0, 0, 0)
        }
        val itemTotal = cart.sumOf { it.lineTotal }
        val mrpTotal = cart.sumOf { it.lineMrpTotal }
        val mrpSavings = (mrpTotal - itemTotal).coerceAtLeast(0)
        val deliveryFee = if (itemTotal >= 199) 0 else 15
        val handlingCharge = 4
        val validPromo = draft.appliedPromo?.takeIf { itemTotal >= it.minOrderInr }
        val promoDiscount = validPromo?.discountInr ?: 0
        val tipAmount = if (draft.isCustomTip) {
            draft.customTipInput.toIntOrNull()?.coerceIn(0, 500) ?: 0
        } else {
            draft.selectedTip
        }
        val grandTotal = (itemTotal + deliveryFee + handlingCharge + tipAmount - promoDiscount)
            .coerceAtLeast(1)
        return BillBreakdown(
            itemTotal = itemTotal,
            mrpSavings = mrpSavings,
            deliveryFee = deliveryFee,
            handlingCharge = handlingCharge,
            promoDiscount = promoDiscount,
            tipAmount = tipAmount,
            grandTotal = grandTotal
        )
    }

    private fun startLiveOrderTicker() {
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            while (true) {
                delay(1000L)
                val active = uiState.value.activeOrder ?: continue
                if (active.remainingSeconds > 0) {
                    repository.updateOrderRemainingSeconds(
                        active.orderId,
                        active.remainingSeconds - 1
                    )
                }
            }
        }
    }

    fun toggleDarkTheme(enabled: Boolean) {
        viewModelScope.launch { repository.setDarkTheme(enabled) }
    }

    fun toggleNotifications(enabled: Boolean) {
        viewModelScope.launch { repository.setNotificationsEnabled(enabled) }
    }

    fun selectNeighborhood(neighborhoodId: String) {
        viewModelScope.launch { repository.setSelectedNeighborhood(neighborhoodId) }
    }

    fun signInWithPhone(phone: String, neighborhoodId: String) {
        viewModelScope.launch {
            repository.signInWithPhone(phone, neighborhoodId)
        }
    }

    fun continueAsGuest(neighborhoodId: String) {
        viewModelScope.launch {
            repository.signInAsGuest(neighborhoodId)
        }
    }

    fun signOut() {
        viewModelScope.launch {
            repository.signOut()
        }
    }

    fun selectCategory(category: ProductCategory) {
        selectedCategoryFlow.value = category
    }

    fun updateSearchQuery(query: String) {
        searchQueryFlow.value = query
    }

    fun setVoiceListening(listening: Boolean) {
        isVoiceListeningFlow.value = listening
    }

    fun toggleFavorite(productId: String) {
        viewModelScope.launch { repository.toggleFavorite(productId) }
    }

    fun incrementCart(productId: String, variantId: String) {
        viewModelScope.launch { repository.incrementCartItem(productId, variantId) }
    }

    fun decrementCart(productId: String, variantId: String) {
        viewModelScope.launch { repository.decrementCartItem(productId, variantId) }
    }

    fun removeCartItem(productId: String, variantId: String) {
        viewModelScope.launch { repository.setCartQuantity(productId, variantId, 0) }
    }

    fun addProductToCart(product: Product, variantId: String = product.defaultVariant.id, qty: Int = 1) {
        viewModelScope.launch {
            val current = uiState.value.cartItems.find {
                it.product.id == product.id && it.variant.id == variantId
            }?.quantity ?: 0
            repository.setCartQuantity(product.id, variantId, current + qty)
        }
    }

    fun selectAddress(address: SavedAddress) {
        viewModelScope.launch {
            repository.setSelectedAddressId(address.id)
            repository.setSelectedNeighborhood(address.neighborhoodId)
        }
    }

    fun setDefaultAddress(addressId: String) {
        viewModelScope.launch { repository.setDefaultAddress(addressId) }
    }

    fun addAddress(label: String, fullAddress: String, landmark: String, neighborhoodId: String) {
        viewModelScope.launch {
            val newAddr = SavedAddress(
                id = "addr_${System.currentTimeMillis()}",
                label = label,
                fullAddress = fullAddress.trim(),
                landmark = landmark.trim(),
                neighborhoodId = neighborhoodId,
                isDefault = true
            )
            repository.addAddress(newAddr)
        }
    }

    fun deleteAddress(addressId: String) {
        viewModelScope.launch { repository.deleteAddress(addressId) }
    }

    fun updatePromoInput(input: String) {
        checkoutDraftFlow.update { it.copy(promoInput = input, promoMessage = null) }
    }

    fun applyPromoCode(codeInput: String = checkoutDraftFlow.value.promoInput) {
        val clean = codeInput.trim().uppercase()
        val match = repository.availablePromos.find { it.code == clean }
        val itemTotal = uiState.value.billBreakdown.itemTotal
        if (match == null) {
            checkoutDraftFlow.update {
                it.copy(
                    promoInput = clean,
                    appliedPromo = null,
                    promoMessage = "Invalid code. Try QUICK50, FRESH20, or FAST8."
                )
            }
        } else if (itemTotal < match.minOrderInr) {
            checkoutDraftFlow.update {
                it.copy(
                    promoInput = clean,
                    appliedPromo = null,
                    promoMessage = "Add ₹${match.minOrderInr - itemTotal} more to unlock ${match.code}."
                )
            }
        } else {
            checkoutDraftFlow.update {
                it.copy(
                    promoInput = clean,
                    appliedPromo = match,
                    promoMessage = "${match.code} applied! Saved ₹${match.discountInr}"
                )
            }
        }
    }

    fun removePromo() {
        checkoutDraftFlow.update {
            it.copy(promoInput = "", appliedPromo = null, promoMessage = null)
        }
    }

    fun selectTip(amount: Int) {
        checkoutDraftFlow.update {
            it.copy(selectedTip = amount, isCustomTip = false, customTipInput = "")
        }
    }

    fun selectCustomTipMode() {
        checkoutDraftFlow.update { it.copy(isCustomTip = true) }
    }

    fun updateCustomTip(value: String) {
        val digitsOnly = value.filter { it.isDigit() }.take(3)
        checkoutDraftFlow.update {
            it.copy(isCustomTip = true, customTipInput = digitsOnly)
        }
    }

    fun toggleDeliveryInstruction(chip: String) {
        checkoutDraftFlow.update { draft ->
            val next = draft.selectedInstructions.toMutableSet()
            if (!next.add(chip)) next.remove(chip)
            draft.copy(selectedInstructions = next)
        }
    }

    fun updateRiderNote(note: String) {
        checkoutDraftFlow.update { it.copy(riderNote = note) }
    }

    fun selectPaymentMethod(method: PaymentMethod) {
        checkoutDraftFlow.update { it.copy(paymentMethod = method) }
    }

    fun placeOrder(onOrderCreated: (OrderRecord) -> Unit) {
        val snapshot = uiState.value
        if (snapshot.cartItems.isEmpty()) return

        viewModelScope.launch {
            val neighborhood = snapshot.selectedNeighborhood ?: repository.neighborhoods.first()
            val address = snapshot.selectedAddress ?: snapshot.savedAddresses.firstOrNull()
            val draft = snapshot.checkoutDraft
            val bill = snapshot.billBreakdown

            val orderId = "QK-${Random.nextInt(85000, 99999)}"
            val newOrder = OrderRecord(
                orderId = orderId,
                createdAtMillis = System.currentTimeMillis(),
                neighborhoodName = neighborhood.displayTitle,
                etaMinutes = neighborhood.etaMinutes,
                remainingSeconds = neighborhood.etaMinutes * 60,
                stage = OrderStage.CONFIRMED,
                items = snapshot.cartItems.map { cartItem ->
                    OrderLineItem(
                        productId = cartItem.product.id,
                        productName = cartItem.product.name,
                        variantId = cartItem.variant.id,
                        variantLabel = cartItem.variant.label,
                        category = cartItem.product.category,
                        quantity = cartItem.quantity,
                        unitPriceInr = cartItem.variant.priceInr,
                        unitMrpInr = cartItem.variant.mrpInr
                    )
                },
                deliveryAddressLabel = address?.label ?: "Home",
                deliveryAddressFull = address?.fullAddress ?: "${neighborhood.name}, ${neighborhood.city}",
                deliveryInstructions = draft.selectedInstructions.toList(),
                riderNote = draft.riderNote.trim(),
                paymentMethod = draft.paymentMethod,
                riderName = "Arjun Verma",
                riderPhone = "+91 98450 77218",
                riderRating = 4.9f,
                itemTotal = bill.itemTotal,
                mrpSavings = bill.mrpSavings,
                deliveryFee = bill.deliveryFee,
                handlingCharge = bill.handlingCharge,
                promoDiscount = bill.promoDiscount,
                tipAmount = bill.tipAmount,
                totalPaid = bill.grandTotal
            )

            repository.saveOrder(newOrder)
            repository.clearCart()
            lastActiveOrderIdFlow.value = orderId
            checkoutDraftFlow.value = CheckoutDraft()

            if (snapshot.preferences.notificationsEnabled) {
                NotificationHelper.sendOrderStageNotification(
                    getApplication(),
                    newOrder.orderId,
                    OrderStage.CONFIRMED,
                    newOrder.etaMinutes
                )
            }
            onOrderCreated(newOrder)
        }
    }

    fun advanceOrderStage(order: OrderRecord) {
        val next = order.stage.next() ?: return
        val newEtaSeconds = when (next) {
            OrderStage.CONFIRMED -> order.etaMinutes * 60
            OrderStage.PACKING -> (order.etaMinutes * 45).coerceAtLeast(180)
            OrderStage.OUT_FOR_DELIVERY -> 180
            OrderStage.DELIVERED -> 0
        }
        viewModelScope.launch {
            lastActiveOrderIdFlow.value = order.orderId
            repository.updateOrderStage(order.orderId, next, newEtaSeconds)
            if (uiState.value.preferences.notificationsEnabled) {
                NotificationHelper.sendOrderStageNotification(
                    getApplication(),
                    order.orderId,
                    next,
                    (newEtaSeconds / 60).coerceAtLeast(1)
                )
            }
        }
    }

    fun revertOrderStage(order: OrderRecord) {
        val prev = order.stage.previous() ?: return
        val newEtaSeconds = when (prev) {
            OrderStage.CONFIRMED -> order.etaMinutes * 60
            OrderStage.PACKING -> 360
            OrderStage.OUT_FOR_DELIVERY -> 180
            OrderStage.DELIVERED -> 0
        }
        viewModelScope.launch {
            lastActiveOrderIdFlow.value = order.orderId
            repository.updateOrderStage(order.orderId, prev, newEtaSeconds)
        }
    }

    fun toggleAutoSimulate(order: OrderRecord) {
        val enable = !autoSimFlow.value
        autoSimFlow.value = enable
        autoSimJob?.cancel()
        if (enable) {
            lastActiveOrderIdFlow.value = order.orderId
            autoSimJob = viewModelScope.launch {
                while (autoSimFlow.value) {
                    delay(4000L)
                    val current = uiState.value.orders.find { it.orderId == order.orderId }
                    if (current == null || current.stage == OrderStage.DELIVERED) {
                        autoSimFlow.value = false
                        break
                    }
                    advanceOrderStage(current)
                }
            }
        }
    }

    fun submitRating(orderId: String, rating: Int, praiseTags: List<String>, comment: String) {
        viewModelScope.launch {
            repository.submitOrderRating(orderId, rating, praiseTags, comment.trim())
        }
    }

    fun quickReorder(order: OrderRecord) {
        viewModelScope.launch {
            repository.addOrderItemsToCart(order.items, replaceExisting = false)
        }
    }

    fun reorderAndCheckout(order: OrderRecord, onReady: () -> Unit) {
        viewModelScope.launch {
            repository.addOrderItemsToCart(order.items, replaceExisting = true)
            onReady()
        }
    }

    fun updateHistorySearch(query: String) {
        historySearchFlow.value = query
    }

    fun selectHistoryTimeFilter(filter: OrderHistoryTimeFilter) {
        historyTimeFilterFlow.value = filter
    }
}
