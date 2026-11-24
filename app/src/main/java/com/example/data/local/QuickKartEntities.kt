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
    val isDefault: Boolean,
    val area: String = "",
    val city: String = "",
    val state: String = "",
    val postalCode: String = "",
    val country: String = "India",
    val latitude: Double? = null,
    val longitude: Double? = null
) {
    fun toDomain(): SavedAddress = SavedAddress(
        id = id,
        label = label,
        fullAddress = fullAddress,
        landmark = landmark,
        neighborhoodId = neighborhoodId,
        isDefault = isDefault,
        area = area,
        city = city,
        state = state,
        postalCode = postalCode,
        country = country,
        latitude = latitude,
        longitude = longitude
    )

    companion object {
        fun fromDomain(address: SavedAddress): SavedAddressEntity = SavedAddressEntity(
            id = address.id,
            label = address.label,
            fullAddress = address.fullAddress,
            landmark = address.landmark,
            neighborhoodId = address.neighborhoodId,
            isDefault = address.isDefault,
            area = address.area,
            city = address.city,
            state = address.state,
            postalCode = address.postalCode,
            country = address.country,
            latitude = address.latitude,
            longitude = address.longitude
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
    val feedbackComment: String,
    val deliveryWindowLabel: String = "Express • 8 mins"
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
            feedbackComment = feedbackComment,
            deliveryWindowLabel = deliveryWindowLabel
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
                feedbackComment = order.feedbackComment,
                deliveryWindowLabel = order.deliveryWindowLabel
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

@Entity(tableName = "support_tickets")
data class SupportTicketEntity(
    @PrimaryKey val ticketId: String,
    val orderId: String,
    val createdAtMillis: Long,
    val updatedAtMillis: Long,
    val categoryName: String,
    val affectedItemsSummary: String,
    val affectedAmountInr: Int,
    val preferredResolutionName: String,
    val resolutionSummary: String,
    val statusName: String,
    val photoUriString: String?,
    val messagesJson: String,
    val csatRating: Int?
) {
    fun toDomain(): com.example.data.model.SupportTicketRecord {
        val messages = mutableListOf<com.example.data.model.SupportMessage>()
        val msgArray = JSONArray(messagesJson)
        for (i in 0 until msgArray.length()) {
            val obj = msgArray.getJSONObject(i)
            messages.add(
                com.example.data.model.SupportMessage(
                    id = obj.getString("id"),
                    isUser = obj.getBoolean("isUser"),
                    text = obj.getString("text"),
                    timestampMillis = obj.getLong("timestampMillis")
                )
            )
        }
        val category = runCatching {
            com.example.data.model.SupportIssueCategory.valueOf(categoryName)
        }.getOrDefault(com.example.data.model.SupportIssueCategory.OTHER)
        val resolution = runCatching {
            com.example.data.model.SupportResolutionOption.valueOf(preferredResolutionName)
        }.getOrDefault(com.example.data.model.SupportResolutionOption.INSTANT_CREDIT)
        val status = runCatching {
            com.example.data.model.SupportTicketStatus.valueOf(statusName)
        }.getOrDefault(com.example.data.model.SupportTicketStatus.OPEN)

        return com.example.data.model.SupportTicketRecord(
            ticketId = ticketId,
            orderId = orderId,
            createdAtMillis = createdAtMillis,
            updatedAtMillis = updatedAtMillis,
            category = category,
            affectedItemsSummary = affectedItemsSummary,
            affectedAmountInr = affectedAmountInr,
            preferredResolution = resolution,
            resolutionSummary = resolutionSummary,
            status = status,
            photoUriString = photoUriString,
            messages = messages,
            csatRating = csatRating
        )
    }

    companion object {
        fun fromDomain(ticket: com.example.data.model.SupportTicketRecord): SupportTicketEntity {
            val msgArray = JSONArray()
            ticket.messages.forEach { msg ->
                msgArray.put(
                    JSONObject().apply {
                        put("id", msg.id)
                        put("isUser", msg.isUser)
                        put("text", msg.text)
                        put("timestampMillis", msg.timestampMillis)
                    }
                )
            }
            return SupportTicketEntity(
                ticketId = ticket.ticketId,
                orderId = ticket.orderId,
                createdAtMillis = ticket.createdAtMillis,
                updatedAtMillis = ticket.updatedAtMillis,
                categoryName = ticket.category.name,
                affectedItemsSummary = ticket.affectedItemsSummary,
                affectedAmountInr = ticket.affectedAmountInr,
                preferredResolutionName = ticket.preferredResolution.name,
                resolutionSummary = ticket.resolutionSummary,
                statusName = ticket.status.name,
                photoUriString = ticket.photoUriString,
                messagesJson = msgArray.toString(),
                csatRating = ticket.csatRating
            )
        }
    }
}

