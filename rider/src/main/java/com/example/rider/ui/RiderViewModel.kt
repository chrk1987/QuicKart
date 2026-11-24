package com.example.rider.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.rider.data.local.RiderDatabase
import com.example.rider.data.local.RiderPreferences
import com.example.rider.data.local.RiderPreferencesManager
import com.example.rider.data.model.PayoutTransaction
import com.example.rider.data.model.RiderEarningsSummary
import com.example.rider.data.model.RiderHub
import com.example.rider.data.model.RiderPaymentMode
import com.example.rider.data.model.RiderTripOrder
import com.example.rider.data.model.RiderTripStage
import com.example.rider.data.model.RiderVehicleType
import com.example.rider.data.model.computeTripEarningsBreakdown
import com.example.rider.data.model.verifyDeliveryCompletion
import com.example.rider.data.repository.RiderRepository
import com.example.rider.data.seed.RiderSeedData
import com.example.rider.util.RiderNotificationHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class RiderCelebration(
    val orderId: String,
    val customerName: String,
    val earnedInr: Int,
    val elapsedMinutes: Int
)

data class RiderUiState(
    val prefs: RiderPreferences = RiderPreferences(),
    val currentHub: RiderHub = RiderSeedData.hubs.first(),
    val availableHubs: List<RiderHub> = RiderSeedData.hubs,
    val allTrips: List<RiderTripOrder> = emptyList(),
    val activeTrip: RiderTripOrder? = null,
    val pendingOffer: RiderTripOrder? = null,
    val offerSecondsRemaining: Int = 0,
    val completedTrips: List<RiderTripOrder> = emptyList(),
    val transactions: List<PayoutTransaction> = emptyList(),
    val earnings: RiderEarningsSummary = computeTripEarningsBreakdown(emptyList(), emptyList()),
    val nowMillis: Long = System.currentTimeMillis(),
    val otpError: String? = null
)

class RiderViewModel(application: Application) : AndroidViewModel(application) {

    private val prefsManager = RiderPreferencesManager(application)
    private val database = RiderDatabase.getInstance(application)
    private val repository = RiderRepository(
        tripDao = database.tripDao(),
        transactionDao = database.transactionDao()
    )

    private val nowMillisFlow = MutableStateFlow(System.currentTimeMillis())
    private val otpErrorFlow = MutableStateFlow<String?>(null)

    private val _snackbarMessages = MutableSharedFlow<String>(extraBufferCapacity = 8)
    val snackbarMessages = _snackbarMessages.asSharedFlow()

    private val _celebration = MutableStateFlow<RiderCelebration?>(null)
    val celebration: StateFlow<RiderCelebration?> = _celebration

    private val _navigateToActiveTripEvent = MutableSharedFlow<Unit>(extraBufferCapacity = 4)
    val navigateToActiveTripEvent = _navigateToActiveTripEvent.asSharedFlow()

    val uiState: StateFlow<RiderUiState> = combine(
        prefsManager.preferencesFlow,
        repository.tripsFlow,
        repository.transactionsFlow,
        nowMillisFlow,
        otpErrorFlow
    ) { prefs, trips, transactions, now, otpError ->
        val hub = RiderSeedData.hubById(prefs.hubId)
        val active = trips.firstOrNull { it.stage.isActiveRun() }
        val offer = if (active == null && prefs.isOnline) {
            trips.firstOrNull {
                it.stage == RiderTripStage.OFFERED &&
                    (it.offerExpiresAtMillis == null || it.offerExpiresAtMillis > now)
            }
        } else {
            null
        }
        val secondsLeft = offer?.offerExpiresAtMillis?.let { exp ->
            (((exp - now) + 999L) / 1000L).toInt().coerceAtLeast(0)
        } ?: 0
        val completed = trips.filter { it.stage == RiderTripStage.DELIVERED }
        val earnings = computeTripEarningsBreakdown(trips = trips, transactions = transactions)

        RiderUiState(
            prefs = prefs,
            currentHub = hub,
            availableHubs = RiderSeedData.hubs,
            allTrips = trips,
            activeTrip = active,
            pendingOffer = offer,
            offerSecondsRemaining = secondsLeft,
            completedTrips = completed,
            transactions = transactions,
            earnings = earnings,
            nowMillis = now,
            otpError = otpError
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = RiderUiState()
    )

    init {
        viewModelScope.launch {
            repository.ensureSeeded()
        }

        // 1-second live clock for dispatch countdown & elapsed SLA timer
        viewModelScope.launch {
            while (true) {
                delay(1_000L)
                val now = System.currentTimeMillis()
                nowMillisFlow.value = now
                expireStaleOffers(now)
            }
        }
    }

    private suspend fun expireStaleOffers(now: Long) {
        val currentTrips = uiState.value.allTrips
        currentTrips.filter {
            it.stage == RiderTripStage.OFFERED &&
                it.offerExpiresAtMillis != null &&
                it.offerExpiresAtMillis <= now
        }.forEach { stale ->
            repository.declineOffer(stale.orderId)
        }
    }

    private fun postOngoingNotificationIfEnabled(trip: RiderTripOrder?) {
        if (uiState.value.prefs.notificationsEnabled) {
            RiderNotificationHelper.updateOngoingTripNotification(getApplication(), trip)
        }
    }

    fun loginRider(
        name: String,
        phone: String,
        fleetId: String,
        vehicleType: RiderVehicleType,
        hubId: String
    ) {
        viewModelScope.launch {
            val cleanName = name.trim().ifBlank { "Arjun Kumar" }
            val cleanPhone = phone.trim().ifBlank { "+91 98452 31090" }
            val cleanFleet = fleetId.trim().ifBlank { "QKR-BLR-248" }
            prefsManager.loginRider(cleanName, cleanPhone, cleanFleet, vehicleType, hubId)
            val hasOfferOrActive = uiState.value.activeTrip != null || uiState.value.pendingOffer != null
            if (!hasOfferOrActive) {
                repository.spawnOffer(hubId)
            }
            _snackbarMessages.tryEmit("Shift started at ${RiderSeedData.hubById(hubId).podName}")
        }
    }

    fun logout() {
        viewModelScope.launch {
            RiderNotificationHelper.updateOngoingTripNotification(getApplication(), null)
            prefsManager.logout()
            _snackbarMessages.tryEmit("Signed out of shift")
        }
    }

    fun toggleOnline(online: Boolean) {
        viewModelScope.launch {
            prefsManager.setOnline(online)
            if (online && uiState.value.activeTrip == null && uiState.value.pendingOffer == null) {
                val offer = repository.spawnOffer(uiState.value.prefs.hubId)
                if (uiState.value.prefs.notificationsEnabled) {
                    RiderNotificationHelper.showIncomingOfferAlert(getApplication(), offer)
                }
            }
            _snackbarMessages.tryEmit(
                if (online) "You are Online — receiving ${uiState.value.currentHub.neighborhood} orders"
                else "You are Offline — active runs still visible"
            )
        }
    }

    fun selectHub(hubId: String) {
        viewModelScope.launch {
            prefsManager.setHubId(hubId)
            val hub = RiderSeedData.hubById(hubId)
            if (uiState.value.prefs.isOnline && uiState.value.activeTrip == null) {
                // Expire old offer and ping one in the newly chosen zone
                uiState.value.pendingOffer?.let { repository.declineOffer(it.orderId) }
                repository.spawnOffer(hubId)
            }
            _snackbarMessages.tryEmit("Switched zone to ${hub.podName} (${hub.neighborhood})")
        }
    }

    fun selectVehicle(vehicleType: RiderVehicleType) {
        viewModelScope.launch {
            prefsManager.setVehicleType(vehicleType)
            _snackbarMessages.tryEmit("Vehicle updated to ${vehicleType.label}")
        }
    }

    fun pingNewOrderOffer() {
        viewModelScope.launch {
            if (!uiState.value.prefs.isOnline) {
                prefsManager.setOnline(true)
            }
            if (uiState.value.activeTrip != null) {
                _snackbarMessages.tryEmit("Complete your current active drop before taking another.")
                return@launch
            }
            uiState.value.pendingOffer?.let { repository.declineOffer(it.orderId) }
            val offer = repository.spawnOffer(uiState.value.prefs.hubId)
            if (uiState.value.prefs.notificationsEnabled) {
                RiderNotificationHelper.showIncomingOfferAlert(getApplication(), offer)
            }
            _snackbarMessages.tryEmit("New order ping: ${offer.orderId} • ₹${offer.totalRiderPayoutInr}")
        }
    }

    fun acceptOffer(orderId: String) {
        viewModelScope.launch {
            val accepted = repository.acceptOffer(orderId)
            if (accepted != null) {
                postOngoingNotificationIfEnabled(accepted)
                _navigateToActiveTripEvent.tryEmit(Unit)
                _snackbarMessages.tryEmit("Accepted ${accepted.orderId} — Head to ${accepted.pickupBayCode}")
            }
        }
    }

    fun declineOffer(orderId: String) {
        viewModelScope.launch {
            repository.declineOffer(orderId)
            _snackbarMessages.tryEmit("Offer declined and reassigned")
        }
    }

    fun markArrivedAtHub(orderId: String) {
        viewModelScope.launch {
            repository.markArrivedAtHub(orderId)
            val updated = uiState.value.allTrips.firstOrNull { it.orderId == orderId }
                ?.copy(stage = RiderTripStage.PICKING_AT_POD)
            postOngoingNotificationIfEnabled(updated)
            _snackbarMessages.tryEmit("Checked in at Pod. Verify items & seal bag.")
        }
    }

    fun togglePickedItem(orderId: String, productId: String) {
        viewModelScope.launch {
            repository.togglePickedItem(orderId, productId)
        }
    }

    fun pickAllAndSeal(orderId: String) {
        viewModelScope.launch {
            repository.pickAllAndSeal(orderId)
            _snackbarMessages.tryEmit("All items verified & bag sealed")
        }
    }

    fun toggleColdSeal(orderId: String) {
        viewModelScope.launch {
            repository.toggleColdSeal(orderId)
        }
    }

    fun startOutForDelivery(orderId: String) {
        viewModelScope.launch {
            repository.startOutForDelivery(orderId)
            val updated = uiState.value.allTrips.firstOrNull { it.orderId == orderId }
                ?.copy(stage = RiderTripStage.OUT_FOR_DELIVERY)
            postOngoingNotificationIfEnabled(updated)
            _snackbarMessages.tryEmit("Trip started! Navigate carefully.")
        }
    }

    fun markArrivedAtDoor(orderId: String) {
        viewModelScope.launch {
            repository.markArrivedAtDoor(orderId)
            val updated = uiState.value.allTrips.firstOrNull { it.orderId == orderId }
                ?.copy(stage = RiderTripStage.ARRIVED_AT_DOOR)
            postOngoingNotificationIfEnabled(updated)
            otpErrorFlow.value = null
            _snackbarMessages.tryEmit("Marked arrived at doorstep. Ask customer for 4-digit PIN.")
        }
    }

    fun attachPodPhoto(orderId: String, uri: Uri) {
        viewModelScope.launch {
            repository.attachPodPhoto(orderId, uri.toString())
            _snackbarMessages.tryEmit("Proof-of-Delivery photo attached")
        }
    }

    fun clearOtpError() {
        otpErrorFlow.value = null
    }

    fun completeDelivery(orderId: String, enteredOtp: String, codConfirmed: Boolean) {
        val trip = uiState.value.allTrips.firstOrNull { it.orderId == orderId } ?: return
        val error = verifyDeliveryCompletion(trip, enteredOtp, codConfirmed)
        if (error != null) {
            otpErrorFlow.value = error
            return
        }
        otpErrorFlow.value = null
        viewModelScope.launch {
            val codDone = trip.paymentMode == RiderPaymentMode.COD && codConfirmed
            val completed = repository.completeDelivery(orderId, codDone)
            postOngoingNotificationIfEnabled(null)
            if (completed != null) {
                val startMs = completed.acceptedAtMillis ?: completed.createdAtMillis
                val endMs = completed.deliveredAtMillis ?: System.currentTimeMillis()
                val elapsedMins = (((endMs - startMs) / 60_000L).toInt()).coerceAtLeast(1)
                _celebration.value = RiderCelebration(
                    orderId = completed.orderId,
                    customerName = completed.customerName,
                    earnedInr = completed.totalRiderPayoutInr,
                    elapsedMinutes = elapsedMins
                )
            }
        }
    }

    fun dismissCelebration() {
        _celebration.value = null
    }

    fun reassignActiveTrip(orderId: String, reason: String) {
        viewModelScope.launch {
            repository.reassignActiveTrip(orderId)
            postOngoingNotificationIfEnabled(null)
            _snackbarMessages.tryEmit("Trip $orderId flagged ($reason) & reassigned by Hub Dispatch")
        }
    }

    fun withdrawAvailableBalance() {
        val available = uiState.value.earnings.availableWalletInr
        if (available <= 0) {
            _snackbarMessages.tryEmit("No withdrawable wallet balance available yet.")
            return
        }
        viewModelScope.launch {
            repository.recordBankWithdrawal(available)
            _snackbarMessages.tryEmit("₹$available transferred via Instant UPI to HDFC ****4819")
        }
    }

    fun settleCodCash() {
        val cashInHand = uiState.value.earnings.codCashInHandInr
        if (cashInHand <= 0) {
            _snackbarMessages.tryEmit("COD ledger is already settled at ₹0.")
            return
        }
        viewModelScope.launch {
            repository.recordCodSettlement(cashInHand, uiState.value.currentHub.podName)
            _snackbarMessages.tryEmit("₹$cashInHand COD cash settled with ${uiState.value.currentHub.podName}")
        }
    }

    fun setDarkTheme(enabled: Boolean) {
        viewModelScope.launch { prefsManager.setDarkTheme(enabled) }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            prefsManager.setNotificationsEnabled(enabled)
            if (!enabled) {
                RiderNotificationHelper.updateOngoingTripNotification(getApplication(), null)
            } else {
                uiState.value.activeTrip?.let {
                    RiderNotificationHelper.updateOngoingTripNotification(getApplication(), it)
                }
            }
        }
    }

    fun setScreenZoomPercent(zoomPercent: Int) {
        viewModelScope.launch { prefsManager.setScreenZoomPercent(zoomPercent) }
    }

    fun updateChecklist(helmet: Boolean, bag: Boolean, battery: Boolean) {
        viewModelScope.launch { prefsManager.updateChecklist(helmet, bag, battery) }
    }

    fun showMessage(message: String) {
        _snackbarMessages.tryEmit(message)
    }
}
