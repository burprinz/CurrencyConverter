package com.example.currencyconverter.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import com.example.currencyconverter.ui.LocalizedText as Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.currencyconverter.ui.MainViewModel
import com.example.currencyconverter.ui.components.CurrencyRow

/**
 * Shows all selected currencies, or only favorites when [favoritesOnly] is enabled.
 * The empty state explains the next action; otherwise each [CurrencyRow] owns its quick amounts and
 * delegates editing, rounding, color, and removal actions to the view model.
 */
@Composable
fun ConverterListScreen(
    vm: MainViewModel,
    favoritesOnly: Boolean,
    onSelectCurrencies: () -> Unit
) {
    // The favorite destination is a filtered view of the same selected list, not a second selection store.
    val visible = vm.selectedCurrencies.filter { !favoritesOnly || it.id in vm.favoriteIds }

    // Keep first-use guidance in place of an empty list so the primary action is immediately visible.
    if (visible.isEmpty()) {
        Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)) {
                Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        if (favoritesOnly) "No favorites yet" else "Welcome to Currency Converter!",
                        style = MaterialTheme.typography.headlineSmall,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        if (favoritesOnly) "Tap the star when choosing currencies to find them here quickly."
                        else "Choose the currencies you want to compare. They will appear here. Tap an amount to change the conversion.",
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(20.dp))
                    Button(onClick = onSelectCurrencies) {
                        Text(if (favoritesOnly) "Choose favorites" else "Choose currencies")
                    }
                }
            }
        }
        return
    }

    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            Column(Modifier.padding(start = 4.dp, bottom = 8.dp)) {
                Text(if (favoritesOnly) "Your favorites" else "Your currencies", style = MaterialTheme.typography.headlineSmall)
                Text("Tap an amount to convert all values.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        items(visible, key = { it.id }) { c ->
            // Values and callbacks come from the shared view model so all cards use one conversion source.
            CurrencyRow(
                currency = c,
                value = vm.displayValue(c),
                onRemove = { vm.removeFromSelection(c.id) },
                onValueChange = { vm.onValueChange(c.id, it) },
                onRoundDown = { vm.roundDown(c) },
                onRoundUp = { vm.roundUp(c) },
                onSetToOne = { vm.setToOne(c) },
                onQuickAmount = { vm.setQuickAmount(c, it) },
                selectedColor = vm.currencyColors[c.id],
                onColorSelected = { vm.setCurrencyColor(c.id, it) }
            )
        }
    }
}
