package com.example.util

/** "1 item" / "3 items" — singular/plural item count for UI text. */
fun itemCountLabel(count: Int): String = if (count == 1) "1 item" else "$count items"
