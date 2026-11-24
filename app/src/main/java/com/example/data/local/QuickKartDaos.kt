package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {
    @Query("SELECT productId FROM favorites")
    fun observeFavoriteIds(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(entity: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE productId = :productId")
    suspend fun deleteFavorite(productId: String)

    @Query("SELECT COUNT(*) FROM favorites WHERE productId = :productId")
    suspend fun isFavorite(productId: String): Int
}

@Dao
interface CartDao {
    @Query("SELECT * FROM cart_items ORDER BY addedAtMillis ASC")
    fun observeCartItems(): Flow<List<CartItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCartItem(entity: CartItemEntity)

    @Query("SELECT * FROM cart_items WHERE `key` = :key LIMIT 1")
    suspend fun getCartItem(key: String): CartItemEntity?

    @Query("DELETE FROM cart_items WHERE `key` = :key")
    suspend fun deleteCartItem(key: String)

    @Query("DELETE FROM cart_items")
    suspend fun clearCart()

    @Transaction
    suspend fun replaceCart(items: List<CartItemEntity>) {
        clearCart()
        items.forEach { upsertCartItem(it) }
    }
}

@Dao
interface SavedAddressDao {
    @Query("SELECT * FROM saved_addresses ORDER BY isDefault DESC, label ASC")
    fun observeAddresses(): Flow<List<SavedAddressEntity>>

    @Query("SELECT COUNT(*) FROM saved_addresses")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(addresses: List<SavedAddressEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(address: SavedAddressEntity)

    @Query("UPDATE saved_addresses SET isDefault = CASE WHEN id = :id THEN 1 ELSE 0 END")
    suspend fun setDefaultAddress(id: String)

    @Query("DELETE FROM saved_addresses WHERE id = :id")
    suspend fun deleteAddress(id: String)
}

@Dao
interface OrderDao {
    @Query("SELECT * FROM orders ORDER BY createdAtMillis DESC")
    fun observeOrders(): Flow<List<OrderEntity>>

    @Query("SELECT COUNT(*) FROM orders")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(orders: List<OrderEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertOrder(order: OrderEntity)

    @Query("SELECT * FROM orders WHERE orderId = :orderId LIMIT 1")
    suspend fun getOrderById(orderId: String): OrderEntity?
}
