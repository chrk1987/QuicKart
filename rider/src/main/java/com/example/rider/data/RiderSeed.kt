package com.example.rider.data

import java.util.concurrent.TimeUnit
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/** Fictional stores, customers and trips around Hyderabad's IT corridor for the prototype. */
object RiderSeed {
    private val hour = TimeUnit.HOURS.toMillis(1)
    private val day = TimeUnit.DAYS.toMillis(1)

    /** Rider's idle position (Madhapur). */
    val home = GeoPoint(17.4483, 78.3915)

    private data class Place(val name: String, val address: String, val phone: String, val point: GeoPoint)

    private val stores = listOf(
        Place("Sri Lakshmi Kirana", "Shop 12, Ayyappa Society Main Rd, Madhapur", "9000011111", GeoPoint(17.4512, 78.3898)),
        Place("Green Basket Grocers", "Plot 44, Kondapur Main Rd, Kondapur", "9000022222", GeoPoint(17.4606, 78.3640)),
        Place("Daily Fresh Mart", "DLF Rd, Gachibowli", "9000033333", GeoPoint(17.4410, 78.3570)),
        Place("Annapurna Tiffins", "Road No. 36, Jubilee Hills", "9000044444", GeoPoint(17.4310, 78.4070))
    )
    private val customers = listOf(
        Place("Rina S.", "Flat 4B, Oakwood Apts, Madhapur", "98XXXXXX21", GeoPoint(17.4441, 78.3982)),
        Place("Karthik V.", "Villa 12, Silver Oaks, Kondapur", "97XXXXXX54", GeoPoint(17.4655, 78.3712)),
        Place("Ayesha M.", "Tower C 1203, Hill Ridge, Gachibowli", "99XXXXXX07", GeoPoint(17.4370, 78.3655)),
        Place("Pranav R.", "H.No 8-2-293, Road No. 10, Jubilee Hills", "96XXXXXX88", GeoPoint(17.4255, 78.4135)),
        Place("Divya K.", "Plot 7, Hitech City Rd, Madhapur", "95XXXXXX40", GeoPoint(17.4495, 78.3810))
    )
    private val baskets = listOf(
        listOf(TripItem("Whole Wheat Atta 5 kg", 1), TripItem("Iodised Salt 1 kg", 1), TripItem("Masala Noodles (4 pack)", 2), TripItem("Table Butter 100 g", 1)),
        listOf(TripItem("Toned Milk 500 ml", 2), TripItem("Farm Eggs (6)", 1), TripItem("Brown Bread", 1)),
        listOf(TripItem("Basmati Rice 1 kg", 1), TripItem("Toor Dal 1 kg", 1)),
        listOf(TripItem("Veg Meals", 2), TripItem("Masala Dosa", 1), TripItem("Filter Coffee", 2)),
        listOf(TripItem("Bananas (6)", 1), TripItem("Shimla Apples (4)", 1), TripItem("Thick Curd 400 g", 2))
    )

    fun km(a: GeoPoint, b: GeoPoint): Double {
        val r = 6371.0
        val dLat = Math.toRadians(b.lat - a.lat); val dLng = Math.toRadians(b.lng - a.lng)
        val h = sin(dLat / 2) * sin(dLat / 2) + cos(Math.toRadians(a.lat)) * cos(Math.toRadians(b.lat)) * sin(dLng / 2) * sin(dLng / 2)
        return 2 * r * atan2(sqrt(h), sqrt(1 - h)) * 1.3 // road factor
    }

    fun offer(seq: Int, from: GeoPoint, now: Long): Trip {
        val store = stores[seq % stores.size]
        val cust = customers[(seq * 3 + 1) % customers.size]
        val pickup = km(from, store.point)
        val drop = km(store.point, cust.point)
        val items = baskets[seq % baskets.size]
        val cod = seq % 3 == 1
        return Trip(
            orderId = "QK${7840 + seq}",
            storeName = store.name, storeAddress = store.address, storePhone = store.phone, store = store.point,
            customerName = cust.name, customerAddress = cust.address, customerPhone = cust.phone, customer = cust.point,
            items = items,
            paymentMode = if (cod) PaymentMode.COD else PaymentMode.PREPAID,
            orderAmount = 180 + seq * 37 % 420,
            pickupKm = pickup, dropKm = drop,
            basePay = 25, distancePay = (drop * 8).toInt().coerceAtLeast(10),
            tip = if (seq % 4 == 2) 20 else 0,
            incentive = if (seq % 5 == 0) 15 else 0,
            customerOtp = (1000 + (seq * 7919) % 9000).toString(),
            offeredAtMillis = now,
            note = if (seq % 2 == 0) "Ring the bell, don't call" else null
        )
    }

    /** Completed / cancelled trips for history and earnings. */
    fun history(now: Long): List<Trip> {
        fun done(seq: Int, ago: Long, stage: TripStage, reason: String? = null): Trip {
            val t = offer(seq, home, now - ago)
            return t.copy(stage = stage, completedAtMillis = now - ago + 25 * 60_000, cancelReason = reason, readiness = StoreReadiness.READY, legProgress = 1f)
        }
        return listOf(
            done(31, 1 * hour, TripStage.DELIVERED),
            done(32, 2 * hour, TripStage.DELIVERED),
            done(33, 3 * hour, TripStage.CANCELLED, "Long wait at store"),
            done(34, 4 * hour, TripStage.DELIVERED),
            done(35, 5 * hour, TripStage.DELIVERED),
            done(36, day + 2 * hour, TripStage.DELIVERED),
            done(37, day + 4 * hour, TripStage.DELIVERED),
            done(38, day + 6 * hour, TripStage.RETURNED, "Customer unreachable"),
            done(39, 2 * day + hour, TripStage.DELIVERED),
            done(40, 2 * day + 3 * hour, TripStage.DELIVERED),
            done(41, 3 * day + 2 * hour, TripStage.DELIVERED),
            done(42, 4 * day + 5 * hour, TripStage.DELIVERED)
        )
    }

    fun cashLedger(now: Long): List<CashEntry> = listOf(
        CashEntry("c1", CashEntryType.COLLECTED, 412, now - 1 * hour, "Order #7871"),
        CashEntry("c2", CashEntryType.COLLECTED, 286, now - 3 * hour, "Order #7874"),
        CashEntry("c3", CashEntryType.COLLECTED, 352, now - 5 * hour, "Order #7875"),
        CashEntry("c4", CashEntryType.DEPOSITED, 1240, now - day, "UPI ref 4821-77")
    )

    /** Weekly incentive target. */
    const val WEEKLY_TARGET_TRIPS = 40
    const val WEEKLY_TARGET_BONUS = 400
    const val WEEK_TRIPS_BEFORE_TODAY = 27
    const val WEEK_EARNINGS_BEFORE_TODAY = 3480
}
