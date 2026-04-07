package com.valentincolombat.upnews.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.valentincolombat.upnews.ui.theme.TextDark
import com.valentincolombat.upnews.ui.theme.UpNewsOrange
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class CompanionEntry(val name: String, val imageName: String)

@Composable
fun UnlockCompanionPopup(
    companions: List<CompanionEntry>,
    onDismiss: () -> Unit,
    onNavigateToCompanions: (() -> Unit)? = null
) {
    var showContent by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) { showContent = true }

    val scale by animateFloatAsState(
        targetValue = if (showContent) 1f else 0.8f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = Spring.StiffnessMediumLow),
        label = "popup_scale"
    )
    val alpha by animateFloatAsState(
        targetValue = if (showContent) 1f else 0f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = Spring.StiffnessMediumLow),
        label = "popup_alpha"
    )

    fun dismiss() {
        scope.launch {
            showContent = false
            delay(200)
            onDismiss()
        }
    }

    Dialog(
        onDismissRequest = ::dismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.4f))
        ) {
            Surface(
                shape = RoundedCornerShape(32.dp),
                color = Color.White.copy(alpha = 0.9f),
                shadowElevation = 24.dp,
                modifier = Modifier
                    .padding(horizontal = 32.dp)
                    .fillMaxWidth()
                    .fillMaxHeight(0.85f)
                    .scale(scale)
                    .border(
                        width = 2.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.8f),
                                UpNewsOrange.copy(alpha = 0.3f)
                            )
                        ),
                        shape = RoundedCornerShape(32.dp)
                    )
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Header
                    PopupHeader(onClose = ::dismiss)

                    // Titre
                    TitleSection(
                        companions = companions,
                        showContent = showContent,
                        modifier = Modifier.padding(top = 16.dp)
                    )

                    // Carrousel (prend l'espace restant)
                    CompanionsCarousel(
                        companions = companions,
                        showContent = showContent,
                        modifier = Modifier
                            .weight(1f)
                            .padding(top = 8.dp)
                    )

                    // Bouton action
                    ActionButton(
                        hasNavigation = onNavigateToCompanions != null,
                        onClick = {
                            scope.launch {
                                showContent = false
                                delay(200)
                                if (onNavigateToCompanions != null) onNavigateToCompanions()
                                else onDismiss()
                            }
                        },
                        modifier = Modifier
                            .padding(horizontal = 24.dp)
                            .padding(bottom = 24.dp)
                            .padding(top = 12.dp)
                    )
                }
            }
        }
    }
}

// MARK: - Header

@Composable
private fun PopupHeader(onClose: () -> Unit) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(top = 20.dp)
    ) {
        // Badge NOUVEAU
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(CircleShape)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(UpNewsOrange, UpNewsOrange.copy(alpha = 0.8f))
                    )
                )
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(10.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "NOUVEAU",
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Bouton fermer
        IconButton(onClick = onClose) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color.Gray.copy(alpha = 0.2f))
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Fermer",
                    tint = TextDark.copy(alpha = 0.6f),
                    modifier = Modifier.size(12.dp)
                )
            }
        }
    }
}

// MARK: - Titre

@Composable
private fun TitleSection(
    companions: List<CompanionEntry>,
    showContent: Boolean,
    modifier: Modifier = Modifier
) {
    val emojiScale by animateFloatAsState(
        targetValue = if (showContent) 1f else 0f,
        animationSpec = spring(
            dampingRatio = 0.5f,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "emoji_scale"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp)
    ) {
        Text(
            text = "🎉",
            fontSize = 56.sp,
            modifier = Modifier.scale(emojiScale)
        )
        Text(
            text = "Nouveau${if (companions.size > 1) "x" else ""} compagnon${if (companions.size > 1) "s" else ""} !",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = TextDark,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Niveau ${getLevelForCompanion(companions.firstOrNull()?.name ?: "")} débloqué",
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = TextDark.copy(alpha = 0.6f),
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

// MARK: - Carrousel

@Composable
private fun CompanionsCarousel(
    companions: List<CompanionEntry>,
    showContent: Boolean,
    modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState(pageCount = { companions.size })
    val currentCompanion = companions.getOrNull(pagerState.currentPage)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) { index ->
            CompanionCard(companion = companions[index], showContent = showContent)
        }

        // Nom du compagnon — toujours visible, hors du pager
        Text(
            text = currentCompanion?.name ?: "",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = TextDark,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(top = 12.dp)
        )

        // Indicateurs de page
        if (companions.size > 1) {
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
            ) {
                companions.indices.forEach { index ->
                    val isSelected = index == pagerState.currentPage
                    val dotScale by animateFloatAsState(
                        targetValue = if (isSelected) 1.2f else 1f,
                        animationSpec = spring(dampingRatio = 0.6f),
                        label = "dot_$index"
                    )
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .scale(dotScale)
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) UpNewsOrange else Color.Gray.copy(alpha = 0.3f)
                            )
                    )
                }
        }
        } else {
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

@Composable
private fun CompanionCard(companion: CompanionEntry, showContent: Boolean) {
    val cardScale by animateFloatAsState(
        targetValue = if (showContent) 1f else 0.5f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessMediumLow),
        label = "card_scale"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .scale(cardScale)
    ) {
        Box(contentAlignment = Alignment.Center) {
            // Ombre colorée (image floue décalée)
            AsyncImage(
                model = companion.imageName,
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(160.dp)
                    .blur(20.dp)
                    .padding(bottom = 10.dp)
                    .align(Alignment.BottomCenter)
            )
            // Image principale
            AsyncImage(
                model = companion.imageName,
                contentDescription = companion.name,
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(160.dp)
            )
        }
    }
}

// MARK: - Bouton action

@Composable
private fun ActionButton(
    hasNavigation: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(UpNewsOrange, UpNewsOrange.copy(alpha = 0.85f))
                )
            )
    ) {
        Text(
            text = if (hasNavigation) "Voir mes compagnons" else "Super !",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        if (hasNavigation) {
            Spacer(modifier = Modifier.width(10.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

// MARK: - Helper

private fun getLevelForCompanion(name: String): Int {
    val levelMap = mapOf(
        "Brume" to 2, "Flocon" to 2,
        "Vera" to 3,
        "Jura" to 4,
        "Caramel" to 5, "Écorce" to 5, "Luciole" to 5,
        "Olga" to 6,
        "Luka" to 7,
        "Nina" to 8,
        "Mochi" to 10, "Sève" to 10,
        "Pépite" to 15,
        "Noisette" to 20
    )
    return levelMap[name] ?: 1
}
