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
    val feedbackComment: String = ""
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
