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

    // The single named preference file keeps the app's small local state together and is private to this app.
    private val prefs = context.getSharedPreferences("currency_converter", Context.MODE_PRIVATE)

    /** Raw API response, retained so [CurrencyRepository] can rebuild rates without network access. */
    var ratesJson: String?
        get() = prefs.getString("rates_json", null)
        // apply() updates the in-memory preference immediately and writes to disk asynchronously.
        set(value) { prefs.edit().putString("rates_json", value).apply() }

    /** Epoch timestamp for the response in [ratesJson]; zero means no successful refresh is known. */
    var lastUpdateMillis: Long
        get() = prefs.getLong("last_update", 0L)
        set(value) { prefs.edit().putLong("last_update", value).apply() }

    /** Currency IDs shown in the converter, stored as a comma-separated stable-ID list. */
    var selectedIds: List<String>
        // Older installs or a first launch return an empty string, which becomes an empty list here.
        get() = prefs.getString("selected", "").orEmpty().split(",").filter { it.isNotEmpty() }
        // Currency IDs cannot contain commas, so joining them is sufficient for this simple list.
        set(value) { prefs.edit().putString("selected", value.joinToString(",")).apply() }

    /** User-marked favorites; a copy is returned so callers cannot mutate SharedPreferences' set. */
    var favoriteIds: Set<String>
        get() = prefs.getStringSet("favorites", emptySet()).orEmpty().toSet()
        set(value) { prefs.edit().putStringSet("favorites", value).apply() }

    /** Catalog IDs the user has unlocked and may add to the conversion list. */
    var unlockedIds: Set<String>
        get() = prefs.getStringSet("unlocked", emptySet()).orEmpty().toSet()
        set(value) { prefs.edit().putStringSet("unlocked", value).apply() }

    /** Currency whose text field the user edited last (the "source" of all values); null means none. */
    var activeId: String?
        get() = prefs.getString("active_id", null)
        set(value) { prefs.edit().putString("active_id", value).apply() }

    /** Exact text in the active amount field, kept as text so incomplete input such as `1.` survives. */
    var activeText: String
        get() = prefs.getString("active_text", "1") ?: "1"
        set(value) { prefs.edit().putString("active_text", value).apply() }

    /** Last source currency selected in the camera converter, defaulting to euros on first use. */
    var cameraFromCurrency: String
        get() = prefs.getString("camera_from_currency", "EUR") ?: "EUR"
        set(value) { prefs.edit().putString("camera_from_currency", value).apply() }

    /** Last target currency selected in the camera converter, defaulting to US dollars on first use. */
    var cameraToCurrency: String
        get() = prefs.getString("camera_to_currency", "USD") ?: "USD"
        set(value) { prefs.edit().putString("camera_to_currency", value).apply() }

    /** Base currency last selected for the history graph; kept separate from the camera pair. */
    var historyFromCurrency: String
        get() = prefs.getString("history_from_currency", "EUR") ?: "EUR"
        set(value) { prefs.edit().putString("history_from_currency", value).apply() }

    /** Quote currency last selected for the history graph. */
    var historyToCurrency: String
        get() = prefs.getString("history_to_currency", "USD") ?: "USD"
        set(value) { prefs.edit().putString("history_to_currency", value).apply() }

    /** Last reference currency used while creating or editing a custom currency, defaulting to USD. */
    var customReferenceCurrency: String
        get() = prefs.getString("custom_reference_currency", "USD") ?: "USD"
        set(value) { prefs.edit().putString("custom_reference_currency", value).apply() }

    /** Per-currency palette indexes; a missing ID means use the current theme's default card. */
    var currencyColors: Map<String, Int>
        get() = try {
            // A malformed or old color record should not prevent the rest of the app from starting.
            val json = JSONObject(prefs.getString("currency_colors", "{}") ?: "{}")
            // JSON object keys are currency IDs; values are palette positions understood by CurrencyRow.
            buildMap {
                val keys = json.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    put(key, json.getInt(key))
                }
            }
        } catch (_: Exception) { emptyMap() }
        set(value) {
            // Convert the map to JSON so IDs remain associated with their palette entries across launches.
            val json = JSONObject()
            value.forEach { (id, color) -> json.put(id, color) }
            prefs.edit().putString("currency_colors", json.toString()).apply()
        }

    /** Selected UI language code; unknown/empty data falls back to English. */
    var language: String
        get() = prefs.getString("language", "en") ?: "en"
        set(value) { prefs.edit().putString("language", value).apply() }

    /** Theme preference (`light`, `dark`, or `system`); the UI resolves `system` at runtime. */
    var themeMode: String
        get() = prefs.getString("theme_mode", "system") ?: "system"
        set(value) { prefs.edit().putString("theme_mode", value).apply() }

    /** User-created currencies encoded as JSON because each record has several typed fields. */
    var customCurrencies: List<Currency>
        get() = try {
            // Reconstruct only user-created fields; kind is always CUSTOM and is not user-editable.
            val arr = JSONArray(prefs.getString("custom", "[]"))
            (0 until arr.length()).map { i ->
                // Parse records independently inside the enclosing try; one invalid record fails safely to an empty list.
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
            // Ignore invalid persisted JSON rather than preventing the app from opening.
            emptyList()
        }
        set(value) {
            // Serialize only fields required to reconstruct the user entry; kind is restored as CUSTOM above.
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
