package com.example.rider.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.rider.MainActivity
import com.example.rider.R
import com.example.rider.data.model.RiderTripOrder
import com.example.rider.data.model.RiderTripStage

object RiderNotificationHelper {
    private const val CHANNEL_DISPATCH_ID = "quickkart_rider_dispatch"
    private const val CHANNEL_TRIP_ID = "quickkart_rider_trip"
    private const val DISPATCH_NOTIFICATION_ID = 4101
    private const val ONGOING_TRIP_NOTIFICATION_ID = 4102

    fun ensureChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                ?: return
            val dispatchChannel = NotificationChannel(
                CHANNEL_DISPATCH_ID,
                "Order Dispatch Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "High-priority alerts for incoming QuickKart delivery offers."
                enableVibration(true)
            }
            val tripChannel = NotificationChannel(
                CHANNEL_TRIP_ID,
                "Ongoing Delivery Progress",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Lock-screen live progress for your active QuickKart trip."
                setShowBadge(false)
            }
            manager.createNotificationChannel(dispatchChannel)
            manager.createNotificationChannel(tripChannel)
        }
    }

    private fun canPost(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun showIncomingOfferAlert(context: Context, offer: RiderTripOrder) {
        if (!canPost(context)) return
        ensureChannels(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            101,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_DISPATCH_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("New Order • Earn ₹${offer.totalRiderPayoutInr}")
            .setContentText("${offer.hubName} → ${offer.deliveryAddressLabel} (${offer.distanceKm} km • ${offer.totalUnits} items)")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        runCatching {
            NotificationManagerCompat.from(context).notify(DISPATCH_NOTIFICATION_ID, notification)
        }
    }

    fun updateOngoingTripNotification(context: Context, trip: RiderTripOrder?) {
        if (!canPost(context)) return
        ensureChannels(context)

        if (trip == null || !trip.stage.isActiveRun()) {
            runCatching {
                NotificationManagerCompat.from(context).cancel(ONGOING_TRIP_NOTIFICATION_ID)
            }
            return
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("navigate_to", "active_trip")
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            102,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val subtitle = when (trip.stage) {
            RiderTripStage.HEADING_TO_HUB -> "Pickup at ${trip.hubName} • Bay ${trip.pickupBayCode}"
            RiderTripStage.PICKING_AT_POD -> "Scan ${trip.totalUnits} items • Seal ${trip.bagSealCode}"
            RiderTripStage.OUT_FOR_DELIVERY -> "Delivering to ${trip.customerName} (${trip.distanceKm} km)"
            RiderTripStage.ARRIVED_AT_DOOR -> "Verify PIN with ${trip.customerName} • ₹${trip.totalRiderPayoutInr}"
            else -> trip.stage.shortActionHint
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_TRIP_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("${trip.orderId} • ${trip.stage.label}")
            .setContentText(subtitle)
            .setOngoing(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
            .setProgress(4, trip.stage.stepIndex.coerceIn(1, 4), false)
            .setContentIntent(pendingIntent)
            .build()

        runCatching {
            NotificationManagerCompat.from(context).notify(ONGOING_TRIP_NOTIFICATION_ID, notification)
        }
    }
}
