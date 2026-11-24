package com.example.data.model

import androidx.compose.ui.graphics.Color

data class Neighborhood(
    val id: String,
    val name: String,
    val city: String,
    val etaMinutes: Int,
    val hubName: String
) {
    val displayTitle: String get() = "$name, $city"
    val shortLabel: String get() = "$name • ${etaMinutes}m"
}

enum class ProductCategory(val label: String, val shortLabel: String) {
    ALL("All Essentials", "All"),
    DAIRY_EGGS("Dairy & Eggs", "Dairy"),
    FRESH_PRODUCE("Fresh Produce", "Produce"),
    BAKERY_BREAD("Bakery & Bread", "Bakery"),
    COLD_BREW_JUICES("Cold Brew & Juices", "Drinks"),
    SNACKS_PANTRY("Snacks & Pantry", "Snacks")
}

enum class ProductArtworkType {
    MILK_BOTTLE,
    EGGS_CARTON,
    PANEER_BLOCK,
    GREEK_YOGURT,
    AVOCADO,
    BANANAS,
    BABY_SPINACH,
    TOMATOES,
    SOURDOUGH,
    CROISSANT,
    BRIOCHE,
    COLD_BREW,
    ORANGE_JUICE,
    SUGARCANE_JUICE,
    KETTLE_CHIPS,
    MAKHANA,
    DARK_CHOCOLATE,
    PEANUT_BUTTER
}

data class ProductVariant(
    val id: String,
    val label: String,
    val priceInr: Int,
    val mrpInr: Int
) {
    val discountPercent: Int
        get() = if (mrpInr > priceInr) ((mrpInr - priceInr) * 100) / mrpInr else 0
}

data class Product(
    val id: String,
    val name: String,
    val brand: String,
    val category: ProductCategory,
    val description: String,
    val badgeText: String?,
    val rating: Float,
    val ratingCount: Int,
    val deliveryMinutes: Int,
    val artworkType: ProductArtworkType,
    val accentColor: Color,
    val variants: List<ProductVariant>,
    val highlights: List<String> = emptyList()
) {
    val defaultVariant: ProductVariant get() = variants.first()
}

data class CartItem(
    val product: Product,
    val variant: ProductVariant,
    val quantity: Int
) {
    val lineTotal: Int get() = variant.priceInr * quantity
    val lineMrpTotal: Int get() = variant.mrpInr * quantity
}

data class SavedAddress(
    val id: String,
    val label: String, // Home, Work, Other
    val fullAddress: String,
    val landmark: String,
    val neighborhoodId: String,
    val isDefault: Boolean = false
)

enum class PaymentMethod(val label: String, val subtitle: String) {
    UPI("UPI Instant", "GPay, PhonePe, Paytm • Zero fee"),
    CARD("Credit / Debit Card", "Visa, Mastercard, RuPay"),
    COD("Cash on Delivery", "Pay via cash or UPI QR at doorstep")
}

data class PromoCode(
    val code: String,
    val title: String,
    val discountInr: Int,
    val minOrderInr: Int
)

data class DeliverySlot(
    val id: String,
    val dayLabel: String, // "Now", "Today", "Tomorrow"
    val title: String,
    val windowText: String,
    val badge: String?,
    val isExpress: Boolean,
    val etaMinutes: Int
) {
    val displaySummary: String
        get() = if (isExpress) "Express • $etaMinutes mins" else "$dayLabel • $windowText"
}

data class BillBreakdown(
    val itemTotal: Int,
    val mrpSavings: Int,
    val deliveryFee: Int,
    val handlingCharge: Int,
    val promoDiscount: Int,
    val tipAmount: Int,
    val grandTotal: Int,
    val freeDeliveryThreshold: Int = 199
) {
    val isFreeDelivery: Boolean get() = itemTotal >= freeDeliveryThreshold
    val amountForFreeDelivery: Int get() = (freeDeliveryThreshold - itemTotal).coerceAtLeast(0)
    val freeDeliveryProgress: Float
        get() = (itemTotal.toFloat() / freeDeliveryThreshold.toFloat()).coerceIn(0f, 1f)
}

enum class OrderStage(
    val stepIndex: Int,
    val title: String,
    val subtitle: String,
    val notificationBody: String
) {
    CONFIRMED(
        stepIndex = 0,
        title = "Confirmed",
        subtitle = "Dark store received your order",
        notificationBody = "Order confirmed! Our dark-store picker is picking up your basket."
    ),
    PACKING(
        stepIndex = 1,
        title = "Packing",
        subtitle = "Quality-checked & sealed in cold bag",
        notificationBody = "Packing in progress — fresh items sealed and chilled for dispatch."
    ),
    OUT_FOR_DELIVERY(
        stepIndex = 2,
        title = "Out for Delivery",
        subtitle = "Rider is zooming to your doorstep",
        notificationBody = "Out for delivery! Your QuickKart rider is minutes away."
    ),
    DELIVERED(
        stepIndex = 3,
        title = "Delivered",
        subtitle = "Handed over safely at your door",
        notificationBody = "Delivered! Hope you enjoy your fresh QuickKart order."
    );

    fun next(): OrderStage? = when (this) {
        CONFIRMED -> PACKING
        PACKING -> OUT_FOR_DELIVERY
        OUT_FOR_DELIVERY -> DELIVERED
        DELIVERED -> null
    }

    fun previous(): OrderStage? = when (this) {
        CONFIRMED -> null
        PACKING -> CONFIRMED
        OUT_FOR_DELIVERY -> PACKING
        DELIVERED -> OUT_FOR_DELIVERY
    }
}

data class OrderLineItem(
    val productId: String,
    val productName: String,
    val variantId: String,
    val variantLabel: String,
    val category: ProductCategory,
    val quantity: Int,
    val unitPriceInr: Int,
    val unitMrpInr: Int
) {
    val lineTotal: Int get() = unitPriceInr * quantity
}

data class OrderRecord(
    val orderId: String,
    val createdAtMillis: Long,
    val neighborhoodName: String,
    val etaMinutes: Int,
    val remainingSeconds: Int,
    val stage: OrderStage,
    val items: List<OrderLineItem>,
    val deliveryAddressLabel: String,
    val deliveryAddressFull: String,
    val deliveryInstructions: List<String>,
    val riderNote: String,
    val paymentMethod: PaymentMethod,
    val riderName: String,
    val riderPhone: String,
    val riderRating: Float,
    val itemTotal: Int,
    val mrpSavings: Int,
    val deliveryFee: Int,
    val handlingCharge: Int,
    val promoDiscount: Int,
    val tipAmount: Int,
    val totalPaid: Int,
    val userRating: Int? = null,
    val praiseTags: List<String> = emptyList(),
    val feedbackComment: String = "",
    val deliveryWindowLabel: String = "Express • 8 mins"
) {
    val isActive: Boolean get() = stage != OrderStage.DELIVERED
    val totalItemCount: Int get() = items.sumOf { it.quantity }
}

data class RestockSuggestion(
    val product: Product,
    val daysSinceLastOrder: Int,
    val avgIntervalDays: Int,
    val orderCount: Int
)

enum class OrderHistoryTimeFilter(val label: String, val days: Int?) {
    LAST_30_DAYS("Last 30 Days", 30),
    LAST_3_MONTHS("Last 3 Months", 90),
    ALL_TIME("All Time", null)
}

fun filterCatalogProducts(
    products: List<Product>,
    selectedCategory: ProductCategory,
    searchQuery: String,
    favoritesOnly: Boolean,
    favoriteIds: Set<String>
): List<Product> {
    val q = searchQuery.trim()
    return products.filter { product ->
        val matchesCat = selectedCategory == ProductCategory.ALL || product.category == selectedCategory
        val matchesFav = !favoritesOnly || product.id in favoriteIds
        val matchesQuery = q.isEmpty() ||
            product.name.contains(q, ignoreCase = true) ||
            product.brand.contains(q, ignoreCase = true) ||
            product.category.label.contains(q, ignoreCase = true) ||
            product.description.contains(q, ignoreCase = true)
        matchesCat && matchesFav && matchesQuery
    }
}

enum class SupportIssueCategory(
    val label: String,
    val subtitle: String,
    val requiresItemSelection: Boolean,
    val defaultPrompts: List<String>
) {
    MISSING_ITEM(
        label = "Missing Item",
        subtitle = "Item(s) didn't arrive in the bag",
        requiresItemSelection = true,
        defaultPrompts = listOf(
            "Item missing from sealed bag",
            "Received fewer units than ordered"
        )
    ),
    DAMAGED_SPOILED(
        label = "Damaged / Spoiled",
        subtitle = "Leaking, crushed, or not fresh",
        requiresItemSelection = true,
        defaultPrompts = listOf(
            "Packaging leaked or crushed",
            "Cold-chain item was warm / spoiled"
        )
    ),
    WRONG_ITEM(
        label = "Wrong Item",
        subtitle = "Received a different product or size",
        requiresItemSelection = true,
        defaultPrompts = listOf(
            "Wrong variant / pack size delivered",
            "Swapped item inside the bag"
        )
    ),
    DELIVERY_DELAY(
        label = "Delivery Delay",
        subtitle = "Order arrived late or rider stuck",
        requiresItemSelection = false,
        defaultPrompts = listOf(
            "Order delayed past promised ETA",
            "Rider needed help finding location"
        )
    ),
    BILLING_PROMO(
        label = "Billing / Promo",
        subtitle = "Coupon discount or payment query",
        requiresItemSelection = false,
        defaultPrompts = listOf(
            "Promo discount not reflected",
            "Double charge or UPI settlement query"
        )
    ),
    OTHER(
        label = "Other Support",
        subtitle = "Packaging, rider, or invoice help",
        requiresItemSelection = false,
        defaultPrompts = listOf(
            "Delivery instruction not followed",
            "Need help with order details"
        )
    )
}

enum class SupportResolutionOption(
    val title: String,
    val subtitle: String
) {
    INSTANT_CREDIT(
        title = "Instant QuickKart Credit",
        subtitle = "Credited to wallet in under 10 seconds"
    ),
    EXPRESS_REPLACEMENT(
        title = "Free 8-Min Replacement",
        subtitle = "Priority rider dispatch at ₹0 extra"
    ),
    SPECIALIST_CHAT(
        title = "Chat with Support Specialist",
        subtitle = "Priority live desk review in ~2 mins"
    )
}

enum class SupportTicketStatus(val stepIndex: Int, val label: String) {
    OPEN(0, "Submitted"),
    UNDER_REVIEW(1, "Under Review"),
    RESOLVED(2, "Resolved")
}

data class SupportMessage(
    val id: String,
    val isUser: Boolean,
    val text: String,
    val timestampMillis: Long
)

data class SupportTicketRecord(
    val ticketId: String,
    val orderId: String,
    val createdAtMillis: Long,
    val updatedAtMillis: Long,
    val category: SupportIssueCategory,
    val affectedItemsSummary: String,
    val affectedAmountInr: Int,
    val preferredResolution: SupportResolutionOption,
    val resolutionSummary: String,
    val status: SupportTicketStatus,
    val photoUriString: String? = null,
    val messages: List<SupportMessage> = emptyList(),
    val csatRating: Int? = null
)

data class SupportFaqTopic(
    val id: String,
    val question: String,
    val answer: String,
    val linkedCategory: SupportIssueCategory
)

data class SupportInitialOutcome(
    val status: SupportTicketStatus,
    val resolutionSummary: String,
    val deskMessage: String
)

data class SupportDeskAction(
    val replyText: String,
    val newStatus: SupportTicketStatus? = null,
    val updatedResolutionSummary: String? = null
)

fun orderLineItemKey(item: OrderLineItem): String = "${item.productId}::${item.variantId}"

fun computeSupportRefundEstimate(
    order: OrderRecord,
    selectedItemKeys: Set<String>,
    category: SupportIssueCategory
): Int {
    if (order.totalPaid <= 0) return 0
    if (selectedItemKeys.isNotEmpty()) {
        val selectedSum = order.items
            .filter { orderLineItemKey(it) in selectedItemKeys }
            .sumOf { it.lineTotal }
        if (selectedSum > 0) {
            return selectedSum.coerceIn(1, order.totalPaid)
        }
    }
    return when (category) {
        SupportIssueCategory.DELIVERY_DELAY -> (order.deliveryFee.takeIf { it > 0 } ?: 25).coerceAtMost(order.totalPaid)
        SupportIssueCategory.BILLING_PROMO -> (order.handlingCharge + 30).coerceAtMost(order.totalPaid)
        SupportIssueCategory.OTHER -> 25.coerceAtMost(order.totalPaid)
        else -> 0
    }
}

fun resolveInitialTicketOutcome(
    ticketId: String,
    order: OrderRecord,
    category: SupportIssueCategory,
    affectedItemsSummary: String,
    affectedAmountInr: Int,
    preferredResolution: SupportResolutionOption,
    hasPhoto: Boolean
): SupportInitialOutcome {
    val photoNote = if (hasPhoto) " Photo proof received." else ""
    val canInstantCredit = preferredResolution == SupportResolutionOption.INSTANT_CREDIT &&
        affectedAmountInr in 1..250 &&
        category.requiresItemSelection

    return when {
        canInstantCredit -> SupportInitialOutcome(
            status = SupportTicketStatus.RESOLVED,
            resolutionSummary = "₹$affectedAmountInr Instant QuickKart Credit added",
            deskMessage = "We're sorry about ${category.label.lowercase()} in ${order.orderId} ($affectedItemsSummary).$photoNote Per QuickKart's Instant Care Promise, ₹$affectedAmountInr has been credited to your QuickKart Wallet under ref $ticketId."
        )

        preferredResolution == SupportResolutionOption.EXPRESS_REPLACEMENT && category.requiresItemSelection -> SupportInitialOutcome(
            status = SupportTicketStatus.UNDER_REVIEW,
            resolutionSummary = "8-Min Replacement pod verification queued",
            deskMessage = "Logged under $ticketId for ${order.orderId} ($affectedItemsSummary).$photoNote Our Dark Store Pod lead is verifying fresh stock for a ₹0 8-minute replacement dispatch. Reply below if you prefer ₹$affectedAmountInr Instant Credit instead."
        )

        else -> {
            val estimateText = if (affectedAmountInr > 0) " (eligible credit up to ₹$affectedAmountInr)" else ""
            SupportInitialOutcome(
                status = SupportTicketStatus.UNDER_REVIEW,
                resolutionSummary = "Assigned to QuickKart Care Desk",
                deskMessage = "Ticket $ticketId is now Under Review for ${order.orderId} — ${category.label}$estimateText.$photoNote A Care Specialist is checking pod & rider logs and can process a credit or callback right here."
            )
        }
    }
}

fun generateSupportDeskReply(
    ticket: SupportTicketRecord,
    userText: String
): SupportDeskAction {
    val lower = userText.trim().lowercase()
    val creditAmount = ticket.affectedAmountInr.coerceAtLeast(25)

    return when {
        listOf("credit", "refund", "wallet", "money").any { it in lower } -> SupportDeskAction(
            replyText = "Done! We've processed ₹$creditAmount as Instant QuickKart Credit for ${ticket.orderId} (Ticket ${ticket.ticketId}). It is available immediately at checkout.",
            newStatus = SupportTicketStatus.RESOLVED,
            updatedResolutionSummary = "₹$creditAmount QuickKart Credit issued"
        )

        listOf("replace", "dispatch", "resend", "send again").any { it in lower } -> SupportDeskAction(
            replyText = "Confirmed! A complimentary 8-Min Express replacement for ${ticket.affectedItemsSummary} on ${ticket.orderId} has been queued with our Dark Store rider.",
            newStatus = SupportTicketStatus.RESOLVED,
            updatedResolutionSummary = "Free 8-Min Replacement dispatched"
        )

        listOf("call", "agent", "specialist", "human", "manager").any { it in lower } -> SupportDeskAction(
            replyText = "Priority callback requested for ${ticket.ticketId}. Our Senior Pod Specialist will ring your registered number within 2 minutes.",
            newStatus = SupportTicketStatus.UNDER_REVIEW,
            updatedResolutionSummary = "Priority specialist callback queued (~2 mins)"
        )

        listOf("thank", "resolved", "got it", "received", "sorted", "done").any { it in lower } -> SupportDeskAction(
            replyText = "Glad we could sort out ${ticket.orderId}! We've marked ${ticket.ticketId} as Resolved. Please share a quick star rating below whenever you have a moment.",
            newStatus = SupportTicketStatus.RESOLVED,
            updatedResolutionSummary = ticket.resolutionSummary
        )

        else -> SupportDeskAction(
            replyText = "Thanks for the update on ${ticket.orderId} (${ticket.category.label}). We've noted this on ${ticket.ticketId} — tap a quick action below anytime for an instant ₹$creditAmount credit or replacement.",
            newStatus = SupportTicketStatus.UNDER_REVIEW,
            updatedResolutionSummary = "Active review by QuickKart Care Desk"
        )
    }
}

