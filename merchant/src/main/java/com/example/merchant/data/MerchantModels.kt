package com.example.merchant.data

/*
 * Merchant (store partner) domain models. Design prototype: everything is simulated locally,
 * there is no backend. Amounts are whole rupees.
 */

enum class StoreType(val label: String) { KIRANA("Kirana"), GROCERY("Grocery"), RESTAURANT("Restaurant") }

/** Document verification state shown as a badge (KYC). */
enum class DocStatus(val label: String) {
    NOT_UPLOADED("Not uploaded"),
    PENDING("Pending verification"),
    VERIFIED("Verified"),
    REJECTED("Rejected")
}

data class MerchantProfile(
    val phone: String = "",
    val storeName: String = "",
    val ownerName: String = "",
    val storeType: StoreType = StoreType.KIRANA,
    val address: String = "",
    val fssaiNumber: String = "",
    val fssaiStatus: DocStatus = DocStatus.NOT_UPLOADED,
    val gstin: String = "",
    val gstinStatus: DocStatus = DocStatus.NOT_UPLOADED,
    val bankName: String = "",
    val accountNumber: String = "",
    val ifsc: String = "",
    val kycSubmitted: Boolean = false
) {
    val accountMasked: String
        get() = if (accountNumber.length >= 4) "•••• ${accountNumber.takeLast(4)}" else "Not added"

    /** Store can only take orders once the licences are verified. */
    val kycVerified: Boolean
        get() = kycSubmitted && fssaiStatus == DocStatus.VERIFIED && gstinStatus == DocStatus.VERIFIED
}

enum class PaymentMode(val label: String) { UPI("UPI"), CARD("Card"), COD("Cash on Delivery") }

enum class OrderStatus(val label: String) {
    NEW("New"),
    PREPARING("Preparing"),
    READY("Ready for pickup"),
    HANDED_OVER("Out for delivery"),
    DELIVERED("Delivered"),
    REJECTED("Rejected"),
    MISSED("Missed"),
    CANCELLED("Cancelled by customer")
}

data class OrderLine(
    val productId: String,
    val name: String,
    val quantity: Int,
    val unitPrice: Int,
    /** Quantity the store can actually supply (partial fulfilment); equals [quantity] normally. */
    val fulfilledQuantity: Int = quantity,
    val substituteName: String? = null
) {
    val lineTotal: Int get() = unitPrice * quantity
    val fulfilledTotal: Int get() = unitPrice * fulfilledQuantity
    val refundAmount: Int get() = if (substituteName != null) 0 else unitPrice * (quantity - fulfilledQuantity)
    val isShort: Boolean get() = fulfilledQuantity < quantity
}

enum class RiderStage(val label: String) { ASSIGNING("Finding a rider"), ON_THE_WAY("On the way to store"), AT_STORE("Arrived at store") }

data class AssignedRider(
    val name: String,
    val vehicle: String,
    val phone: String,
    val rating: Float,
    val stage: RiderStage,
    val etaMinutes: Int
)

data class StoreOrder(
    val id: String,
    val customerName: String,
    val customerArea: String,
    val lines: List<OrderLine>,
    val placedAtMillis: Long,
    val status: OrderStatus,
    val paymentMode: PaymentMode,
    val deliveryFee: Int = 0,
    val prepMinutes: Int = 15,
    val acceptedAtMillis: Long? = null,
    val rider: AssignedRider? = null,
    val packedLineIds: Set<String> = emptySet(),
    val billAttached: Boolean = false,
    val bagSealed: Boolean = false,
    val rejectReason: String? = null,
    val isPartial: Boolean = false,
    val customerRating: Int? = null,
    val note: String? = null
) {
    val itemCount: Int get() = lines.sumOf { it.quantity }
    val itemTotal: Int get() = lines.sumOf { it.lineTotal }
    val refundTotal: Int get() = lines.sumOf { it.refundAmount }
    val payableTotal: Int get() = itemTotal - refundTotal + deliveryFee
    val isActive: Boolean get() = status in setOf(OrderStatus.NEW, OrderStatus.PREPARING, OrderStatus.READY)
    val shortId: String get() = "#${id.removePrefix("QK")}"
}

data class CatalogueItem(
    val id: String,
    val name: String,
    val description: String,
    val category: String,
    val price: Int,
    val discountPercent: Int,
    val stock: Int,
    val active: Boolean = true,
    val unit: String = "1 pc",
    /** Content URI of a photo picked from the gallery (prototype: kept for this session). */
    val photoUri: String? = null
) {
    val sellingPrice: Int get() = (price * (100 - discountPercent) / 100.0).toInt()
    val isOutOfStock: Boolean get() = stock <= 0
    val isLowStock: Boolean get() = stock in 1..LOW_STOCK_THRESHOLD

    companion object {
        const val LOW_STOCK_THRESHOLD = 5
        val CATEGORIES = listOf(
            "Grains & Atta", "Dairy & Eggs", "Fruits", "Vegetables", "Snacks", "Beverages",
            "Personal Care", "Household", "Ready to Eat"
        )
    }
}

enum class OfferType(val label: String) {
    PERCENT_OFF("% Off"),
    FLAT_OFF("Flat ₹ Off"),
    FREE_DELIVERY("Free Delivery")
}

data class StoreOffer(
    val id: String,
    val type: OfferType,
    val value: Int,
    val minOrder: Int,
    val validFromMillis: Long,
    val validToMillis: Long,
    val active: Boolean,
    val category: String? = null
) {
    val title: String
        get() = when (type) {
            OfferType.PERCENT_OFF -> "$value% off" + (category?.let { " on $it" } ?: "")
            OfferType.FLAT_OFF -> "Flat ₹$value off"
            OfferType.FREE_DELIVERY -> "Free delivery"
        } + if (minOrder > 0) " above ₹$minOrder" else ""

    fun isExpired(now: Long): Boolean = validToMillis < now
}

enum class PayoutStatus(val label: String) { PAID("Paid"), PENDING("Pending"), PROCESSING("Processing") }

data class Payout(
    val id: String,
    val periodLabel: String,
    val dateMillis: Long,
    val grossSales: Int,
    val commission: Int,
    val amount: Int,
    val status: PayoutStatus,
    val utr: String? = null
)

enum class AlertType { NEW_ORDER, RIDER_ARRIVED, LOW_STOCK, PAYOUT, ORDER_CANCELLED, SYSTEM }

data class MerchantAlert(
    val id: String,
    val type: AlertType,
    val title: String,
    val body: String,
    val atMillis: Long,
    val read: Boolean = false,
    val orderId: String? = null
)

enum class TicketStatus(val label: String) { OPEN("Open"), IN_PROGRESS("In progress"), RESOLVED("Resolved") }

data class SupportTicket(
    val id: String,
    val category: String,
    val subject: String,
    val description: String,
    val orderId: String?,
    val createdAtMillis: Long,
    val status: TicketStatus
)

data class StoreSettings(
    val isOpen: Boolean = true,
    val serviceRadiusKm: Float = 3f,
    val openMinuteOfDay: Int = 8 * 60,
    val closeMinuteOfDay: Int = 22 * 60,
    val autoAcceptOrders: Boolean = false,
    val orderSound: Boolean = true,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val zoomPercent: Int = 85 // Compact by default
)

enum class ThemeMode(val label: String) { LIGHT("Light"), DARK("Dark"), SYSTEM("System") }

/** Platform commission on gross item sales (prototype rate). */
const val PLATFORM_COMMISSION_PERCENT = 12
