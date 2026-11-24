package com.example.rider.ui.components

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
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.location.Location
import android.net.Uri
import android.os.Looper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import com.example.rider.data.model.RiderTripOrder
import com.example.rider.data.model.RiderTripStage
import com.example.rider.ui.theme.DeviceSizeClass
import com.example.rider.ui.theme.JetBrainsMonoFontFamily
import com.example.rider.ui.theme.RiderMetricsTokens
import com.example.rider.ui.theme.RiderThemeTokens
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
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RiderStreetMapCard(
    trip: RiderTripOrder,
    onMapMessage: (String) -> Unit
) {
    val tokens = RiderThemeTokens.colors
    val metrics = RiderMetricsTokens.metrics
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val hubPoint = remember(trip.hubLat, trip.hubLng) { OsmGeoPoint(trip.hubLat, trip.hubLng) }
    val dropPoint = remember(trip.dropLat, trip.dropLng) { OsmGeoPoint(trip.dropLat, trip.dropLng) }

    val midPoint = remember(hubPoint, dropPoint) {
        OsmGeoPoint(
            (hubPoint.latitude + dropPoint.latitude) / 2.0 + 0.0007,
            (hubPoint.longitude + dropPoint.longitude) / 2.0 - 0.0005
        )
    }

    var liveRiderPoint by remember(trip.orderId) { mutableStateOf<OsmGeoPoint?>(null) }
    var isLiveGpsOn by remember(trip.orderId) { mutableStateOf(false) }
    var recenterTrigger by remember { mutableIntStateOf(0) }
    var showFullScreen by remember { mutableStateOf(false) }

    val simulatedRiderPoint = remember(trip.stage, hubPoint, midPoint, dropPoint) {
        when (trip.stage) {
            RiderTripStage.OFFERED, RiderTripStage.HEADING_TO_HUB -> OsmGeoPoint(
                hubPoint.latitude - 0.0022,
                hubPoint.longitude - 0.0018
            )
            RiderTripStage.PICKING_AT_POD -> hubPoint
            RiderTripStage.OUT_FOR_DELIVERY -> midPoint
            RiderTripStage.ARRIVED_AT_DOOR, RiderTripStage.DELIVERED -> dropPoint
            RiderTripStage.DECLINED -> hubPoint
        }
    }

    val riderPoint = liveRiderPoint ?: simulatedRiderPoint
    val routeWaypoints = remember(hubPoint, midPoint, dropPoint) {
        listOf(hubPoint, midPoint, dropPoint)
    }

    val targetDestination = if (
        trip.stage == RiderTripStage.HEADING_TO_HUB ||
        trip.stage == RiderTripStage.PICKING_AT_POD
    ) {
        hubPoint
    } else {
        dropPoint
    }

    val distanceMeters = remember(riderPoint, targetDestination) {
        val out = FloatArray(1)
        Location.distanceBetween(
            riderPoint.latitude,
            riderPoint.longitude,
            targetDestination.latitude,
            targetDestination.longitude,
            out
        )
        out[0].roundToInt().coerceAtLeast(0)
    }

    val distanceLabel = when {
        distanceMeters < 50 -> "At pin"
        distanceMeters >= 1000 -> String.format(Locale.US, "%.1f km", distanceMeters / 1000f)
        else -> "$distanceMeters m"
    }

    val fusedClient = remember(context) { LocationServices.getFusedLocationProviderClient(context) }
    val locationCallback = remember {
        object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val loc = result.lastLocation ?: return
                liveRiderPoint = OsmGeoPoint(loc.latitude, loc.longitude)
            }
        }
    }

    DisposableEffect(isLiveGpsOn, lifecycleOwner) {
        var started = false
        fun hasPerm(): Boolean {
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
        fun start() {
            if (isLiveGpsOn && hasPerm() && !started) {
                val req = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5_000L)
                    .setMinUpdateIntervalMillis(2_500L)
                    .build()
                fusedClient.requestLocationUpdates(req, locationCallback, Looper.getMainLooper())
                started = true
            }
        }

        fun stop() {
            if (started) {
                fusedClient.removeLocationUpdates(locationCallback)
                started = false
            }
        }

        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> start()
                Lifecycle.Event.ON_PAUSE -> stop()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        if (isLiveGpsOn) start()

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            stop()
        }
    }

    val permLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        val granted = grants[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            grants[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            isLiveGpsOn = true
            fetchRiderCurrentLocation(
                context = context,
                onSuccess = { pt ->
                    liveRiderPoint = pt
                    recenterTrigger++
                    onMapMessage("Rider live GPS locked")
                },
                onFailure = { onMapMessage(it) }
            )
        } else {
            isLiveGpsOn = false
            onMapMessage("Location permission denied — showing Pod route preview.")
        }
    }

    fun toggleLiveGps() {
        if (isLiveGpsOn) {
            isLiveGpsOn = false
            liveRiderPoint = null
            onMapMessage("Switched to simulated Pod route")
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
            isLiveGpsOn = true
            fetchRiderCurrentLocation(
                context = context,
                onSuccess = { pt ->
                    liveRiderPoint = pt
                    recenterTrigger++
                    onMapMessage("Rider live GPS locked")
                },
                onFailure = { onMapMessage(it) }
            )
        } else {
            permLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    val mapHeight = when (metrics.sizeClass) {
        DeviceSizeClass.SMALL_COMPACT -> 225.dp
        DeviceSizeClass.COMPACT -> 255.dp
        DeviceSizeClass.MEDIUM -> 290.dp
        DeviceSizeClass.EXPANDED -> 320.dp
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = tokens.surface),
        border = BorderStroke(1.dp, tokens.border),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("rider_street_map_card")
    ) {
        Column {
            RiderOsmMapSurface(
                trip = trip,
                waypoints = routeWaypoints,
                hubPoint = hubPoint,
                riderPoint = riderPoint,
                dropPoint = dropPoint,
                recenterTrigger = recenterTrigger,
                mapHeight = mapHeight,
                isLiveGpsOn = isLiveGpsOn,
                onToggleGps = { toggleLiveGps() },
                onExpandFullscreen = { showFullScreen = true },
                onTapPin = onMapMessage
            )

            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = metrics.cardPadding, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Column(modifier = Modifier.weight(1f, fill = false)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Navigation,
                            contentDescription = null,
                            tint = tokens.deliveryGreenDeep,
                            modifier = Modifier.size(metrics.iconSmall)
                        )
                        Text(
                            text = if (trip.stage == RiderTripStage.HEADING_TO_HUB) {
                                "$distanceLabel to ${trip.hubName}"
                            } else {
                                "$distanceLabel to ${trip.customerName}"
                            },
                            style = MaterialTheme.typography.titleSmall,
                            color = tokens.ink,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "${trip.coordinatesLabel} • ${trip.estimatedMinutes} min SLA",
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = JetBrainsMonoFontFamily,
                        color = tokens.muted
                    )
                }

                OutlinedButton(
                    onClick = {
                        val dest = targetDestination
                        val label = if (trip.stage == RiderTripStage.HEADING_TO_HUB) {
                            trip.hubName
                        } else {
                            "${trip.customerName} (${trip.deliveryAddressLabel})"
                        }
                        val uri = Uri.parse(
                            "geo:${dest.latitude},${dest.longitude}?q=${dest.latitude},${dest.longitude}(${Uri.encode(label)})"
                        )
                        runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, uri)) }
                            .onFailure { onMapMessage("No external Maps app installed.") }
                    },
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier
                        .defaultMinSize(minHeight = 42.dp)
                        .testTag("rider_open_maps_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Map,
                        contentDescription = null,
                        modifier = Modifier.size(metrics.iconSmall)
                    )
                    Spacer(Modifier.width(5.dp))
                    Text("Navigate", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }

    if (showFullScreen) {
        Dialog(
            onDismissRequest = { showFullScreen = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                color = tokens.background,
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Surface(
                        color = tokens.deliveryGreenDeep,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${trip.orderId} • ${trip.stage.label}",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${trip.deliveryAddressFull} ($distanceLabel)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.88f)
                                )
                            }
                            IconButton(
                                onClick = { showFullScreen = false },
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Close,
                                    contentDescription = "Close map",
                                    tint = Color.White
                                )
                            }
                        }
                    }

                    Box(modifier = Modifier.weight(1f)) {
                        RiderOsmMapSurface(
                            trip = trip,
                            waypoints = routeWaypoints,
                            hubPoint = hubPoint,
                            riderPoint = riderPoint,
                            dropPoint = dropPoint,
                            recenterTrigger = recenterTrigger,
                            mapHeight = null,
                            isLiveGpsOn = isLiveGpsOn,
                            onToggleGps = { toggleLiveGps() },
                            onExpandFullscreen = null,
                            onTapPin = onMapMessage
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RiderOsmMapSurface(
    trip: RiderTripOrder,
    waypoints: List<OsmGeoPoint>,
    hubPoint: OsmGeoPoint,
    riderPoint: OsmGeoPoint,
    dropPoint: OsmGeoPoint,
    recenterTrigger: Int,
    mapHeight: Dp?,
    isLiveGpsOn: Boolean,
    onToggleGps: () -> Unit,
    onExpandFullscreen: (() -> Unit)?,
    onTapPin: (String) -> Unit
) {
    val tokens = RiderThemeTokens.colors
    val metrics = RiderMetricsTokens.metrics
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    remember(context) {
        Configuration.getInstance().apply {
            userAgentValue = "${context.packageName}/1.0"
            osmdroidBasePath = File(context.cacheDir, "osmdroid")
            osmdroidTileCache = File(context.cacheDir, "osmdroid/tiles")
        }
        true
    }

    val lightFilter = remember {
        val cm = ColorMatrix().apply { setSaturation(0.75f) }
        ColorMatrixColorFilter(cm)
    }

    val hubDrawable = remember(context) {
        createPinDrawable(
            context = context,
            label = "Pod",
            pinColor = android.graphics.Color.rgb(20, 20, 20),
            accentColor = android.graphics.Color.rgb(255, 212, 0)
        )
    }
    val dropDrawable = remember(context, trip.deliveryAddressLabel) {
        createPinDrawable(
            context = context,
            label = trip.deliveryAddressLabel,
            pinColor = android.graphics.Color.rgb(0, 110, 47),
            accentColor = android.graphics.Color.WHITE
        )
    }
    val riderDrawable = remember(context) {
        createPinDrawable(
            context = context,
            label = "You",
            pinColor = android.graphics.Color.rgb(249, 115, 22),
            accentColor = android.graphics.Color.WHITE
        )
    }

    val mapView = remember(context) {
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
            isTilesScaledToDpi = true
            minZoomLevel = 11.0
            maxZoomLevel = 19.0
            controller.setZoom(15.5)
            controller.setCenter(hubPoint)
            setOnTouchListener { view, _ ->
                view.parent?.requestDisallowInterceptTouchEvent(true)
                false
            }
        }
    }

    val overlays = remember(mapView) {
        val casing = Polyline(mapView).apply {
            outlinePaint.strokeWidth = 18f
            outlinePaint.strokeCap = Paint.Cap.ROUND
            outlinePaint.strokeJoin = Paint.Join.ROUND
        }
        val routeLine = Polyline(mapView).apply {
            outlinePaint.color = android.graphics.Color.rgb(0, 110, 47)
            outlinePaint.strokeWidth = 10f
            outlinePaint.strokeCap = Paint.Cap.ROUND
            outlinePaint.strokeJoin = Paint.Join.ROUND
            outlinePaint.pathEffect = DashPathEffect(floatArrayOf(20f, 12f), 0f)
        }
        val hubMarker = Marker(mapView).apply { setAnchor(Marker.ANCHOR_CENTER, 0.72f) }
        val dropMarker = Marker(mapView).apply { setAnchor(Marker.ANCHOR_CENTER, 0.72f) }
        val riderMarker = Marker(mapView).apply { setAnchor(Marker.ANCHOR_CENTER, 0.72f) }
        mapView.overlays.addAll(listOf(casing, routeLine, hubMarker, dropMarker, riderMarker))
        listOf(casing, routeLine, hubMarker, dropMarker, riderMarker)
    }

    var fittedKey by remember { mutableStateOf<Pair<String, Int>?>(null) }

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

    val boxMod = if (mapHeight != null) {
        Modifier
            .fillMaxWidth()
            .height(mapHeight)
    } else {
        Modifier.fillMaxSize()
    }

    Box(
        modifier = boxMod.background(if (tokens.isDark) Color(0xFF0E141D) else Color(0xFFEAF0F6))
    ) {
        AndroidView(
            factory = { mapView },
            update = { mv ->
                mv.overlayManager.tilesOverlay.setColorFilter(
                    if (tokens.isDark) TilesOverlay.INVERT_COLORS else lightFilter
                )
                val casing = overlays[0] as Polyline
                val routeLine = overlays[1] as Polyline
                val hubMarker = overlays[2] as Marker
                val dropMarker = overlays[3] as Marker
                val riderMarker = overlays[4] as Marker

                casing.outlinePaint.color = if (tokens.isDark) {
                    android.graphics.Color.rgb(21, 26, 34)
                } else {
                    android.graphics.Color.WHITE
                }
                casing.setPoints(waypoints)
                routeLine.setPoints(waypoints)

                hubMarker.position = hubPoint
                hubMarker.icon = hubDrawable
                hubMarker.setOnMarkerClickListener { _, _ ->
                    onTapPin("Dark Store Pod: ${trip.hubName} • Bay ${trip.pickupBayCode}")
                    true
                }

                dropMarker.position = dropPoint
                dropMarker.icon = dropDrawable
                dropMarker.setOnMarkerClickListener { _, _ ->
                    onTapPin("Drop-off: ${trip.customerName} • ${trip.deliveryAddressFull}")
                    true
                }

                riderMarker.position = riderPoint
                riderMarker.icon = riderDrawable
                riderMarker.setOnMarkerClickListener { _, _ ->
                    onTapPin("Your location • ${trip.stage.label}")
                    true
                }

                val key = trip.orderId to recenterTrigger
                if (fittedKey != key) {
                    fittedKey = key
                    mv.post {
                        runCatching {
                            val box = BoundingBox.fromGeoPoints(waypoints + riderPoint)
                            mv.zoomToBoundingBox(box, false, 84)
                        }.onFailure {
                            mv.controller.setZoom(15.5)
                            mv.controller.setCenter(hubPoint)
                        }
                    }
                }
                mv.invalidate()
            },
            modifier = Modifier.fillMaxSize()
        )

        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(50),
                color = if (isLiveGpsOn) tokens.deliveryGreenDeep else tokens.surface.copy(alpha = 0.96f),
                border = BorderStroke(1.dp, if (isLiveGpsOn) tokens.deliveryGreenDeep else tokens.border),
                modifier = Modifier
                    .defaultMinSize(minHeight = 40.dp)
                    .shadow(4.dp, RoundedCornerShape(50))
                    .clip(RoundedCornerShape(50))
                    .clickable { onToggleGps() }
                    .testTag("rider_live_gps_button")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.MyLocation,
                        contentDescription = "Toggle rider GPS",
                        tint = if (isLiveGpsOn) Color.White else tokens.deliveryGreenDeep,
                        modifier = Modifier.size(metrics.iconSmall)
                    )
                    Text(
                        text = if (isLiveGpsOn) "GPS On" else "Live GPS",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isLiveGpsOn) Color.White else tokens.ink,
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
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.Fullscreen,
                            contentDescription = "Fullscreen street map",
                            tint = tokens.ink,
                            modifier = Modifier.size(metrics.iconMedium)
                        )
                    }
                }
            }
        }

        Surface(
            color = tokens.surface.copy(alpha = 0.96f),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, tokens.border),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(10.dp)
                .shadow(4.dp, RoundedCornerShape(12.dp))
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(
                    onClick = { mapView.controller.zoomIn() },
                    modifier = Modifier.size(38.dp)
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = "Zoom in", tint = tokens.ink)
                }
                HorizontalDivider(modifier = Modifier.width(24.dp), color = tokens.border)
                IconButton(
                    onClick = { mapView.controller.zoomOut() },
                    modifier = Modifier.size(38.dp)
                ) {
                    Icon(Icons.Rounded.Remove, contentDescription = "Zoom out", tint = tokens.ink)
                }
                HorizontalDivider(modifier = Modifier.width(24.dp), color = tokens.border)
                IconButton(
                    onClick = {
                        runCatching {
                            val box = BoundingBox.fromGeoPoints(waypoints + riderPoint)
                            mapView.zoomToBoundingBox(box, true, 84)
                        }
                    },
                    modifier = Modifier.size(38.dp)
                ) {
                    Icon(Icons.Rounded.GpsFixed, contentDescription = "Recenter route", tint = tokens.ink)
                }
            }
        }
    }
}

private fun createPinDrawable(
    context: Context,
    label: String,
    pinColor: Int,
    accentColor: Int
): BitmapDrawable {
    val density = context.resources.displayMetrics.density
    val width = (84 * density).roundToInt()
    val height = (66 * density).roundToInt()
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    val cx = width / 2f
    val circleY = 20 * density

    paint.color = android.graphics.Color.WHITE
    canvas.drawCircle(cx, circleY, 15 * density, paint)

    paint.color = pinColor
    canvas.drawCircle(cx, circleY, 12.5f * density, paint)

    paint.color = accentColor
    canvas.drawCircle(cx, circleY, 5.5f * density, paint)

    val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.WHITE
        textSize = 10f * density
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }
    val textWidth = textPaint.measureText(label)
    val pillPadH = 7f * density
    val pillW = (textWidth + pillPadH * 2).coerceAtLeast(36f * density)
    val pillH = 18f * density
    val left = cx - pillW / 2f
    val top = 39f * density

    val pillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = pinColor }
    canvas.drawRoundRect(left, top, left + pillW, top + pillH, 9f * density, 9f * density, pillPaint)

    val baseline = top + pillH / 2f - (textPaint.descent() + textPaint.ascent()) / 2f
    canvas.drawText(label, cx - textWidth / 2f, baseline, textPaint)

    return BitmapDrawable(context.resources, bitmap)
}

@SuppressLint("MissingPermission")
private fun fetchRiderCurrentLocation(
    context: Context,
    onSuccess: (OsmGeoPoint) -> Unit,
    onFailure: (String) -> Unit
) {
    val client = LocationServices.getFusedLocationProviderClient(context)
    val cts = CancellationTokenSource()
    client.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token)
        .addOnSuccessListener { loc ->
            if (loc != null) {
                onSuccess(OsmGeoPoint(loc.latitude, loc.longitude))
            } else {
                client.lastLocation.addOnSuccessListener { last ->
                    if (last != null) {
                        onSuccess(OsmGeoPoint(last.latitude, last.longitude))
                    } else {
                        onFailure("GPS fix unavailable right now.")
                    }
                }
            }
        }
        .addOnFailureListener {
            onFailure("Could not fetch GPS fix.")
        }
}
