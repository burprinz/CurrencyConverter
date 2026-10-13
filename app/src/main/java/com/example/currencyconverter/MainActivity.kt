package com.example.currencyconverter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.currencyconverter.ui.CurrencyApp

/** Android entry point: creates the Compose UI and hands the screen tree to [CurrencyApp]. */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CurrencyApp()
        }
    }
}
