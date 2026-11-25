package com.example.rider.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.rider.data.GeoPoint
import com.example.ui.theme.QuickKartThemeTokens
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import java.io.File
import org.osmdroid.util.GeoPoint as OsmPoint

enum class PinKind { STORE, CUSTOMER }

/**
 * OpenStreetMap view for the rider: optional route (origin → destination with a street-like
 * dog-leg, no routing API in the prototype), destination pin and the rider marker.
 */
@Composable
fun RiderMap(
    rider: GeoPoint,
    modifier: Modifier = Modifier,
    routeFrom: GeoPoint? = null,
    routeTo: GeoPoint? = null,
    destinationKind: PinKind = PinKind.STORE,
    extraPins: List<Pair<GeoPoint, PinKind>> = emptyList(),
    zoom: Double = 15.0
) {
    val context = LocalContext.current
    val tokens = QuickKartThemeTokens.colors
    val lifecycleOwner = LocalLifecycleOwner.current
    remember(context) {
        Configuration.getInstance().apply {
            userAgentValue = "${context.packageName}/1.0"
            osmdroidBasePath = File(context.cacheDir, "osmdroid")
            osmdroidTileCache = File(context.cacheDir, "osmdroid/tiles")
        }
        true
    }
    val tileFilter = remember {
        // Same calmer basemap as the customer app: desaturated, slightly brighter tiles.
        val cm = ColorMatrix().apply { setSaturation(0.45f) }
        cm.postConcat(ColorMatrix(floatArrayOf(1.03f, 0f, 0f, 0f, 6f, 0f, 1.03f, 0f, 0f, 6f, 0f, 0f, 1.02f, 0f, 4f, 0f, 0f, 0f, 1f, 0f)))
        ColorMatrixColorFilter(cm)
    }
    val mapView = remember(context) {
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
            isTilesScaledToDpi = true
            minZoomLevel = 11.0
            maxZoomLevel = 19.0
            controller.setZoom(zoom)
            controller.setCenter(OsmPoint(rider.lat, rider.lng))
            overlayManager.tilesOverlay.setColorFilter(tileFilter)
            setOnTouchListener { v, _ -> v.parent?.requestDisallowInterceptTouchEvent(true); false }
        }
    }
    val casing = remember(mapView) { Polyline(mapView).apply { outlinePaint.color = android.graphics.Color.WHITE; outlinePaint.strokeWidth = 18f; outlinePaint.strokeCap = Paint.Cap.ROUND; outlinePaint.strokeJoin = Paint.Join.ROUND } }
    val route = remember(mapView) { Polyline(mapView).apply { outlinePaint.color = android.graphics.Color.rgb(20, 20, 20); outlinePaint.strokeWidth = 10f; outlinePaint.strokeCap = Paint.Cap.ROUND; outlinePaint.strokeJoin = Paint.Join.ROUND } }
    val riderMarker = remember(mapView) { Marker(mapView).apply { setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER); icon = BitmapDrawable(context.resources, riderBitmap(context)); setInfoWindow(null) } }
    val destMarker = remember(mapView) { Marker(mapView).apply { setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM); setInfoWindow(null) } }
    val extraMarkers = remember(mapView) { mutableListOf<Marker>() }
    remember(mapView) { mapView.overlays.addAll(listOf(casing, route, destMarker, riderMarker)); true }

    DisposableEffect(mapView, lifecycleOwner) {
        val obs = LifecycleEventObserver { _, e -> if (e == Lifecycle.Event.ON_RESUME) mapView.onResume() else if (e == Lifecycle.Event.ON_PAUSE) mapView.onPause() }
        lifecycleOwner.lifecycle.addObserver(obs)
        mapView.onResume()
        onDispose { lifecycleOwner.lifecycle.removeObserver(obs); mapView.onPause(); mapView.onDetach() }
    }

    // Fit the route once per destination.
    LaunchedEffect(routeFrom, routeTo) {
        if (routeFrom != null && routeTo != null) {
            val box = BoundingBox.fromGeoPoints(listOf(OsmPoint(routeFrom.lat, routeFrom.lng), OsmPoint(routeTo.lat, routeTo.lng)))
            mapView.post { runCatching { mapView.zoomToBoundingBox(box.increaseByScale(1.6f), false) } }
        } else {
            mapView.controller.setCenter(OsmPoint(rider.lat, rider.lng))
        }
    }

    Box(modifier.background(if (tokens.isDark) Color(0xFF0E141D) else Color(0xFFEAF0F6))) {
        AndroidView(factory = { mapView }, update = { mv ->
            riderMarker.position = OsmPoint(rider.lat, rider.lng)
            if (routeFrom != null && routeTo != null) {
                val mid = OsmPoint(routeFrom.lat, routeTo.lng) // L-shaped street-like path
                val pts = listOf(OsmPoint(routeFrom.lat, routeFrom.lng), mid, OsmPoint(routeTo.lat, routeTo.lng))
                casing.setPoints(pts); route.setPoints(pts)
                destMarker.position = OsmPoint(routeTo.lat, routeTo.lng)
                destMarker.icon = BitmapDrawable(context.resources, pinBitmap(context, destinationKind))
                destMarker.setVisible(true)
            } else {
                casing.setPoints(emptyList()); route.setPoints(emptyList()); destMarker.setVisible(false)
            }
            extraMarkers.forEach { mv.overlays.remove(it) }
            extraMarkers.clear()
            extraPins.forEach { (p, kind) ->
                val m = Marker(mv).apply { position = OsmPoint(p.lat, p.lng); setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM); icon = BitmapDrawable(context.resources, pinBitmap(context, kind)); setInfoWindow(null) }
                extraMarkers += m; mv.overlays.add(m)
            }
            mv.invalidate()
        }, modifier = Modifier)
        Text("© OpenStreetMap", style = MaterialTheme.typography.labelSmall, color = Color(0xFF555555),
            modifier = Modifier.align(Alignment.BottomStart).padding(6.dp).background(Color.White.copy(alpha = 0.7f)).padding(horizontal = 4.dp))
    }
}

private fun dp(context: Context, v: Float) = v * context.resources.displayMetrics.density

/** Yellow disc with a black ring and a small black arrow: the rider. */
private fun riderBitmap(context: Context): Bitmap {
    val s = dp(context, 34f).toInt()
    val bmp = Bitmap.createBitmap(s, s, Bitmap.Config.ARGB_8888)
    val c = Canvas(bmp)
    val p = Paint(Paint.ANTI_ALIAS_FLAG)
    p.color = android.graphics.Color.argb(60, 255, 212, 0); c.drawCircle(s / 2f, s / 2f, s / 2f, p)
    p.color = android.graphics.Color.rgb(20, 20, 20); c.drawCircle(s / 2f, s / 2f, s * 0.32f, p)
    p.color = android.graphics.Color.rgb(255, 212, 0); c.drawCircle(s / 2f, s / 2f, s * 0.25f, p)
    p.color = android.graphics.Color.rgb(20, 20, 20); c.drawCircle(s / 2f, s / 2f, s * 0.09f, p)
    return bmp
}

/** Teardrop pin: store = black with yellow dot, customer = yellow with black dot. */
private fun pinBitmap(context: Context, kind: PinKind): Bitmap {
    val w = dp(context, 30f).toInt(); val h = dp(context, 40f).toInt()
    val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
    val c = Canvas(bmp)
    val p = Paint(Paint.ANTI_ALIAS_FLAG)
    val body = if (kind == PinKind.STORE) android.graphics.Color.rgb(20, 20, 20) else android.graphics.Color.rgb(255, 212, 0)
    val dot = if (kind == PinKind.STORE) android.graphics.Color.rgb(255, 212, 0) else android.graphics.Color.rgb(20, 20, 20)
    val r = w / 2f
    val path = android.graphics.Path().apply {
        addCircle(r, r, r - 2, android.graphics.Path.Direction.CW)
        moveTo(r * 0.45f, r * 1.6f); lineTo(r, h.toFloat() - 1); lineTo(r * 1.55f, r * 1.6f); close()
    }
    p.color = android.graphics.Color.WHITE; p.style = Paint.Style.STROKE; p.strokeWidth = dp(context, 3f); c.drawPath(path, p)
    p.style = Paint.Style.FILL; p.color = body; c.drawPath(path, p)
    p.color = dot; c.drawCircle(r, r, r * 0.38f, p)
    return bmp
}
