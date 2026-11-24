package com.example.rider

import androidx.compose.ui.unit.dp
import com.example.rider.data.model.COD_FLOATING_LIMIT_INR
import com.example.rider.data.model.PayoutTransaction
import com.example.rider.data.model.PayoutTxType
import com.example.rider.data.model.RiderPaymentMode
import com.example.rider.data.model.RiderTripOrder
import com.example.rider.data.model.RiderTripStage
import com.example.rider.data.model.computeTripEarningsBreakdown
import com.example.rider.data.model.generateDispatchOffer
import com.example.rider.data.model.verifyDeliveryCompletion
import com.example.rider.data.seed.RiderSeedData
import com.example.rider.ui.theme.DeviceSizeClass
import com.example.rider.ui.theme.metricsForWidth
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RiderUnitTest {

    @Test
    fun verifyDeliveryCompletion_validatesPinAndCodConfirmation() {
        val now = 1_700_000_000_000L
        val hub = RiderSeedData.hubs.first()
        val codOffer = generateDispatchOffer(hub, now, sequenceIndex = 1)
            .copy(paymentMode = RiderPaymentMode.COD, collectAmountInr = 450, deliveryOtp = "7742")

        assertNotNull(verifyDeliveryCompletion(codOffer, "77", codConfirmed = true))
        assertNotNull(verifyDeliveryCompletion(codOffer, "0000", codConfirmed = true))
        assertNotNull(verifyDeliveryCompletion(codOffer, "7742", codConfirmed = false))
        assertNull(verifyDeliveryCompletion(codOffer, "7742", codConfirmed = true))

        val prepaidOffer = codOffer.copy(paymentMode = RiderPaymentMode.PREPAID_UPI, collectAmountInr = 0)
        assertNull(verifyDeliveryCompletion(prepaidOffer, "7742", codConfirmed = false))
    }

    @Test
    fun computeTripEarningsBreakdown_calculatesTotalsIncentivesAndCodLedger() {
        val now = 1_700_000_000_000L
        val hub = RiderSeedData.hubs.first()
        val sampleTrips = List(4) { idx ->
            generateDispatchOffer(hub, now + idx * 10_000L, sequenceIndex = idx).copy(
                orderId = "QK-TEST-$idx",
                stage = RiderTripStage.DELIVERED,
                basePayInr = 25,
                distancePayInr = 15,
                surgeBonusInr = 10,
                tipInr = 10,
                onTimeBonusInr = 10,
                paymentMode = if (idx == 0) RiderPaymentMode.COD else RiderPaymentMode.PREPAID_UPI,
                collectAmountInr = if (idx == 0) 2200 else 0,
                codCollected = true
            )
        }
        val transactions = listOf(
            PayoutTransaction("WD-1", PayoutTxType.BANK_WITHDRAWAL, "Bank", 100, now),
            PayoutTransaction("CD-1", PayoutTxType.COD_DEPOSIT, "COD Deposit", 200, now)
        )

        val summary = computeTripEarningsBreakdown(sampleTrips, transactions)

        assertEquals(4, summary.completedDrops)
        assertEquals(100, summary.unlockedIncentiveInr) // 4 drops tier unlocked
        assertEquals(160, summary.baseAndDistanceInr) // 4 * (25 + 15)
        assertEquals(40, summary.surgeEarnedInr)
        assertEquals(40, summary.tipsEarnedInr)
        assertEquals(40, summary.onTimeBonusInr)
        assertEquals(380, summary.totalEarnedInr)
        assertEquals(280, summary.availableWalletInr)
        assertEquals(2000, summary.codCashInHandInr)
        assertTrue(summary.isCodLimitExceeded)
        assertEquals(COD_FLOATING_LIMIT_INR, summary.codCashInHandInr)
    }

    @Test
    fun generateDispatchOffer_producesValidCoordinatesAndPay() {
        val now = 1_700_000_000_000L
        RiderSeedData.hubs.forEachIndexed { idx, hub ->
            val offer: RiderTripOrder = generateDispatchOffer(hub, now, sequenceIndex = idx)
            assertEquals(RiderTripStage.OFFERED, offer.stage)
            assertEquals(hub.id, offer.hubId)
            assertTrue(offer.totalRiderPayoutInr >= 35)
            assertTrue(offer.distanceKm >= 0.8)
            assertEquals(4, offer.deliveryOtp.length)
            assertTrue(offer.items.isNotEmpty())
            assertNotNull(offer.offerExpiresAtMillis)
            assertFalse(offer.podSealed)
        }
    }

    @Test
    fun metricsForWidth_scalesWithZoomAndDeviceClass() {
        val small = metricsForWidth(340.dp, 100)
        val expanded = metricsForWidth(900.dp, 100)
        val zoomedIn = metricsForWidth(400.dp, 130)

        assertEquals(DeviceSizeClass.SMALL_COMPACT, small.sizeClass)
        assertEquals(DeviceSizeClass.EXPANDED, expanded.sizeClass)
        assertTrue(expanded.useNavRail)
        assertTrue(zoomedIn.iconLarge > metricsForWidth(400.dp, 100).iconLarge)
    }
}
