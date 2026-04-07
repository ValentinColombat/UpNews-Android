package com.valentincolombat.upnews.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.valentincolombat.upnews.ui.theme.NotifBlue
import com.valentincolombat.upnews.ui.theme.NotifTeal
import com.valentincolombat.upnews.ui.theme.NotifViolet
import com.valentincolombat.upnews.ui.theme.NotifVioletDark
import com.valentincolombat.upnews.ui.theme.NotifVioletGlow

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
        Color.White.copy(alpha = 0.3f),
        Color.White.copy(alpha = 0.1f)
    )
)

@Composable
fun NotificationBoostCard(onActivate: () -> Unit) {
    val shape = RoundedCornerShape(14.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 12.dp, shape = shape, spotColor = NotifVioletGlow.copy(alpha = 0.4f))
            .clip(shape)
            .background(cardGradient)
            .background(shimmerGradient)
            .border(width = 1.5.dp, brush = borderGradient, shape = shape)
            .clickable(onClick = onActivate)
            .padding(horizontal = 12.dp, vertical = 12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Icône cloche
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                NotifVioletDark.copy(alpha = 0.8f),
                                NotifBlue.copy(alpha = 0.6f)
                            )
                        )
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.NotificationsActive,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Texte
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = Color.Yellow,
                        modifier = Modifier.size(11.dp)
                    )
                    Text(
                        text = "+80 XP",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }
                Text(
                    text = "Active les notifications",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Flèche
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}
