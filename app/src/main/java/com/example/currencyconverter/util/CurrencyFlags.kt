package com.example.currencyconverter.util

import java.util.Currency
import java.util.Locale

private val preferredRegions = mapOf(
    "EUR" to "EU",
    "USD" to "US",
    "GBP" to "GB",
    "JPY" to "JP",
    "CNY" to "CN",
    "AUD" to "AU",
    "CAD" to "CA",
    "CHF" to "CH",
    "NZD" to "NZ",
    "HKD" to "HK",
    "SGD" to "SG",
    "KRW" to "KR",
    "INR" to "IN",
    "RUB" to "RU",
    "TRY" to "TR",
    "ZAR" to "ZA",
    "BRL" to "BR",
    "MXN" to "MX",
    "AED" to "AE",
    "SAR" to "SA",
    "XAF" to "CM",
    "XOF" to "SN",
    "XCD" to "AG",
    "XPF" to "PF"
)

/** Returns the flag for the main country or region using an ISO currency code. */
fun currencyFlag(currencyCode: String): String {
    val code = currencyCode.uppercase(Locale.ROOT)
    val region = preferredRegions[code] ?: Locale.getISOCountries().firstOrNull { countryCode ->
        runCatching {
            Currency.getInstance(Locale("", countryCode)).currencyCode == code
        }.getOrDefault(false)
    } ?: return "🌐"

    return region.toFlagEmoji()
}

private fun String.toFlagEmoji(): String = map { character ->
    String(Character.toChars(0x1F1E6 + character.code - 'A'.code))
}.joinToString("")
