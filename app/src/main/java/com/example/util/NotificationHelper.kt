package com.example.util

import android.Manifest
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
    private const val CHANNEL_NAME = "Order & Delivery Updates"
    private const val CHANNEL_DESC = "Live status updates for your 8-minute QuickKart orders"

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESC
                enableVibration(true)
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

    fun sendOrderStageNotification(
        context: Context,
        orderId: String,
        stage: OrderStage,
        etaMinutes: Int
    ): Boolean {
        if (!hasNotificationPermission(context)) return false
        ensureChannel(context)

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
            OrderStage.CONFIRMED -> "QuickKart • Order $orderId Confirmed"
            OrderStage.PACKING -> "Packing $orderId • ${etaMinutes}m ETA"
            OrderStage.OUT_FOR_DELIVERY -> "Rider on the way! • $orderId"
            OrderStage.DELIVERED -> "Delivered! • $orderId"
        }

        val largeIcon = runCatching {
            BitmapFactory.decodeResource(context.resources, R.drawable.ic_quickkart_icon)
        }.getOrNull()

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .apply { if (largeIcon != null) setLargeIcon(largeIcon) }
            .setContentTitle(title)
            .setContentText(stage.notificationBody)
            .setStyle(NotificationCompat.BigTextStyle().bigText(stage.notificationBody))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        return try {
            NotificationManagerCompat.from(context).notify(orderId.hashCode(), notification)
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

        val body = "Your 8-minute QuickKart alerts are active! You'll get live updates from Confirmed to Doorstep Delivery."
        val largeIcon = runCatching {
            BitmapFactory.decodeResource(context.resources, R.drawable.ic_quickkart_icon)
        }.getOrNull()
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .apply { if (largeIcon != null) setLargeIcon(largeIcon) }
            .setContentTitle("QuickKart Alerts Ready ⚡")
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
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
