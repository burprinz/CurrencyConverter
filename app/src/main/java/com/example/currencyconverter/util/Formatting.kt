package com.example.currencyconverter.util

import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Locale
import kotlin.math.abs

/** Formats an amount compactly (max. 2/4/6 decimals, trailing zeros removed). */
fun formatAmount(value: Double): String {
    if (value.isNaN() || value.isInfinite()) return ""
    val pattern = when {
        abs(value) >= 100 -> "%.2f"
        abs(value) >= 1 -> "%.4f"
        else -> "%.6f"
    }
    val s = pattern.format(Locale.US, value)
    return if (s.contains('.')) s.trimEnd('0').trimEnd('.') else s
}

/** Accepts both "1.5" and "1,5". */
fun parseAmount(text: String): Double? =
    text.trim().replace(',', '.').toDoubleOrNull()?.takeIf { it.isFinite() }

/** Keeps numeric input editable while allowing at most one decimal separator. */
fun sanitizeAmountInput(text: String): String {
    val separatorIndex = text.indexOfFirst { it == '.' || it == ',' }
    if (separatorIndex < 0) return text.filter(Char::isDigit)

    val whole = text.take(separatorIndex).filter(Char::isDigit)
    val decimals = text.drop(separatorIndex + 1).filter(Char::isDigit)
    return whole + text[separatorIndex] + decimals
}

/** Removes floating point noise before floor/ceil (2.9999999999 -> 3.0). */
fun roundClean(value: Double): Double =
    BigDecimal(value).setScale(6, RoundingMode.HALF_UP).toDouble()

/** Human readable English name for an ISO code; falls back to the code. */
fun currencyDisplayName(code: String): String = try {
    java.util.Currency.getInstance(code).getDisplayName(Locale.ENGLISH)
} catch (e: Exception) {
    code
}
