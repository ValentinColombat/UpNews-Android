package com.valentincolombat.upnews.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.valentincolombat.upnews.ui.theme.SurfaceSystem
import com.valentincolombat.upnews.ui.theme.UpNewsGreen
import com.valentincolombat.upnews.ui.theme.UpNewsLightPurple

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountDeletionInfoView(onDismiss: () -> Unit) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "Suppression de compte",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    },
                    actions = {
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Fermer"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
                )
            },
            containerColor = Color.White
        ) { innerPadding ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.widthIn(max = 500.dp)
                ) {
                    // Header
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 20.dp, bottom = 10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color.Red,
                            modifier = Modifier.size(60.dp)
                        )
                        Text(
                            text = "Supprimer votre compte",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Cette action est définitive et irréversible",
                            fontSize = 15.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp))

                    // Données supprimées
                    SectionCard(
                        title = "Données qui seront supprimées",
                        background = SurfaceSystem
                    ) {
                        InfoItem(icon = Icons.Default.Person,   title = "Profil utilisateur",      description = "Votre nom, email et préférences")
                        InfoItem(icon = Icons.Default.LocalFireDepartment, title = "Progression", description = "Votre série, XP et niveau")
                        InfoItem(icon = Icons.AutoMirrored.Filled.MenuBook, title = "Historique de lecture",   description = "Tous vos articles lus et interactions")
                        InfoItem(icon = Icons.Default.Star,     title = "Préférences",             description = "Vos catégories et réglages personnalisés")
                    }

                    // Alternative
                    SectionCard(
                        title = "Vous hésitez ?",
                        background = UpNewsGreen.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = "Si vous souhaitez simplement faire une pause, vous pouvez :",
                            fontSize = 15.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Icon(imageVector = Icons.Default.NotificationsOff, contentDescription = null, tint = UpNewsGreen)
                            Text(text = "Désactiver les notifications", fontSize = 15.sp)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.Logout, contentDescription = null, tint = UpNewsGreen)
                            Text(text = "Simplement vous déconnecter", fontSize = 15.sp)
                        }
                    }

                    // Process
                    SectionCard(
                        title = "Comment supprimer votre compte ?",
                        background = SurfaceSystem
                    ) {
                        ProcessStep(number = 1, text = "Appuyez sur 'Supprimer mon compte' dans Réglages")
                        ProcessStep(number = 2, text = "Confirmez votre décision")
                        ProcessStep(number = 3, text = "Vos données seront supprimées immédiatement")
                    }

                    // Contact
                    val uriHandler = LocalUriHandler.current
                    SectionCard(
                        title = "Besoin d'aide ?",
                        background = UpNewsLightPurple.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = "Si vous rencontrez des problèmes ou avez des questions, contactez-nous à :",
                            fontSize = 15.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(
                            text = "support@upnews.app",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = UpNewsGreen,
                            modifier = Modifier.clickable {
                                uriHandler.openUri("mailto:valentincolombat@gmail.com")
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }
    }
}

// MARK: - Section Card

@Composable
private fun SectionCard(
    title: String,
    background: Color,
    content: @Composable () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(background)
            .padding(16.dp)
    ) {
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        content()
    }
}

// MARK: - Info Item

@Composable
private fun InfoItem(icon: ImageVector, title: String, description: String) {
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.Red,
            modifier = Modifier.size(20.dp).padding(top = 2.dp)
        )
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(text = title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            Text(text = description, fontSize = 14.sp, color = Color.Gray)
        }
    }
}

// MARK: - Process Step

@Composable
private fun ProcessStep(number: Int, text: String) {
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(UpNewsGreen)
        ) {
            Text(
                text = number.toString(),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
        Text(
            text = text,
            fontSize = 15.sp,
            modifier = Modifier.weight(1f)
        )
    }
}
