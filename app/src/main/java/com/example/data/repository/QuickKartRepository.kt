package com.example.data.repository

import com.example.util.itemCountLabel
import com.example.data.local.CartDao
import com.example.data.local.CartItemEntity
import com.example.data.local.FavoriteDao
import com.example.data.local.FavoriteEntity
import com.example.data.local.OrderDao
import com.example.data.local.OrderEntity
import com.example.data.local.PreferencesManager
import com.example.data.local.SavedAddressDao
import com.example.data.local.SavedAddressEntity
import com.example.data.local.SeedData
import com.example.data.local.SupportTicketDao
import com.example.data.local.SupportTicketEntity
import com.example.data.local.UserPreferences
import com.example.data.local.WalletDao
import com.example.data.local.WalletTransactionEntity
import com.example.data.model.CartItem
import com.example.data.model.Neighborhood
import com.example.data.model.OrderLineItem
import com.example.data.model.OrderRecord
import com.example.data.model.OrderStage
import com.example.data.model.Product
import com.example.data.model.PromoCode
import com.example.data.model.RestockSuggestion
import com.example.data.model.SavedAddress
import com.example.data.model.SupportIssueCategory
import com.example.data.model.SupportMessage
import com.example.data.model.SupportResolutionOption
import com.example.data.model.SupportTicketRecord
import com.example.data.model.SupportTicketStatus
import com.example.data.model.WalletTransaction
import com.example.data.model.computeSupportRefundEstimate
import com.example.data.model.generateSupportDeskReply
import com.example.data.model.orderLineItemKey
import com.example.data.model.resolveInitialTicketOutcome
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt
import kotlin.random.Random

class QuickKartRepository(
    private val favoriteDao: FavoriteDao,
    private val cartDao: CartDao,
    private val addressDao: SavedAddressDao,
    private val orderDao: OrderDao,
    private val supportTicketDao: SupportTicketDao,
    private val walletDao: WalletDao,
    private val prefsManager: PreferencesManager
) {
    val products: List<Product> = SeedData.products
    val neighborhoods: List<Neighborhood> = SeedData.neighborhoods
    val availablePromos: List<PromoCode> = SeedData.availablePromos

    val userPreferences: Flow<UserPreferences> = prefsManager.preferencesFlow

    val favoriteProductIds: Flow<Set<String>> = favoriteDao.observeFavoriteIds()
        .map { it.toSet() }

    val cartItems: Flow<List<CartItem>> = cartDao.observeCartItems().map { entities ->
        entities.mapNotNull { entity ->
            val product = products.find { it.id == entity.productId } ?: return@mapNotNull null
            val variant = product.variants.find { it.id == entity.variantId }
                ?: product.defaultVariant
            CartItem(
                product = product,
                variant = variant,
                quantity = entity.quantity
            )
        }
    }

    val savedAddresses: Flow<List<SavedAddress>> = addressDao.observeAddresses()
        .map { list -> list.map { it.toDomain() } }

    val orders: Flow<List<OrderRecord>> = orderDao.observeOrders()
        .map { list -> list.map { it.toDomain() } }

    val supportTickets: Flow<List<SupportTicketRecord>> = supportTicketDao.observeTickets()
        .map { list -> list.map { it.toDomain() } }

    val walletTransactions: Flow<List<WalletTransaction>> = walletDao.observeTransactions()
        .map { list -> list.map { it.toDomain() } }

    suspend fun ensureSeeded() {
        if (addressDao.count() == 0) {
            addressDao.insertAll(SeedData.savedAddresses.map { SavedAddressEntity.fromDomain(it) })
        }
        if (orderDao.count() == 0) {
            orderDao.insertAll(SeedData.createSeededOrders().map { OrderEntity.fromDomain(it) })
            // Seed initial favorites for extra polish
            favoriteDao.insertFavorite(FavoriteEntity("prod_a2_milk"))
            favoriteDao.insertFavorite(FavoriteEntity("prod_sourdough"))
            favoriteDao.insertFavorite(FavoriteEntity("prod_cold_brew"))
        }
        if (supportTicketDao.count() == 0) {
            supportTicketDao.insertAll(
                SeedData.createSeededSupportTickets().map { SupportTicketEntity.fromDomain(it) }
            )
        }
        if (walletDao.count() == 0) {
            val now = System.currentTimeMillis()
            val seededWallet = listOf(
                WalletTransactionEntity(
                    id = "WTX-INIT-101",
                    title = "QuickKart Welcome Bonus",
                    subtitle = "Instant signup promo credit",
                    amountInr = 250,
                    isCredit = true,
                    createdAtMillis = now - 86400000L * 3
                ),
                WalletTransactionEntity(
                    id = "WTX-INIT-102",
                    title = "Instant Care Refund",
                    subtitle = "Ref for missing item in #ORD-3021",
                    amountInr = 120,
                    isCredit = true,
                    createdAtMillis = now - 86400000L * 2
                ),
                WalletTransactionEntity(
                    id = "WTX-INIT-103",
                    title = "Paid for Order #ORD-3012",
                    subtitle = "Fresh bakery & organic milk",
                    amountInr = 180,
                    isCredit = false,
                    createdAtMillis = now - 86400000L
                )
            )
            walletDao.insertAll(seededWallet)
        }
    }

    suspend fun addMoneyToWallet(amountInr: Int, note: String = "Added via UPI / Netbanking") {
        val tx = WalletTransactionEntity(
            id = "WTX-${System.currentTimeMillis()}-${Random.nextInt(100, 999)}",
            title = "Money Added",
            subtitle = note,
            amountInr = amountInr,
            isCredit = true,
            createdAtMillis = System.currentTimeMillis()
        )
        walletDao.insert(tx)
    }

    suspend fun debitWallet(amountInr: Int, orderId: String) {
        val tx = WalletTransactionEntity(
            id = "WTX-${System.currentTimeMillis()}-${Random.nextInt(100, 999)}",
            title = "Paid for Order #$orderId",
            subtitle = "QuickKart 8-min grocery order",
            amountInr = amountInr,
            isCredit = false,
            createdAtMillis = System.currentTimeMillis()
        )
        walletDao.insert(tx)
    }

    suspend fun toggleFavorite(productId: String) {
        if (favoriteDao.isFavorite(productId) > 0) {
            favoriteDao.deleteFavorite(productId)
        } else {
            favoriteDao.insertFavorite(FavoriteEntity(productId))
        }
    }

    suspend fun setCartQuantity(productId: String, variantId: String, quantity: Int) {
        val key = "$productId::$variantId"
        if (quantity <= 0) {
            cartDao.deleteCartItem(key)
        } else {
            val existing = cartDao.getCartItem(key)
            cartDao.upsertCartItem(
                CartItemEntity(
                    key = key,
                    productId = productId,
                    variantId = variantId,
                    quantity = quantity,
                    addedAtMillis = existing?.addedAtMillis ?: System.currentTimeMillis()
                )
            )
        }
    }

    suspend fun incrementCartItem(productId: String, variantId: String) {
        val key = "$productId::$variantId"
        val currentQty = cartDao.getCartItem(key)?.quantity ?: 0
        setCartQuantity(productId, variantId, currentQty + 1)
    }

    suspend fun decrementCartItem(productId: String, variantId: String) {
        val key = "$productId::$variantId"
        val currentQty = cartDao.getCartItem(key)?.quantity ?: 0
        setCartQuantity(productId, variantId, currentQty - 1)
    }

    suspend fun addOrderItemsToCart(items: List<OrderLineItem>, replaceExisting: Boolean) {
        if (replaceExisting) {
            val entities = items.map { item ->
                CartItemEntity(
                    key = "${item.productId}::${item.variantId}",
                    productId = item.productId,
                    variantId = item.variantId,
                    quantity = item.quantity
                )
            }
            cartDao.replaceCart(entities)
        } else {
            items.forEach { item ->
                val key = "${item.productId}::${item.variantId}"
                val existing = cartDao.getCartItem(key)
                cartDao.upsertCartItem(
                    CartItemEntity(
                        key = key,
                        productId = item.productId,
                        variantId = item.variantId,
                        quantity = (existing?.quantity ?: 0) + item.quantity,
                        addedAtMillis = existing?.addedAtMillis ?: System.currentTimeMillis()
                    )
                )
            }
        }
    }

    suspend fun quickAddProducts(itemsToAdd: List<Product>) {
        itemsToAdd.forEach { product ->
            val variant = product.defaultVariant
            val key = "${product.id}::${variant.id}"
            val existing = cartDao.getCartItem(key)
            cartDao.upsertCartItem(
                CartItemEntity(
                    key = key,
                    productId = product.id,
                    variantId = variant.id,
                    quantity = (existing?.quantity ?: 0) + 1,
                    addedAtMillis = existing?.addedAtMillis ?: System.currentTimeMillis()
                )
            )
        }
    }

    suspend fun clearCart() = cartDao.clearCart()

    suspend fun addAddress(address: SavedAddress) {
        addressDao.upsert(SavedAddressEntity.fromDomain(address))
        if (address.isDefault) {
            addressDao.setDefaultAddress(address.id)
        }
        prefsManager.setSelectedAddressId(address.id)
    }

    suspend fun setDefaultAddress(addressId: String) {
        addressDao.setDefaultAddress(addressId)
        prefsManager.setSelectedAddressId(addressId)
    }

    suspend fun deleteAddress(addressId: String) {
        addressDao.deleteAddress(addressId)
    }

    suspend fun saveOrder(order: OrderRecord) {
        orderDao.upsertOrder(OrderEntity.fromDomain(order))
    }

    suspend fun deleteOrder(orderId: String) = orderDao.deleteOrder(orderId)

    /** Credits the wallet with a non top-up entry (refunds, referral rewards, cashback). */
    suspend fun creditWallet(title: String, note: String, amountInr: Int) {
        walletDao.insert(
            WalletTransactionEntity(
                id = "WTX-${System.currentTimeMillis()}-${Random.nextInt(100, 999)}",
                title = title,
                subtitle = note,
                amountInr = amountInr,
                isCredit = true,
                createdAtMillis = System.currentTimeMillis()
            )
        )
    }

    suspend fun addRecentSearch(query: String) = prefsManager.addRecentSearch(query)
    suspend fun clearRecentSearches() = prefsManager.clearRecentSearches()
    suspend fun markInboxSeen(millis: Long) = prefsManager.markInboxSeen(millis)
    suspend fun savePaymentMethod(upiId: String?, cardLast4: String?) =
        prefsManager.savePaymentMethod(upiId, cardLast4)
    suspend fun removeSavedPaymentMethods() = prefsManager.removeSavedPaymentMethods()
    suspend fun setReferralRedeemed() = prefsManager.setReferralRedeemed()

    suspend fun updateOrderStage(orderId: String, newStage: OrderStage, newRemainingSeconds: Int) {
        val current = orderDao.getOrderById(orderId)?.toDomain() ?: return
        val updated = current.copy(
            stage = newStage,
            remainingSeconds = newRemainingSeconds
        )
        orderDao.upsertOrder(OrderEntity.fromDomain(updated))
    }

    suspend fun updateOrderRemainingSeconds(orderId: String, remainingSeconds: Int) {
        val current = orderDao.getOrderById(orderId)?.toDomain() ?: return
        if (current.remainingSeconds == remainingSeconds) return
        orderDao.upsertOrder(
            OrderEntity.fromDomain(current.copy(remainingSeconds = remainingSeconds))
        )
    }

    suspend fun submitOrderRating(
        orderId: String,
        rating: Int,
        praiseTags: List<String>,
        comment: String
    ) {
        val current = orderDao.getOrderById(orderId)?.toDomain() ?: return
        orderDao.upsertOrder(
            OrderEntity.fromDomain(
                current.copy(
                    userRating = rating,
                    praiseTags = praiseTags,
                    feedbackComment = comment
                )
            )
        )
    }

    suspend fun setDarkTheme(enabled: Boolean) = prefsManager.setDarkTheme(enabled)
    suspend fun setFollowSystemTheme() = prefsManager.setFollowSystemTheme()
    suspend fun setNotificationsEnabled(enabled: Boolean) = prefsManager.setNotificationsEnabled(enabled)
    suspend fun setScreenZoomPercent(percent: Int) = prefsManager.setScreenZoomPercent(percent)
    suspend fun setSelectedNeighborhood(id: String) = prefsManager.setSelectedNeighborhood(id)
    suspend fun setSelectedAddressId(id: String) = prefsManager.setSelectedAddressId(id)
    suspend fun signInWithPhone(
        phone: String,
        neighborhoodId: String,
        name: String = "Aarav Sharma",
        email: String = "",
        googleEmail: String = ""
    ) = prefsManager.signInWithPhone(phone, neighborhoodId, name, email, googleEmail)

    suspend fun linkGoogleAccount(googleEmail: String) = prefsManager.linkGoogleAccount(googleEmail)
    suspend fun unlinkGoogleAccount() = prefsManager.unlinkGoogleAccount()

    suspend fun updateUserProfile(name: String, email: String, phone: String? = null) =
        prefsManager.updateUserProfile(name, email, phone)

    suspend fun signInAsGuest(neighborhoodId: String) =
        prefsManager.signInAsGuest(neighborhoodId)
    suspend fun signOut() = prefsManager.signOut()

    /**
     * Analyzes delivered order frequency to recommend staples likely running low.
     */
    fun computeRestockSuggestions(allOrders: List<OrderRecord>): List<RestockSuggestion> {
        val now = System.currentTimeMillis()
        val delivered = allOrders.filter { it.stage == OrderStage.DELIVERED }
        if (delivered.isEmpty()) return emptyList()

        val occurrences = mutableMapOf<String, MutableList<Long>>()
        delivered.forEach { order ->
            order.items.forEach { line ->
                occurrences.getOrPut(line.productId) { mutableListOf() }.add(order.createdAtMillis)
            }
        }

        return occurrences.mapNotNull { (productId, timestamps) ->
            val product = products.find { it.id == productId } ?: return@mapNotNull null
            if (timestamps.size < 2) return@mapNotNull null
            val sorted = timestamps.sorted()
            val lastMillis = sorted.last()
            val daysSinceLast = TimeUnit.MILLISECONDS.toDays(now - lastMillis).toInt().coerceAtLeast(1)
            val spanDays = TimeUnit.MILLISECONDS.toDays(sorted.last() - sorted.first()).toFloat()
            val avgInterval = (spanDays / (sorted.size - 1)).roundToInt().coerceIn(3, 21)

            RestockSuggestion(
                product = product,
                daysSinceLastOrder = daysSinceLast,
                avgIntervalDays = avgInterval,
                orderCount = sorted.size
            )
        }
            .sortedByDescending { (it.daysSinceLastOrder.toFloat() / it.avgIntervalDays) * it.orderCount }
            .take(3)
    }

    suspend fun createSupportTicket(
        order: OrderRecord,
        category: SupportIssueCategory,
        selectedItemKeys: Set<String>,
        preferredResolution: SupportResolutionOption,
        userNote: String,
        photoUriString: String?
    ): SupportTicketRecord {
        val now = System.currentTimeMillis()
        val ticketId = "SUP-${Random.nextInt(5000, 9999)}"
        val selectedItems = order.items.filter { orderLineItemKey(it) in selectedItemKeys }
        val affectedItemsSummary = if (selectedItems.isNotEmpty()) {
            selectedItems.joinToString(", ") { "${it.quantity}x ${it.productName} (${it.variantLabel})" }
        } else {
            "Full Order ${order.orderId} (${itemCountLabel(order.totalItemCount)})"
        }
        val estimatedAmount = computeSupportRefundEstimate(order, selectedItemKeys, category)
        val outcome = resolveInitialTicketOutcome(
            ticketId = ticketId,
            order = order,
            category = category,
            affectedItemsSummary = affectedItemsSummary,
            affectedAmountInr = estimatedAmount,
            preferredResolution = preferredResolution,
            hasPhoto = !photoUriString.isNullOrBlank()
        )
        val cleanNote = userNote.trim().ifBlank {
            "${category.label} reported for $affectedItemsSummary"
        }
        val initialMessages = listOf(
            SupportMessage(
                id = "msg_${now}_u",
                isUser = true,
                text = cleanNote,
                timestampMillis = now
            ),
            SupportMessage(
                id = "msg_${now}_d",
                isUser = false,
                text = outcome.deskMessage,
                timestampMillis = now + 500L
            )
        )

        val record = SupportTicketRecord(
            ticketId = ticketId,
            orderId = order.orderId,
            createdAtMillis = now,
            updatedAtMillis = now + 500L,
            category = category,
            affectedItemsSummary = affectedItemsSummary,
            affectedAmountInr = estimatedAmount,
            preferredResolution = preferredResolution,
            resolutionSummary = outcome.resolutionSummary,
            status = outcome.status,
            photoUriString = photoUriString,
            messages = initialMessages,
            csatRating = null
        )
        supportTicketDao.upsertTicket(SupportTicketEntity.fromDomain(record))
        return record
    }

    suspend fun sendSupportMessage(ticketId: String, userText: String) {
        val clean = userText.trim()
        if (clean.isEmpty()) return
        val current = supportTicketDao.getTicketById(ticketId)?.toDomain() ?: return
        val now = System.currentTimeMillis()
        val userMsg = SupportMessage(
            id = "msg_${now}_u",
            isUser = true,
            text = clean,
            timestampMillis = now
        )
        val action = generateSupportDeskReply(current, clean)
        val deskMsg = SupportMessage(
            id = "msg_${now}_d",
            isUser = false,
            text = action.replyText,
            timestampMillis = now + 400L
        )
        val updated = current.copy(
            updatedAtMillis = now + 400L,
            status = action.newStatus ?: current.status,
            resolutionSummary = action.updatedResolutionSummary ?: current.resolutionSummary,
            messages = current.messages + userMsg + deskMsg
        )
        supportTicketDao.upsertTicket(SupportTicketEntity.fromDomain(updated))
    }

    suspend fun resolveSupportTicket(ticketId: String, resolutionSummary: String? = null) {
        val current = supportTicketDao.getTicketById(ticketId)?.toDomain() ?: return
        val now = System.currentTimeMillis()
        val finalSummary = resolutionSummary ?: current.resolutionSummary
        val closeMsg = SupportMessage(
            id = "msg_${now}_res",
            isUser = false,
            text = "Ticket $ticketId marked Resolved ($finalSummary). Thank you for shopping with QuickKart!",
            timestampMillis = now
        )
        val updated = current.copy(
            updatedAtMillis = now,
            status = SupportTicketStatus.RESOLVED,
            resolutionSummary = finalSummary,
            messages = current.messages + closeMsg
        )
        supportTicketDao.upsertTicket(SupportTicketEntity.fromDomain(updated))
    }

    suspend fun reopenSupportTicket(ticketId: String) {
        val current = supportTicketDao.getTicketById(ticketId)?.toDomain() ?: return
        val now = System.currentTimeMillis()
        val reopenMsg = SupportMessage(
            id = "msg_${now}_reopen",
            isUser = false,
            text = "Ticket $ticketId has been reopened for follow-up. Tell us what's still unresolved and our Care Desk will assist immediately.",
            timestampMillis = now
        )
        val updated = current.copy(
            updatedAtMillis = now,
            status = SupportTicketStatus.UNDER_REVIEW,
            csatRating = null,
            messages = current.messages + reopenMsg
        )
        supportTicketDao.upsertTicket(SupportTicketEntity.fromDomain(updated))
    }

    suspend fun submitSupportCsat(ticketId: String, rating: Int) {
        val current = supportTicketDao.getTicketById(ticketId)?.toDomain() ?: return
        val updated = current.copy(
            csatRating = rating.coerceIn(1, 5)
        )
        supportTicketDao.upsertTicket(SupportTicketEntity.fromDomain(updated))
    }
}
