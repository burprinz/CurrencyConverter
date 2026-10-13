package com.example.currencyconverter.domain

import com.example.currencyconverter.data.model.Currency
import com.example.currencyconverter.data.model.CurrencyKind

/** Pure conversion logic. [rates] = units of currency per 1 USD. */
class CurrencyConverter(private val rates: Map<String, Double>) {

    private fun unitsPerUsd(c: Currency): Double? = when (c.kind) {
        CurrencyKind.REAL -> rates[c.id]?.takeIf { it.isFinite() && it > 0.0 }
        else -> {
            val refRate = c.refCode?.let { rates[it] }?.takeIf { it.isFinite() && it > 0.0 }
            if (refRate == null || !c.factor.isFinite() || c.factor <= 0.0) null
            else (refRate / c.factor).takeIf { it.isFinite() && it > 0.0 }
        }
    }

    fun convert(amount: Double, from: Currency, to: Currency): Double? {
        if (!amount.isFinite()) return null
        val f = unitsPerUsd(from) ?: return null
        val t = unitsPerUsd(to) ?: return null
        return (amount * t / f).takeIf { it.isFinite() }
    }
}
