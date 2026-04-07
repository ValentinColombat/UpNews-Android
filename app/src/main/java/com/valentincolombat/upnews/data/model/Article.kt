package com.valentincolombat.upnews.data.model

import androidx.compose.ui.graphics.Color
import com.valentincolombat.upnews.ui.theme.CategoryCulture
import com.valentincolombat.upnews.ui.theme.CategoryDefault
import com.valentincolombat.upnews.ui.theme.CategoryEcology
import com.valentincolombat.upnews.ui.theme.CategoryHealth
import com.valentincolombat.upnews.ui.theme.CategoryTech
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.LocalDate

@Serializable
data class Article(
    val id: String,
    @SerialName("published_date") val publishedDate: String,
    val language: String,
    val title: String,
    val summary: String,
    val content: String,
    val category: String,
    @SerialName("image_url") val imageUrl: String? = null,
    @SerialName("source_url") val sourceUrl: String? = null,
    @SerialName("created_at") val createdAt: String,
    @SerialName("audio_url") val audioUrl: String? = null,
    @SerialName("audio_format") val audioFormat: String? = null,
)  {
    val isToday: Boolean
        get() = publishedDate == LocalDate.now().toString()

    // Clé d'icône Material — le composable UI fait le mapping vers ImageVector
    val categoryIcon: String
        get() = when (category.lowercase()) {
            "ecologie", "écologie"  -> "eco"
            "santé", "sante"        -> "health_and_safety"
            "sciences-et-tech"      -> "science"
            "social-et-culture"     -> "theater_comedy"
            else                    -> "newspaper"
        }

    val categoryColor: Color
        get() = when (category.lowercase()) {
            "ecologie", "écologie"  -> CategoryEcology
            "santé", "sante"        -> CategoryHealth
            "sciences-et-tech"      -> CategoryTech
            "social-et-culture"     -> CategoryCulture
            else                    -> CategoryDefault
        }

    val categoryDisplayName: String
        get() = when (category.lowercase()) {
            "ecologie", "écologie"  -> "Écologie"
            "santé", "sante"        -> "Santé"
            "sciences-et-tech"      -> "Sciences & Tech"
            "social-et-culture"     -> "Social & Culture"
            else                    -> category.replaceFirstChar { it.uppercase() }
        }

    val contentPreview: String
        get() {
            val cleaned = content
                .replace("**", "")
                .replace("##", "")
                .replace("#", "")
                .trim()
            if (cleaned.length <= 70) return cleaned
            val preview = cleaned.take(70)
            val lastSpace = preview.lastIndexOf(' ')
            return if (lastSpace != -1) preview.substring(0, lastSpace) + "..." else "$preview..."
        }
}
