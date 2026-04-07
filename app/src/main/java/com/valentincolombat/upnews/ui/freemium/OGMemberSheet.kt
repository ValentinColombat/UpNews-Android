package com.valentincolombat.upnews.ui.freemium

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AllInclusive
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.valentincolombat.upnews.ui.theme.OGBrown
import com.valentincolombat.upnews.ui.theme.OGGold
import com.valentincolombat.upnews.ui.theme.TextDark
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun OGMemberSheet(onDismiss: () -> Unit) {
    var showContent by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) { showContent = true }

    val scale by animateFloatAsState(
        targetValue = if (showContent) 1f else 0.9f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = Spring.StiffnessMediumLow),
        label = "sheet_scale"
    )

    fun dismiss() {
        scope.launch {
            showContent = false
            delay(200)
            onDismiss()
        }
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .clickable(onClick = ::dismiss)
    ) {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = Color.White,
                shadowElevation = 20.dp,
                modifier = Modifier
                    .padding(horizontal = 20.dp, vertical = 60.dp)
                    .fillMaxWidth()
                    .scale(scale)
                    .clickable(enabled = false) {}
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 20.dp)
                ) {
                    // Header avec badge OG
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Badge OG circulaire
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .shadow(
                                    elevation = 14.dp,
                                    shape = CircleShape,
                                    spotColor = OGBrown.copy(alpha = 0.4f)
                                )
                                .size(60.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(OGBrown, OGGold)
                                    )
                                )
                        ) {
                            Text(
                                text = "OG",
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Merci !",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextDark
                            )
                            Text(
                                text = "Tu fais partie des 50 premiers membres",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = OGBrown,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    // Message de remerciement
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Tu as cru en nous dès le début",
                            fontSize = 14.sp,
                            color = TextDark,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Pour te remercier de ton soutien lors des premiers moments d'UpNews, tu as accès à toutes les fonctionnalités de l'application à vie, gratuitement.",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )
                    }

                    // Avantages OG
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OGFeatureRow(
                            icon = Icons.Default.AllInclusive,
                            iconBackground = OGGold.copy(alpha = 0.2f),
                            title = "Accès Premium à vie",
                            description = "Toutes les fonctionnalités, pour toujours"
                        )
                        OGFeatureRow(
                            icon = Icons.Default.EmojiEvents,
                            iconBackground = OGBrown.copy(alpha = 0.2f),
                            title = "Badge exclusif OG",
                            description = "Visible uniquement par les pionniers"
                        )
                    }

                    // Bouton Merci
                    Button(
                        onClick = ::dismiss,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .shadow(
                                elevation = 8.dp,
                                shape = RoundedCornerShape(12.dp),
                                spotColor = OGBrown.copy(alpha = 0.3f)
                            )
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                Brush.horizontalGradient(colors = listOf(OGBrown, OGGold))
                            )
                    ) {
                        Text(
                            text = "Merci !",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
}

// MARK: - Feature Row

@Composable
private fun OGFeatureRow(
    icon: ImageVector,
    iconBackground: Color,
    title: String,
    description: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(iconBackground)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = OGBrown,
                modifier = Modifier.size(18.dp)
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextDark
            )
            Text(
                text = description,
                fontSize = 11.sp,
                color = Color.Gray
            )
        }

        Spacer(modifier = Modifier.weight(1f))
    }
}
