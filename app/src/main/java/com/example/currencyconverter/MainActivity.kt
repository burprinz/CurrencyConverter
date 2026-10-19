package com.example.currencyconverter

import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.currencyconverter.ui.CurrencyApp
import com.google.android.gms.ads.MobileAds
import com.google.android.ump.ConsentDebugSettings
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Gemeinsamer Zustand für das Laden von Werbeanzeigen.
 */
object AdMobState {
    var adsReady by mutableStateOf(false)
        private set

    fun updateAdsReady(ready: Boolean) {
        adsReady = ready
    }
}

class MainActivity : ComponentActivity() {

    companion object {
        // Nur für Tests. Vor dem Release auf false setzen.
        private const val CONSENT_DEBUG = false

        private const val TEST_DEVICE_ID =
            "B3EEABB8EE11C2BE770B684D95219ECB"
    }

    private lateinit var consentInformation: ConsentInformation
    private val mobileAdsInitialized = AtomicBoolean(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            CurrencyApp()
        }

        logManifestAppId()
        requestConsent()
    }

    @Suppress("DEPRECATION")
    private fun logManifestAppId() {
        try {
            val metaData = packageManager
                .getApplicationInfo(
                    packageName,
                    PackageManager.GET_META_DATA
                )
                .metaData

            val appId = metaData?.getString(
                "com.google.android.gms.ads.APPLICATION_ID"
            )

            Log.d("AdMobConsent", "Package: $packageName")
            Log.d("AdMobConsent", "Manifest APPLICATION_ID: $appId")

            if (appId == "ca-app-pub-3940256099942544~3347511713") {
                Log.w(
                    "AdMobConsent",
                    "Google-Test-App-ID erkannt."
                )
            }
        } catch (e: Exception) {
            Log.e(
                "AdMobConsent",
                "Konnte APPLICATION_ID nicht lesen: ${e.message}"
            )
        }
    }

    private fun requestConsent() {
        consentInformation =
            UserMessagingPlatform.getConsentInformation(this)

        val paramsBuilder = ConsentRequestParameters.Builder()

        Log.d("AdMobConsent", "CONSENT_DEBUG = $CONSENT_DEBUG")

        if (CONSENT_DEBUG) {
            // Nur während der Entwicklung:
            // Einwilligung bei jedem Start zurücksetzen.
            consentInformation.reset()

            val debugSettings = ConsentDebugSettings.Builder(this)
                .setDebugGeography(
                    ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA
                )
                .addTestDeviceHashedId(TEST_DEVICE_ID)
                .build()

            paramsBuilder.setConsentDebugSettings(debugSettings)
        }

        val params = paramsBuilder.build()

        consentInformation.requestConsentInfoUpdate(
            this,
            params,
            {
                Log.d(
                    "AdMobConsent",
                    "Consent status: ${consentInformation.consentStatus}"
                )
                Log.d(
                    "AdMobConsent",
                    "Privacy options: " +
                            consentInformation.privacyOptionsRequirementStatus
                )
                Log.d(
                    "AdMobConsent",
                    "Form available: " +
                            consentInformation.isConsentFormAvailable
                )

                UserMessagingPlatform.loadAndShowConsentFormIfRequired(
                    this
                ) { formError ->
                    if (formError != null) {
                        Log.e(
                            "AdMobConsent",
                            "Consent form error: " +
                                    "${formError.errorCode} ${formError.message}"
                        )
                    } else {
                        Log.d(
                            "AdMobConsent",
                            "Consent form flow completed."
                        )
                    }

                    checkConsentStatus()
                }

                // Eine frühere gültige Einwilligung kann bereits vorliegen.
                checkConsentStatus()
            },
            { error ->
                Log.e(
                    "AdMobConsent",
                    "Consent update error: " +
                            "${error.errorCode} ${error.message}"
                )

                // Vorhandene Einwilligung prüfen, falls das Update fehlschlägt.
                checkConsentStatus()
            }
        )
    }

    private fun checkConsentStatus() {
        if (consentInformation.canRequestAds()) {
            Log.d("AdMobConsent", "Ads may be requested.")
            initializeMobileAds()
        } else {
            AdMobState.updateAdsReady(false)
            Log.d(
                "AdMobConsent",
                "Ads must not be requested yet."
            )
        }
    }

    private fun initializeMobileAds() {
        if (!mobileAdsInitialized.compareAndSet(false, true)) {
            return
        }

        MobileAds.initialize(this) {
            runOnUiThread {
                // Erneut prüfen, bevor das Banner freigegeben wird.
                if (consentInformation.canRequestAds()) {
                    AdMobState.updateAdsReady(true)
                    Log.d("AdMobConsent", "MobileAds initialized; ads ready.")
                } else {
                    AdMobState.updateAdsReady(false)
                    Log.d(
                        "AdMobConsent",
                        "Consent does not permit ad requests."
                    )
                }
            }
        }
    }
}