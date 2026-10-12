package com.example.currencyconverter.domain

import com.example.currencyconverter.data.model.Currency
import com.example.currencyconverter.data.model.CurrencyKind

/** Pure conversion logic. [rates] = units of currency per 1 USD. */
class CurrencyConverter(private val rates: Map<String, Double>) {

    private fun unitsPerUsd(c: Currency): Double? = when (c.kind) {
        CurrencyKind.REAL -> rates[c.id]
        else -> {
            val refRate = c.refCode?.let { rates[it] }
            if (refRate == null || c.factor <= 0.0) null else refRate / c.factor
        }
    }

    fun convert(amount: Double, from: Currency, to: Currency): Double? {
        val f = unitsPerUsd(from) ?: return null
        val t = unitsPerUsd(to) ?: return null
        return amount * t / f
    }
}
