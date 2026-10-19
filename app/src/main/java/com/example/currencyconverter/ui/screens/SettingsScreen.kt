package com.example.currencyconverter.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import androidx.compose.material3.Button
import androidx.compose.ui.platform.LocalContext
import com.example.currencyconverter.AdMobState
import com.google.android.ump.UserMessagingPlatform

/** One selectable locale, its representative flag, and alternate names accepted by search. */
private data class LanguageOption(val code: String, val nativeName: String, val flag: String, val searchNames: List<String>)

private tailrec fun Context.findActivity(): Activity? {
    return when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }
}

/**
 * Theme and language preferences plus short notes about the app's rate sources.
 * The language catalog keeps native names, common search aliases, and a flag together; filtering
 * changes only the visible options, while the selected language remains owned by the view model.
 */
@Composable
fun SettingsScreen(vm: MainViewModel) {
    val context = LocalContext.current

    var languageQuery by rememberSaveable { mutableStateOf("") }
    // Native names and aliases travel together so search can accept either spelling without changing the label.
    val languages = listOf(
        LanguageOption("en", "English", "🇬🇧", listOf("English", "Englisch", "Inglés")),
        LanguageOption("de", "Deutsch", "🇩🇪", listOf("Deutsch", "German", "Alemán")),
        LanguageOption("es", "Español", "🇪🇸", listOf("Español", "Spanish", "Spanisch")),
        LanguageOption("zh", "简体中文", "🇨🇳", listOf("Chinese", "中文", "Chinesisch", "Mandarin", "简体中文", "Chinese (Simplified)")),
        LanguageOption("ja", "日本語", "🇯🇵", listOf("Japanese", "日本語", "Japanisch", "Japonés")),
        LanguageOption("hi", "हिन्दी", "🇮🇳", listOf("Hindi", "हिन्दी", "Indisch")),
        LanguageOption("ar", "العربية", "🇸🇦", listOf("Arabic", "العربية", "Arabisch", "Árabe")),
        LanguageOption("fr", "Français", "🇫🇷", listOf("French", "Français", "Französisch", "Francés")),
        LanguageOption("bn", "বাংলা", "🇧🇩", listOf("Bengali", "Bangla", "বাংলা", "Bengalisch")),
        LanguageOption("pt", "Português", "🇧🇷", listOf("Portuguese", "Português", "Portugiesisch", "Portugués")),
        LanguageOption("ru", "Русский", "🇷🇺", listOf("Russian", "Русский", "Russisch", "Ruso")),
        LanguageOption("id", "Bahasa Indonesia", "🇮🇩", listOf("Indonesian", "Bahasa Indonesia", "Indonesisch")),
        LanguageOption("ur", "اردو", "🇵🇰", listOf("Urdu", "اردو")),
        LanguageOption("tr", "Türkçe", "🇹🇷", listOf("Turkish", "Türkçe", "Türkisch", "Turco")),
        LanguageOption("vi", "Tiếng Việt", "🇻🇳", listOf("Vietnamese", "Tiếng Việt", "Vietnamesisch", "Vietnamita")),
        LanguageOption("th", "ไทย", "🇹🇭", listOf("Thai", "ไทย", "Thailändisch")),
        LanguageOption("ko", "한국어", "🇰🇷", listOf("Korean", "한국어", "Koreanisch")),
        LanguageOption("it", "Italiano", "🇮🇹", listOf("Italian", "Italiano", "Italienisch")),
        LanguageOption("nl", "Nederlands", "🇳🇱", listOf("Dutch", "Nederlands", "Holländisch", "Niederländisch")),
        LanguageOption("pl", "Polski", "🇵🇱", listOf("Polish", "Polski", "Polnisch")),
        LanguageOption("ms", "Bahasa Melayu", "🇲🇾", listOf("Malay", "Bahasa Melayu", "Malaiisch")),
        LanguageOption("sw", "Kiswahili", "🇰🇪", listOf("Swahili", "Kiswahili", "Suaheli"))
    )
    // Search accepts the localized name, the language's own name, and common translations of its name.
    val visibleLanguages = languages.filter { option ->
        // Search aliases are intentionally separate from the displayed native name (e.g. German finds Deutsch).
        languageQuery.isBlank() || option.searchNames.any { it.contains(languageQuery.trim(), ignoreCase = true) } ||
            option.nativeName.contains(languageQuery.trim(), ignoreCase = true)
    }
    // A vertical scroll container is needed because the language list is taller than many screens.
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Text(
            "Privacy",
            style = MaterialTheme.typography.titleLarge
        )

        Button(
            onClick = {
                val activity = context.findActivity()

                if (activity == null) {
                    Log.e("AdMobConsent", "Activity not found.")
                } else {
                    UserMessagingPlatform.showPrivacyOptionsForm(activity) { formError ->
                        if (formError != null) {
                            Log.e(
                                "AdMobConsent",
                                "Privacy options error: " +
                                        "${formError.errorCode} ${formError.message}"
                            )
                        } else {
                            Log.d(
                                "AdMobConsent",
                                "Privacy options form closed."
                            )
                        }

                        // Den aktuellen Einwilligungsstatus erneut prüfen.
                        val consentInformation =
                            UserMessagingPlatform.getConsentInformation(activity)

                        val adsAllowed = consentInformation.canRequestAds()
                        AdMobState.updateAdsReady(adsAllowed)

                        Log.d(
                            "AdMobConsent",
                            "Ads allowed after privacy change: $adsAllowed"
                        )
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Datenschutzeinstellungen")
        }
        Text("Theme", style = MaterialTheme.typography.titleLarge)
        listOf("system" to "System", "light" to "Light", "dark" to "Dark").forEach { (value, label) ->
            // Clicking either the row or radio button changes the same persisted preference.
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
            // A tap changes the global app language; the list stays in each language's own name for easy recognition.
            Row(Modifier.fillMaxWidth().clickable { vm.changeLanguage(language.code) }, verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = vm.language == language.code, onClick = { vm.changeLanguage(language.code) })
                Text(language.flag, modifier = Modifier.padding(end = 10.dp))
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
