package com.example.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** "1 item" / "3 items" — singular/plural item count for UI text. */
fun itemCountLabel(count: Int): String = if (count == 1) "1 item" else "$count items"

/** Whole minutes left for a delivery, rounded up and never below 1 (e.g. 61s -> 2). */
fun etaMinutesLeft(remainingSeconds: Int): Int = ((remainingSeconds + 59) / 60).coerceAtLeast(1)

/** Customer-facing ETA, e.g. "6 min" (no ticking seconds). */
fun etaMinutesLabel(remainingSeconds: Int): String = "${etaMinutesLeft(remainingSeconds)} min"

/** Expected arrival clock time, e.g. "by 12:05 AM". */
fun arrivalByLabel(
    remainingSeconds: Int,
    nowMillis: Long = System.currentTimeMillis(),
    locale: Locale = Locale.getDefault()
): String {
    val arrival = nowMillis + remainingSeconds.coerceAtLeast(0) * 1000L
    return "by " + SimpleDateFormat("h:mm a", locale).format(Date(arrival))
}
