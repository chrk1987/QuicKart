package com.example

import com.example.data.model.OrderStage
import com.example.util.elapsedFraction
import com.example.util.riderRouteProgress
import com.example.util.stageForElapsed
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DeliveryTimelineTest {

    private val total = 8 * 60 // 8-minute express order

    private fun stageAt(secondsIntoOrder: Int): OrderStage {
        val remaining = (total - secondsIntoOrder).coerceAtLeast(0)
        return stageForElapsed(elapsedFraction(remaining, total), remaining)
    }

    @Test
    fun eightMinuteOrder_followsDocumentedTimeline() {
        assertEquals(OrderStage.CONFIRMED, stageAt(0))
        assertEquals(OrderStage.CONFIRMED, stageAt(71))         // before 1:12
        assertEquals(OrderStage.PACKING, stageAt(72))           // 1:12
        assertEquals(OrderStage.PACKING, stageAt(167))          // before 2:48
        assertEquals(OrderStage.OUT_FOR_DELIVERY, stageAt(168)) // 2:48
        assertEquals(OrderStage.OUT_FOR_DELIVERY, stageAt(479))
        assertEquals(OrderStage.DELIVERED, stageAt(480))        // 8:00
    }

    @Test
    fun stagesNeverGoBackwards_asTimePasses() {
        var last = OrderStage.CONFIRMED
        for (s in 0..total) {
            val stage = stageAt(s)
            assertTrue("went back at $s s", stage.stepIndex >= last.stepIndex)
            last = stage
        }
    }

    @Test
    fun rider_waitsAtStore_thenMovesSmoothlyToHome() {
        assertEquals(0f, riderRouteProgress(OrderStage.CONFIRMED, 0.1f), 0.0001f)
        assertTrue(riderRouteProgress(OrderStage.PACKING, 0.3f) < 0.05f)

        // Out for delivery: starts near the store, ends near home, always moving forward.
        val start = riderRouteProgress(OrderStage.OUT_FOR_DELIVERY, 0.35f)
        val mid = riderRouteProgress(OrderStage.OUT_FOR_DELIVERY, 0.675f)
        val end = riderRouteProgress(OrderStage.OUT_FOR_DELIVERY, 0.999f)
        assertTrue(start < 0.1f)
        assertTrue(mid in 0.4f..0.6f)
        assertTrue(end > 0.95f)
        assertTrue(start < mid && mid < end)

        assertEquals(1f, riderRouteProgress(OrderStage.DELIVERED, 1f), 0.0001f)
    }
}
