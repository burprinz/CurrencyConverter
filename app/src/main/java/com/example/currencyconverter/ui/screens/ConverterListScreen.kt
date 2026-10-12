package com.example.currencyconverter.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.currencyconverter.ui.MainViewModel
import com.example.currencyconverter.ui.components.CurrencyRow

/** Used for both "List" (all selected) and "Favorites" (selected + favorite). */
@Composable
fun ConverterListScreen(vm: MainViewModel, favoritesOnly: Boolean) {
    val visible = vm.selectedCurrencies.filter { !favoritesOnly || it.id in vm.favoriteIds }

    if (visible.isEmpty()) {
        Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
            Text(
                if (favoritesOnly) {
                    "No favorites among the selected currencies.\nMenu → Select.. to mark favorites."
                } else {
                    "No currencies selected.\nMenu → Select.. to choose some."
                },
                textAlign = TextAlign.Center
            )
        }
        return
    }

    LazyColumn(
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
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
