package com.example

import com.example.data.local.SeedData
import com.example.data.model.BillBreakdown
import com.example.data.model.OrderStage
import com.example.data.model.ProductCategory
import com.example.data.model.SupportIssueCategory
import com.example.data.model.SupportResolutionOption
import com.example.data.model.SupportTicketStatus
import com.example.data.model.computeSupportRefundEstimate
import com.example.data.model.filterCatalogProducts
import com.example.data.model.generateSupportDeskReply
import com.example.data.model.orderLineItemKey
import com.example.data.model.resolveInitialTicketOutcome
import com.example.service.PdfReceiptService
import com.example.ui.theme.QuickKartTypography
import com.example.ui.theme.metricsForWidth
import com.example.ui.theme.scaledQuickKartTypography
import com.example.util.AddressPinResolver
import com.example.util.NotificationHelper
import androidx.compose.ui.unit.dp
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
        assertEquals(completedOrder.deliveryWindowLabel, receiptData.deliveryWindowLabel)
        assertEquals(
            completedOrder.items.sumOf { it.lineTotal },
            receiptData.items.sumOf { it.lineTotalInr }
        )
    }

    @Test
    fun filterCatalogProducts_supportsFavoritesFilterAndCategorySearch() {
        val favIds = setOf("prod_a2_milk", "prod_sourdough")
        val allFavs = filterCatalogProducts(
            products = SeedData.products,
            selectedCategory = ProductCategory.ALL,
            searchQuery = "",
            favoritesOnly = true,
            favoriteIds = favIds
        )
        assertEquals(2, allFavs.size)
        assertTrue(allFavs.all { it.id in favIds })

        val bakeryFavs = filterCatalogProducts(
            products = SeedData.products,
            selectedCategory = ProductCategory.BAKERY_BREAD,
            searchQuery = "",
            favoritesOnly = true,
            favoriteIds = favIds
        )
        assertEquals(listOf("prod_sourdough"), bakeryFavs.map { it.id })
    }

    @Test
    fun deliverySlots_includeExpressTodayAndTomorrowWindows() {
        val slots = SeedData.deliverySlots
        assertTrue(slots.any { it.isExpress && it.dayLabel == "Now" })
        assertTrue(slots.any { !it.isExpress && it.dayLabel == "Today" })
        assertTrue(slots.any { !it.isExpress && it.dayLabel == "Tomorrow" })

        val express = slots.first { it.isExpress }
        assertEquals("Express • 8 mins", express.displaySummary)

        val scheduled = slots.first { !it.isExpress }
        assertEquals("${scheduled.dayLabel} • ${scheduled.windowText}", scheduled.displaySummary)
    }

    @Test
    fun notificationHelper_computesMonotonicProgressAndStageLine() {
        val confirmedStart = NotificationHelper.computeStageProgressPercent(
            stage = OrderStage.CONFIRMED,
            remainingSeconds = 480,
            totalEtaMinutes = 8
        )
        val packingMid = NotificationHelper.computeStageProgressPercent(
            stage = OrderStage.PACKING,
            remainingSeconds = 240,
            totalEtaMinutes = 8
        )
        val outForDeliveryNear = NotificationHelper.computeStageProgressPercent(
            stage = OrderStage.OUT_FOR_DELIVERY,
            remainingSeconds = 60,
            totalEtaMinutes = 8
        )
        val delivered = NotificationHelper.computeStageProgressPercent(
            stage = OrderStage.DELIVERED,
            remainingSeconds = 0,
            totalEtaMinutes = 8
        )

        assertTrue(confirmedStart in 15..30)
        assertTrue(packingMid in 35..62)
        assertTrue(outForDeliveryNear in 65..96)
        assertEquals(100, delivered)

        val outLine = NotificationHelper.buildLockScreenStageLine(OrderStage.OUT_FOR_DELIVERY)
        assertTrue(outLine.contains("✓ Confirmed"))
        assertTrue(outLine.contains("✓ Packing"))
        assertTrue(outLine.contains("● On Way"))
        assertTrue(outLine.contains("○ Delivered"))
    }

    @Test
    fun screenZoom_scalesTypographyIconsAndAdaptsResponsiveGrid() {
        assertEquals(QuickKartTypography, scaledQuickKartTypography(100))

        val enlargedType = scaledQuickKartTypography(125)
        assertEquals(
            QuickKartTypography.titleLarge.fontSize.value * 1.25f,
            enlargedType.titleLarge.fontSize.value,
            0.01f
        )

        val compactZoom = metricsForWidth(400.dp, 85)
        val defaultMetrics = metricsForWidth(400.dp, 100)
        val zoomedMetrics = metricsForWidth(400.dp, 125)

        assertEquals(100, defaultMetrics.zoomPercent)
        assertTrue(compactZoom.iconMedium < defaultMetrics.iconMedium)
        assertTrue(zoomedMetrics.iconMedium > defaultMetrics.iconMedium)
        assertFalse(defaultMetrics.isNarrowPhone)
        assertTrue(zoomedMetrics.isNarrowPhone)

        // Foldable/tablet adapts column count smoothly on zoom change
        val tabletDefault = metricsForWidth(620.dp, 100)
        val tabletZoomed = metricsForWidth(620.dp, 125)
        assertEquals(3, tabletDefault.gridColumns)
        assertEquals(2, tabletZoomed.gridColumns)
    }

    @Test
    fun customerSupport_refundEstimateTriageAndRepliesWork() {
        val orders = SeedData.createSeededOrders()
        val sampleOrder = orders.first()
        val firstItem = sampleOrder.items.first()
        val selectedKeys = setOf(orderLineItemKey(firstItem))

        val itemRefund = computeSupportRefundEstimate(
            order = sampleOrder,
            selectedItemKeys = selectedKeys,
            category = SupportIssueCategory.MISSING_ITEM
        )
        assertEquals(firstItem.lineTotal, itemRefund)

        val delayCredit = computeSupportRefundEstimate(
            order = sampleOrder,
            selectedItemKeys = emptySet(),
            category = SupportIssueCategory.DELIVERY_DELAY
        )
        assertEquals(25, delayCredit)

        val instantOutcome = resolveInitialTicketOutcome(
            ticketId = "SUP-9001",
            order = sampleOrder,
            category = SupportIssueCategory.MISSING_ITEM,
            affectedItemsSummary = firstItem.productName,
            affectedAmountInr = firstItem.lineTotal,
            preferredResolution = SupportResolutionOption.INSTANT_CREDIT,
            hasPhoto = true
        )
        assertEquals(SupportTicketStatus.RESOLVED, instantOutcome.status)
        assertTrue(instantOutcome.resolutionSummary.contains("₹${firstItem.lineTotal}"))

        val seededTickets = SeedData.createSeededSupportTickets()
        assertTrue(SeedData.supportFaqTopics.size >= 4)
        assertTrue(seededTickets.any { it.status == SupportTicketStatus.UNDER_REVIEW })
        assertTrue(seededTickets.any { it.status == SupportTicketStatus.RESOLVED })

        val openTicket = seededTickets.first { it.status == SupportTicketStatus.UNDER_REVIEW }
        val creditAction = generateSupportDeskReply(openTicket, "Switch to Instant Credit")
        assertEquals(SupportTicketStatus.RESOLVED, creditAction.newStatus)
        assertTrue(creditAction.replyText.contains("₹${openTicket.affectedAmountInr}"))
    }

    @Test
    fun addressPinResolver_generatesStateAreaCityPostalAndHubFromCoordinates() {
        val indiranagarPin = AddressPinResolver.resolveFromCoordinateGrid(12.9718, 77.6462)
        assertEquals("Bengaluru", indiranagarPin.city)
        assertEquals("Karnataka", indiranagarPin.state)
        assertEquals("India", indiranagarPin.country)
        assertEquals("indiranagar", indiranagarPin.nearestNeighborhoodId)
        assertTrue(indiranagarPin.postalCode.startsWith("560"))
        assertTrue(indiranagarPin.area.isNotBlank())
        assertTrue(indiranagarPin.suggestedStreet.isNotBlank())

        val mumbaiPin = AddressPinResolver.resolveFromCoordinateGrid(19.0551, 72.8318)
        assertEquals("Mumbai", mumbaiPin.city)
        assertEquals("Maharashtra", mumbaiPin.state)
        assertEquals("bandra_west", mumbaiPin.nearestNeighborhoodId)
        assertTrue(mumbaiPin.postalCode.startsWith("400"))

        val delhiPin = AddressPinResolver.resolveFromCoordinateGrid(28.5540, 77.2060)
        assertEquals("New Delhi", delhiPin.city)
        assertEquals("Delhi", delhiPin.state)
        assertEquals("hauz_khas", delhiPin.nearestNeighborhoodId)
        assertTrue(delhiPin.postalCode.startsWith("110"))

        val seededHome = SeedData.savedAddresses.first()
        assertTrue(seededHome.formattedLocationLine.contains("Karnataka"))
        assertEquals("12.9716, 77.6466", seededHome.formattedCoordinates)
    }
}
