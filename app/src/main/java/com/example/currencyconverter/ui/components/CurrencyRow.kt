package com.example.currencyconverter.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
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
    onSetToOne: () -> Unit,
    selectedColor: Int? = null,
    onColorSelected: (Int?) -> Unit = {}
) {
    var paletteOpen by remember { mutableStateOf(false) }
    val palette = listOf(0xFFE57373, 0xFFFFA726, 0xFFFFD54F, 0xFF9CCC65, 0xFF4DB6AC, 0xFF4FC3F7, 0xFF64B5F6, 0xFF7986CB, 0xFFBA68C8, 0xFFF06292).map { Color(it) }
    val dark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val accent = selectedColor?.let(palette::getOrNull)
    val cardBackground = accent?.let { if (dark) Color(red = it.red * .20f, green = it.green * .20f, blue = it.blue * .20f) else it.copy(alpha = .14f) }
    val cardBorder = accent?.let { if (dark) it.copy(red = it.red * .75f, green = it.green * .75f, blue = it.blue * .75f) else it.copy(red = it.red * .72f, green = it.green * .72f, blue = it.blue * .72f) }
    val roundingDigits = if (currency.kind == CurrencyKind.REAL) {
        runCatching { java.util.Currency.getInstance(currency.id).defaultFractionDigits }
            .getOrNull()?.takeIf { it >= 0 } ?: 2
    } else 2
    Card(
        Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = cardBackground ?: MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, cardBorder ?: MaterialTheme.colorScheme.outlineVariant)
    ) {
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
                androidx.compose.material3.TextButton(onClick = { paletteOpen = true }, modifier = Modifier.size(40.dp)) { Text("●") }
                DropdownMenu(expanded = paletteOpen, onDismissRequest = { paletteOpen = false }) {
                    DropdownMenuItem(text = { Text("Default") }, onClick = { onColorSelected(null); paletteOpen = false })
                    palette.forEachIndexed { index, swatch ->
                        DropdownMenuItem(
                            text = { Row(verticalAlignment = Alignment.CenterVertically) { androidx.compose.foundation.layout.Box(Modifier.size(22.dp).background(swatch, CircleShape)); Text("Pastel ${index + 1}", Modifier.padding(start = 10.dp)) } },
                            onClick = { onColorSelected(index); paletteOpen = false }
                        )
                    }
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
