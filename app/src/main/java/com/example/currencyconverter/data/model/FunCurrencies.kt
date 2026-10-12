package com.example.currencyconverter.data.model

/** Catalog of fun currencies that can be unlocked. Add new entries here. */
object FunCurrencies {
    val all: List<Currency> = listOf(
        // 1 Berry = 1 JPY
        Currency("fun_berries", "Berries", CurrencyKind.FUN, "JPY", 1.0, "From One Piece"),
        // 100 Jewel = 1 USD  ->  1 Jewel = 0.01 USD
        Currency("fun_jewels", "Jewels", CurrencyKind.FUN, "USD", 0.01, "From Fairy Tail"),
        // 1 Ryō = 10 JPY
        Currency("fun_ryo", "Ryō", CurrencyKind.FUN, "JPY", 10.0, "From Naruto")
    )
}
