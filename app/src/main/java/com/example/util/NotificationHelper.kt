package com.example.util

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import com.example.data.model.OrderStage

object NotificationHelper {
    const val CHANNEL_ID = "quickkart_order_updates"
    private const val CHANNEL_NAME = "Order & Live Lock-Screen Progress"
    private const val CHANNEL_DESC = "Live lock-screen progress, countdown, and stage alerts for QuickKart orders"

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESC
                enableVibration(true)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            val manager = context.getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun computeStageProgressPercent(
        stage: OrderStage,
        remainingSeconds: Int,
        totalEtaMinutes: Int
    ): Int {
        if (stage == OrderStage.DELIVERED) return 100
        val totalSeconds = (totalEtaMinutes.coerceAtLeast(1) * 60).toFloat()
        val elapsedFraction = (1f - (remainingSeconds.coerceAtLeast(0).toFloat() / totalSeconds))
            .coerceIn(0f, 1f)

        return when (stage) {
            OrderStage.CONFIRMED -> (15 + (elapsedFraction * 15f)).toInt().coerceIn(15, 30)
            OrderStage.PACKING -> (35 + (elapsedFraction * 27f)).toInt().coerceIn(35, 62)
            OrderStage.OUT_FOR_DELIVERY -> (65 + (elapsedFraction * 31f)).toInt().coerceIn(65, 96)
            OrderStage.DELIVERED -> 100
        }
    }

    fun buildLockScreenStageLine(stage: OrderStage): String {
        val labels = listOf(
            OrderStage.CONFIRMED to "Confirmed",
            OrderStage.PACKING to "Packing",
            OrderStage.OUT_FOR_DELIVERY to "On Way",
            OrderStage.DELIVERED to "Delivered"
        )
        return labels.joinToString(" → ") { (entry, shortLabel) ->
            when {
                stage == OrderStage.DELIVERED || entry.stepIndex < stage.stepIndex -> "✓ $shortLabel"
                entry == stage -> "● $shortLabel"
                else -> "○ $shortLabel"
            }
        }
    }

    fun sendOrderStageNotification(
        context: Context,
        orderId: String,
        stage: OrderStage,
        etaMinutes: Int,
        remainingSeconds: Int = etaMinutes * 60,
        totalEtaMinutes: Int = etaMinutes,
        deliveryWindowLabel: String = "Express • $etaMinutes mins",
        silentUpdate: Boolean = false
    ): Boolean {
        if (!hasNotificationPermission(context)) return false
        ensureChannel(context)

        val isDelivered = stage == OrderStage.DELIVERED
        val progressPercent = computeStageProgressPercent(stage, remainingSeconds, totalEtaMinutes)
        val stageLine = buildLockScreenStageLine(stage)
        val remainingMins = ((remainingSeconds.coerceAtLeast(0) + 59) / 60).coerceAtLeast(1)

        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("navigate_to", "track")
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            orderId.hashCode(),
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = when (stage) {
            OrderStage.CONFIRMED -> "QuickKart • $orderId Confirmed"
            OrderStage.PACKING -> "Packing $orderId • ~$remainingMins min left"
            OrderStage.OUT_FOR_DELIVERY -> "Rider on the way • $remainingMins min"
            OrderStage.DELIVERED -> "Delivered! • $orderId"
        }

        val compactText = if (isDelivered) {
            "$deliveryWindowLabel • Order delivered safely"
        } else {
            "$stageLine ($progressPercent%)"
        }

        val expandedBody = if (isDelivered) {
            "$stageLine\n${stage.notificationBody}\nSlot: $deliveryWindowLabel"
        } else {
            "$stageLine\n$deliveryWindowLabel • $progressPercent% complete\n${stage.notificationBody}"
        }

        val largeIcon = runCatching {
            BitmapFactory.decodeResource(context.resources, R.drawable.ic_quickkart_icon)
        }.getOrNull()

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .apply { if (largeIcon != null) setLargeIcon(largeIcon) }
            .setContentTitle(title)
            .setContentText(compactText)
            .setSubText(if (isDelivered) "Completed" else "Live • $deliveryWindowLabel")
            .setStyle(NotificationCompat.BigTextStyle().bigText(expandedBody))
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setCategory(
                if (isDelivered) NotificationCompat.CATEGORY_STATUS else NotificationCompat.CATEGORY_PROGRESS
            )
            .setPriority(if (silentUpdate) NotificationCompat.PRIORITY_DEFAULT else NotificationCompat.PRIORITY_HIGH)
            .setOngoing(!isDelivered)
            .setAutoCancel(isDelivered)
            .setOnlyAlertOnce(silentUpdate)
            .setContentIntent(pendingIntent)
            .addAction(
                R.mipmap.ic_launcher_round,
                if (isDelivered) "Open Summary" else "View Live Map",
                pendingIntent
            )

        if (!isDelivered) {
            val etaTargetMillis = System.currentTimeMillis() + remainingSeconds.coerceAtLeast(0) * 1000L
            builder
                .setProgress(100, progressPercent, false)
                .setWhen(etaTargetMillis)
                .setShowWhen(true)
                .setUsesChronometer(true)
                .setChronometerCountDown(true)
        } else {
            builder
                .setProgress(0, 0, false)
                .setUsesChronometer(false)
        }

        return try {
            NotificationManagerCompat.from(context).notify(orderId.hashCode(), builder.build())
            true
        } catch (_: SecurityException) {
            false
        }
    }

    fun sendTestNotification(context: Context): Boolean {
        if (!hasNotificationPermission(context)) return false
        ensureChannel(context)

        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            9911,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val body = "Live lock-screen progress & countdown alerts are ready for your QuickKart orders."
        val largeIcon = runCatching {
            BitmapFactory.decodeResource(context.resources, R.drawable.ic_quickkart_icon)
        }.getOrNull()
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .apply { if (largeIcon != null) setLargeIcon(largeIcon) }
            .setContentTitle("QuickKart Live Alerts Ready ⚡")
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        return try {
            NotificationManagerCompat.from(context).notify(9911, notification)
            true
        } catch (_: SecurityException) {
            false
        }
    }
}
