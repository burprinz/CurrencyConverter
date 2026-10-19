package com.example.currencyconverter

import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

@Composable
fun AdBanner(modifier: Modifier = Modifier) {

    val adsReady by AdMobState::adsReady
    if (!adsReady) return


    val context = LocalContext.current

    AndroidView(
        modifier = modifier,
        factory = {
            AdView(context).apply {
                setAdSize(
                    AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(
                        context,
                        (
                                context.resources.displayMetrics.widthPixels /
                                        context.resources.displayMetrics.density
                                ).toInt()
                    )
                )

                adUnitId = "ca-app-pub-7363535846240442/3205641970"

                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )

                loadAd(AdRequest.Builder().build())
            }
        }
    )
}