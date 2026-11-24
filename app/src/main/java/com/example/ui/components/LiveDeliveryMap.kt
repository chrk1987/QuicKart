package com.example.ui.components

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.location.Location
import android.net.Uri
import android.os.Looper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeliveryDining
import androidx.compose.material.icons.rounded.Fullscreen
import androidx.compose.material.icons.rounded.GpsFixed
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.material.icons.rounded.Navigation
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.data.model.OrderRecord
import com.example.data.model.OrderStage
import com.example.ui.theme.DeviceSizeClass
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.QuickKartMetricsTokens
import com.example.ui.theme.QuickKartThemeTokens
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint as OsmGeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.TilesOverlay
import java.io.File
import java.util.Locale
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

private data class NeighborhoodStreetRoute(
    val hubName: String,
    val streetAxisLabel: String,
    val waypoints: List<OsmGeoPoint>
)

private fun routeForOrder(order: OrderRecord): NeighborhoodStreetRoute {
    val n = order.neighborhoodName.lowercase()
    return when {
        "koramangala" in n -> NeighborhoodStreetRoute(
            hubName = "4th Block Pod #11",
            streetAxisLabel = "80 Feet Rd → 4th Block → Sony Signal",
            waypoints = listOf(
                OsmGeoPoint(12.9352, 77.6245),
                OsmGeoPoint(12.9336, 77.6256),
                OsmGeoPoint(12.9315, 77.6264),
                OsmGeoPoint(12.9296, 77.6268),
                OsmGeoPoint(12.9279, 77.6271)
            )
        )
        "bandra" in n -> NeighborhoodStreetRoute(
            hubName = "Pali Hill Pod #07",
            streetAxisLabel = "Pali Hill Rd → Dr Ambedkar Rd → 16th Rd",
            waypoints = listOf(
                OsmGeoPoint(19.0607, 72.8270),
                OsmGeoPoint(19.0592, 72.8286),
                OsmGeoPoint(19.0573, 72.8301),
                OsmGeoPoint(19.0558, 72.8315),
                OsmGeoPoint(19.0544, 72.8322)
            )
        )
        "hauz khas" in n -> NeighborhoodStreetRoute(
            hubName = "Aurobindo Pod #02",
            streetAxisLabel = "Sri Aurobindo Marg → Hauz Khas Enclave",
            waypoints = listOf(
                OsmGeoPoint(28.5494, 77.2001),
                OsmGeoPoint(28.5512, 77.2018),
                OsmGeoPoint(28.5528, 77.2039),
                OsmGeoPoint(28.5539, 77.2054),
                OsmGeoPoint(28.5546, 77.2066)
            )
        )
        "jubilee" in n -> NeighborhoodStreetRoute(
            hubName = "Road No. 36 Pod #09",
            streetAxisLabel = "Road No. 36 → Jubilee Hills Checkpost",
            waypoints = listOf(
                OsmGeoPoint(17.4326, 78.4071),
                OsmGeoPoint(17.4307, 78.4090),
                OsmGeoPoint(17.4288, 78.4112),
                OsmGeoPoint(17.4272, 78.4129),
                OsmGeoPoint(17.4258, 78.4142)
            )
        )
        else -> NeighborhoodStreetRoute(
            hubName = "100ft Rd Pod #04",
            streetAxisLabel = "100 Feet Rd → CMH Rd → 12th Main Rd",
            waypoints = listOf(
                OsmGeoPoint(12.9784, 77.6408),
                OsmGeoPoint(12.9765, 77.6409),
                OsmGeoPoint(12.9749, 77.6426),
                OsmGeoPoint(12.9732, 77.6448),
                OsmGeoPoint(12.9716, 77.6466)
            )
        )
    }
}

private fun calculateDistanceMeters(a: OsmGeoPoint, b: OsmGeoPoint): Int {
    val results = FloatArray(1)
    Location.distanceBetween(a.latitude, a.longitude, b.latitude, b.longitude, results)
    return results[0].roundToInt().coerceAtLeast(0)
}

private fun computeBearingDegrees(from: OsmGeoPoint, to: OsmGeoPoint): Float {
    val lat1 = Math.toRadians(from.latitude)
    val lon1 = Math.toRadians(from.longitude)
    val lat2 = Math.toRadians(to.latitude)
    val lon2 = Math.toRadians(to.longitude)
    val dLon = lon2 - lon1
    val y = sin(dLon) * cos(lat2)
    val x = cos(lat1) * sin(lat2) - sin(lat1) * cos(lat2) * cos(dLon)
    return ((Math.toDegrees(atan2(y, x)) + 360.0) % 360.0).toFloat()
}

private data class PolylineSplit(
    val coveredPoints: List<OsmGeoPoint>,
    val riderPoint: OsmGeoPoint,
    val aheadPoint: OsmGeoPoint
)

private fun splitPolylineAtFraction(points: List<OsmGeoPoint>, fraction: Float): PolylineSplit {
    if (points.isEmpty()) {
        val fallback = OsmGeoPoint(12.9716, 77.6466)
        return PolylineSplit(emptyList(), fallback, fallback)
    }
    if (points.size == 1) {
        return PolylineSplit(points, points.first(), points.first())
    }

    val clamped = fraction.coerceIn(0f, 1f)
    if (clamped <= 0f) return PolylineSplit(listOf(points.first()), points.first(), points[1])
    if (clamped >= 1f) return PolylineSplit(points, points.last(), points.last())

    val segLengths = FloatArray(points.size - 1)
    var totalLen = 0f
    for (i in 0 until points.size - 1) {
        val len = calculateDistanceMeters(points[i], points[i + 1]).toFloat().coerceAtLeast(1f)
        segLengths[i] = len
        totalLen += len
    }

    val targetDist = totalLen * clamped
    var accum = 0f
    val covered = mutableListOf(points.first())

    for (i in segLengths.indices) {
        val segLen = segLengths[i]
        if (accum + segLen >= targetDist) {
            val localFrac = ((targetDist - accum) / segLen).coerceIn(0f, 1f).toDouble()
            val a = points[i]
            val b = points[i + 1]
            val interp = OsmGeoPoint(
                a.latitude + (b.latitude - a.latitude) * localFrac,
                a.longitude + (b.longitude - a.longitude) * localFrac
            )
            covered.add(interp)
            return PolylineSplit(covered, interp, b)
        } else {
            accum += segLen
            covered.add(points[i + 1])
        }
    }
    return PolylineSplit(points, points.last(), points.last())
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InteractiveDeliveryRouteMap(
    order: OrderRecord,
    onPinInfo: (String) -> Unit
) {
    val tokens = QuickKartThemeTokens.colors
    val metrics = QuickKartMetricsTokens.metrics
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val geoRoute = remember(order.orderId, order.neighborhoodName) { routeForOrder(order) }
    var liveDropoffPoint by remember(order.orderId) { mutableStateOf<OsmGeoPoint?>(null) }
    var isLiveGpsTracking by remember(order.orderId) { mutableStateOf(false) }
    var showFullScreenMap by remember { mutableStateOf(false) }
    var recenterTrigger by remember { mutableIntStateOf(0) }

    val fullWaypoints = remember(geoRoute, liveDropoffPoint) {
        val drop = liveDropoffPoint
        if (drop != null) {
            geoRoute.waypoints.dropLast(1) + drop
        } else {
            geoRoute.waypoints
        }
    }
    val hubPoint = fullWaypoints.first()
    val activeDropoff = fullWaypoints.last()

    // Continuous rider progress along real street waypoints tied to stage + countdown timer
    val totalStageSeconds = (order.etaMinutes * 60).coerceAtLeast(60)
    val elapsedFraction = (1f - (order.remainingSeconds.toFloat() / totalStageSeconds.toFloat()))
        .coerceIn(0f, 1f)
    val targetProgress = when (order.stage) {
        OrderStage.CONFIRMED -> 0.05f
        OrderStage.PACKING -> 0.14f
        OrderStage.OUT_FOR_DELIVERY -> (0.20f + elapsedFraction * 0.76f).coerceIn(0.20f, 0.96f)
        OrderStage.DELIVERED -> 1.0f
    }
    val riderProgress by animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = tween(750, easing = FastOutSlowInEasing),
        label = "rider_route_progress"
    )

    val polySplit = splitPolylineAtFraction(fullWaypoints, riderProgress)
    val riderGeo = polySplit.riderPoint
    val riderHeading = remember(riderGeo, polySplit.aheadPoint, order.stage) {
        if (order.stage == OrderStage.DELIVERED) 0f
        else computeBearingDegrees(riderGeo, polySplit.aheadPoint)
    }

    val remainingMeters = if (order.stage == OrderStage.DELIVERED) {
        0
    } else {
        calculateDistanceMeters(riderGeo, activeDropoff)
    }
    val remainingDistanceLabel = when {
        remainingMeters == 0 -> "Arrived"
        remainingMeters >= 1000 -> String.format(Locale.US, "%.1f km away", remainingMeters / 1000f)
        else -> "$remainingMeters m away"
    }
    val speedLabel = when (order.stage) {
        OrderStage.CONFIRMED -> "Queued at Pod"
        OrderStage.PACKING -> "Packing Bag"
        OrderStage.OUT_FOR_DELIVERY -> "24 km/h • En route"
        OrderStage.DELIVERED -> "Delivered"
    }
    val etaMins = ((order.remainingSeconds + 59) / 60).coerceAtLeast(1)
    val riderBadgeLabel = when (order.stage) {
        OrderStage.CONFIRMED -> "At Pod"
        OrderStage.PACKING -> "Packing"
        OrderStage.OUT_FOR_DELIVERY -> "${etaMins}m away"
        OrderStage.DELIVERED -> "Delivered"
    }

    var selectedPinLabel by remember(order.stage, liveDropoffPoint) {
        mutableStateOf(
            when (order.stage) {
                OrderStage.CONFIRMED -> "${geoRoute.hubName}: Picking items for your order"
                OrderStage.PACKING -> "${geoRoute.hubName}: Sealed in temperature-controlled bag"
                OrderStage.OUT_FOR_DELIVERY ->
                    "${order.riderName} is $remainingDistanceLabel via ${geoRoute.streetAxisLabel}"
                OrderStage.DELIVERED ->
                    "Delivered safely to ${order.deliveryAddressLabel}"
            }
        )
    }

    // Continuous FusedLocationProviderClient updates when Live GPS toggle is active
    val fusedClient = remember(context) { LocationServices.getFusedLocationProviderClient(context) }
    val locationCallback = remember {
        object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val loc = result.lastLocation ?: return
                liveDropoffPoint = OsmGeoPoint(loc.latitude, loc.longitude)
            }
        }
    }

    DisposableEffect(isLiveGpsTracking, lifecycleOwner) {
        var started = false
        fun hasLocationPerm(): Boolean {
            val fine = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
            val coarse = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
            return fine || coarse
        }

        @SuppressLint("MissingPermission")
        fun startUpdates() {
            if (isLiveGpsTracking && hasLocationPerm() && !started) {
                val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000L)
                    .setMinUpdateIntervalMillis(2500L)
                    .build()
                fusedClient.requestLocationUpdates(request, locationCallback, Looper.getMainLooper())
                started = true
            }
        }

        fun stopUpdates() {
            if (started) {
                fusedClient.removeLocationUpdates(locationCallback)
                started = false
            }
        }

        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> startUpdates()
                Lifecycle.Event.ON_PAUSE -> stopUpdates()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        if (isLiveGpsTracking) startUpdates()

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            stopUpdates()
        }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        val granted = grants[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            grants[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            isLiveGpsTracking = true
            fetchImmediateLocation(
                context = context,
                onSuccess = { point ->
                    liveDropoffPoint = point
                    recenterTrigger++
                    val coords = String.format(Locale.US, "%.4f, %.4f", point.latitude, point.longitude)
                    onPinInfo("Live GPS pin locked ($coords)")
                },
                onFailure = { msg -> onPinInfo(msg) }
            )
        } else {
            isLiveGpsTracking = false
            onPinInfo("Location permission denied. Using saved ${order.deliveryAddressLabel} pin.")
        }
    }

    fun toggleLiveGps() {
        if (isLiveGpsTracking) {
            isLiveGpsTracking = false
            onPinInfo("Paused live GPS pin updates.")
            return
        }
        val hasFine = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (hasFine || hasCoarse) {
            isLiveGpsTracking = true
            fetchImmediateLocation(
                context = context,
                onSuccess = { point ->
                    liveDropoffPoint = point
                    recenterTrigger++
                    val coords = String.format(Locale.US, "%.4f, %.4f", point.latitude, point.longitude)
                    onPinInfo("Live GPS pin locked ($coords)")
                },
                onFailure = { msg -> onPinInfo(msg) }
            )
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    val mapHeight = when (metrics.sizeClass) {
        DeviceSizeClass.SMALL_COMPACT -> 255.dp
        DeviceSizeClass.COMPACT -> 292.dp
        DeviceSizeClass.MEDIUM -> 330.dp
        DeviceSizeClass.EXPANDED -> 360.dp
    }

    // Blinkit-style hero street map card
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = tokens.surface),
        border = BorderStroke(1.dp, tokens.border),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("live_delivery_map_card")
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(modifier = Modifier.fillMaxWidth()) {
                BlinkitOsmStreetMapSurface(
                    order = order,
                    geoRoute = geoRoute,
                    fullWaypoints = fullWaypoints,
                    coveredWaypoints = polySplit.coveredPoints,
                    hubPoint = hubPoint,
                    riderPoint = riderGeo,
                    riderHeadingDegrees = riderHeading,
                    riderBadgeLabel = riderBadgeLabel,
                    dropoffPoint = activeDropoff,
                    recenterTrigger = recenterTrigger,
                    mapHeight = mapHeight,
                    isLiveGpsTracking = isLiveGpsTracking,
                    onToggleGps = { toggleLiveGps() },
                    onExpandFullscreen = { showFullScreenMap = true },
                    onTapPin = { message ->
                        selectedPinLabel = message
                        onPinInfo(message)
                    }
                )
            }

            // Clean Blinkit-style bottom telemetry & street info footer
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = metrics.cardPadding, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    TelemetryChip(
                        icon = Icons.Rounded.Navigation,
                        label = remainingDistanceLabel,
                        highlighted = order.stage == OrderStage.OUT_FOR_DELIVERY
                    )
                    TelemetryChip(
                        icon = Icons.Rounded.DeliveryDining,
                        label = speedLabel,
                        highlighted = false
                    )
                    TelemetryChip(
                        icon = Icons.Rounded.GpsFixed,
                        label = String.format(Locale.US, "%.4f, %.4f", riderGeo.latitude, riderGeo.longitude),
                        highlighted = false,
                        mono = true
                    )
                }

                HorizontalDivider(color = tokens.border.copy(alpha = 0.6f))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = selectedPinLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = tokens.ink,
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .align(Alignment.CenterVertically)
                    )

                    OutlinedButton(
                        onClick = {
                            val uri = Uri.parse(
                                "geo:${riderGeo.latitude},${riderGeo.longitude}?q=${activeDropoff.latitude},${activeDropoff.longitude}(${Uri.encode(order.deliveryAddressLabel)})"
                            )
                            val intent = Intent(Intent.ACTION_VIEW, uri)
                            runCatching { context.startActivity(intent) }
                                .onFailure {
                                    onPinInfo("No external Maps app installed. Live street map active in-app.")
                                }
                        },
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier
                            .defaultMinSize(minHeight = 40.dp)
                            .testTag("open_external_maps_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Map,
                            contentDescription = null,
                            modifier = Modifier.size(metrics.iconSmall)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("Open Maps", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }

    if (showFullScreenMap) {
        Dialog(
            onDismissRequest = { showFullScreenMap = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                color = tokens.background,
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Blinkit Green Fullscreen Header
                    Surface(
                        color = tokens.deliveryGreenDeep,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = metrics.screenHorizontalPadding, vertical = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (order.stage == OrderStage.DELIVERED) {
                                        "Delivered to ${order.deliveryAddressLabel}"
                                    } else {
                                        "Arriving in $etaMins min • ${order.orderId}"
                                    },
                                    style = MaterialTheme.typography.titleLarge,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${order.riderName} • $remainingDistanceLabel • $speedLabel",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.88f)
                                )
                            }
                            IconButton(
                                onClick = { showFullScreenMap = false },
                                modifier = Modifier
                                    .size(48.dp)
                                    .testTag("close_fullscreen_map_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Close,
                                    contentDescription = "Close fullscreen map",
                                    tint = Color.White,
                                    modifier = Modifier.size(metrics.iconLarge)
                                )
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        BlinkitOsmStreetMapSurface(
                            order = order,
                            geoRoute = geoRoute,
                            fullWaypoints = fullWaypoints,
                            coveredWaypoints = polySplit.coveredPoints,
                            hubPoint = hubPoint,
                            riderPoint = riderGeo,
                            riderHeadingDegrees = riderHeading,
                            riderBadgeLabel = riderBadgeLabel,
                            dropoffPoint = activeDropoff,
                            recenterTrigger = recenterTrigger,
                            mapHeight = null,
                            isLiveGpsTracking = isLiveGpsTracking,
                            onToggleGps = { toggleLiveGps() },
                            onExpandFullscreen = null,
                            onTapPin = { message ->
                                selectedPinLabel = message
                                onPinInfo(message)
                            }
                        )
                    }

                    Surface(
                        color = tokens.surface,
                        border = BorderStroke(1.dp, tokens.border),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(metrics.cardPadding),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = selectedPinLabel,
                                style = MaterialTheme.typography.bodyMedium,
                                color = tokens.ink,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Real OpenStreetMap street route • Pinch/drag to explore • Tap pins for details",
                                style = MaterialTheme.typography.bodySmall,
                                color = tokens.muted
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BlinkitOsmStreetMapSurface(
    order: OrderRecord,
    geoRoute: NeighborhoodStreetRoute,
    fullWaypoints: List<OsmGeoPoint>,
    coveredWaypoints: List<OsmGeoPoint>,
    hubPoint: OsmGeoPoint,
    riderPoint: OsmGeoPoint,
    riderHeadingDegrees: Float,
    riderBadgeLabel: String,
    dropoffPoint: OsmGeoPoint,
    recenterTrigger: Int,
    mapHeight: Dp?,
    isLiveGpsTracking: Boolean,
    onToggleGps: () -> Unit,
    onExpandFullscreen: (() -> Unit)?,
    onTapPin: (String) -> Unit
) {
    val tokens = QuickKartThemeTokens.colors
    val metrics = QuickKartMetricsTokens.metrics
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Configure osmdroid cache strictly in app-private cacheDir (zero storage permissions)
    remember(context) {
        Configuration.getInstance().apply {
            userAgentValue = "${context.packageName}/1.0"
            osmdroidBasePath = File(context.cacheDir, "osmdroid")
            osmdroidTileCache = File(context.cacheDir, "osmdroid/tiles")
        }
        true
    }

    // Softened pastel light-mode tile filter for crisp Blinkit-style contrast under route & pins
    val lightPastelTileFilter = remember {
        val cm = ColorMatrix().apply { setSaturation(0.72f) }
        val bright = ColorMatrix(
            floatArrayOf(
                1.03f, 0f, 0f, 0f, 6f,
                0f, 1.03f, 0f, 0f, 6f,
                0f, 0f, 1.02f, 0f, 4f,
                0f, 0f, 0f, 1f, 0f
            )
        )
        cm.postConcat(bright)
        ColorMatrixColorFilter(cm)
    }

    val hubDrawable = remember(context, geoRoute.hubName) {
        createLabeledPodPinDrawable(
            context = context,
            label = "Pod"
        )
    }
    val destDrawable = remember(context, order.deliveryAddressLabel) {
        createLabeledHomePinDrawable(
            context = context,
            label = order.deliveryAddressLabel
        )
    }
    val quantizedHeading = ((riderHeadingDegrees / 15f).roundToInt() * 15) % 360
    val riderDrawable = remember(context, quantizedHeading, riderBadgeLabel) {
        createBlinkitRiderPinDrawable(
            context = context,
            headingDegrees = quantizedHeading.toFloat(),
            pillLabel = riderBadgeLabel
        )
    }

    val mapView = remember(context) {
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
            isTilesScaledToDpi = true
            minZoomLevel = 11.0
            maxZoomLevel = 19.5
            controller.setZoom(15.8)
            controller.setCenter(riderPoint)
            // Keep LazyColumn vertical scroll from stealing pan/pinch map gestures
            setOnTouchListener { view, _ ->
                view.parent?.requestDisallowInterceptTouchEvent(true)
                false
            }
        }
    }

    // Double-cased route polylines (white/surface outer casing + green covered + dashed remaining) + 3 Blinkit markers
    val overlayHolders = remember(mapView) {
        val routeCasingLine = Polyline(mapView).apply {
            outlinePaint.strokeCap = Paint.Cap.ROUND
            outlinePaint.strokeJoin = Paint.Join.ROUND
        }
        val pendingLine = Polyline(mapView).apply {
            outlinePaint.strokeCap = Paint.Cap.ROUND
            outlinePaint.strokeJoin = Paint.Join.ROUND
            outlinePaint.pathEffect = DashPathEffect(floatArrayOf(22f, 14f), 0f)
        }
        val coveredLine = Polyline(mapView).apply {
            outlinePaint.color = android.graphics.Color.rgb(12, 131, 31) // Blinkit Green
            outlinePaint.strokeWidth = 12f
            outlinePaint.strokeCap = Paint.Cap.ROUND
            outlinePaint.strokeJoin = Paint.Join.ROUND
        }
        val hubMarker = Marker(mapView).apply {
            setAnchor(Marker.ANCHOR_CENTER, 0.72f)
        }
        val destMarker = Marker(mapView).apply {
            setAnchor(Marker.ANCHOR_CENTER, 0.72f)
        }
        val riderMarker = Marker(mapView).apply {
            setAnchor(Marker.ANCHOR_CENTER, 0.65f)
        }
        mapView.overlays.addAll(
            listOf(routeCasingLine, pendingLine, coveredLine, hubMarker, destMarker, riderMarker)
        )
        listOf(routeCasingLine, pendingLine, coveredLine, hubMarker, destMarker, riderMarker)
    }

    var lastFittedOrderAndTrigger by remember { mutableStateOf<Pair<String, Int>?>(null) }

    DisposableEffect(mapView, lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        mapView.onResume()
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapView.onPause()
            mapView.onDetach()
        }
    }

    val boxModifier = if (mapHeight != null) {
        Modifier
            .fillMaxWidth()
            .height(mapHeight)
    } else {
        Modifier.fillMaxSize()
    }

    Box(
        modifier = boxModifier
            .background(if (tokens.isDark) Color(0xFF0E141D) else Color(0xFFEAF0F6))
    ) {
        AndroidView(
            factory = { mapView },
            update = { mv ->
                mv.overlayManager.tilesOverlay.setColorFilter(
                    if (tokens.isDark) TilesOverlay.INVERT_COLORS else lightPastelTileFilter
                )

                val routeCasingLine = overlayHolders[0] as Polyline
                val pendingLine = overlayHolders[1] as Polyline
                val coveredLine = overlayHolders[2] as Polyline
                val hubMarker = overlayHolders[3] as Marker
                val destMarker = overlayHolders[4] as Marker
                val riderMarker = overlayHolders[5] as Marker

                routeCasingLine.outlinePaint.color = if (tokens.isDark) {
                    android.graphics.Color.rgb(21, 26, 34)
                } else {
                    android.graphics.Color.WHITE
                }
                routeCasingLine.outlinePaint.strokeWidth = 20f
                routeCasingLine.setPoints(fullWaypoints)

                pendingLine.outlinePaint.color = if (tokens.isDark) {
                    android.graphics.Color.rgb(255, 212, 0)
                } else {
                    android.graphics.Color.rgb(20, 20, 20)
                }
                pendingLine.outlinePaint.strokeWidth = 10f
                pendingLine.setPoints(fullWaypoints)

                coveredLine.setPoints(coveredWaypoints)

                hubMarker.position = hubPoint
                hubMarker.icon = hubDrawable
                hubMarker.title = geoRoute.hubName
                hubMarker.snippet = "Dark Store Hub: ${geoRoute.hubName} (${order.neighborhoodName})"
                hubMarker.setOnMarkerClickListener { m, _ ->
                    onTapPin(m.snippet ?: m.title)
                    true
                }

                destMarker.position = dropoffPoint
                destMarker.icon = destDrawable
                destMarker.title = order.deliveryAddressLabel
                destMarker.snippet = "Drop-off (${order.deliveryAddressLabel}): ${order.deliveryAddressFull}"
                destMarker.setOnMarkerClickListener { m, _ ->
                    onTapPin(m.snippet ?: m.title)
                    true
                }

                riderMarker.position = riderPoint
                riderMarker.icon = riderDrawable
                riderMarker.title = order.riderName
                riderMarker.snippet = "${order.riderName} • ${order.stage.title} • ${geoRoute.streetAxisLabel}"
                riderMarker.setOnMarkerClickListener { m, _ ->
                    onTapPin(m.snippet ?: m.title)
                    true
                }

                val currentKey = order.orderId to recenterTrigger
                if (lastFittedOrderAndTrigger != currentKey) {
                    lastFittedOrderAndTrigger = currentKey
                    mv.post {
                        runCatching {
                            val box = BoundingBox.fromGeoPoints(fullWaypoints)
                            mv.zoomToBoundingBox(box, false, 88)
                        }.onFailure {
                            mv.controller.setZoom(15.8)
                            mv.controller.setCenter(riderPoint)
                        }
                    }
                }
                mv.invalidate()
            },
            modifier = Modifier.fillMaxSize()
        )

        // Blinkit-style floating top-left street route status pill
        Surface(
            color = tokens.surface.copy(alpha = 0.96f),
            shape = RoundedCornerShape(50),
            border = BorderStroke(1.dp, tokens.border),
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(10.dp)
                .shadow(4.dp, RoundedCornerShape(50))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                LivePulseBadge(
                    label = if (isLiveGpsTracking) "GPS LIVE" else "LIVE",
                    color = tokens.deliveryGreenDeep
                )
                Text(
                    text = geoRoute.streetAxisLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = tokens.ink,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )
            }
        }

        // Top-right floating GPS + Fullscreen controls
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(50),
                color = if (isLiveGpsTracking) tokens.deliveryGreenDeep else tokens.surface.copy(alpha = 0.96f),
                border = BorderStroke(
                    1.dp,
                    if (isLiveGpsTracking) tokens.deliveryGreenDeep else tokens.border
                ),
                modifier = Modifier
                    .defaultMinSize(minHeight = 40.dp)
                    .shadow(4.dp, RoundedCornerShape(50))
                    .clip(RoundedCornerShape(50))
                    .clickable { onToggleGps() }
                    .testTag("locate_me_button")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.MyLocation,
                        contentDescription = "Toggle live GPS pin",
                        tint = if (isLiveGpsTracking) Color.White else tokens.deliveryGreenDeep,
                        modifier = Modifier.size(metrics.iconSmall)
                    )
                    Text(
                        text = if (isLiveGpsTracking) "GPS On" else "GPS",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isLiveGpsTracking) Color.White else tokens.ink,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (onExpandFullscreen != null) {
                Surface(
                    shape = CircleShape,
                    color = tokens.surface.copy(alpha = 0.96f),
                    border = BorderStroke(1.dp, tokens.border),
                    modifier = Modifier
                        .size(40.dp)
                        .shadow(4.dp, CircleShape)
                        .clip(CircleShape)
                        .clickable { onExpandFullscreen() }
                        .testTag("expand_live_map_button")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.Fullscreen,
                            contentDescription = "Expand street map",
                            tint = tokens.ink,
                            modifier = Modifier.size(metrics.iconMedium)
                        )
                    }
                }
            }
        }

        // Bottom-left OpenStreetMap attribution pill
        Surface(
            color = tokens.surface.copy(alpha = 0.88f),
            shape = RoundedCornerShape(6.dp),
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(10.dp)
        ) {
            Text(
                text = "© OpenStreetMap",
                style = MaterialTheme.typography.labelSmall,
                color = tokens.muted,
                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
            )
        }

        // Bottom-right grouped Blinkit-style floating map controls
        Surface(
            color = tokens.surface.copy(alpha = 0.96f),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, tokens.border),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(10.dp)
                .shadow(4.dp, RoundedCornerShape(14.dp))
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                MapControlButton(
                    icon = Icons.Rounded.Add,
                    contentDescription = "Zoom in street map",
                    onClick = { mapView.controller.zoomIn() }
                )
                HorizontalDivider(
                    modifier = Modifier.width(28.dp),
                    color = tokens.border.copy(alpha = 0.7f)
                )
                MapControlButton(
                    icon = Icons.Rounded.Remove,
                    contentDescription = "Zoom out street map",
                    onClick = { mapView.controller.zoomOut() }
                )
                HorizontalDivider(
                    modifier = Modifier.width(28.dp),
                    color = tokens.border.copy(alpha = 0.7f)
                )
                MapControlButton(
                    icon = Icons.Rounded.GpsFixed,
                    contentDescription = "Fit route on street map",
                    onClick = {
                        runCatching {
                            val box = BoundingBox.fromGeoPoints(fullWaypoints)
                            mapView.zoomToBoundingBox(box, true, 88)
                        }
                    }
                )
            }
        }
    }
}

private fun createLabeledPodPinDrawable(
    context: Context,
    label: String
): BitmapDrawable {
    val density = context.resources.displayMetrics.density
    val width = (72 * density).roundToInt()
    val height = (64 * density).roundToInt()
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    val cx = width / 2f
    val circleY = 20 * density

    // White border + charcoal circle + yellow center badge
    paint.color = android.graphics.Color.WHITE
    canvas.drawCircle(cx, circleY, 15 * density, paint)

    paint.color = android.graphics.Color.rgb(20, 20, 20)
    canvas.drawCircle(cx, circleY, 12.5f * density, paint)

    paint.color = android.graphics.Color.rgb(255, 212, 0)
    canvas.drawCircle(cx, circleY, 6 * density, paint)

    drawFloatingPillLabel(
        canvas = canvas,
        density = density,
        centerX = cx,
        topY = 38 * density,
        text = label,
        bgColor = android.graphics.Color.rgb(20, 20, 20),
        textColor = android.graphics.Color.WHITE
    )
    return BitmapDrawable(context.resources, bitmap)
}

private fun createLabeledHomePinDrawable(
    context: Context,
    label: String
): BitmapDrawable {
    val density = context.resources.displayMetrics.density
    val width = (92 * density).roundToInt()
    val height = (72 * density).roundToInt()
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    val cx = width / 2f
    val circleY = 24 * density

    // Soft Blinkit green arrival halo
    paint.color = android.graphics.Color.argb(56, 12, 131, 31)
    canvas.drawCircle(cx, circleY, 23 * density, paint)

    // Crisp white ring + Blinkit green home pin
    paint.color = android.graphics.Color.WHITE
    canvas.drawCircle(cx, circleY, 15.5f * density, paint)

    paint.color = android.graphics.Color.rgb(12, 131, 31)
    canvas.drawCircle(cx, circleY, 13 * density, paint)

    // Minimalist white house silhouette inside pin
    paint.color = android.graphics.Color.WHITE
    val housePath = Path().apply {
        moveTo(cx, circleY - 6 * density)
        lineTo(cx + 6 * density, circleY - 0.5f * density)
        lineTo(cx + 4.2f * density, circleY - 0.5f * density)
        lineTo(cx + 4.2f * density, circleY + 5.5f * density)
        lineTo(cx - 4.2f * density, circleY + 5.5f * density)
        lineTo(cx - 4.2f * density, circleY - 0.5f * density)
        lineTo(cx - 6 * density, circleY - 0.5f * density)
        close()
    }
    canvas.drawPath(housePath, paint)

    drawFloatingPillLabel(
        canvas = canvas,
        density = density,
        centerX = cx,
        topY = 44 * density,
        text = label,
        bgColor = android.graphics.Color.rgb(12, 131, 31),
        textColor = android.graphics.Color.WHITE
    )
    return BitmapDrawable(context.resources, bitmap)
}

private fun createBlinkitRiderPinDrawable(
    context: Context,
    headingDegrees: Float,
    pillLabel: String
): BitmapDrawable {
    val density = context.resources.displayMetrics.density
    val width = (104 * density).roundToInt()
    val height = (78 * density).roundToInt()
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    val cx = width / 2f

    // Floating ETA pill above rider badge (Blinkit signature callout)
    drawFloatingPillLabel(
        canvas = canvas,
        density = density,
        centerX = cx,
        topY = 2 * density,
        text = pillLabel,
        bgColor = android.graphics.Color.rgb(20, 20, 20),
        textColor = android.graphics.Color.rgb(255, 212, 0)
    )

    val circleY = 46 * density
    // Outer yellow radar pulse halo
    paint.color = android.graphics.Color.argb(72, 255, 212, 0)
    canvas.drawCircle(cx, circleY, 26 * density, paint)

    // Crisp white collar + electric yellow circle + green ring
    paint.color = android.graphics.Color.WHITE
    canvas.drawCircle(cx, circleY, 18.5f * density, paint)

    paint.color = android.graphics.Color.rgb(12, 131, 31)
    canvas.drawCircle(cx, circleY, 16.5f * density, paint)

    paint.color = android.graphics.Color.rgb(255, 212, 0)
    canvas.drawCircle(cx, circleY, 14.5f * density, paint)

    // Rotated directional navigation / rider arrow aligned to route heading
    canvas.save()
    canvas.rotate(headingDegrees, cx, circleY)
    paint.color = android.graphics.Color.rgb(20, 20, 20)
    val arrowPath = Path().apply {
        moveTo(cx, circleY - 8.5f * density)
        lineTo(cx + 6.5f * density, circleY + 6.5f * density)
        lineTo(cx, circleY + 3.2f * density)
        lineTo(cx - 6.5f * density, circleY + 6.5f * density)
        close()
    }
    canvas.drawPath(arrowPath, paint)
    canvas.restore()

    return BitmapDrawable(context.resources, bitmap)
}

private fun drawFloatingPillLabel(
    canvas: Canvas,
    density: Float,
    centerX: Float,
    topY: Float,
    text: String,
    bgColor: Int,
    textColor: Int
) {
    val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = textColor
        textSize = 10.5f * density
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }
    val textWidth = textPaint.measureText(text)
    val padH = 7f * density
    val pillH = 19f * density
    val halfW = (textWidth / 2f + padH).coerceAtLeast(18f * density)
    val rect = RectF(centerX - halfW, topY, centerX + halfW, topY + pillH)

    val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.WHITE
    }
    canvas.drawRoundRect(
        RectF(rect.left - 1.5f * density, rect.top - 1.5f * density, rect.right + 1.5f * density, rect.bottom + 1.5f * density),
        pillH / 2f,
        pillH / 2f,
        borderPaint
    )

    val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = bgColor
    }
    canvas.drawRoundRect(rect, pillH / 2f, pillH / 2f, bgPaint)

    val fm = textPaint.fontMetrics
    val baseline = rect.centerY() - (fm.ascent + fm.descent) / 2f
    canvas.drawText(text, centerX - textWidth / 2f, baseline, textPaint)
}

@Composable
private fun MapControlButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    val tokens = QuickKartThemeTokens.colors
    val metrics = QuickKartMetricsTokens.metrics
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(44.dp)
            .clickable(onClick = onClick)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tokens.ink,
            modifier = Modifier.size(metrics.iconMedium)
        )
    }
}

@Composable
private fun LivePulseBadge(
    label: String,
    color: Color
) {
    val pulseTransition = rememberInfiniteTransition(label = "badge_pulse")
    val alpha by pulseTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(700),
            repeatMode = RepeatMode.Reverse
        ),
        label = "badge_alpha"
    )
    Surface(
        color = color.copy(alpha = 0.14f),
        shape = RoundedCornerShape(50)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = alpha))
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = color,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun TelemetryChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    highlighted: Boolean,
    mono: Boolean = false
) {
    val tokens = QuickKartThemeTokens.colors
    val metrics = QuickKartMetricsTokens.metrics
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (highlighted) tokens.deliveryGreenSoft else tokens.surfaceElevated,
        border = BorderStroke(1.dp, if (highlighted) tokens.deliveryGreen else tokens.border)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (highlighted) tokens.deliveryGreenDeep else tokens.muted,
                modifier = Modifier.size(metrics.iconSmall)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontFamily = if (mono) JetBrainsMonoFontFamily else null,
                color = if (highlighted) tokens.deliveryGreenDeep else tokens.ink,
                fontWeight = if (highlighted) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}

@SuppressLint("MissingPermission")
private fun fetchImmediateLocation(
    context: Context,
    onSuccess: (OsmGeoPoint) -> Unit,
    onFailure: (String) -> Unit
) {
    val client = LocationServices.getFusedLocationProviderClient(context)
    val cancellationTokenSource = CancellationTokenSource()
    client.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cancellationTokenSource.token)
        .addOnSuccessListener { location ->
            if (location != null) {
                onSuccess(OsmGeoPoint(location.latitude, location.longitude))
            } else {
                client.lastLocation.addOnSuccessListener { lastLoc ->
                    if (lastLoc != null) {
                        onSuccess(OsmGeoPoint(lastLoc.latitude, lastLoc.longitude))
                    } else {
                        onFailure("GPS signal unavailable. Using saved neighborhood hub.")
                    }
                }.addOnFailureListener {
                    onFailure("Unable to fetch GPS coordinates right now.")
                }
            }
        }
        .addOnFailureListener {
            onFailure("Location services unavailable on this device.")
        }
}
