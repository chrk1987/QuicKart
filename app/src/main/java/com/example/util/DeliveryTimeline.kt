package com.example.util

import com.example.data.model.OrderStage

/*
 * Prototype delivery timeline — one place that defines how an order moves from
 * "Confirmed" to "Delivered" over its ETA, used by both the order status and the
 * rider's position on the map.
 *
 * For an 8-minute express order (480 s):
 *   0:00 – 1:12   Confirmed          store accepts and picks items     (rider waits at store)
 *   1:12 – 2:48   Packing            items packed into a cold bag      (rider at store)
 *   2:48 – 8:00   Out for delivery   rider travels store -> home       (pin moves along the route)
 *   8:00          Delivered          countdown reaches zero            (pin at home)
 * A real backend would replace this with store / rider events; the UI stays the same.
 */

/** Fraction of the ETA after which the store starts packing. */
const val PACKING_STARTS_AT = 0.15f

/** Fraction of the ETA after which the rider leaves the store. */
const val OUT_FOR_DELIVERY_STARTS_AT = 0.35f

/** 0.0 at order time -> 1.0 when the ETA runs out. */
fun elapsedFraction(remainingSeconds: Int, totalSeconds: Int): Float {
    if (totalSeconds <= 0) return 1f
    return (1f - remainingSeconds.toFloat() / totalSeconds.toFloat()).coerceIn(0f, 1f)
}

/** The stage an order should be in after [elapsed] of its ETA. */
fun stageForElapsed(elapsed: Float, remainingSeconds: Int): OrderStage {
    // Small tolerance so float rounding (e.g. 1 - 408/480 = 0.14999998) still hits the boundary.
    val e = elapsed + 1e-4f
    return when {
        remainingSeconds <= 0 -> OrderStage.DELIVERED
        e >= OUT_FOR_DELIVERY_STARTS_AT -> OrderStage.OUT_FOR_DELIVERY
        e >= PACKING_STARTS_AT -> OrderStage.PACKING
        else -> OrderStage.CONFIRMED
    }
}

/**
 * Rider position along the store -> home route (0.0 = store, 1.0 = home).
 * The rider stays at the store until out for delivery, then moves smoothly
 * across the delivery window instead of jumping.
 */
fun riderRouteProgress(stage: OrderStage, elapsed: Float): Float = when (stage) {
    OrderStage.CONFIRMED -> 0f
    OrderStage.PACKING -> 0.02f
    OrderStage.OUT_FOR_DELIVERY -> {
        val window = (elapsed - OUT_FOR_DELIVERY_STARTS_AT) / (1f - OUT_FOR_DELIVERY_STARTS_AT)
        (0.04f + window.coerceIn(0f, 1f) * 0.94f).coerceIn(0.04f, 0.98f)
    }
    OrderStage.DELIVERED -> 1f
}
