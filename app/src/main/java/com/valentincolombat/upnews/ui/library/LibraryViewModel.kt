package com.valentincolombat.upnews.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.valentincolombat.upnews.data.model.Article
import com.valentincolombat.upnews.data.model.SubscriptionTier
import com.valentincolombat.upnews.data.repository.ArticleRepository
import com.valentincolombat.upnews.data.repository.InteractionRepository
import com.valentincolombat.upnews.data.repository.UserRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

enum class CategoryFilter(val label: String, val categoryKey: String?) {
    ALL("Tous", null),
    ECOLOGY("Écologie", "ecologie"),
    HEALTH("Santé", "santé"),
    SCIENCE_TECH("Sciences & Tech", "sciences-et-tech"),
    SOCIAL_CULTURE("Social & Culture", "social-et-culture")
}

enum class DateRangeFilter(val label: String) {
    ALL("Tous"),
    TODAY("Aujourd'hui"),
    LAST_7_DAYS("7 derniers jours"),
    THIS_MONTH("Ce mois")
}

class LibraryViewModel : ViewModel() {

    private val articleRepo     = ArticleRepository.shared
    private val interactionRepo = InteractionRepository.shared
    private val userRepo        = UserRepository.shared

    val isPremium = userRepo.subscriptionTier
        .map { it == SubscriptionTier.PREMIUM }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), userRepo.isPremium)

    // MARK: - État principal

    private val _articles   = MutableStateFlow<List<Article>>(emptyList())
    val articles: StateFlow<List<Article>> = _articles.asStateFlow()

    private val _isLoading  = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // MARK: - Filtres

    private val _selectedCategory   = MutableStateFlow(CategoryFilter.ALL)
    val selectedCategory: StateFlow<CategoryFilter> = _selectedCategory.asStateFlow()

    private val _selectedDateRange   = MutableStateFlow(DateRangeFilter.ALL)
    val selectedDateRange: StateFlow<DateRangeFilter> = _selectedDateRange.asStateFlow()

    private val _showOnlyFavorites  = MutableStateFlow(false)
    val showOnlyFavorites: StateFlow<Boolean> = _showOnlyFavorites.asStateFlow()

    // MARK: - Favoris

    private val _favoriteIds = MutableStateFlow<Set<String>>(emptySet())
    val favoriteIds: StateFlow<Set<String>> = _favoriteIds.asStateFlow()

    // MARK: - Articles lus

    private val _readIds = MutableStateFlow<Set<String>>(emptySet())
    val readIds: StateFlow<Set<String>> = _readIds.asStateFlow()

    // MARK: - Paywall

    private val _showPaywall = MutableStateFlow(false)
    val showPaywall: StateFlow<Boolean> = _showPaywall.asStateFlow()

    // MARK: - Tâche de chargement

    private var loadJob: Job? = null

    // MARK: - Init

    init { load() }

    // MARK: - Chargement

    fun load() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _isLoading.value = true
            runCatching {
                _articles.value    = articleRepo.fetchAllArticles()
                _favoriteIds.value = interactionRepo.loadFavoriteIds()
                _readIds.value     = interactionRepo.loadReadIds()
            }
            _isLoading.value = false
        }
    }

    // MARK: - Refresh articles lus

    fun refreshReadIds() {
        viewModelScope.launch {
            runCatching { _readIds.value = interactionRepo.loadReadIds() }
        }
    }

    fun refreshFavoriteIds() {
        viewModelScope.launch {
            runCatching { _favoriteIds.value = interactionRepo.loadFavoriteIds() }
        }
    }

    // MARK: - Toggle favori

    fun toggleFavorite(articleId: String) {
        val wasFav = _favoriteIds.value.contains(articleId)
        // Mise à jour optimiste
        _favoriteIds.value = if (wasFav) _favoriteIds.value - articleId else _favoriteIds.value + articleId

        viewModelScope.launch {
            runCatching {
                interactionRepo.toggleFavorite(articleId, !wasFav)
            }.onFailure {
                // Rollback
                _favoriteIds.value = if (wasFav) _favoriteIds.value + articleId else _favoriteIds.value - articleId
            }
        }
    }

    // MARK: - Filtres

    fun setCategory(filter: CategoryFilter)   { _selectedCategory.value  = filter }
    fun setDateRange(filter: DateRangeFilter) { _selectedDateRange.value = filter }
    fun toggleShowOnlyFavorites()             { _showOnlyFavorites.value = !_showOnlyFavorites.value }

    // MARK: - Paywall

    fun showPaywall()    { _showPaywall.value = true  }
    fun dismissPaywall() { _showPaywall.value = false }

    // MARK: - Articles filtrés (logique côté ViewModel)

    fun filteredArticles(
        articles: List<Article>,
        favoriteIds: Set<String>,
        showOnlyFavorites: Boolean,
        category: CategoryFilter,
        dateRange: DateRangeFilter
    ): List<Article> {
        val registrationDate = userRepo.registrationDate.value
        var result = if (registrationDate != null) {
            articles.filter { (parseDate(it.publishedDate) ?: LocalDate.MIN) >= registrationDate }
        } else {
            articles
        }

        if (showOnlyFavorites) {
            result = result.filter { favoriteIds.contains(it.id) }
        }

        if (category.categoryKey != null) {
            result = result.filter { it.category == category.categoryKey }
        }

        val today = LocalDate.now()
        result = when (dateRange) {
            DateRangeFilter.TODAY       -> result.filter { parseDate(it.publishedDate) == today }
            DateRangeFilter.LAST_7_DAYS -> result.filter {
                val d = parseDate(it.publishedDate) ?: return@filter false
                ChronoUnit.DAYS.between(d, today) <= 7
            }
            DateRangeFilter.THIS_MONTH  -> result.filter {
                val d = parseDate(it.publishedDate) ?: return@filter false
                d.year == today.year && d.month == today.month
            }
            DateRangeFilter.ALL         -> result
        }

        return result.sortedByDescending { it.publishedDate }
    }

    // MARK: - Helpers

    fun parseDate(dateString: String): LocalDate? = runCatching {
        LocalDate.parse(dateString, DateTimeFormatter.ISO_LOCAL_DATE)
    }.getOrNull()

    fun formatDateFR(dateString: String): String {
        val date  = parseDate(dateString) ?: return dateString
        val today = LocalDate.now()
        return when {
            date == today                              -> "Aujourd'hui"
            date == today.minusDays(1)                 -> "Hier"
            ChronoUnit.DAYS.between(date, today) <= 7 -> {
                val days = ChronoUnit.DAYS.between(date, today)
                "Il y a $days jour${if (days > 1) "s" else ""}"
            }
            else -> {
                val months = listOf("jan", "fév", "mars", "avr", "mai", "juin",
                    "juil", "août", "sep", "oct", "nov", "déc")
                "${date.dayOfMonth} ${months[date.monthValue - 1]}"
            }
        }
    }
}
