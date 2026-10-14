package com.example.currencyconverter.domain

import com.example.currencyconverter.data.model.Currency
import com.example.currencyconverter.data.model.CurrencyKind

/**
 * Converts amounts without Android/UI dependencies. [rates] stores units of each real currency per USD.
 * For custom/fun currencies, the catalog factor says how much of [Currency.refCode] one unit represents.
 * Every source is normalized to “units per USD” first, then converted using a ratio. This common
 * intermediate unit prevents separate pairwise formulas from drifting or reversing the rate.
 */
class CurrencyConverter(private val rates: Map<String, Double>) {

    /** Returns how many units of [c] equal one USD; invalid or missing rates make conversion unavailable. */
    private fun unitsPerUsd(c: Currency): Double? = when (c.kind) {
        // API rates already use the common USD base; reject zero and invalid server values.
        CurrencyKind.REAL -> rates[c.id]?.takeIf { it.isFinite() && it > 0.0 }
        else -> {
            // A custom/fan factor means `1 custom = factor * refCode`; convert that reference into USD units.
            val refRate = c.refCode?.let { rates[it] }?.takeIf { it.isFinite() && it > 0.0 }
            if (refRate == null || !c.factor.isFinite() || c.factor <= 0.0) null
            else (refRate / c.factor).takeIf { it.isFinite() && it > 0.0 }
        }
    }

    /** Converts one amount; null signals missing or invalid reference data rather than a fake zero. */
    fun convert(amount: Double, from: Currency, to: Currency): Double? {
        // Convert through USD as a common reference: amount * (target units/USD) / (source units/USD).
        if (!amount.isFinite()) return null
        val f = unitsPerUsd(from) ?: return null
        val t = unitsPerUsd(to) ?: return null
        return (amount * t / f).takeIf { it.isFinite() }
    }
}
