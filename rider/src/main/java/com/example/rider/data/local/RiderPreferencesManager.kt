package com.example.rider.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.rider.data.model.RiderVehicleType
import com.example.rider.ui.theme.DEFAULT_SCREEN_ZOOM_PERCENT
import com.example.rider.ui.theme.MAX_SCREEN_ZOOM_PERCENT
import com.example.rider.ui.theme.MIN_SCREEN_ZOOM_PERCENT
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.riderDataStore by preferencesDataStore(name = "quickkart_rider_prefs")

data class RiderPreferences(
    val isLoggedIn: Boolean = false,
    val riderName: String = "Arjun Kumar",
    val riderPhone: String = "+91 98452 31090",
    val fleetId: String = "QKR-BLR-248",
    val vehicleType: RiderVehicleType = RiderVehicleType.ELECTRIC_SCOOTER,
    val hubId: String = "hub_indiranagar",
    val isOnline: Boolean = true,
    val isDarkTheme: Boolean = false,
    val notificationsEnabled: Boolean = true,
    val screenZoomPercent: Int = DEFAULT_SCREEN_ZOOM_PERCENT,
    val checklistHelmet: Boolean = true,
    val checklistThermalBag: Boolean = true,
    val checklistBatteryOk: Boolean = true
)

class RiderPreferencesManager(private val context: Context) {

    private object Keys {
        val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        val RIDER_NAME = stringPreferencesKey("rider_name")
        val RIDER_PHONE = stringPreferencesKey("rider_phone")
        val FLEET_ID = stringPreferencesKey("fleet_id")
        val VEHICLE_TYPE = stringPreferencesKey("vehicle_type")
        val HUB_ID = stringPreferencesKey("hub_id")
        val IS_ONLINE = booleanPreferencesKey("is_online")
        val IS_DARK_THEME = booleanPreferencesKey("is_dark_theme")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val SCREEN_ZOOM_PERCENT = intPreferencesKey("screen_zoom_percent")
        val CHECKLIST_HELMET = booleanPreferencesKey("checklist_helmet")
        val CHECKLIST_BAG = booleanPreferencesKey("checklist_bag")
        val CHECKLIST_BATTERY = booleanPreferencesKey("checklist_battery")
    }

    val preferencesFlow: Flow<RiderPreferences> = context.riderDataStore.data.map { prefs ->
        val rawVehicle = prefs[Keys.VEHICLE_TYPE] ?: RiderVehicleType.ELECTRIC_SCOOTER.name
        val vehicle = runCatching { RiderVehicleType.valueOf(rawVehicle) }
            .getOrDefault(RiderVehicleType.ELECTRIC_SCOOTER)

        RiderPreferences(
            isLoggedIn = prefs[Keys.IS_LOGGED_IN] ?: false,
            riderName = prefs[Keys.RIDER_NAME] ?: "Arjun Kumar",
            riderPhone = prefs[Keys.RIDER_PHONE] ?: "+91 98452 31090",
            fleetId = prefs[Keys.FLEET_ID] ?: "QKR-BLR-248",
            vehicleType = vehicle,
            hubId = prefs[Keys.HUB_ID] ?: "hub_indiranagar",
            isOnline = prefs[Keys.IS_ONLINE] ?: true,
            isDarkTheme = prefs[Keys.IS_DARK_THEME] ?: false,
            notificationsEnabled = prefs[Keys.NOTIFICATIONS_ENABLED] ?: true,
            screenZoomPercent = (prefs[Keys.SCREEN_ZOOM_PERCENT] ?: DEFAULT_SCREEN_ZOOM_PERCENT)
                .coerceIn(MIN_SCREEN_ZOOM_PERCENT, MAX_SCREEN_ZOOM_PERCENT),
            checklistHelmet = prefs[Keys.CHECKLIST_HELMET] ?: true,
            checklistThermalBag = prefs[Keys.CHECKLIST_BAG] ?: true,
            checklistBatteryOk = prefs[Keys.CHECKLIST_BATTERY] ?: true
        )
    }

    suspend fun loginRider(
        name: String,
        phone: String,
        fleetId: String,
        vehicleType: RiderVehicleType,
        hubId: String
    ) {
        context.riderDataStore.edit { prefs ->
            prefs[Keys.IS_LOGGED_IN] = true
            prefs[Keys.RIDER_NAME] = name
            prefs[Keys.RIDER_PHONE] = phone
            prefs[Keys.FLEET_ID] = fleetId
            prefs[Keys.VEHICLE_TYPE] = vehicleType.name
            prefs[Keys.HUB_ID] = hubId
            prefs[Keys.IS_ONLINE] = true
        }
    }

    suspend fun logout() {
        context.riderDataStore.edit { prefs ->
            prefs[Keys.IS_LOGGED_IN] = false
            prefs[Keys.IS_ONLINE] = false
        }
    }

    suspend fun setOnline(online: Boolean) {
        context.riderDataStore.edit { prefs ->
            prefs[Keys.IS_ONLINE] = online
        }
    }

    suspend fun setHubId(hubId: String) {
        context.riderDataStore.edit { prefs ->
            prefs[Keys.HUB_ID] = hubId
        }
    }

    suspend fun setVehicleType(vehicleType: RiderVehicleType) {
        context.riderDataStore.edit { prefs ->
            prefs[Keys.VEHICLE_TYPE] = vehicleType.name
        }
    }

    suspend fun setDarkTheme(enabled: Boolean) {
        context.riderDataStore.edit { prefs ->
            prefs[Keys.IS_DARK_THEME] = enabled
        }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.riderDataStore.edit { prefs ->
            prefs[Keys.NOTIFICATIONS_ENABLED] = enabled
        }
    }

    suspend fun setScreenZoomPercent(zoomPercent: Int) {
        context.riderDataStore.edit { prefs ->
            prefs[Keys.SCREEN_ZOOM_PERCENT] = zoomPercent.coerceIn(
                MIN_SCREEN_ZOOM_PERCENT,
                MAX_SCREEN_ZOOM_PERCENT
            )
        }
    }

    suspend fun updateChecklist(helmet: Boolean, thermalBag: Boolean, batteryOk: Boolean) {
        context.riderDataStore.edit { prefs ->
            prefs[Keys.CHECKLIST_HELMET] = helmet
            prefs[Keys.CHECKLIST_BAG] = thermalBag
            prefs[Keys.CHECKLIST_BATTERY] = batteryOk
        }
    }
}
