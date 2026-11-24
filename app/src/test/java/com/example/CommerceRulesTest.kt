package com.example

import com.example.data.local.SeedData
import com.example.data.model.OrderStage
import com.example.data.model.PaymentMethod
import com.example.data.model.PromoCode
import com.example.ui.referralCodeFor
import com.example.util.cancellationRefundInr
import com.example.util.cardLast4ToStore
import com.example.util.isCancellable
import com.example.util.arrivalByLabel
import com.example.util.etaMinutesLabel
import com.example.util.etaMinutesLeft
import com.example.util.itemCountLabel
import com.example.util.nearestLockedOffer
import com.example.util.pickBestOffer
import com.example.util.referralCodeError
import com.example.util.updatedRecentSearches
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CommerceRulesTest {

    private val promos = listOf(
        PromoCode("QUICK50", "Flat ₹50 off above ₹299", discountInr = 50, minOrderInr = 299),
        PromoCode("FRESH20", "Flat ₹20 off above ₹149", discountInr = 20, minOrderInr = 149),
        PromoCode("FAST8", "₹30 off above ₹249", discountInr = 30, minOrderInr = 249)
    )

    // ── Best offer ──

    @Test
    fun bestOffer_picksBiggestEligibleDiscount() {
        assertEquals("QUICK50", pickBestOffer(promos, 300)?.code)
        assertEquals("FAST8", pickBestOffer(promos, 260)?.code)
        assertEquals("FRESH20", pickBestOffer(promos, 149)?.code)
    }

    @Test
    fun bestOffer_returnsNullBelowEveryMinimum_andNudgesToNearest() {
        assertNull(pickBestOffer(promos, 100))
        assertEquals("FRESH20", nearestLockedOffer(promos, 100)?.code)
        // Once FRESH20 is unlocked, the nearest locked one is FAST8.
        assertEquals("FAST8", nearestLockedOffer(promos, 200)?.code)
        assertNull(nearestLockedOffer(promos, 500))
    }

    // ── Referral ──

    @Test
    fun referral_acceptsValidFriendCode() {
        assertNull(referralCodeError("qkfriend50", ownCode = "QK1234FR", alreadyRedeemed = false))
    }

    @Test
    fun referral_rejectsOwnCodeInvalidFormatAndSecondRedemption() {
        assertNotNull(referralCodeError("QK1234FR", ownCode = "QK1234FR", alreadyRedeemed = false))
        assertNotNull(referralCodeError("HELLO", ownCode = "QK1234FR", alreadyRedeemed = false))
        assertNotNull(referralCodeError("QK1", ownCode = "QK1234FR", alreadyRedeemed = false))
        assertNotNull(referralCodeError("QKFRIEND50", ownCode = "QK1234FR", alreadyRedeemed = true))
    }

    @Test
    fun referralCode_isStableAndWellFormed() {
        val code = referralCodeFor("+91 98450 77218")
        assertEquals(code, referralCodeFor("+91 98450 77218"))
        assertTrue(code.startsWith("QK7218"))
        // The user's own code must itself be redeemable-format (so the own-code check is meaningful).
        assertNotNull(referralCodeError(code, ownCode = code, alreadyRedeemed = false))
    }

    // ── Recent searches ──

    @Test
    fun recentSearches_newestFirst_deduplicatedCaseInsensitive_capped() {
        val current = listOf("milk", "eggs", "bread")
        assertEquals(listOf("Eggs", "milk", "bread"), updatedRecentSearches(current, "Eggs", 6))

        val full = listOf("a1", "a2", "a3", "a4", "a5", "a6")
        val updated = updatedRecentSearches(full, "avocado", 6)
        assertEquals(6, updated.size)
        assertEquals("avocado", updated.first())
        assertFalse("a6" in updated)
    }

    @Test
    fun recentSearches_ignoresTooShortQueries() {
        val current = listOf("milk")
        assertEquals(current, updatedRecentSearches(current, " a ", 6))
        assertEquals(current, updatedRecentSearches(current, "", 6))
    }

    // ── Order cancellation & refunds ──

    @Test
    fun cancel_onlyAllowedWhileConfirmed() {
        val order = SeedData.createSeededOrders().first()
        OrderStage.entries.forEach { stage ->
            assertEquals(stage == OrderStage.CONFIRMED, isCancellable(order.copy(stage = stage)))
        }
    }

    @Test
    fun cancel_refundsPrepaidOrders_butNotCashOnDelivery() {
        val order = SeedData.createSeededOrders().first().copy(totalPaid = 188)
        assertEquals(188, cancellationRefundInr(order.copy(paymentMethod = PaymentMethod.UPI)))
        assertEquals(188, cancellationRefundInr(order.copy(paymentMethod = PaymentMethod.CARD)))
        assertEquals(188, cancellationRefundInr(order.copy(paymentMethod = PaymentMethod.WALLET)))
        assertEquals(0, cancellationRefundInr(order.copy(paymentMethod = PaymentMethod.COD)))
    }

    // ── Saved cards ──

    @Test
    fun savedCard_keepsOnlyLastFourDigits() {
        assertEquals("4242", cardLast4ToStore("4242424242424242"))
        assertEquals("4242", cardLast4ToStore("4242 4242 4242 4242"))
        assertEquals("1234", cardLast4ToStore("1234"))
        assertNull(cardLast4ToStore("12"))
        assertNull(cardLast4ToStore("not a card"))
    }

    // ── Copy ──

    // ── Customer ETA wording ──

    @Test
    fun eta_isWholeMinutesRoundedUp_neverBelowOne() {
        assertEquals(1, etaMinutesLeft(0))
        assertEquals(1, etaMinutesLeft(59))
        assertEquals(1, etaMinutesLeft(60))
        assertEquals(2, etaMinutesLeft(61))
        assertEquals(7, etaMinutesLeft(375))
        assertEquals("7 min", etaMinutesLabel(375))
    }

    @Test
    fun arrivalLabel_showsClockTimeNotCountdown() {
        val calendar = java.util.Calendar.getInstance().apply {
            set(2026, java.util.Calendar.NOVEMBER, 25, 23, 58, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }
        val label = arrivalByLabel(remainingSeconds = 7 * 60, nowMillis = calendar.timeInMillis, locale = java.util.Locale.US)
        assertEquals("by 12:05 AM", label)
    }

    @Test
    fun itemCountLabel_singularAndPlural() {
        assertEquals("1 item", itemCountLabel(1))
        assertEquals("0 items", itemCountLabel(0))
        assertEquals("3 items", itemCountLabel(3))
    }
}
