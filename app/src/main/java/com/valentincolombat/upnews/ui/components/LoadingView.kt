package com.valentincolombat.upnews.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.valentincolombat.upnews.ui.theme.UpNewsBackground
import com.valentincolombat.upnews.ui.theme.UpNewsOrange

@Composable
fun LoadingView() {
    val transition = rememberInfiniteTransition(label = "ripple")

    // 3 anneaux décalés pour un effet ondulation continu
    val ripples = List(3) { index ->
        val scale by transition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1800, easing = LinearEasing),
                repeatMode = RepeatMode.Restart,
                initialStartOffset = androidx.compose.animation.core.StartOffset(index * 600)
            ),
            label = "ripple_$index"
        )
        scale
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(UpNewsBackground),
        contentAlignment = Alignment.Center
    ) {
        ripples.forEach { scale ->
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .scale(scale)
                    .border(
                        width = 2.dp,
                        color = UpNewsOrange.copy(alpha = (1f - scale).coerceIn(0f, 1f)),
                        shape = CircleShape
                    )
            )
        }
    }
}
