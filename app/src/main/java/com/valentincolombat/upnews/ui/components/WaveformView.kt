package com.valentincolombat.upnews.ui.components

import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.valentincolombat.upnews.ui.theme.UpNewsBlueMid
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

private const val BAR_COUNT = 30

@Composable
fun WaveformView(isPlaying: Boolean) {
    val amplitudes = remember { mutableStateListOf<Float>().also { list ->
        repeat(BAR_COUNT) { list.add(4f) }
    }}

    // Démarre ou arrête l'animation selon isPlaying
    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            while (isActive) {
                repeat(BAR_COUNT) { i ->
                    amplitudes[i] = (8f..40f).random()
                }
                delay(150)
            }
        } else {
            repeat(BAR_COUNT) { i -> amplitudes[i] = 4f }
        }
    }

    val gradient = Brush.verticalGradient(
        colors = listOf(
            UpNewsBlueMid.copy(alpha = 0.4f),
            UpNewsBlueMid.copy(alpha = 0.8f)
        )
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.height(40.dp)
    ) {
        amplitudes.forEachIndexed { index, targetHeight ->
            val animatedHeight by animateFloatAsState(
                targetValue = targetHeight,
                animationSpec = tween(durationMillis = 300, easing = EaseInOut),
                label = "bar_$index"
            )

            Box(
                modifier = Modifier
                    .width(2.dp)
                    .height(animatedHeight.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(gradient)
            )

            if (index < BAR_COUNT - 1) {
                Spacer(modifier = Modifier.width(2.dp))
            }
        }
    }
}

private fun ClosedFloatingPointRange<Float>.random(): Float =
    start + (endInclusive - start) * kotlin.random.Random.nextFloat()
