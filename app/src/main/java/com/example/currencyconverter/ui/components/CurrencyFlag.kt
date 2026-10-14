package com.example.currencyconverter.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.currencyconverter.ui.LocalizedText as Text
import com.example.currencyconverter.data.model.Currency
import com.example.currencyconverter.data.model.CurrencyKind
import com.example.currencyconverter.util.currencyFlag

/**
 * Displays a country or region flag for regular currencies only.
 * The lookup is remembered by ISO currency ID, so rebuilding a row does not repeatedly scan locale data.
 */
@Composable
fun CurrencyFlag(currency: Currency) {
    // Fictional and user-defined currencies do not have a country, so they intentionally have no flag.
    if (currency.kind != CurrencyKind.REAL) return

    // Remembering by code avoids repeating the locale-to-region lookup on ordinary recompositions.
    val flag = remember(currency.id) { currencyFlag(currency.id) }
    Text(
        text = flag,
        fontSize = 24.sp,
        fontWeight = FontWeight.Normal,
        modifier = Modifier.semantics { contentDescription = "Flag of ${currency.name}" }
    )
}
