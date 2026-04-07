package com.valentincolombat.upnews.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Park
import androidx.compose.material.icons.filled.Science
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class CategoryItem(
    val id: String,
    val name: String,
    val description: String,
    val icon: ImageVector,
    val color: Color
) {
    companion object {
        val allCategories = listOf(
            CategoryItem(
                id = "ecologie",
                name = "Écologie",
                description = "Environnement et nature",
                icon = Icons.Filled.Park,
                color = Color(0xFFAAD98A)
            ),
            CategoryItem(
                id = "santé",
                name = "Santé",
                description = "Bien-être et médecine",
                icon = Icons.Filled.Favorite,
                color = Color(0xFFEFADAD)
            ),
            CategoryItem(
                id = "sciences-et-tech",
                name = "Sciences & Tech",
                description = "Découvertes et innovations",
                icon = Icons.Filled.Science,
                color = Color(0xFF9EC8E0)
            ),
            CategoryItem(
                id = "social-et-culture",
                name = "Social & Culture",
                description = "Solidarité, art et société",
                icon = Icons.Filled.Groups,
                color = Color(0xFFC0B4E0)
            )
        )
    }
}
