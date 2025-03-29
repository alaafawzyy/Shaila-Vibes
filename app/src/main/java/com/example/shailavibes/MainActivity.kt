
package com.example.shailavibes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.shailavibes.ui.presentation.MusicPlayerScreen
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

class MainActivity : ComponentActivity() {

    private var interstitialAd: InterstitialAd? = null
    private var appOpenAd: AppOpenAd? = null
    private var touchCount = 0
    private var isAppStopped = false // عشان نعرف التطبيق خرج من الشاشة
    private var hasShownAdOnStart = false // عشان نمنع التكرار في نفس الـ Session

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        MobileAds.initialize(this) {
            loadAppOpenAd()
            loadInterstitialAd()
        }

        val requestConfiguration = RequestConfiguration.Builder()
            .setTestDeviceIds(listOf(getString(R.string.device_id)))
            .build()
        MobileAds.setRequestConfiguration(requestConfiguration)
    }

    override fun onStart() {
        super.onStart()
        // لو التطبيق كان مخفي ولم نعرض إعلان بعد في الـ Start ده، نعرض الإعلان
        if (isAppStopped && !hasShownAdOnStart && appOpenAd != null) {
            showAppOpenAd()
        } else if (isAppStopped && !hasShownAdOnStart) {
            loadAppOpenAd()
        }
        isAppStopped = false // نرجعه لـ false لما التطبيق يظهر
    }

    override fun onStop() {
        super.onStop()
        // لما التطبيق يختفي من الشاشة، نشغل المتغير ونرجع الإعلان جاهز للظهور
        isAppStopped = true
        hasShownAdOnStart = false // نرجعه لـ false عشان يظهر الإعلان لما يرجع
    }

    private fun loadAppOpenAd() {
        val adRequest = AdRequest.Builder().build()
        AppOpenAd.load(
            this,
            getString(R.string.open_id),
            adRequest,
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    appOpenAd = ad
                    // نعرض الإعلان بس لو التطبيق كان مخفي ولم نعرض إعلان بعد
                    if ((isAppStopped || !hasShownAdOnStart) && !hasShownAdOnStart) {
                        showAppOpenAd()
                    }
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    println("App Open Ad failed to load: ${error.message}")
                    appOpenAd = null
                    setMainContent()
                }
            }
        )
    }

    private fun showAppOpenAd() {
        appOpenAd?.let { ad ->
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    hasShownAdOnStart = true // نشير إن الإعلان اتعرض في الـ Start ده
                    appOpenAd = null
                    loadAppOpenAd() // نحمل إعلان جديد للمرة الجاية
                    setMainContent()
                }

                override fun onAdFailedToShowFullScreenContent(error: AdError) {
                    hasShownAdOnStart = true // حتى لو فشل، نعتبره اتعرض عشان ما يتكررش
                    appOpenAd = null
                    loadAppOpenAd()
                    setMainContent()
                }
            }
            ad.show(this)
        } ?: run {
            setMainContent()
        }
    }

    private fun setMainContent() {
        setContent {
            MusicPlayerScreen(activity = this, initialize = Unit)
        }
    }

    private fun loadInterstitialAd() {
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            this,
            getString(R.string.Interstitial_id),
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    interstitialAd = null
                    println("Interstitial Ad failed to load: ${error.message}")
                }
            }
        )
    }

    private fun showInterstitialAd() {
        interstitialAd?.let { ad ->
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    interstitialAd = null
                    loadInterstitialAd()
                }

                override fun onAdFailedToShowFullScreenContent(error: AdError) {
                    interstitialAd = null
                    loadInterstitialAd()
                }
            }
            ad.show(this)
        } ?: run {
            loadInterstitialAd()
        }
    }

    private fun handleTouch() {
        touchCount++
        if (touchCount == 1 || touchCount % 4 == 0) {
            showInterstitialAd()
        }
    }
}


