package com.example.currencyconverter.data.model

import com.example.currencyconverter.util.formatAmount

/** Determines the source of a currency's value and which conversion rule applies to it. */
enum class CurrencyKind { REAL, CUSTOM, FUN }

/**
 * REAL   : normal currency, rate comes from the API (id = ISO code, e.g. "EUR").
 * CUSTOM : created by the user.
 * FUN    : fun currency from the built-in catalog (must be unlocked first).
 *
 * CUSTOM and FUN are defined as: 1 unit of this currency = [factor] x [refCode].
 * Keeping all three kinds in one immutable model lets lists and conversion controls render them
 * uniformly while the converter still knows whether to use an API rate or a fixed estimate.
 */
data class Currency(
    val id: String,
    val name: String,
    val kind: CurrencyKind,
    val refCode: String? = null,
    val factor: Double = 1.0,
    val description: String = "",
    val category: String = "Other"
) {
    /** Short secondary label: ISO code for real money, or the configured reference value otherwise. */
    val subtitle: String
        get() = if (kind == CurrencyKind.REAL) id else "1 = ${formatAmount(factor)} $refCode"
}
