package com.example.currencyconverter.util

import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Locale
import kotlin.math.abs

/** Formats an amount compactly (max. 2/4/6 decimals, trailing zeros removed). */
fun formatAmount(value: Double): String {
    // Avoid rendering special floating-point values as user-facing numbers.
    if (value.isNaN() || value.isInfinite()) return ""
    // Larger amounts need fewer digits; small fractions keep extra precision for fan-currency estimates.
    val pattern = when {
        abs(value) >= 100 -> "%.2f"
        abs(value) >= 1 -> "%.4f"
        else -> "%.6f"
    }
    val s = pattern.format(Locale.US, value)
    return if (s.contains('.')) s.trimEnd('0').trimEnd('.') else s
}

/** Accepts both decimal separators; parsing rejects NaN and infinity so they never reach the UI. */
fun parseAmount(text: String): Double? =
    // The app accepts either locale-style comma or programming-style dot as a decimal mark.
    text.trim().replace(',', '.').toDoubleOrNull()?.takeIf { it.isFinite() }

/** Keeps numeric input editable while allowing at most one decimal separator and retaining its style. */
fun sanitizeAmountInput(text: String): String {
    // Keep the first decimal separator the user typed and discard any later punctuation or non-digit characters.
    val separatorIndex = text.indexOfFirst { it == '.' || it == ',' }
    if (separatorIndex < 0) return text.filter(Char::isDigit)

    val whole = text.take(separatorIndex).filter(Char::isDigit)
    val decimals = text.drop(separatorIndex + 1).filter(Char::isDigit)
    return whole + text[separatorIndex] + decimals
}

/** Removes floating point noise before floor/ceil (2.9999999999 -> 3.0). */
fun roundClean(value: Double): Double =
    BigDecimal(value).setScale(6, RoundingMode.HALF_UP).toDouble()

/** Human-readable currency name for an ISO code in [language]; falls back to the code if unknown. */
fun currencyDisplayName(code: String, language: String = "en"): String = try {
    // Java's locale data supplies translated currency names without maintaining another name catalog.
    val locale = Locale.forLanguageTag(language)
    java.util.Currency.getInstance(code).getDisplayName(locale)
} catch (e: Exception) {
    code
}
