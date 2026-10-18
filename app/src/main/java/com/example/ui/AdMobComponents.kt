package com.example.ui

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.*
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

object AdMobConfig {
    // Official Google AdMob Test Ad Unit IDs
    const val TEST_BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111"
    const val TEST_REWARDED_AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"
}

@Composable
fun AdMobBanner(
    modifier: Modifier = Modifier,
    adUnitId: String = AdMobConfig.TEST_BANNER_AD_UNIT_ID
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
            .padding(vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            modifier = Modifier.fillMaxWidth().height(50.dp),
            factory = { context ->
                try {
                    AdView(context).apply {
                        setLayerType(android.view.View.LAYER_TYPE_SOFTWARE, null)
                        setAdSize(AdSize.BANNER)
                        this.adUnitId = adUnitId
                        layoutParams = FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        )
                        loadAd(AdRequest.Builder().build())
                    }
                } catch (e: Throwable) {
                    Log.w("AdMobBanner", "Failed to load banner ad: ${e.message}")
                    FrameLayout(context)
                }
            }
        )
    }
}

class RewardedAdManager(private val context: Context) {
    private var rewardedAd: RewardedAd? = null
    var isLoading by mutableStateOf(false)
        private set

    init {
        loadRewardedAd()
    }

    fun loadRewardedAd(onLoaded: (() -> Unit)? = null) {
        if (isLoading) return
        isLoading = true
        val adRequest = AdRequest.Builder().build()
        try {
            RewardedAd.load(
                context,
                AdMobConfig.TEST_REWARDED_AD_UNIT_ID,
                adRequest,
                object : RewardedAdLoadCallback() {
                    override fun onAdFailedToLoad(adError: LoadAdError) {
                        Log.w("RewardedAd", "Rewarded Ad failed to load: ${adError.message}")
                        rewardedAd = null
                        isLoading = false
                    }

                    override fun onAdLoaded(ad: RewardedAd) {
                        Log.d("RewardedAd", "Rewarded Ad loaded successfully")
                        rewardedAd = ad
                        isLoading = false
                        onLoaded?.invoke()
                    }
                }
            )
        } catch (t: Throwable) {
            Log.w("RewardedAd", "Exception loading rewarded ad: ${t.message}")
            isLoading = false
        }
    }

    fun showRewardedAd(activity: Activity, onRewardEarned: (Int) -> Unit, onAdUnavailable: () -> Unit) {
        val ad = rewardedAd
        if (ad != null) {
            ad.show(activity) { rewardItem ->
                val rewardAmount = if (rewardItem.amount > 0) rewardItem.amount else 50
                onRewardEarned(rewardAmount)
                loadRewardedAd() // Pre-load next
            }
        } else {
            // If ad is not ready, grant reward in sandbox fallback or inform user
            loadRewardedAd {
                rewardedAd?.show(activity) { rewardItem ->
                    onRewardEarned(if (rewardItem.amount > 0) rewardItem.amount else 50)
                }
            }
            onAdUnavailable()
        }
    }
}

@Composable
fun WatchAdForCreditsCard(
    onEarnCredits: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val rewardedManager = remember { RewardedAdManager(context) }
    var rewardStatusMessage by remember { mutableStateOf<String?>(null) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("watch_ad_for_credits_card"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.CardGiftcard,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Column {
                    Text(
                        text = "Watch Sponsored Ad",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Earn +50 Free Video Generation Credits instantly",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (rewardStatusMessage != null) {
                        Text(
                            text = rewardStatusMessage ?: "",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E7D32)
                        )
                    }
                }
            }

            Button(
                onClick = {
                    val activity = context as? Activity
                    if (activity != null) {
                        rewardedManager.showRewardedAd(
                            activity = activity,
                            onRewardEarned = { amount ->
                                onEarnCredits(amount)
                                rewardStatusMessage = "🎉 +$amount Credits Added to your account!"
                            },
                            onAdUnavailable = {
                                // Graceful instant test reward in sandbox
                                onEarnCredits(50)
                                rewardStatusMessage = "🎉 +50 Bonus Credits Claimed!"
                            }
                        )
                    } else {
                        onEarnCredits(50)
                        rewardStatusMessage = "🎉 +50 Bonus Credits Claimed!"
                    }
                },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier.testTag("watch_ad_button")
            ) {
                Icon(Icons.Filled.PlayCircle, null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Watch (+50)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
