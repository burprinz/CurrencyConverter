package com.example.currencyconverter.data.model

/** Catalog of fun currencies that can be unlocked. Add new entries here. */
object FunCurrencies {
    val all: List<Currency> = listOf(
        // 1 Berry = 1 JPY
        Currency("fun_berries", "Berries", CurrencyKind.FUN, "JPY", 1.0, "From One Piece", "Anime"),

        // 1 Extol = 0.0001 Berry = 0.0001 JPY
        Currency("fun_extol", "Extol", CurrencyKind.FUN, "JPY", 0.0001, "From One Piece (Skypiea)", "Anime"),

        // 1 Jewel = 0.01 USD
        Currency("fun_jewels", "Jewels", CurrencyKind.FUN, "USD", 0.01, "From Fairy Tail", "Anime"),

        // 1 Jenny = 0.9 JPY (Fan estimate)
        Currency("fun_jenny", "Jenny", CurrencyKind.FUN, "JPY", 0.9, "From Hunter x Hunter", "Anime"),

        // 1 Eris = 1 JPY (Fan estimate)
        Currency("fun_eris", "Eris", CurrencyKind.FUN, "JPY", 1.0, "From KonoSuba", "Anime"),

        // 1 Ryō = 10 JPY
        Currency("fun_ryo", "Ryō", CurrencyKind.FUN, "JPY", 10.0, "From Naruto", "Anime"),

        // 1 Schmeckles = 148 USD
        Currency("fun_schmeckles", "Schmeckles", CurrencyKind.FUN, "USD", 148.0, "From Rick and Morty", "Series"),

        // 1 Col = 1 JPY (Fan estimate)
        Currency("fun_col", "Col", CurrencyKind.FUN, "JPY", 1.0, "From Sword Art Online", "Anime"),

        // 1 Yul = 1 JPY (Fan estimate)
        Currency("fun_yul", "Yul", CurrencyKind.FUN, "JPY", 1.0, "From ALfheim Online", "Anime"),

        // 1 Woolong = 1 JPY (Fan estimate)
        Currency("fun_woolong", "Woolong", CurrencyKind.FUN, "JPY", 1.0, "From Cowboy Bebop", "Anime"),

        // Latinum Slip has no established USD exchange rate; placeholder value
        Currency("fun_latinum", "Latinum Slip", CurrencyKind.FUN, "USD", 1.0, "From Star Trek", "Series"),

        // 1 Zeni = 1 JPY
        Currency("fun_zeni", "Zeni", CurrencyKind.FUN, "JPY", 1.0, "From Dragon Ball", "Anime"),

        // Fan estimate: 1 PokéDollar ≈ 1 JPY; Japanese games traditionally use yen-like prices.
        Currency("fun_pokedollar", "PokéDollar", CurrencyKind.FUN, "JPY", 1.0, "Pokémon · Fan estimate: 1 ≈ 1 JPY", "Video games"),

        // Fan estimate based on in-game staple item prices: about 50 green rupees to 1 USD.
        Currency("fun_hyrule_rupee", "Hyrule Rupee", CurrencyKind.FUN, "USD", 0.02, "The Legend of Zelda · Fan estimate: 50 ≈ 1 USD", "Video games"),

        // Rowling's 2001 estimate was about GBP 5 per Galleon; the rate was described as variable.
        Currency("fun_galleon", "Galleon", CurrencyKind.FUN, "GBP", 5.0, "Harry Potter · Author estimate: 1 ≈ 5 GBP", "Movies & books"),
        Currency("fun_sickle", "Sickle", CurrencyKind.FUN, "GBP", 5.0 / 17.0, "Harry Potter · 17 Sickles = 1 Galleon", "Movies & books"),
        Currency("fun_knut", "Knut", CurrencyKind.FUN, "GBP", 5.0 / 493.0, "Harry Potter · 493 Knuts = 1 Galleon", "Movies & books"),

        // Fan estimate: Gil is loosely based on Japanese yen; no canonical real-money rate exists.
        Currency("fun_gil", "Gil", CurrencyKind.FUN, "JPY", 1.0, "Final Fantasy · Fan estimate: 1 ≈ 1 JPY", "Video games"),

        // Approximation from a US Marketplace pack; store bundles and regional prices vary.
        Currency("fun_minecoin", "Minecoin", CurrencyKind.FUN, "USD", 1.99 / 320.0, "Minecraft · US store estimate: 320 ≈ 1.99 USD", "Video games"),

        // DevEx cash-out estimate for eligible Earned Robux only; retail purchase value differs.
        Currency("fun_robux", "Robux", CurrencyKind.FUN, "USD", 0.0038, "Roblox · DevEx estimate per eligible Earned Robux; not the purchase price", "Video games"),

        // Approximation from the US 800 V-Bucks pack; pack sizes and regional prices vary.
        Currency("fun_vbucks", "V-Bucks", CurrencyKind.FUN, "USD", 8.99 / 800.0, "Fortnite · US store estimate: 800 ≈ 8.99 USD; regional prices vary", "Video games"),

        // Illustrative North American bundle estimate; RP prices vary by region and bundle.
        Currency("fun_riot_points", "Riot Points", CurrencyKind.FUN, "USD", 10.0 / 1380.0, "League of Legends · illustrative US estimate: 1,380 ≈ 10 USD; prices vary", "Video games")
    )
}
