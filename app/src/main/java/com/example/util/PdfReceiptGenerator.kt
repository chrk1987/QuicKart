package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.example.data.model.OrderRecord
import com.example.service.GeneratedReceiptResult
import com.example.service.PdfReceiptService

/**
 * Compatibility facade delegating to [PdfReceiptService].
 */
object PdfReceiptGenerator {

    suspend fun generateReceipt(context: Context, order: OrderRecord): GeneratedReceiptResult =
        PdfReceiptService(context).generateAndSaveReceipt(order).getOrThrow()

    fun openPdfIntent(uri: Uri): Intent = PdfReceiptService.openPdfIntent(uri)

    fun sharePdfIntent(uri: Uri, orderId: String): Intent =
        PdfReceiptService.sharePdfIntent(uri, orderId)
}
