package com.valentincolombat.upnews.ui.category

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.valentincolombat.upnews.data.model.CategoryItem
import com.valentincolombat.upnews.ui.theme.BorderInput
import com.valentincolombat.upnews.ui.theme.CategorySelectionBlue
import com.valentincolombat.upnews.ui.theme.CategorySelectionPurple
import com.valentincolombat.upnews.ui.theme.CategorySelectionViolet
import com.valentincolombat.upnews.ui.theme.SurfaceInput
import com.valentincolombat.upnews.ui.theme.UpNewsBackground
import com.valentincolombat.upnews.ui.theme.UpNewsPrimary

@Composable
fun CategorySelectionScreen(viewModel: CategorySelectionViewModel = viewModel()) {
    val selectedCategories by viewModel.selectedCategories.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(UpNewsBackground)
    ) {
        // MARK: - Scrollable content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 140.dp),
            verticalArrangement = Arrangement.Center
        ) {
            // Header
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(top = 24.dp, bottom = 8.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = "Tes thématiques",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    color = Color.Black.copy(alpha = 0.7f)
                )
                Text(
                    text = "préférées",
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Personnalise ton fil d'actualité",
                    fontSize = 15.sp,
                    textAlign = TextAlign.Center,
                    color = Color.Gray,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            // Category cards — 2 par ligne, hauteur uniforme via IntrinsicSize.Max
            val categories = CategoryItem.allCategories
            categories.chunked(2).forEach { rowItems ->
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Max),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    rowItems.forEach { category ->
                        CategoryCard(
                            category = category,
                            isSelected = selectedCategories.contains(category.id),
                            onTap = { viewModel.toggleCategory(category.id) },
                            modifier = Modifier.weight(1f).fillMaxHeight()
                        )
                    }
                    // Si nombre impair, remplir l'espace vide
                    if (rowItems.size == 1) Spacer(modifier = Modifier.weight(1f))
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Counter + stat block
            Column {
                    // Compteur
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    ) {
                        if (selectedCategories.isEmpty()) {
                            Text(
                                text = "Sélectionne au moins une catégorie",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        } else {
                            val count = selectedCategories.size
                            Text(
                                text = "$count catégorie${if (count > 1) "s" else ""} sélectionnée${if (count > 1) "s" else ""}",
                                fontSize = 12.sp,
                                color = UpNewsPrimary
                            )
                        }
                        if (errorMessage != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = errorMessage!!, fontSize = 12.sp, color = Color.Gray, textAlign = TextAlign.Center)
                        }
                    }

                    // Card stat (toujours visible)
                    Box(
                        modifier = Modifier
                            .padding(vertical = 8.dp)
                            .fillMaxWidth()
                            .shadow(
                                elevation = 10.dp,
                                shape = RoundedCornerShape(24.dp),
                                spotColor = CategorySelectionViolet.copy(alpha = 0.35f),
                                ambientColor = CategorySelectionViolet.copy(alpha = 0.15f)
                            )
                            .clip(RoundedCornerShape(24.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        CategorySelectionPurple,
                                        CategorySelectionBlue
                                    ),
                                    start = Offset(0f, 0f),
                                    end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                                )
                            )
                    ) {
                        // Contenu en premier — détermine la hauteur de la card
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 12.dp, horizontal = 20.dp)
                        ) {
                            // Chiffre + source gauche
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "68%",
                                    fontSize = 38.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "APA, 2024",
                                    fontSize = 9.sp,
                                    color = Color.White.copy(alpha = 0.55f)
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            // Séparateur vertical
                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .height(44.dp)
                                    .background(Color.White.copy(alpha = 0.35f))
                            )

                            Spacer(modifier = Modifier.width(16.dp))

                            // Texte droite
                            Column {
                                Text(
                                    text = "de réduction d'anxiété",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White,
                                    lineHeight = 18.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "chez les personnes qui limitent les actualités négatives",
                                    fontSize = 11.sp,
                                    color = Color.White.copy(alpha = 0.75f),
                                    lineHeight = 15.sp
                                )
                            }
                        }

                        // Cercles décoratifs en overlay — matchParentSize pour ne pas influer sur la hauteur
                        Box(modifier = Modifier.matchParentSize()) {
                            Box(
                                modifier = Modifier
                                    .size(100.dp)
                                    .offset(x = (-20).dp, y = (-20).dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.08f))
                            )
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .align(Alignment.BottomEnd)
                                    .offset(x = 20.dp, y = 20.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.08f))
                            )
                        }
                    }
                }
        }

        // MARK: - Floating button
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                UpNewsBackground.copy(alpha = 0f),
                                UpNewsBackground.copy(alpha = 0.95f),
                                UpNewsBackground
                            )
                        )
                    )
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(UpNewsBackground)
                    .navigationBarsPadding()
                    .padding(bottom = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Button(
                    onClick = { viewModel.confirmSelection() },
                    enabled = selectedCategories.isNotEmpty() && !isLoading,
                    modifier = Modifier
                        .width(140.dp)
                        .height(50.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = UpNewsPrimary,
                        disabledContainerColor = Color.Gray,
                        disabledContentColor = Color.White
                    )
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Text("SUIVANT", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}

// MARK: - Category Card

@Composable
private fun CategoryCard(
    category: CategoryItem,
    isSelected: Boolean,
    onTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.03f else 1.0f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = Spring.StiffnessMedium),
        label = "scale_${category.id}"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .shadow(
                elevation = if (isSelected) 14.dp else 6.dp,
                shape = RoundedCornerShape(20.dp),
                spotColor = if (isSelected) category.color.copy(alpha = 0.45f) else Color.Black.copy(alpha = 0.18f),
                ambientColor = if (isSelected) category.color.copy(alpha = 0.2f) else Color.Black.copy(alpha = 0.08f)
            )
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.verticalGradient(
                    if (isSelected) listOf(
                        Color.White,
                        category.color.copy(alpha = 0.07f)
                    ) else listOf(
                        Color.White,
                        SurfaceInput
                    )
                )
            )
            .then(
                if (isSelected) Modifier.border(1.5.dp, category.color.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
                else Modifier.border(1.dp, BorderInput, RoundedCornerShape(20.dp))
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onTap
            )
            .padding(16.dp)
    ) {
        // Shine subtile en haut de la card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.White.copy(alpha = 0.6f),
                            Color.Transparent
                        )
                    )
                )
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Icon circle avec checkmark
            Box(contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .shadow(
                            elevation = if (isSelected) 6.dp else 2.dp,
                            shape = CircleShape,
                            spotColor = category.color.copy(alpha = if (isSelected) 0.4f else 0.1f)
                        )
                        .clip(CircleShape)
                        .background(
                            if (isSelected) category.color
                            else category.color.copy(alpha = 0.12f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = category.icon,
                        contentDescription = category.name,
                        tint = if (isSelected) Color.White else category.color,
                        modifier = Modifier.size(22.dp)
                    )
                }

                if (isSelected) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier
                            .size(16.dp)
                            .offset(x = 17.dp, y = (-17).dp)
                            .clip(CircleShape)
                            .background(category.color)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = category.name,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isSelected) Color.Black else Color.Black.copy(alpha = 0.85f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = category.description,
                fontSize = 11.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                lineHeight = 15.sp
            )
        }
    }
}
