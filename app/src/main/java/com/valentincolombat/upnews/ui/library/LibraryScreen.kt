package com.valentincolombat.upnews.ui.library

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.Inbox
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.Diamond
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.valentincolombat.upnews.data.model.Article
import com.valentincolombat.upnews.ui.article.ArticleDetailScreen
import com.valentincolombat.upnews.ui.components.CategoryIconBadge
import com.valentincolombat.upnews.ui.freemium.SubscriptionScreen
import com.valentincolombat.upnews.ui.theme.BorderChip
import com.valentincolombat.upnews.ui.theme.BorderMedium
import com.valentincolombat.upnews.ui.theme.IconMuted
import com.valentincolombat.upnews.ui.theme.LibraryBadgeOrange
import com.valentincolombat.upnews.ui.theme.LibraryBadgePink
import com.valentincolombat.upnews.ui.theme.LibraryBadgeYellow
import com.valentincolombat.upnews.ui.theme.SurfaceNeutral
import com.valentincolombat.upnews.ui.theme.TextBrown
import com.valentincolombat.upnews.ui.theme.TextPrimary
import com.valentincolombat.upnews.ui.theme.UpNewsBackground
import com.valentincolombat.upnews.ui.theme.UpNewsBlueMid
import com.valentincolombat.upnews.ui.theme.UpNewsOrange

@Composable
fun LibraryScreen(vm: LibraryViewModel = viewModel()) {

    val articles          by vm.articles.collectAsStateWithLifecycle()
    val isLoading         by vm.isLoading.collectAsStateWithLifecycle()
    val favoriteIds       by vm.favoriteIds.collectAsStateWithLifecycle()
    val readIds           by vm.readIds.collectAsStateWithLifecycle()
    val selectedCategory  by vm.selectedCategory.collectAsStateWithLifecycle()
    val selectedDateRange by vm.selectedDateRange.collectAsStateWithLifecycle()
    val showOnlyFavorites by vm.showOnlyFavorites.collectAsStateWithLifecycle()
    val showPaywall       by vm.showPaywall.collectAsStateWithLifecycle()
    val isPremium         by vm.isPremium.collectAsStateWithLifecycle()

    var selectedArticle by remember { mutableStateOf<Article?>(null) }

    // MARK: - Overlays

    selectedArticle?.let { article ->
        ArticleDetailScreen(article = article, autoPlayAudio = false, onBack = {
            selectedArticle = null
            vm.refreshReadIds()
        })
        return
    }

    if (showPaywall) {
        SubscriptionScreen(onDismiss = { vm.dismissPaywall() })
        return
    }

    // MARK: - Articles filtrés

    val filtered = vm.filteredArticles(articles, favoriteIds, showOnlyFavorites, selectedCategory, selectedDateRange)

    // MARK: - Écran principal

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(UpNewsBackground)
            .statusBarsPadding()
    ) {
        LibraryHeader(articleCount = filtered.size)

        FilterChipRow(
            showOnlyFavorites   = showOnlyFavorites,
            selectedDateRange   = selectedDateRange,
            selectedCategory    = selectedCategory,
            onToggleFavorites   = { vm.toggleShowOnlyFavorites() },
            onDateRangeSelected = { vm.setDateRange(it) },
            onCategorySelected  = { vm.setCategory(it) }
        )

        when {
            isLoading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = UpNewsOrange)
                }
            }
            filtered.isEmpty() -> {
                EmptyStateView(showOnlyFavorites = showOnlyFavorites)
            }
            else -> {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filtered, key = { it.id }) { article ->
                        if (isPremium) {
                            ArticleCard(
                                article       = article,
                                dateLabel     = vm.formatDateFR(article.publishedDate),
                                onTap         = { selectedArticle = article },
                                isFavorite    = favoriteIds.contains(article.id),
                                isRead        = readIds.contains(article.id),
                                onFavoriteTap = { vm.toggleFavorite(article.id) }
                            )
                        } else {
                            ArticleCard(
                                article   = article,
                                dateLabel = vm.formatDateFR(article.publishedDate),
                                onTap     = { vm.showPaywall() },
                                isLocked  = true
                            )
                        }
                    }
                    item { Spacer(Modifier.height(84.dp)) }
                }
            }
        }
    }
}

// MARK: - Header

@Composable
private fun LibraryHeader(articleCount: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp, top = 20.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text       = "Bibliothèque",
            fontSize   = 22.sp,
            fontWeight = FontWeight.Bold,
            color      = Color.Black
        )
        Spacer(Modifier.width(10.dp))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(
                    Brush.horizontalGradient(
                        listOf(LibraryBadgeYellow, LibraryBadgeOrange, LibraryBadgePink)
                    )
                )
                .border(0.5.dp, BorderChip, RoundedCornerShape(50))
                .padding(horizontal = 10.dp, vertical = 3.dp)
        ) {
            Text(
                text       = "$articleCount",
                fontSize   = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color      = TextBrown
            )
        }
    }
}

// MARK: - Filter chip row

@Composable
private fun FilterChipRow(
    showOnlyFavorites: Boolean,
    selectedDateRange: DateRangeFilter,
    selectedCategory: CategoryFilter,
    onToggleFavorites: () -> Unit,
    onDateRangeSelected: (DateRangeFilter) -> Unit,
    onCategorySelected: (CategoryFilter) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(bottom = 12.dp)
    ) {
        item {
            FavoritesChip(active = showOnlyFavorites, onClick = onToggleFavorites)
        }
        item {
            DropdownFilterChip(
                label    = selectedDateRange.label,
                isActive = selectedDateRange != DateRangeFilter.ALL,
                icon     = Icons.Rounded.CalendarMonth
            ) { dismiss ->
                DateRangeFilter.entries.forEach { filter ->
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(filter.label, Modifier.weight(1f))
                                if (selectedDateRange == filter) {
                                    Icon(
                                        imageVector = Icons.Rounded.CheckCircle,
                                        contentDescription = null,
                                        tint = UpNewsBlueMid,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        },
                        onClick = { onDateRangeSelected(filter); dismiss() }
                    )
                }
            }
        }
        item {
            DropdownFilterChip(
                label    = selectedCategory.label,
                isActive = selectedCategory != CategoryFilter.ALL,
                icon     = Icons.Rounded.Tune
            ) { dismiss ->
                CategoryFilter.entries.forEach { filter ->
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(filter.label, Modifier.weight(1f))
                                if (selectedCategory == filter) {
                                    Icon(
                                        imageVector = Icons.Rounded.CheckCircle,
                                        contentDescription = null,
                                        tint = UpNewsOrange,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        },
                        onClick = { onCategorySelected(filter); dismiss() }
                    )
                }
            }
        }
    }
}

@Composable
private fun FavoritesChip(active: Boolean, onClick: () -> Unit) {
    val bgColor by animateColorAsState(
        targetValue = if (active) UpNewsOrange else Color.White,
        label = "favChipBg"
    )
    val contentColor by animateColorAsState(
        targetValue = if (active) Color.White else Color.Black,
        label = "favChipContent"
    )

    Row(
        modifier = Modifier
            .shadow(if (active) 0.dp else 1.dp, RoundedCornerShape(50))
            .clip(RoundedCornerShape(50))
            .background(bgColor)
            .border(
                width = if (active) 0.dp else 1.dp,
                color = BorderMedium,
                shape = RoundedCornerShape(50)
            )
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Icon(
            imageVector = if (active) Icons.Rounded.AutoStories else Icons.AutoMirrored.Rounded.MenuBook,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(12.dp)
        )
        Text(
            text       = "Favoris",
            fontSize   = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color      = contentColor
        )
    }
}

@Composable
private fun DropdownFilterChip(
    label: String,
    isActive: Boolean,
    icon: ImageVector,
    content: @Composable (dismiss: () -> Unit) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    val borderColor = if (isActive) UpNewsBlueMid.copy(alpha = 0.5f) else BorderMedium
    val accentColor = if (isActive) UpNewsBlueMid else Color.Gray

    Box {
        Row(
            modifier = Modifier
                .shadow(1.dp, RoundedCornerShape(50))
                .clip(RoundedCornerShape(50))
                .background(Color.White)
                .border(1.dp, borderColor, RoundedCornerShape(50))
                .clickable { expanded = true }
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Icon(icon, null, modifier = Modifier.size(12.dp), tint = accentColor)
            Text(
                text       = label,
                fontSize   = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color      = if (isActive) UpNewsBlueMid else Color.Black,
                maxLines   = 1,
                overflow   = TextOverflow.Ellipsis
            )
            Icon(Icons.Rounded.KeyboardArrowDown, null, modifier = Modifier.size(12.dp), tint = Color.Gray)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            content { expanded = false }
        }
    }
}

// MARK: - Article Card (premium)

@Composable
private fun ArticleCard(
    article: Article,
    dateLabel: String,
    onTap: () -> Unit,
    isLocked: Boolean = false,
    isFavorite: Boolean = false,
    isRead: Boolean = false,
    onFavoriteTap: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .shadow(1.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .clickable { onTap() }
    ) {
        val base = article.categoryColor
        val topColor = Color(
            red   = base.red   + (1f - base.red)   * 0.35f,
            green = base.green + (1f - base.green) * 0.35f,
            blue  = base.blue  + (1f - base.blue)  * 0.35f,
            alpha = 1f
        )
        Box(
            modifier = Modifier
                .width(8.dp)
                .fillMaxHeight()
                .background(Brush.verticalGradient(listOf(topColor, base)))
        )
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp).weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = if (isRead) Icons.Outlined.Check else Icons.Rounded.Visibility,
                        contentDescription = if (isRead) "Déjà lu" else "Non lu",
                        tint = if (isRead) Color(0xFF6BBF9A) else Color.Gray.copy(alpha = 0.4f),
                        modifier = Modifier.size(13.dp)
                    )
                    Text(text = article.categoryDisplayName, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = Color.Gray)
                }
                Text(text = dateLabel, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = Color.Gray)
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                CategoryIconBadge(article = article)
                Text(
                    text       = article.title,
                    fontSize   = 12.sp,
                    lineHeight = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color      = TextPrimary,
                    maxLines   = 2,
                    overflow   = TextOverflow.Ellipsis,
                    modifier   = Modifier.weight(1f)
                )
                if (isLocked) {
                    Icon(Icons.Rounded.Diamond, contentDescription = "Premium requis", tint = UpNewsOrange, modifier = Modifier.size(20.dp))
                } else {
                    Icon(
                        imageVector        = Icons.Rounded.AutoStories,
                        contentDescription = if (isFavorite) "Retirer des favoris" else "Ajouter aux favoris",
                        tint               = if (isFavorite) UpNewsOrange else Color.Gray,
                        modifier           = Modifier.size(20.dp).clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onFavoriteTap
                        )
                    )
                }
            }
        }
    }
}

// MARK: - Empty State

@Composable
private fun EmptyStateView(showOnlyFavorites: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 100.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(SurfaceNeutral),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (showOnlyFavorites) Icons.AutoMirrored.Rounded.MenuBook else Icons.Rounded.Inbox,
                contentDescription = null,
                tint = IconMuted,
                modifier = Modifier.size(24.dp)
            )
        }
        Text(
            text       = if (showOnlyFavorites) "Aucun article favori" else "Aucun article disponible",
            fontSize   = 15.sp,
            fontWeight = FontWeight.Bold,
            color      = Color.Black
        )
        Text(
            text     = if (showOnlyFavorites)
                "Marque des articles pour les retrouver ici"
            else
                "Ils arrivent bientôt !",
            fontSize = 12.sp,
            color    = Color.Gray
        )
    }
}
