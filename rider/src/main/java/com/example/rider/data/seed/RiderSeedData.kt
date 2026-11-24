package com.example.rider.data.seed

import com.example.rider.data.model.PayoutTransaction
import com.example.rider.data.model.PayoutTxType
import com.example.rider.data.model.RiderHub
import com.example.rider.data.model.RiderPaymentMode
import com.example.rider.data.model.RiderTripOrder
import com.example.rider.data.model.RiderTripStage
import com.example.rider.data.model.TripLineItem

object RiderSeedData {
    val hubs: List<RiderHub> = listOf(
        RiderHub(
            id = "hub_indiranagar",
            neighborhood = "Indiranagar",
            city = "Bengaluru",
            podName = "100ft Rd Pod #04",
            bayPrefix = "IND",
            latitude = 12.9784,
            longitude = 77.6408,
            surgeBonusInr = 15,
            demandLevel = "High Demand • +₹15 Surge"
        ),
        RiderHub(
            id = "hub_koramangala",
            neighborhood = "Koramangala",
            city = "Bengaluru",
            podName = "4th Block Pod #11",
            bayPrefix = "KRM",
            latitude = 12.9352,
            longitude = 77.6245,
            surgeBonusInr = 20,
            demandLevel = "Peak Surge • +₹20/drop"
        ),
        RiderHub(
            id = "hub_bandra",
            neighborhood = "Bandra West",
            city = "Mumbai",
            podName = "Pali Hill Pod #07",
            bayPrefix = "BND",
            latitude = 19.0607,
            longitude = 72.8270,
            surgeBonusInr = 15,
            demandLevel = "Busy Zone • +₹15 Surge"
        ),
        RiderHub(
            id = "hub_hauzkhas",
            neighborhood = "Hauz Khas",
            city = "New Delhi",
            podName = "Aurobindo Pod #02",
            bayPrefix = "HKV",
            latitude = 28.5494,
            longitude = 77.2001,
            surgeBonusInr = 10,
            demandLevel = "Steady Flow • +₹10 Surge"
        ),
        RiderHub(
            id = "hub_jubilee",
            neighborhood = "Jubilee Hills",
            city = "Hyderabad",
            podName = "Road No. 36 Pod #09",
            bayPrefix = "JBH",
            latitude = 17.4326,
            longitude = 78.4071,
            surgeBonusInr = 15,
            demandLevel = "High Demand • +₹15 Surge"
        )
    )

    fun hubById(id: String): RiderHub = hubs.firstOrNull { it.id == id } ?: hubs.first()

    fun initialTrips(nowMillis: Long): List<RiderTripOrder> {
        val defaultHub = hubs.first()
        return listOf(
            // 1. Live incoming offer ready for Rider review/accept
            RiderTripOrder(
                orderId = "QK-9084",
                hubId = defaultHub.id,
                hubName = defaultHub.podName,
                pickupBayCode = "IND-B2-18",
                bagSealCode = "QK-SEAL-742",
                hubLat = 12.9784,
                hubLng = 77.6408,
                customerName = "Siddharth Rao",
                customerPhone = "+91 98450 77312",
                deliveryAddressLabel = "Home",
                deliveryAddressFull = "42, 12th Main Rd, HAL 2nd Stage, Indiranagar, Bengaluru",
                dropLat = 12.9716,
                dropLng = 77.6466,
                deliveryInstructions = listOf("Leave at doorstep", "Avoid ringing bell"),
                riderNote = "Ring brass doorbell once; guard at main gate knows #42",
                paymentMode = RiderPaymentMode.PREPAID_UPI,
                collectAmountInr = 0,
                deliveryOtp = "4829",
                basePayInr = 25,
                distancePayInr = 18,
                surgeBonusInr = 15,
                tipInr = 25,
                onTimeBonusInr = 10,
                distanceKm = 1.4,
                estimatedMinutes = 9,
                deliveryWindowLabel = "Express 10-min SLA",
                stage = RiderTripStage.OFFERED,
                items = listOf(
                    TripLineItem("item_1", "Fresh Alphonso Mangoes", "1 kg", 1, 299),
                    TripLineItem("item_2", "Organic A2 Whole Milk", "1 L", 2, 78),
                    TripLineItem("item_3", "Cold-Pressed Valencia OJ", "300 ml", 2, 110)
                ),
                podSealed = false,
                codCollected = false,
                podPhotoUri = null,
                createdAtMillis = nowMillis - 5_000L,
                offerExpiresAtMillis = nowMillis + 30_000L
            ),
            // Completed drops earlier in today's shift so Earnings & COD Ledger look rich immediately
            RiderTripOrder(
                orderId = "QK-9041",
                hubId = defaultHub.id,
                hubName = defaultHub.podName,
                pickupBayCode = "IND-B1-04",
                bagSealCode = "QK-SEAL-615",
                hubLat = 12.9784,
                hubLng = 77.6408,
                customerName = "Priya Menon",
                customerPhone = "+91 98801 51204",
                deliveryAddressLabel = "Home",
                deliveryAddressFull = "78, 100 Feet Rd, Indira Nagar 1st Stage, Bengaluru",
                dropLat = 12.9810,
                dropLng = 77.6401,
                deliveryInstructions = listOf("Call on arrival"),
                riderNote = "Cash payment ready in envelope",
                paymentMode = RiderPaymentMode.COD,
                collectAmountInr = 640,
                deliveryOtp = "1184",
                basePayInr = 25,
                distancePayInr = 14,
                surgeBonusInr = 15,
                tipInr = 10,
                onTimeBonusInr = 10,
                distanceKm = 1.1,
                estimatedMinutes = 8,
                deliveryWindowLabel = "Express 10-min SLA",
                stage = RiderTripStage.DELIVERED,
                items = listOf(
                    TripLineItem("c1", "Farm Eggs Pack", "12 pcs", 2, 140, isPicked = true),
                    TripLineItem("c2", "Whole Wheat Bread", "400 g", 1, 60, isPicked = true),
                    TripLineItem("c3", "Roasted Almond Butter", "340 g", 1, 300, isPicked = true)
                ),
                podSealed = true,
                codCollected = true,
                podPhotoUri = null,
                createdAtMillis = nowMillis - 3_900_000L,
                acceptedAtMillis = nowMillis - 3_880_000L,
                deliveredAtMillis = nowMillis - 3_420_000L
            ),
            RiderTripOrder(
                orderId = "QK-9058",
                hubId = defaultHub.id,
                hubName = defaultHub.podName,
                pickupBayCode = "IND-B4-09",
                bagSealCode = "QK-SEAL-678",
                hubLat = 12.9784,
                hubLng = 77.6408,
                customerName = "Vikramaditya Sen",
                customerPhone = "+91 98440 39012",
                deliveryAddressLabel = "Work",
                deliveryAddressFull = "214, CMH Rd, Indiranagar, Bengaluru",
                dropLat = 12.9762,
                dropLng = 77.6428,
                deliveryInstructions = listOf("No cutlery needed"),
                riderNote = "Lobby security desk pickup",
                paymentMode = RiderPaymentMode.PREPAID_UPI,
                collectAmountInr = 0,
                deliveryOtp = "7390",
                basePayInr = 25,
                distancePayInr = 12,
                surgeBonusInr = 15,
                tipInr = 20,
                onTimeBonusInr = 10,
                distanceKm = 0.9,
                estimatedMinutes = 7,
                deliveryWindowLabel = "Express 10-min SLA",
                stage = RiderTripStage.DELIVERED,
                items = listOf(
                    TripLineItem("c4", "Tender Coconut Water", "330 ml", 4, 55, isPicked = true),
                    TripLineItem("c5", "Greek Yogurt Berry Cup", "120 g", 2, 80, isPicked = true)
                ),
                podSealed = true,
                codCollected = true,
                podPhotoUri = null,
                createdAtMillis = nowMillis - 2_400_000L,
                acceptedAtMillis = nowMillis - 2_385_000L,
                deliveredAtMillis = nowMillis - 1_960_000L
            ),
            RiderTripOrder(
                orderId = "QK-9072",
                hubId = defaultHub.id,
                hubName = defaultHub.podName,
                pickupBayCode = "IND-B3-12",
                bagSealCode = "QK-SEAL-701",
                hubLat = 12.9784,
                hubLng = 77.6408,
                customerName = "Neha Deshmukh",
                customerPhone = "+91 97391 88109",
                deliveryAddressLabel = "Home",
                deliveryAddressFull = "9, 6th Cross Rd, Defence Colony, Indiranagar, Bengaluru",
                dropLat = 12.9739,
                dropLng = 77.6451,
                deliveryInstructions = listOf("Avoid ringing bell"),
                riderNote = "Knock gently; baby sleeping",
                paymentMode = RiderPaymentMode.COD,
                collectAmountInr = 485,
                deliveryOtp = "5521",
                basePayInr = 25,
                distancePayInr = 16,
                surgeBonusInr = 10,
                tipInr = 0,
                onTimeBonusInr = 10,
                distanceKm = 1.2,
                estimatedMinutes = 9,
                deliveryWindowLabel = "Express 10-min SLA",
                stage = RiderTripStage.DELIVERED,
                items = listOf(
                    TripLineItem("c6", "Basmati Aged Rice", "1 kg", 1, 195, isPicked = true),
                    TripLineItem("c7", "Cold-Pressed Mustard Oil", "1 L", 1, 290, isPicked = true)
                ),
                podSealed = true,
                codCollected = true,
                podPhotoUri = null,
                createdAtMillis = nowMillis - 1_200_000L,
                acceptedAtMillis = nowMillis - 1_190_000L,
                deliveredAtMillis = nowMillis - 690_000L
            )
        )
    }

    fun initialTransactions(nowMillis: Long): List<PayoutTransaction> = listOf(
        PayoutTransaction(
            id = "TX-901",
            type = PayoutTxType.BANK_WITHDRAWAL,
            title = "Instant UPI Transfer • HDFC ****4819",
            amountInr = 120,
            createdAtMillis = nowMillis - 1_800_000L
        )
    )
}
