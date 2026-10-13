package com.example.currencyconverter.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL

/** One dated quote: [rate] is the amount of the selected quote currency for one base unit. */
data class HistoricalRatePoint(val date: String, val rate: Double)

/**
 * Fetches historical currency quotes from Frankfurter on the IO dispatcher.
 * The endpoint returns one JSON row per date; invalid dates and non-positive rates are discarded,
 * then the remaining observations are sorted so chart x-positions follow time from left to right.
 */
object HistoricalRatesApi {
    /** Downloads a date range; [monthly] asks the API to group older all-time results by month. */
    suspend fun fetch(base: String, quote: String, from: String, to: String, monthly: Boolean = false): List<HistoricalRatePoint> = withContext(Dispatchers.IO) {
        val params = buildString {
            append("base=$base&quotes=$quote&from=$from&to=$to")
            if (monthly) append("&group=month")
        }
        val connection = URL("https://api.frankfurter.dev/v2/rates?$params").openConnection() as HttpURLConnection
        try {
            connection.connectTimeout = 10000
            connection.readTimeout = 15000
            if (connection.responseCode != 200) throw IllegalStateException("HTTP ${connection.responseCode}")
            val array = JSONArray(connection.inputStream.bufferedReader().use { it.readText() })
            (0 until array.length()).mapNotNull { index ->
                val row = array.optJSONObject(index) ?: return@mapNotNull null
                val date = row.optString("date").takeIf { it.matches(Regex("\\d{4}-\\d{2}-\\d{2}")) } ?: return@mapNotNull null
                val rate = row.optDouble("rate", Double.NaN).takeIf { it.isFinite() && it > 0.0 } ?: return@mapNotNull null
                HistoricalRatePoint(date, rate)
            }.sortedBy { it.date }
        } finally {
            connection.disconnect()
        }
    }
}
