package com.example.rider.data.repository

import com.example.rider.data.local.PayoutTransactionDao
import com.example.rider.data.local.RiderTripDao
import com.example.rider.data.local.toEntity
import com.example.rider.data.local.toModel
import com.example.rider.data.model.PayoutTransaction
import com.example.rider.data.model.PayoutTxType
import com.example.rider.data.model.RiderTripOrder
import com.example.rider.data.model.RiderTripStage
import com.example.rider.data.model.generateDispatchOffer
import com.example.rider.data.seed.RiderSeedData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RiderRepository(
    private val tripDao: RiderTripDao,
    private val transactionDao: PayoutTransactionDao
) {
    val tripsFlow: Flow<List<RiderTripOrder>> = tripDao.observeAllTrips().map { list ->
        list.map { it.toModel() }
    }

    val transactionsFlow: Flow<List<PayoutTransaction>> = transactionDao.observeAllTransactions().map { list ->
        list.map { it.toModel() }
    }

    suspend fun ensureSeeded() {
        val now = System.currentTimeMillis()
        if (tripDao.getTripCount() == 0) {
            tripDao.upsertTrips(RiderSeedData.initialTrips(now).map { it.toEntity() })
        }
        if (transactionDao.getCount() == 0) {
            transactionDao.insertTransactions(RiderSeedData.initialTransactions(now).map { it.toEntity() })
        }
    }

    suspend fun spawnOffer(hubId: String): RiderTripOrder {
        val hub = RiderSeedData.hubById(hubId)
        val now = System.currentTimeMillis()
        val offer = generateDispatchOffer(hub = hub, nowMillis = now)
        tripDao.upsertTrip(offer.toEntity())
        return offer
    }

    suspend fun acceptOffer(orderId: String): RiderTripOrder? {
        val existing = tripDao.getTripById(orderId)?.toModel() ?: return null
        val updated = existing.copy(
            stage = RiderTripStage.HEADING_TO_HUB,
            acceptedAtMillis = System.currentTimeMillis(),
            offerExpiresAtMillis = null
        )
        tripDao.upsertTrip(updated.toEntity())
        return updated
    }

    suspend fun declineOffer(orderId: String) {
        val existing = tripDao.getTripById(orderId)?.toModel() ?: return
        tripDao.upsertTrip(
            existing.copy(
                stage = RiderTripStage.DECLINED,
                offerExpiresAtMillis = null
            ).toEntity()
        )
    }

    suspend fun markArrivedAtHub(orderId: String) {
        val existing = tripDao.getTripById(orderId)?.toModel() ?: return
        if (existing.stage == RiderTripStage.HEADING_TO_HUB) {
            tripDao.upsertTrip(existing.copy(stage = RiderTripStage.PICKING_AT_POD).toEntity())
        }
    }

    suspend fun togglePickedItem(orderId: String, productId: String) {
        val existing = tripDao.getTripById(orderId)?.toModel() ?: return
        val updatedItems = existing.items.map { item ->
            if (item.productId == productId) item.copy(isPicked = !item.isPicked) else item
        }
        tripDao.upsertTrip(existing.copy(items = updatedItems).toEntity())
    }

    suspend fun pickAllAndSeal(orderId: String) {
        val existing = tripDao.getTripById(orderId)?.toModel() ?: return
        val updatedItems = existing.items.map { it.copy(isPicked = true) }
        tripDao.upsertTrip(
            existing.copy(
                items = updatedItems,
                podSealed = true
            ).toEntity()
        )
    }

    suspend fun toggleColdSeal(orderId: String) {
        val existing = tripDao.getTripById(orderId)?.toModel() ?: return
        tripDao.upsertTrip(existing.copy(podSealed = !existing.podSealed).toEntity())
    }

    suspend fun startOutForDelivery(orderId: String) {
        val existing = tripDao.getTripById(orderId)?.toModel() ?: return
        val sealedItems = existing.items.map { it.copy(isPicked = true) }
        tripDao.upsertTrip(
            existing.copy(
                items = sealedItems,
                podSealed = true,
                stage = RiderTripStage.OUT_FOR_DELIVERY
            ).toEntity()
        )
    }

    suspend fun markArrivedAtDoor(orderId: String) {
        val existing = tripDao.getTripById(orderId)?.toModel() ?: return
        if (existing.stage == RiderTripStage.OUT_FOR_DELIVERY) {
            tripDao.upsertTrip(existing.copy(stage = RiderTripStage.ARRIVED_AT_DOOR).toEntity())
        }
    }

    suspend fun attachPodPhoto(orderId: String, uriString: String) {
        val existing = tripDao.getTripById(orderId)?.toModel() ?: return
        tripDao.upsertTrip(existing.copy(podPhotoUri = uriString).toEntity())
    }

    suspend fun completeDelivery(orderId: String, codCollected: Boolean): RiderTripOrder? {
        val existing = tripDao.getTripById(orderId)?.toModel() ?: return null
        val completed = existing.copy(
            stage = RiderTripStage.DELIVERED,
            codCollected = codCollected,
            deliveredAtMillis = System.currentTimeMillis()
        )
        tripDao.upsertTrip(completed.toEntity())
        return completed
    }

    suspend fun reassignActiveTrip(orderId: String) {
        val existing = tripDao.getTripById(orderId)?.toModel() ?: return
        tripDao.upsertTrip(
            existing.copy(
                stage = RiderTripStage.DECLINED,
                offerExpiresAtMillis = null
            ).toEntity()
        )
    }

    suspend fun recordBankWithdrawal(amountInr: Int) {
        if (amountInr <= 0) return
        val now = System.currentTimeMillis()
        transactionDao.insertTransaction(
            PayoutTransaction(
                id = "WD-${now % 100000}",
                type = PayoutTxType.BANK_WITHDRAWAL,
                title = "Instant UPI Payout • HDFC ****4819",
                amountInr = amountInr,
                createdAtMillis = now
            ).toEntity()
        )
    }

    suspend fun recordCodSettlement(amountInr: Int, hubName: String) {
        if (amountInr <= 0) return
        val now = System.currentTimeMillis()
        transactionDao.insertTransaction(
            PayoutTransaction(
                id = "COD-${now % 100000}",
                type = PayoutTxType.COD_DEPOSIT,
                title = "Cash Settled at $hubName Kiosk",
                amountInr = amountInr,
                createdAtMillis = now
            ).toEntity()
        )
    }
}
