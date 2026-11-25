package com.example.merchant.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/**
 * Local persistence for the prototype (SharedPreferences + JSON). Keeps the session, store
 * profile/KYC, settings, catalogue, offers and tickets across launches. Orders, payouts and
 * alerts are demo data re-created each launch.
 */
class MerchantStorage(context: Context) {
    private val prefs = context.getSharedPreferences("quickkart_merchant", Context.MODE_PRIVATE)

    var loggedInPhone: String?
        get() = prefs.getString(KEY_PHONE, null)
        set(value) = prefs.edit().apply { if (value == null) remove(KEY_PHONE) else putString(KEY_PHONE, value) }.apply()

    fun loadProfile(): MerchantProfile? = prefs.getString(KEY_PROFILE, null)?.let { raw ->
        runCatching {
            val o = JSONObject(raw)
            MerchantProfile(
                phone = o.optString("phone"),
                storeName = o.optString("storeName"),
                ownerName = o.optString("ownerName"),
                storeType = enumOr(o.optString("storeType"), StoreType.KIRANA),
                address = o.optString("address"),
                fssaiNumber = o.optString("fssai"),
                fssaiStatus = enumOr(o.optString("fssaiStatus"), DocStatus.NOT_UPLOADED),
                gstin = o.optString("gstin"),
                gstinStatus = enumOr(o.optString("gstinStatus"), DocStatus.NOT_UPLOADED),
                bankName = o.optString("bankName"),
                accountNumber = o.optString("account"),
                ifsc = o.optString("ifsc"),
                kycSubmitted = o.optBoolean("kycSubmitted")
            )
        }.getOrNull()
    }

    fun saveProfile(p: MerchantProfile) {
        val o = JSONObject()
            .put("phone", p.phone).put("storeName", p.storeName).put("ownerName", p.ownerName)
            .put("storeType", p.storeType.name).put("address", p.address)
            .put("fssai", p.fssaiNumber).put("fssaiStatus", p.fssaiStatus.name)
            .put("gstin", p.gstin).put("gstinStatus", p.gstinStatus.name)
            .put("bankName", p.bankName).put("account", p.accountNumber).put("ifsc", p.ifsc)
            .put("kycSubmitted", p.kycSubmitted)
        prefs.edit().putString(KEY_PROFILE, o.toString()).apply()
    }

    fun loadSettings(): StoreSettings {
        val raw = prefs.getString(KEY_SETTINGS, null) ?: return StoreSettings()
        return runCatching {
            val o = JSONObject(raw)
            StoreSettings(
                isOpen = o.optBoolean("isOpen", true),
                serviceRadiusKm = o.optDouble("radius", 3.0).toFloat(),
                openMinuteOfDay = o.optInt("open", 480),
                closeMinuteOfDay = o.optInt("close", 1320),
                autoAcceptOrders = o.optBoolean("autoAccept", false),
                orderSound = o.optBoolean("sound", true),
                themeMode = enumOr(o.optString("theme"), ThemeMode.SYSTEM),
                zoomPercent = o.optInt("zoom", 85)
            )
        }.getOrDefault(StoreSettings())
    }

    fun saveSettings(s: StoreSettings) {
        val o = JSONObject()
            .put("isOpen", s.isOpen).put("radius", s.serviceRadiusKm.toDouble())
            .put("open", s.openMinuteOfDay).put("close", s.closeMinuteOfDay)
            .put("autoAccept", s.autoAcceptOrders).put("sound", s.orderSound)
            .put("theme", s.themeMode.name).put("zoom", s.zoomPercent)
        prefs.edit().putString(KEY_SETTINGS, o.toString()).apply()
    }

    fun loadCatalogue(): List<CatalogueItem>? = prefs.getString(KEY_CATALOGUE, null)?.let { raw ->
        runCatching {
            val arr = JSONArray(raw)
            (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                CatalogueItem(
                    id = o.getString("id"), name = o.getString("name"),
                    description = o.optString("desc"), category = o.optString("cat"),
                    price = o.optInt("price"), discountPercent = o.optInt("disc"),
                    stock = o.optInt("stock"), active = o.optBoolean("active", true),
                    unit = o.optString("unit", "1 pc"),
                    photoUri = o.optString("photo").ifBlank { null }
                )
            }
        }.getOrNull()
    }

    fun saveCatalogue(items: List<CatalogueItem>) {
        val arr = JSONArray()
        items.forEach { c ->
            arr.put(
                JSONObject().put("id", c.id).put("name", c.name).put("desc", c.description)
                    .put("cat", c.category).put("price", c.price).put("disc", c.discountPercent)
                    .put("stock", c.stock).put("active", c.active).put("unit", c.unit)
                    .put("photo", c.photoUri ?: "")
            )
        }
        prefs.edit().putString(KEY_CATALOGUE, arr.toString()).apply()
    }

    fun loadOffers(): List<StoreOffer>? = prefs.getString(KEY_OFFERS, null)?.let { raw ->
        runCatching {
            val arr = JSONArray(raw)
            (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                StoreOffer(
                    id = o.getString("id"), type = enumOr(o.optString("type"), OfferType.PERCENT_OFF),
                    value = o.optInt("value"), minOrder = o.optInt("min"),
                    validFromMillis = o.optLong("from"), validToMillis = o.optLong("to"),
                    active = o.optBoolean("active"), category = o.optString("cat").ifBlank { null }
                )
            }
        }.getOrNull()
    }

    fun saveOffers(offers: List<StoreOffer>) {
        val arr = JSONArray()
        offers.forEach { f ->
            arr.put(
                JSONObject().put("id", f.id).put("type", f.type.name).put("value", f.value)
                    .put("min", f.minOrder).put("from", f.validFromMillis).put("to", f.validToMillis)
                    .put("active", f.active).put("cat", f.category ?: "")
            )
        }
        prefs.edit().putString(KEY_OFFERS, arr.toString()).apply()
    }

    fun loadTickets(): List<SupportTicket>? = prefs.getString(KEY_TICKETS, null)?.let { raw ->
        runCatching {
            val arr = JSONArray(raw)
            (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                SupportTicket(
                    id = o.getString("id"), category = o.optString("cat"), subject = o.optString("subject"),
                    description = o.optString("desc"), orderId = o.optString("order").ifBlank { null },
                    createdAtMillis = o.optLong("at"), status = enumOr(o.optString("status"), TicketStatus.OPEN)
                )
            }
        }.getOrNull()
    }

    fun saveTickets(tickets: List<SupportTicket>) {
        val arr = JSONArray()
        tickets.forEach { t ->
            arr.put(
                JSONObject().put("id", t.id).put("cat", t.category).put("subject", t.subject)
                    .put("desc", t.description).put("order", t.orderId ?: "")
                    .put("at", t.createdAtMillis).put("status", t.status.name)
            )
        }
        prefs.edit().putString(KEY_TICKETS, arr.toString()).apply()
    }

    /** Sign out keeps the store profile (the same phone can sign back in) but ends the session. */
    fun signOut() {
        prefs.edit().remove(KEY_PHONE).apply()
    }

    private inline fun <reified T : Enum<T>> enumOr(name: String?, fallback: T): T =
        runCatching { enumValueOf<T>(name ?: "") }.getOrDefault(fallback)

    private companion object {
        const val KEY_PHONE = "session_phone"
        const val KEY_PROFILE = "profile"
        const val KEY_SETTINGS = "settings"
        const val KEY_CATALOGUE = "catalogue"
        const val KEY_OFFERS = "offers"
        const val KEY_TICKETS = "tickets"
    }
}
