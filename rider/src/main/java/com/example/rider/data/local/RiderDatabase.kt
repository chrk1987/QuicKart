package com.example.rider.data.local

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.Database
import androidx.room.RoomDatabase
import android.content.Context
import com.example.rider.data.model.PayoutTransaction
import com.example.rider.data.model.PayoutTxType
import com.example.rider.data.model.RiderPaymentMode
import com.example.rider.data.model.RiderTripOrder
import com.example.rider.data.model.RiderTripStage
import com.example.rider.data.model.TripLineItem
import kotlinx.coroutines.flow.Flow
import org.json.JSONArray
import org.json.JSONObject

@Entity(tableName = "rider_trips")
data class RiderTripEntity(
    @PrimaryKey val orderId: String,
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
    val instructionsJson: String,
    val riderNote: String,
    val paymentModeName: String,
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
    val stageName: String,
    val itemsJson: String,
    val podSealed: Boolean,
    val codCollected: Boolean,
    val podPhotoUri: String?,
    val createdAtMillis: Long,
    val acceptedAtMillis: Long?,
    val deliveredAtMillis: Long?,
    val offerExpiresAtMillis: Long?
)

@Entity(tableName = "payout_transactions")
data class PayoutTransactionEntity(
    @PrimaryKey val id: String,
    val typeName: String,
    val title: String,
    val amountInr: Int,
    val createdAtMillis: Long
)

fun RiderTripOrder.toEntity(): RiderTripEntity {
    val instrArr = JSONArray().apply { deliveryInstructions.forEach { put(it) } }
    val itemArr = JSONArray().apply {
        items.forEach { item ->
            put(
                JSONObject().apply {
                    put("productId", item.productId)
                    put("name", item.name)
                    put("variantLabel", item.variantLabel)
                    put("quantity", item.quantity)
                    put("unitPriceInr", item.unitPriceInr)
                    put("isPicked", item.isPicked)
                }
            )
        }
    }
    return RiderTripEntity(
        orderId = orderId,
        hubId = hubId,
        hubName = hubName,
        pickupBayCode = pickupBayCode,
        bagSealCode = bagSealCode,
        hubLat = hubLat,
        hubLng = hubLng,
        customerName = customerName,
        customerPhone = customerPhone,
        deliveryAddressLabel = deliveryAddressLabel,
        deliveryAddressFull = deliveryAddressFull,
        dropLat = dropLat,
        dropLng = dropLng,
        instructionsJson = instrArr.toString(),
        riderNote = riderNote,
        paymentModeName = paymentMode.name,
        collectAmountInr = collectAmountInr,
        deliveryOtp = deliveryOtp,
        basePayInr = basePayInr,
        distancePayInr = distancePayInr,
        surgeBonusInr = surgeBonusInr,
        tipInr = tipInr,
        onTimeBonusInr = onTimeBonusInr,
        distanceKm = distanceKm,
        estimatedMinutes = estimatedMinutes,
        deliveryWindowLabel = deliveryWindowLabel,
        stageName = stage.name,
        itemsJson = itemArr.toString(),
        podSealed = podSealed,
        codCollected = codCollected,
        podPhotoUri = podPhotoUri,
        createdAtMillis = createdAtMillis,
        acceptedAtMillis = acceptedAtMillis,
        deliveredAtMillis = deliveredAtMillis,
        offerExpiresAtMillis = offerExpiresAtMillis
    )
}

fun RiderTripEntity.toModel(): RiderTripOrder {
    val instrList = buildList {
        val arr = runCatching { JSONArray(instructionsJson) }.getOrNull() ?: JSONArray()
        for (i in 0 until arr.length()) {
            add(arr.optString(i))
        }
    }
    val itemList = buildList {
        val arr = runCatching { JSONArray(itemsJson) }.getOrNull() ?: JSONArray()
        for (i in 0 until arr.length()) {
            val obj = arr.optJSONObject(i) ?: continue
            add(
                TripLineItem(
                    productId = obj.optString("productId"),
                    name = obj.optString("name"),
                    variantLabel = obj.optString("variantLabel"),
                    quantity = obj.optInt("quantity", 1),
                    unitPriceInr = obj.optInt("unitPriceInr", 0),
                    isPicked = obj.optBoolean("isPicked", false)
                )
            )
        }
    }
    val mode = runCatching { RiderPaymentMode.valueOf(paymentModeName) }
        .getOrDefault(RiderPaymentMode.PREPAID_UPI)
    val stage = runCatching { RiderTripStage.valueOf(stageName) }
        .getOrDefault(RiderTripStage.OFFERED)

    return RiderTripOrder(
        orderId = orderId,
        hubId = hubId,
        hubName = hubName,
        pickupBayCode = pickupBayCode,
        bagSealCode = bagSealCode,
        hubLat = hubLat,
        hubLng = hubLng,
        customerName = customerName,
        customerPhone = customerPhone,
        deliveryAddressLabel = deliveryAddressLabel,
        deliveryAddressFull = deliveryAddressFull,
        dropLat = dropLat,
        dropLng = dropLng,
        deliveryInstructions = instrList,
        riderNote = riderNote,
        paymentMode = mode,
        collectAmountInr = collectAmountInr,
        deliveryOtp = deliveryOtp,
        basePayInr = basePayInr,
        distancePayInr = distancePayInr,
        surgeBonusInr = surgeBonusInr,
        tipInr = tipInr,
        onTimeBonusInr = onTimeBonusInr,
        distanceKm = distanceKm,
        estimatedMinutes = estimatedMinutes,
        deliveryWindowLabel = deliveryWindowLabel,
        stage = stage,
        items = itemList,
        podSealed = podSealed,
        codCollected = codCollected,
        podPhotoUri = podPhotoUri,
        createdAtMillis = createdAtMillis,
        acceptedAtMillis = acceptedAtMillis,
        deliveredAtMillis = deliveredAtMillis,
        offerExpiresAtMillis = offerExpiresAtMillis
    )
}

fun PayoutTransaction.toEntity(): PayoutTransactionEntity = PayoutTransactionEntity(
    id = id,
    typeName = type.name,
    title = title,
    amountInr = amountInr,
    createdAtMillis = createdAtMillis
)

fun PayoutTransactionEntity.toModel(): PayoutTransaction = PayoutTransaction(
    id = id,
    type = runCatching { PayoutTxType.valueOf(typeName) }.getOrDefault(PayoutTxType.BANK_WITHDRAWAL),
    title = title,
    amountInr = amountInr,
    createdAtMillis = createdAtMillis
)

@Dao
interface RiderTripDao {
    @Query("SELECT * FROM rider_trips ORDER BY createdAtMillis DESC")
    fun observeAllTrips(): Flow<List<RiderTripEntity>>

    @Query("SELECT COUNT(*) FROM rider_trips")
    suspend fun getTripCount(): Int

    @Query("SELECT * FROM rider_trips WHERE orderId = :orderId LIMIT 1")
    suspend fun getTripById(orderId: String): RiderTripEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTrip(entity: RiderTripEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTrips(entities: List<RiderTripEntity>)

    @Query("DELETE FROM rider_trips")
    suspend fun clearAllTrips()
}

@Dao
interface PayoutTransactionDao {
    @Query("SELECT * FROM payout_transactions ORDER BY createdAtMillis DESC")
    fun observeAllTransactions(): Flow<List<PayoutTransactionEntity>>

    @Query("SELECT COUNT(*) FROM payout_transactions")
    suspend fun getCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(entity: PayoutTransactionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactions(entities: List<PayoutTransactionEntity>)
}

@Database(
    entities = [RiderTripEntity::class, PayoutTransactionEntity::class],
    version = 1,
    exportSchema = false
)
abstract class RiderDatabase : RoomDatabase() {
    abstract fun tripDao(): RiderTripDao
    abstract fun transactionDao(): PayoutTransactionDao

    companion object {
        @Volatile
        private var INSTANCE: RiderDatabase? = null

        fun getInstance(context: Context): RiderDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    RiderDatabase::class.java,
                    "quickkart_rider.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
