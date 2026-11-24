package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.PreferencesManager
import com.example.data.local.QuickKartDatabase
import com.example.data.local.SeedData
import com.example.data.local.UserPreferences
import com.example.data.model.BillBreakdown
import com.example.data.model.CartItem
import com.example.data.model.DeliverySlot
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
import com.example.data.model.SupportIssueCategory
import com.example.data.model.SupportResolutionOption
import com.example.data.model.SupportTicketRecord
import com.example.data.model.SupportTicketStatus
import com.example.data.model.WalletTransaction
import com.example.data.model.filterCatalogProducts
import com.example.data.repository.QuickKartRepository
import com.example.util.NotificationHelper
import com.example.util.elapsedFraction
import com.example.util.stageForElapsed
import com.example.util.cancellationRefundInr
import com.example.util.cardLast4ToStore
import com.example.util.isCancellable
import com.example.util.nearestLockedOffer
import com.example.util.pickBestOffer
import com.example.util.referralCodeError
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

enum class ThemeMode(val label: String) { LIGHT("Light"), DARK("Dark"), SYSTEM("System") }

const val REFERRAL_REWARD_INR = 50

/** Stable, shareable referral code derived from the user's phone (prototype). */
fun referralCodeFor(phone: String): String {
    val digits = phone.filter { it.isDigit() }.takeLast(4).ifBlank { "0000" }
    return "QK$digits${if (digits.sum() % 2 == 0) "FR" else "GO"}"
}

private fun String.sum(): Int = sumOf { it.digitToIntOrNull() ?: 0 }

enum class BottomTab(val route: String, val label: String) {
    HOME("home", "Home"),
    CATEGORIES("categories", "Categories"),
    CART("cart", "Cart"),
    TRACK("track", "Track"),
    ORDERS("orders", "Orders"),
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
    val paymentMethod: PaymentMethod = PaymentMethod.UPI,
    val selectedDeliverySlot: DeliverySlot = SeedData.deliverySlots.first()
)

data class QuickKartUiState(
    val isReady: Boolean = false,
    val preferences: UserPreferences = UserPreferences(),
    val neighborhoods: List<Neighborhood> = emptyList(),
    val selectedNeighborhood: Neighborhood? = null,
    val products: List<Product> = emptyList(),
    val selectedCategory: ProductCategory = ProductCategory.ALL,
    val favoritesOnly: Boolean = false,
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
    val autoSimulateTracking: Boolean = false,
    val supportTickets: List<SupportTicketRecord> = emptyList(),
    val walletTransactions: List<WalletTransaction> = emptyList()
) {
    val totalCartCount: Int get() = cartItems.sumOf { it.quantity }
    val openSupportTicketCount: Int
        get() = supportTickets.count { it.status != SupportTicketStatus.RESOLVED }

    val walletBalance: Int
        get() = walletTransactions.fold(0) { acc, tx ->
            if (tx.isCredit) acc + tx.amountInr else acc - tx.amountInr
        }

    val favoriteProducts: List<Product>
        get() = products.filter { it.id in favoriteIds }

    val filteredProducts: List<Product>
        get() = filterCatalogProducts(
            products = products,
            selectedCategory = selectedCategory,
            searchQuery = searchQuery,
            favoritesOnly = favoritesOnly,
            favoriteIds = favoriteIds
        )

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
    private val favoritesOnlyFlow = MutableStateFlow(false)
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
    private var lastNotifiedOrderId: String? = null

    init {
        val db = QuickKartDatabase.getInstance(application)
        val prefs = PreferencesManager(application)
        repository = QuickKartRepository(
            favoriteDao = db.favoriteDao(),
            cartDao = db.cartDao(),
            addressDao = db.savedAddressDao(),
            orderDao = db.orderDao(),
            supportTicketDao = db.supportTicketDao(),
            walletDao = db.walletDao(),
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
        val orders: List<OrderRecord>,
        val supportTickets: List<SupportTicketRecord>,
        val walletTransactions: List<WalletTransaction>
    )

    private data class FilterStreams(
        val category: ProductCategory,
        val favoritesOnly: Boolean,
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
        CoreStreams(prefs, favs, cart, addrs, orders, emptyList(), emptyList())
    }.combine(repository.supportTickets) { core, tickets ->
        core.copy(supportTickets = tickets)
    }.combine(repository.walletTransactions) { core, wallet ->
        core.copy(walletTransactions = wallet)
    }

    private val catalogFilterFlow = combine(
        selectedCategoryFlow,
        favoritesOnlyFlow,
        searchQueryFlow,
        isVoiceListeningFlow
    ) { cat, favOnly, search, listening ->
        CatalogFilterState(cat, favOnly, search, listening)
    }

    private data class CatalogFilterState(
        val category: ProductCategory,
        val favoritesOnly: Boolean,
        val search: String,
        val listening: Boolean
    )

    private val filterFlow = combine(
        catalogFilterFlow,
        historySearchFlow,
        historyTimeFilterFlow,
        checkoutDraftFlow,
        autoSimFlow
    ) { catalog, histSearch, histFilter, draft, sim ->
        FilterStreams(
            category = catalog.category,
            favoritesOnly = catalog.favoritesOnly,
            search = catalog.search,
            listening = catalog.listening,
            historySearch = histSearch,
            historyFilter = histFilter,
            checkout = draft,
            autoSim = sim,
            ready = true,
            lastTrackedOrderId = null
        )
    }.combine(
        combine(isReadyFlow, lastActiveOrderIdFlow) { ready, lastId -> ready to lastId }
    ) { streams, (ready, lastId) ->
        streams.copy(ready = ready, lastTrackedOrderId = lastId)
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
            favoritesOnly = filters.favoritesOnly,
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
            autoSimulateTracking = filters.autoSim,
            supportTickets = core.supportTickets,
            walletTransactions = core.walletTransactions
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
                val snapshot = uiState.value
                val active = snapshot.activeOrder ?: continue
                val nextRemaining = (active.remainingSeconds - 1).coerceAtLeast(0)
                // Prototype timeline: move the order to the stage it should be in by now
                // (Confirmed -> Packing -> Out for delivery -> Delivered). Never moves backwards,
                // so the demo controls can still jump ahead.
                val timelineStage = stageForElapsed(
                    elapsedFraction(nextRemaining, active.etaMinutes * 60),
                    nextRemaining
                )
                if (timelineStage.stepIndex > active.stage.stepIndex) {
                    lastNotifiedOrderId = active.orderId
                    repository.updateOrderStage(active.orderId, timelineStage, nextRemaining)
                    if (snapshot.preferences.notificationsEnabled) {
                        NotificationHelper.sendOrderStageNotification(
                            context = getApplication(),
                            orderId = active.orderId,
                            stage = timelineStage,
                            etaMinutes = active.etaMinutes,
                            remainingSeconds = nextRemaining,
                            totalEtaMinutes = active.etaMinutes,
                            deliveryWindowLabel = active.deliveryWindowLabel,
                            silentUpdate = false
                        )
                    }
                    continue
                }
                if (active.remainingSeconds > 0) {
                    repository.updateOrderRemainingSeconds(active.orderId, nextRemaining)
                }
                val isFirstNotificationForOrder = lastNotifiedOrderId != active.orderId
                val shouldRefreshNotification = isFirstNotificationForOrder || (nextRemaining % 10 == 0)
                if (snapshot.preferences.notificationsEnabled && shouldRefreshNotification) {
                    lastNotifiedOrderId = active.orderId
                    NotificationHelper.sendOrderStageNotification(
                        context = getApplication(),
                        orderId = active.orderId,
                        stage = active.stage,
                        etaMinutes = active.etaMinutes,
                        remainingSeconds = nextRemaining,
                        totalEtaMinutes = active.etaMinutes,
                        deliveryWindowLabel = active.deliveryWindowLabel,
                        silentUpdate = true
                    )
                }
            }
        }
    }

    fun toggleDarkTheme(enabled: Boolean) {
        viewModelScope.launch { repository.setDarkTheme(enabled) }
    }

    /** Light, Dark or follow the phone's setting. */
    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            when (mode) {
                ThemeMode.LIGHT -> repository.setDarkTheme(false)
                ThemeMode.DARK -> repository.setDarkTheme(true)
                ThemeMode.SYSTEM -> repository.setFollowSystemTheme()
            }
        }
    }

    fun toggleNotifications(enabled: Boolean) {
        viewModelScope.launch { repository.setNotificationsEnabled(enabled) }
    }

    fun setScreenZoomPercent(percent: Int) {
        viewModelScope.launch { repository.setScreenZoomPercent(percent) }
    }

    fun selectNeighborhood(neighborhoodId: String) {
        viewModelScope.launch { repository.setSelectedNeighborhood(neighborhoodId) }
    }

    fun signInWithPhone(
        phone: String,
        neighborhoodId: String,
        firstName: String = "Aarav",
        lastName: String = "",
        email: String = "",
        googleEmail: String = ""
    ) {
        val fullName = if (lastName.isNotBlank()) "${firstName.trim()} ${lastName.trim()}" else firstName.trim()
        viewModelScope.launch {
            repository.signInWithPhone(phone, neighborhoodId, fullName, email.trim(), googleEmail)
        }
    }

    fun linkGoogleAccount(googleEmail: String) {
        viewModelScope.launch { repository.linkGoogleAccount(googleEmail) }
    }

    fun unlinkGoogleAccount() {
        viewModelScope.launch { repository.unlinkGoogleAccount() }
    }

    fun updateUserProfile(firstName: String, lastName: String, email: String, phone: String? = null) {
        val fullName = if (lastName.isNotBlank()) "${firstName.trim()} ${lastName.trim()}" else firstName.trim()
        viewModelScope.launch {
            repository.updateUserProfile(fullName, email.trim(), phone)
        }
    }

    /** Credit the wallet. Call only after the top-up payment has succeeded. */
    fun addWalletMoney(amountInr: Int, methodNote: String) {
        viewModelScope.launch {
            repository.addMoneyToWallet(amountInr, methodNote)
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

    fun toggleFavoritesFilter() {
        favoritesOnlyFlow.update { !it }
    }

    fun setFavoritesOnly(enabled: Boolean) {
        favoritesOnlyFlow.value = enabled
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

    fun quickAddAllFavorites(onComplete: (Int) -> Unit = {}) {
        val favs = uiState.value.favoriteProducts
        if (favs.isEmpty()) {
            onComplete(0)
            return
        }
        viewModelScope.launch {
            repository.quickAddProducts(favs)
            onComplete(favs.size)
        }
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

    fun addAddress(
        label: String,
        fullAddress: String,
        landmark: String,
        neighborhoodId: String,
        area: String = "",
        city: String = "",
        state: String = "",
        postalCode: String = "",
        country: String = "India",
        latitude: Double? = null,
        longitude: Double? = null
    ) {
        viewModelScope.launch {
            val newAddr = SavedAddress(
                id = "addr_${System.currentTimeMillis()}",
                label = label,
                fullAddress = fullAddress.trim(),
                landmark = landmark.trim(),
                neighborhoodId = neighborhoodId,
                isDefault = true,
                area = area.trim(),
                city = city.trim(),
                state = state.trim(),
                postalCode = postalCode.trim(),
                country = country.trim().ifBlank { "India" },
                latitude = latitude,
                longitude = longitude
            )
            repository.addAddress(newAddr)
            repository.setSelectedNeighborhood(neighborhoodId)
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

    /** Applies the eligible offer with the biggest discount for the current basket. */
    fun applyBestOffer() {
        val itemTotal = uiState.value.billBreakdown.itemTotal
        val best = pickBestOffer(repository.availablePromos, itemTotal)
        if (best == null) {
            val nearest = nearestLockedOffer(repository.availablePromos, itemTotal)
            checkoutDraftFlow.update {
                it.copy(
                    appliedPromo = null,
                    promoMessage = nearest?.let { p ->
                        "No offer unlocked yet. Add ₹${p.minOrderInr - itemTotal} more for ${p.code}."
                    } ?: "No offers available right now."
                )
            }
        } else {
            applyPromoCode(best.code)
        }
    }

    val availablePromos get() = repository.availablePromos

    fun addRecentSearch(query: String) {
        viewModelScope.launch { repository.addRecentSearch(query) }
    }

    fun clearRecentSearches() {
        viewModelScope.launch { repository.clearRecentSearches() }
    }

    fun markInboxSeen() {
        viewModelScope.launch { repository.markInboxSeen(System.currentTimeMillis()) }
    }

    fun savePaymentMethod(upiId: String?, cardLast4: String?) {
        // Defensive: never persist more than a card's last 4 digits.
        val safeLast4 = cardLast4?.let { cardLast4ToStore(it) }
        viewModelScope.launch { repository.savePaymentMethod(upiId?.trim(), safeLast4) }
    }

    fun removeSavedPaymentMethods() {
        viewModelScope.launch { repository.removeSavedPaymentMethods() }
    }

    /**
     * Prototype referral: any code starting with "QK" (not the user's own) credits ₹50 once.
     * Returns a message for the UI.
     */
    fun redeemReferralCode(code: String, ownCode: String): String {
        val clean = code.trim().uppercase()
        val error = referralCodeError(clean, ownCode, uiState.value.preferences.referralRedeemed)
        return when {
            error != null -> error
            else -> {
                viewModelScope.launch {
                    repository.creditWallet("Referral Reward", "Code $clean • Welcome reward", REFERRAL_REWARD_INR)
                    repository.setReferralRedeemed()
                }
                "₹$REFERRAL_REWARD_INR added to your Wallet!"
            }
        }
    }

    /** Cancels an order that is still CONFIRMED; prepaid amounts are refunded to the Wallet. */
    fun cancelOrder(order: OrderRecord, onDone: (String) -> Unit) {
        if (!isCancellable(order)) {
            onDone("This order is already being packed and can't be cancelled.")
            return
        }
        viewModelScope.launch {
            autoSimFlow.value = false
            repository.deleteOrder(order.orderId)
            val refundInr = cancellationRefundInr(order)
            val refunded = refundInr > 0
            if (refunded) {
                repository.creditWallet(
                    "Refund for #${order.orderId}",
                    "Order cancelled • Instant refund to Wallet",
                    refundInr
                )
            }
            if (lastActiveOrderIdFlow.value == order.orderId) lastActiveOrderIdFlow.value = null
            onDone(
                if (refunded) "Order ${order.orderId} cancelled. ₹$refundInr refunded to Wallet."
                else "Order ${order.orderId} cancelled."
            )
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

    fun selectDeliverySlot(slot: DeliverySlot) {
        checkoutDraftFlow.update { it.copy(selectedDeliverySlot = slot) }
    }

    fun placeOrder(onOrderCreated: (OrderRecord) -> Unit) {
        val snapshot = uiState.value
        if (snapshot.cartItems.isEmpty()) return
        if (snapshot.checkoutDraft.paymentMethod == PaymentMethod.WALLET &&
            snapshot.walletBalance < snapshot.billBreakdown.grandTotal
        ) {
            return
        }

        viewModelScope.launch {
            val neighborhood = snapshot.selectedNeighborhood ?: repository.neighborhoods.first()
            val address = snapshot.selectedAddress ?: snapshot.savedAddresses.firstOrNull()
            val draft = snapshot.checkoutDraft
            val bill = snapshot.billBreakdown
            val slot = draft.selectedDeliverySlot
            val etaMinutes = if (slot.isExpress) neighborhood.etaMinutes else slot.etaMinutes
            val windowLabel = if (slot.isExpress) {
                "Express • ${neighborhood.etaMinutes} mins"
            } else {
                slot.displaySummary
            }

            val orderId = "QK-${Random.nextInt(85000, 99999)}"
            val newOrder = OrderRecord(
                orderId = orderId,
                createdAtMillis = System.currentTimeMillis(),
                neighborhoodName = neighborhood.displayTitle,
                etaMinutes = etaMinutes,
                remainingSeconds = etaMinutes * 60,
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
                deliveryAddressFull = if (address != null) {
                    listOf(address.fullAddress, address.formattedLocationLine)
                        .filter { it.isNotBlank() }
                        .joinToString(", ")
                } else {
                    "${neighborhood.name}, ${neighborhood.city}"
                },
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
                totalPaid = bill.grandTotal,
                deliveryWindowLabel = windowLabel
            )

            repository.saveOrder(newOrder)
            if (draft.paymentMethod == PaymentMethod.WALLET) {
                repository.debitWallet(bill.grandTotal, orderId)
            }
            repository.clearCart()
            lastActiveOrderIdFlow.value = orderId
            lastNotifiedOrderId = orderId
            checkoutDraftFlow.value = CheckoutDraft()

            if (snapshot.preferences.notificationsEnabled) {
                NotificationHelper.sendOrderStageNotification(
                    context = getApplication(),
                    orderId = newOrder.orderId,
                    stage = OrderStage.CONFIRMED,
                    etaMinutes = newOrder.etaMinutes,
                    remainingSeconds = newOrder.remainingSeconds,
                    totalEtaMinutes = newOrder.etaMinutes,
                    deliveryWindowLabel = newOrder.deliveryWindowLabel,
                    silentUpdate = false
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
            lastNotifiedOrderId = order.orderId
            repository.updateOrderStage(order.orderId, next, newEtaSeconds)
            if (uiState.value.preferences.notificationsEnabled) {
                NotificationHelper.sendOrderStageNotification(
                    context = getApplication(),
                    orderId = order.orderId,
                    stage = next,
                    etaMinutes = (newEtaSeconds / 60).coerceAtLeast(1),
                    remainingSeconds = newEtaSeconds,
                    totalEtaMinutes = order.etaMinutes,
                    deliveryWindowLabel = order.deliveryWindowLabel,
                    silentUpdate = false
                )
            }
        }
    }

    fun revertOrderStage(order: OrderRecord) {
        val prev = order.stage.previous() ?: return
        val newEtaSeconds = when (prev) {
            OrderStage.CONFIRMED -> order.etaMinutes * 60
            OrderStage.PACKING -> (order.etaMinutes * 45).coerceAtLeast(180)
            OrderStage.OUT_FOR_DELIVERY -> 180
            OrderStage.DELIVERED -> 0
        }
        viewModelScope.launch {
            lastActiveOrderIdFlow.value = order.orderId
            lastNotifiedOrderId = order.orderId
            repository.updateOrderStage(order.orderId, prev, newEtaSeconds)
            if (uiState.value.preferences.notificationsEnabled) {
                NotificationHelper.sendOrderStageNotification(
                    context = getApplication(),
                    orderId = order.orderId,
                    stage = prev,
                    etaMinutes = (newEtaSeconds / 60).coerceAtLeast(1),
                    remainingSeconds = newEtaSeconds,
                    totalEtaMinutes = order.etaMinutes,
                    deliveryWindowLabel = order.deliveryWindowLabel,
                    silentUpdate = true
                )
            }
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

    fun createSupportTicket(
        order: OrderRecord,
        category: SupportIssueCategory,
        selectedItemKeys: Set<String>,
        preferredResolution: SupportResolutionOption,
        userNote: String,
        photoUriString: String?,
        onCreated: (SupportTicketRecord) -> Unit
    ) {
        viewModelScope.launch {
            val ticket = repository.createSupportTicket(
                order = order,
                category = category,
                selectedItemKeys = selectedItemKeys,
                preferredResolution = preferredResolution,
                userNote = userNote,
                photoUriString = photoUriString
            )
            onCreated(ticket)
        }
    }

    fun sendSupportMessage(ticketId: String, message: String) {
        viewModelScope.launch {
            repository.sendSupportMessage(ticketId, message)
        }
    }

    fun resolveSupportTicket(ticketId: String, resolutionSummary: String? = null) {
        viewModelScope.launch {
            repository.resolveSupportTicket(ticketId, resolutionSummary)
        }
    }

    fun reopenSupportTicket(ticketId: String) {
        viewModelScope.launch {
            repository.reopenSupportTicket(ticketId)
        }
    }

    fun submitSupportCsat(ticketId: String, rating: Int) {
        viewModelScope.launch {
            repository.submitSupportCsat(ticketId, rating)
        }
    }
}
