package com.example.rider.data

/*
 * Delivery partner (rider) domain models. Design prototype: dispatch, payments and verification
 * are simulated locally. Amounts are whole rupees, distances in km.
 */

enum class VehicleType(val label: String) { BIKE("Bike"), SCOOTER("Scooter"), EV("EV scooter"), BICYCLE("Bicycle") }

enum class DocStatus(val label: String) { NOT_UPLOADED("Not added"), PENDING("Verifying"), VERIFIED("Verified"), REJECTED("Rejected") }

enum class KycDoc(val label: String) { LICENSE("Driving Licence"), RC("Vehicle RC"), BANK("Bank Account"), PHOTO("Profile Photo") }

data class RiderProfile(
    val phone: String = "",
    val name: String = "",
    val vehicleType: VehicleType = VehicleType.BIKE,
    val licenseNumber: String = "",
    val vehicleNumber: String = "",
    val bankAccount: String = "",
    val ifsc: String = "",
    val docs: Map<KycDoc, DocStatus> = KycDoc.entries.associateWith { DocStatus.NOT_UPLOADED },
    val rating: Float = 4.8f,
    val preferredZones: Set<String> = setOf("Madhapur", "Gachibowli", "Kondapur"),
    val shift: Shift = Shift.DAY,
    /** Set when the rider finishes KYC and taps Continue. */
    val onboarded: Boolean = false
) {
    fun status(doc: KycDoc): DocStatus = docs[doc] ?: DocStatus.NOT_UPLOADED
    val kycVerified: Boolean get() = requiredDocs.all { status(it) == DocStatus.VERIFIED }
    val bankMasked: String get() = if (bankAccount.length >= 4) "•••• ${bankAccount.takeLast(4)}" else "Not added"
    /** Bicycles have no RC. */
    val requiredDocs: List<KycDoc> get() = if (vehicleType == VehicleType.BICYCLE) KycDoc.entries - KycDoc.RC else KycDoc.entries
}

enum class Shift(val label: String, val hours: String) {
    MORNING("Morning", "6 AM – 12 PM"), DAY("Day", "9 AM – 6 PM"), EVENING("Evening", "4 PM – 11 PM"), NIGHT("Late night", "8 PM – 2 AM")
}

val HYDERABAD_ZONES = listOf("Madhapur", "Gachibowli", "Kondapur", "Hitech City", "Kukatpally", "Jubilee Hills", "Banjara Hills")

data class GeoPoint(val lat: Double, val lng: Double)

enum class PaymentMode(val label: String) { PREPAID("Prepaid"), COD("Cash on Delivery") }

/** Trip stages in order. */
enum class TripStage(val label: String) {
    TO_STORE("Heading to store"),
    AT_STORE("At store"),
    TO_CUSTOMER("Heading to customer"),
    AT_CUSTOMER("At customer"),
    DELIVERED("Delivered"),
    CANCELLED("Cancelled"),
    FAILED("Delivery failed"),
    RETURNED("Returned to store")
}

enum class StoreReadiness(val label: String) { PREPARING("Preparing"), READY("Ready") }

data class TripItem(val name: String, val quantity: Int)

data class Trip(
    val orderId: String,
    val storeName: String,
    val storeAddress: String,
    val storePhone: String,
    val store: GeoPoint,
    val customerName: String,
    val customerAddress: String,
    val customerPhone: String,
    val customer: GeoPoint,
    val items: List<TripItem>,
    val paymentMode: PaymentMode,
    val orderAmount: Int,
    val pickupKm: Double,
    val dropKm: Double,
    val basePay: Int,
    val distancePay: Int,
    val tip: Int = 0,
    val incentive: Int = 0,
    val customerOtp: String,
    val offeredAtMillis: Long,
    /** Where the rider accepted the trip (start of the pickup leg). */
    val origin: GeoPoint = GeoPoint(17.4483, 78.3915),
    val stage: TripStage = TripStage.TO_STORE,
    val readiness: StoreReadiness = StoreReadiness.PREPARING,
    /** 0..1 progress along the current leg (simulated travel). */
    val legProgress: Float = 0f,
    val pickedItems: Set<String> = emptySet(),
    val cashCollected: Boolean = false,
    val proofPhotoTaken: Boolean = false,
    val completedAtMillis: Long? = null,
    val cancelReason: String? = null,
    val calledCustomer: Boolean = false,
    val note: String? = null
) {
    val payout: Int get() = basePay + distancePay + incentive
    val totalEarning: Int get() = if (stage == TripStage.DELIVERED) payout + tip else if (stage == TripStage.RETURNED || stage == TripStage.FAILED) basePay else 0
    val itemCount: Int get() = items.sumOf { it.quantity }
    val isActive: Boolean get() = stage in setOf(TripStage.TO_STORE, TripStage.AT_STORE, TripStage.TO_CUSTOMER, TripStage.AT_CUSTOMER)
    val shortId: String get() = "#${orderId.removePrefix("QK")}"
    val totalKm: Double get() = pickupKm + dropKm
}

/** Incoming dispatch offer shown in the assignment alert. */
data class TripOffer(val trip: Trip, val expiresAtMillis: Long)

enum class CashEntryType { COLLECTED, DEPOSITED }

data class CashEntry(val id: String, val type: CashEntryType, val amount: Int, val atMillis: Long, val ref: String)

enum class TxType(val label: String) { TRIP("Trip"), TIP("Tip"), INCENTIVE("Incentive"), PAYOUT("Instant payout") }

data class EarningTx(val id: String, val type: TxType, val amount: Int, val atMillis: Long, val label: String)

data class RiderSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val zoomPercent: Int = 85, // Compact by default
    val sound: Boolean = true
)

enum class ThemeMode(val label: String) { LIGHT("Light"), DARK("Dark"), SYSTEM("System") }

/** Floating cash a rider may hold before depositing COD collections. */
const val FLOATING_CASH_LIMIT = 2000
const val ASSIGNMENT_WINDOW_MS = 30_000L
const val INSTANT_PAYOUT_MIN = 100

val CANCEL_REASONS = listOf("Store closed", "Long wait at store", "Vehicle breakdown", "Customer requested", "Unsafe to travel")
val FAILED_REASONS = listOf("Customer unreachable", "Wrong address", "Customer unavailable", "Payment issue", "Customer refused", "Unsafe location", "Other")
