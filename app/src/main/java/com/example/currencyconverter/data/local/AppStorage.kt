package com.example.currencyconverter.data.local

import android.content.Context
import com.example.currencyconverter.data.model.Currency
import com.example.currencyconverter.data.model.CurrencyKind
import org.json.JSONArray
import org.json.JSONObject

/**
 * Small persistence layer backed by Android SharedPreferences.
 * It stores rates and user choices; UI state itself stays in [com.example.currencyconverter.ui.MainViewModel].
 * Each property translates one app concept to a stable preference key. Structured values such as
 * custom currencies and color assignments are encoded as JSON; simple sets/lists use preferences
 * primitives so they can be read synchronously when the view model starts.
 */
class AppStorage(context: Context) {

    private val prefs = context.getSharedPreferences("currency_converter", Context.MODE_PRIVATE)

    var ratesJson: String?
        get() = prefs.getString("rates_json", null)
        set(value) { prefs.edit().putString("rates_json", value).apply() }

    var lastUpdateMillis: Long
        get() = prefs.getLong("last_update", 0L)
        set(value) { prefs.edit().putLong("last_update", value).apply() }

    var selectedIds: List<String>
        get() = prefs.getString("selected", "").orEmpty().split(",").filter { it.isNotEmpty() }
        set(value) { prefs.edit().putString("selected", value.joinToString(",")).apply() }

    var favoriteIds: Set<String>
        get() = prefs.getStringSet("favorites", emptySet()).orEmpty().toSet()
        set(value) { prefs.edit().putStringSet("favorites", value).apply() }

    var unlockedIds: Set<String>
        get() = prefs.getStringSet("unlocked", emptySet()).orEmpty().toSet()
        set(value) { prefs.edit().putStringSet("unlocked", value).apply() }

    /** Currency whose text field the user edited last (the "source" of all values). */
    var activeId: String?
        get() = prefs.getString("active_id", null)
        set(value) { prefs.edit().putString("active_id", value).apply() }

    var activeText: String
        get() = prefs.getString("active_text", "1") ?: "1"
        set(value) { prefs.edit().putString("active_text", value).apply() }

    /** Last source and target currencies selected in the camera converter. */
    var cameraFromCurrency: String
        get() = prefs.getString("camera_from_currency", "EUR") ?: "EUR"
        set(value) { prefs.edit().putString("camera_from_currency", value).apply() }

    var cameraToCurrency: String
        get() = prefs.getString("camera_to_currency", "USD") ?: "USD"
        set(value) { prefs.edit().putString("camera_to_currency", value).apply() }

    /** Last source and target currencies selected in the rate history chart. */
    var historyFromCurrency: String
        get() = prefs.getString("history_from_currency", "EUR") ?: "EUR"
        set(value) { prefs.edit().putString("history_from_currency", value).apply() }

    var historyToCurrency: String
        get() = prefs.getString("history_to_currency", "USD") ?: "USD"
        set(value) { prefs.edit().putString("history_to_currency", value).apply() }

    /** Last reference currency used while creating or editing a custom currency. */
    var customReferenceCurrency: String
        get() = prefs.getString("custom_reference_currency", "USD") ?: "USD"
        set(value) { prefs.edit().putString("custom_reference_currency", value).apply() }

    var currencyColors: Map<String, Int>
        get() = try {
            // A malformed or old color record should not prevent the rest of the app from starting.
            val json = JSONObject(prefs.getString("currency_colors", "{}") ?: "{}")
            buildMap { val keys = json.keys(); while (keys.hasNext()) { val key = keys.next(); put(key, json.getInt(key)) } }
        } catch (_: Exception) { emptyMap() }
        set(value) {
            val json = JSONObject()
            value.forEach { (id, color) -> json.put(id, color) }
            prefs.edit().putString("currency_colors", json.toString()).apply()
        }

    var language: String
        get() = prefs.getString("language", "en") ?: "en"
        set(value) { prefs.edit().putString("language", value).apply() }

    var themeMode: String
        get() = prefs.getString("theme_mode", "system") ?: "system"
        set(value) { prefs.edit().putString("theme_mode", value).apply() }

    var customCurrencies: List<Currency>
        get() = try {
            // Reconstruct only user-created fields; kind is always CUSTOM and is not user-editable.
            val arr = JSONArray(prefs.getString("custom", "[]"))
            (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                Currency(
                    id = o.getString("id"),
                    name = o.getString("name"),
                    kind = CurrencyKind.CUSTOM,
                    refCode = o.getString("ref"),
                    factor = o.getDouble("factor")
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
        set(value) {
            val arr = JSONArray()
            value.forEach {
                arr.put(
                    JSONObject()
                        .put("id", it.id)
                        .put("name", it.name)
                        .put("ref", it.refCode)
                        .put("factor", it.factor)
                )
            }
            prefs.edit().putString("custom", arr.toString()).apply()
        }
}
