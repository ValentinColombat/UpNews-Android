package com.valentincolombat.upnews.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material.icons.rounded.HeadsetOff
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.Newspaper
import androidx.compose.material.icons.rounded.Pets
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.rounded.Diamond
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material.icons.rounded.WorkspacePremium
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.valentincolombat.upnews.R
import com.valentincolombat.upnews.data.model.Article
import com.valentincolombat.upnews.ui.article.ArticleDetailScreen
import com.valentincolombat.upnews.ui.components.CategoryIconBadge
import com.valentincolombat.upnews.ui.components.CategoryTag
import com.valentincolombat.upnews.ui.components.VerticalProgressBar
import com.valentincolombat.upnews.ui.components.companionDrawable
import com.valentincolombat.upnews.ui.freemium.OGMemberSheet
import com.valentincolombat.upnews.ui.freemium.PremiumBadge
import com.valentincolombat.upnews.ui.freemium.PremiumInfoSheet
import com.valentincolombat.upnews.ui.freemium.SubscriptionScreen
import com.valentincolombat.upnews.ui.theme.BorderDark
import com.valentincolombat.upnews.ui.theme.BorderLight
import com.valentincolombat.upnews.ui.theme.OGBrown
import com.valentincolombat.upnews.ui.theme.OGGold
import com.valentincolombat.upnews.ui.theme.UpNewsBackground
import com.valentincolombat.upnews.ui.theme.UpNewsBlueMid
import com.valentincolombat.upnews.ui.theme.UpNewsOrange

@Composable
fun HomeScreen(
    onNavigateToProfile: () -> Unit = {},
    onNavigateToCompanions: () -> Unit = {},
    resetKey: Int = 0,
    onArticleOpenChanged: (Boolean) -> Unit = {},
    viewModel: HomeViewModel = viewModel()
) {
    val isDataReady       by viewModel.isDataReady.collectAsStateWithLifecycle()
    val mainArticle       by viewModel.mainArticle.collectAsStateWithLifecycle()
    val secondaryArticles by viewModel.secondaryArticles.collectAsStateWithLifecycle()
    val displayName       by viewModel.displayName.collectAsStateWithLifecycle()
    val currentStreak     by viewModel.currentStreak.collectAsStateWithLifecycle()
    val currentLevel      by viewModel.currentLevel.collectAsStateWithLifecycle()
    val xpProgress        by viewModel.xpProgress.collectAsStateWithLifecycle()
    val companionId       by viewModel.selectedCompanionId.collectAsStateWithLifecycle()
    val isOGMember        by viewModel.isOGMember.collectAsStateWithLifecycle()
    val isPremium         by viewModel.isPremium.collectAsStateWithLifecycle()
    val readIds           by viewModel.readIds.collectAsStateWithLifecycle()

    var selectedArticle     by remember { mutableStateOf<Article?>(null) }
    var autoPlayAudio       by remember { mutableStateOf(false) }
    var showPaywall         by remember { mutableStateOf(false) }
    var showPremiumInfo     by remember { mutableStateOf(false) }
    var showOGInfo          by remember { mutableStateOf(false) }

    // Reset depuis la nav bar (re-tap sur Home)
    LaunchedEffect(resetKey) {
        if (resetKey > 0) {
            selectedArticle = null
            onArticleOpenChanged(false)
        }
    }

    // MARK: - Article Detail overlay
    selectedArticle?.let { article ->
        ArticleDetailScreen(
            article = article,
            autoPlayAudio = autoPlayAudio,
            onBack = {
                selectedArticle = null
                onArticleOpenChanged(false)
                viewModel.refreshReadIds()
            }
        )
        return
    }

    val blurRadius by animateDpAsState(
        targetValue = if (showPremiumInfo || showOGInfo || showPaywall) 20.dp else 0.dp,
        animationSpec = tween(durationMillis = 300),
        label = "blur"
    )

    Box(Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(UpNewsBackground)
                .blur(blurRadius)
        ) {
        if (isDataReady && mainArticle == null && secondaryArticles.isEmpty()) {
            EmptyStateView()
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // MARK: - Hero Card
                HeroCard(
                    displayName = displayName,
                    currentStreak = currentStreak,
                    currentLevel = currentLevel,
                    xpProgress = xpProgress,
                    companionId = companionId,
                    isPremium = isPremium,
                    isOGMember = isOGMember,
                    mainArticle = mainArticle,
                    onStreakTap = onNavigateToProfile,
                    onLevelTap = onNavigateToProfile,
                    onCompanionTap = onNavigateToCompanions,
                    onPremiumTap = { showPremiumInfo = true },
                    onOGTap = { showOGInfo = true },
                    onReadArticle = { article ->
                        autoPlayAudio = false
                        selectedArticle = article
                        onArticleOpenChanged(true)
                    }
                )

                Spacer(modifier = Modifier.height(20.dp))

                // MARK: - Main Article Card
                mainArticle?.let { article ->
                    MainArticleCard(
                        article = article,
                        isRead  = readIds.contains(article.id),
                        onRead = { autoPlayAudio = false; selectedArticle = article; onArticleOpenChanged(true) },
                        onAudio = { autoPlayAudio = true; selectedArticle = article; onArticleOpenChanged(true) }
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // MARK: - Secondary Articles
                SecondaryArticlesSection(
                    articles = secondaryArticles,
                    isPremium = isPremium,
                    readIds   = readIds,
                    onArticleTap = { article -> autoPlayAudio = false; selectedArticle = article; onArticleOpenChanged(true) },
                    onPaywallTap = { showPaywall = true }
                )

                Spacer(modifier = Modifier.height(40.dp))
            }
        }
        }
        if (showPaywall) SubscriptionScreen(onDismiss = { showPaywall = false })
        if (showPremiumInfo) PremiumInfoSheet(onDismiss = { showPremiumInfo = false })
        if (showOGInfo) OGMemberSheet(onDismiss = { showOGInfo = false })
    }
}

// MARK: - Fire Badge (isolé pour éviter les recompositions parasites sur LottieAnimation)

@Composable
private fun FireBadge() {
    val fireComposition by rememberLottieComposition(LottieCompositionSpec.Asset("Fire.json"))
    Box(
        modifier = Modifier
            .size(50.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.9f)),
        contentAlignment = Alignment.Center
    ) {
        LottieAnimation(
            composition = fireComposition,
            iterations = LottieConstants.IterateForever,
            modifier = Modifier.size(36.dp)
        )
    }
}

// MARK: - Hero Card

@Composable
private fun HeroCard(
    displayName: String,
    currentStreak: Int,
    currentLevel: Int,
    xpProgress: Float,
    companionId: String,
    isPremium: Boolean,
    isOGMember: Boolean,
    mainArticle: Article?,
    onStreakTap: () -> Unit,
    onLevelTap: () -> Unit,
    onCompanionTap: () -> Unit,
    onPremiumTap: () -> Unit,
    onOGTap: () -> Unit,
    onReadArticle: (Article) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(500.dp)
            .clip(RoundedCornerShape(bottomStart = 0.dp, bottomEnd = 0.dp))
    ) {
        // Background image floutée
        Image(
            painter = painterResource(id = R.drawable.backgroundhome),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .blur(3.dp)
        )


        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars.only(WindowInsetsSides.Top))
                .padding(horizontal = 30.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Header : Streak | Bonjour/Prénom | Niveau
            HeroHeaderRow(
                displayName   = displayName,
                currentStreak = currentStreak,
                currentLevel  = currentLevel,
                onStreakTap   = onStreakTap,
                onLevelTap    = onLevelTap
            )

            Spacer(modifier = Modifier.weight(1f))

            // Centre : badges Premium/OG + Compagnon + XP bar
            HeroCompanionSection(
                companionId    = companionId,
                xpProgress     = xpProgress,
                isPremium      = isPremium,
                isOGMember     = isOGMember,
                onCompanionTap = onCompanionTap,
                onPremiumTap   = onPremiumTap,
                onOGTap        = onOGTap
            )

            Spacer(modifier = Modifier.weight(1f))

            // CTA Button
            mainArticle?.let { article ->
                GlassButton(
                    onClick = { onReadArticle(article) },
                    baseColor = UpNewsOrange,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Découvre ta bonne nouvelle",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(Icons.Rounded.WbSunny, contentDescription = null, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

// MARK: - Hero Header Row

@Composable
private fun HeroHeaderRow(
    displayName: String,
    currentStreak: Int,
    currentLevel: Int,
    onStreakTap: () -> Unit,
    onLevelTap: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .offset(x = (-4).dp)
                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onStreakTap)
        ) {
            FireBadge()
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "$currentStreak jour${if (currentStreak > 1) "s" else ""}",
                fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "Bonjour", fontSize = 40.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
            Text(text = displayName.take(10), fontSize = 28.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .offset(x = 8.dp)
                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onLevelTap)
        ) {
            Box(
                modifier = Modifier.size(50.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.9f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.EmojiEvents, contentDescription = null, tint = UpNewsOrange, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Niv. $currentLevel", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}

// MARK: - Hero Companion Section

@Composable
private fun HeroCompanionSection(
    companionId: String,
    xpProgress: Float,
    isPremium: Boolean,
    isOGMember: Boolean,
    onCompanionTap: () -> Unit,
    onPremiumTap: () -> Unit,
    onOGTap: () -> Unit
) {
    Box(modifier = Modifier.fillMaxWidth().height(280.dp)) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onCompanionTap),
            contentAlignment = Alignment.Center
        ) {
            if (companionId.isNotEmpty()) {
                val drawableRes = companionDrawable(companionId)
                Image(
                    painter = painterResource(id = drawableRes),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.size(280.dp)
                )
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Rounded.Pets, contentDescription = null, tint = Color.White.copy(alpha = 0.5f), modifier = Modifier.size(60.dp))
                    Text("Aucun compagnon", fontSize = 12.sp, color = Color.White.copy(alpha = 0.7f))
                }
            }
        }

        VerticalProgressBar(
            progress = xpProgress,
            modifier = Modifier.width(12.dp).height(220.dp).align(Alignment.CenterEnd).offset(x = (-10).dp, y = 10.dp)
        )

        if (isPremium || isOGMember) {
            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.align(Alignment.TopStart).offset(x = (-8).dp).padding(start = 16.dp, top = 12.dp)
            ) {
                if (isPremium) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(36.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.9f))
                            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onPremiumTap)
                    ) {
                        Icon(Icons.Rounded.Diamond, contentDescription = null, tint = UpNewsOrange, modifier = Modifier.size(20.dp))
                    }
                }
                if (isOGMember) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(36.dp).clip(CircleShape)
                            .background(Brush.linearGradient(listOf(OGBrown, OGGold)))
                            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onOGTap)
                    ) {
                        Text(text = "OG", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}

// MARK: - Main Article Card

@Composable
private fun MainArticleCard(
    article: Article,
    isRead: Boolean = false,
    onRead: () -> Unit,
    onAudio: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .cardBorder(cornerRadius = 16.dp)
            .clickable(onClick = onRead)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CategoryTag(article = article)
            Icon(
                imageVector = if (isRead) Icons.Outlined.Check else Icons.Rounded.Visibility,
                contentDescription = if (isRead) "Déjà lu" else "Non lu",
                tint = if (isRead) Color(0xFF6BBF9A) else Color.Gray.copy(alpha = 0.4f),
                modifier = Modifier.size(18.dp)
            )
        }

        Text(
            text = article.title,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis
        )

        Text(
            text = article.contentPreview,
            fontSize = 13.sp,
            color = Color.Gray,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
            lineHeight = 17.sp
        )

        // Boutons Lire / Audio
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            GlassButton(
                onClick = onRead,
                baseColor = UpNewsOrange,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.AutoMirrored.Rounded.MenuBook, contentDescription = null, modifier = Modifier.size(15.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Lire", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            }

            if (article.audioUrl != null) {
                GlassButton(
                    onClick = onAudio,
                    baseColor = UpNewsBlueMid,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Rounded.Headphones, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Audio", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
            } else {
                GlassButton(
                    onClick = {},
                    baseColor = Color.Gray,
                    enabled = false,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Rounded.HeadsetOff, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Audio", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

// MARK: - Secondary Articles Section

@Composable
private fun SecondaryArticlesSection(
    articles: List<Article>,
    isPremium: Boolean,
    readIds: Set<String>,
    onArticleTap: (Article) -> Unit,
    onPaywallTap: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier.padding(top = 20.dp, start = 30.dp, bottom = 0.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Voir tous les articles du jour",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black
            )
            Text(text = " ...", fontSize = 16.sp, color = Color.Gray)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier.padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            articles.forEach { article ->
                if (isPremium) {
                    ArticleListItemCard(
                        article = article,
                        isRead  = readIds.contains(article.id),
                        onTap   = { onArticleTap(article) }
                    )
                } else {
                    ArticleListItemCard(article = article, isLocked = true, onTap = onPaywallTap)
                }
            }
        }
    }
}

@Composable
private fun ArticleListItemCard(
    article: Article,
    onTap: () -> Unit,
    isLocked: Boolean = false,
    isRead: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(12.dp),
                spotColor = Color.Black.copy(alpha = 0.08f),
                ambientColor = Color.Black.copy(alpha = 0.03f)
            )
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .cardBorder(cornerRadius = 12.dp)
            .clickable(onClick = onTap)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        CategoryIconBadge(article = article)

        Column(modifier = Modifier.weight(1f)) {
            Text(text = article.categoryDisplayName, fontSize = 11.sp, color = Color.Gray)
            Text(
                text = article.title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 17.sp
            )
        }

        if (isLocked) {
            Icon(Icons.Rounded.Diamond, contentDescription = null, tint = UpNewsOrange, modifier = Modifier.size(15.dp))
        } else {
            Icon(
                imageVector = if (isRead) Icons.Outlined.Check else Icons.Rounded.Visibility,
                contentDescription = if (isRead) "Déjà lu" else "Non lu",
                tint = if (isRead) Color(0xFF6BBF9A) else Color.Gray.copy(alpha = 0.4f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

// MARK: - Glass Button

@Composable
private fun GlassButton(
    onClick: () -> Unit,
    baseColor: Color,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit
) {
    val t = 0.38f
    val topColor    = if (enabled) Color(
        red   = baseColor.red   + (1f - baseColor.red)   * t,
        green = baseColor.green + (1f - baseColor.green) * t,
        blue  = baseColor.blue  + (1f - baseColor.blue)  * t,
        alpha = 1f
    ) else Color.Gray.copy(alpha = 0.12f)
    val bottomColor = if (enabled) baseColor else Color.Gray.copy(alpha = 0.06f)
    val borderTop   = Color.White.copy(alpha = if (enabled) 0.55f else 0.15f)

    Box(
        modifier = modifier
            .height(48.dp)
            .shadow(
                elevation = if (enabled) 8.dp else 0.dp,
                shape = RoundedCornerShape(12.dp),
                spotColor = if (enabled) baseColor.copy(alpha = 0.45f) else Color.Transparent,
                ambientColor = if (enabled) baseColor.copy(alpha = 0.15f) else Color.Transparent
            )
            .clip(RoundedCornerShape(12.dp))
            .background(Brush.verticalGradient(listOf(topColor, bottomColor)))
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(listOf(borderTop, Color.White.copy(alpha = 0.0f))),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        CompositionLocalProvider(
            LocalContentColor provides if (enabled) Color.White else Color.Gray.copy(alpha = 0.5f)
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                content()
            }
        }
    }
}

// MARK: - Empty State

@Composable
private fun EmptyStateView() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(UpNewsBackground),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(40.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(UpNewsOrange.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Newspaper,
                    contentDescription = null,
                    tint = UpNewsOrange,
                    modifier = Modifier.size(60.dp)
                )
            }

            Text(
                text = "Nos petits journalistes sont en congé",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = Color.Black
            )

            Text(
                text = "Reviens un peu plus tard !",
                fontSize = 16.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("☕", fontSize = 32.sp)
                Text("🌙", fontSize = 32.sp)
                Text("⛱️", fontSize = 32.sp)
            }
        }
    }
}

// MARK: - Card Border Modifier

private fun Modifier.cardBorder(cornerRadius: Dp): Modifier = drawWithContent {
    drawContent()
    val cr = CornerRadius(cornerRadius.toPx())
    // Bordure fine tout autour — stroke 2dp centré sur le bord → ~1dp visible après clip
    drawRoundRect(
        color = BorderLight,
        cornerRadius = cr,
        style = Stroke(width = 2.dp.toPx())
    )
    // Bordure plus épaisse en bas pour l'effet 3D
    val bottomW = 3.dp.toPx()
    drawLine(
        color = BorderDark,
        start = Offset(0f, size.height - bottomW / 2f),
        end = Offset(size.width, size.height - bottomW / 2f),
        strokeWidth = bottomW
    )
}

