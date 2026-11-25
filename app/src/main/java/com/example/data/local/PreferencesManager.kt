package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
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
    val screenZoomPercent: Int = 85, // Compact by default
    val isLoggedIn: Boolean = false,
    val isGuest: Boolean = false,
    val userPhone: String = "",
    val userName: String = "QuickKart Member",
    val userEmail: String = "",
    val selectedNeighborhoodId: String = "indiranagar",
    val selectedAddressId: String = "addr_home",
    /** How the user signed in: [SIGN_IN_PHONE], [SIGN_IN_GOOGLE] or [SIGN_IN_GUEST]. */
    val signInMethod: String = SIGN_IN_PHONE,
    /** Google account linked to this profile (set by Google sign-in or "Link Google account"). */
    val linkedGoogleEmail: String = "",
    /** Most recent first, max [MAX_RECENT_SEARCHES]. */
    val recentSearches: List<String> = emptyList(),
    /** Notifications newer than this are shown as unread in the inbox. */
    val inboxLastSeenMillis: Long = 0L,
    /** Saved payment methods (prototype: UPI ID and card last 4 digits only, never full card data). */
    val savedUpiId: String = "",
    val savedCardLast4: String = "",
    val referralRedeemed: Boolean = false,
    /** When true the app follows the phone's light/dark setting and [isDarkTheme] is ignored. */
    val followSystemTheme: Boolean = false
)

const val MAX_RECENT_SEARCHES = 6

const val SIGN_IN_PHONE = "phone"
const val SIGN_IN_GOOGLE = "google"
const val SIGN_IN_GUEST = "guest"

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
        val SIGN_IN_METHOD = stringPreferencesKey("sign_in_method")
        val LINKED_GOOGLE_EMAIL = stringPreferencesKey("linked_google_email")
        val RECENT_SEARCHES = stringPreferencesKey("recent_searches")
        val INBOX_LAST_SEEN = longPreferencesKey("inbox_last_seen")
        val SAVED_UPI_ID = stringPreferencesKey("saved_upi_id")
        val SAVED_CARD_LAST4 = stringPreferencesKey("saved_card_last4")
        val REFERRAL_REDEEMED = booleanPreferencesKey("referral_redeemed")
        val FOLLOW_SYSTEM_THEME = booleanPreferencesKey("follow_system_theme")
    }

    private val recentSeparator = "\u001F"

    val preferencesFlow: Flow<UserPreferences> = context.quickKartDataStore.data.map { prefs ->
        UserPreferences(
            isDarkTheme = prefs[Keys.IS_DARK_THEME] ?: false,
            notificationsEnabled = prefs[Keys.NOTIFICATIONS_ENABLED] ?: true,
            screenZoomPercent = (prefs[Keys.SCREEN_ZOOM_PERCENT] ?: 85).coerceIn(80, 130),
            isLoggedIn = prefs[Keys.IS_LOGGED_IN] ?: false,
            isGuest = prefs[Keys.IS_GUEST] ?: false,
            userPhone = prefs[Keys.USER_PHONE] ?: "",
            userName = prefs[Keys.USER_NAME] ?: "QuickKart Member",
            userEmail = prefs[Keys.USER_EMAIL] ?: "",
            selectedNeighborhoodId = prefs[Keys.SELECTED_NEIGHBORHOOD] ?: "indiranagar",
            selectedAddressId = prefs[Keys.SELECTED_ADDRESS_ID] ?: "addr_home",
            signInMethod = prefs[Keys.SIGN_IN_METHOD]
                ?: if (prefs[Keys.IS_GUEST] == true) SIGN_IN_GUEST else SIGN_IN_PHONE,
            linkedGoogleEmail = prefs[Keys.LINKED_GOOGLE_EMAIL] ?: "",
            recentSearches = prefs[Keys.RECENT_SEARCHES]
                ?.split(recentSeparator)?.filter { it.isNotBlank() }.orEmpty(),
            inboxLastSeenMillis = prefs[Keys.INBOX_LAST_SEEN] ?: 0L,
            savedUpiId = prefs[Keys.SAVED_UPI_ID] ?: "",
            savedCardLast4 = prefs[Keys.SAVED_CARD_LAST4] ?: "",
            referralRedeemed = prefs[Keys.REFERRAL_REDEEMED] ?: false,
            followSystemTheme = prefs[Keys.FOLLOW_SYSTEM_THEME] ?: false,
        )
    }

    suspend fun addRecentSearch(query: String) {
        if (query.trim().length < 2) return
        context.quickKartDataStore.edit {
            val current = it[Keys.RECENT_SEARCHES]?.split(recentSeparator).orEmpty()
            val updated = com.example.util.updatedRecentSearches(current, query, MAX_RECENT_SEARCHES)
            it[Keys.RECENT_SEARCHES] = updated.joinToString(recentSeparator)
        }
    }

    suspend fun clearRecentSearches() {
        context.quickKartDataStore.edit { it[Keys.RECENT_SEARCHES] = "" }
    }

    suspend fun markInboxSeen(millis: Long) {
        context.quickKartDataStore.edit { it[Keys.INBOX_LAST_SEEN] = millis }
    }

    suspend fun savePaymentMethod(upiId: String?, cardLast4: String?) {
        context.quickKartDataStore.edit {
            if (!upiId.isNullOrBlank()) it[Keys.SAVED_UPI_ID] = upiId
            if (!cardLast4.isNullOrBlank()) it[Keys.SAVED_CARD_LAST4] = cardLast4
        }
    }

    suspend fun removeSavedPaymentMethods() {
        context.quickKartDataStore.edit {
            it[Keys.SAVED_UPI_ID] = ""
            it[Keys.SAVED_CARD_LAST4] = ""
        }
    }

    suspend fun setReferralRedeemed() {
        context.quickKartDataStore.edit { it[Keys.REFERRAL_REDEEMED] = true }
    }

    /** Explicit Light/Dark choice (stops following the system theme). */
    suspend fun setDarkTheme(enabled: Boolean) {
        context.quickKartDataStore.edit {
            it[Keys.IS_DARK_THEME] = enabled
            it[Keys.FOLLOW_SYSTEM_THEME] = false
        }
    }

    suspend fun setFollowSystemTheme() {
        context.quickKartDataStore.edit { it[Keys.FOLLOW_SYSTEM_THEME] = true }
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
        email: String = "",
        googleEmail: String = ""
    ) {
        context.quickKartDataStore.edit {
            it[Keys.IS_LOGGED_IN] = true
            it[Keys.IS_GUEST] = false
            it[Keys.USER_PHONE] = phone
            it[Keys.USER_NAME] = name.ifBlank { "QuickKart Member" }
            it[Keys.USER_EMAIL] = email
            it[Keys.SELECTED_NEIGHBORHOOD] = neighborhoodId
            it[Keys.SIGN_IN_METHOD] = if (googleEmail.isNotBlank()) SIGN_IN_GOOGLE else SIGN_IN_PHONE
            it[Keys.LINKED_GOOGLE_EMAIL] = googleEmail
        }
    }

    /** Links a Google account to the signed-in profile; fills the email if it was empty. */
    suspend fun linkGoogleAccount(googleEmail: String) {
        context.quickKartDataStore.edit {
            it[Keys.LINKED_GOOGLE_EMAIL] = googleEmail
            if (it[Keys.USER_EMAIL].isNullOrBlank()) it[Keys.USER_EMAIL] = googleEmail
        }
    }

    suspend fun unlinkGoogleAccount() {
        context.quickKartDataStore.edit { it[Keys.LINKED_GOOGLE_EMAIL] = "" }
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
            it[Keys.SIGN_IN_METHOD] = SIGN_IN_GUEST
            it[Keys.LINKED_GOOGLE_EMAIL] = ""
        }
    }

    suspend fun signOut() {
        context.quickKartDataStore.edit {
            it[Keys.IS_LOGGED_IN] = false
            it[Keys.IS_GUEST] = false
            it[Keys.USER_PHONE] = ""
            it[Keys.USER_EMAIL] = ""
            it[Keys.LINKED_GOOGLE_EMAIL] = ""
            it[Keys.SAVED_UPI_ID] = ""
            it[Keys.SAVED_CARD_LAST4] = ""
        }
    }
}
