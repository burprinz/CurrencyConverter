package com.example.currencyconverter.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.currencyconverter.data.model.Currency
import com.example.currencyconverter.ui.MainViewModel
import com.example.currencyconverter.util.formatAmount
import com.example.currencyconverter.util.parseAmount

/** Create, edit and remove own currencies. */
@Composable
fun AddCurrencyScreen(vm: MainViewModel) {
    var editingId by rememberSaveable { mutableStateOf<String?>(null) }
    var name by rememberSaveable { mutableStateOf("") }
    var refCode by rememberSaveable { mutableStateOf("USD") }
    var factorText by rememberSaveable { mutableStateOf("1") }
    var error by remember { mutableStateOf<String?>(null) }
    var menuOpen by remember { mutableStateOf(false) }
    var pendingDelete by remember { mutableStateOf<Currency?>(null) }

    val refOptions = vm.realCurrencies.map { it.id }.sorted().ifEmpty { listOf("USD") }

    fun resetForm() {
        editingId = null
        name = ""
        refCode = "USD"
        factorText = "1"
        error = null
    }

    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 16.dp)) {
        item {
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    if (editingId == null) "New currency" else "Edit currency",
                    style = MaterialTheme.typography.titleMedium
                )
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name (e.g. Gold Coins)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Reference currency for conversion:", style = MaterialTheme.typography.bodyMedium)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("1 ${name.ifBlank { "unit" }} =")
                    OutlinedTextField(
                        value = factorText,
                        onValueChange = { factorText = it.filter { c -> c.isDigit() || c == '.' || c == ',' } },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                    Box {
                        OutlinedButton(onClick = { menuOpen = true }) {
                            Text(refCode)
                            Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Choose reference currency")
                        }
                        DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                            refOptions.forEach { code ->
                                DropdownMenuItem(
                                    text = { Text(code) },
                                    onClick = { refCode = code; menuOpen = false }
                                )
                            }
                        }
                    }
                }
                error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = {
                        val result = vm.saveCustom(editingId, name, refCode, parseAmount(factorText))
                        if (result == null) resetForm() else error = result
                    }) { Text(if (editingId == null) "Create" else "Save changes") }
                    if (editingId != null) {
                        TextButton(onClick = { resetForm() }) { Text("Cancel") }
                    }
                }
            }
            HorizontalDivider()
            Text(
                "Your currencies",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(12.dp)
            )
            if (vm.customCurrencies.isEmpty()) {
                Text("You haven't created any currencies yet.", modifier = Modifier.padding(horizontal = 12.dp))
            }
        }

        items(vm.customCurrencies, key = { it.id }) { c ->
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(c.name, style = MaterialTheme.typography.titleMedium)
                    Text(c.subtitle, style = MaterialTheme.typography.bodySmall)
                }
                IconButton(onClick = {
                    editingId = c.id
                    name = c.name
                    refCode = c.refCode ?: "USD"
                    factorText = formatAmount(c.factor)
                    error = null
                }) { Icon(Icons.Default.Edit, contentDescription = "Edit") }
                IconButton(onClick = { pendingDelete = c }) {
                    Icon(Icons.Default.Delete, contentDescription = "Remove")
                }
            }
        }
    }

    pendingDelete?.let { c ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("Remove currency?") },
            text = { Text("\"${c.name}\" will be deleted.") },
            confirmButton = {
                TextButton(onClick = {
                    if (editingId == c.id) resetForm()
                    vm.deleteCustom(c.id)
                    pendingDelete = null
                }) { Text("Remove") }
            },
            dismissButton = { TextButton(onClick = { pendingDelete = null }) { Text("Cancel") } }
        )
    }
}
