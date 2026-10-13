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

class MainViewModel(app: Application) : AndroidViewModel(app) {

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

    fun changeThemeMode(mode: String) {
        if (mode !in setOf("light", "dark", "system")) return
        themeMode = mode
        storage.themeMode = mode
    }

    fun changeLanguage(code: String) {
        if (code !in setOf("en", "de", "es", "zh", "ja")) return
        language = code
        storage.language = code
    }

    fun setCurrencyColor(id: String, paletteIndex: Int?) {
        currencyColors = if (paletteIndex == null) currencyColors - id else currencyColors + (id to paletteIndex)
        storage.currencyColors = currencyColors
    }

    /** The currency the user typed into last; all other values are derived from it. */
    var activeId by mutableStateOf(storage.activeId)
        private set
    var activeText by mutableStateOf(storage.activeText)
        private set

    private var undoHistory by mutableStateOf<List<UndoSnapshot>>(emptyList())
        private set
    private var lastTypedChangeAt = 0L
    val canUndo: Boolean get() = undoHistory.isNotEmpty()

    // ---- derived state ----------------------------------------------------
    val realCurrencies: List<Currency> by derivedStateOf {
        rates.keys
            .map { Currency(it, currencyDisplayName(it, language), CurrencyKind.REAL) }
            .sortedBy { it.name.lowercase() }
    }

    /** Own currencies + unlocked fun currencies (the "Custom / Fun" dropdown). */
    val customAndFunCurrencies: List<Currency> by derivedStateOf {
        customCurrencies + FunCurrencies.all.filter { it.id in unlockedIds }
    }

    private val knownById: Map<String, Currency> by derivedStateOf {
        (realCurrencies + customAndFunCurrencies).associateBy { it.id }
    }

    val selectedCurrencies: List<Currency> by derivedStateOf {
        selectedIds.mapNotNull { knownById[it] }
    }

    init {
        refresh() // try to update the rates on every app start
    }

    // ---- rates ------------------------------------------------------------
    fun refresh() {
        if (fetchState == FetchState.LOADING) return
        viewModelScope.launch {
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
    private val converter get() = CurrencyConverter(rates)

    private fun amountOf(c: Currency): Double? {
        val active = activeId?.let { knownById[it] } ?: return null
        val amount = parseAmount(activeText) ?: return null
        return if (c.id == active.id) amount else converter.convert(amount, active, c)
    }

    fun displayValue(c: Currency): String =
        if (c.id == activeId) activeText else amountOf(c)?.let { formatAmount(it) } ?: ""

    private fun setActive(id: String?, text: String) {
        activeId = id
        activeText = text
        storage.activeId = id
        storage.activeText = text
    }

    fun onValueChange(id: String, text: String) {
        val cleanText = sanitizeAmountInput(text)
        if (id == activeId && cleanText == activeText) return
        val now = android.os.SystemClock.elapsedRealtime()
        if (now - lastTypedChangeAt > 700L) saveUndoSnapshot()
        lastTypedChangeAt = now
        setActive(id, cleanText)
    }

    fun roundDown(c: Currency) {
        amountOf(c)?.let {
            val rounded = roundedText(it, c, RoundingMode.FLOOR)
            if (c.id == activeId && rounded == activeText) return
            saveUndoSnapshot()
            setActive(c.id, rounded)
        }
    }

    fun roundUp(c: Currency) {
        amountOf(c)?.let {
            val rounded = roundedText(it, c, RoundingMode.CEILING)
            if (c.id == activeId && rounded == activeText) return
            saveUndoSnapshot()
            setActive(c.id, rounded)
        }
    }

    private fun roundedText(value: Double, currency: Currency, mode: RoundingMode): String =
        java.math.BigDecimal.valueOf(value)
            .setScale(fractionDigits(currency), mode)
            .stripTrailingZeros()
            .toPlainString()

    private fun fractionDigits(currency: Currency): Int = if (currency.kind == CurrencyKind.REAL) {
        runCatching { java.util.Currency.getInstance(currency.id).defaultFractionDigits }
            .getOrNull()?.takeIf { it >= 0 } ?: 2
    } else 2

    fun setToOne(c: Currency) {
        if (c.id == activeId && parseAmount(activeText) == 1.0) return
        saveUndoSnapshot()
        setActive(c.id, "1")
    }

    private fun saveUndoSnapshot() {
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

    fun undoLastChange() {
        val previous = undoHistory.lastOrNull() ?: return
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
    private fun updateSelected(list: List<String>) {
        selectedIds = list
        storage.selectedIds = list
    }

    fun toggleSelected(id: String) {
        if (id !in knownById) return
        saveUndoSnapshot()
        if (id in selectedIds) removeFromSelectionInternal(id) else addToSelection(id)
    }

    private fun addToSelection(id: String) {
        val active = activeId?.let { knownById[it] }
        // First selected currency (nothing selected before) is normalized to 1.
        if (selectedIds.isEmpty() || active == null) setActive(id, "1")
        updateSelected(selectedIds + id)
    }

    fun removeFromSelection(id: String) {
        if (id !in selectedIds) return
        saveUndoSnapshot()
        removeFromSelectionInternal(id)
    }

    private fun removeFromSelectionInternal(id: String) {
        val remaining = selectedIds - id
        if (id == activeId) {
            // Keep the current values: the next currency becomes the new source.
            val next = remaining.firstOrNull()?.let { knownById[it] }
            if (next != null) setActive(next.id, displayValue(next)) else setActive(null, "1")
        }
        updateSelected(remaining)
    }

    // ---- favorites --------------------------------------------------------
    fun toggleFavorite(id: String) {
        if (id !in knownById) return
        saveUndoSnapshot()
        favoriteIds = if (id in favoriteIds) favoriteIds - id else favoriteIds + id
        storage.favoriteIds = favoriteIds
    }

    // ---- unlock (later: only via rewarded ad) -----------------------------
    fun unlock(id: String) {
        if (id in unlockedIds) return
        saveUndoSnapshot()
        unlockedIds = unlockedIds + id
        storage.unlockedIds = unlockedIds
    }

    // ---- custom currencies (later: create/edit only via rewarded ad) ------
    /** Creates (editingId == null) or updates a custom currency. Returns an error text or null. */
    fun saveCustom(editingId: String?, name: String, refCode: String, factor: Double?): String? {
        val cleanName = name.trim()
        if (cleanName.isEmpty()) return "Enter a name."
        if (factor == null || !factor.isFinite() || factor <= 0.0) return "Enter a finite value greater than 0."
        if (refCode !in rates) return "The reference currency is not available yet. Wait for rates to load."

        // Names must be unique (case-insensitive) across real, own and fun currencies.
        val nameTaken = (realCurrencies + customCurrencies.filter { it.id != editingId } + FunCurrencies.all)
            .any { it.name.equals(cleanName, ignoreCase = true) || it.id.equals(cleanName, ignoreCase = true) }
        if (nameTaken) return "A currency with this name already exists."

        saveUndoSnapshot()
        val isNewCurrency = editingId == null
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

    fun deleteCustom(id: String) {
        if (customCurrencies.none { it.id == id }) return
        saveUndoSnapshot()
        removeFromSelectionInternal(id)
        favoriteIds = favoriteIds - id
        storage.favoriteIds = favoriteIds
        customCurrencies = customCurrencies.filter { it.id != id }
        storage.customCurrencies = customCurrencies
    }
}
