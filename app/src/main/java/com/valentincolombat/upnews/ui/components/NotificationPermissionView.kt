package com.valentincolombat.upnews.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.valentincolombat.upnews.ui.theme.NotifBlue
import com.valentincolombat.upnews.ui.theme.NotifTeal
import com.valentincolombat.upnews.ui.theme.NotifViolet

private val cardGradient = Brush.linearGradient(
    colors = listOf(NotifViolet, NotifBlue, NotifTeal)
)
private val shimmerGradient = Brush.linearGradient(
    colors = listOf(
        Color.White.copy(alpha = 0.15f),
        Color.Transparent,
        Color.White.copy(alpha = 0.05f)
    )
)
private val borderGradient = Brush.linearGradient(
    colors = listOf(
        Color.White.copy(alpha = 0.4f),
        Color.White.copy(alpha = 0.1f)
    )
)

@Composable
fun NotificationPermissionView(
    onAllow: () -> Unit,
    onLater: () -> Unit,
    onDismiss: () -> Unit
) {
    val cardShape = RoundedCornerShape(28.dp)

    Dialog(
        onDismissRequest = { /* non-dismissable */ },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .widthIn(max = 360.dp)
                    .padding(horizontal = 20.dp)
                    .shadow(
                        elevation = 20.dp,
                        shape = cardShape,
                        spotColor = Color.Magenta.copy(alpha = 0.4f)
                    )
                    .clip(cardShape)
                    .background(cardGradient)
                    .background(shimmerGradient)
                    .border(width = 1.5.dp, brush = borderGradient, shape = cardShape)
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 40.dp)
            ) {
                // Icône cloche
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .padding(top = 40.dp)
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color.Magenta.copy(alpha = 0.3f),
                                    Color.Blue.copy(alpha = 0.2f)
                                )
                            )
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(44.dp)
                    )
                }

                // Titre
                Text(
                    text = "Reçois ta bonne nouvelle\nchaque jour",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 24.dp, start = 24.dp, end = 24.dp)
                )

                // Description
                Text(
                    text = "Active les notifications et reçois un rappel quotidien pour ne jamais manquer ton article.",
                    fontSize = 16.sp,
                    color = Color.White.copy(alpha = 0.9f),
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp,
                    modifier = Modifier.padding(top = 12.dp, start = 32.dp, end = 32.dp)
                )

                // Bonus XP
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .padding(top = 24.dp, start = 24.dp, end = 24.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White.copy(alpha = 0.15f))
                        .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color.Yellow,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "BONUS",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.85f),
                            letterSpacing = 1.5.sp
                        )
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color.Yellow,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Text(
                        text = "+80 XP offert !",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                }

                // Boutons
                Column(
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.padding(top = 28.dp, start = 24.dp, end = 24.dp)
                ) {
                    // Activer
                    Button(
                        onClick = { onAllow(); onDismiss() },
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                    ) {
                        Text(
                            text = "Activer les notifications",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = NotifViolet
                        )
                    }

                    // Plus tard
                    TextButton(
                        onClick = { onLater(); onDismiss() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Plus tard",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }
    }
}
