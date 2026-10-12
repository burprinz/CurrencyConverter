package com.example.currencyconverter.data

import com.example.currencyconverter.data.local.AppStorage
import com.example.currencyconverter.data.remote.RatesApi

enum class FetchState { IDLE, LOADING, SUCCESS, OFFLINE }

/** Loads rates from the API and keeps the last successful result cached. */
class CurrencyRepository(private val storage: AppStorage) {

    val lastUpdateMillis: Long get() = storage.lastUpdateMillis

    fun cachedRates(): Map<String, Double> = RatesApi.parseRates(storage.ratesJson)

    /** Throws if the request fails (no internet, HTTP error, ...). */
    suspend fun refreshRates(): Map<String, Double> {
        val result = RatesApi.fetchLatest()
        storage.ratesJson = result.rawJson
        storage.lastUpdateMillis = System.currentTimeMillis()
        return result.rates
    }
}
