package com.valentincolombat.upnews.ui.onboarding

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.valentincolombat.upnews.R
import com.valentincolombat.upnews.ui.theme.Divider
import com.valentincolombat.upnews.ui.theme.OnboardingGradientDark
import com.valentincolombat.upnews.ui.theme.OnboardingGradientGreen
import com.valentincolombat.upnews.ui.theme.UpNewsBackground
import com.valentincolombat.upnews.ui.theme.UpNewsGreen
import com.valentincolombat.upnews.ui.theme.UpNewsPrimary

// MARK: - Companion Data

private data class CompanionData(
    val id: String,
    val name: String,
    val drawableRes: Int
)

private val companions = listOf(
    CompanionData("givre_et_plume", "Givre & Plume", R.drawable.givreetplume),
    CompanionData("cannelle", "Cannelle", R.drawable.cannelle),
    CompanionData("mousse", "Mousse", R.drawable.mousse)
)

// MARK: - Screen

@Composable
fun CompanionSelectionScreen(viewModel: CompanionSelectionViewModel = viewModel()) {
    val selectedId by viewModel.selectedCompanionId.collectAsStateWithLifecycle()
    val displayName by viewModel.displayName.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()

    val trimmed = displayName.trim()
    val isDisplayNameValid = trimmed.length >= 2 && trimmed.length <= 10
    val displayNameError = if (trimmed.isNotEmpty() && trimmed.length < 2)
        "Le pseudo doit contenir au moins 2 caractères" else ""
    val canConfirm = selectedId != null && isDisplayNameValid && !isLoading

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(UpNewsBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // MARK: - Header
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.statusBarsPadding().padding(top = 32.dp, bottom = 8.dp)
            ) {
                Text(
                    text = "Choisis ton premier",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Compagnon",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Il t'accompagnera dans ta lecture quotidienne",
                    fontSize = 13.sp,
                    color = Color.Black.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 32.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // MARK: - Companion Cards
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                companions.forEach { companion ->
                    CompanionCard(
                        companion = companion,
                        isSelected = selectedId == companion.id,
                        onTap = { viewModel.selectCompanion(companion.id) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // MARK: - Display Name
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Ton pseudo",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black
                    )
                    Text(
                        text = "${displayName.length}/10",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black.copy(alpha = 0.4f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = displayName,
                    onValueChange = { viewModel.updateDisplayName(it) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Ex: Alex", color = Color.Gray) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        keyboardType = KeyboardType.Text,
                        autoCorrectEnabled = false
                    ),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (isDisplayNameValid && displayName.isNotEmpty())
                            UpNewsPrimary.copy(alpha = 0.4f) else UpNewsPrimary,
                        unfocusedBorderColor = if (isDisplayNameValid && displayName.isNotEmpty())
                            UpNewsPrimary.copy(alpha = 0.4f) else Divider,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )

                if (displayNameError.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.Error, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                        Text(displayNameError, fontSize = 13.sp, color = Color.Gray)
                    }
                } else if (isDisplayNameValid) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = UpNewsPrimary, modifier = Modifier.size(14.dp))
                        Text("Parfait !", fontSize = 13.sp, color = UpNewsPrimary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // MARK: - Info Box
            Box(
                modifier = Modifier
                    .padding(horizontal = 24.dp, vertical = 8.dp)
                    .fillMaxWidth()
                    .shadow(
                        elevation = 10.dp,
                        shape = RoundedCornerShape(24.dp),
                        spotColor = UpNewsGreen.copy(alpha = 0.35f),
                        ambientColor = UpNewsGreen.copy(alpha = 0.15f)
                    )
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(OnboardingGradientGreen, OnboardingGradientDark),
                            start = Offset(0f, 0f),
                            end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                        )
                    )
            ) {
                // Cercles décoratifs
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .offset(x = (-30).dp, y = (-30).dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.08f))
                )
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .align(Alignment.BottomEnd)
                        .offset(x = 25.dp, y = 25.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.08f))
                )

                Text(
                    text = "Chaque jour, des milliers d'événements positifs se produisent dans le monde. Nous les trouvons pour vous.",
                    fontSize = 13.sp,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp,
                    modifier = Modifier.padding(vertical = 20.dp, horizontal = 20.dp)
                )
            }

            // MARK: - Error
            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Error, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                    Text(errorMessage!!, fontSize = 14.sp, color = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // MARK: - Confirm Button
            Button(
                onClick = { viewModel.confirmSelection() },
                enabled = canConfirm,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = UpNewsPrimary,
                    disabledContainerColor = Color.Gray.copy(alpha = 0.4f),
                    disabledContentColor = Color.White
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text("Commencer l'aventure", fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("→", fontSize = 16.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            }
        }

    }
}

// MARK: - Companion Card

@Composable
private fun CompanionCard(
    companion: CompanionData,
    isSelected: Boolean,
    onTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.05f else 1.0f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = Spring.StiffnessMedium),
        label = "scale_${companion.id}"
    )

    Column(
        modifier = modifier
            .scale(scale)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onTap
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.linearGradient(
                        if (isSelected) listOf(UpNewsPrimary.copy(alpha = 0.15f), UpNewsPrimary.copy(alpha = 0.08f))
                        else listOf(Color.Gray.copy(alpha = 0.08f), Color.Gray.copy(alpha = 0.05f))
                    )
                )
                .then(
                    if (isSelected) Modifier.border(3.dp, UpNewsPrimary, RoundedCornerShape(20.dp))
                    else Modifier
                ),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = companion.drawableRes),
                contentDescription = companion.name,
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(80.dp, 120.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = companion.name,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
            color = if (isSelected) Color.Black else Color.Black.copy(alpha = 0.6f),
            textAlign = TextAlign.Center
        )
    }
}
