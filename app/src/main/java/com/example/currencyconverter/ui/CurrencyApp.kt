package com.example.currencyconverter.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.currencyconverter.ui.components.StatusBar
import com.example.currencyconverter.ui.screens.AddCurrencyScreen
import com.example.currencyconverter.ui.screens.ConverterListScreen
import com.example.currencyconverter.ui.screens.SelectScreen
import com.example.currencyconverter.ui.screens.UnlockScreen
import kotlinx.coroutines.launch

enum class Screen(val title: String) {
    LIST("List"),
    FAVORITES("Favorites"),
    SELECT("Select.."),
    ADD_NEW("Add new"),
    UNLOCK("Unlock")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrencyApp(vm: MainViewModel = viewModel()) {
    var screen by rememberSaveable { mutableStateOf(Screen.LIST) }
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Text(
                    "Currency Converter",
                    style = androidx.compose.material3.MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(24.dp)
                )
                Screen.values().forEach { s ->
                    NavigationDrawerItem(
                        label = { Text(s.title) },
                        selected = s == screen,
                        onClick = {
                            screen = s
                            scope.launch { drawerState.close() }
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(screen.title) },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    }
                )
            },
            bottomBar = {
                StatusBar(
                    state = vm.fetchState,
                    lastUpdateMillis = vm.lastUpdateMillis,
                    onRefresh = { vm.refresh() }
                )
            }
        ) { padding ->
            Box(Modifier.padding(padding).fillMaxSize()) {
                when (screen) {
                    Screen.LIST -> ConverterListScreen(vm, favoritesOnly = false)
                    Screen.FAVORITES -> ConverterListScreen(vm, favoritesOnly = true)
                    Screen.SELECT -> SelectScreen(vm)
                    Screen.ADD_NEW -> AddCurrencyScreen(vm)
                    Screen.UNLOCK -> UnlockScreen(vm)
                }
            }
        }
    }
}
