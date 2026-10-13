package com.example.currencyconverter.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.AssistChip
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import com.example.currencyconverter.ui.LocalizedText as Text
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

/**
 * Compact currency card: identity and color controls sit above the editable amount row.
 * The amount is the only editable conversion input; callbacks delegate rounding, removal, and
 * propagation to the view model. Per-card quick presets set this row's currency directly, while
 * palette indices are persisted per currency so selection changes do not lose the user's color.
 */
@Composable
fun CurrencyRow(
    currency: Currency,
    value: String,
    onRemove: () -> Unit,
    onValueChange: (String) -> Unit,
    onRoundDown: () -> Unit,
    onRoundUp: () -> Unit,
    onSetToOne: () -> Unit,
    onQuickAmount: (Int) -> Unit,
    selectedColor: Int? = null,
    onColorSelected: (Int?) -> Unit = {}
) {
    var paletteOpen by remember { mutableStateOf(false) }
    /** One named swatch; its position in [palette] is the value saved by the view model. */
    data class PaletteColor(val name: String, val color: Color)
    val palette = listOf(
        PaletteColor("Coral", Color(0xFFE57373)), PaletteColor("Apricot", Color(0xFFFFA726)),
        PaletteColor("Butter", Color(0xFFFFD54F)), PaletteColor("Pistachio", Color(0xFF9CCC65)),
        PaletteColor("Mint", Color(0xFF4DB6AC)), PaletteColor("Ice blue", Color(0xFF4FC3F7)),
        PaletteColor("Periwinkle", Color(0xFF64B5F6)), PaletteColor("Lavender", Color(0xFF7986CB)),
        PaletteColor("Lilac", Color(0xFFBA68C8)), PaletteColor("Rose", Color(0xFFF06292))
    )
    // Derive a subdued card fill and a stronger outline from one accent while preserving text contrast.
    val dark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val accent = selectedColor?.let(palette::getOrNull)?.color
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
                IconButton(onClick = { paletteOpen = true }, modifier = Modifier.size(40.dp)) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit card color",
                        tint = accent ?: MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                DropdownMenu(expanded = paletteOpen, onDismissRequest = { paletteOpen = false }) {
                    DropdownMenuItem(text = { Text("Default") }, onClick = { onColorSelected(null); paletteOpen = false })
                    palette.forEachIndexed { index, swatch ->
                        DropdownMenuItem(
                            text = { Row(verticalAlignment = Alignment.CenterVertically) { androidx.compose.foundation.layout.Box(Modifier.size(22.dp).background(swatch.color, CircleShape)); Text(swatch.name, Modifier.padding(start = 10.dp)) } },
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
            // Keeping presets inside each card makes their currency context visible at the point of use.
            Row(
                modifier = Modifier.padding(top = 8.dp, bottom = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Quick amounts", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(" · ${currency.id}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Row(
                Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(1, 5, 10, 20, 50, 100).forEach { amount ->
                    AssistChip(
                        onClick = { onQuickAmount(amount) },
                        label = { Text(amount.toString()) }
                    )
                }
            }
        }
    }
}
