package com.example

import com.example.data.local.SeedData
import com.example.data.model.BillBreakdown
import com.example.data.model.OrderStage
import com.example.service.PdfReceiptService
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun seededData_hasActiveAndHistoricalOrders() {
        val orders = SeedData.createSeededOrders()
        assertEquals(1, orders.count { it.isActive })
        assertTrue(orders.count { it.stage == OrderStage.DELIVERED } >= 5)
        assertEquals(5, SeedData.neighborhoods.size)
        assertTrue(SeedData.products.size >= 15)
    }

    @Test
    fun billBreakdown_freeDeliveryThresholdWorks() {
        val belowThreshold = BillBreakdown(
            itemTotal = 150,
            mrpSavings = 20,
            deliveryFee = 15,
            handlingCharge = 4,
            promoDiscount = 0,
            tipAmount = 0,
            grandTotal = 169
        )
        assertFalse(belowThreshold.isFreeDelivery)
        assertEquals(49, belowThreshold.amountForFreeDelivery)

        val aboveThreshold = BillBreakdown(
            itemTotal = 210,
            mrpSavings = 30,
            deliveryFee = 0,
            handlingCharge = 4,
            promoDiscount = 20,
            tipAmount = 10,
            grandTotal = 204
        )
        assertTrue(aboveThreshold.isFreeDelivery)
        assertEquals(0, aboveThreshold.amountForFreeDelivery)
    }

    @Test
    fun pdfReceiptService_buildsItemizedDataOnlyForCompletedOrders() {
        val orders = SeedData.createSeededOrders()
        val activeOrder = orders.first { it.isActive }
        val completedOrder = orders.first { it.stage == OrderStage.DELIVERED }

        assertFalse(PdfReceiptService.canGenerateReceipt(activeOrder))
        assertTrue(PdfReceiptService.canGenerateReceipt(completedOrder))

        var rejectedIncomplete = false
        try {
            PdfReceiptService.buildDocumentData(activeOrder)
        } catch (_: IllegalArgumentException) {
            rejectedIncomplete = true
        }
        assertTrue(rejectedIncomplete)

        val receiptData = PdfReceiptService.buildDocumentData(completedOrder)
        assertEquals(completedOrder.orderId, receiptData.orderId)
        assertEquals(completedOrder.items.size, receiptData.items.size)
        assertEquals(completedOrder.itemTotal, receiptData.pricing.itemTotal)
        assertEquals(completedOrder.totalPaid, receiptData.pricing.totalPaid)
        assertEquals(
            completedOrder.items.sumOf { it.lineTotal },
            receiptData.items.sumOf { it.lineTotalInr }
        )
    }
}
