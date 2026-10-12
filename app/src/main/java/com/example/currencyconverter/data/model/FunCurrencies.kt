package com.example.currencyconverter.data.model

/** Catalog of fun currencies that can be unlocked. Add new entries here. */
object FunCurrencies {
    val all: List<Currency> = listOf(
        // 1 Berry = 1 JPY
        Currency("fun_berries", "Berries", CurrencyKind.FUN, "JPY", 1.0, "From One Piece"),

        // 1 Extol = 0.0001 Berry = 0.0001 JPY
        Currency("fun_extol", "Extol", CurrencyKind.FUN, "JPY", 0.0001, "From One Piece (Skypiea)"),

        // 1 Jewel = 0.01 USD
        Currency("fun_jewels", "Jewels", CurrencyKind.FUN, "USD", 0.01, "From Fairy Tail"),

        // 1 Jenny = 0.9 JPY (Fan estimate)
        Currency("fun_jenny", "Jenny", CurrencyKind.FUN, "JPY", 0.9, "From Hunter x Hunter"),

        // 1 Eris = 1 JPY (Fan estimate)
        Currency("fun_eris", "Eris", CurrencyKind.FUN, "JPY", 1.0, "From KonoSuba"),

        // 1 Ryō = 10 JPY
        Currency("fun_ryo", "Ryō", CurrencyKind.FUN, "JPY", 10.0, "From Naruto"),

        // 1 Schmeckles = 148 USD
        Currency("fun_schmeckles", "Schmeckles", CurrencyKind.FUN, "USD", 148.0, "From Rick and Morty"),

        // 1 Col = 1 JPY (Fan estimate)
        Currency("fun_col", "Col", CurrencyKind.FUN, "JPY", 1.0, "From Sword Art Online"),

        // 1 Yul = 1 JPY (Fan estimate)
        Currency("fun_yul", "Yul", CurrencyKind.FUN, "JPY", 1.0, "From ALfheim Online"),

        // 1 Woolong = 1 JPY (Fan estimate)
        Currency("fun_woolong", "Woolong", CurrencyKind.FUN, "JPY", 1.0, "From Cowboy Bebop"),

        // Latinum Slip has no established USD exchange rate; placeholder value
        Currency("fun_latinum", "Latinum Slip", CurrencyKind.FUN, "USD", 1.0, "From Star Trek"),

        // 1 Zeni = 1 JPY
        Currency("fun_zeni", "Zeni", CurrencyKind.FUN, "JPY", 1.0, "From Dragon Ball")
    )
}
