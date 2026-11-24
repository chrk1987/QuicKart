package com.example.util

import com.example.data.model.OrderRecord
import com.example.data.model.OrderStage
import com.example.data.model.PaymentMethod
import com.example.data.model.PromoCode

/*
 * Pure business rules shared by the ViewModel / preferences and covered by unit tests.
 */

/** The eligible promo with the biggest discount for [itemTotalInr], or null if none qualifies. */
fun pickBestOffer(promos: List<PromoCode>, itemTotalInr: Int): PromoCode? =
    promos.filter { itemTotalInr >= it.minOrderInr }.maxByOrNull { it.discountInr }

/** The promo that needs the least extra spend, used to nudge the user when nothing qualifies. */
fun nearestLockedOffer(promos: List<PromoCode>, itemTotalInr: Int): PromoCode? =
    promos.filter { itemTotalInr < it.minOrderInr }.minByOrNull { it.minOrderInr - itemTotalInr }

/** Returns an error message, or null when [code] can be redeemed. */
fun referralCodeError(code: String, ownCode: String, alreadyRedeemed: Boolean): String? {
    val clean = code.trim().uppercase()
    return when {
        alreadyRedeemed -> "You've already redeemed a referral code."
        clean == ownCode.uppercase() -> "You can't use your own referral code."
        !Regex("^QK[A-Z0-9]{4,}$").matches(clean) -> "Enter a valid code, e.g. QKFRIEND50."
        else -> null
    }
}

/** New recent-search list: [query] first, case-insensitive de-duplication, capped at [max]. */
fun updatedRecentSearches(current: List<String>, query: String, max: Int): List<String> {
    val clean = query.trim()
    if (clean.length < 2) return current.take(max)
    return (listOf(clean) + current.filterNot { it.equals(clean, ignoreCase = true) })
        .filter { it.isNotBlank() }
        .take(max)
}

/** Orders can be cancelled only before the store starts packing. */
fun isCancellable(order: OrderRecord): Boolean = order.stage == OrderStage.CONFIRMED

/** Amount refunded to the wallet when [order] is cancelled (nothing was collected for COD). */
fun cancellationRefundInr(order: OrderRecord): Int =
    if (order.paymentMethod == PaymentMethod.COD) 0 else order.totalPaid

/** Only the last 4 digits of a card are ever kept; returns null for anything that isn't a card number. */
fun cardLast4ToStore(cardNumberOrLast4: String): String? {
    val digits = cardNumberOrLast4.filter { it.isDigit() }
    return if (digits.length == 4 || digits.length in 12..19) digits.takeLast(4) else null
}
