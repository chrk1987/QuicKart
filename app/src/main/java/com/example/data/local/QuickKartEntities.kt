package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.OrderLineItem
import com.example.data.model.OrderRecord
import com.example.data.model.OrderStage
import com.example.data.model.PaymentMethod
import com.example.data.model.ProductCategory
import com.example.data.model.SavedAddress
import org.json.JSONArray
import org.json.JSONObject

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey val productId: String
)

@Entity(tableName = "cart_items")
data class CartItemEntity(
    @PrimaryKey val key: String, // "$productId::$variantId"
    val productId: String,
    val variantId: String,
    val quantity: Int,
    val addedAtMillis: Long = System.currentTimeMillis()
)

@Entity(tableName = "saved_addresses")
data class SavedAddressEntity(
    @PrimaryKey val id: String,
    val label: String,
    val fullAddress: String,
    val landmark: String,
    val neighborhoodId: String,
    val isDefault: Boolean
) {
    fun toDomain(): SavedAddress = SavedAddress(
        id = id,
        label = label,
        fullAddress = fullAddress,
        landmark = landmark,
        neighborhoodId = neighborhoodId,
        isDefault = isDefault
    )

    companion object {
        fun fromDomain(address: SavedAddress): SavedAddressEntity = SavedAddressEntity(
            id = address.id,
            label = address.label,
            fullAddress = address.fullAddress,
            landmark = address.landmark,
            neighborhoodId = address.neighborhoodId,
            isDefault = address.isDefault
        )
    }
}

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey val orderId: String,
    val createdAtMillis: Long,
    val neighborhoodName: String,
    val etaMinutes: Int,
    val remainingSeconds: Int,
    val stageName: String,
    val itemsJson: String,
    val deliveryAddressLabel: String,
    val deliveryAddressFull: String,
    val deliveryInstructionsJson: String,
    val riderNote: String,
    val paymentMethodName: String,
    val riderName: String,
    val riderPhone: String,
    val riderRating: Float,
    val itemTotal: Int,
    val mrpSavings: Int,
    val deliveryFee: Int,
    val handlingCharge: Int,
    val promoDiscount: Int,
    val tipAmount: Int,
    val totalPaid: Int,
    val userRating: Int?,
    val praiseTagsJson: String,
    val feedbackComment: String
) {
    fun toDomain(): OrderRecord {
        val items = mutableListOf<OrderLineItem>()
        val itemsArray = JSONArray(itemsJson)
        for (i in 0 until itemsArray.length()) {
            val obj = itemsArray.getJSONObject(i)
            val category = runCatching { ProductCategory.valueOf(obj.getString("category")) }
                .getOrDefault(ProductCategory.ALL)
            items.add(
                OrderLineItem(
                    productId = obj.getString("productId"),
                    productName = obj.getString("productName"),
                    variantId = obj.getString("variantId"),
                    variantLabel = obj.getString("variantLabel"),
                    category = category,
                    quantity = obj.getInt("quantity"),
                    unitPriceInr = obj.getInt("unitPriceInr"),
                    unitMrpInr = obj.getInt("unitMrpInr")
                )
            )
        }
        val instructions = jsonToStringList(deliveryInstructionsJson)
        val praiseTags = jsonToStringList(praiseTagsJson)
        val stage = runCatching { OrderStage.valueOf(stageName) }.getOrDefault(OrderStage.CONFIRMED)
        val paymentMethod = runCatching { PaymentMethod.valueOf(paymentMethodName) }
            .getOrDefault(PaymentMethod.UPI)

        return OrderRecord(
            orderId = orderId,
            createdAtMillis = createdAtMillis,
            neighborhoodName = neighborhoodName,
            etaMinutes = etaMinutes,
            remainingSeconds = remainingSeconds,
            stage = stage,
            items = items,
            deliveryAddressLabel = deliveryAddressLabel,
            deliveryAddressFull = deliveryAddressFull,
            deliveryInstructions = instructions,
            riderNote = riderNote,
            paymentMethod = paymentMethod,
            riderName = riderName,
            riderPhone = riderPhone,
            riderRating = riderRating,
            itemTotal = itemTotal,
            mrpSavings = mrpSavings,
            deliveryFee = deliveryFee,
            handlingCharge = handlingCharge,
            promoDiscount = promoDiscount,
            tipAmount = tipAmount,
            totalPaid = totalPaid,
            userRating = userRating,
            praiseTags = praiseTags,
            feedbackComment = feedbackComment
        )
    }

    companion object {
        fun fromDomain(order: OrderRecord): OrderEntity {
            val itemsArray = JSONArray()
            order.items.forEach { item ->
                itemsArray.put(
                    JSONObject().apply {
                        put("productId", item.productId)
                        put("productName", item.productName)
                        put("variantId", item.variantId)
                        put("variantLabel", item.variantLabel)
                        put("category", item.category.name)
                        put("quantity", item.quantity)
                        put("unitPriceInr", item.unitPriceInr)
                        put("unitMrpInr", item.unitMrpInr)
                    }
                )
            }
            return OrderEntity(
                orderId = order.orderId,
                createdAtMillis = order.createdAtMillis,
                neighborhoodName = order.neighborhoodName,
                etaMinutes = order.etaMinutes,
                remainingSeconds = order.remainingSeconds,
                stageName = order.stage.name,
                itemsJson = itemsArray.toString(),
                deliveryAddressLabel = order.deliveryAddressLabel,
                deliveryAddressFull = order.deliveryAddressFull,
                deliveryInstructionsJson = stringListToJson(order.deliveryInstructions),
                riderNote = order.riderNote,
                paymentMethodName = order.paymentMethod.name,
                riderName = order.riderName,
                riderPhone = order.riderPhone,
                riderRating = order.riderRating,
                itemTotal = order.itemTotal,
                mrpSavings = order.mrpSavings,
                deliveryFee = order.deliveryFee,
                handlingCharge = order.handlingCharge,
                promoDiscount = order.promoDiscount,
                tipAmount = order.tipAmount,
                totalPaid = order.totalPaid,
                userRating = order.userRating,
                praiseTagsJson = stringListToJson(order.praiseTags),
                feedbackComment = order.feedbackComment
            )
        }

        private fun stringListToJson(list: List<String>): String {
            val arr = JSONArray()
            list.forEach { arr.put(it) }
            return arr.toString()
        }

        private fun jsonToStringList(json: String): List<String> {
            val arr = JSONArray(json)
            return buildList {
                for (i in 0 until arr.length()) {
                    add(arr.getString(i))
                }
            }
        }
    }
}
