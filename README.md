# Currency Converter

A Kotlin Android currency converter built with Jetpack Compose. It combines live exchange rates, a camera price reader, historical charts, custom currencies, and optional fictional and rewards currencies.

## Features

- Convert between the real currencies supplied by the latest-rate service.
- Select multiple currencies and enter an amount in any one of them to update the others.
- Use quick amount chips (1, 5, 10, 20, 50, 100) to try common values in the active currency.
- Keep favorites, use undo for supported changes, and choose a per-currency card color.
- Inspect historical rates by day, week, month, year, or all time. Drag across the chart to inspect a data point.
- Read prices with the rear camera. Text recognition runs on-device; an optional captured image is temporary and is not saved to the gallery.
- Create custom currencies by defining how many units of a real reference currency equal one custom unit.
- Unlock fictional, game, and reward currencies. Their reference values are estimates and are not official exchange rates or cash values.
- Choose a light, dark, or system theme and one of 22 app language options.

## Quick start

1. Open this folder in Android Studio.
2. Let Gradle sync and install the Android SDK platform required by `compileSdk` in `app/build.gradle.kts`.
3. Run the `app` configuration on a device or emulator running Android 7.0 (API 24) or newer.
4. Allow camera access only if you want to use the camera converter. Internet access is needed to refresh rates and load history; the most recent rates remain cached for offline use.

From Windows PowerShell, a debug APK can be built with:

```powershell
.\gradlew.bat :app:assembleDebug
```

## How conversion works

The app treats USD as the common reference. The current-rate response stores how many units of each real currency equal one USD. A custom or fun currency stores a reference ISO code and a factor, meaning:

```text
1 custom/fun unit = factor × reference-currency units
```

The converter derives each currency's units per USD and converts through that shared reference. Missing, zero, negative, or non-finite rates are treated as unavailable instead of producing invalid results.

## Project layout

```text
app/src/main/java/com/example/currencyconverter/
├── MainActivity.kt                 Android activity and Compose entry point
├── data/
│   ├── CurrencyRepository.kt       Latest-rate cache and refresh state
│   ├── local/AppStorage.kt         Saved preferences and user currency data
│   ├── model/                      Currency model and unlockable currency catalog
│   └── remote/                     Latest and historical rate HTTP clients
├── domain/CurrencyConverter.kt     Currency conversion math
├── ui/
│   ├── CurrencyApp.kt              Drawer, app bar, screen routing, app shell
│   ├── MainViewModel.kt            Shared state, persistence coordination, actions
│   ├── Localization.kt             In-app translations
│   ├── components/                 Currency rows, flags, and rate status bar
│   ├── screens/                    Converter, chooser, camera, history, settings, etc.
│   └── theme/                      Light/dark Material theme
└── util/                            Number formatting, flags, and OCR amount parsing
```

The UI is written with Jetpack Compose. `MainViewModel` owns shared observable state and delegates storage, network access, and conversion math to the `data` and `domain` layers. Screen composables render that state and call view-model actions.

## Data sources and privacy

- Latest rates: [ExchangeRate-API Open Access](https://www.exchangerate-api.com/docs/free), cached locally with its required attribution shown in the app.
- Historical rates: [Frankfurter](https://www.frankfurter.app/).
- Camera text recognition: Google ML Kit Text Recognition on-device. The app does not upload the camera image to an AI service or save captured frames to the gallery.
- Saved settings, selections, custom currencies, colors, and latest-rate cache are stored locally using Android `SharedPreferences`.

The unlockable-currency catalog uses a single USD or ISO-currency reference factor. Store prices, bundles, regional prices, reward tiers, and fan estimates can change or vary by platform and country. These values are for comparison only and must not be treated as exchange offers or redeemable balances.

## Languages

The settings list offers English, German, Spanish, Simplified Chinese, Japanese, Hindi, Arabic, French, Bengali, Portuguese, Russian, Indonesian, Urdu, Turkish, Vietnamese, Thai, Korean, Italian, Dutch, Polish, Malay, and Swahili. English, German, Spanish, Chinese, and Japanese have the broadest translation coverage. Some longer explanatory text in the other languages can still fall back to English.

## License

No project-level `LICENSE` file is included yet. Choose and add a license before redistributing this source. Third-party libraries and rate providers have their own terms and attribution requirements.
