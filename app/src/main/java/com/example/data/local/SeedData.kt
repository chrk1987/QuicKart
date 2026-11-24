package com.example.data.local

import androidx.compose.ui.graphics.Color
import com.example.data.model.DeliverySlot
import com.example.data.model.Neighborhood
import com.example.data.model.OrderLineItem
import com.example.data.model.OrderRecord
import com.example.data.model.OrderStage
import com.example.data.model.PaymentMethod
import com.example.data.model.Product
import com.example.data.model.ProductArtworkType
import com.example.data.model.ProductCategory
import com.example.data.model.ProductVariant
import com.example.data.model.PromoCode
import com.example.data.model.SavedAddress
import com.example.data.model.SupportFaqTopic
import com.example.data.model.SupportIssueCategory
import com.example.data.model.SupportMessage
import com.example.data.model.SupportResolutionOption
import com.example.data.model.SupportTicketRecord
import com.example.data.model.SupportTicketStatus
import java.util.concurrent.TimeUnit

object SeedData {

    val neighborhoods = listOf(
        Neighborhood(
            id = "indiranagar",
            name = "Indiranagar",
            city = "Bengaluru",
            etaMinutes = 8,
            hubName = "100ft Rd Dark Store #04"
        ),
        Neighborhood(
            id = "koramangala",
            name = "Koramangala",
            city = "Bengaluru",
            etaMinutes = 9,
            hubName = "4th Block Micro-Hub #11"
        ),
        Neighborhood(
            id = "bandra_west",
            name = "Bandra West",
            city = "Mumbai",
            etaMinutes = 10,
            hubName = "Pali Hill Express Hub #07"
        ),
        Neighborhood(
            id = "hauz_khas",
            name = "Hauz Khas",
            city = "New Delhi",
            etaMinutes = 8,
            hubName = "Aurobindo Pod #02"
        ),
        Neighborhood(
            id = "jubilee_hills",
            name = "Jubilee Hills",
            city = "Hyderabad",
            etaMinutes = 9,
            hubName = "Road No. 36 Cold Pod #09"
        )
    )

    val availablePromos = listOf(
        PromoCode("QUICK50", "Flat ₹50 off above ₹299", discountInr = 50, minOrderInr = 299),
        PromoCode("FRESH20", "Flat ₹20 off above ₹149", discountInr = 20, minOrderInr = 149),
        PromoCode("FAST8", "₹30 instant speed saver above ₹249", discountInr = 30, minOrderInr = 249)
    )

    val deliveryInstructionChips = listOf(
        "Leave at the door",
        "Don't ring bell",
        "Call upon arrival",
        "Avoid plastic bag"
    )

    val deliverySlots = listOf(
        DeliverySlot(
            id = "slot_express",
            dayLabel = "Now",
            title = "8-Min Express",
            windowText = "Arrives in ~8 mins",
            badge = "FASTEST",
            isExpress = true,
            etaMinutes = 8
        ),
        DeliverySlot(
            id = "slot_today_afternoon",
            dayLabel = "Today",
            title = "Afternoon Breeze",
            windowText = "1:00 PM – 3:00 PM",
            badge = "ZERO RUSH",
            isExpress = false,
            etaMinutes = 120
        ),
        DeliverySlot(
            id = "slot_today_evening",
            dayLabel = "Today",
            title = "Evening Prime",
            windowText = "6:00 PM – 8:00 PM",
            badge = "POPULAR",
            isExpress = false,
            etaMinutes = 180
        ),
        DeliverySlot(
            id = "slot_today_night",
            dayLabel = "Today",
            title = "Dinner Prep",
            windowText = "8:30 PM – 10:30 PM",
            badge = null,
            isExpress = false,
            etaMinutes = 240
        ),
        DeliverySlot(
            id = "slot_tomorrow_morning",
            dayLabel = "Tomorrow",
            title = "Morning Farm Drop",
            windowText = "7:00 AM – 9:00 AM",
            badge = "FRESH BATCH",
            isExpress = false,
            etaMinutes = 600
        ),
        DeliverySlot(
            id = "slot_tomorrow_midday",
            dayLabel = "Tomorrow",
            title = "Midday Restock",
            windowText = "11:00 AM – 1:00 PM",
            badge = null,
            isExpress = false,
            etaMinutes = 720
        ),
        DeliverySlot(
            id = "slot_tomorrow_evening",
            dayLabel = "Tomorrow",
            title = "Evening Slot",
            windowText = "5:00 PM – 7:00 PM",
            badge = null,
            isExpress = false,
            etaMinutes = 840
        )
    )

    val praiseTags = listOf(
        "Super Fast",
        "Chilled & Fresh",
        "Polite Rider",
        "Followed Note",
        "Zero Spill"
    )

    val issueTags = listOf(
        "Late Delivery",
        "Damaged Pack",
        "Missing Item",
        "Wrong Temp",
        "Rider Issue"
    )

    val products: List<Product> = listOf(
        Product(
            id = "prod_a2_milk",
            name = "A2 Desi Cow Milk",
            brand = "Akshayakalpa Organic",
            category = ProductCategory.DAIRY_EGGS,
            description = "Unprocessed, antibiotic-free farm-fresh A2 Gir cow milk delivered chilled at 4°C in glass-grade pouches.",
            badgeText = "BESTSELLER",
            rating = 4.9f,
            ratingCount = 2480,
            deliveryMinutes = 8,
            artworkType = ProductArtworkType.MILK_BOTTLE,
            accentColor = Color(0xFF0EA5E9),
            variants = listOf(
                ProductVariant("v_milk_500", "500 ml", 52, 60),
                ProductVariant("v_milk_1000", "1 L Pack", 98, 115)
            ),
            highlights = listOf("Zero preservatives", "Chilled 4°C cold chain", "Morning farm batch")
        ),
        Product(
            id = "prod_farm_eggs",
            name = "Free-Range Brown Eggs",
            brand = "Happy Hens Farm",
            category = ProductCategory.DAIRY_EGGS,
            description = "Nutrient-rich cage-free brown eggs with bright golden yolks, UV-sanitized and cushioned in pulp trays.",
            badgeText = "HIGH PROTEIN",
            rating = 4.8f,
            ratingCount = 1920,
            deliveryMinutes = 8,
            artworkType = ProductArtworkType.EGGS_CARTON,
            accentColor = Color(0xFFF59E0B),
            variants = listOf(
                ProductVariant("v_eggs_6", "6 pcs", 88, 105),
                ProductVariant("v_eggs_12", "12 pcs", 168, 199)
            ),
            highlights = listOf("100% cage-free", "Omega-3 enriched", "Laid within 24 hrs")
        ),
        Product(
            id = "prod_malai_paneer",
            name = "Artisan Malai Paneer",
            brand = "Milky Mist Reserve",
            category = ProductCategory.DAIRY_EGGS,
            description = "Ultra-soft, creamy fresh paneer blocks crafted with pure buffalo milk for melt-in-mouth curries and grills.",
            badgeText = "15% OFF",
            rating = 4.7f,
            ratingCount = 1310,
            deliveryMinutes = 8,
            artworkType = ProductArtworkType.PANEER_BLOCK,
            accentColor = Color(0xFF38BDF8),
            variants = listOf(
                ProductVariant("v_paneer_200", "200 g", 89, 105),
                ProductVariant("v_paneer_400", "400 g", 169, 199)
            ),
            highlights = listOf("22g protein per 100g", "Soft non-rubbery texture", "Vacuum sealed")
        ),
        Product(
            id = "prod_greek_yogurt",
            name = "Strained Honey Greek Yogurt",
            brand = "Epigamia",
            category = ProductCategory.DAIRY_EGGS,
            description = "Thick, velvety Greek yogurt naturally sweetened with wildflower honey and packed with active probiotics.",
            badgeText = null,
            rating = 4.8f,
            ratingCount = 870,
            deliveryMinutes = 8,
            artworkType = ProductArtworkType.GREEK_YOGURT,
            accentColor = Color(0xFF6366F1),
            variants = listOf(
                ProductVariant("v_yogurt_90", "90 g Cup", 54, 65),
                ProductVariant("v_yogurt_400", "400 g Tub", 195, 225)
            ),
            highlights = listOf("Double strained", "Live probiotic cultures", "No artificial flavors")
        ),
        Product(
            id = "prod_hass_avocado",
            name = "Ripe Hass Avocados",
            brand = "Trikaya Fresh",
            category = ProductCategory.FRESH_PRODUCE,
            description = "Creamy, buttery Hass avocados hand-sorted for immediate slicing onto sourdough toast or guacamole.",
            badgeText = "READY TO EAT",
            rating = 4.7f,
            ratingCount = 1140,
            deliveryMinutes = 8,
            artworkType = ProductArtworkType.AVOCADO,
            accentColor = Color(0xFF22C55E),
            variants = listOf(
                ProductVariant("v_avo_1", "1 pc (180g)", 95, 125),
                ProductVariant("v_avo_2", "2 pcs Pack", 179, 239)
            ),
            highlights = listOf("Hand-tested ripeness", "Rich healthy fats", "Zero wax coating")
        ),
        Product(
            id = "prod_organic_bananas",
            name = "Robusta Baby Bananas",
            brand = "Sahyadri Farms",
            category = ProductCategory.FRESH_PRODUCE,
            description = "Naturally ripened sweet Robusta bananas harvested from residue-free orchards for morning smoothies.",
            badgeText = "FARM PICK",
            rating = 4.6f,
            ratingCount = 1640,
            deliveryMinutes = 8,
            artworkType = ProductArtworkType.BANANAS,
            accentColor = Color(0xFFEAB308),
            variants = listOf(
                ProductVariant("v_ban_6", "500 g (5-6 pcs)", 42, 52),
                ProductVariant("v_ban_12", "1 kg Bunch", 78, 96)
            ),
            highlights = listOf("Carbide-free ripening", "Potassium rich", "Fresh morning arrival")
        ),
        Product(
            id = "prod_baby_spinach",
            name = "Hydroponic Baby Spinach",
            brand = "UrbanKisaan",
            category = ProductCategory.FRESH_PRODUCE,
            description = "Triple-washed crisp hydroponic baby spinach leaves ready straight out of the box for salads and omelettes.",
            badgeText = "PESTICIDE FREE",
            rating = 4.9f,
            ratingCount = 740,
            deliveryMinutes = 8,
            artworkType = ProductArtworkType.BABY_SPINACH,
            accentColor = Color(0xFF16A34A),
            variants = listOf(
                ProductVariant("v_spin_150", "150 g Box", 69, 85),
                ProductVariant("v_spin_300", "300 g Family", 129, 159)
            ),
            highlights = listOf("Ready to eat", "90% less water grown", "Crisp cold-mist pack")
        ),
        Product(
            id = "prod_cherry_tomatoes",
            name = "Vine-Ripened Cherry Tomatoes",
            brand = "Nama Fresh",
            category = ProductCategory.FRESH_PRODUCE,
            description = "Sweet, juicy ruby cherry tomatoes picked on the vine with vibrant acidity for pastas and snacking.",
            badgeText = "20% OFF",
            rating = 4.7f,
            ratingCount = 590,
            deliveryMinutes = 8,
            artworkType = ProductArtworkType.TOMATOES,
            accentColor = Color(0xFFEF4444),
            variants = listOf(
                ProductVariant("v_tom_200", "200 g Punnet", 64, 80),
                ProductVariant("v_tom_400", "400 g Twin", 119, 150)
            ),
            highlights = listOf("Vine harvested", "Bursting sweetness", "Recyclable punnet")
        ),
        Product(
            id = "prod_sourdough",
            name = "Sourdough Country Loaf",
            brand = "The Baker's Dozen",
            category = ProductCategory.BAKERY_BREAD,
            description = "36-hour slow-fermented artisan sourdough loaf with crackling caramelized crust and open airy crumb.",
            badgeText = "FRESH BAKED",
            rating = 4.9f,
            ratingCount = 2110,
            deliveryMinutes = 8,
            artworkType = ProductArtworkType.SOURDOUGH,
            accentColor = Color(0xFFD97706),
            variants = listOf(
                ProductVariant("v_sd_400", "400 g Sliced", 135, 155),
                ProductVariant("v_sd_600", "600 g Whole", 189, 219)
            ),
            highlights = listOf("36h wild yeast ferment", "Zero maida / improvers", "Baked 5 AM today")
        ),
        Product(
            id = "prod_croissant",
            name = "French Butter Croissants",
            brand = "Paul & Co Bakehouse",
            category = ProductCategory.BAKERY_BREAD,
            description = "Flaky 27-layer laminated pastries crafted with cultured Normandy-style butter for crisp golden layers.",
            badgeText = "2 FOR ₹149",
            rating = 4.8f,
            ratingCount = 980,
            deliveryMinutes = 8,
            artworkType = ProductArtworkType.CROISSANT,
            accentColor = Color(0xFFF59E0B),
            variants = listOf(
                ProductVariant("v_crois_2", "Pack of 2", 149, 180),
                ProductVariant("v_crois_4", "Box of 4", 279, 340)
            ),
            highlights = listOf("100% pure butter", "27 flaky layers", "Zero trans fat")
        ),
        Product(
            id = "prod_brioche",
            name = "Toasted Sesame Brioche Buns",
            brand = "Subko Craft Bake",
            category = ProductCategory.BAKERY_BREAD,
            description = "Pillow-soft golden brioche burger buns glazed lightly with milk wash and toasted white sesame.",
            badgeText = null,
            rating = 4.6f,
            ratingCount = 430,
            deliveryMinutes = 8,
            artworkType = ProductArtworkType.BRIOCHE,
            accentColor = Color(0xFFEA580C),
            variants = listOf(
                ProductVariant("v_brio_4", "4 Buns (260g)", 110, 130)
            ),
            highlights = listOf("Rich buttery crumb", "Preservative free", "Cafe-style soft")
        ),
        Product(
            id = "prod_cold_brew",
            name = "Cold Brew Hazelnut Latte",
            brand = "Blue Tokai Coffee",
            category = ProductCategory.COLD_BREW_JUICES,
            description = "18-hour steeped 100% Chikmagalur Arabica cold brew blended with silky milk and roasted hazelnut notes.",
            badgeText = "CHILLED",
            rating = 4.9f,
            ratingCount = 1780,
            deliveryMinutes = 8,
            artworkType = ProductArtworkType.COLD_BREW,
            accentColor = Color(0xFF92400E),
            variants = listOf(
                ProductVariant("v_cb_250", "250 ml Can", 115, 135),
                ProductVariant("v_cb_pack3", "3 x 250 ml", 319, 399)
            ),
            highlights = listOf("18-hour slow steep", "Low acidity Arabica", "Served ice-cold")
        ),
        Product(
            id = "prod_orange_juice",
            name = "Cold-Pressed Valencia Orange",
            brand = "Raw Pressery",
            category = ProductCategory.COLD_BREW_JUICES,
            description = "Pure cold-pressed Valencia oranges with zero added sugar, zero water, and vibrant Vitamin C freshness.",
            badgeText = "NO SUGAR",
            rating = 4.7f,
            ratingCount = 1120,
            deliveryMinutes = 8,
            artworkType = ProductArtworkType.ORANGE_JUICE,
            accentColor = Color(0xFFF97316),
            variants = listOf(
                ProductVariant("v_oj_250", "250 ml Bottle", 79, 95),
                ProductVariant("v_oj_1000", "1 L Carafe", 249, 299)
            ),
            highlights = listOf("6 oranges per bottle", "HPP cold pressed", "No concentrates")
        ),
        Product(
            id = "prod_sugarcane",
            name = "Pressed Sugarcane & Ginger",
            brand = "Paper Boat Swing",
            category = ProductCategory.COLD_BREW_JUICES,
            description = "Crisp freshly pressed sugarcane juice lifted with zesty ginger and a dash of Gondhoraj lime.",
            badgeText = "25% OFF",
            rating = 4.6f,
            ratingCount = 650,
            deliveryMinutes = 8,
            artworkType = ProductArtworkType.SUGARCANE_JUICE,
            accentColor = Color(0xFF84CC16),
            variants = listOf(
                ProductVariant("v_sc_250", "250 ml", 45, 60),
                ProductVariant("v_sc_500", "500 ml", 82, 110)
            ),
            highlights = listOf("Instant hydration", "Real ginger Zing", "Chilled dispatch")
        ),
        Product(
            id = "prod_kettle_chips",
            name = "Himalayan Salt Kettle Chips",
            brand = "TagZ Craft",
            category = ProductCategory.SNACKS_PANTRY,
            description = "Thick-cut popped potato chips cooked in small batches with pure pink Himalayan salt and 50% less fat.",
            badgeText = "CRUNCHY",
            rating = 4.7f,
            ratingCount = 1490,
            deliveryMinutes = 8,
            artworkType = ProductArtworkType.KETTLE_CHIPS,
            accentColor = Color(0xFFEC4899),
            variants = listOf(
                ProductVariant("v_chip_50", "50 g Pouch", 45, 55),
                ProductVariant("v_chip_110", "110 g Party", 89, 110)
            ),
            highlights = listOf("No palm oil", "Popped not deep fried", "Pink rock salt")
        ),
        Product(
            id = "prod_makhana",
            name = "Peri-Peri Roasted Makhana",
            brand = "Farmley Select",
            category = ProductCategory.SNACKS_PANTRY,
            description = "Light, crunchy lotus seeds slow-roasted in olive oil and dusted with smoky peri-peri spice.",
            badgeText = "GUILT-FREE",
            rating = 4.8f,
            ratingCount = 910,
            deliveryMinutes = 8,
            artworkType = ProductArtworkType.MAKHANA,
            accentColor = Color(0xFFE11D48),
            variants = listOf(
                ProductVariant("v_makh_70", "70 g Jar", 119, 145)
            ),
            highlights = listOf("Roasted in olive oil", "Gluten free", "98 kcal per serve")
        ),
        Product(
            id = "prod_dark_chocolate",
            name = "70% Cacao Sea Salt Bar",
            brand = "Mason & Co",
            category = ProductCategory.SNACKS_PANTRY,
            description = "Bean-to-bar single-origin South Indian dark chocolate finished with flaky organic sea salt crystals.",
            badgeText = "ARTISAN",
            rating = 4.9f,
            ratingCount = 780,
            deliveryMinutes = 8,
            artworkType = ProductArtworkType.DARK_CHOCOLATE,
            accentColor = Color(0xFF78350F),
            variants = listOf(
                ProductVariant("v_choc_75", "75 g Bar", 215, 250)
            ),
            highlights = listOf("Single-origin cacao", "Vegan & soy-free", "Unrefined cane sugar")
        ),
        Product(
            id = "prod_peanut_butter",
            name = "Crunchy Unsweetened Peanut Butter",
            brand = "Pintola High Protein",
            category = ProductCategory.SNACKS_PANTRY,
            description = "100% roasted bold peanuts stone-ground into thick crunchy butter with zero added sugar or hydrogenated oils.",
            badgeText = "30G PROTEIN",
            rating = 4.8f,
            ratingCount = 2340,
            deliveryMinutes = 8,
            artworkType = ProductArtworkType.PEANUT_BUTTER,
            accentColor = Color(0xFFB45309),
            variants = listOf(
                ProductVariant("v_pb_350", "350 g Jar", 149, 175),
                ProductVariant("v_pb_1000", "1 kg Tub", 369, 449)
            ),
            highlights = listOf("100% roasted peanuts", "Zero added sugar", "Keto friendly")
        )
    )

    val savedAddresses = listOf(
        SavedAddress(
            id = "addr_home",
            label = "Home",
            fullAddress = "Flat 402, Palm Grove Residency, 12th Main Rd, Indiranagar",
            landmark = "Near CMH Park Metro Gate 2",
            neighborhoodId = "indiranagar",
            isDefault = true
        ),
        SavedAddress(
            id = "addr_work",
            label = "Work",
            fullAddress = "WeWork Prestige Cube, Laskar Hosur Rd, Koramangala 4th Block",
            landmark = "Opposite Sony Signal",
            neighborhoodId = "koramangala",
            isDefault = false
        ),
        SavedAddress(
            id = "addr_other",
            label = "Other",
            fullAddress = "B-14 Sea Breeze Apartments, 16th Road, Bandra West",
            landmark = "Behind Pali Market",
            neighborhoodId = "bandra_west",
            isDefault = false
        )
    )

    fun createSeededOrders(nowMillis: Long = System.currentTimeMillis()): List<OrderRecord> {
        fun daysAgo(days: Long): Long = nowMillis - TimeUnit.DAYS.toMillis(days)
        fun minsAgo(mins: Long): Long = nowMillis - TimeUnit.MINUTES.toMillis(mins)

        return listOf(
            // 1 Active Live Order
            OrderRecord(
                orderId = "QK-84920",
                createdAtMillis = minsAgo(4),
                neighborhoodName = "Indiranagar, Bengaluru",
                etaMinutes = 8,
                remainingSeconds = 252, // 4m 12s
                stage = OrderStage.OUT_FOR_DELIVERY,
                items = listOf(
                    OrderLineItem("prod_a2_milk", "A2 Desi Cow Milk", "v_milk_1000", "1 L Pack", ProductCategory.DAIRY_EGGS, 1, 98, 115),
                    OrderLineItem("prod_sourdough", "Sourdough Country Loaf", "v_sd_400", "400 g Sliced", ProductCategory.BAKERY_BREAD, 1, 135, 155),
                    OrderLineItem("prod_hass_avocado", "Ripe Hass Avocados", "v_avo_2", "2 pcs Pack", ProductCategory.FRESH_PRODUCE, 1, 179, 239)
                ),
                deliveryAddressLabel = "Home",
                deliveryAddressFull = "Flat 402, Palm Grove Residency, 12th Main Rd, Indiranagar",
                deliveryInstructions = listOf("Don't ring bell", "Leave at the door"),
                riderNote = "Please place the chilled milk bag inside the shoe cabinet shade.",
                paymentMethod = PaymentMethod.UPI,
                riderName = "Arjun Verma",
                riderPhone = "+91 98450 77218",
                riderRating = 4.9f,
                itemTotal = 412,
                mrpSavings = 97,
                deliveryFee = 0,
                handlingCharge = 4,
                promoDiscount = 50,
                tipAmount = 20,
                totalPaid = 386
            ),
            // Historical Order 1 (3 days ago)
            OrderRecord(
                orderId = "QK-84311",
                createdAtMillis = daysAgo(3),
                neighborhoodName = "Indiranagar, Bengaluru",
                etaMinutes = 8,
                remainingSeconds = 0,
                stage = OrderStage.DELIVERED,
                items = listOf(
                    OrderLineItem("prod_cold_brew", "Cold Brew Hazelnut Latte", "v_cb_250", "250 ml Can", ProductCategory.COLD_BREW_JUICES, 2, 115, 135),
                    OrderLineItem("prod_croissant", "French Butter Croissants", "v_crois_2", "Pack of 2", ProductCategory.BAKERY_BREAD, 1, 149, 180),
                    OrderLineItem("prod_baby_spinach", "Hydroponic Baby Spinach", "v_spin_150", "150 g Box", ProductCategory.FRESH_PRODUCE, 1, 69, 85)
                ),
                deliveryAddressLabel = "Home",
                deliveryAddressFull = "Flat 402, Palm Grove Residency, 12th Main Rd, Indiranagar",
                deliveryInstructions = listOf("Call upon arrival"),
                riderNote = "Ring intercom 402 once at the gate.",
                paymentMethod = PaymentMethod.UPI,
                riderName = "Karthik Nair",
                riderPhone = "+91 98451 11904",
                riderRating = 4.8f,
                itemTotal = 448,
                mrpSavings = 86,
                deliveryFee = 0,
                handlingCharge = 4,
                promoDiscount = 30,
                tipAmount = 20,
                totalPaid = 442,
                userRating = 5,
                praiseTags = listOf("Super Fast", "Chilled & Fresh"),
                feedbackComment = "Croissants were crisp and cold brew arrived ice cold!"
            ),
            // Historical Order 2 (7 days ago - A2 Milk, Farm Eggs, Sourdough)
            OrderRecord(
                orderId = "QK-83904",
                createdAtMillis = daysAgo(7),
                neighborhoodName = "Indiranagar, Bengaluru",
                etaMinutes = 8,
                remainingSeconds = 0,
                stage = OrderStage.DELIVERED,
                items = listOf(
                    OrderLineItem("prod_a2_milk", "A2 Desi Cow Milk", "v_milk_1000", "1 L Pack", ProductCategory.DAIRY_EGGS, 1, 98, 115),
                    OrderLineItem("prod_farm_eggs", "Free-Range Brown Eggs", "v_eggs_12", "12 pcs", ProductCategory.DAIRY_EGGS, 1, 168, 199),
                    OrderLineItem("prod_sourdough", "Sourdough Country Loaf", "v_sd_400", "400 g Sliced", ProductCategory.BAKERY_BREAD, 1, 135, 155)
                ),
                deliveryAddressLabel = "Home",
                deliveryAddressFull = "Flat 402, Palm Grove Residency, 12th Main Rd, Indiranagar",
                deliveryInstructions = listOf("Leave at the door", "Avoid plastic bag"),
                riderNote = "No extra carry bag needed, thanks.",
                paymentMethod = PaymentMethod.CARD,
                riderName = "Suresh Babu",
                riderPhone = "+91 99001 45812",
                riderRating = 4.9f,
                itemTotal = 401,
                mrpSavings = 68,
                deliveryFee = 0,
                handlingCharge = 4,
                promoDiscount = 50,
                tipAmount = 10,
                totalPaid = 365,
                userRating = 5,
                praiseTags = listOf("Polite Rider", "Followed Note"),
                feedbackComment = "Delivered in 7 minutes flat."
            ),
            // Historical Order 3 (14 days ago - A2 Milk, Paneer, Tomatoes)
            OrderRecord(
                orderId = "QK-83118",
                createdAtMillis = daysAgo(14),
                neighborhoodName = "Koramangala, Bengaluru",
                etaMinutes = 9,
                remainingSeconds = 0,
                stage = OrderStage.DELIVERED,
                items = listOf(
                    OrderLineItem("prod_a2_milk", "A2 Desi Cow Milk", "v_milk_500", "500 ml", ProductCategory.DAIRY_EGGS, 2, 52, 60),
                    OrderLineItem("prod_malai_paneer", "Artisan Malai Paneer", "v_paneer_200", "200 g", ProductCategory.DAIRY_EGGS, 2, 89, 105),
                    OrderLineItem("prod_cherry_tomatoes", "Vine-Ripened Cherry Tomatoes", "v_tom_200", "200 g Punnet", ProductCategory.FRESH_PRODUCE, 1, 64, 80)
                ),
                deliveryAddressLabel = "Work",
                deliveryAddressFull = "WeWork Prestige Cube, Laskar Hosur Rd, Koramangala 4th Block",
                deliveryInstructions = listOf("Call upon arrival"),
                riderNote = "Drop at WeWork reception desk B.",
                paymentMethod = PaymentMethod.UPI,
                riderName = "Imran Pasha",
                riderPhone = "+91 97412 66301",
                riderRating = 4.7f,
                itemTotal = 346,
                mrpSavings = 64,
                deliveryFee = 0,
                handlingCharge = 4,
                promoDiscount = 0,
                tipAmount = 0,
                totalPaid = 350,
                userRating = 4,
                praiseTags = listOf("Super Fast")
            ),
            // Historical Order 4 (22 days ago - Eggs, Sourdough, Bananas, Peanut Butter)
            OrderRecord(
                orderId = "QK-82405",
                createdAtMillis = daysAgo(22),
                neighborhoodName = "Indiranagar, Bengaluru",
                etaMinutes = 8,
                remainingSeconds = 0,
                stage = OrderStage.DELIVERED,
                items = listOf(
                    OrderLineItem("prod_farm_eggs", "Free-Range Brown Eggs", "v_eggs_6", "6 pcs", ProductCategory.DAIRY_EGGS, 1, 88, 105),
                    OrderLineItem("prod_sourdough", "Sourdough Country Loaf", "v_sd_400", "400 g Sliced", ProductCategory.BAKERY_BREAD, 1, 135, 155),
                    OrderLineItem("prod_organic_bananas", "Robusta Baby Bananas", "v_ban_6", "500 g (5-6 pcs)", ProductCategory.FRESH_PRODUCE, 1, 42, 52),
                    OrderLineItem("prod_peanut_butter", "Crunchy Unsweetened Peanut Butter", "v_pb_350", "350 g Jar", ProductCategory.SNACKS_PANTRY, 1, 149, 175)
                ),
                deliveryAddressLabel = "Home",
                deliveryAddressFull = "Flat 402, Palm Grove Residency, 12th Main Rd, Indiranagar",
                deliveryInstructions = listOf("Don't ring bell"),
                riderNote = "Baby sleeping, please message on arrival.",
                paymentMethod = PaymentMethod.UPI,
                riderName = "Arjun Verma",
                riderPhone = "+91 98450 77218",
                riderRating = 4.9f,
                itemTotal = 414,
                mrpSavings = 73,
                deliveryFee = 0,
                handlingCharge = 4,
                promoDiscount = 20,
                tipAmount = 30,
                totalPaid = 428,
                userRating = 5,
                praiseTags = listOf("Followed Note", "Chilled & Fresh")
            ),
            // Historical Order 5 (47 days ago - Last 3 Months)
            OrderRecord(
                orderId = "QK-81092",
                createdAtMillis = daysAgo(47),
                neighborhoodName = "Indiranagar, Bengaluru",
                etaMinutes = 8,
                remainingSeconds = 0,
                stage = OrderStage.DELIVERED,
                items = listOf(
                    OrderLineItem("prod_a2_milk", "A2 Desi Cow Milk", "v_milk_1000", "1 L Pack", ProductCategory.DAIRY_EGGS, 1, 98, 115),
                    OrderLineItem("prod_orange_juice", "Cold-Pressed Valencia Orange", "v_oj_1000", "1 L Carafe", ProductCategory.COLD_BREW_JUICES, 1, 249, 299),
                    OrderLineItem("prod_kettle_chips", "Himalayan Salt Kettle Chips", "v_chip_110", "110 g Party", ProductCategory.SNACKS_PANTRY, 1, 89, 110)
                ),
                deliveryAddressLabel = "Home",
                deliveryAddressFull = "Flat 402, Palm Grove Residency, 12th Main Rd, Indiranagar",
                deliveryInstructions = listOf("Leave at the door"),
                riderNote = "",
                paymentMethod = PaymentMethod.CARD,
                riderName = "Rohan Kulkarni",
                riderPhone = "+91 98860 33120",
                riderRating = 4.8f,
                itemTotal = 436,
                mrpSavings = 88,
                deliveryFee = 0,
                handlingCharge = 4,
                promoDiscount = 50,
                tipAmount = 20,
                totalPaid = 410,
                userRating = 5,
                praiseTags = listOf("Super Fast", "Zero Spill")
            ),
            // Historical Order 6 (74 days ago - Last 3 Months)
            OrderRecord(
                orderId = "QK-79834",
                createdAtMillis = daysAgo(74),
                neighborhoodName = "Bandra West, Mumbai",
                etaMinutes = 10,
                remainingSeconds = 0,
                stage = OrderStage.DELIVERED,
                items = listOf(
                    OrderLineItem("prod_farm_eggs", "Free-Range Brown Eggs", "v_eggs_12", "12 pcs", ProductCategory.DAIRY_EGGS, 1, 168, 199),
                    OrderLineItem("prod_dark_chocolate", "70% Cacao Sea Salt Bar", "v_choc_75", "75 g Bar", ProductCategory.SNACKS_PANTRY, 1, 215, 250),
                    OrderLineItem("prod_makhana", "Peri-Peri Roasted Makhana", "v_makh_70", "70 g Jar", ProductCategory.SNACKS_PANTRY, 1, 119, 145)
                ),
                deliveryAddressLabel = "Other",
                deliveryAddressFull = "B-14 Sea Breeze Apartments, 16th Road, Bandra West",
                deliveryInstructions = listOf("Call upon arrival"),
                riderNote = "Hand over to security if no response.",
                paymentMethod = PaymentMethod.COD,
                riderName = "Vikram Sawant",
                riderPhone = "+91 98204 51092",
                riderRating = 4.8f,
                itemTotal = 502,
                mrpSavings = 92,
                deliveryFee = 0,
                handlingCharge = 4,
                promoDiscount = 50,
                tipAmount = 0,
                totalPaid = 456,
                userRating = 5,
                praiseTags = listOf("Polite Rider")
            ),
            // Historical Order 7 (112 days ago - All Time only)
            OrderRecord(
                orderId = "QK-77210",
                createdAtMillis = daysAgo(112),
                neighborhoodName = "Indiranagar, Bengaluru",
                etaMinutes = 8,
                remainingSeconds = 0,
                stage = OrderStage.DELIVERED,
                items = listOf(
                    OrderLineItem("prod_a2_milk", "A2 Desi Cow Milk", "v_milk_1000", "1 L Pack", ProductCategory.DAIRY_EGGS, 1, 98, 115),
                    OrderLineItem("prod_sugarcane", "Pressed Sugarcane & Ginger", "v_sc_500", "500 ml", ProductCategory.COLD_BREW_JUICES, 2, 82, 110),
                    OrderLineItem("prod_brioche", "Toasted Sesame Brioche Buns", "v_brio_4", "4 Buns (260g)", ProductCategory.BAKERY_BREAD, 1, 110, 130)
                ),
                deliveryAddressLabel = "Home",
                deliveryAddressFull = "Flat 402, Palm Grove Residency, 12th Main Rd, Indiranagar",
                deliveryInstructions = listOf("Avoid plastic bag"),
                riderNote = "",
                paymentMethod = PaymentMethod.UPI,
                riderName = "Karthik Nair",
                riderPhone = "+91 98451 11904",
                riderRating = 4.9f,
                itemTotal = 372,
                mrpSavings = 93,
                deliveryFee = 0,
                handlingCharge = 4,
                promoDiscount = 0,
                tipAmount = 15,
                totalPaid = 391,
                userRating = 4,
                praiseTags = listOf("Chilled & Fresh")
            )
        )
    }

    val supportFaqTopics = listOf(
        SupportFaqTopic(
            id = "faq_missing_item",
            question = "An item is missing from my bag — how fast do I get a refund?",
            answer = "Select Missing Item and tap the missing item(s). Under QuickKart's Instant Care Promise, orders up to ₹250 qualify for instant QuickKart Wallet credit in under 10 seconds, or a free 8-minute replacement.",
            linkedCategory = SupportIssueCategory.MISSING_ITEM
        ),
        SupportFaqTopic(
            id = "faq_damaged_coldchain",
            question = "Produce bruised or dairy/ice bag wasn't chilled?",
            answer = "Pick Damaged / Spoiled, optionally attach a quick photo proof from your gallery, and choose either Instant QuickKart Credit or a complimentary cold-chain replacement.",
            linkedCategory = SupportIssueCategory.DAMAGED_SPOILED
        ),
        SupportFaqTopic(
            id = "faq_wrong_variant",
            question = "Received the wrong size, weight, or swapped product?",
            answer = "Tap Wrong Item, check off the swapped product, and we'll dispatch the exact item in 8 minutes or issue an instant refund—no return pickup needed for perishables.",
            linkedCategory = SupportIssueCategory.WRONG_ITEM
        ),
        SupportFaqTopic(
            id = "faq_delay",
            question = "My order took longer than the promised delivery window",
            answer = "Choose Delivery Delay to track pod & rider logs or claim an instant ₹25 delay care credit directly into your QuickKart account.",
            linkedCategory = SupportIssueCategory.DELIVERY_DELAY
        ),
        SupportFaqTopic(
            id = "faq_promo_upi",
            question = "Promo code didn't apply or UPI payment debited twice",
            answer = "Raise a Billing / Promo request on the order and our Care Desk verifies UPI gateway settlement or applies missed promo credits right inside the ticket thread.",
            linkedCategory = SupportIssueCategory.BILLING_PROMO
        )
    )

    fun createSeededSupportTickets(nowMillis: Long = System.currentTimeMillis()): List<SupportTicketRecord> {
        fun hoursAgo(hours: Long): Long = nowMillis - TimeUnit.HOURS.toMillis(hours)
        fun daysAgo(days: Long): Long = nowMillis - TimeUnit.DAYS.toMillis(days)

        return listOf(
            SupportTicketRecord(
                ticketId = "SUP-4821",
                orderId = "QK-84311",
                createdAtMillis = hoursAgo(6),
                updatedAtMillis = hoursAgo(5),
                category = SupportIssueCategory.DAMAGED_SPOILED,
                affectedItemsSummary = "1x Hydroponic Baby Spinach (150 g Box)",
                affectedAmountInr = 69,
                preferredResolution = SupportResolutionOption.EXPRESS_REPLACEMENT,
                resolutionSummary = "8-Min Replacement pod verification queued",
                status = SupportTicketStatus.UNDER_REVIEW,
                photoUriString = null,
                messages = listOf(
                    SupportMessage(
                        id = "msg_seed_1",
                        isUser = true,
                        text = "The spinach box lid popped open inside the delivery bag.",
                        timestampMillis = hoursAgo(6)
                    ),
                    SupportMessage(
                        id = "msg_seed_2",
                        isUser = false,
                        text = "Ticket SUP-4821 is Under Review for QK-84311. Our Indiranagar Pod Lead is checking fresh stock for a ₹0 replacement, or tap 'Instant Credit' below to receive ₹69 immediately.",
                        timestampMillis = hoursAgo(5)
                    )
                ),
                csatRating = null
            ),
            SupportTicketRecord(
                ticketId = "SUP-4510",
                orderId = "QK-83118",
                createdAtMillis = daysAgo(14),
                updatedAtMillis = daysAgo(14) + TimeUnit.MINUTES.toMillis(4),
                category = SupportIssueCategory.DAMAGED_SPOILED,
                affectedItemsSummary = "1x Vine-Ripened Cherry Tomatoes (200 g Punnet)",
                affectedAmountInr = 64,
                preferredResolution = SupportResolutionOption.INSTANT_CREDIT,
                resolutionSummary = "₹64 Instant QuickKart Credit added",
                status = SupportTicketStatus.RESOLVED,
                photoUriString = null,
                messages = listOf(
                    SupportMessage(
                        id = "msg_seed_3",
                        isUser = true,
                        text = "A few cherry tomatoes at the bottom of the punnet were squished.",
                        timestampMillis = daysAgo(14)
                    ),
                    SupportMessage(
                        id = "msg_seed_4",
                        isUser = false,
                        text = "Per QuickKart's Instant Care Promise, ₹64 has been credited to your QuickKart Wallet under ref SUP-4510.",
                        timestampMillis = daysAgo(14) + TimeUnit.MINUTES.toMillis(1)
                    )
                ),
                csatRating = 5
            )
        )
    }
}
