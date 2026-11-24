package com.example.util

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Build
import com.example.data.model.ResolvedPinAddress
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

object AddressPinResolver {

    data class HubGeoZone(
        val neighborhoodId: String,
        val area: String,
        val subAreas: List<String>,
        val city: String,
        val state: String,
        val postalBase: Int,
        val country: String,
        val lat: Double,
        val lng: Double,
        val streets: List<String>,
        val landmarks: List<String>
    )

    private val hubZones = listOf(
        HubGeoZone(
            neighborhoodId = "indiranagar",
            area = "Indiranagar",
            subAreas = listOf("HAL 2nd Stage", "12th Main Indiranagar", "100 Feet Road", "CMH Road"),
            city = "Bengaluru",
            state = "Karnataka",
            postalBase = 560038,
            country = "India",
            lat = 12.9716,
            lng = 77.6466,
            streets = listOf("12th Main Rd", "100 Feet Rd", "CMH Road", "80 Feet Rd"),
            landmarks = listOf("Near CMH Park Metro", "Opposite 100ft Rd Junction", "Near Indiranagar Club")
        ),
        HubGeoZone(
            neighborhoodId = "koramangala",
            area = "Koramangala",
            subAreas = listOf("Koramangala 4th Block", "Koramangala 6th Block", "Koramangala 1st Block", "Ejipura"),
            city = "Bengaluru",
            state = "Karnataka",
            postalBase = 560034,
            country = "India",
            lat = 12.9279,
            lng = 77.6271,
            streets = listOf("80 Feet Rd", "Laskar Hosur Rd", "17th Main Rd", "100 Feet Inner Ring Rd"),
            landmarks = listOf("Opposite Sony Signal", "Near BDA Complex", "Near Koramangala Club")
        ),
        HubGeoZone(
            neighborhoodId = "bandra_west",
            area = "Bandra West",
            subAreas = listOf("Pali Hill", "16th Road Bandra", "Carter Road", "Chapel Road"),
            city = "Mumbai",
            state = "Maharashtra",
            postalBase = 400050,
            country = "India",
            lat = 19.0544,
            lng = 72.8322,
            streets = listOf("16th Road", "Pali Hill Rd", "Dr Ambedkar Rd", "Hill Road"),
            landmarks = listOf("Behind Pali Market", "Near Bandra Gymkhana", "Off Carter Promenade")
        ),
        HubGeoZone(
            neighborhoodId = "hauz_khas",
            area = "Hauz Khas",
            subAreas = listOf("Hauz Khas Enclave", "Green Park", "Safdarjung Dev Area", "Yusuf Sarai"),
            city = "New Delhi",
            state = "Delhi",
            postalBase = 110016,
            country = "India",
            lat = 28.5546,
            lng = 77.2066,
            streets = listOf("Sri Aurobindo Marg", "Hauz Khas Enclave Rd", "August Kranti Marg", "Green Park Main Rd"),
            landmarks = listOf("Near Hauz Khas Metro Gate 1", "Opposite Deer Park Gate", "Near Aurobindo Market")
        ),
        HubGeoZone(
            neighborhoodId = "jubilee_hills",
            area = "Jubilee Hills",
            subAreas = listOf("Road No. 36 Jubilee Hills", "Film Nagar", "Kavuri Hills", "Madhapur"),
            city = "Hyderabad",
            state = "Telangana",
            postalBase = 500033,
            country = "India",
            lat = 17.4258,
            lng = 78.4142,
            streets = listOf("Road No. 36", "Road No. 45", "KBR Park Perimeter Rd", "Kavuri Hills Link Rd"),
            landmarks = listOf("Near Jubilee Hills Checkpost", "Near Peddamma Temple Metro", "Opposite KBR Park")
        )
    )

    private data class RegionFallback(
        val minLat: Double,
        val maxLat: Double,
        val minLng: Double,
        val maxLng: Double,
        val city: String,
        val state: String,
        val postalBase: Int,
        val country: String = "India"
    )

    private val broaderRegions = listOf(
        RegionFallback(12.75, 13.20, 77.35, 77.85, "Bengaluru", "Karnataka", 560001),
        RegionFallback(18.85, 19.35, 72.75, 73.15, "Mumbai", "Maharashtra", 400001),
        RegionFallback(18.40, 18.70, 73.70, 74.05, "Pune", "Maharashtra", 411001),
        RegionFallback(28.40, 28.88, 76.85, 77.50, "New Delhi", "Delhi", 110001),
        RegionFallback(17.25, 17.60, 78.25, 78.65, "Hyderabad", "Telangana", 500001),
        RegionFallback(12.85, 13.25, 80.10, 80.35, "Chennai", "Tamil Nadu", 600001),
        RegionFallback(22.40, 22.72, 88.20, 88.50, "Kolkata", "West Bengal", 700001),
        RegionFallback(6.0, 36.0, 68.0, 97.5, "Metro Zone", "India", 560001)
    )

    fun defaultCoordinateForNeighborhood(neighborhoodId: String): Pair<Double, Double> {
        val zone = hubZones.find { it.neighborhoodId == neighborhoodId } ?: hubZones.first()
        return zone.lat to zone.lng
    }

    fun haversineKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0
        val dLat = (lat2 - lat1) * PI / 180.0
        val dLon = (lon2 - lon1) * PI / 180.0
        val a = sin(dLat / 2) * sin(dLat / 2) +
            cos(lat1 * PI / 180.0) * cos(lat2 * PI / 180.0) *
            sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }

    /**
     * Pure deterministic reverse-coordinate resolver so pin dragging/tapping always produces
     * structured Area, City, State, PIN/Postal Code, Country, Street & Landmark fields.
     */
    fun resolveFromCoordinateGrid(latitude: Double, longitude: Double): ResolvedPinAddress {
        val nearestHub = hubZones.minByOrNull { haversineKm(latitude, longitude, it.lat, it.lng) }
            ?: hubZones.first()
        val distanceKm = haversineKm(latitude, longitude, nearestHub.lat, nearestHub.lng)

        // Stable coordinate-based index so slight pin moves pick distinct local roads/blocks
        val latBucket = ((latitude * 1000).roundToInt()).let { abs(it) }
        val lngBucket = ((longitude * 1000).roundToInt()).let { abs(it) }
        val hashIdx = (latBucket * 31 + lngBucket * 17)

        if (distanceKm <= 18.0) {
            val street = nearestHub.streets[hashIdx % nearestHub.streets.size]
            val subArea = nearestHub.subAreas[(hashIdx / 2) % nearestHub.subAreas.size]
            val landmark = nearestHub.landmarks[(hashIdx / 3) % nearestHub.landmarks.size]
            val plotNum = 10 + (hashIdx % 89)
            val postalOffset = (hashIdx % 7)
            val postalCode = (nearestHub.postalBase + postalOffset).toString()

            return ResolvedPinAddress(
                latitude = latitude,
                longitude = longitude,
                suggestedStreet = "Plot $plotNum, $street, $subArea",
                area = subArea,
                city = nearestHub.city,
                state = nearestHub.state,
                postalCode = postalCode,
                country = nearestHub.country,
                suggestedLandmark = landmark,
                nearestNeighborhoodId = nearestHub.neighborhoodId,
                sourceLabel = "QuickKart Pin Grid"
            )
        }

        val region = broaderRegions.firstOrNull {
            latitude in it.minLat..it.maxLat && longitude in it.minLng..it.maxLng
        }

        if (region != null) {
            val sectorNo = 1 + (hashIdx % 24)
            val blockChar = ('A' + (hashIdx % 8))
            val postal = (region.postalBase + (hashIdx % 45)).toString()
            return ResolvedPinAddress(
                latitude = latitude,
                longitude = longitude,
                suggestedStreet = "Block $blockChar-${10 + (hashIdx % 80)}, Sector $sectorNo Main Rd",
                area = "Sector $sectorNo, ${region.city}",
                city = region.city,
                state = region.state,
                postalCode = postal,
                country = region.country,
                suggestedLandmark = "Near Sector $sectorNo Central Plaza",
                nearestNeighborhoodId = nearestHub.neighborhoodId,
                sourceLabel = "QuickKart Pin Grid"
            )
        }

        // Global fallback outside India
        val sectorNo = 1 + (hashIdx % 40)
        val latHem = if (latitude >= 0) "N" else "S"
        val lngHem = if (longitude >= 0) "E" else "W"
        val formattedPin = String.format(Locale.US, "%05d", 10000 + (hashIdx % 89999))
        return ResolvedPinAddress(
            latitude = latitude,
            longitude = longitude,
            suggestedStreet = "${10 + (hashIdx % 190)} Central Avenue, District $sectorNo",
            area = "District $sectorNo (${String.format(Locale.US, "%.2f%s", abs(latitude), latHem)})",
            city = "Metro District $sectorNo",
            state = "Zone ${String.format(Locale.US, "%.1f°%s %.1f°%s", abs(latitude), latHem, abs(longitude), lngHem)}",
            postalCode = formattedPin,
            country = "International",
            suggestedLandmark = "Near District $sectorNo Hub",
            nearestNeighborhoodId = nearestHub.neighborhoodId,
            sourceLabel = "Pin Coordinates"
        )
    }

    /**
     * Uses Android's native Geocoder on-device when available and merges live street/area/state/PIN
     * fields over the deterministic fallback so every field is cleanly filled even offline.
     */
    suspend fun resolveCoordinate(
        context: Context,
        latitude: Double,
        longitude: Double
    ): ResolvedPinAddress {
        val fallback = resolveFromCoordinateGrid(latitude, longitude)
        val liveAddress = queryPlatformGeocoder(context, latitude, longitude) ?: return fallback

        val streetLine = listOfNotNull(
            liveAddress.subThoroughfare?.takeIf { it.isNotBlank() },
            liveAddress.thoroughfare?.takeIf { it.isNotBlank() },
            liveAddress.featureName?.takeIf {
                it.isNotBlank() && it != liveAddress.postalCode && it != liveAddress.adminArea
            }
        ).distinct().joinToString(", ")

        val resolvedArea = liveAddress.subLocality?.takeIf { it.isNotBlank() }
            ?: liveAddress.subAdminArea?.takeIf { it.isNotBlank() }
            ?: fallback.area

        val resolvedCity = liveAddress.locality?.takeIf { it.isNotBlank() }
            ?: liveAddress.subAdminArea?.takeIf { it.isNotBlank() }
            ?: fallback.city

        val resolvedState = liveAddress.adminArea?.takeIf { it.isNotBlank() }
            ?: fallback.state

        val resolvedPostal = liveAddress.postalCode?.takeIf { it.isNotBlank() }
            ?: fallback.postalCode

        val resolvedCountry = liveAddress.countryName?.takeIf { it.isNotBlank() }
            ?: fallback.country

        val resolvedStreet = when {
            streetLine.isNotBlank() -> streetLine
            liveAddress.getAddressLine(0)?.isNotBlank() == true -> {
                liveAddress.getAddressLine(0).split(",").take(2).joinToString(",").trim()
            }
            else -> fallback.suggestedStreet
        }

        val resolvedLandmark = liveAddress.premises?.takeIf { it.isNotBlank() }
            ?: liveAddress.thoroughfare?.takeIf { it.isNotBlank() }?.let { "Near $it" }
            ?: fallback.suggestedLandmark

        return fallback.copy(
            suggestedStreet = resolvedStreet,
            area = resolvedArea,
            city = resolvedCity,
            state = resolvedState,
            postalCode = resolvedPostal,
            country = resolvedCountry,
            suggestedLandmark = resolvedLandmark,
            sourceLabel = "Live Map Geocoder"
        )
    }

    private suspend fun queryPlatformGeocoder(
        context: Context,
        latitude: Double,
        longitude: Double
    ): Address? {
        if (!Geocoder.isPresent()) return null
        return withTimeoutOrNull(2500L) {
            withContext(Dispatchers.IO) {
                runCatching {
                    val geocoder = Geocoder(context, Locale.getDefault())
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        suspendCancellableCoroutine { cont ->
                            geocoder.getFromLocation(
                                latitude,
                                longitude,
                                1,
                                object : Geocoder.GeocodeListener {
                                    override fun onGeocode(addresses: MutableList<Address>) {
                                        if (cont.isActive) {
                                            cont.resume(addresses.firstOrNull())
                                        }
                                    }

                                    override fun onError(errorMessage: String?) {
                                        if (cont.isActive) {
                                            cont.resume(null)
                                        }
                                    }
                                }
                            )
                        }
                    } else {
                        @Suppress("DEPRECATION")
                        geocoder.getFromLocation(latitude, longitude, 1)?.firstOrNull()
                    }
                }.getOrNull()
            }
        }
    }
}
