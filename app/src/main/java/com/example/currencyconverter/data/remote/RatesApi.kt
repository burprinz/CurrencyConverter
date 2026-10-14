package com.example.currencyconverter.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

/**
 * ExchangeRate-API "Open Access" endpoint (no API key).
 * Commercial use and caching are allowed, attribution is required
 * ("Rates By Exchange Rate API" -> https://www.exchangerate-api.com), no redistribution.
 * Data updates once per day. Rates are "units of currency per 1 USD".
 */
object RatesApi {
    private const val LATEST_URL = "https://open.er-api.com/v6/latest/USD"

    /** Parsed rates plus their original JSON so the repository can cache the response for offline use. */
    class Result(val rawJson: String, val rates: Map<String, Double>)

    /** Performs blocking URL I/O on Dispatchers.IO and validates that the response has usable rates. */
    suspend fun fetchLatest(): Result = withContext(Dispatchers.IO) {
        // Network work stays off the main thread so refreshing rates cannot block UI interaction.
        val conn = URL(LATEST_URL).openConnection() as HttpURLConnection
        try {
            // Explicit timeouts prevent a stalled connection from keeping the loading state indefinitely.
            conn.connectTimeout = 8000
            conn.readTimeout = 8000
            if (conn.responseCode != 200) throw IOException("HTTP ${conn.responseCode}")
            val text = conn.inputStream.bufferedReader().use { it.readText() }
            // Keep both representations: parsed values for conversion, raw JSON for an offline restart.
            val rates = parseRates(text)
            if (rates.isEmpty()) throw IOException("No rates in response")
            Result(text, rates)
        } finally {
            conn.disconnect()
        }
    }

    /** Extracts only finite, positive rates; an absent or malformed cache safely becomes empty. */
    fun parseRates(json: String?): Map<String, Double> {
        // Missing cache is normal on first launch, so represent it as no rates instead of throwing.
        if (json == null) return emptyMap()
        return try {
            // The API's top-level rates object maps ISO codes to units per one US dollar.
            val obj = JSONObject(json).getJSONObject("rates")
            obj.keys().asSequence().mapNotNull { code ->
                val rate = obj.optDouble(code, Double.NaN)
                if (rate.isFinite() && rate > 0.0) code to rate else null
            }.toMap()
        } catch (e: Exception) {
            emptyMap()
        }
    }
}
