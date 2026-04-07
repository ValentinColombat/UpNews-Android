package com.valentincolombat.upnews.ui.companions

import android.Manifest
import android.content.Intent
import android.graphics.BlurMaskFilter
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.offset
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Diamond
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.valentincolombat.upnews.data.model.SubscriptionTier
import com.valentincolombat.upnews.service.NotificationManager
import com.valentincolombat.upnews.ui.components.HorizontalXpBar
import com.valentincolombat.upnews.ui.components.NotificationPermissionView
import com.valentincolombat.upnews.ui.freemium.SubscriptionScreen
import com.valentincolombat.upnews.ui.theme.BorderEquipped
import com.valentincolombat.upnews.ui.theme.CompanionBlue
import com.valentincolombat.upnews.ui.theme.CompanionGreen
import com.valentincolombat.upnews.ui.theme.CompanionViolet
import com.valentincolombat.upnews.ui.theme.UpNewsBackground
import com.valentincolombat.upnews.ui.theme.UpNewsOrange
import kotlinx.coroutines.launch

@Composable
fun CompanionsScreen(vm: CompanionsViewModel = viewModel()) {

    val isLoading             by vm.isLoading.collectAsStateWithLifecycle()
    val companions            by vm.companions.collectAsStateWithLifecycle()
    val currentLevel          by vm.currentLevel.collectAsStateWithLifecycle()
    val currentXp             by vm.currentXp.collectAsStateWithLifecycle()
    val maxXp                 by vm.maxXp.collectAsStateWithLifecycle()
    val bonusClaimed          by vm.notificationBonusClaimed.collectAsStateWithLifecycle()
    val subscriptionTier      by vm.subscriptionTier.collectAsStateWithLifecycle()
    val showPaywall           by vm.showPaywall.collectAsStateWithLifecycle()
    val isPremium             by vm.isPremium.collectAsStateWithLifecycle()
    val lockedCompanionPopup  by vm.lockedCompanionPopup.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val scope   = rememberCoroutineScope()

    var prevTier by remember { mutableStateOf(subscriptionTier) }
    LaunchedEffect(subscriptionTier) {
        if (prevTier == SubscriptionTier.FREE && subscriptionTier == SubscriptionTier.PREMIUM) {
            vm.handleUpgradeToPremium()
        }
        prevTier = subscriptionTier
    }

    var showDeniedDialog      by remember { mutableStateOf(false) }
    var showNotifPermission   by remember { mutableStateOf(false) }

    val notifLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) scope.launch {
            vm.claimNotificationBonus()
            // Utilise l'heure déjà choisie par l'utilisateur si elle existe,
            // sinon 09:00 par défaut (évite d'écraser une heure personnalisée).
            val time = NotificationManager.getSavedTime(context) ?: "09:00"
            NotificationManager.scheduleDailyNotification(context, time)
        }
        else showDeniedDialog = true
    }

    // MARK: - Overlays

    if (showPaywall) {
        SubscriptionScreen(onDismiss = { vm.dismissPaywall() })
        return
    }

    if (showNotifPermission) {
        NotificationPermissionView(
            onAllow = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                    notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                else scope.launch {
                    vm.claimNotificationBonus()
                    val time = NotificationManager.getSavedTime(context) ?: "09:00"
                    NotificationManager.scheduleDailyNotification(context, time)
                }
            },
            onLater   = {},
            onDismiss = { showNotifPermission = false }
        )
    }

    if (showDeniedDialog) {
        AlertDialog(
            onDismissRequest = { showDeniedDialog = false },
            title = { Text("Notifications désactivées") },
            text  = { Text("Active les notifications dans les paramètres Android pour recevoir tes rappels quotidiens et débloquer +80 XP.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeniedDialog = false
                    context.startActivity(
                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.fromParts("package", context.packageName, null))
                    )
                }) { Text("Ouvrir les paramètres") }
            },
            dismissButton = {
                TextButton(onClick = { showDeniedDialog = false }) { Text("Annuler") }
            }
        )
    }

    // Popup compagnon verrouillé
    lockedCompanionPopup?.let { companion ->
        val message = when {
            isPremium ->
                "Ce personnage sera débloqué dès que vous atteindrez le niveau ${companion.unlockLevel}."
            companion.unlockLevel <= 5 ->
                "Ce personnage sera débloqué au niveau ${companion.unlockLevel}."
            else ->
                "Ce personnage nécessite le niveau ${companion.unlockLevel} et un abonnement Premium."
        }
        val showSubscribeButton = !isPremium && companion.unlockLevel > 5

        AlertDialog(
            onDismissRequest = { vm.dismissLockedPopup() },
            title = { Text("Compagnon verrouillé") },
            text  = { Text(message) },
            confirmButton = {
                if (showSubscribeButton) {
                    TextButton(onClick = { vm.dismissLockedPopup(); vm.showPaywall() }) {
                        Text("Voir Premium", color = UpNewsOrange)
                    }
                } else {
                    TextButton(onClick = { vm.dismissLockedPopup() }) {
                        Text("OK", color = UpNewsOrange)
                    }
                }
            },
            dismissButton = if (showSubscribeButton) {
                { TextButton(onClick = { vm.dismissLockedPopup() }) { Text("Fermer", color = UpNewsOrange) } }
            } else null
        )
    }

    // MARK: - Contenu

    Box(modifier = Modifier.fillMaxSize().background(UpNewsBackground)) {
        if (isLoading) {
            CircularProgressIndicator(color = UpNewsOrange, modifier = Modifier.align(Alignment.Center))
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
                    .padding(top = 72.dp, bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Header
                CompanionsHeader(currentLevel = currentLevel)

                // Barre XP horizontale
                XpProgressSection(currentLevel = currentLevel, currentXp = currentXp, maxXp = maxXp)

                // Boost notification
                if (!bonusClaimed) {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        NotificationBoostCard(
                            onClick = { showNotifPermission = true }
                        )
                    }
                }

                // Grille compagnons
                CompanionsGrid(
                    companions   = companions,
                    currentLevel = currentLevel,
                    isPremium    = isPremium,
                    onEquip      = { vm.equipCompanion(it) },
                    onLockedTap  = { vm.onLockedCompanionTapped(it) }
                )
            }
        }
    }
}

// MARK: - Header

@Composable
private fun CompanionsHeader(currentLevel: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(
                    listOf(UpNewsOrange.copy(alpha = 0.9f), UpNewsOrange.copy(alpha = 0.7f))
                )
            )
            .padding(horizontal = 20.dp, vertical = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Compagnons", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.weight(1f))
        Row(
            modifier = Modifier
                .background(Color.White.copy(alpha = 0.25f), RoundedCornerShape(20.dp))
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(Icons.Rounded.Star, null, tint = Color.White, modifier = Modifier.size(12.dp))
            Text("Niv. $currentLevel", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}

// MARK: - XP Progress

@Composable
private fun XpProgressSection(currentLevel: Int, currentXp: Int, maxXp: Int) {
    val progress = if (maxXp > 0) currentXp.toFloat() / maxXp.toFloat() else 0f

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.5f))
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                text = "Progression vers niveau ${currentLevel + 1}",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black
            )
            Text(
                text = "$currentXp / $maxXp XP",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = UpNewsOrange
            )
        }
        HorizontalXpBar(
            progress = progress,
            trackColor = Color.Black.copy(alpha = 0.08f),
            modifier = Modifier.fillMaxWidth().height(10.dp)
        )
    }
}

// MARK: - Notification Boost Card

@Composable
private fun NotificationBoostCard(onClick: () -> Unit) {
    val shadowColor = CompanionViolet.copy(alpha = 0.3f).toArgb()
    Box(
        modifier = Modifier
            .fillMaxWidth(0.88f)
            .drawBehind {
                drawIntoCanvas { canvas ->
                    val paint = Paint()
                    paint.asFrameworkPaint().apply {
                        isAntiAlias = true
                        color = shadowColor
                        maskFilter = BlurMaskFilter(28f, BlurMaskFilter.Blur.NORMAL)
                    }
                    canvas.drawRoundRect(0f, 0f, size.width, size.height, 20.dp.toPx(), 20.dp.toPx(), paint)
                }
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            CompanionViolet,
                            CompanionBlue,
                            CompanionGreen
                        )
                    )
                )
                .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(20.dp))
                .clickable { onClick() }
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .background(Color.White.copy(alpha = 0.18f), RoundedCornerShape(10.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.NotificationsActive, null, tint = Color.White, modifier = Modifier.size(20.dp))
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("Boost notification", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text("Active les notifications", fontSize = 11.sp, color = Color.White.copy(alpha = 0.8f))
            }
            Box(
                modifier = Modifier
                    .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text("+80 XP", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

// MARK: - Grille compagnons

@Composable
private fun CompanionsGrid(
    companions: List<CompanionCharacter>,
    currentLevel: Int,
    isPremium: Boolean,
    onEquip: (CompanionCharacter) -> Unit,
    onLockedTap: (CompanionCharacter) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Personnages disponibles", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black, modifier = Modifier.padding(horizontal = 4.dp))

        companions.chunked(2).forEach { pair ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                pair.forEach { companion ->
                    CompanionCard(
                        companion    = companion,
                        currentLevel = currentLevel,
                        isPremium    = isPremium,
                        onEquip      = onEquip,
                        onLockedTap  = onLockedTap,
                        modifier     = Modifier.weight(1f)
                    )
                }
                if (pair.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

// MARK: - Carte compagnon

@Composable
private fun CompanionCard(
    companion: CompanionCharacter,
    currentLevel: Int,
    isPremium: Boolean,
    onEquip: (CompanionCharacter) -> Unit,
    onLockedTap: (CompanionCharacter) -> Unit,
    modifier: Modifier = Modifier
) {
    val isPremiumLocked = !isPremium && companion.unlockLevel > 5 && !companion.isUnlocked
    val shadowColor = Color.Black.copy(alpha = 0.10f).toArgb()

    val equippedBg = Color(0xFFF0F2F5) // bleu/gris très léger
    val equippedBorder = BorderEquipped

    val giftComposition by rememberLottieComposition(LottieCompositionSpec.Asset("Gift.json"))

    Box(
        modifier = modifier
            .drawBehind {
                drawIntoCanvas { canvas ->
                    val paint = Paint()
                    paint.asFrameworkPaint().apply {
                        isAntiAlias = true
                        color = shadowColor
                        maskFilter = BlurMaskFilter(14f, BlurMaskFilter.Blur.NORMAL)
                    }
                    canvas.drawRoundRect(0f, 0f, size.width, size.height, 20.dp.toPx(), 20.dp.toPx(), paint)
                }
            }
            .clip(RoundedCornerShape(20.dp))
            .background(if (companion.isEquipped) equippedBg else Color.White)
            .then(
                if (companion.isEquipped)
                    Modifier.border(1.5.dp, equippedBorder, RoundedCornerShape(20.dp))
                else Modifier
            )
            .clickable {
                if (companion.isUnlocked && !companion.isEquipped) onEquip(companion)
                else if (!companion.isUnlocked) onLockedTap(companion)
            }
            .padding(vertical = 20.dp, horizontal = 12.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            CompanionImage(drawableId = companion.drawableId, isLocked = !companion.isUnlocked)

            Text(companion.name, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.Black, textAlign = TextAlign.Center)

            CompanionStatusLabel(companion = companion, isPremiumLocked = isPremiumLocked)
        }

        // Lottie Gift — coin haut droite (uniquement si non débloqué)
        if (!companion.isUnlocked) {
            LottieAnimation(
                composition = giftComposition,
                iterations  = LottieConstants.IterateForever,
                modifier    = Modifier
                    .size(36.dp)
                    .align(Alignment.TopEnd)
            )
        }

        // Badge "New"
        if (companion.isUnlocked && companion.unlockLevel == currentLevel) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 6.dp, y = (-8).dp)
                    .background(UpNewsOrange, RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text("New", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

// MARK: - Statut compagnon

@Composable
private fun CompanionStatusLabel(companion: CompanionCharacter, isPremiumLocked: Boolean) {
    when {
        companion.isEquipped -> {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(Icons.Rounded.Check, null, tint = Color.Black, modifier = Modifier.size(12.dp))
                Text("Équipé", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
            }
        }
        companion.isUnlocked -> {
            Text("Niveau ${companion.unlockLevel}", fontSize = 13.sp, color = Color.Gray)
        }
        isPremiumLocked -> {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Rounded.Diamond, null, tint = UpNewsOrange, modifier = Modifier.size(12.dp))
                    Text("Premium", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = UpNewsOrange)
                }
                Text("Niveau ${companion.unlockLevel}", fontSize = 11.sp, color = Color.Gray)
            }
        }
        else -> {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(Icons.Rounded.Lock, null, tint = UpNewsOrange, modifier = Modifier.size(10.dp))
                Text("Niveau ${companion.unlockLevel}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = UpNewsOrange)
            }
        }
    }
}

// MARK: - Image compagnon

@Composable
private fun CompanionImage(@DrawableRes drawableId: Int, isLocked: Boolean) {
    val colorFilter = if (isLocked) {
        // Silhouette noire complète
        ColorFilter.colorMatrix(
            ColorMatrix(floatArrayOf(
                0f, 0f, 0f, 0f, 0f,
                0f, 0f, 0f, 0f, 0f,
                0f, 0f, 0f, 0f, 0f,
                0f, 0f, 0f, 1f, 0f
            ))
        )
    } else null

    Image(
        painter      = painterResource(id = drawableId),
        contentDescription = null,
        contentScale = ContentScale.Fit,
        colorFilter  = colorFilter,
        modifier     = Modifier.size(100.dp)
    )
}

// MARK: - Popup déblocage

data class UnlockedCompanionInfo(val name: String, val drawableId: Int, val level: Int)

// Overlay plein écran glassmorphisme (sans Dialog — confettis en vrai plein écran)
@Composable
fun UnlockCompanionOverlay(
    companions: List<UnlockedCompanionInfo>,
    onDismiss: () -> Unit,
    onNavigateToCompanions: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { companions.size })
    val currentInfo = companions[pagerState.currentPage]

    Box(modifier = Modifier.fillMaxSize()) {

        // Fond sombre
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.75f))
                .clickable { onDismiss() }
        )

        // Card glassmorphisme
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .fillMaxHeight(0.72f)
                .padding(horizontal = 20.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(Color.White.copy(alpha = 0.88f))
                .background(
                    Brush.linearGradient(
                        listOf(
                            Color.White.copy(alpha = 0.60f),
                            Color(0xFFF8F4FF).copy(alpha = 0.50f)
                        )
                    )
                )
                .border(1.5.dp, Color.White.copy(alpha = 0.70f), RoundedCornerShape(28.dp))
                .clickable(enabled = false) {}
        ) {
            // Contenu principal
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
                    .padding(top = 56.dp, bottom = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Emoji fête
                Text("🎉", fontSize = 48.sp, textAlign = TextAlign.Center)

                Spacer(Modifier.height(10.dp))

                // Titre
                Text(
                    text = if (companions.size == 1) "Nouveau compagnon !" else "${companions.size} compagnons débloqués !",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(6.dp))

                // Niveau — réactif à la page courante
                Text(
                    text = "Niveau ${currentInfo.level} débloqué",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = UpNewsOrange,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(12.dp))

                // Pager
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxWidth().weight(1f)
                ) { page ->
                    val info = companions[page]
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Image(
                            painter = painterResource(id = info.drawableId),
                            contentDescription = info.name,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.size(300.dp)
                        )

                        Spacer(Modifier.height(14.dp))

                        Text(
                            text = info.name,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Dots (si plusieurs compagnons)
                if (companions.size > 1) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 10.dp)
                    ) {
                        repeat(companions.size) { index ->
                            Box(
                                modifier = Modifier
                                    .size(if (pagerState.currentPage == index) 10.dp else 6.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (pagerState.currentPage == index) UpNewsOrange
                                        else Color.Black.copy(alpha = 0.2f)
                                    )
                            )
                        }
                    }
                } else {
                    Spacer(Modifier.height(16.dp))
                }

                // CTA
                Button(
                    onClick = { onDismiss(); onNavigateToCompanions() },
                    colors  = ButtonDefaults.buttonColors(containerColor = UpNewsOrange),
                    shape   = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Voir mes compagnons →",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 15.sp
                    )
                }
            }

            // Badge "Nouveau" — coin haut gauche
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(14.dp)
                    .background(UpNewsOrange, RoundedCornerShape(20.dp))
                    .padding(horizontal = 12.dp, vertical = 5.dp)
            ) {
                Text("★ NOUVEAU", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White, letterSpacing = 1.sp)
            }

            // Bouton fermer — coin haut droit
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.08f))
                    .border(1.dp, Color.Black.copy(alpha = 0.10f), CircleShape)
                    .clickable { onDismiss() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.Close, contentDescription = "Fermer", tint = Color.Black.copy(alpha = 0.55f), modifier = Modifier.size(15.dp))
            }
        }

    }
}
