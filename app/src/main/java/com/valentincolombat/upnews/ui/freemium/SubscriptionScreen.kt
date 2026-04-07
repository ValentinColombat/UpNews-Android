package com.valentincolombat.upnews.ui.freemium

import android.app.Activity
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.CardGiftcard
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.Newspaper
import androidx.compose.material.icons.rounded.Pets
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
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
import com.android.billingclient.api.ProductDetails
import com.valentincolombat.upnews.R
import com.valentincolombat.upnews.ui.theme.SurfaceNeutral
import com.valentincolombat.upnews.ui.theme.UpNewsBlueMid
import com.valentincolombat.upnews.ui.theme.UpNewsGreen
import com.valentincolombat.upnews.ui.theme.UpNewsOrange
import kotlinx.coroutines.launch

@Composable
fun SubscriptionScreen(
    onDismiss: () -> Unit,
    vm: SubscriptionViewModel = viewModel()
) {
    val isLoading      by vm.isLoading.collectAsStateWithLifecycle()
    val monthlyProduct by vm.monthlyProduct.collectAsStateWithLifecycle()
    val yearlyProduct  by vm.yearlyProduct.collectAsStateWithLifecycle()
    val errorMessage   by vm.errorMessage.collectAsStateWithLifecycle()
    val purchaseSuccess by vm.purchaseSuccess.collectAsStateWithLifecycle()

    val context = LocalContext.current

    var selectedProductId by remember { mutableStateOf<String?>(null) }
    var showContent       by remember { mutableStateOf(false) }

    // Animation d'apparition (spring, comme iOS)
    LaunchedEffect(Unit) { showContent = true }

    val scale by animateFloatAsState(
        targetValue   = if (showContent) 1f else 0.8f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = Spring.StiffnessMediumLow),
        label         = "subScale"
    )
    val alpha by animateFloatAsState(
        targetValue   = if (showContent) 1f else 0f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = Spring.StiffnessMediumLow),
        label         = "subAlpha"
    )

    // Fermer automatiquement si achat réussi
    LaunchedEffect(purchaseSuccess) {
        if (purchaseSuccess) {
            vm.resetPurchaseSuccess()
            onDismiss()
        }
    }

    // Dialog erreur
    if (errorMessage != null) {
        AlertDialog(
            onDismissRequest = { vm.clearError() },
            title = { Text("Abonnement") },
            text  = { Text(errorMessage ?: "L'achat a échoué. Réessaie dans quelques instants.") },
            confirmButton = {
                TextButton(onClick = { vm.clearError() }) { Text("OK") }
            }
        )
    }

    // MARK: - Fond cliquable (ferme la modal)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable { onDismiss() },
        contentAlignment = Alignment.BottomCenter
    ) {
        // MARK: - Carte principale

        Column(
            modifier = Modifier
                .statusBarsPadding()
                .padding(top = 60.dp)
                .graphicsLayer { scaleX = scale; scaleY = scale; this.alpha = alpha }
                .padding(horizontal = 24.dp)
                .padding(bottom = 60.dp)
                .shadow(
                    elevation = 32.dp,
                    shape = RoundedCornerShape(32.dp),
                    spotColor = Color.Black.copy(alpha = 0.35f),
                    ambientColor = Color.Black.copy(alpha = 0.15f)
                )
                .clip(RoundedCornerShape(32.dp))
                .background(Color.White)
                .border(1.5.dp, Color.Gray.copy(alpha = 0.15f), RoundedCornerShape(32.dp))
                .clickable { /* bloquer propagation */ }
        ) {
            // Bouton fermer
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, end = 20.dp),
                horizontalArrangement = Arrangement.End
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(SurfaceNeutral)
                        .clickable { onDismiss() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Fermer",
                        tint = Color.Black.copy(alpha = 0.5f),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            // Contenu scrollable (hauteur max 640dp)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(680.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Header
                HeaderSection()

                // Bannière essai gratuit
                FreeTrialBanner()

                // Avantages
                FeaturesSection()

                // Produits
                if (isLoading && monthlyProduct == null && yearlyProduct == null) {
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = UpNewsOrange, modifier = Modifier.size(40.dp))
                    }
                } else {
                    ProductsSection(
                        monthlyProduct    = monthlyProduct,
                        yearlyProduct     = yearlyProduct,
                        selectedProductId = selectedProductId,
                        onSelect          = { selectedProductId = it }
                    )
                }

                // Bouton CTA
                CtaButton(
                    isLoading         = isLoading,
                    selectedProductId = selectedProductId,
                    monthlyProduct    = monthlyProduct,
                    yearlyProduct     = yearlyProduct,
                    onClick = {
                        val product = when (selectedProductId) {
                            monthlyProduct?.productId -> monthlyProduct
                            yearlyProduct?.productId  -> yearlyProduct
                            else -> null
                        }
                        product?.let {
                            (context as? Activity)?.let { activity ->
                                vm.purchase(activity, it)
                            }
                        }
                    }
                )

                // Liens légaux
                LegalSection(context = context)
            }
        }
    }
}

// MARK: - Header

@Composable
private fun HeaderSection() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Premium", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.Black)
        Text(
            text      = "Débloque tous les avantages d'UpNews",
            fontSize  = 15.sp,
            color     = Color.Gray,
            textAlign = TextAlign.Center,
            modifier  = Modifier.padding(horizontal = 20.dp)
        )
    }
}

// MARK: - Bannière essai gratuit

@Composable
private fun FreeTrialBanner() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Rounded.CardGiftcard, null, tint = UpNewsOrange, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text(
            "14 jours d'essai gratuit",
            fontSize   = 16.sp,
            fontWeight = FontWeight.Bold,
            color      = UpNewsOrange
        )
    }
}

// MARK: - Avantages

private data class Feature(val icon: ImageVector, val label: String)

@Composable
private fun FeaturesSection() {
    val features = listOf(
        Feature(Icons.Rounded.Newspaper,      "Tous les articles"),
        Feature(Icons.Rounded.Headphones,     "Audio haute qualité"),
        Feature(Icons.AutoMirrored.Rounded.MenuBook,       "Bibliothèque complète"),
        Feature(Icons.Rounded.Pets,           "Tous les compagnons"),
        Feature(Icons.Rounded.Star,           "XP bonus x2")
    )
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        features.forEach { feature ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(2.dp, RoundedCornerShape(12.dp), spotColor = Color.Black.copy(alpha = 0.08f))
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(UpNewsBlueMid.copy(alpha = 0.15f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(feature.icon, null, tint = UpNewsBlueMid, modifier = Modifier.size(18.dp))
                }
                Text(feature.label, fontSize = 13.sp, fontWeight = FontWeight.Normal, color = Color.Black, modifier = Modifier.weight(1f), maxLines = 1)
                Icon(Icons.Rounded.CheckCircle, null, tint = UpNewsBlueMid, modifier = Modifier.size(18.dp))
            }
        }
    }
}

// MARK: - Produits

@Composable
private fun ProductsSection(
    monthlyProduct: ProductDetails?,
    yearlyProduct: ProductDetails?,
    selectedProductId: String?,
    onSelect: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Mensuel
        ProductCard(
            productId    = monthlyProduct?.productId ?: "monthly_placeholder",
            isRecommended = false,
            isSelected   = selectedProductId == (monthlyProduct?.productId ?: "monthly_placeholder"),
            title        = "MENSUEL",
            priceLabel   = monthlyProduct?.subscriptionOfferDetails
                ?.firstOrNull()?.pricingPhases?.pricingPhaseList
                ?.lastOrNull()?.formattedPrice?.let { "$it / mois" } ?: "3,99 € / mois",
            discountLabel = null,
            companionDrawable = R.drawable.mousse,
            onClick      = { onSelect(monthlyProduct?.productId ?: "monthly_placeholder") }
        )
        // Annuel
        ProductCard(
            productId    = yearlyProduct?.productId ?: "yearly_placeholder",
            isRecommended = true,
            isSelected   = selectedProductId == (yearlyProduct?.productId ?: "yearly_placeholder"),
            title        = "ANNUEL",
            priceLabel   = yearlyProduct?.subscriptionOfferDetails
                ?.firstOrNull()?.pricingPhases?.pricingPhaseList
                ?.lastOrNull()?.formattedPrice?.let { "$it / an" } ?: "39,99 € / an",
            discountLabel = "-16%",
            companionDrawable = R.drawable.nina,
            onClick      = { onSelect(yearlyProduct?.productId ?: "yearly_placeholder") }
        )
    }
}

@Composable
private fun ProductCard(
    productId: String,
    isRecommended: Boolean,
    isSelected: Boolean,
    title: String,
    priceLabel: String,
    discountLabel: String?,
    companionDrawable: Int,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue   = if (isSelected) 1.025f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessMediumLow),
        label         = "cardScale"
    )
    val borderColor = if (isSelected) Color.Gray.copy(alpha = 0.5f) else Color.Gray.copy(alpha = 0.15f)
    val borderWidth = if (isSelected) 1.dp else 1.dp
    val elevation   = if (isSelected) 12.dp else 2.dp
    val shadowColor = if (isSelected) UpNewsOrange.copy(alpha = 0.35f) else Color.Black.copy(alpha = 0.06f)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .shadow(elevation, RoundedCornerShape(18.dp), clip = false, spotColor = shadowColor, ambientColor = shadowColor)
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White)
            .border(borderWidth, borderColor, RoundedCornerShape(18.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() }
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Ligne 1 : badges à gauche, checkbox à droite
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier
                        .background(if (isRecommended) UpNewsOrange else UpNewsBlueMid, RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = if (isRecommended) Icons.Rounded.AutoAwesome else Icons.Rounded.Star,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(9.dp)
                    )
                    Text(
                        if (isRecommended) "POPULAIRE" else "LE PLUS SIMPLE",
                        fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White
                    )
                }
                if (discountLabel != null) {
                    Box(
                        modifier = Modifier
                            .background(UpNewsGreen, RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(discountLabel, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
            Spacer(Modifier.weight(1f))
            Icon(
                imageVector = if (isSelected) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
                contentDescription = null,
                tint     = if (isSelected) UpNewsGreen else Color.Gray.copy(alpha = 0.4f),
                modifier = Modifier.size(24.dp)
            )
        }

        // Ligne 2 : image à gauche, titre + prix centrés
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Image(
                painter      = painterResource(id = companionDrawable),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier     = Modifier.size(76.dp)
            )
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(title, fontSize = 20.sp, fontWeight = FontWeight.Black, color = Color.Black)
                Text(priceLabel, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = UpNewsOrange)
            }
        }
    }
}

// MARK: - CTA

@Composable
private fun CtaButton(
    isLoading: Boolean,
    selectedProductId: String?,
    monthlyProduct: ProductDetails?,
    yearlyProduct: ProductDetails?,
    onClick: () -> Unit
) {
    val realIds = listOfNotNull(monthlyProduct?.productId, yearlyProduct?.productId)
    val enabled = selectedProductId != null && selectedProductId in realIds && !isLoading

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(
            onClick  = onClick,
            enabled  = enabled,
            shape    = RoundedCornerShape(16.dp),
            colors   = ButtonDefaults.buttonColors(
                containerColor = UpNewsOrange,
                disabledContainerColor = UpNewsOrange.copy(alpha = 0.5f)
            ),
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text(
                    "Commencer l'essai gratuit",
                    fontSize   = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color      = Color.White
                )
                Spacer(Modifier.width(12.dp))
                Icon(Icons.AutoMirrored.Rounded.ArrowForward, null, tint = Color.White, modifier = Modifier.size(16.dp))
            }
        }

        Text(
            "14 jours gratuits, puis renouvellement automatique.",
            fontSize  = 12.sp,
            color     = Color.Gray,
            textAlign = TextAlign.Center,
            modifier  = Modifier.fillMaxWidth()
        )
    }
}

// MARK: - Section légale

@Composable
private fun LegalSection(context: android.content.Context) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            "Annule à tout moment depuis les paramètres de ton compte",
            fontSize  = 11.sp,
            color     = Color.Gray,
            textAlign = TextAlign.Center,
            modifier  = Modifier.padding(horizontal = 20.dp)
        )
        // CGU
        Text(
            text      = "Conditions d'utilisation",
            fontSize  = 12.sp,
            color     = Color.Gray,
            modifier  = Modifier.clickable {
                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW,
                    android.net.Uri.parse("https://valentincolombat.github.io/upnews-CGU/"))
                context.startActivity(intent)
            }
        )
        // Politique de confidentialité
        Text(
            text      = "Politique de confidentialité",
            fontSize  = 12.sp,
            color     = Color.Gray,
            modifier  = Modifier.clickable {
                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW,
                    android.net.Uri.parse("https://valentincolombat.github.io/upnews-privacy/"))
                context.startActivity(intent)
            }
        )
    }
}
