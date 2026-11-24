package com.example.ui.components

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.EditLocationAlt
import androidx.compose.material.icons.rounded.GpsFixed
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.data.model.Neighborhood
import com.example.data.model.ResolvedPinAddress
import com.example.ui.theme.DeviceSizeClass
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.QuickKartMetricsTokens
import com.example.ui.theme.QuickKartThemeTokens
import com.example.util.AddressPinResolver
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.delay
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint as OsmGeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.TilesOverlay
import java.io.File
import java.util.Locale
import kotlin.math.abs

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddAddressDialog(
    neighborhoods: List<Neighborhood>,
    initialNeighborhoodId: String,
    onDismiss: () -> Unit,
    onSave: (
        label: String,
        fullAddress: String,
        landmark: String,
        neighborhoodId: String,
        area: String,
        city: String,
        state: String,
        postalCode: String,
        country: String,
        latitude: Double?,
        longitude: Double?
    ) -> Unit
) {
    val tokens = QuickKartThemeTokens.colors
    val metrics = QuickKartMetricsTokens.metrics
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val startCoords = remember(initialNeighborhoodId) {
        AddressPinResolver.defaultCoordinateForNeighborhood(initialNeighborhoodId)
    }

    var pinLatitude by remember { mutableDoubleStateOf(startCoords.first) }
    var pinLongitude by remember { mutableDoubleStateOf(startCoords.second) }
    var isDraggingPin by remember { mutableStateOf(false) }
    var isResolvingPin by remember { mutableStateOf(false) }
    var resolvedPin by remember {
        mutableStateOf(
            AddressPinResolver.resolveFromCoordinateGrid(startCoords.first, startCoords.second)
        )
    }

    var selectedLabel by remember { mutableStateOf("Home") }
    var neighborhoodId by remember { mutableStateOf(resolvedPin.nearestNeighborhoodId) }
    var streetAddress by remember { mutableStateOf(resolvedPin.suggestedStreet) }
    var area by remember { mutableStateOf(resolvedPin.area) }
    var city by remember { mutableStateOf(resolvedPin.city) }
    var stateName by remember { mutableStateOf(resolvedPin.state) }
    var postalCode by remember { mutableStateOf(resolvedPin.postalCode) }
    var country by remember { mutableStateOf(resolvedPin.country) }
    var landmark by remember { mutableStateOf(resolvedPin.suggestedLandmark) }

    var streetManuallyEdited by remember { mutableStateOf(false) }
    var landmarkManuallyEdited by remember { mutableStateOf(false) }
    var showCoordinateEditor by remember { mutableStateOf(false) }
    var latInput by remember { mutableStateOf(String.format(Locale.US, "%.5f", startCoords.first)) }
    var lngInput by remember { mutableStateOf(String.format(Locale.US, "%.5f", startCoords.second)) }
    var coordStatusHint by remember { mutableStateOf<String?>(null) }
    var showError by remember { mutableStateOf(false) }

    // Reference to map controller to fly when tapping quick hubs, GPS, or manual coordinates
    var moveMapToPoint by remember { mutableStateOf<((Double, Double) -> Unit)?>(null) }

    fun applyResolvedAddress(resolved: ResolvedPinAddress, forceAllFields: Boolean) {
        resolvedPin = resolved
        area = resolved.area
        city = resolved.city
        stateName = resolved.state
        postalCode = resolved.postalCode
        country = resolved.country
        neighborhoodId = resolved.nearestNeighborhoodId
        if (forceAllFields || !streetManuallyEdited || streetAddress.isBlank()) {
            streetAddress = resolved.suggestedStreet
            showError = false
        }
        if (forceAllFields || !landmarkManuallyEdited || landmark.isBlank()) {
            landmark = resolved.suggestedLandmark
        }
    }

    // Debounced pin reverse-geocoding whenever coordinates settle
    LaunchedEffect(pinLatitude, pinLongitude) {
        latInput = String.format(Locale.US, "%.5f", pinLatitude)
        lngInput = String.format(Locale.US, "%.5f", pinLongitude)
        isResolvingPin = true
        delay(320L)
        isDraggingPin = false
        val resolved = AddressPinResolver.resolveCoordinate(context, pinLatitude, pinLongitude)
        applyResolvedAddress(resolved, forceAllFields = false)
        isResolvingPin = false
    }

    val gpsPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        val granted = grants[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            grants[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            fetchCurrentDeviceCoordinate(
                context = context,
                onSuccess = { lat, lng ->
                    coordStatusHint = "Locked onto your live GPS coordinate"
                    pinLatitude = lat
                    pinLongitude = lng
                    moveMapToPoint?.invoke(lat, lng)
                },
                onError = { msg -> coordStatusHint = msg }
            )
        } else {
            coordStatusHint = "Location permission denied — drag or tap the map pin instead."
        }
    }

    fun requestCurrentGpsPin() {
        val hasFine = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (hasFine || hasCoarse) {
            fetchCurrentDeviceCoordinate(
                context = context,
                onSuccess = { lat, lng ->
                    coordStatusHint = "Locked onto your live GPS coordinate"
                    pinLatitude = lat
                    pinLongitude = lng
                    moveMapToPoint?.invoke(lat, lng)
                },
                onError = { msg -> coordStatusHint = msg }
            )
        } else {
            gpsPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    val mapHeight = when (metrics.sizeClass) {
        DeviceSizeClass.SMALL_COMPACT -> 210.dp
        DeviceSizeClass.COMPACT -> 240.dp
        DeviceSizeClass.MEDIUM -> 270.dp
        DeviceSizeClass.EXPANDED -> 290.dp
    }

    val matchedHub = neighborhoods.find { it.id == neighborhoodId }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = if (metrics.isNarrowPhone) 10.dp else 16.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = tokens.surface,
                border = BorderStroke(1.dp, tokens.border),
                shadowElevation = 10.dp,
                modifier = Modifier
                    .widthIn(max = 680.dp)
                    .fillMaxWidth()
                    .testTag("add_address_dialog")
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = metrics.cardPadding + 2.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            color = tokens.deliveryGreenSoft,
                            shape = CircleShape,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Rounded.LocationOn,
                                    contentDescription = null,
                                    tint = tokens.deliveryGreenDeep,
                                    modifier = Modifier.size(metrics.iconMedium)
                                )
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Pin Delivery Location",
                                style = MaterialTheme.typography.titleLarge,
                                color = tokens.ink
                            )
                            Text(
                                text = matchedHub?.let {
                                    "Nearest Hub: ${it.name} (${it.etaMinutes}m express)"
                                } ?: "Move map pin to auto-fill address fields",
                                style = MaterialTheme.typography.bodySmall,
                                color = tokens.deliveryGreenDeep,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("close_add_address_dialog")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = "Close add address dialog",
                                tint = tokens.ink
                            )
                        }
                    }

                    HorizontalDivider(color = tokens.border)

                    // Scrollable Map + Pin-Generated Fields
                    Column(
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .verticalScroll(scrollState)
                            .padding(metrics.cardPadding),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Interactive Graphical Map Coordinate Picker
                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = tokens.surfaceElevated),
                            border = BorderStroke(1.dp, tokens.border),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("address_pin_map_card")
                        ) {
                            Column {
                                AddressPinPickerMapSurface(
                                    initialLat = startCoords.first,
                                    initialLng = startCoords.second,
                                    mapHeight = mapHeight,
                                    isDraggingPin = isDraggingPin,
                                    isResolvingPin = isResolvingPin,
                                    onMapMoved = { lat, lng, dragging ->
                                        isDraggingPin = dragging
                                        pinLatitude = lat
                                        pinLongitude = lng
                                    },
                                    onRegisterMover = { mover ->
                                        moveMapToPoint = mover
                                    },
                                    onUseCurrentLocation = { requestCurrentGpsPin() }
                                )

                                // Coordinate Readout & Manual Coordinate Editor Strip
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.GpsFixed,
                                            contentDescription = null,
                                            tint = tokens.deliveryGreenDeep,
                                            modifier = Modifier.size(metrics.iconSmall)
                                        )
                                        Text(
                                            text = formatCoordinateDisplay(pinLatitude, pinLongitude),
                                            style = MaterialTheme.typography.labelMedium,
                                            fontFamily = JetBrainsMonoFontFamily,
                                            color = tokens.ink,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("address_pin_coordinates_text")
                                        )
                                        TextButton(
                                            onClick = { showCoordinateEditor = !showCoordinateEditor },
                                            modifier = Modifier.testTag("toggle_manual_coords_button")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.EditLocationAlt,
                                                contentDescription = null,
                                                modifier = Modifier.size(metrics.iconSmall)
                                            )
                                            Spacer(Modifier.width(4.dp))
                                            Text(
                                                text = if (showCoordinateEditor) "Hide" else "Edit Coords",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    AnimatedVisibility(visible = showCoordinateEditor) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            OutlinedTextField(
                                                value = latInput,
                                                onValueChange = { latInput = it },
                                                label = { Text("Latitude") },
                                                singleLine = true,
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                                shape = RoundedCornerShape(10.dp),
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .testTag("manual_lat_input")
                                            )
                                            OutlinedTextField(
                                                value = lngInput,
                                                onValueChange = { lngInput = it },
                                                label = { Text("Longitude") },
                                                singleLine = true,
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                                shape = RoundedCornerShape(10.dp),
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .testTag("manual_lng_input")
                                            )
                                            Button(
                                                onClick = {
                                                    val parsedLat = latInput.toDoubleOrNull()?.coerceIn(-85.0, 85.0)
                                                    val parsedLng = lngInput.toDoubleOrNull()?.coerceIn(-180.0, 180.0)
                                                    if (parsedLat != null && parsedLng != null) {
                                                        coordStatusHint = null
                                                        pinLatitude = parsedLat
                                                        pinLongitude = parsedLng
                                                        moveMapToPoint?.invoke(parsedLat, parsedLng)
                                                    } else {
                                                        coordStatusHint = "Enter valid decimal latitude & longitude."
                                                    }
                                                },
                                                shape = RoundedCornerShape(10.dp),
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = tokens.deliveryGreenDeep,
                                                    contentColor = Color.White
                                                ),
                                                modifier = Modifier
                                                    .defaultMinSize(minHeight = 48.dp)
                                                    .testTag("apply_manual_coords_button")
                                            ) {
                                                Text("Pin")
                                            }
                                        }
                                    }

                                    coordStatusHint?.let { hint ->
                                        Text(
                                            text = hint,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = tokens.muted
                                        )
                                    }
                                }
                            }
                        }

                        // Quick Jump Hub / Area Pills
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "Quick Jump to Service Area",
                                style = MaterialTheme.typography.labelMedium,
                                color = tokens.muted
                            )
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(neighborhoods, key = { it.id }) { hub ->
                                    val active = neighborhoodId == hub.id
                                    Surface(
                                        shape = RoundedCornerShape(50),
                                        color = if (active) tokens.deliveryGreenSoft else tokens.surfaceElevated,
                                        border = BorderStroke(
                                            1.dp,
                                            if (active) tokens.deliveryGreen else tokens.border
                                        ),
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(50))
                                            .clickable {
                                                val (lat, lng) = AddressPinResolver.defaultCoordinateForNeighborhood(hub.id)
                                                coordStatusHint = null
                                                streetManuallyEdited = false
                                                landmarkManuallyEdited = false
                                                pinLatitude = lat
                                                pinLongitude = lng
                                                moveMapToPoint?.invoke(lat, lng)
                                            }
                                            .testTag("quick_jump_hub_${hub.id}")
                                    ) {
                                        Text(
                                            text = "${hub.name} • ${hub.city}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (active) tokens.deliveryGreenDeep else tokens.ink,
                                            fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // Auto-Generated Header + Re-sync Button
                        Surface(
                            color = tokens.brandYellowSoft,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, tokens.brandYellow),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.AutoAwesome,
                                    contentDescription = null,
                                    tint = tokens.ink,
                                    modifier = Modifier.size(metrics.iconSmall)
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Pin-Generated Address Fields",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = tokens.ink,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Source: ${resolvedPin.sourceLabel} • Edit flat/door anytime",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = tokens.muted
                                    )
                                }
                                TextButton(
                                    onClick = {
                                        streetManuallyEdited = false
                                        landmarkManuallyEdited = false
                                        applyResolvedAddress(resolvedPin, forceAllFields = true)
                                    },
                                    modifier = Modifier.testTag("resync_from_pin_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Refresh,
                                        contentDescription = null,
                                        modifier = Modifier.size(metrics.iconSmall)
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text("Re-sync", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        // Flat / House / Building / Street
                        OutlinedTextField(
                            value = streetAddress,
                            onValueChange = {
                                streetAddress = it
                                streetManuallyEdited = true
                                showError = false
                            },
                            label = { Text("Flat / House / Building / Street") },
                            placeholder = { Text("e.g. Flat 402, Palm Grove, 12th Main Rd") },
                            minLines = 2,
                            maxLines = 3,
                            isError = showError,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("add_address_street_input")
                        )

                        // Area / Locality
                        OutlinedTextField(
                            value = area,
                            onValueChange = { area = it },
                            label = { Text("Area / Locality / Sector") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("add_address_area_input")
                        )

                        // City + State Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = city,
                                onValueChange = { city = it },
                                label = { Text("City") },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("add_address_city_input")
                            )
                            OutlinedTextField(
                                value = stateName,
                                onValueChange = { stateName = it },
                                label = { Text("State / Region") },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("add_address_state_input")
                            )
                        }

                        // PIN / Postal Code + Country Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = postalCode,
                                onValueChange = { postalCode = it },
                                label = { Text("PIN / Postal Code") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("add_address_postal_input")
                            )
                            OutlinedTextField(
                                value = country,
                                onValueChange = { country = it },
                                label = { Text("Country") },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("add_address_country_input")
                            )
                        }

                        // Landmark (Optional)
                        OutlinedTextField(
                            value = landmark,
                            onValueChange = {
                                landmark = it
                                landmarkManuallyEdited = true
                            },
                            label = { Text("Nearby Landmark (Optional)") },
                            placeholder = { Text("e.g. Near Metro Station") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("add_address_landmark_input")
                        )

                        // Save As Chips
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "Save Address As",
                                style = MaterialTheme.typography.labelMedium,
                                color = tokens.muted
                            )
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf("Home", "Work", "Other").forEach { tag ->
                                    val active = selectedLabel == tag
                                    Surface(
                                        shape = RoundedCornerShape(50),
                                        color = if (active) tokens.brandYellow else tokens.surfaceElevated,
                                        border = BorderStroke(
                                            1.dp,
                                            if (active) tokens.brandYellow else tokens.border
                                        ),
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(50))
                                            .clickable { selectedLabel = tag }
                                    ) {
                                        Text(
                                            text = tag,
                                            style = MaterialTheme.typography.labelMedium,
                                            color = if (active) tokens.onBrandYellow else tokens.ink,
                                            fontWeight = FontWeight.SemiBold,
                                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                        )
                                    }
                                }
                            }
                        }

                        if (showError) {
                            Text(
                                text = "Please enter your house/building and street address.",
                                style = MaterialTheme.typography.bodySmall,
                                color = tokens.alertRed
                            )
                        }
                    }

                    HorizontalDivider(color = tokens.border)

                    // Bottom Action Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = metrics.cardPadding, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                        ) {
                            Text("Cancel", color = tokens.muted)
                        }
                        Button(
                            onClick = {
                                if (streetAddress.isBlank()) {
                                    showError = true
                                } else {
                                    onSave(
                                        selectedLabel,
                                        streetAddress.trim(),
                                        landmark.trim(),
                                        neighborhoodId,
                                        area.trim(),
                                        city.trim(),
                                        stateName.trim(),
                                        postalCode.trim(),
                                        country.trim().ifBlank { "India" },
                                        pinLatitude,
                                        pinLongitude
                                    )
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = tokens.brandYellow,
                                contentColor = tokens.onBrandYellow
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .defaultMinSize(minHeight = 48.dp)
                                .testTag("save_address_confirm_button")
                        ) {
                            Text(
                                text = "Confirm Pin & Save",
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AddressPinPickerMapSurface(
    initialLat: Double,
    initialLng: Double,
    mapHeight: androidx.compose.ui.unit.Dp,
    isDraggingPin: Boolean,
    isResolvingPin: Boolean,
    onMapMoved: (lat: Double, lng: Double, dragging: Boolean) -> Unit,
    onRegisterMover: ((Double, Double) -> Unit) -> Unit,
    onUseCurrentLocation: () -> Unit
) {
    val tokens = QuickKartThemeTokens.colors
    val metrics = QuickKartMetricsTokens.metrics
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

    val lightPastelTileFilter = remember {
        val cm = ColorMatrix().apply { setSaturation(0.75f) }
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

    val mapView = remember(context) {
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
            isTilesScaledToDpi = true
            minZoomLevel = 4.0
            maxZoomLevel = 19.5
            controller.setZoom(16.2)
            controller.setCenter(OsmGeoPoint(initialLat, initialLng))

            setOnTouchListener { view, _ ->
                view.parent?.requestDisallowInterceptTouchEvent(true)
                false
            }

            overlays.add(
                MapEventsOverlay(object : MapEventsReceiver {
                    override fun singleTapConfirmedHelper(p: OsmGeoPoint?): Boolean {
                        if (p != null) {
                            controller.animateTo(p)
                            onMapMoved(p.latitude, p.longitude, false)
                        }
                        return true
                    }

                    override fun longPressHelper(p: OsmGeoPoint?): Boolean {
                        if (p != null) {
                            controller.animateTo(p)
                            onMapMoved(p.latitude, p.longitude, false)
                        }
                        return true
                    }
                })
            )

            addMapListener(object : MapListener {
                override fun onScroll(event: ScrollEvent?): Boolean {
                    val center = mapCenter
                    onMapMoved(center.latitude, center.longitude, true)
                    return false
                }

                override fun onZoom(event: ZoomEvent?): Boolean {
                    val center = mapCenter
                    onMapMoved(center.latitude, center.longitude, false)
                    return false
                }
            })
        }
    }

    LaunchedEffect(mapView) {
        onRegisterMover { lat, lng ->
            mapView.controller.animateTo(OsmGeoPoint(lat, lng))
            if (mapView.zoomLevelDouble < 14.5) {
                mapView.controller.setZoom(16.0)
            }
        }
    }

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

    val pinLift by animateDpAsState(
        targetValue = if (isDraggingPin) (-10).dp else 0.dp,
        animationSpec = tween(180),
        label = "pin_lift_anim"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(mapHeight)
            .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp))
            .background(if (tokens.isDark) Color(0xFF0E141D) else Color(0xFFEAF0F6))
    ) {
        AndroidView(
            factory = { mapView },
            update = { mv ->
                mv.overlayManager.tilesOverlay.setColorFilter(
                    if (tokens.isDark) TilesOverlay.INVERT_COLORS else lightPastelTileFilter
                )
            },
            modifier = Modifier.fillMaxSize()
        )

        // Center Fixed Graphical Drop Pin Overlay
        Box(
            modifier = Modifier.align(Alignment.Center),
            contentAlignment = Alignment.BottomCenter
        ) {
            // Ground target shadow dot
            Box(
                modifier = Modifier
                    .size(width = 16.dp, height = 6.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.28f))
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.offset(y = (-6).dp + pinLift)
            ) {
                Surface(
                    color = Color(0xFF141414),
                    shape = RoundedCornerShape(50),
                    shadowElevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        if (isResolvingPin) {
                            CircularProgressIndicator(
                                color = tokens.brandYellow,
                                strokeWidth = 1.8.dp,
                                modifier = Modifier.size(11.dp)
                            )
                        }
                        Text(
                            text = if (isResolvingPin) "Reading pin..." else "Deliver here",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(Modifier.height(2.dp))
                Icon(
                    imageVector = Icons.Rounded.LocationOn,
                    contentDescription = "Selected map drop pin",
                    tint = tokens.deliveryGreenDeep,
                    modifier = Modifier.size(38.dp)
                )
            }
        }

        // Top-left drag instruction hint
        Surface(
            color = tokens.surface.copy(alpha = 0.94f),
            shape = RoundedCornerShape(50),
            border = BorderStroke(1.dp, tokens.border),
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(10.dp)
        ) {
            Text(
                text = "Drag or tap map to move pin",
                style = MaterialTheme.typography.labelSmall,
                color = tokens.ink,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
            )
        }

        // Top-right Use Current GPS Pill
        Surface(
            color = tokens.surface.copy(alpha = 0.96f),
            shape = RoundedCornerShape(50),
            border = BorderStroke(1.dp, tokens.deliveryGreen),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(10.dp)
                .shadow(4.dp, RoundedCornerShape(50))
                .clip(RoundedCornerShape(50))
                .clickable(onClick = onUseCurrentLocation)
                .testTag("use_current_gps_pin_button")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.MyLocation,
                    contentDescription = "Use current GPS location",
                    tint = tokens.deliveryGreenDeep,
                    modifier = Modifier.size(metrics.iconSmall)
                )
                Text(
                    text = "Use GPS",
                    style = MaterialTheme.typography.labelSmall,
                    color = tokens.deliveryGreenDeep,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Bottom-right Zoom In / Out controls
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
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(40.dp)
                        .clickable { mapView.controller.zoomIn() }
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = "Zoom in map",
                        tint = tokens.ink,
                        modifier = Modifier.size(metrics.iconSmall)
                    )
                }
                HorizontalDivider(modifier = Modifier.width(24.dp), color = tokens.border)
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(40.dp)
                        .clickable { mapView.controller.zoomOut() }
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Remove,
                        contentDescription = "Zoom out map",
                        tint = tokens.ink,
                        modifier = Modifier.size(metrics.iconSmall)
                    )
                }
            }
        }
    }
}

private fun formatCoordinateDisplay(lat: Double, lng: Double): String {
    val latDir = if (lat >= 0) "N" else "S"
    val lngDir = if (lng >= 0) "E" else "W"
    return String.format(Locale.US, "%.4f° %s, %.4f° %s", abs(lat), latDir, abs(lng), lngDir)
}

@SuppressLint("MissingPermission")
private fun fetchCurrentDeviceCoordinate(
    context: Context,
    onSuccess: (Double, Double) -> Unit,
    onError: (String) -> Unit
) {
    val client = LocationServices.getFusedLocationProviderClient(context)
    val cts = CancellationTokenSource()
    client.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token)
        .addOnSuccessListener { location ->
            if (location != null) {
                onSuccess(location.latitude, location.longitude)
            } else {
                client.lastLocation.addOnSuccessListener { lastLoc ->
                    if (lastLoc != null) {
                        onSuccess(lastLoc.latitude, lastLoc.longitude)
                    } else {
                        onError("GPS signal unavailable — drag map pin to set your location.")
                    }
                }.addOnFailureListener {
                    onError("Could not acquire GPS fix.")
                }
            }
        }
        .addOnFailureListener {
            onError("Location services unavailable on this device.")
        }
}
