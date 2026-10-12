package com.example.currencyconverter.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.currencyconverter.data.model.FunCurrencies
import com.example.currencyconverter.ui.MainViewModel

/** Fun currencies: Name | Description | Unlock button. */
@Composable
fun UnlockScreen(vm: MainViewModel) {
    LazyColumn(Modifier.fillMaxSize()) {
        items(FunCurrencies.all, key = { it.id }) { c ->
            val unlocked = c.id in vm.unlockedIds
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(c.name, style = MaterialTheme.typography.titleMedium)
                    Text(c.description, style = MaterialTheme.typography.bodySmall)
                }
                // Later: unlocking should require watching a rewarded ad.
                Button(onClick = { vm.unlock(c.id) }, enabled = !unlocked) {
                    Text(if (unlocked) "Unlocked" else "Unlock")
                }
            }
            HorizontalDivider()
        }
    }
}
