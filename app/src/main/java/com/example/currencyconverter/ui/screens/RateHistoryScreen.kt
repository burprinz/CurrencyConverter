package com.example.currencyconverter.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.currencyconverter.data.model.Currency
import com.example.currencyconverter.data.remote.HistoricalRatePoint
import com.example.currencyconverter.data.remote.HistoricalRatesApi
import com.example.currencyconverter.ui.MainViewModel
import com.example.currencyconverter.ui.components.CurrencyFlag
import com.example.currencyconverter.util.formatAmount
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

private enum class RatePeriod(val label: String) { DAY("Day"), WEEK("Week"), MONTH("Month"), YEAR("Year"), ALL("All time") }

@Composable
fun RateHistoryScreen(vm: MainViewModel) {
    val uriHandler = LocalUriHandler.current
    val currencies = vm.realCurrencies
    var base by rememberSaveable { mutableStateOf("EUR") }
    var quote by rememberSaveable { mutableStateOf("USD") }
    var period by rememberSaveable { mutableStateOf(RatePeriod.MONTH) }
    var points by remember { mutableStateOf<List<HistoricalRatePoint>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf(false) }
    var reload by remember { mutableStateOf(0) }
    val today = remember { apiDate(Calendar.getInstance(TimeZone.getTimeZone("UTC"))) }
    val start = when (period) {
        RatePeriod.DAY -> shiftedApiDate(today, Calendar.DAY_OF_MONTH, -1)
        RatePeriod.WEEK -> shiftedApiDate(today, Calendar.DAY_OF_MONTH, -7)
        RatePeriod.MONTH -> shiftedApiDate(today, Calendar.MONTH, -1)
        RatePeriod.YEAR -> shiftedApiDate(today, Calendar.YEAR, -1)
        RatePeriod.ALL -> "1948-01-01"
    }
    val currencyCodes = currencies.map { it.id }

    LaunchedEffect(base, quote, period, reload, currencyCodes) {
        if (base == quote) {
            points = emptyList()
            error = false
            loading = false
            return@LaunchedEffect
        }
        if (base !in currencyCodes || quote !in currencyCodes) {
            points = emptyList()
            loading = false
            return@LaunchedEffect
        }
        loading = true
        error = false
        try {
            points = HistoricalRatesApi.fetch(base, quote, start, today, monthly = period == RatePeriod.ALL)
            error = points.isEmpty()
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (_: Exception) {
            points = emptyList()
            error = true
        } finally {
            loading = false
        }
    }

    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("Exchange rate history", style = MaterialTheme.typography.headlineSmall)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            SearchCurrencyPicker("From", currencies, base, { base = it }, Modifier.weight(1f))
            Text("→", color = MaterialTheme.colorScheme.primary)
            SearchCurrencyPicker("To", currencies, quote, { quote = it }, Modifier.weight(1f))
        }
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            RatePeriod.values().forEach { option ->
                FilterChip(selected = period == option, onClick = { period = option }, label = { Text(option.label) })
            }
        }
        if (points.isNotEmpty()) {
            val first = points.first().rate
            val last = points.last().rate
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                Column(Modifier.fillMaxWidth().padding(16.dp)) {
                    Text("1 $base = ${formatAmount(last)} $quote", style = MaterialTheme.typography.headlineSmall)
                    Text(
                        if (points.size > 1) "${formatAmount(kotlin.math.abs(last - first))} ${if (last >= first) "increase" else "decrease"} over ${period.label.lowercase()}"
                        else "Only one published rate is available for this period.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            RateChart(points, Modifier.fillMaxWidth().height(260.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(points.first().date.toString(), style = MaterialTheme.typography.labelSmall)
                Text(points.last().date.toString(), style = MaterialTheme.typography.labelSmall)
            }
            Text("Daily reference rates. Weekends and holidays may have no new data.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Historical data by Frankfurter", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, modifier = Modifier.clickable { uriHandler.openUri("https://frankfurter.dev/") })
        } else if (loading) {
            Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else if (error) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text("Historical rates could not be loaded. Check your connection or choose another currency pair.")
                Spacer(Modifier.height(8.dp))
                Button(onClick = { reload++ }) { Text("Retry") }
            }
        } else {
            Text("Select two different currencies to view their history.")
        }
    }
}

private fun shiftedApiDate(date: String, field: Int, amount: Int): String {
    val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
    calendar.time = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }.parse(date) ?: calendar.time
    calendar.add(field, amount)
    return apiDate(calendar)
}

private fun apiDate(calendar: Calendar): String =
    SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }.format(calendar.time)

@Composable
private fun SearchCurrencyPicker(label: String, currencies: List<Currency>, selectedId: String, onSelected: (String) -> Unit, modifier: Modifier = Modifier) {
    var expanded by remember { mutableStateOf(false) }
    var query by rememberSaveable(label) { mutableStateOf("") }
    val selected = currencies.firstOrNull { it.id == selectedId }
    Column(modifier) {
        Text(label, style = MaterialTheme.typography.labelMedium)
        Box {
            OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
                selected?.let { CurrencyFlag(it) }
                Text(selected?.let { "${it.id} · ${it.name}" } ?: selectedId, maxLines = 1, modifier = Modifier.padding(start = 4.dp))
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }, modifier = Modifier.heightIn(max = 440.dp)) {
                OutlinedTextField(value = query, onValueChange = { query = it }, label = { Text("Search currency") }, singleLine = true, modifier = Modifier.padding(8.dp))
                currencies.filter { it.id.contains(query, true) || it.name.contains(query, true) }.take(80).forEach { currency ->
                    DropdownMenuItem(
                        text = { Row(verticalAlignment = Alignment.CenterVertically) { CurrencyFlag(currency); Text("${currency.id} · ${currency.name}", modifier = Modifier.padding(start = 8.dp)) } },
                        onClick = { onSelected(currency.id); query = ""; expanded = false }
                    )
                }
            }
        }
    }
}

@Composable
private fun RateChart(points: List<HistoricalRatePoint>, modifier: Modifier = Modifier) {
    val color = MaterialTheme.colorScheme.primary
    val grid = MaterialTheme.colorScheme.outlineVariant
    Canvas(modifier.padding(vertical = 8.dp)) {
        if (points.size < 2) return@Canvas
        val min = points.minOf { it.rate }
        val max = points.maxOf { it.rate }
        val range = (max - min).takeIf { it > 0.0 } ?: max * 0.01
        val inset = 12.dp.toPx()
        val width = size.width - inset * 2
        val height = size.height - inset * 2
        repeat(4) { row ->
            val y = inset + height * row / 3f
            drawLine(grid, Offset(inset, y), Offset(size.width - inset, y), strokeWidth = 1.dp.toPx())
        }
        val path = Path()
        points.forEachIndexed { index, point ->
            val x = inset + width * index / (points.size - 1).toFloat()
            val y = inset + height * (1f - ((point.rate - min) / range).toFloat())
            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        drawPath(path, color, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round))
    }
}
