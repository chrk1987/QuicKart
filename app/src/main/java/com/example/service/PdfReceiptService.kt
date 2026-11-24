package com.example.service

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Canvas
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
import com.example.data.model.OrderStage
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ReceiptLineSummary(
    val name: String,
    val variantLabel: String,
    val quantity: Int,
    val unitPriceInr: Int,
    val unitMrpInr: Int,
    val lineTotalInr: Int
)

data class ReceiptPricingSummary(
    val itemTotal: Int,
    val mrpSavings: Int,
    val deliveryFee: Int,
    val handlingCharge: Int,
    val promoDiscount: Int,
    val tipAmount: Int,
    val totalPaid: Int,
    val paymentMethodLabel: String
)

data class ReceiptDocumentData(
    val orderId: String,
    val formattedDate: String,
    val neighborhoodName: String,
    val stageTitle: String,
    val riderName: String,
    val riderPhone: String,
    val addressLabel: String,
    val addressFull: String,
    val deliveryNotes: String,
    val items: List<ReceiptLineSummary>,
    val pricing: ReceiptPricingSummary,
    val userRating: Int?,
    val praiseTags: List<String>,
    val feedbackComment: String,
    val etaMinutes: Int
)

data class GeneratedReceiptResult(
    val fileUri: Uri,
    val downloadsUri: Uri?,
    val fileName: String,
    val localFilePath: String,
    val savedToDownloads: Boolean
)

class PdfReceiptService(
    private val context: Context,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    suspend fun generateAndSaveReceipt(order: OrderRecord): Result<GeneratedReceiptResult> =
        withContext(ioDispatcher) {
            runCatching {
                val docData = buildDocumentData(order)
                val fileName = "QuickKart_${docData.orderId}.pdf"
                val receiptsDir = File(context.filesDir, "receipts").apply { mkdirs() }
                val localFile = File(receiptsDir, fileName)

                renderPdfToFile(docData, localFile)

                val downloadsUri = saveCopyToDownloadsIfSupported(localFile, fileName)
                val shareUri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    localFile
                )

                GeneratedReceiptResult(
                    fileUri = shareUri,
                    downloadsUri = downloadsUri,
                    fileName = fileName,
                    localFilePath = localFile.absolutePath,
                    savedToDownloads = downloadsUri != null
                )
            }
        }

    private fun renderPdfToFile(data: ReceiptDocumentData, targetFile: File) {
        val pdfDocument = PdfDocument()
        val pageWidth = 595
        val pageHeight = 842
        val left = 40f
        val right = pageWidth - 40f
        val maxContentBottom = pageHeight - 58f

        var pageNumber = 1
        var currentPage = pdfDocument.startPage(
            PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
        )
        var canvas: Canvas = currentPage.canvas
        var cursorY = 48f

        val bannerPaint = Paint().apply {
            color = Color.rgb(255, 212, 0)
            style = Paint.Style.FILL
        }
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
        val footPaint = Paint(mutedPaint).apply { textAlign = Paint.Align.CENTER }

        fun drawFooter(targetCanvas: Canvas, pageIdx: Int) {
            targetCanvas.drawText(
                "QuickKart • Delivered in ${data.etaMinutes} minutes • Page $pageIdx",
                pageWidth / 2f,
                pageHeight - 30f,
                footPaint
            )
        }

        fun startContinuationPage() {
            drawFooter(canvas, pageNumber)
            pdfDocument.finishPage(currentPage)
            pageNumber += 1
            currentPage = pdfDocument.startPage(
                PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
            )
            canvas = currentPage.canvas
            cursorY = 48f
            canvas.drawText(
                "QuickKart Receipt • ${data.orderId} (Continued)",
                left,
                cursorY,
                sectionPaint
            )
            cursorY += 12f
            canvas.drawLine(left, cursorY, right, cursorY, linePaint)
            cursorY += 20f
        }

        fun ensureSpace(neededHeight: Float, onNewPage: (() -> Unit)? = null) {
            if (cursorY + neededHeight > maxContentBottom) {
                startContinuationPage()
                onNewPage?.invoke()
            }
        }

        try {
            // Top Branded Header Banner
            canvas.drawRoundRect(left, cursorY - 22f, right, cursorY + 46f, 12f, 12f, bannerPaint)
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
            canvas.drawText(data.orderId, right - 14f, cursorY + 6f, rightTitlePaint)
            canvas.drawText("COMPLETED ORDER RECEIPT", right - 14f, cursorY + 24f, rightSubPaint)

            cursorY += 72f

            // Metadata Section
            canvas.drawText("ORDER METADATA", left, cursorY, sectionPaint)
            cursorY += 18f
            canvas.drawText("Date: ${data.formattedDate}", left, cursorY, textPaint)
            canvas.drawText("Area: ${data.neighborhoodName}", right - 210f, cursorY, textPaint)
            cursorY += 16f
            canvas.drawText("Status: ${data.stageTitle}", left, cursorY, textPaint)
            canvas.drawText("Paid via: ${data.pricing.paymentMethodLabel}", right - 210f, cursorY, textPaint)
            cursorY += 16f
            canvas.drawText("Courier: ${data.riderName} (${data.riderPhone})", left, cursorY, textPaint)
            cursorY += 18f

            canvas.drawLine(left, cursorY, right, cursorY, linePaint)
            cursorY += 20f

            // Delivery Address & Rider Instructions
            canvas.drawText("DELIVER TO (${data.addressLabel.uppercase(Locale.US)})", left, cursorY, sectionPaint)
            cursorY += 16f
            splitLines(data.addressFull, textPaint, right - left).forEach { line ->
                ensureSpace(16f)
                canvas.drawText(line, left, cursorY, textPaint)
                cursorY += 15f
            }

            if (data.deliveryNotes.isNotBlank()) {
                splitLines("Rider Note: ${data.deliveryNotes}", mutedPaint, right - left).forEach { line ->
                    ensureSpace(15f)
                    canvas.drawText(line, left, cursorY, mutedPaint)
                    cursorY += 14f
                }
            }

            cursorY += 10f
            canvas.drawLine(left, cursorY, right, cursorY, linePaint)
            cursorY += 22f

            // Itemized Table Header
            val colPack = left + 225f
            val colQty = left + 330f
            val colRate = left + 400f
            val headerBg = Paint().apply { color = Color.rgb(243, 244, 246) }
            val colHeadLeft = Paint(sectionPaint).apply { textSize = 10.5f }
            val colHeadRight = Paint(colHeadLeft).apply { textAlign = Paint.Align.RIGHT }

            fun drawTableHeader() {
                canvas.drawRect(left, cursorY - 14f, right, cursorY + 8f, headerBg)
                canvas.drawText("ITEM", left + 4f, cursorY, colHeadLeft)
                canvas.drawText("PACK", colPack, cursorY, colHeadLeft)
                canvas.drawText("QTY", colQty, cursorY, colHeadRight)
                canvas.drawText("PRICE", colRate, cursorY, colHeadRight)
                canvas.drawText("TOTAL", right - 4f, cursorY, colHeadRight)
                cursorY += 22f
            }

            ensureSpace(44f)
            drawTableHeader()

            data.items.forEach { item ->
                ensureSpace(22f, onNewPage = { drawTableHeader() })
                val label = if (item.name.length > 32) item.name.take(30) + "…" else item.name
                canvas.drawText(label, left + 4f, cursorY, textPaint)
                canvas.drawText(item.variantLabel, colPack, cursorY, mutedPaint)
                canvas.drawText(item.quantity.toString(), colQty, cursorY, rightTextPaint)
                canvas.drawText("₹${item.unitPriceInr}", colRate, cursorY, rightTextPaint)
                canvas.drawText("₹${item.lineTotalInr}", right - 4f, cursorY, rightTextPaint)
                cursorY += 19f
            }

            cursorY += 6f
            canvas.drawLine(left, cursorY, right, cursorY, linePaint)
            cursorY += 20f

            // Total Pricing & Breakdown
            ensureSpace(155f)
            canvas.drawText("PAYMENT BREAKDOWN", left, cursorY, sectionPaint)
            cursorY += 18f

            fun drawPricingRow(label: String, amountText: String, highlightGreen: Boolean = false) {
                val valPaint = Paint(rightTextPaint).apply {
                    if (highlightGreen) {
                        color = Color.rgb(0, 110, 47)
                        typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                    }
                }
                canvas.drawText(label, left, cursorY, textPaint)
                canvas.drawText(amountText, right, cursorY, valPaint)
                cursorY += 17f
            }

            val pricing = data.pricing
            drawPricingRow("Item Subtotal", "₹${pricing.itemTotal}")
            if (pricing.mrpSavings > 0) {
                drawPricingRow("MRP Savings", "-₹${pricing.mrpSavings}", highlightGreen = true)
            }
            drawPricingRow(
                "Delivery Fee",
                if (pricing.deliveryFee == 0) "FREE" else "₹${pricing.deliveryFee}",
                highlightGreen = pricing.deliveryFee == 0
            )
            drawPricingRow("Handling & Packaging", "₹${pricing.handlingCharge}")
            if (pricing.promoDiscount > 0) {
                drawPricingRow("Promo Discount", "-₹${pricing.promoDiscount}", highlightGreen = true)
            }
            if (pricing.tipAmount > 0) {
                drawPricingRow("Rider Tip", "₹${pricing.tipAmount}")
            }

            cursorY += 6f
            canvas.drawLine(left, cursorY, right, cursorY, linePaint)
            cursorY += 22f

            val grandLeft = Paint(sectionPaint).apply { textSize = 14.5f }
            val grandRight = Paint(grandLeft).apply { textAlign = Paint.Align.RIGHT }
            canvas.drawText("TOTAL PAID", left, cursorY, grandLeft)
            canvas.drawText("₹${pricing.totalPaid}", right, cursorY, grandRight)
            cursorY += 26f

            if (data.userRating != null) {
                ensureSpace(64f)
                canvas.drawLine(left, cursorY, right, cursorY, linePaint)
                cursorY += 20f
                canvas.drawText("CUSTOMER RATING", left, cursorY, sectionPaint)
                cursorY += 16f
                canvas.drawText("Score: ${data.userRating} / 5 Stars", left, cursorY, textPaint)
                cursorY += 15f
                if (data.praiseTags.isNotEmpty()) {
                    canvas.drawText("Tags: ${data.praiseTags.joinToString(", ")}", left, cursorY, mutedPaint)
                    cursorY += 15f
                }
                if (data.feedbackComment.isNotBlank()) {
                    splitLines("Review: ${data.feedbackComment}", textPaint, right - left).forEach {
                        ensureSpace(15f)
                        canvas.drawText(it, left, cursorY, textPaint)
                        cursorY += 14f
                    }
                }
            }

            drawFooter(canvas, pageNumber)
            pdfDocument.finishPage(currentPage)

            FileOutputStream(targetFile).use { out ->
                pdfDocument.writeTo(out)
            }
        } finally {
            pdfDocument.close()
        }
    }

    private fun saveCopyToDownloadsIfSupported(sourceFile: File, fileName: String): Uri? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return null
        return runCatching {
            val resolver = context.contentResolver
            val values = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                put(MediaStore.Downloads.MIME_TYPE, "application/pdf")
                put(MediaStore.Downloads.RELATIVE_PATH, "${Environment.DIRECTORY_DOWNLOADS}/QuickKart")
                put(MediaStore.Downloads.IS_PENDING, 1)
            }
            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                ?: return null
            resolver.openOutputStream(uri)?.use { out ->
                sourceFile.inputStream().use { input -> input.copyTo(out) }
            }
            val publishValues = ContentValues().apply {
                put(MediaStore.Downloads.IS_PENDING, 0)
            }
            resolver.update(uri, publishValues, null, null)
            uri
        }.getOrNull()
    }

    companion object {
        fun canGenerateReceipt(order: OrderRecord): Boolean = order.stage == OrderStage.DELIVERED

        fun buildDocumentData(order: OrderRecord): ReceiptDocumentData {
            require(canGenerateReceipt(order)) {
                "PDF receipts can only be generated for completed orders."
            }
            val dateText = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
                .format(Date(order.createdAtMillis))

            val deliveryNotes = buildList {
                if (order.deliveryInstructions.isNotEmpty()) {
                    add(order.deliveryInstructions.joinToString(" • "))
                }
                if (order.riderNote.isNotBlank()) {
                    add(order.riderNote)
                }
            }.joinToString(" — ")

            val lineSummaries = order.items.map { item ->
                ReceiptLineSummary(
                    name = item.productName,
                    variantLabel = item.variantLabel,
                    quantity = item.quantity,
                    unitPriceInr = item.unitPriceInr,
                    unitMrpInr = item.unitMrpInr,
                    lineTotalInr = item.lineTotal
                )
            }

            val pricingSummary = ReceiptPricingSummary(
                itemTotal = order.itemTotal,
                mrpSavings = order.mrpSavings,
                deliveryFee = order.deliveryFee,
                handlingCharge = order.handlingCharge,
                promoDiscount = order.promoDiscount,
                tipAmount = order.tipAmount,
                totalPaid = order.totalPaid,
                paymentMethodLabel = order.paymentMethod.label
            )

            return ReceiptDocumentData(
                orderId = order.orderId,
                formattedDate = dateText,
                neighborhoodName = order.neighborhoodName,
                stageTitle = order.stage.title,
                riderName = order.riderName,
                riderPhone = order.riderPhone,
                addressLabel = order.deliveryAddressLabel,
                addressFull = order.deliveryAddressFull,
                deliveryNotes = deliveryNotes,
                items = lineSummaries,
                pricing = pricingSummary,
                userRating = order.userRating,
                praiseTags = order.praiseTags,
                feedbackComment = order.feedbackComment,
                etaMinutes = order.etaMinutes
            )
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
}
