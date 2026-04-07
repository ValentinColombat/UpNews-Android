package com.valentincolombat.upnews.ui.components

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.withFrameMillis
import com.valentincolombat.upnews.ui.theme.XpGradientEnd
import com.valentincolombat.upnews.ui.theme.XpGradientStart
import kotlin.random.Random

private val EaseInOutCubic = CubicBezierEasing(0.65f, 0f, 0.35f, 1f)

private data class Particle(
    val phase: Float,       // décalage de phase initial (0..1)
    val speed: Float,       // vitesse en cycles/seconde
    val xOffset: Float,     // décalage horizontal relatif à la largeur (-0.3..0.3)
    val radiusFraction: Float, // rayon relatif à la largeur de la barre
    val maxAlpha: Float,    // opacité maximale
)

@Composable
fun VerticalProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    trackColor: Color = Color.White.copy(alpha = 0.25f),
) {
    // Démarre à 0 puis monte vers la valeur réelle au premier rendu
    var targetProgress by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(progress) {
        delay(300)  // légère pause après l'apparition de l'écran
        targetProgress = progress.coerceIn(0f, 1f)
    }

    val animatedProgress by animateFloatAsState(
        targetValue   = targetProgress,
        animationSpec = tween(durationMillis = 4000, easing = EaseInOutCubic),
        label         = "xpProgress"
    )

    // Particules avec propriétés aléatoires fixes
    val particles = remember {
        List(9) {
            Particle(
                phase         = Random.nextFloat(),
                speed         = 0.22f + Random.nextFloat() * 0.30f,   // 0.22–0.52 cycles/s
                xOffset       = (Random.nextFloat() - 0.5f) * 0.45f,  // ±22% de la largeur
                radiusFraction= 0.10f + Random.nextFloat() * 0.12f,   // 10–22% de la largeur
                maxAlpha      = 0.08f + Random.nextFloat() * 0.13f,   // 0.08–0.21
            )
        }
    }

    // Temps global en secondes, mis à jour chaque frame
    var time by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(Unit) {
        var previous = -1L
        while (true) {
            withFrameMillis { frameMs ->
                if (previous != -1L) time += (frameMs - previous) / 1000f
                previous = frameMs
            }
        }
    }

    Canvas(modifier = modifier) {
        val cornerPx = 6.dp.toPx()
        val cr = CornerRadius(cornerPx)

        // Track
        drawRoundRect(color = trackColor, cornerRadius = cr)

        val fillHeight = size.height * animatedProgress
        if (fillHeight <= 0f) return@Canvas

        // Dégradé orange (clair en bas → intense en haut)
        drawRoundRect(
            brush = Brush.verticalGradient(
                colors = listOf(XpGradientStart, XpGradientEnd),
                startY = size.height,
                endY   = size.height - fillHeight
            ),
            topLeft      = Offset(0f, size.height - fillHeight),
            size         = Size(size.width, fillHeight),
            cornerRadius = cr
        )

        // Particules
        particles.forEach { p ->
            // Avance dans le cycle selon la vitesse propre de chaque particule
            val t = ((time * p.speed + p.phase) % 1f)
            // Zone d'invisibilité : 0.88..1.0 = reset silencieux
            if (t >= 0.88f) return@forEach

            val pos = t / 0.88f  // position normalisée 0→1 dans la zone remplie

            // Fondu entrant / sortant
            val alpha = when {
                pos < 0.10f -> (pos / 0.10f) * p.maxAlpha
                pos > 0.72f -> ((1f - pos) / 0.28f) * p.maxAlpha
                else        -> p.maxAlpha
            }

            // Position : monte de bas en haut dans la zone remplie
            val x = size.width * (0.5f + p.xOffset)
            val y = size.height - fillHeight * pos

            drawCircle(
                color  = Color.White.copy(alpha = alpha),
                radius = size.width * p.radiusFraction,
                center = Offset(x, y)
            )
        }
    }
}

// ─── Barre XP horizontale ────────────────────────────────────────────────────

@Composable
fun HorizontalXpBar(
    progress: Float,
    modifier: Modifier = Modifier,
    trackColor: Color = Color.White.copy(alpha = 0.25f),
) {
    var targetProgress by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(progress) {
        delay(300)
        targetProgress = progress.coerceIn(0f, 1f)
    }

    val animatedProgress by animateFloatAsState(
        targetValue   = targetProgress,
        animationSpec = tween(durationMillis = 4000, easing = EaseInOutCubic),
        label         = "xpHorizontal"
    )

    val particles = remember {
        List(9) {
            Particle(
                phase          = Random.nextFloat(),
                speed          = 0.22f + Random.nextFloat() * 0.30f,
                xOffset        = (Random.nextFloat() - 0.5f) * 0.45f,
                radiusFraction = 0.10f + Random.nextFloat() * 0.12f,
                maxAlpha       = 0.08f + Random.nextFloat() * 0.13f,
            )
        }
    }

    var time by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(Unit) {
        var previous = -1L
        while (true) {
            withFrameMillis { frameMs ->
                if (previous != -1L) time += (frameMs - previous) / 1000f
                previous = frameMs
            }
        }
    }

    Canvas(modifier = modifier) {
        val cornerPx = 6.dp.toPx()
        val cr = CornerRadius(cornerPx)

        // Track
        drawRoundRect(color = trackColor, cornerRadius = cr)

        val fillWidth = size.width * animatedProgress
        if (fillWidth <= 0f) return@Canvas

        // Dégradé orange (gauche → droite)
        drawRoundRect(
            brush = Brush.horizontalGradient(
                colors = listOf(XpGradientStart, XpGradientEnd),
                startX = 0f,
                endX   = fillWidth
            ),
            topLeft      = Offset(0f, 0f),
            size         = Size(fillWidth, size.height),
            cornerRadius = cr
        )

        // Particules (se déplacent de gauche à droite dans la zone remplie)
        particles.forEach { p ->
            val t = ((time * p.speed + p.phase) % 1f)
            if (t >= 0.88f) return@forEach

            val pos = t / 0.88f

            val alpha = when {
                pos < 0.10f -> (pos / 0.10f) * p.maxAlpha
                pos > 0.72f -> ((1f - pos) / 0.28f) * p.maxAlpha
                else        -> p.maxAlpha
            }

            val x = fillWidth * pos
            val y = size.height * (0.5f + p.xOffset)

            drawCircle(
                color  = Color.White.copy(alpha = alpha),
                radius = size.height * p.radiusFraction,
                center = Offset(x, y)
            )
        }
    }
}
