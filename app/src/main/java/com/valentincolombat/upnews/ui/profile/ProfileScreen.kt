package com.valentincolombat.upnews.ui.profile

import android.Manifest
import android.app.TimePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ExitToApp
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Pets
import androidx.compose.material.icons.rounded.PrivacyTip
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.CardGiftcard
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material.icons.rounded.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.valentincolombat.upnews.R
import com.valentincolombat.upnews.data.model.CategoryItem
import com.valentincolombat.upnews.service.NotificationManager
import com.valentincolombat.upnews.ui.components.LoadingView
import com.valentincolombat.upnews.ui.components.companionDrawable
import com.valentincolombat.upnews.ui.freemium.PremiumInfoSheet
import com.valentincolombat.upnews.ui.freemium.SubscriptionScreen
import com.valentincolombat.upnews.ui.theme.StatGreen
import com.valentincolombat.upnews.ui.theme.StatGreenDark
import com.valentincolombat.upnews.ui.theme.OrangeLight
import com.valentincolombat.upnews.ui.theme.PremiumDark
import com.valentincolombat.upnews.ui.theme.PremiumDarkLight
import com.valentincolombat.upnews.ui.theme.TextDark
import com.valentincolombat.upnews.ui.theme.StatGreenIcon
import com.valentincolombat.upnews.ui.theme.StatGreenLight
import com.valentincolombat.upnews.ui.theme.StatPurple
import com.valentincolombat.upnews.ui.theme.TextDarkGreen
import com.valentincolombat.upnews.ui.theme.UpNewsBackground
import com.valentincolombat.upnews.ui.theme.UpNewsBlueMid
import com.valentincolombat.upnews.ui.theme.UpNewsGreen
import com.valentincolombat.upnews.ui.theme.UpNewsOrange

@Composable
fun ProfileScreen(viewModel: ProfileViewModel = viewModel()) {

    val context  = LocalContext.current
    val uiState  by viewModel.uiState.collectAsStateWithLifecycle()

    // Migration : si l'utilisateur avait déjà configuré une heure (Supabase) mais qu'aucune
    // alarme n'est enregistrée localement (SharedPrefs vide), on reprogramme silencieusement.
    LaunchedEffect(uiState.isLoading) {
        if (!uiState.isLoading && NotificationManager.getSavedTime(context) == null) {
            uiState.notificationTimeFromServer?.let { time ->
                NotificationManager.scheduleDailyNotification(context, time)
            }
        }
    }

    var showNotifDeniedDialog by remember { mutableStateOf(false) }
    val notifPermLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (!granted) showNotifDeniedDialog = true
        else showTimePicker(context, uiState.notificationTime) { time ->
            viewModel.saveNotificationTime(time)
            NotificationManager.scheduleDailyNotification(context, time)
        }
    }

    val blurRadius by animateDpAsState(
        targetValue = if (uiState.showPremiumInfo || uiState.showSubscription) 20.dp else 0.dp,
        animationSpec = tween(durationMillis = 300),
        label = "blur"
    )

    Box(Modifier.fillMaxSize()) {
    Box(modifier = Modifier.fillMaxSize().background(UpNewsBackground).blur(blurRadius)) {
        if (uiState.isLoading) {
            LoadingView()
        } else {
            Column(modifier = Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.statusBars).verticalScroll(rememberScrollState())) {

                ProfileHeader(
                    displayName = uiState.displayName,
                    userEmail   = uiState.userEmail,
                    companionId = uiState.companionId ?: "",
                    isPremium   = uiState.isPremium,
                    onBadgeTap  = { viewModel.onPremiumBadgeTap() }
                )

                StatsStrip(
                    articlesReadToday     = uiState.articlesReadToday,
                    currentStreak         = uiState.currentStreak,
                    articlesReadThisMonth = uiState.articlesReadThisMonth,
                    xpRemaining           = uiState.maxXp - uiState.currentXp,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 20.dp)
                )

                PremiumSection(
                    isPremium    = uiState.isPremium,
                    onUpgradeTap = { viewModel.onPremiumBadgeTap() },
                    modifier = Modifier.padding(horizontal = 20.dp).padding(bottom = 20.dp)
                )

                SectionTitle("Préférences", Modifier.padding(horizontal = 20.dp, vertical = 4.dp))
                SettingsCard(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
                    SettingsRow(
                        icon     = Icons.Rounded.Star,
                        title    = "Thématiques préférées",
                        value    = viewModel.categoriesPreviewText,
                        iconTint = UpNewsGreen,
                        onClick  = { viewModel.openCategoryPicker() }
                    )
                    SettingsHorizontalDivider()
                    SettingsRow(
                        icon     = Icons.Rounded.Language,
                        title    = "Langue",
                        value    = "Français",
                        iconTint = Color.Gray.copy(alpha = 0.5f),
                        textColor = Color.Gray,
                        onClick  = {}
                    )
                    SettingsHorizontalDivider()
                    SettingsRow(
                        icon     = Icons.Rounded.Notifications,
                        title    = "Notifications",
                        value    = uiState.notificationTime,
                        iconTint = StatPurple,
                        onClick  = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                notifPermLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                showTimePicker(context, uiState.notificationTime) { time ->
                                    viewModel.saveNotificationTime(time)
                                    NotificationManager.scheduleDailyNotification(context, time)
                                }
                            }
                        }
                    )
                }

                SectionTitle("Compte", Modifier.padding(horizontal = 20.dp, vertical = 4.dp))
                SettingsCard(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
                    SettingsRow(
                        icon        = Icons.AutoMirrored.Rounded.ExitToApp,
                        title       = "Se déconnecter",
                        iconTint    = UpNewsOrange,
                        textColor   = UpNewsOrange,
                        showChevron = false,
                        onClick     = { viewModel.showLogoutDialog() }
                    )
                    SettingsHorizontalDivider()
                    SettingsRow(
                        icon     = Icons.Rounded.PrivacyTip,
                        title    = "Politique de confidentialité",
                        iconTint = Color.Gray,
                        onClick  = {
                            com.valentincolombat.upnews.utils.openUrl(context, "https://valentincolombat.github.io/upnews-privacy/")
                        }
                    )
                    SettingsHorizontalDivider()
                    SettingsRow(
                        icon     = Icons.Rounded.Info,
                        title    = "À propos de la suppression",
                        iconTint = Color.Gray,
                        onClick  = { viewModel.showDeleteInfo() }
                    )
                    SettingsHorizontalDivider()
                    // Ligne suppression — inline pour gérer le spinner isDeleting
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = !uiState.isDeleting) { viewModel.showDeleteDialog() }
                            .padding(horizontal = 16.dp, vertical = 13.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier.size(34.dp).clip(RoundedCornerShape(10.dp)).background(Color.Red.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Rounded.Delete, contentDescription = null, tint = Color.Red, modifier = Modifier.size(18.dp))
                        }
                        Text(
                            "Supprimer mon compte",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Red,
                            modifier = Modifier.weight(1f)
                        )
                        if (uiState.isDeleting) CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    }
                }

                Spacer(modifier = Modifier.height(100.dp))
            }
        }

        // MARK: - Dialogs

        ProfileDialogs(
            uiState              = uiState,
            viewModel            = viewModel,
            showNotifDeniedDialog = showNotifDeniedDialog,
            onDismissNotifDenied = { showNotifDeniedDialog = false },
            onOpenAppSettings    = {
                showNotifDeniedDialog = false
                context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", context.packageName, null)
                })
            }
        )
    }
        if (uiState.showSubscription) SubscriptionScreen(onDismiss = { viewModel.hideSubscription() })
        if (uiState.showPremiumInfo) PremiumInfoSheet(onDismiss = { viewModel.hidePremiumInfo() })
    }
}

// MARK: - Profile Dialogs

@Composable
private fun ProfileDialogs(
    uiState: ProfileUiState,
    viewModel: ProfileViewModel,
    showNotifDeniedDialog: Boolean,
    onDismissNotifDenied: () -> Unit,
    onOpenAppSettings: () -> Unit
) {
    if (uiState.showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.hideLogoutDialog() },
            title = { Text("Déconnexion") },
            text  = { Text("Êtes-vous sûr de vouloir vous déconnecter ?") },
            confirmButton = {
                TextButton(onClick = { viewModel.logout() }) { Text("Se déconnecter", color = Color.Red) }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.hideLogoutDialog() }) { Text("Annuler") }
            }
        )
    }

    if (uiState.showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.hideDeleteDialog() },
            title = { Text("Supprimer mon compte") },
            text  = { Text("Cette action est irréversible. Toutes vos données seront définitivement supprimées.") },
            confirmButton = {
                TextButton(onClick = { viewModel.deleteAccount() }) { Text("Supprimer définitivement", color = Color.Red) }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.hideDeleteDialog() }) { Text("Annuler") }
            }
        )
    }

    uiState.deleteError?.let {
        AlertDialog(
            onDismissRequest = { viewModel.clearDeleteError() },
            title = { Text("Erreur") },
            text  = { Text(it) },
            confirmButton = { TextButton(onClick = { viewModel.clearDeleteError() }) { Text("OK") } }
        )
    }

    if (uiState.showDeleteInfo) {
        AlertDialog(
            onDismissRequest = { viewModel.hideDeleteInfo() },
            title = { Text("Suppression de compte") },
            text  = { Text("La suppression entraîne la suppression permanente de toutes vos données : profil, XP, historique et interactions. Vous pourrez vous réinscrire avec le même email.") },
            confirmButton = { TextButton(onClick = { viewModel.hideDeleteInfo() }) { Text("Compris") } }
        )
    }

    if (uiState.showCategoryPicker) {
        CategoryPickerDialog(
            currentCategories = viewModel.preferredCategories.value,
            onDismiss = { viewModel.closeCategoryPicker() },
            onSave    = { viewModel.saveCategories(it) }
        )
    }

    if (showNotifDeniedDialog) {
        AlertDialog(
            onDismissRequest = onDismissNotifDenied,
            title = { Text("Notifications désactivées") },
            text  = { Text("Active les notifications dans les paramètres Android pour recevoir tes rappels quotidiens.") },
            confirmButton = {
                TextButton(onClick = onOpenAppSettings) { Text("Ouvrir les paramètres") }
            },
            dismissButton = { TextButton(onClick = onDismissNotifDenied) { Text("Annuler") } }
        )
    }
}

// MARK: - Profile Header

@Composable
private fun ProfileHeader(
    displayName: String,
    userEmail: String,
    companionId: String,
    isPremium: Boolean,
    onBadgeTap: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(top = 16.dp)
            .shadow(8.dp, RoundedCornerShape(30.dp))
            .clip(RoundedCornerShape(30.dp))
            .background(Brush.linearGradient(listOf(StatGreenDark, StatGreenLight)))
    ) {
        // Cercles décoratifs
        Box(modifier = Modifier.size(150.dp).offset((-30).dp, (-50).dp).clip(CircleShape).background(Color.White.copy(alpha = 0.1f)))
        Box(modifier = Modifier.size(120.dp).align(Alignment.BottomEnd).clip(CircleShape).background(Color.White.copy(alpha = 0.08f)))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Avatar — ombre colorée remplace le double-image blur hack
            Box(
                modifier = Modifier
                    .size(85.dp)
                    .shadow(16.dp, CircleShape, spotColor = StatGreenDark.copy(alpha = 0.5f))
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painterResource(companionDrawable(companionId)),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.size(70.dp).clip(CircleShape)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(displayName, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
            Text(userEmail,   fontSize = 12.sp, color = Color.White.copy(alpha = 0.85f))
            Spacer(modifier = Modifier.height(8.dp))

            // Badge Premium — glassmorphisme
            Row(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.15f))
                    .border(0.5.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                    .clickable(onClick = onBadgeTap)
                    .padding(horizontal = 14.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(if (isPremium) "🪄" else "🌱", fontSize = 13.sp)
                Text(
                    text = if (isPremium) "Grand maître" else "Jeune Padawan",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.85f)
                )
            }
        }
    }
}

// MARK: - Stats Strip

@Composable
private fun StatsStrip(
    articlesReadToday: Int,
    currentStreak: Int,
    articlesReadThisMonth: Int,
    xpRemaining: Int,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        StatChip(icon = Icons.Rounded.AutoStories,         value = "$articlesReadToday",     label = "Aujourd'hui", color = UpNewsGreen,   modifier = Modifier.weight(1f))
        StatChip(icon = Icons.Rounded.LocalFireDepartment, value = "$currentStreak",         label = "Série",       color = UpNewsOrange,  modifier = Modifier.weight(1f))
        StatChip(icon = Icons.Rounded.AutoStories,         value = "$articlesReadThisMonth", label = "Ce mois",     color = StatPurple,    modifier = Modifier.weight(1f))
        StatChip(icon = Icons.Rounded.EmojiEvents,         value = "$xpRemaining",           label = "XP restants", color = UpNewsBlueMid, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun StatChip(icon: ImageVector, value: String, label: String, color: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .shadow(4.dp, RoundedCornerShape(20.dp), spotColor = color.copy(alpha = 0.15f))
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
            .padding(vertical = 14.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(color.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
        }
        Text(value, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = color)
        Text(label, fontSize = 10.sp, fontWeight = FontWeight.Medium, color = Color.Gray, textAlign = TextAlign.Center)
    }
}

// MARK: - Premium Section

@Composable
private fun PremiumSection(isPremium: Boolean, onUpgradeTap: () -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionTitle(if (isPremium) "Mon abonnement" else "Premium")

        if (isPremium) {
            // Card abonné
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White)
                    .border(1.dp, UpNewsOrange.copy(alpha = 0.2f), RoundedCornerShape(24.dp))
                    .clickable(onClick = onUpgradeTap)
                    .padding(18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier.size(48.dp).clip(RoundedCornerShape(16.dp))
                        .background(Brush.linearGradient(listOf(UpNewsOrange, OrangeLight))),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.WorkspacePremium, contentDescription = null, tint = Color.White, modifier = Modifier.size(26.dp))
                }
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(
                        "Premium",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextDark
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(5.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(UpNewsGreen))
                        Text("Actif", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = UpNewsGreen)
                        Text("·  Voir mes avantages", fontSize = 12.sp, color = Color.Gray)
                    }
                }
                Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = Color.Gray.copy(alpha = 0.4f), modifier = Modifier.size(18.dp))
            }


        } else {
            // Card upgrade — fond sombre, fort contraste
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(16.dp, RoundedCornerShape(24.dp), spotColor = PremiumDark.copy(alpha = 0.3f))
                    .clip(RoundedCornerShape(24.dp))
                    .background(Brush.linearGradient(listOf(PremiumDarkLight, PremiumDark)))
                    .clickable(onClick = onUpgradeTap)
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // En-tête
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text("👑", fontSize = 30.sp)
                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(
                            "PASSEZ PREMIUM",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            letterSpacing = 1.5.sp
                        )
                        Text(
                            "Débloquez tous les avantages",
                            fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.55f)
                        )
                    }
                }

                // Features
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    PremiumFeatureRow(icon = Icons.Rounded.Bookmark,   text = "Tous les articles",    dark = true)
                    PremiumFeatureRow(icon = Icons.Rounded.Headphones, text = "Audio haute qualité",  dark = true)
                    PremiumFeatureRow(icon = Icons.Rounded.Pets,       text = "Tous les compagnons",  dark = true)
                }

                // CTA
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(UpNewsOrange)
                        .padding(vertical = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Rounded.CardGiftcard, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Text(
                        "Commencer · 14 jours gratuits",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun PremiumFeatureRow(icon: ImageVector, text: String, dark: Boolean = false) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Icon(icon, contentDescription = null,
            tint = if (dark) Color.White.copy(alpha = 0.7f) else UpNewsBlueMid,
            modifier = Modifier.size(16.dp))
        Text(text, fontSize = 14.sp, fontWeight = FontWeight.Medium,
            color = if (dark) Color.White.copy(alpha = 0.85f) else TextDarkGreen,
            modifier = Modifier.weight(1f))
        Icon(Icons.Rounded.CheckCircle, contentDescription = null,
            tint = if (dark) StatGreenIcon else UpNewsGreen,
            modifier = Modifier.size(15.dp))
    }
}

// MARK: - Settings components

@Composable
private fun SectionTitle(title: String, modifier: Modifier = Modifier) {
    Text(title, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = TextDarkGreen,
        modifier = modifier.padding(horizontal = 4.dp, vertical = 4.dp))
}

@Composable
private fun SettingsCard(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Column(
        modifier = modifier.fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(20.dp), spotColor = Color.Black.copy(alpha = 0.06f))
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
    ) { content() }
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    title: String,
    value: String?   = null,
    iconTint: Color  = UpNewsGreen,
    textColor: Color = TextDarkGreen,
    showChevron: Boolean = true,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier.size(34.dp).clip(RoundedCornerShape(10.dp)).background(iconTint.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(18.dp))
        }
        Text(title, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = textColor, modifier = Modifier.weight(1f))
        value?.let { Text(it, fontSize = 14.sp, color = Color.Black.copy(alpha = 0.4f)) }
        if (showChevron) Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = Color.Gray.copy(alpha = 0.35f), modifier = Modifier.size(16.dp))
    }
}

@Composable
private fun SettingsHorizontalDivider() {
    // start = 16dp padding + 34dp icon + 12dp spacer = 62dp
    HorizontalDivider(modifier = Modifier.padding(start = 62.dp), color = Color.Gray.copy(alpha = 0.15f))
}

// MARK: - Category Picker

@Composable
private fun CategoryPickerDialog(
    currentCategories: List<String>,
    onDismiss: () -> Unit,
    onSave: (List<String>) -> Unit
) {
    var selected by remember { mutableStateOf(currentCategories.toMutableSet()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Mes thématiques", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Sélectionne tes catégories préférées", fontSize = 13.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(8.dp))
                CategoryItem.allCategories.forEach { category ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                selected = if (selected.contains(category.id))
                                    (selected - category.id).toMutableSet()
                                else (selected + category.id).toMutableSet()
                            }
                            .padding(vertical = 6.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier.size(36.dp).clip(CircleShape)
                                .background(category.color.copy(alpha = if (selected.contains(category.id)) 1f else 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(category.icon, contentDescription = null,
                                tint = if (selected.contains(category.id)) Color.White else category.color,
                                modifier = Modifier.size(18.dp))
                        }
                        Text(category.name, fontSize = 15.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                        Checkbox(
                            checked = selected.contains(category.id),
                            onCheckedChange = {
                                selected = if (it) (selected + category.id).toMutableSet()
                                else (selected - category.id).toMutableSet()
                            }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { if (selected.isNotEmpty()) onSave(selected.toList()) }, enabled = selected.isNotEmpty()) {
                Text("Enregistrer", color = UpNewsGreen)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Annuler") } }
    )
}

// MARK: - Helpers

private fun showTimePicker(context: android.content.Context, current: String, onSelected: (String) -> Unit) {
    val parts  = current.split(":").map { it.toIntOrNull() ?: 0 }
    val hour   = parts.getOrElse(0) { 9 }
    val minute = parts.getOrElse(1) { 0 }
    TimePickerDialog(context, { _, h, m -> onSelected("%d:%02d".format(h, m)) }, hour, minute, true).show()
}

