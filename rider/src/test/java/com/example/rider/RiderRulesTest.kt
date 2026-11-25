package com.example.rider

import com.example.rider.data.CashEntry
import com.example.rider.data.CashEntryType
import com.example.rider.data.DocStatus
import com.example.rider.data.FLOATING_CASH_LIMIT
import com.example.rider.data.KycDoc
import com.example.rider.data.RiderProfile
import com.example.rider.data.RiderSeed
import com.example.rider.data.TripStage
import com.example.rider.data.VehicleType
import com.example.rider.ui.RiderUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RiderRulesTest {
    @Test fun cashInHandIsCollectedMinusDeposited() {
        val s = RiderUiState(cash = listOf(
            CashEntry("1", CashEntryType.COLLECTED, 1500, 0, ""),
            CashEntry("2", CashEntryType.COLLECTED, 700, 0, ""),
            CashEntry("3", CashEntryType.DEPOSITED, 1000, 0, "")
        ))
        assertEquals(1200, s.cashInHand)
        assertFalse(s.overCashLimit)
        assertTrue(s.copy(cash = s.cash + CashEntry("4", CashEntryType.COLLECTED, FLOATING_CASH_LIMIT, 0, "")).overCashLimit)
    }

    @Test fun bicycleDoesNotNeedRc() {
        val verifiedExceptRc = KycDoc.entries.associateWith { if (it == KycDoc.RC) DocStatus.NOT_UPLOADED else DocStatus.VERIFIED }
        assertFalse(RiderProfile(docs = verifiedExceptRc, vehicleType = VehicleType.BIKE).kycVerified)
        assertTrue(RiderProfile(docs = verifiedExceptRc, vehicleType = VehicleType.BICYCLE).kycVerified)
    }

    @Test fun earningsDependOnOutcome() {
        val t = RiderSeed.offer(2, RiderSeed.home, 0L)
        assertEquals(t.payout + t.tip, t.copy(stage = TripStage.DELIVERED).totalEarning)
        assertEquals(t.basePay, t.copy(stage = TripStage.RETURNED).totalEarning)
        assertEquals(0, t.copy(stage = TripStage.CANCELLED).totalEarning)
    }

    @Test fun offerOtpIsFourDigits() {
        repeat(20) { assertEquals(4, RiderSeed.offer(it, RiderSeed.home, 0L).customerOtp.length) }
    }
}
