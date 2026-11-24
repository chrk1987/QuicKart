package com.example.util

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import com.example.R
import com.example.data.model.OrderRecord
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class GeneratedReceiptResult(
    val fileUri: Uri,
    val fileName: String,
    val savedToDownloads: Boolean
)

object PdfReceiptGenerator {

    fun generateReceipt(context: Context, order: OrderRecord): GeneratedReceiptResult {
        val pdfDocument = PdfDocument()
        val width = 595
        val height = 842
        val page = pdfDocument.startPage(PdfDocument.PageInfo.Builder(width, height, 1).create())
        val canvas = page.canvas

        val left = 40f
        val right = width - 40f
        var cursorY = 48f

        val bannerPaint = Paint().apply {
            color = Color.rgb(255, 212, 0)
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(left, cursorY - 22f, right, cursorY + 46f, 12f, 12f, bannerPaint)

        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(20, 20, 20)
            textSize = 22f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }
        val subPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(20, 20, 20)
            textSize = 11f
        }
        val rightTitlePaint = Paint(titlePaint).apply {
            textSize = 14f
            textAlign = Paint.Align.RIGHT
        }
        val rightSubPaint = Paint(subPaint).apply {
            textAlign = Paint.Align.RIGHT
        }

        val logoBitmap = runCatching {
            BitmapFactory.decodeResource(context.resources, R.drawable.ic_quickkart_icon)
        }.getOrNull()
        val textStartX = if (logoBitmap != null) {
            val logoRect = RectF(left + 12f, cursorY - 10f, left + 54f, cursorY + 32f)
            canvas.drawBitmap(logoBitmap, null, logoRect, Paint(Paint.FILTER_BITMAP_FLAG))
            logoBitmap.recycle()
            left + 64f
        } else {
            left + 14f
        }

        canvas.drawText("QuickKart", textStartX, cursorY + 6f, titlePaint)
        canvas.drawText("8-Min Hyperlocal Grocery Receipt", textStartX, cursorY + 24f, subPaint)
        canvas.drawText(order.orderId, right - 14f, cursorY + 6f, rightTitlePaint)
        canvas.drawText("ORDER RECEIPT", right - 14f, cursorY + 24f, rightSubPaint)

        cursorY += 72f

        val sectionPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(20, 20, 20)
            textSize = 12.5f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(55, 65, 81)
            textSize = 11f
        }
        val mutedPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(107, 114, 128)
            textSize = 10f
        }
        val linePaint = Paint().apply {
            color = Color.rgb(229, 231, 235)
            strokeWidth = 1f
        }
        val rightTextPaint = Paint(textPaint).apply { textAlign = Paint.Align.RIGHT }

        val dateText = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
            .format(Date(order.createdAtMillis))

        canvas.drawText("ORDER METADATA", left, cursorY, sectionPaint)
        cursorY += 18f
        canvas.drawText("Date: $dateText", left, cursorY, textPaint)
        canvas.drawText("Area: ${order.neighborhoodName}", right - 210f, cursorY, textPaint)
        cursorY += 16f
        canvas.drawText("Stage: ${order.stage.title}", left, cursorY, textPaint)
        canvas.drawText("Paid via: ${order.paymentMethod.label}", right - 210f, cursorY, textPaint)
        cursorY += 16f
        canvas.drawText("Courier: ${order.riderName} (${order.riderPhone})", left, cursorY, textPaint)
        cursorY += 18f

        canvas.drawLine(left, cursorY, right, cursorY, linePaint)
        cursorY += 20f

        canvas.drawText("DELIVER TO (${order.deliveryAddressLabel.uppercase()})", left, cursorY, sectionPaint)
        cursorY += 16f
        splitLines(order.deliveryAddressFull, textPaint, right - left).forEach { line ->
            canvas.drawText(line, left, cursorY, textPaint)
            cursorY += 15f
        }

        val instructionsCombined = buildList {
            if (order.deliveryInstructions.isNotEmpty()) {
                add(order.deliveryInstructions.joinToString(" • "))
            }
            if (order.riderNote.isNotBlank()) {
                add(order.riderNote)
            }
        }.joinToString(" — ")

        if (instructionsCombined.isNotBlank()) {
            splitLines("Rider Note: $instructionsCombined", mutedPaint, right - left).forEach { line ->
                canvas.drawText(line, left, cursorY, mutedPaint)
                cursorY += 14f
            }
        }

        cursorY += 10f
        canvas.drawLine(left, cursorY, right, cursorY, linePaint)
        cursorY += 22f

        val colPack = left + 225f
        val colQty = left + 330f
        val colRate = left + 400f
        val headerBg = Paint().apply { color = Color.rgb(243, 244, 246) }
        canvas.drawRect(left, cursorY - 14f, right, cursorY + 8f, headerBg)

        val colHeadLeft = Paint(sectionPaint).apply { textSize = 10.5f }
        val colHeadRight = Paint(colHeadLeft).apply { textAlign = Paint.Align.RIGHT }

        canvas.drawText("ITEM", left + 4f, cursorY, colHeadLeft)
        canvas.drawText("PACK", colPack, cursorY, colHeadLeft)
        canvas.drawText("QTY", colQty, cursorY, colHeadRight)
        canvas.drawText("PRICE", colRate, cursorY, colHeadRight)
        canvas.drawText("TOTAL", right - 4f, cursorY, colHeadRight)
        cursorY += 22f

        order.items.forEach { item ->
            val label = if (item.productName.length > 32) item.productName.take(30) + "…" else item.productName
            canvas.drawText(label, left + 4f, cursorY, textPaint)
            canvas.drawText(item.variantLabel, colPack, cursorY, mutedPaint)
            canvas.drawText(item.quantity.toString(), colQty, cursorY, rightTextPaint)
            canvas.drawText("₹${item.unitPriceInr}", colRate, cursorY, rightTextPaint)
            canvas.drawText("₹${item.lineTotal}", right - 4f, cursorY, rightTextPaint)
            cursorY += 19f
        }

        cursorY += 6f
        canvas.drawLine(left, cursorY, right, cursorY, linePaint)
        cursorY += 20f

        canvas.drawText("PAYMENT BREAKDOWN", left, cursorY, sectionPaint)
        cursorY += 18f

        fun drawRow(label: String, amountText: String, green: Boolean = false) {
            val valPaint = Paint(rightTextPaint).apply {
                if (green) {
                    color = Color.rgb(0, 110, 47)
                    typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                }
            }
            canvas.drawText(label, left, cursorY, textPaint)
            canvas.drawText(amountText, right, cursorY, valPaint)
            cursorY += 17f
        }

        drawRow("Item Subtotal", "₹${order.itemTotal}")
        if (order.mrpSavings > 0) drawRow("MRP Savings", "-₹${order.mrpSavings}", green = true)
        drawRow("Delivery Fee", if (order.deliveryFee == 0) "FREE" else "₹${order.deliveryFee}", green = order.deliveryFee == 0)
        drawRow("Handling & Packaging", "₹${order.handlingCharge}")
        if (order.promoDiscount > 0) drawRow("Promo Discount", "-₹${order.promoDiscount}", green = true)
        if (order.tipAmount > 0) drawRow("Rider Tip", "₹${order.tipAmount}")

        cursorY += 6f
        canvas.drawLine(left, cursorY, right, cursorY, linePaint)
        cursorY += 22f

        val grandLeft = Paint(sectionPaint).apply { textSize = 14.5f }
        val grandRight = Paint(grandLeft).apply { textAlign = Paint.Align.RIGHT }
        canvas.drawText("TOTAL PAID", left, cursorY, grandLeft)
        canvas.drawText("₹${order.totalPaid}", right, cursorY, grandRight)
        cursorY += 26f

        if (order.userRating != null) {
            canvas.drawLine(left, cursorY, right, cursorY, linePaint)
            cursorY += 20f
            canvas.drawText("CUSTOMER RATING", left, cursorY, sectionPaint)
            cursorY += 16f
            canvas.drawText("Score: ${order.userRating} / 5 Stars", left, cursorY, textPaint)
            cursorY += 15f
            if (order.praiseTags.isNotEmpty()) {
                canvas.drawText("Tags: ${order.praiseTags.joinToString(", ")}", left, cursorY, mutedPaint)
                cursorY += 15f
            }
            if (order.feedbackComment.isNotBlank()) {
                splitLines("Review: ${order.feedbackComment}", textPaint, right - left).forEach {
                    canvas.drawText(it, left, cursorY, textPaint)
                    cursorY += 14f
                }
            }
        }

        val footPaint = Paint(mutedPaint).apply { textAlign = Paint.Align.CENTER }
        canvas.drawText(
            "QuickKart • Delivered in ${order.etaMinutes} minutes",
            width / 2f,
            height - 34f,
            footPaint
        )

        pdfDocument.finishPage(page)

        val fileName = "QuickKart_${order.orderId}.pdf"
        val dir = File(context.filesDir, "receipts").apply { mkdirs() }
        val localFile = File(dir, fileName)
        FileOutputStream(localFile).use { pdfDocument.writeTo(it) }

        var savedDownloads = false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            runCatching {
                val values = ContentValues().apply {
                    put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                    put(MediaStore.Downloads.MIME_TYPE, "application/pdf")
                    put(MediaStore.Downloads.RELATIVE_PATH, "${Environment.DIRECTORY_DOWNLOADS}/QuickKart")
                }
                val uri = context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                if (uri != null) {
                    context.contentResolver.openOutputStream(uri)?.use { out ->
                        localFile.inputStream().use { input -> input.copyTo(out) }
                    }
                    savedDownloads = true
                }
            }
        }
        pdfDocument.close()

        val shareUri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", localFile)
        return GeneratedReceiptResult(shareUri, fileName, savedDownloads)
    }

    fun openPdfIntent(uri: Uri): Intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, "application/pdf")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    fun sharePdfIntent(uri: Uri, orderId: String): Intent {
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "QuickKart Receipt $orderId")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        return Intent.createChooser(send, "Share or Save PDF Receipt")
    }

    private fun splitLines(text: String, paint: Paint, maxWidth: Float): List<String> {
        val words = text.split(" ")
        val lines = mutableListOf<String>()
        var current = ""
        for (word in words) {
            val candidate = if (current.isEmpty()) word else "$current $word"
            if (paint.measureText(candidate) <= maxWidth) {
                current = candidate
            } else {
                if (current.isNotEmpty()) lines.add(current)
                current = word
            }
        }
        if (current.isNotEmpty()) lines.add(current)
        return lines
    }
}
