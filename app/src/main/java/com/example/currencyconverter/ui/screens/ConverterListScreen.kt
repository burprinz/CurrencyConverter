package com.example.currencyconverter.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.currencyconverter.ui.MainViewModel
import com.example.currencyconverter.ui.components.CurrencyRow

/** Main conversion view and favorites view. */
@Composable
fun ConverterListScreen(
    vm: MainViewModel,
    favoritesOnly: Boolean,
    onSelectCurrencies: () -> Unit
) {
    val visible = vm.selectedCurrencies.filter { !favoritesOnly || it.id in vm.favoriteIds }

    if (visible.isEmpty()) {
        Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)) {
                Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        if (favoritesOnly) "Noch keine Favoriten" else "Willkommen beim Währungsrechner!",
                        style = MaterialTheme.typography.headlineSmall,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        if (favoritesOnly) "Markiere beim Auswählen einen Stern, um Währungen hier schnell wiederzufinden."
                        else "Wähle zuerst die Währungen aus, die du vergleichen möchtest. Danach erscheinen sie hier. Tippe auf einen Betrag, um die Umrechnung zu ändern.",
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(20.dp))
                    Button(onClick = onSelectCurrencies) {
                        Text(if (favoritesOnly) "Favoriten auswählen" else "Währungen auswählen")
                    }
                }
            }
        }
        return
    }

    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            Column(Modifier.padding(start = 4.dp, bottom = 8.dp)) {
                Text(if (favoritesOnly) "Deine Favoriten" else "Deine Währungen", style = MaterialTheme.typography.headlineSmall)
                Text("Tippe auf einen Betrag, um alle Werte umzurechnen.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        items(visible, key = { it.id }) { c ->
            CurrencyRow(
                currency = c,
                value = vm.displayValue(c),
                onRemove = { vm.removeFromSelection(c.id) },
                onValueChange = { vm.onValueChange(c.id, it) },
                onRoundDown = { vm.roundDown(c) },
                onRoundUp = { vm.roundUp(c) },
                onSetToOne = { vm.setToOne(c) }
            )
        }
    }
}
