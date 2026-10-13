package com.example.currencyconverter.ui

import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text as MaterialText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit

val LocalAppLanguage = staticCompositionLocalOf { "en" }

private val german = mapOf(
    "Convert" to "Umrechnen", "Favorites" to "Favoriten", "Rate history" to "Kursverlauf", "Camera converter" to "Kamera-Umrechner", "Choose currencies" to "Währungen auswählen", "Custom currencies" to "Eigene Währungen", "Fan currencies" to "Fan-Währungen", "Settings" to "Einstellungen",
    "Currency Converter" to "Währungsumrechner", "Undo" to "Rückgängig", "Menu" to "Menü", "Refresh rates" to "Kurse aktualisieren",
    "No favorites yet" to "Noch keine Favoriten", "Tap the star when choosing currencies to find them here quickly." to "Tippe bei der Währungsauswahl auf den Stern, um Favoriten hier schnell wiederzufinden.", "Welcome to Currency Converter!" to "Willkommen beim Währungsumrechner!", "Choose the currencies you want to compare. They will appear here. Tap an amount to change the conversion." to "Wähle die Währungen aus, die du vergleichen möchtest. Tippe auf einen Betrag, um die Umrechnung zu ändern.", "Choose favorites" to "Favoriten auswählen", "Tap an amount to convert all values." to "Tippe auf einen Betrag, um alle Werte umzurechnen.", "Your favorites" to "Deine Favoriten", "Your currencies" to "Deine Währungen",
    "Amount" to "Betrag", "Default" to "Standard", "Camera access required" to "Kamerazugriff erforderlich", "The camera is used only for live text recognition and the optional photo." to "Die Kamera wird nur zur Texterkennung in Echtzeit und für optionale Fotos verwendet.", "Allow camera" to "Kamera erlauben", "Recognized conversion" to "Erkannte Umrechnung", "Analyze photo" to "Foto analysieren", "Discard photo" to "Foto verwerfen",
    "Exchange rate history" to "Wechselkursverlauf", "Day" to "Tag", "Week" to "Woche", "Month" to "Monat", "Year" to "Jahr", "All time" to "Gesamt", "increase" to "Anstieg", "decrease" to "Rückgang", "Only one published rate is available for this period." to "Für diesen Zeitraum ist nur ein veröffentlichter Kurs verfügbar.", "Daily reference rates. Weekends and holidays may have no new data." to "Tägliche Referenzkurse. An Wochenenden und Feiertagen gibt es möglicherweise keine neuen Daten.", "Historical data by Frankfurter" to "Historische Daten von Frankfurter", "Historical rates could not be loaded. Check your connection or choose another currency pair." to "Historische Kurse konnten nicht geladen werden. Prüfe die Verbindung oder wähle ein anderes Währungspaar.", "Retry" to "Erneut versuchen", "Select two different currencies to view their history." to "Wähle zwei verschiedene Währungen, um ihren Verlauf zu sehen.", "Search currency" to "Währung suchen", "Exchange rate (\u0024quote per 1 \u0024base)" to "Wechselkurs (\u0024quote je 1 \u0024base)", "Date" to "Datum",
    "1. Choose your currencies" to "1. Währungen auswählen", "Check each currency you want to compare. When you're done, tap ‘Done – convert’. " to "Wähle alle Währungen aus, die du vergleichen möchtest. Tippe anschließend auf „Fertig – umrechnen“.", "Tap the star to mark a favorite." to "Tippe auf den Stern, um eine Währung als Favorit zu markieren.", "Done – convert" to "Fertig – umrechnen", "Search currencies" to "Währungen suchen", "No currencies available. Check your connection and refresh rates." to "Keine Währungen verfügbar. Prüfe die Verbindung und aktualisiere die Kurse.", "Nothing here yet. Create a custom currency in the menu or unlock fan currencies." to "Hier ist noch nichts. Erstelle im Menü eine eigene Währung oder schalte Fan-Währungen frei.", "Collapse section" to "Bereich einklappen", "Expand section" to "Bereich ausklappen", "Remove from favorites" to "Aus Favoriten entfernen", "Mark as favorite" to "Als Favorit markieren",
    "Add special currencies to your conversion list." to "Füge besondere Währungen zu deiner Umrechnungsliste hinzu.", "Added" to "Hinzugefügt", "Add" to "Hinzufügen", "About fan currencies" to "Über Fan-Währungen", "Most fan currency values are community estimates, not official exchange rates. They are for comparison only and may differ from real-world values." to "Die meisten Fan-Währungswerte sind Schätzungen der Community und keine offiziellen Wechselkurse. Sie dienen nur zum Vergleich und können von echten Werten abweichen.", "Got it" to "Verstanden",
    "Create custom currency" to "Eigene Währung erstellen", "Edit currency" to "Währung bearbeiten", "Name (e.g. gold coins)" to "Name (z. B. Goldmünzen)", "Set a value relative to a reference currency:" to "Lege einen Wert im Verhältnis zu einer Referenzwährung fest:", "Enter how many units of the reference currency one unit of your currency is worth." to "Gib an, wie viele Einheiten der Referenzwährung eine Einheit deiner Währung wert ist.", "unit" to "Einheit", "Choose reference currency" to "Referenzwährung auswählen", "Create" to "Erstellen", "Save changes" to "Änderungen speichern", "Cancel" to "Abbrechen", "Go to converter" to "Zum Umrechner", "Your custom currencies" to "Deine eigenen Währungen", "You have not created any custom currencies yet." to "Du hast noch keine eigenen Währungen erstellt.", "Edit" to "Bearbeiten", "Delete" to "Löschen", "Delete currency?" to "Währung löschen?", "Delete" to "Löschen",
    "Light" to "Hell", "Dark" to "Dunkel", "System" to "System", "Language" to "Sprache", "Theme" to "Darstellung", "English" to "Englisch", "German" to "Deutsch", "Spanish" to "Spanisch",
    "Rates By Exchange Rate API" to "Kurse von Exchange Rate API", "never" to "nie", "Updating rates … (as of " to "Kurse werden aktualisiert … (Stand: ", "Offline – last update: " to "Offline – letzte Aktualisierung: ", "Last rate update: " to "Letzte Kursaktualisierung: ",
    "Load rates to choose currencies." to "Lade die Kurse, um Währungen auszuwählen.", "Point the camera at an amount. The largest detected number is used." to "Richte die Kamera auf einen Betrag. Die größte erkannte Zahl wird verwendet.", "Conversion is currently unavailable for these currencies." to "Für diese Währungen ist die Umrechnung derzeit nicht verfügbar.", "Recognition cannot start without camera access." to "Ohne Kamerazugriff kann die Erkennung nicht starten.", "The camera image could not be captured." to "Das Kamerabild konnte nicht aufgenommen werden.", "Analyzing photo on this device …" to "Foto wird auf diesem Gerät analysiert …", "No number detected. Reposition the camera and try again." to "Keine Zahl erkannt. Richte die Kamera neu aus und versuche es erneut.", "The photo could not be read." to "Das Foto konnte nicht gelesen werden.", "Photo discarded. Live recognition continues." to "Foto verworfen. Die Live-Erkennung läuft weiter.", "Text recognition runs on this device. Photos are not saved to your gallery." to "Die Texterkennung läuft auf diesem Gerät. Fotos werden nicht in der Galerie gespeichert.", "Live text recognition is currently unavailable." to "Die Live-Texterkennung ist derzeit nicht verfügbar.", "The rear camera could not be started." to "Die Rückkamera konnte nicht gestartet werden.",
    "Enter a name." to "Gib einen Namen ein.", "Enter a finite value greater than 0." to "Gib einen gültigen Wert größer als 0 ein.", "The reference currency is not available yet. Wait for rates to load." to "Die Referenzwährung ist noch nicht verfügbar. Warte, bis die Kurse geladen sind.", "A currency with this name already exists." to "Eine Währung mit diesem Namen ist bereits vorhanden.", "Currency added and selected for conversion." to "Währung hinzugefügt und für die Umrechnung ausgewählt.", "Changes saved." to "Änderungen gespeichert.", "About rates" to "Über die Kurse", "Exchange rates are provided by Frankfurter. Fan currency values are estimates." to "Die Wechselkurse werden von Frankfurter bereitgestellt. Fan-Währungswerte sind Schätzungen."
)

private val spanish = mapOf(
    "Convert" to "Convertir", "Favorites" to "Favoritos", "Rate history" to "Historial de tipos", "Camera converter" to "Conversor con cámara", "Choose currencies" to "Elegir monedas", "Custom currencies" to "Monedas personalizadas", "Fan currencies" to "Monedas de fans", "Settings" to "Ajustes",
    "Currency Converter" to "Conversor de monedas", "Undo" to "Deshacer", "Menu" to "Menú", "Refresh rates" to "Actualizar tipos",
    "No favorites yet" to "Aún no hay favoritos", "Tap the star when choosing currencies to find them here quickly." to "Toca la estrella al elegir monedas para encontrarlas aquí rápidamente.", "Welcome to Currency Converter!" to "¡Te damos la bienvenida al conversor de monedas!", "Choose the currencies you want to compare. They will appear here. Tap an amount to change the conversion." to "Elige las monedas que quieres comparar. Aparecerán aquí. Toca un importe para cambiar la conversión.", "Choose favorites" to "Elegir favoritos", "Tap an amount to convert all values." to "Toca un importe para convertir todos los valores.", "Your favorites" to "Tus favoritos", "Your currencies" to "Tus monedas",
    "Amount" to "Importe", "Default" to "Predeterminado", "Camera access required" to "Se necesita acceso a la cámara", "The camera is used only for live text recognition and the optional photo." to "La cámara solo se usa para reconocer texto en directo y para la foto opcional.", "Allow camera" to "Permitir cámara", "Recognized conversion" to "Conversión reconocida", "Analyze photo" to "Analizar foto", "Discard photo" to "Descartar foto",
    "Exchange rate history" to "Historial del tipo de cambio", "Day" to "Día", "Week" to "Semana", "Month" to "Mes", "Year" to "Año", "All time" to "Todo el tiempo", "increase" to "aumento", "decrease" to "disminución", "Only one published rate is available for this period." to "Solo hay un tipo publicado disponible para este periodo.", "Daily reference rates. Weekends and holidays may have no new data." to "Tipos de referencia diarios. Puede que no haya datos nuevos en fines de semana y festivos.", "Historical data by Frankfurter" to "Datos históricos de Frankfurter", "Historical rates could not be loaded. Check your connection or choose another currency pair." to "No se pudo cargar el historial. Comprueba la conexión o elige otro par de monedas.", "Retry" to "Reintentar", "Select two different currencies to view their history." to "Elige dos monedas distintas para ver su historial.", "Search currency" to "Buscar moneda", "Exchange rate (\u0024quote per 1 \u0024base)" to "Tipo de cambio (\u0024quote por 1 \u0024base)", "Date" to "Fecha",
    "1. Choose your currencies" to "1. Elige tus monedas", "Check each currency you want to compare. When you're done, tap ‘Done – convert’. " to "Marca cada moneda que quieras comparar. Cuando termines, toca «Listo – convertir».", "Tap the star to mark a favorite." to "Toca la estrella para marcar un favorito.", "Done – convert" to "Listo – convertir", "Search currencies" to "Buscar monedas", "No currencies available. Check your connection and refresh rates." to "No hay monedas disponibles. Comprueba la conexión y actualiza los tipos.", "Nothing here yet. Create a custom currency in the menu or unlock fan currencies." to "Aún no hay nada. Crea una moneda personalizada en el menú o desbloquea monedas de fans.", "Collapse section" to "Contraer sección", "Expand section" to "Expandir sección", "Remove from favorites" to "Quitar de favoritos", "Mark as favorite" to "Marcar como favorito",
    "Add special currencies to your conversion list." to "Añade monedas especiales a tu lista de conversión.", "Added" to "Añadida", "Add" to "Añadir", "About fan currencies" to "Sobre las monedas de fans", "Most fan currency values are community estimates, not official exchange rates. They are for comparison only and may differ from real-world values." to "La mayoría de los valores de monedas de fans son estimaciones de la comunidad, no tipos oficiales. Solo sirven para comparar y pueden diferir de los valores reales.", "Got it" to "Entendido",
    "Create custom currency" to "Crear moneda personalizada", "Edit currency" to "Editar moneda", "Name (e.g. gold coins)" to "Nombre (p. ej., monedas de oro)", "Set a value relative to a reference currency:" to "Establece un valor respecto a una moneda de referencia:", "Enter how many units of the reference currency one unit of your currency is worth." to "Indica cuántas unidades de la moneda de referencia vale una unidad de tu moneda.", "unit" to "unidad", "Choose reference currency" to "Elegir moneda de referencia", "Create" to "Crear", "Save changes" to "Guardar cambios", "Cancel" to "Cancelar", "Go to converter" to "Ir al conversor", "Your custom currencies" to "Tus monedas personalizadas", "You have not created any custom currencies yet." to "Todavía no has creado monedas personalizadas.", "Edit" to "Editar", "Delete" to "Eliminar", "Delete currency?" to "¿Eliminar moneda?",
    "Light" to "Claro", "Dark" to "Oscuro", "System" to "Sistema", "Language" to "Idioma", "Theme" to "Tema", "English" to "Inglés", "German" to "Alemán", "Spanish" to "Español",
    "Rates By Exchange Rate API" to "Tipos de Exchange Rate API", "never" to "nunca", "Offline – last update: " to "Sin conexión – última actualización: ", "Last rate update: " to "Última actualización del tipo: ",
    "Load rates to choose currencies." to "Carga los tipos para elegir monedas.", "Point the camera at an amount. The largest detected number is used." to "Enfoca un importe con la cámara. Se usa el número detectado más grande.", "Conversion is currently unavailable for these currencies." to "La conversión no está disponible actualmente para estas monedas.", "Recognition cannot start without camera access." to "El reconocimiento no puede comenzar sin acceso a la cámara.", "The camera image could not be captured." to "No se pudo capturar la imagen de la cámara.", "Analyzing photo on this device …" to "Analizando la foto en este dispositivo …", "No number detected. Reposition the camera and try again." to "No se detectó ningún número. Ajusta la cámara e inténtalo de nuevo.", "The photo could not be read." to "No se pudo leer la foto.", "Photo discarded. Live recognition continues." to "Foto descartada. El reconocimiento en directo continúa.", "Text recognition runs on this device. Photos are not saved to your gallery." to "El reconocimiento de texto se ejecuta en este dispositivo. Las fotos no se guardan en la galería.", "Live text recognition is currently unavailable." to "El reconocimiento de texto en directo no está disponible.", "The rear camera could not be started." to "No se pudo iniciar la cámara trasera.",
    "Enter a name." to "Introduce un nombre.", "Enter a finite value greater than 0." to "Introduce un valor válido mayor que 0.", "The reference currency is not available yet. Wait for rates to load." to "La moneda de referencia aún no está disponible. Espera a que se carguen los tipos.", "A currency with this name already exists." to "Ya existe una moneda con ese nombre.", "Currency added and selected for conversion." to "Moneda añadida y seleccionada para la conversión.", "Changes saved." to "Cambios guardados.", "About rates" to "Sobre los tipos", "Exchange rates are provided by Frankfurter. Fan currency values are estimates." to "Frankfurter proporciona los tipos de cambio. Los valores de las monedas de fans son estimaciones."
)

@Composable
fun LocalizedText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null,
    fontFamily: FontFamily? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
    onTextLayout: (TextLayoutResult) -> Unit = {},
    style: TextStyle = LocalTextStyle.current
) {
    val language = LocalAppLanguage.current
    val translated = when {
        language == "de" && text.startsWith("Currencies (") -> text.replace("Currencies (", "Währungen (")
        language == "de" && text.startsWith("Custom and fan currencies (") -> text.replace("Custom and fan currencies (", "Eigene Währungen und Fan-Währungen (")
        language == "es" && text.startsWith("Currencies (") -> text.replace("Currencies (", "Monedas (")
        language == "es" && text.startsWith("Custom and fan currencies (") -> text.replace("Custom and fan currencies (", "Monedas personalizadas y de fans (")
        language == "de" && text.startsWith("Last rate update: ") -> text.replace("Last rate update: ", "Letzte Kursaktualisierung: ")
        language == "de" && text.startsWith("Offline – last update: ") -> text.replace("Offline – last update: ", "Offline – letzte Aktualisierung: ")
        language == "de" && text.startsWith("Updating rates … (as of ") -> text.replace("Updating rates … (as of ", "Kurse werden aktualisiert … (Stand: ")
        language == "es" && text.startsWith("Last rate update: ") -> text.replace("Last rate update: ", "Última actualización del tipo: ")
        language == "es" && text.startsWith("Offline – last update: ") -> text.replace("Offline – last update: ", "Sin conexión – última actualización: ")
        language == "es" && text.startsWith("Updating rates … (as of ") -> text.replace("Updating rates … (as of ", "Actualizando tipos … (a fecha de ")
        language == "de" && text.contains(" increase over ") -> text.replace(" increase over ", " Anstieg im Zeitraum ")
        language == "de" && text.contains(" decrease over ") -> text.replace(" decrease over ", " Rückgang im Zeitraum ")
        language == "es" && text.contains(" increase over ") -> text.replace(" increase over ", " de aumento en ")
        language == "es" && text.contains(" decrease over ") -> text.replace(" decrease over ", " de descenso en ")
        else -> when (language) { "de" -> german[text]; "es" -> spanish[text]; else -> null } ?: text
    }
    MaterialText(
        text = translated, modifier = modifier, color = color, fontSize = fontSize, fontStyle = fontStyle,
        fontWeight = fontWeight, fontFamily = fontFamily, letterSpacing = letterSpacing,
        textDecoration = textDecoration, textAlign = textAlign, lineHeight = lineHeight,
        overflow = overflow, softWrap = softWrap, maxLines = maxLines, minLines = minLines,
        onTextLayout = onTextLayout, style = style
    )
}

@Composable
fun ProvideAppLanguage(language: String, content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalAppLanguage provides language, content = content)
}
