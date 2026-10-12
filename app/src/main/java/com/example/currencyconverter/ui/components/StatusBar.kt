package com.example.currencyconverter.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.currencyconverter.data.FetchState
import java.text.DateFormat
import java.util.Date

private val BarGray = Color(0xFFE0E0E0)
private val BarText = Color(0xFF424242)

/** Small gray bar at the bottom: status, last update, refresh button and API attribution. */
@Composable
fun StatusBar(state: FetchState, lastUpdateMillis: Long, onRefresh: () -> Unit) {
    val uriHandler = LocalUriHandler.current
    val last = if (lastUpdateMillis > 0) {
        DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(Date(lastUpdateMillis))
    } else {
        "never"
    }
    val text = when (state) {
        FetchState.LOADING -> "Updating... (Last updated: $last)"
        FetchState.OFFLINE -> "Offline - Last updated: $last"
        else -> "Last updated: $last"
    }

    Surface(color = BarGray, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .navigationBarsPadding()
                .padding(start = 12.dp, end = 4.dp, top = 2.dp, bottom = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(Modifier.weight(1f)) {
                Text(text, color = BarText, fontSize = 12.sp)
                // Attribution required by the free ExchangeRate-API endpoint
                Text(
                    "Rates By Exchange Rate API",
                    color = BarText,
                    fontSize = 10.sp,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier.clickable { uriHandler.openUri("https://www.exchangerate-api.com") }
                )
            }
            IconButton(
                onClick = onRefresh,
                enabled = state != FetchState.LOADING,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Refresh rates", tint = BarText)
            }
        }
    }
}
