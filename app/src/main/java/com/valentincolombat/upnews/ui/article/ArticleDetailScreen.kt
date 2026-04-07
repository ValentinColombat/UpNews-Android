package com.valentincolombat.upnews.ui.article

import android.app.Application
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Animatable
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.VolumeOff
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.BookmarkBorder
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Diamond
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import android.graphics.BlurMaskFilter
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import coil.request.ImageRequest

import com.valentincolombat.upnews.R
import com.valentincolombat.upnews.data.model.Article
import com.valentincolombat.upnews.ui.components.CategoryTag
import com.valentincolombat.upnews.ui.components.ConfettiOverlay
import com.valentincolombat.upnews.ui.components.heavyConfettiParties
import com.valentincolombat.upnews.ui.freemium.SubscriptionScreen
import com.valentincolombat.upnews.ui.theme.BorderCard
import com.valentincolombat.upnews.ui.theme.LikeRed
import com.valentincolombat.upnews.ui.theme.OrangeDeep
import com.valentincolombat.upnews.ui.theme.UpNewsBlueMid
import com.valentincolombat.upnews.ui.theme.UpNewsBackground
import com.valentincolombat.upnews.ui.theme.UpNewsOrange
import com.valentincolombat.upnews.ui.theme.XpClaimedGreen
import com.valentincolombat.upnews.ui.theme.XpClaimedLight
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ArticleDetailScreen(
    article: Article,
    autoPlayAudio: Boolean = false,
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val app     = context.applicationContext as Application

    val viewModel: ArticleDetailViewModel = viewModel(
        key     = article.id,
        factory = ArticleDetailViewModel.Factory(app, article)
    )

    val isPlaying       by viewModel.isPlaying.collectAsStateWithLifecycle()
    val currentTimeMs   by viewModel.currentTimeMs.collectAsStateWithLifecycle()
    val durationMs      by viewModel.durationMs.collectAsStateWithLifecycle()
    val playbackSpeed   by viewModel.playbackSpeed.collectAsStateWithLifecycle()
    val isLoadingAudio  by viewModel.isLoadingAudio.collectAsStateWithLifecycle()
    val audioLoadFailed by viewModel.audioLoadFailed.collectAsStateWithLifecycle()
    val audioLimitReached by viewModel.audioLimitReached.collectAsStateWithLifecycle()
    val isLiked         by viewModel.isLiked.collectAsStateWithLifecycle()
    val isFavorite      by viewModel.isFavorite.collectAsStateWithLifecycle()
    val hasClaimedXp    by viewModel.hasClaimedXp.collectAsStateWithLifecycle()
    val showPaywall     by viewModel.showPaywall.collectAsStateWithLifecycle()
    val subscriptionTier by viewModel.isPremiumFlow.collectAsStateWithLifecycle()
    val isPremium = subscriptionTier == com.valentincolombat.upnews.data.model.SubscriptionTier.PREMIUM

    var xpButtonCenter by remember { mutableStateOf(Offset.Zero) }
    var audioPlayerCenter by remember { mutableStateOf(Offset.Zero) }
    var showConfetti by remember { mutableStateOf(false) }
    var confettiOrigin by remember { mutableStateOf(Offset.Zero) }

    LaunchedEffect(Unit) {
        viewModel.confettiEvent.collect { source ->
            confettiOrigin = if (source == ArticleDetailViewModel.ClaimSource.AUDIO)
                audioPlayerCenter else xpButtonCenter
            showConfetti = true
        }
    }

    // Pause audio quand l'écran quitte la composition (changement d'onglet, retour arrière)
    DisposableEffect(Unit) {
        onDispose { viewModel.pause() }
    }

    // Auto-play
    LaunchedEffect(autoPlayAudio) {
        if (autoPlayAudio && !audioLoadFailed) {
            delay(800)
            viewModel.play()
        }
    }

    if (showPaywall) {
        SubscriptionScreen(onDismiss = { viewModel.dismissPaywall() })
        return
    }

    val scrollState = rememberScrollState()

    BackHandler { onBack() }

    // is_read au scroll (seuil ~300px)
    LaunchedEffect(scrollState.value) {
        if (scrollState.value > 300) viewModel.markAsRead()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(UpNewsBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)

        ) {
            // MARK: - Hero
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(top = 16.dp, bottom = 20.dp)
            ) {
                // Bouton retour
                IconButton(onClick = onBack, modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text("<", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.Black.copy(alpha = 0.6f))
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = article.title,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    lineHeight = 34.sp,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))
            }

            // MARK: - Contenu
            Column(
                modifier = Modifier.padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Header : catégorie + like/favori/partage
                HeaderSection(
                    article       = article,
                    isLiked       = isLiked,
                    isFavorite    = isFavorite,
                    onLike        = { viewModel.toggleLike() },
                    onFavorite    = { viewModel.toggleFavorite() },
                    onShare       = {
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, article.sourceUrl ?: article.title)
                        }
                        context.startActivity(Intent.createChooser(intent, "Partager"))
                    }
                )

                // Lecteur audio
                AudioSection(
                    modifier          = Modifier.onGloballyPositioned { coords ->
                        val bounds = coords.boundsInRoot()
                        audioPlayerCenter = Offset(bounds.center.x, bounds.center.y)
                    },
                    article           = article,
                    isPlaying         = isPlaying,
                    isLoading         = isLoadingAudio,
                    loadFailed        = audioLoadFailed,
                    audioLimitReached = audioLimitReached,
                    currentTimeMs     = currentTimeMs,
                    durationMs        = durationMs,
                    playbackSpeed     = playbackSpeed,
                    hasClaimedXp      = hasClaimedXp,
                    isPremium         = isPremium,
                    onTogglePlay      = { viewModel.togglePlayPause(); viewModel.markAsRead() },
                    onSeekBack        = { viewModel.seekBack() },
                    onSeekForward     = { viewModel.seekForward() },
                    onSeek            = { posMs ->
                        if (!isPremium && posMs > viewModel.freeLimit) viewModel.showPaywall()
                        else viewModel.seekTo(posMs)
                    },
                    onSpeedChange     = { viewModel.setPlaybackSpeed(it) },
                    onShowPaywall     = { viewModel.showPaywall() }
                )

                // Contenu + image inline
                ContentSection(article = article)

                // Source
                SourceSection(article = article, context = context)

                // XP
                XpSection(
                    modifier      = Modifier.onGloballyPositioned { coords ->
                        val bounds = coords.boundsInRoot()
                        xpButtonCenter = Offset(bounds.center.x, bounds.center.y)
                    },
                    hasClaimedXp  = hasClaimedXp,
                    isPremium     = isPremium,
                    onClaim       = { viewModel.claimXp() }
                )

                // Retour accueil
                ReturnToHomeButton(onBack = onBack)

                // Boutons d'action
                ActionButtons(
                    isLiked    = isLiked,
                    isFavorite = isFavorite,
                    article    = article,
                    onLike     = { viewModel.toggleLike() },
                    onFavorite = { viewModel.toggleFavorite() },
                    onShare    = {
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, article.sourceUrl ?: article.title)
                        }
                        context.startActivity(Intent.createChooser(intent, "Partager"))
                    }
                )

                Spacer(modifier = Modifier.height(40.dp))
            }
        }

        // Confettis XP — par dessus tout le contenu
        if (showConfetti) {
            ConfettiOverlay(
                parties    = heavyConfettiParties(confettiOrigin.x, confettiOrigin.y),
                modifier   = Modifier.fillMaxSize(),
                onFinished = { showConfetti = false }
            )
        }
    }
}

// MARK: - Header

@Composable
private fun HeaderSection(
    article: Article,
    isLiked: Boolean,
    isFavorite: Boolean,
    onLike: () -> Unit,
    onFavorite: () -> Unit,
    onShare: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CategoryTag(article = article)
        Spacer(modifier = Modifier.weight(1f))
        Row(horizontalArrangement = Arrangement.spacedBy(0.dp)) {
            IconButton(onClick = onLike, modifier = Modifier.size(36.dp)) {
                Icon(
                    imageVector = if (isLiked) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                    contentDescription = null,
                    tint = if (isLiked) LikeRed else Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
            }
            IconButton(onClick = onFavorite, modifier = Modifier.size(36.dp)) {
                Icon(
                    imageVector = if (isFavorite) Icons.Rounded.Bookmark else Icons.Rounded.BookmarkBorder,
                    contentDescription = null,
                    tint = if (isFavorite) UpNewsOrange else Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
            }
            IconButton(onClick = onShare, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Rounded.Share, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp))
            }
        }
    }
}

// MARK: - Audio Section

@Composable
private fun AudioSection(
    modifier: Modifier = Modifier,
    article: Article,
    isPlaying: Boolean,
    isLoading: Boolean,
    loadFailed: Boolean,
    audioLimitReached: Boolean,
    currentTimeMs: Long,
    durationMs: Long,
    playbackSpeed: Float,
    hasClaimedXp: Boolean,
    isPremium: Boolean,
    onTogglePlay: () -> Unit,
    onSeekBack: () -> Unit,
    onSeekForward: () -> Unit,
    onSeek: (Long) -> Unit,
    onSpeedChange: (Float) -> Unit,
    onShowPaywall: () -> Unit
) {
    when {
        article.audioUrl == null || loadFailed -> AudioUnavailable(modifier)
        isLoading -> AudioLoading(modifier)
        else -> AudioPlayer(
            modifier          = modifier,
            article           = article,
            isPlaying         = isPlaying,
            audioLimitReached = audioLimitReached,
            currentTimeMs     = currentTimeMs,
            durationMs        = durationMs,
            playbackSpeed     = playbackSpeed,
            hasClaimedXp      = hasClaimedXp,
            isPremium         = isPremium,
            onTogglePlay      = onTogglePlay,
            onSeekBack        = onSeekBack,
            onSeekForward     = onSeekForward,
            onSeek            = onSeek,
            onSpeedChange     = onSpeedChange,
            onShowPaywall     = onShowPaywall
        )
    }
}

@Composable
private fun AudioUnavailable(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.Gray.copy(alpha = 0.1f))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier.size(48.dp).clip(CircleShape).background(Color.Gray.copy(alpha = 0.3f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.AutoMirrored.Rounded.VolumeOff, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(24.dp))
        }
        Column {
            Text("Audio indisponible", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
            Text("Cet article n'a pas de version audio", fontSize = 12.sp, color = Color.Gray.copy(alpha = 0.7f))
        }
    }
}

@Composable
private fun AudioLoading(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        androidx.compose.material3.CircularProgressIndicator(
            modifier = Modifier.size(32.dp),
            color = UpNewsBlueMid,
            strokeWidth = 2.dp
        )
        Column {
            Text("Chargement de l'audio...", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text("Veuillez patienter", fontSize = 12.sp, color = Color.Gray)
        }
    }
}

@Composable
private fun AudioPlayer(
    modifier: Modifier = Modifier,
    article: Article,
    isPlaying: Boolean,
    audioLimitReached: Boolean,
    currentTimeMs: Long,
    durationMs: Long,
    playbackSpeed: Float,
    hasClaimedXp: Boolean,
    isPremium: Boolean,
    onTogglePlay: () -> Unit,
    onSeekBack: () -> Unit,
    onSeekForward: () -> Unit,
    onSeek: (Long) -> Unit,
    onSpeedChange: (Float) -> Unit,
    onShowPaywall: () -> Unit
) {
    var showSpeedMenu by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp), spotColor = Color.Black.copy(alpha = 0.06f))
            .clip(RoundedCornerShape(16.dp))
    ) {
        Box {
            // Fond : image de l'article floutée
            SubcomposeAsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(article.imageUrl)
                    .fallback(R.drawable.fallback)
                    .error(R.drawable.fallback)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .matchParentSize()
                    .blur(20.dp)
            ) {
                when (painter.state) {
                    is AsyncImagePainter.State.Loading -> {
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .background(UpNewsBackground),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(28.dp),
                                color = UpNewsBlueMid,
                                strokeWidth = 2.dp
                            )
                        }
                    }
                    else -> SubcomposeAsyncImageContent()
                }
            }
            // Overlay semi-transparent pour lisibilité
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color.Black.copy(alpha = 0.35f))
            )
            // Contenu du player
            Column(modifier = Modifier.padding(16.dp)) {
        // Avertissement limite free
        AnimatedVisibility(visible = audioLimitReached) {
            Column {
                AudioLimitWarning(onShowPaywall = onShowPaywall)
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // Ligne play + waveform + vitesse
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            // Bouton -10s
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.15f))
                    .clickable(onClick = onSeekBack),
                contentAlignment = Alignment.Center
            ) {
                Text("-10", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }

            // Bouton play
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(UpNewsBackground)
                    .clickable(onClick = onTogglePlay),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(22.dp)
                )
            }

            // Bouton +10s
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.15f))
                    .clickable(onClick = onSeekForward),
                contentAlignment = Alignment.Center
            ) {
                Text("+10", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }

            WaveformView(isPlaying = isPlaying, modifier = Modifier.weight(1f))

            // Menu vitesse
            Box {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(7.dp))
                        .background(Color.White.copy(alpha = 0.15f))
                        .clickable { showSpeedMenu = true }
                        .padding(horizontal = 8.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = "${if (playbackSpeed % 1f == 0f) playbackSpeed.toInt() else playbackSpeed}x",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Icon(
                        imageVector = Icons.Rounded.ExpandMore,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }

                DropdownMenu(expanded = showSpeedMenu, onDismissRequest = { showSpeedMenu = false }) {
                    listOf(0.7f, 0.85f, 1f, 1.25f, 1.5f, 1.75f, 2f).forEach { speed ->
                        DropdownMenuItem(
                            text = { Text("${if (speed % 1f == 0f) speed.toInt() else speed}x") },
                            onClick = { onSpeedChange(speed); showSpeedMenu = false }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Barre de progression
        Slider(
            value = if (durationMs > 0) currentTimeMs.toFloat() / durationMs.toFloat() else 0f,
            onValueChange = { onSeek((it * durationMs).toLong()) },
            modifier = Modifier.fillMaxWidth().height(20.dp),
            colors = SliderDefaults.colors(
                thumbColor = UpNewsBackground,
                activeTrackColor = UpNewsBackground,
                inactiveTrackColor = Color.White.copy(alpha = 0.25f)
            )
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Timings + badge XP
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(formatMs(currentTimeMs), fontSize = 10.sp, color = Color.White.copy(alpha = 0.7f))

            if (hasClaimedXp) {
                Row(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(UpNewsOrange)
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Rounded.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(10.dp))
                    Text(if (isPremium) "+40 XP" else "+20 XP", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }

            Text(formatMs(durationMs), fontSize = 10.sp, color = Color.White.copy(alpha = 0.7f))
        }
            } // fin Column contenu
        } // fin Box
    } // fin Column externe
}

@Composable
private fun AudioLimitWarning(onShowPaywall: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(14.dp))
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White.copy(alpha = 0.14f))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column {
            Text("Limite atteinte", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text("Passez Premium - 14 jours gratuits", fontSize = 12.sp, color = Color.White.copy(alpha = 0.7f))
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color.White.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                .clip(RoundedCornerShape(10.dp))
                .background(Brush.horizontalGradient(listOf(UpNewsOrange.copy(alpha = 0.9f), OrangeDeep.copy(alpha = 0.9f))))
                .clickable(onClick = onShowPaywall)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Rounded.Diamond, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
            Text("Continuer à lire mon article", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}

// MARK: - Waveform animée

@Composable
private fun WaveformView(isPlaying: Boolean, modifier: Modifier = Modifier) {
    val barCount = 18
    val heights  = remember { List(barCount) { Animatable(4f) } }

    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            heights.forEachIndexed { i, anim ->
                launch {
                    delay(i * 40L)
                    while (true) {
                        val target = (4..22).random().toFloat()
                        anim.animateTo(target, tween((150..400).random()))
                    }
                }
            }
        } else {
            heights.forEach { it.animateTo(4f, tween(300)) }
        }
    }

    Row(
        modifier = modifier.height(32.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        heights.forEach { anim ->
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(anim.value.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(UpNewsBlueMid.copy(alpha = if (isPlaying) 1f else 0.3f))
            )
        }
    }
}

// MARK: - Shimmer placeholder image

@Composable
private fun ImageShimmer() {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1000, easing = LinearEasing), RepeatMode.Restart),
        label = "shimmerProgress"
    )
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFEAEAEA),
                        Color(0xFFF5F5F5),
                        Color(0xFFEAEAEA)
                    ),
                    start = Offset(progress * 800f - 400f, 0f),
                    end   = Offset(progress * 800f, 0f)
                )
            )
    )
}

// MARK: - Contenu avec image inline

@Composable
private fun ContentSection(article: Article) {
    val paragraphs = article.content.split("\n\n")

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        paragraphs.forEachIndexed { index, paragraph ->
            Text(
                text = paragraph,
                fontSize = 14.sp,
                color = Color.Black.copy(alpha = 0.8f),
                lineHeight = 21.sp
            )
            // Image inline après le 2ème paragraphe
            if (index == 1 && paragraphs.size > 2) {
                SubcomposeAsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(article.imageUrl)
                        .fallback(R.drawable.fallback)
                        .error(R.drawable.fallback)
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .shadow(4.dp, RoundedCornerShape(12.dp))
                ) {
                    when (painter.state) {
                        is AsyncImagePainter.State.Loading -> ImageShimmer()
                        else -> SubcomposeAsyncImageContent()
                    }
                }
            }
        }
    }
}

// MARK: - Source

@Composable
private fun SourceSection(article: Article, context: android.content.Context) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(Icons.Rounded.Link, contentDescription = null, tint = UpNewsBlueMid, modifier = Modifier.size(14.dp))
        if (article.sourceUrl != null) {
            Text(
                text = article.sourceUrl.take(50),
                fontSize = 12.sp,
                color = UpNewsBlueMid,
                textDecoration = TextDecoration.Underline,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.clickable {
                    val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse(article.sourceUrl))
                    context.startActivity(intent)
                }
            )
        } else {
            Text("Source non disponible", fontSize = 12.sp, color = Color.Gray)
        }
    }
}

// MARK: - XP Section

@Composable
private fun XpSection(
    modifier: Modifier = Modifier,
    hasClaimedXp: Boolean,
    isPremium: Boolean,
    onClaim: () -> Unit
) {
    val xpAmount = if (isPremium) 40 else 20

    val xpShadowColor = Color.Black.copy(alpha = 0.22f).toArgb()
    Box(
        modifier = modifier
            .fillMaxWidth()
            .drawBehind {
                drawIntoCanvas { canvas ->
                    val paint = Paint()
                    paint.asFrameworkPaint().apply {
                        isAntiAlias = true
                        color = xpShadowColor
                        maskFilter = BlurMaskFilter(28f, BlurMaskFilter.Blur.NORMAL)
                    }
                    canvas.drawRoundRect(0f, 0f, size.width, size.height, 20.dp.toPx(), 20.dp.toPx(), paint)
                }
            }
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.horizontalGradient(
                    if (hasClaimedXp) listOf(XpClaimedGreen, XpClaimedLight)
                    else listOf(UpNewsOrange, OrangeDeep)
                )
            )
            .clickable(enabled = !hasClaimedXp, onClick = onClaim)
            .padding(vertical = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = if (hasClaimedXp) "XP récupérée !" else "+$xpAmount XP gagnés !",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = if (hasClaimedXp) "Bravo !" else "Appuie pour récupérer",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.9f)
            )
        }
    }
}

// MARK: - Retour accueil

@Composable
private fun ReturnToHomeButton(onBack: () -> Unit) {
    val shadowColor = Color.Black.copy(alpha = 0.25f).toArgb()
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                drawIntoCanvas { canvas ->
                    val paint = Paint()
                    paint.asFrameworkPaint().apply {
                        isAntiAlias = true
                        color = shadowColor
                        maskFilter = BlurMaskFilter(28f, BlurMaskFilter.Blur.NORMAL)
                    }
                    canvas.drawRoundRect(
                        left = 0f,
                        top = 0f,
                        right = size.width,
                        bottom = size.height,
                        radiusX = 16.dp.toPx(),
                        radiusY = 16.dp.toPx(),
                        paint = paint
                    )
                }
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
                .border(1.5.dp, BorderCard, RoundedCornerShape(16.dp))
                .clickable(onClick = onBack)
                .padding(vertical = 18.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Rounded.Home, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Text("Retour à l'accueil", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

// MARK: - Boutons d'action

@Composable
private fun ActionButtons(
    isLiked: Boolean,
    isFavorite: Boolean,
    article: Article,
    onLike: () -> Unit,
    onFavorite: () -> Unit,
    onShare: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ActionButton(modifier = Modifier.weight(1f), onClick = onLike) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(
                        imageVector = if (isLiked) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                        contentDescription = null,
                        tint = if (isLiked) LikeRed else Color(0xFF3A3A3A),
                        modifier = Modifier.size(22.dp)
                    )
                    Text("J'aime", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = if (isLiked) LikeRed else Color(0xFF3A3A3A))
                }
            }

            ActionButton(modifier = Modifier.weight(1f), onClick = onFavorite) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Rounded.Bookmark else Icons.Rounded.BookmarkBorder,
                        contentDescription = null,
                        tint = if (isFavorite) UpNewsOrange else Color(0xFF3A3A3A),
                        modifier = Modifier.size(22.dp)
                    )
                    Text("Favoris", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = if (isFavorite) UpNewsOrange else Color(0xFF3A3A3A))
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            // Vidéo (désactivé)
            ActionButton(modifier = Modifier.weight(1f), onClick = {}) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Rounded.Headphones, contentDescription = null, tint = Color.Gray.copy(alpha = 0.4f), modifier = Modifier.size(22.dp))
                    Text("Vidéo", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Gray.copy(alpha = 0.5f))
                }
            }

            ActionButton(modifier = Modifier.weight(1f), onClick = onShare) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Rounded.Share, contentDescription = null, tint = Color(0xFF3A3A3A), modifier = Modifier.size(22.dp))
                    Text("Partager", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF3A3A3A))
                }
            }
        }
    }
}

@Composable
private fun ActionButton(modifier: Modifier = Modifier, onClick: () -> Unit, content: @Composable () -> Unit) {
    val shadowColor = Color.Black.copy(alpha = 0.16f).toArgb()
    Box(
        modifier = modifier
            .drawBehind {
                drawIntoCanvas { canvas ->
                    val paint = Paint()
                    paint.asFrameworkPaint().apply {
                        isAntiAlias = true
                        color = shadowColor
                        maskFilter = BlurMaskFilter(14f, BlurMaskFilter.Blur.NORMAL)
                    }
                    canvas.drawRoundRect(0f, 0f, size.width, size.height, 16.dp.toPx(), 16.dp.toPx(), paint)
                }
            }
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

// MARK: - Helpers

private fun formatMs(ms: Long): String {
    if (ms <= 0) return "0:00"
    val totalSec = ms / 1000
    return "%d:%02d".format(totalSec / 60, totalSec % 60)
}
