package com.example.rider.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/** Local persistence (SharedPreferences + JSON): session, profile/KYC, settings and the COD cash ledger. */
class RiderStorage(context: Context) {
    private val prefs = context.getSharedPreferences("quickkart_rider", Context.MODE_PRIVATE)

    var sessionPhone: String?
        get() = prefs.getString("phone", null)
        set(v) = prefs.edit().apply { if (v == null) remove("phone") else putString("phone", v) }.apply()

    fun loadProfile(): RiderProfile? = prefs.getString("profile", null)?.let { raw ->
        runCatching {
            val o = JSONObject(raw)
            val docs = o.optJSONObject("docs")
            RiderProfile(
                phone = o.optString("phone"), name = o.optString("name"),
                vehicleType = enumOr(o.optString("vehicle"), VehicleType.BIKE),
                licenseNumber = o.optString("license"), vehicleNumber = o.optString("rc"),
                bankAccount = o.optString("bank"), ifsc = o.optString("ifsc"),
                docs = KycDoc.entries.associateWith { d -> enumOr(docs?.optString(d.name), DocStatus.NOT_UPLOADED) },
                rating = o.optDouble("rating", 4.8).toFloat(),
                preferredZones = o.optJSONArray("zones")?.let { a -> (0 until a.length()).map { a.getString(it) }.toSet() } ?: setOf("Madhapur"),
                shift = enumOr(o.optString("shift"), Shift.DAY),
                onboarded = o.optBoolean("onboarded")
            )
        }.getOrNull()
    }

    fun saveProfile(p: RiderProfile) {
        val docs = JSONObject().apply { p.docs.forEach { (k, v) -> put(k.name, v.name) } }
        val o = JSONObject().put("phone", p.phone).put("name", p.name).put("vehicle", p.vehicleType.name)
            .put("license", p.licenseNumber).put("rc", p.vehicleNumber).put("bank", p.bankAccount).put("ifsc", p.ifsc)
            .put("docs", docs).put("rating", p.rating.toDouble()).put("zones", JSONArray(p.preferredZones.toList())).put("shift", p.shift.name).put("onboarded", p.onboarded)
        prefs.edit().putString("profile", o.toString()).apply()
    }

    fun loadSettings(): RiderSettings = runCatching {
        val o = JSONObject(prefs.getString("settings", null) ?: return RiderSettings())
        RiderSettings(themeMode = enumOr(o.optString("theme"), ThemeMode.SYSTEM), zoomPercent = o.optInt("zoom", 85), sound = o.optBoolean("sound", true))
    }.getOrDefault(RiderSettings())

    fun saveSettings(s: RiderSettings) =
        prefs.edit().putString("settings", JSONObject().put("theme", s.themeMode.name).put("zoom", s.zoomPercent).put("sound", s.sound).toString()).apply()

    fun loadCash(): List<CashEntry>? = prefs.getString("cash", null)?.let { raw ->
        runCatching {
            val a = JSONArray(raw)
            (0 until a.length()).map { i ->
                val o = a.getJSONObject(i)
                CashEntry(o.getString("id"), enumOr(o.optString("type"), CashEntryType.COLLECTED), o.optInt("amount"), o.optLong("at"), o.optString("ref"))
            }
        }.getOrNull()
    }

    fun saveCash(list: List<CashEntry>) {
        val a = JSONArray()
        list.forEach { a.put(JSONObject().put("id", it.id).put("type", it.type.name).put("amount", it.amount).put("at", it.atMillis).put("ref", it.ref)) }
        prefs.edit().putString("cash", a.toString()).apply()
    }

    var lastInstantPayoutAt: Long
        get() = prefs.getLong("lastPayout", 0L)
        set(v) = prefs.edit().putLong("lastPayout", v).apply()

    private inline fun <reified T : Enum<T>> enumOr(name: String?, fallback: T): T =
        runCatching { enumValueOf<T>(name ?: "") }.getOrDefault(fallback)
}
