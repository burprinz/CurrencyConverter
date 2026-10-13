package com.example.currencyconverter.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import com.example.currencyconverter.ui.LocalizedText as Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.currencyconverter.data.model.Currency
import com.example.currencyconverter.ui.MainViewModel
import com.example.currencyconverter.ui.components.CurrencyFlag

/** Select/deselect currencies and mark favorites. Two dropdown sections. */
/** Lets users search, favorite, and select currencies before returning to the converter. */
@Composable
fun SelectScreen(vm: MainViewModel, onDone: () -> Unit) {
    var query by rememberSaveable { mutableStateOf("") }
    var realOpen by rememberSaveable { mutableStateOf(true) }
    var customOpen by rememberSaveable { mutableStateOf(false) }

    val q = query.trim()
    val matches: (Currency) -> Boolean = { c ->
        q.isEmpty() || c.name.contains(q, ignoreCase = true) || c.id.contains(q, ignoreCase = true)
    }
    val real = vm.realCurrencies.filter(matches)
    val custom = vm.customAndFunCurrencies.filter(matches).sortedBy { it.name.lowercase() }

    // While searching, both sections are opened automatically.
    val realShown = realOpen || q.isNotEmpty()
    val customShown = customOpen || q.isNotEmpty()

    Column(Modifier.fillMaxSize()) {
    Card(
        Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("Choose currencies", style = MaterialTheme.typography.titleMedium)
            Text("Check each currency you want to compare. When you're done, tap ‘Done – convert’. ", style = MaterialTheme.typography.bodyMedium)
            Text("Tap the star to mark a favorite.", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 4.dp))
            Button(onClick = onDone, modifier = Modifier.padding(top = 12.dp)) {
                Text("Done – convert")
            }
        }
    }
    OutlinedTextField(
        value = query,
        onValueChange = { query = it },
        label = { Text("Search currencies") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth().padding(12.dp)
    )
    LazyColumn(Modifier.weight(1f).fillMaxWidth(), contentPadding = PaddingValues(bottom = 16.dp)) {
        item { SectionHeader("Currencies (${real.size})", realShown) { realOpen = !realOpen } }
        if (realShown) {
            if (real.isEmpty()) {
                item { Hint("No currencies available. Check your connection and refresh rates.") }
            } else {
                items(real, key = { it.id }) { c -> SelectRow(vm, c) }
            }
        }

        item { SectionHeader("Custom and fan currencies (${custom.size})", customShown) { customOpen = !customOpen } }
        if (customShown) {
            if (custom.isEmpty()) {
                item { Hint("Nothing here yet. Create a custom currency in the menu or unlock fan currencies.") }
            } else {
                items(custom, key = { it.id }) { c -> SelectRow(vm, c) }
            }
        }
    }
    }
}

/** Collapsible heading for a group in the currency chooser. */
@Composable
private fun SectionHeader(title: String, expanded: Boolean, onClick: () -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)
    ) {
        Row(
            Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
            Icon(
                if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = if (expanded) "Collapse section" else "Expand section"
            )
        }
    }
}

/** One currency choice with its current selection and favorite state. */
@Composable
private fun SelectRow(vm: MainViewModel, c: Currency) {
    val selected = c.id in vm.selectedIds
    val favorite = c.id in vm.favoriteIds
    Row(
        Modifier.fillMaxWidth().clickable { vm.toggleSelected(c.id) }.padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked = selected, onCheckedChange = { vm.toggleSelected(c.id) })
        CurrencyFlag(c)
        Column(Modifier.weight(1f)) {
            Text(c.name, style = MaterialTheme.typography.bodyLarge)
            Text(
                c.subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        IconButton(onClick = { vm.toggleFavorite(c.id) }) {
            Icon(
                if (favorite) Icons.Default.Star else Icons.Outlined.Star,
                contentDescription = if (favorite) "Remove from favorites" else "Mark as favorite"
            )
        }
    }
}

/** Reusable helper for instructional or empty-state text. */
@Composable
private fun Hint(text: String) {
    Text(text, modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.bodyMedium)
}
