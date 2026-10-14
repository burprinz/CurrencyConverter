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
import com.example.currencyconverter.ui.LocalizedText as Text
import androidx.compose.material3.TextButton
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
import com.example.currencyconverter.ui.screens.CameraConvertScreen
import com.example.currencyconverter.ui.screens.ConverterListScreen
import com.example.currencyconverter.ui.screens.SelectScreen
import com.example.currencyconverter.ui.screens.UnlockScreen
import com.example.currencyconverter.ui.screens.RateHistoryScreen
import com.example.currencyconverter.ui.screens.SettingsScreen
import com.example.currencyconverter.ui.theme.AppTheme
import kotlinx.coroutines.launch

/** All destinations shown in the drawer, in the order the user sees them, and their source titles. */
enum class Screen(val title: String) {
    LIST("Convert"),
    FAVORITES("Favorites"),
    RATES("Rate history"),
    CAMERA("Camera converter"),
    SELECT("Choose currencies"),
    ADD_NEW("Custom currencies"),
    UNLOCK("Fan currencies"),
    SETTINGS("Settings")
}

/** Applies the saved language and theme around the entire app UI. The providers wrap navigation too. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrencyApp(vm: MainViewModel = viewModel()) {
    // Provide language outside the theme/navigation shell so every nested destination sees one value.
    ProvideAppLanguage(vm.language) {
        AppTheme(vm.themeMode) { CurrencyAppContent(vm) }
    }
}

/**
 * Builds the app shell (drawer, app bar, status bar) and displays the selected destination.
 * [screen] is saveable across configuration changes; each drawer choice updates it and closes the
 * modal drawer. The screen-level undo action is shown only on destinations that can mutate state.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CurrencyAppContent(vm: MainViewModel) {
    // Save destination across rotations/process recreation; drawer expansion remains transient UI state.
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
                            // Update content immediately, then close the modal drawer in the Compose coroutine scope.
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
                    actions = {
                        if (screen in setOf(Screen.LIST, Screen.FAVORITES, Screen.SELECT, Screen.ADD_NEW, Screen.UNLOCK)) {
                        TextButton(onClick = { vm.undoLastChange() }, enabled = vm.canUndo) {
                            Text("Undo")
                        }
                        }
                    },
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
                // Each destination receives shared state/actions; screen-specific temporary state stays local.
                when (screen) {
                    Screen.LIST -> ConverterListScreen(vm, favoritesOnly = false, onSelectCurrencies = { screen = Screen.SELECT })
                    Screen.FAVORITES -> ConverterListScreen(vm, favoritesOnly = true, onSelectCurrencies = { screen = Screen.SELECT })
                    Screen.SELECT -> SelectScreen(vm, onDone = { screen = Screen.LIST })
                    Screen.ADD_NEW -> AddCurrencyScreen(vm, onGoToConverter = { screen = Screen.LIST })
                    Screen.UNLOCK -> UnlockScreen(vm)
                    Screen.CAMERA -> CameraConvertScreen(vm)
                    Screen.RATES -> RateHistoryScreen(vm)
                    Screen.SETTINGS -> SettingsScreen(vm)
                }
            }
        }
    }
}
