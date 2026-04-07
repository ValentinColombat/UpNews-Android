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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.valentincolombat.upnews.ui.theme.TextDark
import com.valentincolombat.upnews.ui.theme.UpNewsOrange
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun PremiumInfoSheet(onDismiss: () -> Unit) {
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
        contentAlignment = Alignment.TopCenter,
        modifier = Modifier
            .fillMaxSize()
            .clickable(onClick = ::dismiss)
    ) {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = Color.White,
                shadowElevation = 20.dp,
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .statusBarsPadding()
                    .padding(top = 80.dp)
                    .fillMaxWidth()
                    .scale(scale)
                    .clickable(enabled = false) {} // consomme le clic pour ne pas fermer
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    // Header
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 20.dp, end = 12.dp, top = 16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = UpNewsOrange,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Tu es Premium !",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextDark
                            )
                        }

                        IconButton(onClick = ::dismiss) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color.Gray.copy(alpha = 0.1f))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Fermer",
                                    tint = Color.Gray,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }

                    // Features
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.padding(horizontal = 20.dp)
                    ) {
                        FeatureRow(
                            icon = Icons.AutoMirrored.Filled.Article,
                            title = "Articles illimités",
                            description = "Accès à tous les articles sans restriction"
                        )
                        FeatureRow(
                            icon = Icons.Default.Headphones,
                            title = "Audio haute qualité",
                            description = "Écoute intégrale de tous les articles"
                        )
                        FeatureRow(
                            icon = Icons.AutoMirrored.Filled.MenuBook,
                            title = "Bibliothèque complète",
                            description = "Sauvegarde et consulte tous tes articles"
                        )
                        FeatureRow(
                            icon = Icons.Default.Pets,
                            title = "Tous les compagnons",
                            description = "Accède à tous les compagnons"
                        )
                        FeatureRow(
                            icon = Icons.Default.Bolt,
                            title = "XP bonus x2",
                            description = "Gagne 40 XP par article au lieu de 20"
                        )
                    }
                }
            }
        }
}

// MARK: - Feature Row

@Composable
private fun FeatureRow(icon: ImageVector, title: String, description: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(UpNewsOrange.copy(alpha = 0.15f))
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = UpNewsOrange,
                modifier = Modifier.size(17.dp)
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextDark,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = description,
                fontSize = 12.sp,
                color = Color.Gray,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
