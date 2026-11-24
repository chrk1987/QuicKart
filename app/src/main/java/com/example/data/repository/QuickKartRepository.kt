package com.example.data.repository

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
import com.example.data.local.UserPreferences
import com.example.data.model.CartItem
import com.example.data.model.Neighborhood
import com.example.data.model.OrderLineItem
import com.example.data.model.OrderRecord
import com.example.data.model.OrderStage
import com.example.data.model.Product
import com.example.data.model.PromoCode
import com.example.data.model.RestockSuggestion
import com.example.data.model.SavedAddress
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

class QuickKartRepository(
    private val favoriteDao: FavoriteDao,
    private val cartDao: CartDao,
    private val addressDao: SavedAddressDao,
    private val orderDao: OrderDao,
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
    suspend fun setNotificationsEnabled(enabled: Boolean) = prefsManager.setNotificationsEnabled(enabled)
    suspend fun setSelectedNeighborhood(id: String) = prefsManager.setSelectedNeighborhood(id)
    suspend fun setSelectedAddressId(id: String) = prefsManager.setSelectedAddressId(id)
    suspend fun signInWithPhone(phone: String, neighborhoodId: String) =
        prefsManager.signInWithPhone(phone, neighborhoodId)
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
}
