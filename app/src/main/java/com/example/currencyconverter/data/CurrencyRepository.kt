package com.example.currencyconverter.data

import com.example.currencyconverter.data.local.AppStorage
import com.example.currencyconverter.data.remote.RatesApi

/** UI-facing status of the latest-rate request; OFFLINE can still display rates from the local cache. */
enum class FetchState { IDLE, LOADING, SUCCESS, OFFLINE }

/**
 * Boundary between the UI-facing view model and rate storage/network code.
 * A successful refresh updates both the cached response and its timestamp; failed requests leave
 * the previous cache untouched so offline conversions can continue using the last known rates.
 */
class CurrencyRepository(private val storage: AppStorage) {

    val lastUpdateMillis: Long get() = storage.lastUpdateMillis

    fun cachedRates(): Map<String, Double> = RatesApi.parseRates(storage.ratesJson)

    /** Fetches fresh rates, persists the raw response for reuse, and propagates network/HTTP errors. */
    suspend fun refreshRates(): Map<String, Double> {
        val result = RatesApi.fetchLatest()
        storage.ratesJson = result.rawJson
        storage.lastUpdateMillis = System.currentTimeMillis()
        return result.rates
    }
}
