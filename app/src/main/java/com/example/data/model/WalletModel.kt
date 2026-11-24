package com.example.data.model

data class WalletTransaction(
    val id: String,
    val title: String,
    val subtitle: String,
    val amountInr: Int,
    val isCredit: Boolean,
    val createdAtMillis: Long
)
