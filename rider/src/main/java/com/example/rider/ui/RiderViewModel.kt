package com.example.rider.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.rider.data.ASSIGNMENT_WINDOW_MS
import com.example.rider.data.CashEntry
import com.example.rider.data.CashEntryType
import com.example.rider.data.DocStatus
import com.example.rider.data.EarningTx
import com.example.rider.data.FLOATING_CASH_LIMIT
import com.example.rider.data.GeoPoint
import com.example.rider.data.INSTANT_PAYOUT_MIN
import com.example.rider.data.KycDoc
import com.example.rider.data.PaymentMode
import com.example.rider.data.RiderProfile
import com.example.rider.data.RiderSeed
import com.example.rider.data.RiderSettings
import com.example.rider.data.RiderStorage
import com.example.rider.data.StoreReadiness
import com.example.rider.data.Trip
import com.example.rider.data.TripOffer
import com.example.rider.data.TripStage
import com.example.rider.data.TxType
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar

data class RiderUiState(
    val isReady: Boolean = false,
    val sessionPhone: String? = null,
    val profile: RiderProfile = RiderProfile(),
    val settings: RiderSettings = RiderSettings(),
    val isOnline: Boolean = false,
    val offer: TripOffer? = null,
    val activeTrip: Trip? = null,
    val history: List<Trip> = emptyList(),
    val cash: List<CashEntry> = emptyList(),
    val payoutsTaken: List<EarningTx> = emptyList(),
    val position: GeoPoint = RiderSeed.home,
    val nowMillis: Long = System.currentTimeMillis(),
    val missedOffers: Int = 0,
    val acceptedOffers: Int = 0,
    val lastInstantPayoutAt: Long = 0L,
    val sosActive: Boolean = false,
    val message: String? = null
) {
    val isLoggedIn get() = sessionPhone != null
    val cashInHand: Int get() = cash.sumOf { if (it.type == CashEntryType.COLLECTED) it.amount else -it.amount }.coerceAtLeast(0)
    val overCashLimit: Boolean get() = cashInHand >= FLOATING_CASH_LIMIT

    private val midnight: Long
        get() = Calendar.getInstance().apply {
            timeInMillis = nowMillis; set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    val todayTrips: List<Trip> get() = history.filter { (it.completedAtMillis ?: 0) >= midnight }
    val todayCompleted: Int get() = todayTrips.count { it.stage == TripStage.DELIVERED }
    val todayEarnings: Int get() = todayTrips.sumOf { it.totalEarning }

    /** This week's split (seeded earlier days + live today). */
    val weekTrips: Int get() = RiderSeed.WEEK_TRIPS_BEFORE_TODAY + todayCompleted
    val weekTripPay: Int get() = RiderSeed.WEEK_EARNINGS_BEFORE_TODAY + todayTrips.filter { it.stage == TripStage.DELIVERED }.sumOf { it.basePay + it.distancePay } + todayTrips.filter { it.stage != TripStage.DELIVERED }.sumOf { it.totalEarning }
    val weekTips: Int get() = 300 + todayTrips.filter { it.stage == TripStage.DELIVERED }.sumOf { it.tip }
    val weekIncentives: Int get() = 750 + todayTrips.filter { it.stage == TripStage.DELIVERED }.sumOf { it.incentive }
    val weekTotal: Int get() = weekTripPay + weekTips + weekIncentives
    /** Earned but not yet paid out (instant payouts reduce it). */
    val withdrawable: Int get() = (todayEarnings + 620 - payoutsTaken.sumOf { it.amount }).coerceAtLeast(0)
    val acceptanceRate: Int
        get() = (acceptedOffers + 46).let { a -> a * 100 / (a + missedOffers + 2) }
    val canInstantPayout: Boolean
        get() = withdrawable >= INSTANT_PAYOUT_MIN && profile.status(KycDoc.BANK) == DocStatus.VERIFIED && nowMillis - lastInstantPayoutAt > 24 * 3_600_000L
}

class RiderViewModel(application: Application) : AndroidViewModel(application) {
    private val storage = RiderStorage(application)
    private val _state = MutableStateFlow(RiderUiState())
    val state: StateFlow<RiderUiState> = _state.asStateFlow()
    private var offerSeq = 0
    private var idleSince = System.currentTimeMillis()

    init {
        val now = System.currentTimeMillis()
        _state.value = RiderUiState(
            isReady = true,
            sessionPhone = storage.sessionPhone,
            profile = storage.loadProfile() ?: RiderProfile(),
            settings = storage.loadSettings(),
            history = RiderSeed.history(now),
            cash = storage.loadCash() ?: RiderSeed.cashLedger(now),
            lastInstantPayoutAt = storage.lastInstantPayoutAt,
            nowMillis = now
        )
        startTicker()
    }

    fun consumeMessage() = _state.update { it.copy(message = null) }
    private fun toast(m: String) = _state.update { it.copy(message = m) }

    // ── Auth & KYC ────────────────────────────────────────────────────────────────────────────

    fun verifyOtp(phone: String, otp: String): Boolean {
        if (otp != DEMO_OTP) return false
        storage.sessionPhone = phone
        val saved = storage.loadProfile()
        val profile = if (saved != null && saved.phone == phone) saved else RiderProfile(phone = phone)
        storage.saveProfile(profile)
        _state.update { it.copy(sessionPhone = phone, profile = profile) }
        return true
    }

    fun signOut(): Boolean {
        if (_state.value.activeTrip != null) { toast("Finish or cancel your active trip before signing out"); return false }
        storage.sessionPhone = null
        _state.update { it.copy(sessionPhone = null, isOnline = false, offer = null) }
        return true
    }

    fun updateProfile(transform: (RiderProfile) -> RiderProfile) {
        val p = transform(_state.value.profile)
        storage.saveProfile(p)
        _state.update { it.copy(profile = p) }
    }

    /** Simulated document check: "Verifying" for a moment, then verified. */
    fun submitDocument(doc: KycDoc) {
        updateProfile { it.copy(docs = it.docs + (doc to DocStatus.PENDING)) }
        viewModelScope.launch {
            delay(2_000)
            updateProfile { it.copy(docs = it.docs + (doc to DocStatus.VERIFIED)) }
        }
    }

    fun completeOnboarding() {
        if (!_state.value.profile.kycVerified) return
        updateProfile { it.copy(onboarded = true) }
        toast("Welcome aboard! Go online to start earning.")
    }

    fun updateSettings(transform: (RiderSettings) -> RiderSettings) {
        val s = transform(_state.value.settings)
        storage.saveSettings(s)
        _state.update { it.copy(settings = s) }
    }

    // ── Duty ──────────────────────────────────────────────────────────────────────────────────

    fun setOnline(online: Boolean) {
        val s = _state.value
        if (online) {
            when {
                !s.profile.kycVerified -> { toast("Complete KYC verification to go online"); return }
                s.overCashLimit -> { toast("Deposit your COD cash (₹${s.cashInHand}) to go online"); return }
            }
            idleSince = System.currentTimeMillis()
            toast("You're online · looking for orders in ${s.profile.preferredZones.firstOrNull() ?: "your zone"}")
        } else {
            if (s.activeTrip != null) { toast("You can go offline after this trip"); return }
            toast("You're offline")
        }
        _state.update { it.copy(isOnline = online, offer = if (online) it.offer else null) }
    }

    /** Demo: request an offer right away instead of waiting. */
    fun requestOfferNow() {
        val s = _state.value
        when {
            !s.isOnline -> toast("Go online to receive orders")
            s.activeTrip != null -> toast("Finish the current trip first")
            s.offer != null -> Unit
            s.overCashLimit -> toast("Deposit COD cash to receive new orders")
            else -> pushOffer()
        }
    }

    private fun pushOffer() {
        val now = System.currentTimeMillis()
        val trip = RiderSeed.offer(offerSeq++, _state.value.position, now)
        _state.update { it.copy(offer = TripOffer(trip, now + ASSIGNMENT_WINDOW_MS)) }
    }

    fun acceptOffer() {
        val offer = _state.value.offer ?: return
        _state.update { it.copy(offer = null, activeTrip = offer.trip.copy(origin = it.position, offeredAtMillis = System.currentTimeMillis()), acceptedOffers = it.acceptedOffers + 1) }
    }

    fun rejectOffer() {
        _state.update { it.copy(offer = null, missedOffers = it.missedOffers + 1) }
        idleSince = System.currentTimeMillis()
        toast("Order declined")
    }

    // ── Active trip ───────────────────────────────────────────────────────────────────────────

    private fun updateTrip(transform: (Trip) -> Trip) = _state.update { s -> s.copy(activeTrip = s.activeTrip?.let(transform)) }

    /** Demo helper: jump to the end of the current leg. */
    fun skipTravel() = updateTrip { it.copy(legProgress = 1f) }

    fun arrivedAtStore() {
        val t = _state.value.activeTrip ?: return
        if (t.legProgress < 0.9f) { toast("You're still ${String.format(java.util.Locale.US, "%.1f", t.pickupKm * (1 - t.legProgress))} km from the store"); return }
        updateTrip { it.copy(stage = TripStage.AT_STORE, legProgress = 1f) }
        _state.update { it.copy(position = t.store) }
    }

    fun togglePicked(item: String) = updateTrip { it.copy(pickedItems = if (item in it.pickedItems) it.pickedItems - item else it.pickedItems + item) }

    fun confirmPickup() {
        val t = _state.value.activeTrip ?: return
        if (t.readiness != StoreReadiness.READY) { toast("The store is still packing this order"); return }
        if (t.pickedItems.size < t.items.size) { toast("Check every item before confirming pickup"); return }
        updateTrip { it.copy(stage = TripStage.TO_CUSTOMER, legProgress = 0f) }
        toast("Picked up · head to ${t.customerName}")
    }

    fun arrivedAtCustomer() {
        val t = _state.value.activeTrip ?: return
        if (t.legProgress < 0.9f) { toast("You're still ${String.format(java.util.Locale.US, "%.1f", t.dropKm * (1 - t.legProgress))} km away"); return }
        updateTrip { it.copy(stage = TripStage.AT_CUSTOMER, legProgress = 1f) }
        _state.update { it.copy(position = t.customer) }
    }

    fun markCalledCustomer() = updateTrip { it.copy(calledCustomer = true) }
    fun markPhotoTaken() = updateTrip { it.copy(proofPhotoTaken = true) }

    /** COD: record the cash before the delivery can be completed. */
    fun collectCash() {
        val t = _state.value.activeTrip ?: return
        if (t.paymentMode != PaymentMode.COD || t.cashCollected) return
        val entry = CashEntry("c${System.currentTimeMillis()}", CashEntryType.COLLECTED, t.orderAmount, System.currentTimeMillis(), "Order ${t.shortId}")
        val cash = listOf(entry) + _state.value.cash
        storage.saveCash(cash)
        _state.update { it.copy(cash = cash) }
        updateTrip { it.copy(cashCollected = true) }
    }

    /** OTP must match, or a photo proof must be taken (e.g. customer can't share OTP). */
    fun completeDelivery(otp: String?): Boolean {
        val t = _state.value.activeTrip ?: return false
        val otpOk = otp != null && otp == t.customerOtp
        if (!otpOk && !t.proofPhotoTaken) return false
        if (t.paymentMode == PaymentMode.COD && !t.cashCollected) { toast("Collect ₹${t.orderAmount} cash first"); return false }
        finishTrip(t.copy(stage = TripStage.DELIVERED, completedAtMillis = System.currentTimeMillis()))
        toast("Delivered! You earned ₹${t.payout + t.tip}" + if (t.tip > 0) " incl. ₹${t.tip} tip" else "")
        return true
    }

    fun cancelTrip(reason: String) {
        val t = _state.value.activeTrip ?: return
        if (t.stage == TripStage.TO_CUSTOMER || t.stage == TripStage.AT_CUSTOMER) { toast("Order is picked up — report a failed delivery instead"); return }
        finishTrip(t.copy(stage = TripStage.CANCELLED, cancelReason = reason, completedAtMillis = System.currentTimeMillis()))
        toast("Trip cancelled · we'll reassign the order")
    }

    /** Failed delivery: the order goes back to the store (return trip earns the base pay). */
    fun reportFailedDelivery(reason: String, comment: String) {
        val t = _state.value.activeTrip ?: return
        val note = listOfNotNull(reason, comment.trim().ifBlank { null }).joinToString(" · ")
        finishTrip(t.copy(stage = TripStage.RETURNED, cancelReason = note, completedAtMillis = System.currentTimeMillis()))
        toast("Report submitted · return the order to ${t.storeName}")
    }

    private fun finishTrip(t: Trip) {
        _state.update { it.copy(activeTrip = null, history = listOf(t) + it.history) }
        idleSince = System.currentTimeMillis()
    }

    // ── Cash & payouts ────────────────────────────────────────────────────────────────────────

    fun depositCash(upiRef: String) {
        val amount = _state.value.cashInHand
        if (amount <= 0) { toast("No cash to deposit"); return }
        val entry = CashEntry("d${System.currentTimeMillis()}", CashEntryType.DEPOSITED, amount, System.currentTimeMillis(), "UPI ref $upiRef")
        val cash = listOf(entry) + _state.value.cash
        storage.saveCash(cash)
        _state.update { it.copy(cash = cash) }
        toast("₹$amount deposited · cash in hand is now ₹0")
    }

    fun instantPayout() {
        val s = _state.value
        if (!s.canInstantPayout) { toast("Instant payout isn't available right now"); return }
        val now = System.currentTimeMillis()
        val tx = EarningTx("p$now", TxType.PAYOUT, s.withdrawable, now, "To bank ${s.profile.bankMasked}")
        storage.lastInstantPayoutAt = now
        _state.update { it.copy(payoutsTaken = listOf(tx) + it.payoutsTaken, lastInstantPayoutAt = now) }
        toast("₹${tx.amount} sent to your bank · arrives in a few minutes")
    }

    // ── SOS ───────────────────────────────────────────────────────────────────────────────────

    fun triggerSos() {
        _state.update { it.copy(sosActive = true) }
        toast("SOS sent · QuickKart safety team is calling you")
    }

    fun cancelSos() = _state.update { it.copy(sosActive = false) }

    // ── Ticker ────────────────────────────────────────────────────────────────────────────────

    private fun startTicker() {
        viewModelScope.launch {
            while (true) {
                delay(1_000)
                val now = System.currentTimeMillis()
                _state.update { it.copy(nowMillis = now) }
                val s = _state.value
                if (!s.isLoggedIn) continue

                // Offer timeout counts as missed.
                s.offer?.let { if (now > it.expiresAtMillis) { _state.update { st -> st.copy(offer = null, missedOffers = st.missedOffers + 1) }; idleSince = now; toast("Order offer expired") } }

                // New offers while online & idle (~every 20 s).
                if (s.isOnline && s.activeTrip == null && s.offer == null && !s.overCashLimit && now - idleSince > 20_000) pushOffer()

                // Travel along the current leg (simulated ~40 s per leg).
                s.activeTrip?.let { t ->
                    if ((t.stage == TripStage.TO_STORE || t.stage == TripStage.TO_CUSTOMER) && t.legProgress < 1f) {
                        val p = (t.legProgress + 0.025f).coerceAtMost(1f)
                        val from = if (t.stage == TripStage.TO_STORE) t.origin else t.store
                        val to = if (t.stage == TripStage.TO_STORE) t.store else t.customer
                        updateTrip { it.copy(legProgress = p) }
                        _state.update { st -> st.copy(position = GeoPoint(from.lat + (to.lat - from.lat) * p, from.lng + (to.lng - from.lng) * p)) }
                    }
                    // Store finishes packing ~35 s after acceptance.
                    if (t.readiness == StoreReadiness.PREPARING && now - t.offeredAtMillis > 35_000) updateTrip { it.copy(readiness = StoreReadiness.READY) }
                }
            }
        }
    }

    companion object { const val DEMO_OTP = "1234" }
}
