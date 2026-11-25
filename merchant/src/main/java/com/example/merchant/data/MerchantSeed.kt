package com.example.merchant.data

import java.util.concurrent.TimeUnit

/**
 * Sample data for the prototype. Store, customer and rider names are fictional and products use
 * generic names (no third-party brands).
 */
object MerchantSeed {

    private val minute = TimeUnit.MINUTES.toMillis(1)
    private val day = TimeUnit.DAYS.toMillis(1)

    fun catalogue(): List<CatalogueItem> = listOf(
        CatalogueItem("p_atta", "Whole Wheat Atta", "Stone-ground chakki atta for soft rotis.", "Grains & Atta", 349, 10, 18, unit = "5 kg"),
        CatalogueItem("p_rice", "Basmati Rice", "Long-grain aged basmati, aromatic and fluffy.", "Grains & Atta", 189, 5, 4, unit = "1 kg"),
        CatalogueItem("p_toor", "Toor Dal", "Unpolished toor dal, cooks soft.", "Grains & Atta", 165, 0, 22, unit = "1 kg"),
        CatalogueItem("p_milk", "Toned Milk", "Fresh pasteurised toned milk.", "Dairy & Eggs", 30, 0, 40, unit = "500 ml"),
        CatalogueItem("p_curd", "Thick Curd", "Set curd, made fresh every morning.", "Dairy & Eggs", 45, 0, 3, unit = "400 g"),
        CatalogueItem("p_eggs", "Farm Eggs", "Brown farm eggs, tray of 6.", "Dairy & Eggs", 66, 0, 12, unit = "6 pcs"),
        CatalogueItem("p_paneer", "Fresh Paneer", "Soft malai paneer block.", "Dairy & Eggs", 95, 5, 0, unit = "200 g"),
        CatalogueItem("p_banana", "Robusta Bananas", "Ripe, naturally sweet.", "Fruits", 52, 0, 25, unit = "6 pcs"),
        CatalogueItem("p_apple", "Shimla Apples", "Crisp hill apples.", "Fruits", 180, 15, 9, unit = "4 pcs"),
        CatalogueItem("p_onion", "Onions", "Everyday red onions.", "Vegetables", 38, 0, 50, unit = "1 kg"),
        CatalogueItem("p_tomato", "Tomatoes", "Firm, juicy tomatoes.", "Vegetables", 40, 0, 2, unit = "1 kg"),
        CatalogueItem("p_chips", "Salted Potato Chips", "Crunchy classic salted chips.", "Snacks", 20, 0, 60, unit = "52 g"),
        CatalogueItem("p_noodles", "Masala Instant Noodles", "2-minute masala noodles, pack of 4.", "Ready to Eat", 56, 0, 30, unit = "280 g"),
        CatalogueItem("p_salt", "Iodised Salt", "Free-flowing iodised salt.", "Grains & Atta", 28, 0, 35, unit = "1 kg"),
        CatalogueItem("p_butter", "Table Butter", "Salted table butter.", "Dairy & Eggs", 58, 0, 8, unit = "100 g"),
        CatalogueItem("p_soap", "Neem Bath Soap", "Gentle neem soap, pack of 3.", "Personal Care", 99, 10, 14, unit = "3 x 100 g"),
        CatalogueItem("p_dish", "Lemon Dishwash Gel", "Grease-cutting dishwash gel.", "Household", 109, 15, 0, unit = "500 ml"),
        CatalogueItem("p_juice", "Mixed Fruit Juice", "No added sugar fruit juice.", "Beverages", 110, 10, 16, unit = "1 L")
    )

    private fun line(id: String, name: String, qty: Int, price: Int) = OrderLine(id, name, qty, price)

    fun orders(now: Long): List<StoreOrder> = listOf(
        StoreOrder(
            id = "QK5789", customerName = "Priya S.", customerArea = "Madhapur",
            lines = listOf(line("p_atta", "Whole Wheat Atta (5 kg)", 1, 314), line("p_milk", "Toned Milk (500 ml)", 2, 30), line("p_eggs", "Farm Eggs (6 pcs)", 1, 66)),
            placedAtMillis = now - 14 * minute, acceptedAtMillis = now - 13 * minute, status = OrderStatus.PREPARING,
            paymentMode = PaymentMode.UPI, deliveryFee = 0, prepMinutes = 15
        ),
        StoreOrder(
            id = "QK5786", customerName = "Arjun R.", customerArea = "Kondapur",
            lines = listOf(line("p_rice", "Basmati Rice (1 kg)", 2, 179), line("p_toor", "Toor Dal (1 kg)", 1, 165)),
            placedAtMillis = now - 26 * minute, acceptedAtMillis = now - 25 * minute, status = OrderStatus.READY,
            paymentMode = PaymentMode.COD, deliveryFee = 25, prepMinutes = 10,
            rider = AssignedRider("Ravi K.", "Bike TS09 EK 4821", "9000012345", 4.8f, RiderStage.ON_THE_WAY, 3)
        ),
        StoreOrder(
            id = "QK5771", customerName = "Meena T.", customerArea = "Gachibowli",
            lines = listOf(line("p_banana", "Robusta Bananas (6 pcs)", 1, 52), line("p_curd", "Thick Curd (400 g)", 2, 45)),
            placedAtMillis = now - 95 * minute, status = OrderStatus.DELIVERED, paymentMode = PaymentMode.UPI, customerRating = 5
        ),
        StoreOrder(
            id = "QK5764", customerName = "Rahul M.", customerArea = "Madhapur",
            lines = listOf(line("p_noodles", "Masala Instant Noodles (4 pack)", 2, 56), line("p_chips", "Salted Potato Chips", 3, 20)),
            placedAtMillis = now - 150 * minute, status = OrderStatus.DELIVERED, paymentMode = PaymentMode.CARD, customerRating = 4
        ),
        StoreOrder(
            id = "QK5752", customerName = "Fatima K.", customerArea = "Hitech City",
            lines = listOf(line("p_paneer", "Fresh Paneer (200 g)", 1, 90), line("p_butter", "Table Butter (100 g)", 1, 58)),
            placedAtMillis = now - 210 * minute, status = OrderStatus.CANCELLED, paymentMode = PaymentMode.UPI,
            rejectReason = "Customer cancelled before preparation"
        ),
        StoreOrder(
            id = "QK5740", customerName = "Sanjay P.", customerArea = "Kondapur",
            lines = listOf(line("p_onion", "Onions (1 kg)", 2, 38), line("p_tomato", "Tomatoes (1 kg)", 1, 40), line("p_salt", "Iodised Salt (1 kg)", 1, 28)),
            placedAtMillis = now - 300 * minute, status = OrderStatus.DELIVERED, paymentMode = PaymentMode.COD, deliveryFee = 25, customerRating = 5
        )
    )

    /** Pool used to simulate a new incoming order. */
    private val customers = listOf(
        "Anil V." to "Madhapur", "Kavya N." to "Gachibowli", "Imran S." to "Kondapur",
        "Deepa R." to "Hitech City", "Vikram J." to "Jubilee Hills", "Sneha L." to "Kukatpally"
    )

    fun simulatedOrder(seq: Int, now: Long, catalogue: List<CatalogueItem>): StoreOrder {
        val pool = catalogue.filter { it.active }.ifEmpty { catalogue() }
        val picks = pool.shuffled().take(2 + seq % 3)
        val (name, area) = customers[seq % customers.size]
        val lines = picks.mapIndexed { i, item ->
            OrderLine(item.id, "${item.name} (${item.unit})", 1 + (seq + i) % 2, item.sellingPrice)
        }
        val itemTotal = lines.sumOf { it.lineTotal }
        return StoreOrder(
            id = "QK${5790 + seq}",
            customerName = name,
            customerArea = area,
            lines = lines,
            placedAtMillis = now,
            status = OrderStatus.NEW,
            paymentMode = PaymentMode.entries[seq % PaymentMode.entries.size],
            deliveryFee = if (itemTotal >= 199) 0 else 25,
            note = if (seq % 3 == 0) "Please pack the eggs separately" else null
        )
    }

    fun offers(now: Long): List<StoreOffer> = listOf(
        StoreOffer("o1", OfferType.PERCENT_OFF, 20, 0, now - 5 * day, now + 20 * day, true, "Vegetables"),
        StoreOffer("o2", OfferType.FREE_DELIVERY, 0, 499, now - 2 * day, now + 30 * day, true),
        StoreOffer("o3", OfferType.FLAT_OFF, 50, 299, now - 40 * day, now - 10 * day, false),
        StoreOffer("o4", OfferType.PERCENT_OFF, 10, 199, now - 30 * day, now - 3 * day, false, "Snacks")
    )

    fun payouts(now: Long): List<Payout> = listOf(
        Payout("po1", "18 – 24 Nov", now + 1 * day, 17420, 2090, 15330, PayoutStatus.PENDING),
        Payout("po2", "11 – 17 Nov", now - 6 * day, 46900, 5628, 41272, PayoutStatus.PAID, "UTR4021987765"),
        Payout("po3", "04 – 10 Nov", now - 13 * day, 44180, 5302, 38878, PayoutStatus.PAID, "UTR4021433102"),
        Payout("po4", "28 Oct – 03 Nov", now - 20 * day, 39750, 4770, 34980, PayoutStatus.PAID, "UTR4020871240")
    )

    fun alerts(now: Long): List<MerchantAlert> = listOf(
        MerchantAlert("a1", AlertType.RIDER_ARRIVED, "Rider on the way – Order #5786", "Ravi K. will reach your store in about 3 min.", now - 2 * minute, orderId = "QK5786"),
        MerchantAlert("a2", AlertType.LOW_STOCK, "Low stock: Tomatoes (1 kg)", "Only 2 left. Restock or mark out of stock.", now - 15 * minute),
        MerchantAlert("a3", AlertType.PAYOUT, "Payout credited – ₹41,272", "Settlement for 11 – 17 Nov sent to your bank.", now - 6 * day, read = true),
        MerchantAlert("a4", AlertType.ORDER_CANCELLED, "Order #5752 cancelled", "The customer cancelled before preparation. No action needed.", now - 210 * minute, read = true, orderId = "QK5752")
    )

    fun tickets(now: Long): List<SupportTicket> = listOf(
        SupportTicket("T1042", "Order issue", "Customer says item missing", "Customer reports butter was not in the bag.", "QK5740", now - 1 * day, TicketStatus.IN_PROGRESS),
        SupportTicket("T1031", "Payout", "Payout for 28 Oct week", "Amount was lower than expected.", null, now - 9 * day, TicketStatus.RESOLVED)
    )

    /** Revenue series for reports: (label, revenue, orders). */
    fun revenueSeries(range: ReportRange): List<Triple<String, Int, Int>> = when (range) {
        ReportRange.DAY -> listOf(
            Triple("8a", 820, 3), Triple("10a", 1640, 6), Triple("12p", 2210, 8), Triple("2p", 1380, 5),
            Triple("4p", 1720, 6), Triple("6p", 2960, 11), Triple("8p", 3420, 12), Triple("10p", 1150, 4)
        )
        ReportRange.WEEK -> listOf(
            Triple("Mon", 6420, 31), Triple("Tue", 5980, 28), Triple("Wed", 7210, 34), Triple("Thu", 6850, 33),
            Triple("Fri", 8340, 40), Triple("Sat", 9870, 47), Triple("Sun", 10450, 51)
        )
        ReportRange.MONTH -> listOf(
            Triple("W1", 39750, 188), Triple("W2", 44180, 205), Triple("W3", 46900, 219), Triple("W4", 17420, 84)
        )
    }
}

enum class ReportRange(val label: String) { DAY("Day"), WEEK("Week"), MONTH("Month") }
