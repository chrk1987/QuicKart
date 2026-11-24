package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.WalletTransaction

@Entity(tableName = "wallet_transactions")
data class WalletTransactionEntity(
    @PrimaryKey val id: String,
    val title: String,
    val subtitle: String,
    val amountInr: Int,
    val isCredit: Boolean,
    val createdAtMillis: Long
) {
    fun toDomain(): WalletTransaction = WalletTransaction(
        id = id,
        title = title,
        subtitle = subtitle,
        amountInr = amountInr,
        isCredit = isCredit,
        createdAtMillis = createdAtMillis
    )

    companion object {
        fun fromDomain(model: WalletTransaction): WalletTransactionEntity = WalletTransactionEntity(
            id = model.id,
            title = model.title,
            subtitle = model.subtitle,
            amountInr = model.amountInr,
            isCredit = model.isCredit,
            createdAtMillis = model.createdAtMillis
        )
    }
}
