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
import androidx.compose.material3.OutlinedTextField
import com.example.currencyconverter.ui.LocalizedText as Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.currencyconverter.ui.MainViewModel

private data class LanguageOption(val code: String, val nativeName: String, val searchNames: List<String>)

@Composable
fun SettingsScreen(vm: MainViewModel) {
    var languageQuery by rememberSaveable { mutableStateOf("") }
    val languages = listOf(
        LanguageOption("en", "English", listOf("English", "Englisch", "Inglés")),
        LanguageOption("de", "Deutsch", listOf("Deutsch", "German", "Alemán")),
        LanguageOption("es", "Español", listOf("Español", "Spanish", "Spanisch")),
        LanguageOption("zh", "简体中文", listOf("Chinese", "中文", "Chinesisch", "Mandarin", "简体中文", "Chinese (Simplified)")),
        LanguageOption("ja", "日本語", listOf("Japanese", "日本語", "Japanisch", "Japonés"))
    )
    val visibleLanguages = languages.filter { option ->
        languageQuery.isBlank() || option.searchNames.any { it.contains(languageQuery.trim(), ignoreCase = true) } ||
            option.nativeName.contains(languageQuery.trim(), ignoreCase = true)
    }
    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
        Text("Theme", style = MaterialTheme.typography.titleLarge)
        listOf("system" to "System", "light" to "Light", "dark" to "Dark").forEach { (value, label) ->
            Row(Modifier.fillMaxWidth().clickable { vm.changeThemeMode(value) }, verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = vm.themeMode == value, onClick = { vm.changeThemeMode(value) })
                Text(label)
            }
        }
        Text("Language", style = MaterialTheme.typography.titleLarge)
        OutlinedTextField(
            value = languageQuery,
            onValueChange = { languageQuery = it },
            label = { Text("Search language") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        visibleLanguages.forEach { language ->
            Row(Modifier.fillMaxWidth().clickable { vm.changeLanguage(language.code) }, verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = vm.language == language.code, onClick = { vm.changeLanguage(language.code) })
                Text(language.nativeName)
            }
        }
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("About rates", style = MaterialTheme.typography.titleMedium)
                Text("Exchange rates are provided by Frankfurter.", style = MaterialTheme.typography.bodyMedium)
                Text("Fan currency values are estimates.", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
