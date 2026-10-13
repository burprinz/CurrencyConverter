package com.example.currencyconverter.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.awaitPointerEventScope
import androidx.compose.ui.input.pointer.awaitPointerEvent
import androidx.compose.ui.graphics.nativeCanvas
import android.graphics.Paint
import android.graphics.Typeface
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
            RateChart(points, base, quote, Modifier.fillMaxWidth().height(300.dp))
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
private fun RateChart(points: List<HistoricalRatePoint>, base: String, quote: String, modifier: Modifier = Modifier) {
    val color = MaterialTheme.colorScheme.primary
    val grid = MaterialTheme.colorScheme.outlineVariant
    var selectedIndex by remember(points) { mutableStateOf<Int?>(null) }
    val labelColor = MaterialTheme.colorScheme.onSurface
    Column(modifier.padding(vertical = 8.dp)) {
        Text("Exchange rate (\u0024quote per 1 \u0024base)", style = MaterialTheme.typography.labelMedium)
        BoxWithConstraints(Modifier.fillMaxWidth().weight(1f)) {
            val chartWidth = constraints.maxWidth.toFloat()
            Canvas(Modifier.fillMaxSize().pointerInput(points) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        when (event.type) {
                            PointerEventType.Move, PointerEventType.Press -> {
                                if (points.isNotEmpty()) {
                                    val inset = 58.dp.toPx()
                                    val plotWidth = (size.width - inset - 10.dp.toPx()).coerceAtLeast(1f)
                                    val fraction = ((event.changes.first().position.x - inset) / plotWidth).coerceIn(0f, 1f)
                                    selectedIndex = (fraction * (points.size - 1)).toInt()
                                }
                            }
                            PointerEventType.Exit, PointerEventType.Release -> selectedIndex = null
                        }
                    }
                }
            }) {
                if (points.size < 2) return@Canvas
        val min = points.minOf { it.rate }
        val max = points.maxOf { it.rate }
        val range = (max - min).takeIf { it > 0.0 } ?: max * 0.01
        val left = 58.dp.toPx()
        val right = 10.dp.toPx()
        val top = 12.dp.toPx()
        val bottom = 28.dp.toPx()
        val width = size.width - left - right
        val height = size.height - top - bottom
        val native = drawContext.canvas.nativeCanvas
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.argb((labelColor.alpha * 255).toInt(), (labelColor.red * 255).toInt(), (labelColor.green * 255).toInt(), (labelColor.blue * 255).toInt())
            textSize = 11.dp.toPx()
            typeface = Typeface.create("sans-serif", Typeface.NORMAL)
        }
        repeat(5) { row ->
            val fraction = row / 4f
            val y = top + height * fraction
            drawLine(grid, Offset(left, y), Offset(size.width - right, y), strokeWidth = 1.dp.toPx())
            native.drawText(formatAmount(max - range * fraction), 0f, y + paint.textSize / 3f, paint)
        }
        val datePaint = Paint(paint).apply { textSize = 10.dp.toPx() }
        native.drawText(points.first().date.toString(), left, size.height - 4.dp.toPx(), datePaint)
        val lastDate = points.last().date.toString()
        native.drawText(lastDate, size.width - right - datePaint.measureText(lastDate), size.height - 4.dp.toPx(), datePaint)
        val path = Path()
        points.forEachIndexed { index, point ->
            val x = left + width * index / (points.size - 1).toFloat()
            val y = top + height * (1f - ((point.rate - min) / range).toFloat())
            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        drawPath(path, color, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round))
        selectedIndex?.let { index ->
            val point = points[index]
            val x = left + width * index / (points.size - 1).toFloat()
            val y = top + height * (1f - ((point.rate - min) / range).toFloat())
            drawLine(color.copy(alpha = 0.5f), Offset(x, top), Offset(x, top + height), strokeWidth = 1.dp.toPx())
            drawCircle(color, 5.dp.toPx(), Offset(x, y))
        }
            }
            Text("Date", style = MaterialTheme.typography.labelSmall, modifier = Modifier.align(Alignment.CenterHorizontally))
            selectedIndex?.let { index ->
                val point = points[index]
                Text("${point.date}: 1 $base = ${formatAmount(point.rate)} $quote", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}
