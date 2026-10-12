package com.example.currencyconverter.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.currencyconverter.data.model.FunCurrencies
import com.example.currencyconverter.ui.MainViewModel

/** Fan currencies with an explanation of their estimated values. */
@Composable
fun UnlockScreen(vm: MainViewModel) {
    var showInfo by remember { mutableStateOf(false) }

    LazyColumn(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        item {
            Card(
                Modifier.fillMaxWidth().padding(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Row(Modifier.padding(start = 16.dp, end = 8.dp, top = 12.dp, bottom = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Fan-Währungen", style = MaterialTheme.typography.titleMedium)
                        Text("Füge besondere Währungen zu deiner Umrechnungsliste hinzu.", style = MaterialTheme.typography.bodySmall)
                    }
                    IconButton(onClick = { showInfo = true }) {
                        Icon(Icons.Default.Info, contentDescription = "Information zu Fan-Währungen")
                    }
                }
            }
        }
        items(FunCurrencies.all, key = { it.id }) { c ->
            val unlocked = c.id in vm.unlockedIds
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f).padding(end = 12.dp)) {
                    Text(c.name, style = MaterialTheme.typography.titleMedium)
                    Text(c.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Button(onClick = { vm.unlock(c.id) }, enabled = !unlocked) {
                    Text(if (unlocked) "Freigeschaltet" else "Hinzufügen")
                }
            }
            HorizontalDivider()
        }
    }

    if (showInfo) {
        AlertDialog(
            onDismissRequest = { showInfo = false },
            title = { Text("Hinweis zu Fan-Währungen") },
            text = { Text("Die Werte der meisten Fan-Währungen sind Schätzungen aus der Community und keine offiziellen Wechselkurse. Sie dienen nur zum Vergleichen und können vom tatsächlichen Wert abweichen.") },
            confirmButton = { TextButton(onClick = { showInfo = false }) { Text("Verstanden") } }
        )
    }
}
