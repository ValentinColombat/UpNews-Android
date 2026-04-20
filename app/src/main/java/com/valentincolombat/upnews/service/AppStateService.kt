package com.valentincolombat.upnews.service

import com.valentincolombat.upnews.data.billing.BillingManager
import com.valentincolombat.upnews.data.remote.SupabaseClient
import com.valentincolombat.upnews.data.repository.AuthRepository
import com.valentincolombat.upnews.data.repository.UserRepository
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

class AppStateService private constructor() {

    // MARK: - Singleton

    companion object {
        val shared = AppStateService()
    }

    // MARK: - App Screen

    enum class AppScreen {
        LOADING,
        ONBOARDING,
        AUTH,
        COMPANION_SELECTION,
        CATEGORY_SELECTION,
        MAIN,
        ERROR
    }

    // MARK: - State

    private val _currentScreen = MutableStateFlow(AppScreen.LOADING)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    // MARK: - Dependencies

    private val authRepository = AuthRepository.shared
    private val userRepository = UserRepository.shared

    /** Scope dédié aux tâches de fond (chargement articles, stats).
     *  SupervisorJob : une coroutine enfant en échec n'annule pas les autres. */
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private var lastRefreshTime = 0L
    private val REFRESH_COOLDOWN_MS = 5 * 60 * 1000L

    // MARK: - Public Methods

    /** Point d'entrée unique pour initialiser l'app.
     *
     *  3 phases distinctes :
     *
     *  PHASE A — Auth Gate (locale, < 500ms)
     *    awaitInitialization() garantit que currentSessionOrNull() est fiable.
     *    Seule phase pouvant légitimement mener à AUTH.
     *
     *  PHASE B — Setup Check (réseau, timeout 10s)
     *    Vérifie compagnon + profil. Toute erreur réseau → ERROR retryable,
     *    jamais AUTH (l'utilisateur est authentifié, c'est le réseau qui flanche).
     *
     *  PHASE C — Data Loading (arrière-plan, non bloquant)
     *    L'app navigue vers MAIN dès la phase B validée. Articles et stats
     *    chargent en fond ; l'UI affiche des skeletons via isDataReady.
     */
    suspend fun initialize(hasCompletedOnboarding: Boolean) {
        _currentScreen.value = AppScreen.LOADING

        // Onboarding non terminé — court-circuit immédiat
        if (!hasCompletedOnboarding) {
            _currentScreen.value = AppScreen.ONBOARDING
            return
        }

        // PHASE A — awaitInitialization() attend que le SDK charge + rafraîchisse
        // le token depuis le stockage local. Aucun timeout : c'est une op locale
        // (sauf si le refresh réseau échoue, auquel cas awaitInitialization retourne
        // quand même et currentSessionOrNull() reflète l'état réel).
        authRepository.checkAuthStatus()
        if (!authRepository.isAuthenticated.value) {
            _currentScreen.value = AppScreen.AUTH
            return
        }

        // Vérification abonnement en best-effort (jamais bloquante)
        runCatching { BillingManager.shared.checkSubscriptionValidity() }

        // PHASES B + C — timeout 12s : couvre profil + articles + stats
        val completed = withTimeoutOrNull(12_000) {
            routeAuthenticatedUser(isNewUserFlow = false)
        }
        if (completed == null) {
            _currentScreen.value = AppScreen.ERROR
        }
    }

    /** Appelé après connexion (Email ou Google).
     *  La session vient d'être créée par signIn/signUp — awaitInitialization()
     *  retourne immédiatement car le SDK est déjà initialisé. On vérifie juste
     *  que la session est bien présente avant de router. */
    suspend fun handleAuthentication() {
        _currentScreen.value = AppScreen.LOADING

        SupabaseClient.client.auth.sessionStatus.first { it !is SessionStatus.Initializing }

        if (SupabaseClient.client.auth.currentSessionOrNull() == null) {
            _currentScreen.value = AppScreen.AUTH
            return
        }

        runCatching { BillingManager.shared.checkSubscriptionValidity() }
        routeAuthenticatedUser(isNewUserFlow = true)
    }

    /** Appelé après sélection du compagnon */
    suspend fun handleCompanionSelected() {
        _currentScreen.value = AppScreen.LOADING
        loadProfileAndRoute(isNewUserFlow = true)
    }

    /** Appelé après sélection des catégories */
    suspend fun handleCategoriesSelected() {
        _currentScreen.value = AppScreen.LOADING
        try {
            userRepository.loadUserProfile()
            userRepository.loadArticlesAndStats()
        } catch (e: Exception) {
            _currentScreen.value = AppScreen.ERROR
            return
        }
        _currentScreen.value = AppScreen.MAIN
    }

    /** Appelé à la fin de l'onboarding */
    fun completeOnboarding() {
        _currentScreen.value = AppScreen.AUTH
    }

    /** Appelé au retour au premier plan.
     *  awaitInitialization() garantit que le refresh token a eu le temps de
     *  s'exécuter avant qu'on lise l'état de session — élimine les faux négatifs
     *  sur session expirée encore en cours de renouvellement. */
    suspend fun refreshIfActive() {
        if (_currentScreen.value != AppScreen.MAIN) return
        val now = System.currentTimeMillis()
        if (now - lastRefreshTime < REFRESH_COOLDOWN_MS) return

        SupabaseClient.client.auth.sessionStatus.first { it !is SessionStatus.Initializing }

        if (SupabaseClient.client.auth.currentSessionOrNull() == null) {
            handleSignOut()
            return
        }
        lastRefreshTime = now
        runCatching { userRepository.loadUserProfile() }
        runCatching { BillingManager.shared.checkSubscriptionValidity() }
        runCatching { userRepository.loadArticlesAndStats() }
    }

    /** Appelé lors de la déconnexion */
    fun handleSignOut() {
        userRepository.reset()
        _currentScreen.value = AppScreen.AUTH
    }

    // MARK: - Private routing

    /** PHASE B — vérifie la présence du compagnon et route en conséquence.
     *
     *  Règle : une erreur réseau ici ne signifie pas que l'utilisateur n'est pas connecté.
     *  On route vers ERROR (retryable) et non vers AUTH.
     *
     *  [isNewUserFlow] = true uniquement lors d'une connexion/inscription fraîche.
     *    → pas de compagnon = setup non terminé → COMPANION_SELECTION (normal)
     *  [isNewUserFlow] = false = recovery de session au démarrage.
     *    → pas de compagnon = incohérence ou erreur réseau → ERROR retryable */
    private suspend fun routeAuthenticatedUser(isNewUserFlow: Boolean) {
        val hasCompanion = try {
            userRepository.checkCompanion()
        } catch (e: Exception) {
            _currentScreen.value = AppScreen.ERROR
            return
        }

        if (!hasCompanion) {
            _currentScreen.value = if (isNewUserFlow) AppScreen.COMPANION_SELECTION else AppScreen.ERROR
            return
        }

        loadProfileAndRoute(isNewUserFlow = isNewUserFlow)
    }

    /** PHASES B+C — charge le profil et les articles, puis navigue vers MAIN.
     *
     *  Tout est bloquant : MAIN n'est affiché qu'une fois toutes les données prêtes,
     *  ce qui garantit un premier rendu complet sans flash de page blanche.
     *  En cas d'erreur réseau → ERROR retryable, jamais AUTH. */
    private suspend fun loadProfileAndRoute(isNewUserFlow: Boolean = false) {
        try {
            userRepository.loadUserProfile()
        } catch (e: Exception) {
            _currentScreen.value = AppScreen.ERROR
            return
        }

        if (userRepository.preferredCategories.value.isEmpty()) {
            _currentScreen.value = if (isNewUserFlow) AppScreen.CATEGORY_SELECTION else AppScreen.ERROR
            return
        }

        try {
            userRepository.loadArticlesAndStats()
        } catch (e: Exception) {
            _currentScreen.value = AppScreen.ERROR
            return
        }

        _currentScreen.value = AppScreen.MAIN
    }
}
