package com.example.rider.data.model

import java.util.Locale
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

const val COD_FLOATING_LIMIT_INR = 2000
const val OFFER_TIMEOUT_SECONDS = 30

enum class RiderVehicleType(val label: String, val badge: String) {
    ELECTRIC_SCOOTER("EV Scooter", "Zero-Emission EV"),
    E_CYCLE("Cargo E-Cycle", "Micro-Pod Cycle"),
    MOTORCYCLE("Motorcycle", "Express 2W")
}

enum class RiderPaymentMode(val label: String, val shortLabel: String) {
    PREPAID_UPI("Prepaid via UPI", "Prepaid UPI"),
    PREPAID_CARD("Prepaid via Card", "Prepaid Card"),
    COD("Cash on Delivery", "Collect COD")
}

enum class RiderTripStage(
    val label: String,
    val shortActionHint: String,
    val stepIndex: Int
) {
    OFFERED("Incoming Offer", "Accept within countdown", 0),
    HEADING_TO_HUB("Heading to Dark Store", "Reach Pod pickup bay", 1),
    PICKING_AT_POD("Packing & Sealing at Pod", "Scan items & seal thermal bag", 2),
    OUT_FOR_DELIVERY("Out for Delivery", "Navigate to customer door", 3),
    ARRIVED_AT_DOOR("At Customer Doorstep", "Verify 4-digit OTP to complete", 4),
    DELIVERED("Delivered", "Payout credited to wallet", 5),
    DECLINED("Declined / Missed", "Reassigned to nearby rider", 0);

    fun isActiveRun(): Boolean = this == HEADING_TO_HUB ||
        this == PICKING_AT_POD ||
        this == OUT_FOR_DELIVERY ||
        this == ARRIVED_AT_DOOR
}

enum class PayoutTxType(val label: String) {
    BANK_WITHDRAWAL("Bank Transfer"),
    COD_DEPOSIT("COD Cash Settled"),
    INCENTIVE_BONUS("Milestone Bonus")
}

data class RiderHub(
    val id: String,
    val neighborhood: String,
    val city: String,
    val podName: String,
    val bayPrefix: String,
    val latitude: Double,
    val longitude: Double,
    val surgeBonusInr: Int,
    val demandLevel: String
)

data class TripLineItem(
    val productId: String,
    val name: String,
    val variantLabel: String,
    val quantity: Int,
    val unitPriceInr: Int,
    val isPicked: Boolean = false
)

data class RiderTripOrder(
    val orderId: String,
    val hubId: String,
    val hubName: String,
    val pickupBayCode: String,
    val bagSealCode: String,
    val hubLat: Double,
    val hubLng: Double,
    val customerName: String,
    val customerPhone: String,
    val deliveryAddressLabel: String,
    val deliveryAddressFull: String,
    val dropLat: Double,
    val dropLng: Double,
    val deliveryInstructions: List<String>,
    val riderNote: String,
    val paymentMode: RiderPaymentMode,
    val collectAmountInr: Int,
    val deliveryOtp: String,
    val basePayInr: Int,
    val distancePayInr: Int,
    val surgeBonusInr: Int,
    val tipInr: Int,
    val onTimeBonusInr: Int,
    val distanceKm: Double,
    val estimatedMinutes: Int,
    val deliveryWindowLabel: String,
    val stage: RiderTripStage,
    val items: List<TripLineItem>,
    val podSealed: Boolean = false,
    val codCollected: Boolean = false,
    val podPhotoUri: String? = null,
    val createdAtMillis: Long,
    val acceptedAtMillis: Long? = null,
    val deliveredAtMillis: Long? = null,
    val offerExpiresAtMillis: Long? = null
) {
    val totalRiderPayoutInr: Int
        get() = basePayInr + distancePayInr + surgeBonusInr + tipInr + onTimeBonusInr

    val totalUnits: Int
        get() = items.sumOf { it.quantity }

    val pickedItemCount: Int
        get() = items.count { it.isPicked }

    val allItemsPicked: Boolean
        get() = items.isNotEmpty() && items.all { it.isPicked }

    val coordinatesLabel: String
        get() = String.format(Locale.US, "%.4f, %.4f", dropLat, dropLng)
}

data class PayoutTransaction(
    val id: String,
    val type: PayoutTxType,
    val title: String,
    val amountInr: Int,
    val createdAtMillis: Long
)

data class IncentiveTier(
    val targetDrops: Int,
    val rewardInr: Int,
    val label: String
)

data class RiderEarningsSummary(
    val completedDrops: Int,
    val declinedCount: Int,
    val onTimeDrops: Int,
    val slaPercent: Int,
    val baseAndDistanceInr: Int,
    val surgeEarnedInr: Int,
    val tipsEarnedInr: Int,
    val onTimeBonusInr: Int,
    val unlockedIncentiveInr: Int,
    val nextIncentiveTier: IncentiveTier?,
    val totalEarnedInr: Int,
    val withdrawnInr: Int,
    val availableWalletInr: Int,
    val codCollectedGrossInr: Int,
    val codSettledInr: Int,
    val codCashInHandInr: Int,
    val isCodLimitExceeded: Boolean
)

fun computeTripEarningsBreakdown(
    trips: List<RiderTripOrder>,
    transactions: List<PayoutTransaction>,
    tiers: List<IncentiveTier> = defaultIncentiveTiers()
): RiderEarningsSummary {
    val delivered = trips.filter { it.stage == RiderTripStage.DELIVERED }
    val declined = trips.count { it.stage == RiderTripStage.DECLINED }

    val baseAndDist = delivered.sumOf { it.basePayInr + it.distancePayInr }
    val surge = delivered.sumOf { it.surgeBonusInr }
    val tips = delivered.sumOf { it.tipInr }
    val onTimeBonus = delivered.sumOf { it.onTimeBonusInr }
    val onTimeDrops = delivered.count { it.onTimeBonusInr > 0 }
    val slaPercent = if (delivered.isEmpty()) 100 else ((onTimeDrops * 100f) / delivered.size).roundToInt()

    val dropsCount = delivered.size
    val unlockedIncentive = tiers.filter { dropsCount >= it.targetDrops }.sumOf { it.rewardInr }
    val nextTier = tiers.firstOrNull { dropsCount < it.targetDrops }

    val totalEarned = baseAndDist + surge + tips + onTimeBonus + unlockedIncentive

    val withdrawn = transactions
        .filter { it.type == PayoutTxType.BANK_WITHDRAWAL }
        .sumOf { it.amountInr }
    val availableWallet = (totalEarned - withdrawn).coerceAtLeast(0)

    val codCollectedGross = delivered
        .filter { it.paymentMode == RiderPaymentMode.COD }
        .sumOf { it.collectAmountInr }
    val codSettled = transactions
        .filter { it.type == PayoutTxType.COD_DEPOSIT }
        .sumOf { it.amountInr }
    val codCashInHand = (codCollectedGross - codSettled).coerceAtLeast(0)

    return RiderEarningsSummary(
        completedDrops = dropsCount,
        declinedCount = declined,
        onTimeDrops = onTimeDrops,
        slaPercent = slaPercent,
        baseAndDistanceInr = baseAndDist,
        surgeEarnedInr = surge,
        tipsEarnedInr = tips,
        onTimeBonusInr = onTimeBonus,
        unlockedIncentiveInr = unlockedIncentive,
        nextIncentiveTier = nextTier,
        totalEarnedInr = totalEarned,
        withdrawnInr = withdrawn,
        availableWalletInr = availableWallet,
        codCollectedGrossInr = codCollectedGross,
        codSettledInr = codSettled,
        codCashInHandInr = codCashInHand,
        isCodLimitExceeded = codCashInHand >= COD_FLOATING_LIMIT_INR
    )
}

fun verifyDeliveryCompletion(
    trip: RiderTripOrder,
    enteredOtp: String,
    codConfirmed: Boolean
): String? {
    val cleanOtp = enteredOtp.trim()
    if (cleanOtp.length != 4 || !cleanOtp.all { it.isDigit() }) {
        return "Enter the customer's 4-digit delivery PIN."
    }
    if (cleanOtp != trip.deliveryOtp) {
        return "PIN mismatch — ask the customer to check their QuickKart order screen."
    }
    if (trip.paymentMode == RiderPaymentMode.COD && !codConfirmed) {
        return "Confirm ₹${trip.collectAmountInr} COD cash collected before closing trip."
    }
    return null
}

fun defaultIncentiveTiers(): List<IncentiveTier> = listOf(
    IncentiveTier(targetDrops = 4, rewardInr = 100, label = "Morning Sprint"),
    IncentiveTier(targetDrops = 8, rewardInr = 250, label = "Peak Rush Star"),
    IncentiveTier(targetDrops = 12, rewardInr = 500, label = "Super Partner")
)

fun haversineKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val r = 6371.0
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = sin(dLat / 2) * sin(dLat / 2) +
        cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
        sin(dLon / 2) * sin(dLon / 2)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return ((r * c) * 10.0).roundToInt() / 10.0
}

private data class DropTemplate(
    val customerName: String,
    val customerPhone: String,
    val label: String,
    val street: String,
    val dLat: Double,
    val dLng: Double,
    val instructions: List<String>,
    val note: String,
    val paymentMode: RiderPaymentMode,
    val tipInr: Int,
    val items: List<TripLineItem>
)

private val dispatchTemplates = listOf(
    DropTemplate(
        customerName = "Ananya Verma",
        customerPhone = "+91 98451 22410",
        label = "Home",
        street = "Flat 402, Palm Grove Residency, 12th Main Rd",
        dLat = -0.0068,
        dLng = 0.0058,
        instructions = listOf("Avoid ringing bell", "Leave with guard if no response"),
        note = "2nd tower lift, call once at lobby intercom #402",
        paymentMode = RiderPaymentMode.PREPAID_UPI,
        tipInr = 20,
        items = listOf(
            TripLineItem("milk_1", "Fresh A2 Full Cream Milk", "1 L", 2, 72),
            TripLineItem("bread_1", "Sourdough Multigrain Loaf", "400 g", 1, 95),
            TripLineItem("avo_1", "Hass Avocado Twin Pack", "2 pcs", 1, 219)
        )
    ),
    DropTemplate(
        customerName = "Rohan Kulkarni",
        customerPhone = "+91 99002 66183",
        label = "Studio",
        street = "3rd Floor, Bricklane Co-work, 80 Feet Rd",
        dLat = 0.0054,
        dLng = -0.0042,
        instructions = listOf("Call on arrival"),
        note = "Hand over to reception desk near coffee bar",
        paymentMode = RiderPaymentMode.COD,
        tipInr = 0,
        items = listOf(
            TripLineItem("coldbrew_1", "Artisanal Iced Cold Brew", "250 ml", 3, 130),
            TripLineItem("chips_1", "Kettle Sea Salt Chips", "150 g", 2, 85),
            TripLineItem("choco_1", "70% Dark Hazelnut Bar", "90 g", 1, 189)
        )
    ),
    DropTemplate(
        customerName = "Meera Nair",
        customerPhone = "+91 98118 40921",
        label = "Apartment",
        street = "B-704, Verandah Heights, 16th Cross",
        dLat = -0.0041,
        dLng = -0.0065,
        instructions = listOf("Leave at doorstep", "No cutlery needed"),
        note = "Keep ice-cream pouch upright at the door mat",
        paymentMode = RiderPaymentMode.PREPAID_CARD,
        tipInr = 30,
        items = listOf(
            TripLineItem("gelato_1", "Belgian Chocolate Gelato Tub", "500 ml", 1, 349),
            TripLineItem("mango_1", "Alphonso Mango Box", "1 kg", 1, 299),
            TripLineItem("curd_1", "Probiotic Set Curd", "400 g", 2, 48)
        )
    ),
    DropTemplate(
        customerName = "Kabir Malhotra",
        customerPhone = "+91 97409 11234",
        label = "Office",
        street = "Suite 12, Apex Tech Square",
        dLat = 0.0072,
        dLng = 0.0049,
        instructions = listOf("Verify security gate pass"),
        note = "Security desk holds elevator card",
        paymentMode = RiderPaymentMode.COD,
        tipInr = 15,
        items = listOf(
            TripLineItem("water_1", "Himalayan Sparkling Water", "750 ml", 2, 95),
            TripLineItem("salad_1", "Roasted Quinoa Protein Bowl", "280 g", 2, 225)
        )
    )
)

fun generateDispatchOffer(
    hub: RiderHub,
    nowMillis: Long,
    sequenceIndex: Int = ((nowMillis / 1000L) % 97L).toInt()
): RiderTripOrder {
    val template = dispatchTemplates[kotlin.math.abs(sequenceIndex) % dispatchTemplates.size]
    val dropLat = hub.latitude + template.dLat
    val dropLng = hub.longitude + template.dLng
    val distKm = haversineKm(hub.latitude, hub.longitude, dropLat, dropLng).coerceAtLeast(0.8)
    val basePay = 25
    val distPay = ((distKm - 0.5).coerceAtLeast(0.0) * 14.0).roundToInt().coerceAtLeast(10)
    val orderNum = 4100 + kotlin.math.abs((nowMillis % 5800L).toInt())
    val otp = (1000 + kotlin.math.abs(((nowMillis / 7) % 8999L).toInt())).toString()
    val baySlot = "B${(kotlin.math.abs(sequenceIndex) % 9) + 1}-${10 + (orderNum % 89)}"
    val seal = "QK-SEAL-${100 + (orderNum % 890)}"
    val collectAmount = if (template.paymentMode == RiderPaymentMode.COD) {
        template.items.sumOf { it.unitPriceInr * it.quantity }
    } else {
        0
    }
    val estMins = (6 + (distKm * 3.5).roundToInt()).coerceIn(7, 16)

    return RiderTripOrder(
        orderId = "QK-$orderNum",
        hubId = hub.id,
        hubName = hub.podName,
        pickupBayCode = "${hub.bayPrefix}-$baySlot",
        bagSealCode = seal,
        hubLat = hub.latitude,
        hubLng = hub.longitude,
        customerName = template.customerName,
        customerPhone = template.customerPhone,
        deliveryAddressLabel = template.label,
        deliveryAddressFull = "${template.street}, ${hub.neighborhood}, ${hub.city}",
        dropLat = dropLat,
        dropLng = dropLng,
        deliveryInstructions = template.instructions,
        riderNote = template.note,
        paymentMode = template.paymentMode,
        collectAmountInr = collectAmount,
        deliveryOtp = otp,
        basePayInr = basePay,
        distancePayInr = distPay,
        surgeBonusInr = hub.surgeBonusInr,
        tipInr = template.tipInr,
        onTimeBonusInr = 10,
        distanceKm = distKm,
        estimatedMinutes = estMins,
        deliveryWindowLabel = "Express ${estMins}-min SLA",
        stage = RiderTripStage.OFFERED,
        items = template.items,
        podSealed = false,
        codCollected = false,
        podPhotoUri = null,
        createdAtMillis = nowMillis,
        acceptedAtMillis = null,
        deliveredAtMillis = null,
        offerExpiresAtMillis = nowMillis + OFFER_TIMEOUT_SECONDS * 1000L
    )
}
