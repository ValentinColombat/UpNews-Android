package com.valentincolombat.upnews.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import com.valentincolombat.upnews.ui.theme.ConfettiBlue
import com.valentincolombat.upnews.ui.theme.ConfettiGreen
import com.valentincolombat.upnews.ui.theme.ConfettiOrange
import com.valentincolombat.upnews.ui.theme.ConfettiPurple
import com.valentincolombat.upnews.ui.theme.ConfettiRed
import com.valentincolombat.upnews.ui.theme.ConfettiYellow
import androidx.compose.ui.graphics.toArgb
import nl.dionsegijn.konfetti.compose.KonfettiView
import nl.dionsegijn.konfetti.compose.OnParticleSystemUpdateListener
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.PartySystem
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.emitter.Emitter
import java.util.concurrent.TimeUnit
import kotlin.random.Random

// MARK: - Palette partagée

private val confettiColorsCompose = listOf(
    ConfettiOrange,
    ConfettiYellow,
    ConfettiGreen,
    ConfettiBlue,
    ConfettiRed,
    ConfettiPurple,
)

private val confettiColorsArgb = confettiColorsCompose.map { it.toArgb() }

// MARK: - Konfetti (KonfettiView AndroidView) --------------------------------

fun heavyConfettiParties(x: Float, y: Float): List<Party> = listOf(
    Party(
        colors   = confettiColorsArgb,
        angle    = 270,
        spread   = 80,
        speed    = 8f,
        maxSpeed = 22f,
        damping  = 0.94f,
        timeToLive = 3500L,
        size     = listOf(
            nl.dionsegijn.konfetti.core.models.Size(14),
            nl.dionsegijn.konfetti.core.models.Size(18)
        ),
        position = Position.Absolute(x, y),
        emitter  = Emitter(duration = 800, TimeUnit.MILLISECONDS).perSecond(120)
    ),
    Party(
        colors   = confettiColorsArgb,
        angle    = 270,
        spread   = 50,
        speed    = 4f,
        maxSpeed = 10f,
        damping  = 0.96f,
        timeToLive = 4500L,
        size     = listOf(nl.dionsegijn.konfetti.core.models.Size(22)),
        position = Position.Absolute(x, y),
        emitter  = Emitter(duration = 600, TimeUnit.MILLISECONDS).perSecond(20)
    )
)

fun companionUnlockConfettiParties(): List<Party> = listOf(
    Party(
        colors   = confettiColorsArgb,
        angle    = 135,
        spread   = 60,
        speed    = 2f,
        maxSpeed = 8f,
        damping  = 0.95f,
        size     = listOf(nl.dionsegijn.konfetti.core.models.Size(10)),
        position = Position.Relative(0.0, 0.0),
        emitter  = Emitter(duration = 2, TimeUnit.SECONDS).perSecond(40)
    ),
    Party(
        colors   = confettiColorsArgb,
        angle    = 45,
        spread   = 60,
        speed    = 2f,
        maxSpeed = 8f,
        damping  = 0.95f,
        size     = listOf(nl.dionsegijn.konfetti.core.models.Size(10)),
        position = Position.Relative(1.0, 0.0),
        emitter  = Emitter(duration = 2, TimeUnit.SECONDS).perSecond(40)
    )
)

@Composable
fun ConfettiOverlay(
    parties: List<Party>,
    modifier: Modifier = Modifier,
    onFinished: () -> Unit = {}
) {
    KonfettiView(
        modifier = modifier,
        parties  = parties,
        updateListener = object : OnParticleSystemUpdateListener {
            override fun onParticleSystemEnded(system: PartySystem, activeSystems: Int) {
                if (activeSystems == 0) onFinished()
            }
        }
    )
}

// MARK: - Canvas pur Compose (z-ordering garanti) ----------------------------

private data class ConfettiParticle(
    val x0: Float, val y0: Float,
    val vx: Float, val vy0: Float,
    val rotation0: Float, val rotSpeed: Float,
    val color: Color,
    val w: Float, val h: Float,
)

private fun generateCornerConfettiParticles(rng: Random = Random.Default): List<ConfettiParticle> {
    val list = mutableListOf<ConfettiParticle>()
    repeat(55) {
        list += ConfettiParticle(
            x0 = rng.nextFloat() * 0.18f, y0 = -rng.nextFloat() * 0.12f,
            vx = rng.nextFloat() * 0.18f + 0.04f,
            vy0 = rng.nextFloat() * 0.30f + 0.05f,
            rotation0 = rng.nextFloat() * 360f,
            rotSpeed = rng.nextFloat() * 300f - 150f,
            color = confettiColorsCompose.random(rng),
            w = rng.nextFloat() * 10f + 6f, h = rng.nextFloat() * 6f + 4f,
        )
    }
    repeat(55) {
        list += ConfettiParticle(
            x0 = 0.82f + rng.nextFloat() * 0.18f, y0 = -rng.nextFloat() * 0.12f,
            vx = -(rng.nextFloat() * 0.18f + 0.04f),
            vy0 = rng.nextFloat() * 0.30f + 0.05f,
            rotation0 = rng.nextFloat() * 360f,
            rotSpeed = rng.nextFloat() * 300f - 150f,
            color = confettiColorsCompose.random(rng),
            w = rng.nextFloat() * 10f + 6f, h = rng.nextFloat() * 6f + 4f,
        )
    }
    return list
}

private const val DURATION_S = 3.5f
private const val GRAVITY    = 0.35f
private const val FADE_START = 2.2f

/**
 * Confettis 100% Compose Canvas depuis les coins supérieurs.
 * Utilisé pour la pop-up de déblocage compagnon (z-ordering garanti au-dessus de tout).
 */
@Composable
fun ComposeConfettiOverlay(
    modifier: Modifier = Modifier,
    onFinished: () -> Unit = {}
) {
    val particles = remember { generateCornerConfettiParticles() }
    var elapsed by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(Unit) {
        var lastMs = 0L
        while (elapsed < DURATION_S) {
            withFrameMillis { frameMs ->
                if (lastMs != 0L) elapsed += (frameMs - lastMs) / 1000f
                lastMs = frameMs
            }
        }
        onFinished()
    }

    val t = elapsed
    Canvas(modifier = modifier.fillMaxSize()) {
        particles.forEach { p ->
            val px = (p.x0 + p.vx * t) * size.width
            val py = (p.y0 + p.vy0 * t + 0.5f * GRAVITY * t * t) * size.height
            if (py > size.height) return@forEach
            val alpha = if (t < FADE_START) 1f
                        else (1f - (t - FADE_START) / (DURATION_S - FADE_START)).coerceIn(0f, 1f)
            val pivot = Offset(px, py)
            rotate(p.rotation0 + p.rotSpeed * t, pivot) {
                drawRect(p.color.copy(alpha = alpha), Offset(px - p.w / 2f, py - p.h / 2f), Size(p.w, p.h))
            }
        }
    }
}
