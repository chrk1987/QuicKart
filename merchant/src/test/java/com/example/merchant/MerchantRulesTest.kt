package com.example.merchant

import com.example.merchant.data.CatalogueItem
import com.example.merchant.data.OfferType
import com.example.merchant.data.OrderLine
import com.example.merchant.data.OrderStatus
import com.example.merchant.data.PaymentMode
import com.example.merchant.data.StoreOffer
import com.example.merchant.data.StoreOrder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MerchantRulesTest {
    private fun order(lines: List<OrderLine>, fee: Int = 25) =
        StoreOrder("QK1", "A", "Madhapur", lines, 0L, OrderStatus.NEW, PaymentMode.COD, deliveryFee = fee)

    @Test fun fullOrderHasNoRefund() {
        val o = order(listOf(OrderLine("a", "Atta", 2, 100), OrderLine("b", "Salt", 1, 30)))
        assertEquals(230, o.itemTotal)
        assertEquals(0, o.refundTotal)
        assertEquals(255, o.payableTotal)
    }

    @Test fun shortQuantityIsRefundedPerUnit() {
        val o = order(listOf(OrderLine("a", "Atta", 3, 100, fulfilledQuantity = 1), OrderLine("b", "Salt", 1, 30)))
        assertEquals(200, o.refundTotal)
        assertEquals(330 - 200 + 25, o.payableTotal)
    }

    @Test fun substituteMeansNoRefund() {
        val line = OrderLine("a", "Paneer", 1, 90, fulfilledQuantity = 0, substituteName = "Tofu")
        assertEquals(0, line.refundAmount)
        assertTrue(line.isShort)
    }

    @Test fun stockThresholds() {
        val base = CatalogueItem("x", "Milk", "", "Dairy & Eggs", 30, 0, 5)
        assertTrue(base.isLowStock)
        assertFalse(base.copy(stock = 6).isLowStock)
        assertTrue(base.copy(stock = 0).isOutOfStock)
        assertFalse(base.copy(stock = 0).isLowStock)
    }

    @Test fun discountedSellingPriceRoundsDown() {
        assertEquals(314, CatalogueItem("x", "Atta", "", "Grains & Atta", 349, 10, 1).sellingPrice)
    }

    @Test fun offerTitleAndExpiry() {
        val f = StoreOffer("o", OfferType.PERCENT_OFF, 20, 0, 0L, 1_000L, true, "Vegetables")
        assertEquals("20% off on Vegetables", f.title)
        assertTrue(f.isExpired(2_000L))
        assertFalse(f.isExpired(500L))
        assertEquals("Free delivery above ₹499", StoreOffer("d", OfferType.FREE_DELIVERY, 0, 499, 0L, 1L, true).title)
    }
}
