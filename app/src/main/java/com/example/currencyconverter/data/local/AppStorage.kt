package com.example.currencyconverter.data.local

import android.content.Context
import com.example.currencyconverter.data.model.Currency
import com.example.currencyconverter.data.model.CurrencyKind
import org.json.JSONArray
import org.json.JSONObject

/** Everything that has to survive an app restart. */
class AppStorage(context: Context) {

    private val prefs = context.getSharedPreferences("currency_converter", Context.MODE_PRIVATE)

    var ratesJson: String?
        get() = prefs.getString("rates_json", null)
        set(value) { prefs.edit().putString("rates_json", value).apply() }

    var lastUpdateMillis: Long
        get() = prefs.getLong("last_update", 0L)
        set(value) { prefs.edit().putLong("last_update", value).apply() }

    var selectedIds: List<String>
        get() = prefs.getString("selected", "")!!.split(",").filter { it.isNotEmpty() }
        set(value) { prefs.edit().putString("selected", value.joinToString(",")).apply() }

    var favoriteIds: Set<String>
        get() = prefs.getStringSet("favorites", emptySet())!!.toSet()
        set(value) { prefs.edit().putStringSet("favorites", value).apply() }

    var unlockedIds: Set<String>
        get() = prefs.getStringSet("unlocked", emptySet())!!.toSet()
        set(value) { prefs.edit().putStringSet("unlocked", value).apply() }

    /** Currency whose text field the user edited last (the "source" of all values). */
    var activeId: String?
        get() = prefs.getString("active_id", null)
        set(value) { prefs.edit().putString("active_id", value).apply() }

    var activeText: String
        get() = prefs.getString("active_text", "1") ?: "1"
        set(value) { prefs.edit().putString("active_text", value).apply() }

    var customCurrencies: List<Currency>
        get() = try {
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
