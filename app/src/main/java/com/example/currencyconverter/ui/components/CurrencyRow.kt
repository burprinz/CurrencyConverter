package com.example.currencyconverter.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.currencyconverter.data.model.Currency
import com.example.currencyconverter.data.model.CurrencyKind

/** Compact two-row currency card that fits narrow phone screens. */
@Composable
fun CurrencyRow(
    currency: Currency,
    value: String,
    onRemove: () -> Unit,
    onValueChange: (String) -> Unit,
    onRoundDown: () -> Unit,
    onRoundUp: () -> Unit,
    onSetToOne: () -> Unit
) {
    val roundingDigits = if (currency.kind == CurrencyKind.REAL) {
        runCatching { java.util.Currency.getInstance(currency.id).defaultFractionDigits }
            .getOrNull()?.takeIf { it >= 0 } ?: 2
    } else 2
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CurrencyFlag(currency)
                Column(Modifier.weight(1f).padding(start = 8.dp, end = 8.dp)) {
                    Text(currency.name, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(currency.subtitle, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                IconButton(onClick = onRemove, modifier = Modifier.size(40.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Remove from list")
                }
            }
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                OutlinedTextField(
                    value = value,
                    onValueChange = onValueChange,
                    label = { Text("Amount") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onRoundDown, modifier = Modifier.size(40.dp)) {
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Round down to $roundingDigits decimal places")
                }
                IconButton(onClick = onRoundUp, modifier = Modifier.size(40.dp)) {
                    Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Round up to $roundingDigits decimal places")
                }
                IconButton(
                    onClick = onSetToOne,
                    modifier = Modifier.size(40.dp).semantics { contentDescription = "Set amount to one" }
                ) {
                    Text("1", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}
