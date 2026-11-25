package com.example.merchant.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.merchant.data.AlertType
import com.example.merchant.data.AssignedRider
import com.example.merchant.data.CatalogueItem
import com.example.merchant.data.DocStatus
import com.example.merchant.data.MerchantAlert
import com.example.merchant.data.MerchantProfile
import com.example.merchant.data.MerchantSeed
import com.example.merchant.data.MerchantStorage
import com.example.merchant.data.OrderLine
import com.example.merchant.data.OrderStatus
import com.example.merchant.data.PLATFORM_COMMISSION_PERCENT
import com.example.merchant.data.Payout
import com.example.merchant.data.RiderStage
import com.example.merchant.data.StoreOffer
import com.example.merchant.data.StoreOrder
import com.example.merchant.data.StoreSettings
import com.example.merchant.data.SupportTicket
import com.example.merchant.data.TicketStatus
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar

data class MerchantUiState(
    val isReady: Boolean = false,
    val sessionPhone: String? = null,
    val profile: MerchantProfile = MerchantProfile(),
    val settings: StoreSettings = StoreSettings(),
    val orders: List<StoreOrder> = emptyList(),
    val catalogue: List<CatalogueItem> = emptyList(),
    val offers: List<StoreOffer> = emptyList(),
    val payouts: List<Payout> = emptyList(),
    val alerts: List<MerchantAlert> = emptyList(),
    val tickets: List<SupportTicket> = emptyList(),
    /** NEW order currently shown in the full-screen incoming-order alert. */
    val incomingOrderId: String? = null,
    val nowMillis: Long = System.currentTimeMillis(),
    /** One-off message for the snackbar. */
    val message: String? = null
) {
    val isLoggedIn: Boolean get() = sessionPhone != null
    val activeOrders: List<StoreOrder> get() = orders.filter { it.isActive }
    val unreadAlerts: Int get() = alerts.count { !it.read }
    val incomingOrder: StoreOrder? get() = orders.firstOrNull { it.id == incomingOrderId && it.status == OrderStatus.NEW }

    /** Today = orders placed since local midnight that were not rejected/missed/cancelled. */
    private val todaysOrders: List<StoreOrder>
        get() {
            val midnight = Calendar.getInstance().apply {
                timeInMillis = nowMillis
                set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
            }.timeInMillis
            return orders.filter {
                it.placedAtMillis >= midnight &&
                    it.status !in setOf(OrderStatus.REJECTED, OrderStatus.MISSED, OrderStatus.CANCELLED, OrderStatus.NEW)
            }
        }
    val todayOrderCount: Int get() = todaysOrders.size
    val todayRevenue: Int get() = todaysOrders.sumOf { it.itemTotal - it.refundTotal }
    val avgPrepMinutes: Int
        get() = todaysOrders.map { it.prepMinutes }.takeIf { it.isNotEmpty() }?.average()?.toInt() ?: 0

    /** Settlement for the current (unpaid) cycle, including today's completed orders. */
    val cycleGross: Int
        get() = (payouts.firstOrNull { it.status != com.example.merchant.data.PayoutStatus.PAID }?.grossSales ?: 0) +
            orders.filter { it.status == OrderStatus.DELIVERED && it.placedAtMillis > nowMillis - 6 * 3_600_000L }
                .sumOf { it.itemTotal - it.refundTotal }
    val cycleCommission: Int get() = cycleGross * PLATFORM_COMMISSION_PERCENT / 100
    val cycleNet: Int get() = cycleGross - cycleCommission

    /** Store accepts orders only when switched on and inside its operating hours. */
    val isWithinHours: Boolean
        get() {
            val cal = Calendar.getInstance().apply { timeInMillis = nowMillis }
            val minute = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)
            return if (settings.closeMinuteOfDay > settings.openMinuteOfDay) {
                minute in settings.openMinuteOfDay until settings.closeMinuteOfDay
            } else { // overnight hours, e.g. 18:00 – 02:00
                minute >= settings.openMinuteOfDay || minute < settings.closeMinuteOfDay
            }
        }
    val isAcceptingOrders: Boolean get() = profile.kycVerified && settings.isOpen && isWithinHours
}

class MerchantViewModel(application: Application) : AndroidViewModel(application) {

    private val storage = MerchantStorage(application)
    private val _state = MutableStateFlow(MerchantUiState())
    val state: StateFlow<MerchantUiState> = _state.asStateFlow()

    private var simulatedSeq = 0
    private var lastAutoOrderAt = 0L

    init {
        val now = System.currentTimeMillis()
        val phone = storage.loggedInPhone
        _state.value = MerchantUiState(
            isReady = true,
            sessionPhone = phone,
            profile = storage.loadProfile() ?: MerchantProfile(),
            settings = storage.loadSettings(),
            orders = MerchantSeed.orders(now),
            catalogue = storage.loadCatalogue() ?: MerchantSeed.catalogue(),
            offers = storage.loadOffers() ?: MerchantSeed.offers(now),
            payouts = MerchantSeed.payouts(now),
            alerts = MerchantSeed.alerts(now),
            tickets = storage.loadTickets() ?: MerchantSeed.tickets(now),
            nowMillis = now
        )
        lastAutoOrderAt = now
        startTicker()
    }

    fun consumeMessage() = _state.update { it.copy(message = null) }
    private fun toast(msg: String) = _state.update { it.copy(message = msg) }

    // ── Auth ──────────────────────────────────────────────────────────────────────────────────

    /** Demo OTP is 1234. Returns false for a wrong code. */
    fun verifyOtp(phone: String, otp: String): Boolean {
        if (otp != DEMO_OTP) return false
        storage.loggedInPhone = phone
        val saved = storage.loadProfile()
        // A different phone number is treated as a new store that must complete KYC.
        val profile = if (saved != null && saved.phone == phone) saved else MerchantProfile(phone = phone)
        storage.saveProfile(profile)
        _state.update { it.copy(sessionPhone = phone, profile = profile) }
        return true
    }

    fun signOut() {
        storage.signOut()
        _state.update { it.copy(sessionPhone = null, incomingOrderId = null) }
    }

    // ── KYC & profile ─────────────────────────────────────────────────────────────────────────

    fun updateProfile(transform: (MerchantProfile) -> MerchantProfile) {
        val updated = transform(_state.value.profile)
        storage.saveProfile(updated)
        _state.update { it.copy(profile = updated) }
    }

    /** Simulated document upload: pending, then verified after a short review. */
    fun uploadDocument(isFssai: Boolean) {
        updateProfile { if (isFssai) it.copy(fssaiStatus = DocStatus.PENDING) else it.copy(gstinStatus = DocStatus.PENDING) }
        viewModelScope.launch {
            delay(2_500)
            updateProfile { if (isFssai) it.copy(fssaiStatus = DocStatus.VERIFIED) else it.copy(gstinStatus = DocStatus.VERIFIED) }
        }
    }

    fun submitKyc() {
        updateProfile { it.copy(kycSubmitted = true) }
        toast(if (_state.value.profile.kycVerified) "Store verified. You can start taking orders." else "KYC submitted. We'll notify you once documents are verified.")
    }

    // ── Store status & settings ───────────────────────────────────────────────────────────────

    fun updateSettings(transform: (StoreSettings) -> StoreSettings) {
        val updated = transform(_state.value.settings)
        storage.saveSettings(updated)
        _state.update { it.copy(settings = updated) }
    }

    fun setStoreOpen(open: Boolean) {
        updateSettings { it.copy(isOpen = open) }
        toast(if (open) "Store is open for orders" else "Store closed. Active orders can still be completed.")
    }

    // ── Orders ────────────────────────────────────────────────────────────────────────────────

    /** Demo: push a new incoming order (also happens automatically while the store is open). */
    fun simulateIncomingOrder() {
        val s = _state.value
        when {
            !s.profile.kycVerified -> { toast("Complete store verification to receive orders"); return }
            !s.settings.isOpen -> { toast("Open your store to receive orders"); return }
            s.incomingOrder != null -> { toast("Respond to the current order first"); return }
        }
        val order = MerchantSeed.simulatedOrder(simulatedSeq++, System.currentTimeMillis(), s.catalogue)
        lastAutoOrderAt = order.placedAtMillis
        if (s.settings.autoAcceptOrders) {
            _state.update { it.copy(orders = listOf(order) + it.orders) }
            acceptOrder(order.id, prepMinutes = 15)
            addAlert(AlertType.NEW_ORDER, "Order ${order.shortId} auto-accepted", "${order.itemCount} items for ${order.customerName}", order.id)
            return
        }
        _state.update { it.copy(orders = listOf(order) + it.orders, incomingOrderId = order.id) }
        addAlert(AlertType.NEW_ORDER, "New order ${order.shortId}", "${order.itemCount} items · ₹${order.payableTotal} · ${order.paymentMode.label}", order.id)
    }

    fun dismissIncomingAlert() = _state.update { it.copy(incomingOrderId = null) }
    fun openIncomingAlert(orderId: String) = _state.update { it.copy(incomingOrderId = orderId) }

    fun acceptOrder(orderId: String, prepMinutes: Int) {
        val order = _state.value.orders.firstOrNull { it.id == orderId } ?: return
        if (order.status != OrderStatus.NEW) { toast("This order is no longer waiting"); return }
        updateOrder(orderId) { it.copy(status = OrderStatus.PREPARING, prepMinutes = prepMinutes, acceptedAtMillis = System.currentTimeMillis()) }
        consumeStock(order.lines)
        _state.update { it.copy(incomingOrderId = null) }
        toast("Order ${order.shortId} accepted · ready in $prepMinutes min")
    }

    /** Accept with some items short or substituted; the difference is refunded to the customer. */
    fun confirmPartialOrder(orderId: String, lines: List<OrderLine>, prepMinutes: Int) {
        val order = _state.value.orders.firstOrNull { it.id == orderId } ?: return
        if (order.status != OrderStatus.NEW) { toast("This order is no longer waiting"); return }
        val updated = order.copy(lines = lines)
        updateOrder(orderId) {
            updated.copy(status = OrderStatus.PREPARING, prepMinutes = prepMinutes, acceptedAtMillis = System.currentTimeMillis(), isPartial = true)
        }
        consumeStock(lines.map { it.copy(quantity = it.fulfilledQuantity) })
        // Items reported unavailable are marked out of stock so the next customer can't order them.
        lines.filter { it.fulfilledQuantity == 0 && it.substituteName == null }.forEach { line ->
            mutateCatalogue { list -> list.map { if (it.id == line.productId) it.copy(stock = 0) else it } }
        }
        _state.update { it.copy(incomingOrderId = null) }
        val refund = updated.refundTotal
        toast(if (refund > 0) "Partial order accepted · ₹$refund refund sent to customer" else "Order accepted with substitutes")
    }

    fun rejectOrder(orderId: String, reason: String) {
        updateOrder(orderId) { it.copy(status = OrderStatus.REJECTED, rejectReason = reason) }
        _state.update { it.copy(incomingOrderId = null) }
        toast("Order rejected · customer will get a full refund")
    }

    fun markReady(orderId: String) {
        updateOrder(orderId) {
            it.copy(
                status = OrderStatus.READY,
                rider = it.rider ?: AssignedRider("Finding rider…", "", "", 0f, RiderStage.ASSIGNING, 0)
            )
        }
        val order = _state.value.orders.first { it.id == orderId }
        if (order.rider?.stage == RiderStage.ASSIGNING) {
            viewModelScope.launch {
                delay(3_000)
                val riders = listOf(
                    AssignedRider("Ravi K.", "Bike TS09 EK 4821", "9000012345", 4.8f, RiderStage.ON_THE_WAY, 2),
                    AssignedRider("Suresh B.", "Scooter TS07 FA 1290", "9000067890", 4.6f, RiderStage.ON_THE_WAY, 2),
                    AssignedRider("Naveen P.", "EV Bike TS08 GH 5532", "9000045678", 4.9f, RiderStage.ON_THE_WAY, 2)
                )
                updateOrder(orderId) { o -> if (o.rider?.stage == RiderStage.ASSIGNING) o.copy(rider = riders[orderId.hashCode().mod(riders.size)]) else o }
            }
        }
        toast("Marked ready · assigning a rider")
    }

    fun togglePacked(orderId: String, productId: String) = updateOrder(orderId) {
        it.copy(packedLineIds = if (productId in it.packedLineIds) it.packedLineIds - productId else it.packedLineIds + productId)
    }

    fun setBillAttached(orderId: String, value: Boolean) = updateOrder(orderId) { it.copy(billAttached = value) }
    fun setBagSealed(orderId: String, value: Boolean) = updateOrder(orderId) { it.copy(bagSealed = value) }

    fun confirmHandover(orderId: String) {
        val order = _state.value.orders.firstOrNull { it.id == orderId } ?: return
        if (order.rider?.stage != RiderStage.AT_STORE) { toast("Wait for the rider to reach your store"); return }
        updateOrder(orderId) { it.copy(status = OrderStatus.HANDED_OVER, acceptedAtMillis = it.acceptedAtMillis, placedAtMillis = it.placedAtMillis) }
        handedOverAt[orderId] = System.currentTimeMillis()
        toast("Handed over to ${order.rider.name}")
    }

    /** Demo: the customer cancels an order that is still being prepared. */
    fun simulateCustomerCancel(orderId: String) {
        val order = _state.value.orders.firstOrNull { it.id == orderId } ?: return
        if (order.status !in setOf(OrderStatus.NEW, OrderStatus.PREPARING)) return
        updateOrder(orderId) { it.copy(status = OrderStatus.CANCELLED, rejectReason = "Customer cancelled") }
        restoreStock(order.lines.map { it.copy(quantity = it.fulfilledQuantity) }, onlyIfAccepted = order.status == OrderStatus.PREPARING)
        if (_state.value.incomingOrderId == orderId) _state.update { it.copy(incomingOrderId = null) }
        addAlert(AlertType.ORDER_CANCELLED, "Order ${order.shortId} cancelled", "The customer cancelled. Put the items back on the shelf.", orderId)
    }

    private val handedOverAt = mutableMapOf<String, Long>()

    private fun updateOrder(orderId: String, transform: (StoreOrder) -> StoreOrder) =
        _state.update { s -> s.copy(orders = s.orders.map { if (it.id == orderId) transform(it) else it }) }

    private fun consumeStock(lines: List<OrderLine>) {
        mutateCatalogue { list ->
            list.map { item ->
                val used = lines.filter { it.productId == item.id }.sumOf { it.quantity }
                if (used == 0) item else item.copy(stock = (item.stock - used).coerceAtLeast(0))
            }
        }
        _state.value.catalogue.filter { item -> lines.any { it.productId == item.id } && (item.isLowStock || item.isOutOfStock) }
            .forEach { addAlert(AlertType.LOW_STOCK, if (it.isOutOfStock) "Out of stock: ${it.name}" else "Low stock: ${it.name}", "Only ${it.stock} left (${it.unit}). Restock or update the catalogue.") }
    }

    private fun restoreStock(lines: List<OrderLine>, onlyIfAccepted: Boolean) {
        if (!onlyIfAccepted) return
        mutateCatalogue { list ->
            list.map { item ->
                val back = lines.filter { it.productId == item.id }.sumOf { it.quantity }
                if (back == 0) item else item.copy(stock = item.stock + back)
            }
        }
    }

    // ── Catalogue ─────────────────────────────────────────────────────────────────────────────

    private fun mutateCatalogue(transform: (List<CatalogueItem>) -> List<CatalogueItem>) {
        val updated = transform(_state.value.catalogue)
        storage.saveCatalogue(updated)
        _state.update { it.copy(catalogue = updated) }
    }

    fun setStock(itemId: String, stock: Int) = mutateCatalogue { list -> list.map { if (it.id == itemId) it.copy(stock = stock.coerceAtLeast(0)) else it } }

    fun saveProduct(item: CatalogueItem) {
        val exists = _state.value.catalogue.any { it.id == item.id }
        mutateCatalogue { list -> if (exists) list.map { if (it.id == item.id) item else it } else listOf(item) + list }
        toast(if (exists) "${item.name} updated" else "${item.name} added to catalogue")
    }

    /** Products in an active order can't be deleted (they would vanish from the packing list). */
    fun deleteProduct(itemId: String): Boolean {
        if (_state.value.activeOrders.any { o -> o.lines.any { it.productId == itemId } }) {
            toast("This item is in an active order. Mark it inactive instead.")
            return false
        }
        val name = _state.value.catalogue.firstOrNull { it.id == itemId }?.name
        mutateCatalogue { list -> list.filterNot { it.id == itemId } }
        toast("${name ?: "Item"} deleted")
        return true
    }

    // ── Offers ────────────────────────────────────────────────────────────────────────────────

    private fun mutateOffers(transform: (List<StoreOffer>) -> List<StoreOffer>) {
        val updated = transform(_state.value.offers)
        storage.saveOffers(updated)
        _state.update { it.copy(offers = updated) }
    }

    fun setOfferActive(offerId: String, active: Boolean) {
        val offer = _state.value.offers.firstOrNull { it.id == offerId } ?: return
        if (active && offer.isExpired(System.currentTimeMillis())) { toast("This offer has expired. Create a new one."); return }
        mutateOffers { list -> list.map { if (it.id == offerId) it.copy(active = active) else it } }
    }

    fun createOffer(offer: StoreOffer) {
        mutateOffers { listOf(offer) + it }
        toast("Offer created: ${offer.title}")
    }

    fun deleteOffer(offerId: String) = mutateOffers { list -> list.filterNot { it.id == offerId } }

    // ── Alerts & support ──────────────────────────────────────────────────────────────────────

    private fun addAlert(type: AlertType, title: String, body: String, orderId: String? = null) {
        val alert = MerchantAlert("a${System.nanoTime()}", type, title, body, System.currentTimeMillis(), orderId = orderId)
        _state.update { it.copy(alerts = listOf(alert) + it.alerts) }
    }

    fun markAlertRead(id: String) = _state.update { s -> s.copy(alerts = s.alerts.map { if (it.id == id) it.copy(read = true) else it }) }
    fun markAllAlertsRead() = _state.update { s -> s.copy(alerts = s.alerts.map { it.copy(read = true) }) }

    fun raiseTicket(category: String, subject: String, description: String, orderId: String?) {
        val ticket = SupportTicket("T${1043 + _state.value.tickets.size}", category, subject.trim(), description.trim(), orderId, System.currentTimeMillis(), TicketStatus.OPEN)
        val updated = listOf(ticket) + _state.value.tickets
        storage.saveTickets(updated)
        _state.update { it.copy(tickets = updated) }
        toast("Ticket ${ticket.id} raised · we usually reply within 2 hours")
    }

    // ── Ticker: timers for missed orders, rider arrival, delivery, auto orders ────────────────

    private fun startTicker() {
        viewModelScope.launch {
            while (true) {
                delay(1_000)
                val now = System.currentTimeMillis()
                _state.update { it.copy(nowMillis = now) }
                val s = _state.value
                if (!s.isLoggedIn) continue

                // 1) Unanswered orders are missed after the response window.
                s.orders.filter { it.status == OrderStatus.NEW && now - it.placedAtMillis > ACCEPT_WINDOW_MS }.forEach { o ->
                    updateOrder(o.id) { it.copy(status = OrderStatus.MISSED, rejectReason = "Not accepted in time") }
                    if (_state.value.incomingOrderId == o.id) _state.update { it.copy(incomingOrderId = null) }
                    addAlert(AlertType.SYSTEM, "Missed order ${o.shortId}", "It was not accepted within 60 seconds and has been reassigned.", o.id)
                }

                // 2) Rider travel to store (one step every ~20 s).
                if (now % 20_000 < 1_000) {
                    s.orders.filter { it.status == OrderStatus.READY && it.rider?.stage == RiderStage.ON_THE_WAY }.forEach { o ->
                        val r = o.rider!!
                        if (r.etaMinutes <= 1) {
                            updateOrder(o.id) { it.copy(rider = r.copy(stage = RiderStage.AT_STORE, etaMinutes = 0)) }
                            addAlert(AlertType.RIDER_ARRIVED, "Rider arrived – Order ${o.shortId}", "${r.name} is at your store. Hand over the packed order.", o.id)
                        } else {
                            updateOrder(o.id) { it.copy(rider = r.copy(etaMinutes = r.etaMinutes - 1)) }
                        }
                    }
                }

                // 3) Handed-over orders are delivered after ~45 s (prototype timeline).
                handedOverAt.entries.toList().forEach { (id, at) ->
                    if (now - at > 45_000) {
                        handedOverAt.remove(id)
                        updateOrder(id) { it.copy(status = OrderStatus.DELIVERED, customerRating = 4 + (id.hashCode() and 1)) }
                    }
                }

                // 4) Auto incoming orders while open (one every ~90 s when nothing is waiting).
                if (s.isAcceptingOrders && s.incomingOrder == null && s.orders.none { it.status == OrderStatus.NEW } &&
                    now - lastAutoOrderAt > AUTO_ORDER_INTERVAL_MS
                ) {
                    simulateIncomingOrder()
                }
            }
        }
    }

    companion object {
        const val DEMO_OTP = "1234"
        const val ACCEPT_WINDOW_MS = 60_000L
        const val AUTO_ORDER_INTERVAL_MS = 90_000L
    }
}
