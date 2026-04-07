package com.valentincolombat.upnews.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Eco
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Newspaper
import androidx.compose.material.icons.rounded.Science
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.valentincolombat.upnews.data.model.Article
import com.valentincolombat.upnews.ui.theme.BorderTag
import com.valentincolombat.upnews.ui.theme.BorderTagLight

@Composable
fun CategoryTag(article: Article) {
    val base = article.categoryColor
    val t = 0.35f
    val topColor = Color(
        red   = base.red   + (1f - base.red)   * t,
        green = base.green + (1f - base.green) * t,
        blue  = base.blue  + (1f - base.blue)  * t,
        alpha = 1f
    )
    val icon = when (article.category.lowercase()) {
        "ecologie", "écologie" -> Icons.Rounded.Eco
        "santé", "sante"       -> Icons.Rounded.Favorite
        "sciences-et-tech"     -> Icons.Rounded.Science
        "social-et-culture"    -> Icons.Rounded.Groups
        else                   -> Icons.Rounded.Newspaper
    }

    Row(
        modifier = Modifier
            .shadow(4.dp, RoundedCornerShape(10.dp), spotColor = base.copy(alpha = 0.4f), ambientColor = base.copy(alpha = 0.15f))
            .border(0.5.dp, BorderTag.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
            .clip(RoundedCornerShape(10.dp))
            .background(Brush.verticalGradient(listOf(topColor, base)))
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.55f), Color.White.copy(alpha = 0f))),
                shape = RoundedCornerShape(10.dp)
            )
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(10.dp)
        )
        Text(
            text = article.categoryDisplayName,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White
        )
    }
}

@Composable
fun CategoryIconBadge(article: Article) {
    val base = article.categoryColor
    val t = 0.35f
    val topColor = Color(
        red   = base.red   + (1f - base.red)   * t,
        green = base.green + (1f - base.green) * t,
        blue  = base.blue  + (1f - base.blue)  * t,
        alpha = 1f
    )
    val icon = when (article.category.lowercase()) {
        "ecologie", "écologie" -> Icons.Rounded.Eco
        "santé", "sante"       -> Icons.Rounded.Favorite
        "sciences-et-tech"     -> Icons.Rounded.Science
        "social-et-culture"    -> Icons.Rounded.Groups
        else                   -> Icons.Rounded.Newspaper
    }
    Box(
        modifier = Modifier
            .shadow(3.dp, RoundedCornerShape(10.dp), spotColor = base.copy(alpha = 0.35f), ambientColor = base.copy(alpha = 0.12f))
            .border(0.5.dp, BorderTagLight, RoundedCornerShape(10.dp))
            .clip(RoundedCornerShape(10.dp))
            .background(Brush.verticalGradient(listOf(topColor, base)))
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.5f), Color.White.copy(alpha = 0f))),
                shape = RoundedCornerShape(10.dp)
            )
            .padding(10.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(contentAlignment = Alignment.Center) {
            // Ombre portée : icône décalée, floutée
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.Black.copy(alpha = 0.3f),
                modifier = Modifier.size(20.dp).offset(x = 0.5.dp, y = 1.5.dp).blur(3.dp)
            )
            // Icône principale blanche
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
