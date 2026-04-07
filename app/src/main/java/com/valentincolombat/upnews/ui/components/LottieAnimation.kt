package com.valentincolombat.upnews.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.valentincolombat.upnews.ui.theme.UpNewsOrange

// MARK: - Animation cadeau (companions verrouillés)
// Fichier requis : src/main/assets/gift.lottie
@Composable
fun LockLottieView() {
    val composition by rememberLottieComposition(LottieCompositionSpec.Asset("Gift.json"))
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever
    )
    LottieAnimation(
        composition = composition,
        progress = { progress },
        modifier = Modifier.size(40.dp)
    )
}

// MARK: - Animation flamme (streak)
// Fichier requis : src/main/assets/fire.lottie
@Composable
fun FlameLottieView() {
    val composition by rememberLottieComposition(LottieCompositionSpec.Asset("Fire.json"))

    if (composition != null) {
        val progress by animateLottieCompositionAsState(
            composition = composition,
            iterations = LottieConstants.IterateForever
        )
        LottieAnimation(
            composition = composition,
            progress = { progress },
            modifier = Modifier.size(40.dp)
        )
    } else {
        // Fallback pendant le chargement
        Icon(
            imageVector = Icons.Default.LocalFireDepartment,
            contentDescription = null,
            tint = UpNewsOrange,
            modifier = Modifier.size(24.dp)
        )
    }
}
