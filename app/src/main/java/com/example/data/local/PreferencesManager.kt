package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.quickKartDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "quickkart_prefs"
)

data class UserPreferences(
    val isDarkTheme: Boolean = false,
    val notificationsEnabled: Boolean = true,
    val screenZoomPercent: Int = 100,
    val isLoggedIn: Boolean = false,
    val isGuest: Boolean = false,
    val userPhone: String = "",
    val userName: String = "QuickKart Member",
    val userEmail: String = "",
    val selectedNeighborhoodId: String = "indiranagar",
    val selectedAddressId: String = "addr_home"
)

class PreferencesManager(private val context: Context) {

    private object Keys {
        val IS_DARK_THEME = booleanPreferencesKey("is_dark_theme")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val SCREEN_ZOOM_PERCENT = intPreferencesKey("screen_zoom_percent")
        val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        val IS_GUEST = booleanPreferencesKey("is_guest")
        val USER_PHONE = stringPreferencesKey("user_phone")
        val USER_NAME = stringPreferencesKey("user_name")
        val USER_EMAIL = stringPreferencesKey("user_email")
        val SELECTED_NEIGHBORHOOD = stringPreferencesKey("selected_neighborhood")
        val SELECTED_ADDRESS_ID = stringPreferencesKey("selected_address_id")
    }

    val preferencesFlow: Flow<UserPreferences> = context.quickKartDataStore.data.map { prefs ->
        UserPreferences(
            isDarkTheme = prefs[Keys.IS_DARK_THEME] ?: false,
            notificationsEnabled = prefs[Keys.NOTIFICATIONS_ENABLED] ?: true,
            screenZoomPercent = (prefs[Keys.SCREEN_ZOOM_PERCENT] ?: 100).coerceIn(80, 130),
            isLoggedIn = prefs[Keys.IS_LOGGED_IN] ?: false,
            isGuest = prefs[Keys.IS_GUEST] ?: false,
            userPhone = prefs[Keys.USER_PHONE] ?: "",
            userName = prefs[Keys.USER_NAME] ?: "QuickKart Member",
            userEmail = prefs[Keys.USER_EMAIL] ?: "",
            selectedNeighborhoodId = prefs[Keys.SELECTED_NEIGHBORHOOD] ?: "indiranagar",
            selectedAddressId = prefs[Keys.SELECTED_ADDRESS_ID] ?: "addr_home"
        )
    }

    suspend fun setDarkTheme(enabled: Boolean) {
        context.quickKartDataStore.edit { it[Keys.IS_DARK_THEME] = enabled }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.quickKartDataStore.edit { it[Keys.NOTIFICATIONS_ENABLED] = enabled }
    }

    suspend fun setScreenZoomPercent(percent: Int) {
        val normalized = ((percent.coerceIn(80, 130) + 2) / 5) * 5
        context.quickKartDataStore.edit {
            it[Keys.SCREEN_ZOOM_PERCENT] = normalized.coerceIn(80, 130)
        }
    }

    suspend fun setSelectedNeighborhood(neighborhoodId: String) {
        context.quickKartDataStore.edit { it[Keys.SELECTED_NEIGHBORHOOD] = neighborhoodId }
    }

    suspend fun setSelectedAddressId(addressId: String) {
        context.quickKartDataStore.edit { it[Keys.SELECTED_ADDRESS_ID] = addressId }
    }

    suspend fun signInWithPhone(
        phone: String,
        neighborhoodId: String,
        name: String = "Aarav Sharma",
        email: String = ""
    ) {
        context.quickKartDataStore.edit {
            it[Keys.IS_LOGGED_IN] = true
            it[Keys.IS_GUEST] = false
            it[Keys.USER_PHONE] = phone
            it[Keys.USER_NAME] = name.ifBlank { "QuickKart Member" }
            it[Keys.USER_EMAIL] = email
            it[Keys.SELECTED_NEIGHBORHOOD] = neighborhoodId
        }
    }

    suspend fun updateUserProfile(name: String, email: String, phone: String? = null) {
        context.quickKartDataStore.edit {
            if (name.isNotBlank()) it[Keys.USER_NAME] = name.trim()
            it[Keys.USER_EMAIL] = email.trim()
            if (!phone.isNullOrBlank()) it[Keys.USER_PHONE] = phone.trim()
        }
    }

    suspend fun signInAsGuest(neighborhoodId: String) {
        context.quickKartDataStore.edit {
            it[Keys.IS_LOGGED_IN] = true
            it[Keys.IS_GUEST] = true
            it[Keys.USER_PHONE] = ""
            it[Keys.USER_NAME] = "Guest Shopper"
            it[Keys.USER_EMAIL] = ""
            it[Keys.SELECTED_NEIGHBORHOOD] = neighborhoodId
        }
    }

    suspend fun signOut() {
        context.quickKartDataStore.edit {
            it[Keys.IS_LOGGED_IN] = false
            it[Keys.IS_GUEST] = false
            it[Keys.USER_PHONE] = ""
            it[Keys.USER_EMAIL] = ""
        }
    }
}
