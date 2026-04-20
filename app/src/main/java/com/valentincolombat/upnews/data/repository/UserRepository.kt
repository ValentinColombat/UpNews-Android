package com.valentincolombat.upnews.data.repository

import com.valentincolombat.upnews.data.model.Article
import com.valentincolombat.upnews.data.model.CompanionData
import com.valentincolombat.upnews.data.model.SubscriptionTier
import com.valentincolombat.upnews.data.remote.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Count
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

class UserRepository private constructor() {

    // MARK: - Singleton

    companion object {
        val shared = UserRepository()
    }

    // MARK: - State (équivalent @Published iOS)

    private val _displayName = MutableStateFlow("")
    val displayName: StateFlow<String> = _displayName.asStateFlow()

    private val _currentStreak = MutableStateFlow(0)
    val currentStreak: StateFlow<Int> = _currentStreak.asStateFlow()

    private val _selectedCompanionId = MutableStateFlow("")
    val selectedCompanionId: StateFlow<String> = _selectedCompanionId.asStateFlow()

    private val _currentXp = MutableStateFlow(0)
    val currentXp: StateFlow<Int> = _currentXp.asStateFlow()

    private val _maxXp = MutableStateFlow(100)
    val maxXp: StateFlow<Int> = _maxXp.asStateFlow()

    private val _currentLevel = MutableStateFlow(1)
    val currentLevel: StateFlow<Int> = _currentLevel.asStateFlow()

    private val _articlesReadToday = MutableStateFlow(0)
    val articlesReadToday: StateFlow<Int> = _articlesReadToday.asStateFlow()

    private val _articlesReadThisMonth = MutableStateFlow(0)
    val articlesReadThisMonth: StateFlow<Int> = _articlesReadThisMonth.asStateFlow()

    private val _preferredCategories = MutableStateFlow<List<String>>(emptyList())
    val preferredCategories: StateFlow<List<String>> = _preferredCategories.asStateFlow()

    private val _notificationTime = MutableStateFlow<String?>(null)
    val notificationTime: StateFlow<String?> = _notificationTime.asStateFlow()

    private val _notificationBonusClaimed = MutableStateFlow(false)
    val notificationBonusClaimed: StateFlow<Boolean> = _notificationBonusClaimed.asStateFlow()

    private val _subscriptionTier = MutableStateFlow(SubscriptionTier.FREE)
    val subscriptionTier: StateFlow<SubscriptionTier> = _subscriptionTier.asStateFlow()

    private val _isOGMember = MutableStateFlow(false)
    val isOGMember: StateFlow<Boolean> = _isOGMember.asStateFlow()

    private val _articles = MutableStateFlow<List<Article>>(emptyList())
    val articles: StateFlow<List<Article>> = _articles.asStateFlow()

    private val _mainArticle = MutableStateFlow<Article?>(null)
    val mainArticle: StateFlow<Article?> = _mainArticle.asStateFlow()

    private val _secondaryArticles = MutableStateFlow<List<Article>>(emptyList())
    val secondaryArticles: StateFlow<List<Article>> = _secondaryArticles.asStateFlow()

    private val _selectedMainArticleId = MutableStateFlow<String?>(null)
    val selectedMainArticleId: StateFlow<String?> = _selectedMainArticleId.asStateFlow()

    private val _selectedMainArticleDate = MutableStateFlow<String?>(null)
    val selectedMainArticleDate: StateFlow<String?> = _selectedMainArticleDate.asStateFlow()

    private val _registrationDate = MutableStateFlow<LocalDate?>(null)
    val registrationDate: StateFlow<LocalDate?> = _registrationDate.asStateFlow()

    /** true dès que loadArticlesAndStats() a terminé au moins une fois avec succès.
     *  Passe à false au début de chaque chargement → permet à l'UI d'afficher
     *  des skeletons pendant que les articles arrivent en arrière-plan. */
    private val _isDataReady = MutableStateFlow(false)
    val isDataReady: StateFlow<Boolean> = _isDataReady.asStateFlow()

    // MARK: - Private fields

    private var currentUserId: String? = null
    private var isLoadingData = false
    private val client = SupabaseClient.client
    private val articleRepository = ArticleRepository.shared
    private val streakRepository = StreakRepository.shared

    // MARK: - Private DB Models

    @Serializable private data class UserCompanion(
        val selected_companion_id: String? = null,
        val display_name: String?          = null
    )

    @Serializable private data class UserProfile(
        val display_name: String                       = "",
        val selected_companion_id: String?             = null,
        val current_xp: Int                            = 0,
        val max_xp: Int                                = 100,
        val current_level: Int                         = 1,
        val current_streak: Int                        = 0,
        val preferred_categories: List<String>?        = null,
        val selected_main_article_id: String?          = null,
        val selected_main_article_date: String?        = null,
        val notification_time: String?                 = null,
        val notification_bonus_claimed: Boolean?       = null,
        val subscription_tier: String?                 = null,
        val is_og_member: Boolean?                     = null,
        val created_at: String?                        = null
    )

    @Serializable private data class XpUpdate(val current_xp: Int, val current_level: Int)

    @Serializable private data class CategoryUpdate(val preferred_categories: List<String>)

    @Serializable private data class CompanionSelectionUpdate(
        val display_name: String,
        val selected_companion_id: String
    )

    @Serializable private data class NotificationBonusUpdate(
        val current_xp: Int,
        val current_level: Int,
        val notification_bonus_claimed: Boolean
    )

    @Serializable private data class EquipCompanionUpdate(val selected_companion_id: String)

    @Serializable private data class NotificationTimeUpdate(val notification_time: String)

    @Serializable private data class MainArticleUpdate(
        @SerialName("selected_main_article_id")   val selectedMainArticleId: String,
        @SerialName("selected_main_article_date") val selectedMainArticleDate: String
    )

    // MARK: - Companion Unlock Event (global — collecté par MainTabView)

    // Triple = (name, id, level)
    private val _companionUnlockEvent = MutableSharedFlow<List<Triple<String, String, Int>>>(extraBufferCapacity = 1)
    val companionUnlockEvent: SharedFlow<List<Triple<String, String, Int>>> = _companionUnlockEvent.asSharedFlow()

    fun notifyCompanionUnlocksIfNeeded(oldLevel: Int, newLevel: Int): Boolean {
        if (newLevel <= oldLevel) return false
        val unlocked = ((oldLevel + 1)..newLevel).flatMap { level ->
            (CompanionData.byUnlockLevel[level] ?: emptyList())
                .filter { isPremium || level <= 5 }
                .map { Triple(it.name, it.id, level) }
        }
        if (unlocked.isNotEmpty()) _companionUnlockEvent.tryEmit(unlocked)
        return unlocked.isNotEmpty()
    }

    fun notifyPremiumCompanionUnlocks(companions: List<Pair<String, String>>) {
        if (companions.isEmpty()) return
        val withLevels = companions.mapNotNull { (name, id) ->
            val level = CompanionData.all.firstOrNull { it.id == id }?.unlockLevel
                ?: return@mapNotNull null
            Triple(name, id, level)
        }
        if (withLevels.isNotEmpty()) _companionUnlockEvent.tryEmit(withLevels)
    }

    // MARK: - Companion Check

    // Retourne true si le compagnon est sélectionné, false s'il ne l'est pas.
    // Propage les exceptions réseau — un résultat vide lève une exception (erreur réseau / RLS)
    // plutôt que de retourner false et d'envoyer à tort l'utilisateur en sélection de compagnon.
    suspend fun checkCompanion(): Boolean {
        val session = client.auth.currentSessionOrNull() ?: return false
        val userId = session.user?.id ?: return false

        val users = client.from("users")
            .select(Columns.list("selected_companion_id", "display_name")) {
                filter { eq("id", userId.toString()) }
            }
            .decodeList<UserCompanion>()

        // Un résultat vide = la ligne n'existe pas ou la requête a échoué silencieusement.
        // On lève une exception pour que l'appelant route vers ERROR, pas vers COMPANION_SELECTION.
        val user = users.firstOrNull()
            ?: throw Exception("Profil utilisateur introuvable (résultat vide)")

        // Seul selected_companion_id détermine si le setup compagnon est terminé.
        // display_name peut être vide sans que le compagnon soit perdu (incohérence partielle en base).
        val hasCompanion = !user.selected_companion_id.isNullOrEmpty()
        if (hasCompanion) {
            _selectedCompanionId.value = user.selected_companion_id!!
        }
        return hasCompanion
    }

    // MARK: - Data Loading

    /** Charge TOUTES les données (profil + streak + articles + stats) */
    suspend fun loadAllData() {
        if (isLoadingData) return
        isLoadingData = true
        try {
            val session = client.auth.currentSessionOrNull() ?: return
            currentUserId = session.user?.id?.toString()

            streakRepository.updateStreak()?.let { _currentStreak.value = it }
            loadUserProfile()
            loadArticles()
            _articlesReadToday.value = fetchArticlesReadToday()
            _articlesReadThisMonth.value = fetchArticlesReadThisMonth()
        } finally {
            isLoadingData = false
        }
    }

    /** Charge uniquement streak + articles + stats (après loadUserProfile).
     *  Bascule isDataReady false→true pour signaler à l'UI de sortir du skeleton. */
    suspend fun loadArticlesAndStats() {
        _isDataReady.value = false
        if (currentUserId == null) {
            currentUserId = client.auth.currentSessionOrNull()?.user?.id?.toString()
        }
        streakRepository.updateStreak()?.let { _currentStreak.value = it }
        loadUserProfile()
        loadArticles()
        _articlesReadToday.value = fetchArticlesReadToday()
        _articlesReadThisMonth.value = fetchArticlesReadThisMonth()
        _isDataReady.value = true
    }

    /** Charge le profil utilisateur complet depuis Supabase */
    suspend fun loadUserProfile() {
        val session = client.auth.currentSessionOrNull() ?: return
        val userId = session.user?.id?.toString() ?: return

        val users = client.from("users")
            .select(Columns.list(
                "display_name", "selected_companion_id", "current_xp", "max_xp",
                "current_level", "current_streak", "preferred_categories", "selected_main_article_id",
                "selected_main_article_date", "notification_time", "notification_bonus_claimed",
                "subscription_tier", "is_og_member", "created_at"
            )) {
                filter { eq("id", userId) }
            }
            .decodeList<UserProfile>()

        val profile = users.firstOrNull()
            ?: throw Exception("Profil utilisateur introuvable")

        _displayName.value = profile.display_name
        _selectedCompanionId.value = profile.selected_companion_id ?: ""
        _currentStreak.value = profile.current_streak
        _currentXp.value = profile.current_xp
        _maxXp.value = profile.max_xp
        _currentLevel.value = profile.current_level
        _preferredCategories.value = profile.preferred_categories ?: emptyList()
        _selectedMainArticleId.value = profile.selected_main_article_id
        _selectedMainArticleDate.value = profile.selected_main_article_date
        _notificationTime.value = profile.notification_time
        _notificationBonusClaimed.value = profile.notification_bonus_claimed ?: false
        _subscriptionTier.value = SubscriptionTier.from(profile.subscription_tier)
        _isOGMember.value = profile.is_og_member ?: false
        _registrationDate.value = profile.created_at?.take(10)?.let { dateStr ->
            runCatching { LocalDate.parse(dateStr) }.getOrNull()
        }
    }

    // MARK: - XP Management

    /** Ajoute de l'XP localement, gère les level-ups. Retourne true si level-up. */
    /** Retourne true si un ou plusieurs compagnons ont été débloqués. */
    fun addXp(amount: Int): Boolean {
        val oldLevel = _currentLevel.value
        var xp = _currentXp.value + amount
        var level = oldLevel
        var leveledUp = false
        while (xp >= _maxXp.value) {
            xp -= _maxXp.value
            level++
            leveledUp = true
        }
        _currentXp.value = xp
        _currentLevel.value = level
        return if (leveledUp) notifyCompanionUnlocksIfNeeded(oldLevel, level)
               else false
    }

    suspend fun saveXpAndLevel() {
        val session = client.auth.currentSessionOrNull() ?: return
        val userId = session.user?.id?.toString() ?: return

        client.from("users")
            .update(XpUpdate(_currentXp.value, _currentLevel.value)) {
                filter { eq("id", userId) }
            }
    }

    // MARK: - Preferred Categories

    suspend fun savePreferredCategories(categories: List<String>) {
        val session = client.auth.currentSessionOrNull() ?: return
        val userId = session.user?.id?.toString() ?: return

        client.from("users")
            .update(CategoryUpdate(categories)) {
                filter { eq("id", userId) }
            }
        _preferredCategories.value = categories
    }

    suspend fun updatePreferredCategories(categories: List<String>) {
        savePreferredCategories(categories)
        loadArticles()
    }

    // MARK: - Articles Stats

    suspend fun fetchArticlesReadToday(): Int {
        val userId = currentUserId ?: return 0
        val today = LocalDate.now()
        val todayStart = today.atStartOfDay(ZoneOffset.UTC).toInstant()
        val tomorrowStart = today.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant()
        val fmt = DateTimeFormatter.ISO_INSTANT

        return client.from("user_article_interactions")
            .select {
                count(Count.EXACT)
                filter {
                    eq("user_id", userId)
                    eq("is_read", true)
                    gte("read_at", fmt.format(todayStart))
                    lt("read_at", fmt.format(tomorrowStart))
                }
            }
            .countOrNull()?.toInt() ?: 0
    }

    suspend fun fetchArticlesReadThisMonth(): Int {
        val userId = currentUserId ?: return 0
        val month = YearMonth.now()
        val startOfMonth = month.atDay(1).atStartOfDay(ZoneOffset.UTC).toInstant()
        val startOfNextMonth = month.plusMonths(1).atDay(1).atStartOfDay(ZoneOffset.UTC).toInstant()
        val fmt = DateTimeFormatter.ISO_INSTANT

        return client.from("user_article_interactions")
            .select {
                count(Count.EXACT)
                filter {
                    eq("user_id", userId)
                    eq("is_read", true)
                    gte("read_at", fmt.format(startOfMonth))
                    lt("read_at", fmt.format(startOfNextMonth))
                }
            }
            .countOrNull()?.toInt() ?: 0
    }

    // MARK: - Notification Management

    suspend fun claimNotificationBonus() {
        if (_notificationBonusClaimed.value) return
        val session = client.auth.currentSessionOrNull() ?: return
        val userId = session.user?.id?.toString() ?: return

        val oldLevel = _currentLevel.value
        var xp = _currentXp.value + 80
        var level = oldLevel
        while (xp >= _maxXp.value) { xp -= _maxXp.value; level++ }
        if (level > oldLevel) notifyCompanionUnlocksIfNeeded(oldLevel, level)

        _currentXp.value = xp
        _currentLevel.value = level
        _notificationBonusClaimed.value = true

        client.from("users")
            .update(NotificationBonusUpdate(xp, level, true)) {
                filter { eq("id", userId) }
            }
    }

    /** Sauvegarde l'heure de notification (in-memory + Supabase) */
    suspend fun saveNotificationTime(time: String) {
        _notificationTime.value = time  // DataStore ajouté lors du port Notifications

        val session = client.auth.currentSessionOrNull() ?: return
        val userId = session.user?.id?.toString() ?: return

        client.from("users")
            .update(NotificationTimeUpdate(time)) {
                filter { eq("id", userId) }
            }
    }


    suspend fun saveCompanionSelection(displayName: String, companionId: String) {
        val session = client.auth.currentSessionOrNull() ?: throw Exception("Session introuvable")
        val userId  = session.user?.id?.toString() ?: throw Exception("Utilisateur introuvable")
        client.from("users")
            .update(CompanionSelectionUpdate(displayName, companionId)) {
                filter { eq("id", userId) }
            }
    }

    // MARK: - Reset

    fun reset() {
        _displayName.value = ""
        _currentStreak.value = 0
        _selectedCompanionId.value = ""
        _currentXp.value = 0
        _maxXp.value = 100
        _currentLevel.value = 1
        _articles.value = emptyList()
        _mainArticle.value = null
        _secondaryArticles.value = emptyList()
        _articlesReadToday.value = 0
        _articlesReadThisMonth.value = 0
        _preferredCategories.value = emptyList()
        _selectedMainArticleId.value = null
        _selectedMainArticleDate.value = null
        _notificationTime.value = null
        _notificationBonusClaimed.value = false
        _subscriptionTier.value = SubscriptionTier.FREE
        _isOGMember.value = false
        _registrationDate.value = null
        _isDataReady.value = false
        currentUserId = null
    }

    // MARK: - Articles Read (local increment)

    fun incrementArticlesRead() {
        _articlesReadToday.value += 1
        _articlesReadThisMonth.value += 1
    }

    // MARK: - Subscription Helpers

    val isPremium: Boolean get() = _subscriptionTier.value == SubscriptionTier.PREMIUM

    fun setSelectedCompanionId(id: String)              { _selectedCompanionId.value  = id   }
    fun setSubscriptionTier(tier: SubscriptionTier)     { _subscriptionTier.value     = tier }

    fun isCompanionUnlocked(unlockLevel: Int): Boolean {
        return if (isPremium) {
            _currentLevel.value >= unlockLevel
        } else {
            unlockLevel <= 5 && _currentLevel.value >= unlockLevel
        }
    }

    fun unlockPremiumCompanions(): List<Pair<String, String>> {
        if (!isPremium || _currentLevel.value < 6) return emptyList()
        return (6.._currentLevel.value).flatMap { level ->
            CompanionData.byUnlockLevel[level]?.map { it.name to it.id } ?: emptyList()
        }
    }

    suspend fun saveEquippedCompanion(id: String) {
        val session = client.auth.currentSessionOrNull() ?: return
        val userId  = session.user?.id?.toString() ?: return
        client.from("users")
            .update(EquipCompanionUpdate(id)) {
                filter { eq("id", userId) }
            }
    }

    // MARK: - Private helpers

    private suspend fun loadArticles() {
        val fetchedArticles = articleRepository.fetchTodayArticles()
        _articles.value = fetchedArticles

        val today = LocalDate.now().toString()

        // Réutiliser la sélection du jour si elle existe
        val savedId = _selectedMainArticleId.value
        val savedDate = _selectedMainArticleDate.value
        if (savedId != null && savedDate == today) {
            val savedArticle = fetchedArticles.find { it.id == savedId }
            if (savedArticle != null) {
                _mainArticle.value = savedArticle
                _secondaryArticles.value = fetchedArticles.filter { it.id != savedId }.take(4)
                return
            }
        }

        selectNewMainArticle(fetchedArticles, today)
    }

    private suspend fun selectNewMainArticle(fetchedArticles: List<Article>, today: String) {
        val preferred = _preferredCategories.value

        if (preferred.isNotEmpty()) {
            val preferredArticles = fetchedArticles.filter { preferred.contains(it.category.lowercase()) }

            if (preferredArticles.isNotEmpty()) {
                val main = preferredArticles.random()
                _mainArticle.value = main
                saveMainArticleSelection(main.id, today)

                val remainingPreferred = preferredArticles.filter { it.id != main.id }
                val others = fetchedArticles.filter { it.id != main.id && !preferred.contains(it.category.lowercase()) }
                _secondaryArticles.value = (remainingPreferred + others).take(4)
                return
            }
        }

        fallbackArticleSelection(fetchedArticles, today)
    }

    private suspend fun fallbackArticleSelection(articles: List<Article>, today: String) {
        val first = articles.firstOrNull() ?: return
        _mainArticle.value = first
        saveMainArticleSelection(first.id, today)
        _secondaryArticles.value = articles.drop(1).take(4)
    }

    private suspend fun saveMainArticleSelection(articleId: String, date: String) {
        val session = client.auth.currentSessionOrNull() ?: return
        val userId = session.user?.id?.toString() ?: return

        client.from("users")
            .update(MainArticleUpdate(articleId, date)) {
                filter { eq("id", userId) }
            }

        _selectedMainArticleId.value = articleId
        _selectedMainArticleDate.value = date
    }
}
