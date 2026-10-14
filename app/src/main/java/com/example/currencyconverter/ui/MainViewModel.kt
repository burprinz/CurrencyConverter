package com.example.currencyconverter.ui

import android.app.Application
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.currencyconverter.data.CurrencyRepository
import com.example.currencyconverter.data.FetchState
import com.example.currencyconverter.data.local.AppStorage
import com.example.currencyconverter.data.model.Currency
import com.example.currencyconverter.data.model.CurrencyKind
import com.example.currencyconverter.data.model.FunCurrencies
import com.example.currencyconverter.domain.CurrencyConverter
import com.example.currencyconverter.util.currencyDisplayName
import com.example.currencyconverter.util.formatAmount
import com.example.currencyconverter.util.parseAmount
import com.example.currencyconverter.util.sanitizeAmountInput
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import java.math.RoundingMode

/**
 * Shared screen state and app actions live here so Compose screens stay mostly presentational.
 * [AppStorage] persists user choices, while [CurrencyRepository] loads/caches exchange rates.
 * The view model also derives display lists from the stored IDs and the latest rates; screens send
 * user intent here and observe the resulting Compose state rather than implementing business rules.
 */
class MainViewModel(app: Application) : AndroidViewModel(app) {

    /** Minimal copy of editable state used to restore the UI after an undo action. */
    private data class UndoSnapshot(
        val selectedIds: List<String>,
        val favoriteIds: Set<String>,
        val unlockedIds: Set<String>,
        val customCurrencies: List<Currency>,
        val activeId: String?,
        val activeText: String
    )

    private val storage = AppStorage(app)
    private val repository = CurrencyRepository(storage)

    // ---- persistent state -------------------------------------------------
    var rates by mutableStateOf(repository.cachedRates())
        private set
    var lastUpdateMillis by mutableStateOf(repository.lastUpdateMillis)
        private set
    var fetchState by mutableStateOf(FetchState.IDLE)
        private set

    var selectedIds by mutableStateOf(storage.selectedIds)
        private set
    var favoriteIds by mutableStateOf(storage.favoriteIds)
        private set
    var unlockedIds by mutableStateOf(storage.unlockedIds)
        private set
    var customCurrencies by mutableStateOf(storage.customCurrencies)
        private set
    var currencyColors by mutableStateOf(storage.currencyColors)
        private set
    var language by mutableStateOf(storage.language)
        private set
    var themeMode by mutableStateOf(storage.themeMode)
        private set

    /** Validates and persists a theme choice before Compose redraws the app with its new palette. */
    fun changeThemeMode(mode: String) {
        if (mode !in setOf("light", "dark", "system")) return
        themeMode = mode
        storage.themeMode = mode
    }

    /** Changes the translation key used by [LocalizedText] and stores it for the next app launch. */
    fun changeLanguage(code: String) {
        // Keep this allow-list aligned with the language options and translation tables in SettingsScreen/Localization.
        if (code !in setOf("en", "de", "es", "zh", "ja", "hi", "ar", "fr", "bn", "pt", "ru", "id", "ur", "tr", "vi", "th", "ko", "it", "nl", "pl", "ms", "sw")) return
        language = code
        storage.language = code
    }

    /** Adds a palette choice for [id], or removes it when null requests the theme default. */
    fun setCurrencyColor(id: String, paletteIndex: Int?) {
        currencyColors = if (paletteIndex == null) currencyColors - id else currencyColors + (id to paletteIndex)
        storage.currencyColors = currencyColors
    }

    /** The currency the user typed into last; all other values are derived from it. */
    var activeId by mutableStateOf(storage.activeId)
        private set
    var activeText by mutableStateOf(storage.activeText)
        private set

    /** Remembered pair for the camera converter, independent from the graph's pair. */
    var cameraFromCurrency by mutableStateOf(storage.cameraFromCurrency)
        private set
    var cameraToCurrency by mutableStateOf(storage.cameraToCurrency)
        private set

    /** Remembered pair for the rate history chart. */
    var historyFromCurrency by mutableStateOf(storage.historyFromCurrency)
        private set
    var historyToCurrency by mutableStateOf(storage.historyToCurrency)
        private set
    var customReferenceCurrency by mutableStateOf(storage.customReferenceCurrency)
        private set

    /** Updates both camera selections together so a swap is persisted as one complete pair. */
    fun setCameraCurrencies(from: String, to: String) {
        cameraFromCurrency = from
        cameraToCurrency = to
        storage.cameraFromCurrency = from
        storage.cameraToCurrency = to
    }

    /** Updates both graph selections together so reloads and app restarts keep the chosen pair. */
    fun setHistoryCurrencies(from: String, to: String) {
        historyFromCurrency = from
        historyToCurrency = to
        storage.historyFromCurrency = from
        storage.historyToCurrency = to
    }

    /** Persists the reference unit most recently chosen in the custom-currency form. */
    fun rememberCustomReferenceCurrency(currency: String) {
        customReferenceCurrency = currency
        storage.customReferenceCurrency = currency
    }

    private var undoHistory by mutableStateOf<List<UndoSnapshot>>(emptyList())
        private set
    private var lastTypedChangeAt = 0L
    /** Lets screens hide or disable Undo when there is no reversible user action. */
    val canUndo: Boolean get() = undoHistory.isNotEmpty()

    // ---- derived state ----------------------------------------------------
    /** Builds display models from API codes; recomposition is limited to changes in rates/language. */
    val realCurrencies: List<Currency> by derivedStateOf {
        rates.keys
            .map { Currency(it, currencyDisplayName(it, language), CurrencyKind.REAL) }
            .sortedBy { it.name.lowercase() }
    }

    /** Own currencies + unlocked fun currencies (the "Custom / Fun" dropdown). */
    /** Combines user-defined entries with only the fictional currencies the user has unlocked. */
    val customAndFunCurrencies: List<Currency> by derivedStateOf {
        customCurrencies + FunCurrencies.all.filter { it.id in unlockedIds }
    }

    /** Lookup shared by selection, conversion, and validation code; IDs are the stable keys. */
    private val knownById: Map<String, Currency> by derivedStateOf {
        (realCurrencies + customAndFunCurrencies).associateBy { it.id }
    }

    /** Resolves saved IDs in their existing order and skips IDs unavailable in the current catalog. */
    val selectedCurrencies: List<Currency> by derivedStateOf {
        selectedIds.mapNotNull { knownById[it] }
    }

    init {
        // Cached rates are already exposed above; this refresh replaces them only after a valid response arrives.
        refresh() // try to update the rates on every app start
    }

    // ---- rates ------------------------------------------------------------
    /** Refreshes rates asynchronously and preserves cached rates if the request cannot complete. */
    fun refresh() {
        if (fetchState == FetchState.LOADING) return
        viewModelScope.launch {
            // Compose observes these state writes and updates the status bar without screens owning network logic.
            fetchState = FetchState.LOADING
            try {
                rates = repository.refreshRates()
                lastUpdateMillis = repository.lastUpdateMillis
                fetchState = FetchState.SUCCESS
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                fetchState = FetchState.OFFLINE
            }
        }
    }

    // ---- values -----------------------------------------------------------
    /** Converter is derived from the latest rate map so a refresh is reflected immediately. */
    private val converter get() = CurrencyConverter(rates)

    /** Resolves the edited amount once, then derives every other row from the selected source row. */
    private fun amountOf(c: Currency): Double? {
        // A conversion needs both a valid source row and a parseable amount; blank/in-progress text stays blank.
        val active = activeId?.let { knownById[it] } ?: return null
        val amount = parseAmount(activeText) ?: return null
        return if (c.id == active.id) amount else converter.convert(amount, active, c)
    }

    /** Preserves raw editable text in the source field and formats calculated values for other rows. */
    fun displayValue(c: Currency): String =
        if (c.id == activeId) activeText else amountOf(c)?.let { formatAmount(it) } ?: ""

    private fun setActive(id: String?, text: String) {
        // The observable properties update the screen now; matching writes restore them on a future launch.
        activeId = id
        activeText = text
        storage.activeId = id
        storage.activeText = text
    }

    /** Makes [id] the source row and sanitizes its raw editable text before persisting it. */
    fun onValueChange(id: String, text: String) {
        val cleanText = sanitizeAmountInput(text)
        if (id == activeId && cleanText == activeText) return
        val now = android.os.SystemClock.elapsedRealtime()
        // Group a burst of keystrokes into one undo step so undo restores the value before editing.
        if (now - lastTypedChangeAt > 700L) saveUndoSnapshot()
        lastTypedChangeAt = now
        setActive(id, cleanText)
    }

    /**
     * Sets a preset amount in [currency], making it the source for every other visible conversion.
     * One tap creates one undo step, just like rounding or setting the amount to one.
     */
    fun setQuickAmount(currency: Currency, amount: Int) {
        if (amount < 0) return
        val text = amount.toString()
        if (currency.id == activeId && text == activeText) return
        saveUndoSnapshot()
        setActive(currency.id, text)
    }

    /** Floors the displayed amount to the currency's normal minor-unit precision. */
    fun roundDown(c: Currency) {
        amountOf(c)?.let {
            val rounded = roundedText(it, c, RoundingMode.FLOOR)
            if (c.id == activeId && rounded == activeText) return
            saveUndoSnapshot()
            setActive(c.id, rounded)
        }
    }

    /** Ceils the displayed amount to the currency's normal minor-unit precision. */
    fun roundUp(c: Currency) {
        amountOf(c)?.let {
            val rounded = roundedText(it, c, RoundingMode.CEILING)
            if (c.id == activeId && rounded == activeText) return
            saveUndoSnapshot()
            setActive(c.id, rounded)
        }
    }

    private fun roundedText(value: Double, currency: Currency, mode: RoundingMode): String =
        // BigDecimal avoids binary floating-point artifacts at the currency's minor-unit boundary.
        java.math.BigDecimal.valueOf(value)
            .setScale(fractionDigits(currency), mode)
            .stripTrailingZeros()
            .toPlainString()

    /** Uses ISO minor-unit precision for real currencies; custom/fan estimates use two decimals. */
    private fun fractionDigits(currency: Currency): Int = if (currency.kind == CurrencyKind.REAL) {
        runCatching { java.util.Currency.getInstance(currency.id).defaultFractionDigits }
            .getOrNull()?.takeIf { it >= 0 } ?: 2
    } else 2

    /** Sets a row to one unit and makes it the source for all derived rows. */
    fun setToOne(c: Currency) {
        if (c.id == activeId && parseAmount(activeText) == 1.0) return
        saveUndoSnapshot()
        setActive(c.id, "1")
    }

    private fun saveUndoSnapshot() {
        // Keep the history bounded: each snapshot contains only reversible user choices and amount state.
        // Copies prevent later list/set edits from changing an older undo entry by reference.
        undoHistory = (undoHistory + UndoSnapshot(
            selectedIds = selectedIds.toList(),
            favoriteIds = favoriteIds.toSet(),
            unlockedIds = unlockedIds.toSet(),
            customCurrencies = customCurrencies.toList(),
            activeId = activeId,
            activeText = activeText
        )).takeLast(20)
        lastTypedChangeAt = 0L
    }

    /** Restores the most recent reversible snapshot and writes the restored choices back to storage. */
    fun undoLastChange() {
        val previous = undoHistory.lastOrNull() ?: return
        // Remove first so an undo does not immediately restore itself as the next undo action.
        undoHistory = undoHistory.dropLast(1)
        selectedIds = previous.selectedIds
        favoriteIds = previous.favoriteIds
        unlockedIds = previous.unlockedIds
        customCurrencies = previous.customCurrencies
        activeId = previous.activeId
        activeText = previous.activeText
        storage.selectedIds = selectedIds
        storage.favoriteIds = favoriteIds
        storage.unlockedIds = unlockedIds
        storage.customCurrencies = customCurrencies
        storage.activeId = activeId
        storage.activeText = activeText
        lastTypedChangeAt = 0L
    }

    // ---- selection --------------------------------------------------------
    /** Keeps Compose state and the on-disk selection synchronized in one place. */
    private fun updateSelected(list: List<String>) {
        selectedIds = list
        storage.selectedIds = list
    }

    /** Toggles list membership while keeping the active source row valid. */
    fun toggleSelected(id: String) {
        if (id !in knownById) return
        saveUndoSnapshot()
        if (id in selectedIds) removeFromSelectionInternal(id) else addToSelection(id)
    }

    private fun addToSelection(id: String) {
        // Preserve the active amount when adding rows; only initialize it if no valid source exists.
        val active = activeId?.let { knownById[it] }
        // First selected currency (nothing selected before) is normalized to 1.
        if (selectedIds.isEmpty() || active == null) setActive(id, "1")
        updateSelected(selectedIds + id)
    }

    /** Removes a selected currency; if it supplied the amount, another selected row becomes source. */
    fun removeFromSelection(id: String) {
        if (id !in selectedIds) return
        saveUndoSnapshot()
        removeFromSelectionInternal(id)
    }

    private fun removeFromSelectionInternal(id: String) {
        // Internal form lets compound actions such as delete reuse selection cleanup without a second snapshot.
        val remaining = selectedIds - id
        if (id == activeId) {
            // Keep the current displayed amount: the next currency becomes the new source.
            val next = remaining.firstOrNull()?.let { knownById[it] }
            if (next != null) setActive(next.id, displayValue(next)) else setActive(null, "1")
        }
        updateSelected(remaining)
    }

    // ---- favorites --------------------------------------------------------
    /** Adds or removes an ID from the favorite set without changing the conversion selection. */
    fun toggleFavorite(id: String) {
        if (id !in knownById) return
        // Favorites are independent of which currencies are currently visible in the converter.
        saveUndoSnapshot()
        favoriteIds = if (id in favoriteIds) favoriteIds - id else favoriteIds + id
        storage.favoriteIds = favoriteIds
    }

    // ---- unlock (later: only via rewarded ad) -----------------------------
    /** Persists access to one catalog ID so it can appear in currency pickers. */
    fun unlock(id: String) {
        if (id in unlockedIds) return
        // The UI passes IDs from the built-in catalog; this set is what makes them appear elsewhere.
        saveUndoSnapshot()
        unlockedIds = unlockedIds + id
        storage.unlockedIds = unlockedIds
    }

    // ---- custom currencies (later: create/edit only via rewarded ad) ------
    /** Creates (editingId == null) or updates a custom currency. Returns an error text or null. */
    fun saveCustom(editingId: String?, name: String, refCode: String, factor: Double?): String? {
        // Validate the complete form before saving undo state or changing any persistent data.
        val cleanName = name.trim()
        if (cleanName.isEmpty()) return "Enter a name."
        if (factor == null || !factor.isFinite() || factor <= 0.0) return "Enter a finite value greater than 0."
        if (refCode !in rates) return "The reference currency is not available yet. Wait for rates to load."

        // Names must be unique (case-insensitive) across real, own and fun currencies.
        // Validate before saving a snapshot so invalid form submissions do not create empty undo steps.
        val nameTaken = (realCurrencies + customCurrencies.filter { it.id != editingId } + FunCurrencies.all)
            .any { it.name.equals(cleanName, ignoreCase = true) || it.id.equals(cleanName, ignoreCase = true) }
        if (nameTaken) return "A currency with this name already exists."

        saveUndoSnapshot()
        val isNewCurrency = editingId == null
        // Editing retains the stable ID so existing selection/favorite references continue to work.
        customCurrencies = if (isNewCurrency) {
            customCurrencies + Currency(
                id = "custom_${System.currentTimeMillis()}",
                name = cleanName,
                kind = CurrencyKind.CUSTOM,
                refCode = refCode,
                factor = factor
            )
        } else {
            customCurrencies.map {
                if (it.id == editingId) it.copy(name = cleanName, refCode = refCode, factor = factor) else it
            }
        }
        storage.customCurrencies = customCurrencies
        if (isNewCurrency) addToSelection(customCurrencies.last().id)
        return null
    }

    /** Deletes a user currency and removes its references from selection and favorites. */
    fun deleteCustom(id: String) {
        if (customCurrencies.none { it.id == id }) return
        // Remove dependent references before deleting the model so no stale row survives recomposition.
        saveUndoSnapshot()
        removeFromSelectionInternal(id)
        favoriteIds = favoriteIds - id
        storage.favoriteIds = favoriteIds
        customCurrencies = customCurrencies.filter { it.id != id }
        storage.customCurrencies = customCurrencies
    }
}
