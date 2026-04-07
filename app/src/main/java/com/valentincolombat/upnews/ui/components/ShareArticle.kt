package com.valentincolombat.upnews.ui.components

import android.content.Intent
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.valentincolombat.upnews.data.model.Article

// MARK: - Bouton icône simple
@Composable
fun ShareArticle(article: Article) {
    val context = LocalContext.current

    IconButton(onClick = { shareArticle(context, article) }) {
        Icon(
            imageVector = Icons.Default.Share,
            contentDescription = "Partager",
            tint = Color.Gray,
        )
    }
}

// MARK: - Variante carte (design cohérent avec l'app)
@Composable
fun ShareArticleEdit(article: Article) {
    val context = LocalContext.current

    Surface(
        onClick = { shareArticle(context, article) },
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 4.dp,
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, Color.Gray.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Share,
                contentDescription = "Partager",
                tint = Color.Black,
            )
            Text(
                text = "Partager",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

// MARK: - Intent partagé
private fun shareArticle(context: android.content.Context, article: Article) {
    val url = article.sourceUrl ?: "https://upnews.app"
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "📰 ${article.title}")
        putExtra(Intent.EXTRA_TEXT, "${article.summary}\n\n$url")
    }
    context.startActivity(Intent.createChooser(intent, null))
}
