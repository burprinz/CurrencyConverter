package com.example.currencyconverter.data.model

import com.example.currencyconverter.util.formatAmount

/** Determines where a currency's value comes from and how it is displayed. */
enum class CurrencyKind { REAL, CUSTOM, FUN }

/**
 * REAL   : normal currency, rate comes from the API (id = ISO code, e.g. "EUR").
 * CUSTOM : created by the user.
 * FUN    : fun currency from the built-in catalog (must be unlocked first).
 *
 * CUSTOM and FUN are defined as: 1 unit of this currency = [factor] x [refCode].
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
    val subtitle: String
        get() = if (kind == CurrencyKind.REAL) id else "1 = ${formatAmount(factor)} $refCode"
}
