package com.example.currencyconverter.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.clickable
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import com.example.currencyconverter.ui.LocalizedText as Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.currencyconverter.ui.MainViewModel

@Composable
fun SettingsScreen(vm: MainViewModel) {
    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
        Text("Theme", style = MaterialTheme.typography.titleLarge)
        listOf("system" to "System", "light" to "Light", "dark" to "Dark").forEach { (value, label) ->
            Row(Modifier.fillMaxWidth().clickable { vm.changeThemeMode(value) }, verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = vm.themeMode == value, onClick = { vm.changeThemeMode(value) })
                Text(label)
            }
        }
        Text("Language", style = MaterialTheme.typography.titleLarge)
        listOf("en" to "English", "de" to "German", "es" to "Spanish").forEach { (code, label) ->
            Row(Modifier.fillMaxWidth().clickable { vm.changeLanguage(code) }, verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = vm.language == code, onClick = { vm.changeLanguage(code) })
                Text(label)
            }
        }
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("About rates", style = MaterialTheme.typography.titleMedium)
                Text("Exchange rates are provided by Frankfurter. Fan currency values are estimates.", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
