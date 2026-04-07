package com.valentincolombat.upnews.service

import com.valentincolombat.upnews.data.billing.BillingManager
import com.valentincolombat.upnews.data.remote.SupabaseClient
import com.valentincolombat.upnews.data.repository.AuthRepository
import com.valentincolombat.upnews.data.repository.UserRepository
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
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

    private var lastRefreshTime = 0L
    private val REFRESH_COOLDOWN_MS = 5 * 60 * 1000L

    // MARK: - Public Methods

    /** Point d'entrée unique pour initialiser l'app */
    suspend fun initialize(hasCompletedOnboarding: Boolean) {
        _currentScreen.value = AppScreen.LOADING

        // 1. Onboarding non terminé
        if (!hasCompletedOnboarding) {
            _currentScreen.value = AppScreen.ONBOARDING
            return
        }

        // 2. Vérifier authentification
        authRepository.checkAuthStatus()

        if (!authRepository.isAuthenticated.value) {
            _currentScreen.value = AppScreen.AUTH
            return
        }

        // 2b. Vérifier validité abonnement (downgrade si expiré/annulé)
        runCatching { BillingManager.shared.checkSubscriptionValidity() }

        // 3-5. Routing utilisateur authentifié — timeout 15s pour éviter un LOADING infini
        val completed = withTimeoutOrNull(15_000) {
            routeAuthenticatedUser(isNewUserFlow = false)
        }

        if (completed == null) {
            _currentScreen.value = AppScreen.ERROR
        }
    }

    /** Appelé après connexion (Email ou Google) */
    suspend fun handleAuthentication() {
        _currentScreen.value = AppScreen.LOADING

        // Attendre confirmation de la session (remplace le delay(300) arbitraire)
        val sessionReady = withTimeoutOrNull(5_000) {
            SupabaseClient.client.auth.sessionStatus
                .first { it is SessionStatus.Authenticated }
        }
        if (sessionReady == null) {
            _currentScreen.value = AppScreen.AUTH
            return
        }

        runCatching { BillingManager.shared.checkSubscriptionValidity() }
        routeAuthenticatedUser(isNewUserFlow = true)
    }

    /** Appelé après sélection du compagnon */
    suspend fun handleCompanionSelected() {
        _currentScreen.value = AppScreen.LOADING
        // Le save est déjà complété (SDK Supabase est synchrone) — pas de delay nécessaire
        loadProfileAndRoute(isNewUserFlow = true)
    }

    /** Appelé après sélection des catégories */
    suspend fun handleCategoriesSelected() {
        _currentScreen.value = AppScreen.LOADING
        // Le save est déjà complété (SDK Supabase est synchrone) — pas de delay nécessaire
        try {
            userRepository.loadAllData()
            _currentScreen.value = AppScreen.MAIN
        } catch (e: Exception) {
            _currentScreen.value = AppScreen.AUTH
        }
    }

    /** Appelé à la fin de l'onboarding */
    fun completeOnboarding() {
        _currentScreen.value = AppScreen.AUTH
    }

    /** Appelé au retour au premier plan */
    suspend fun refreshIfActive() {
        if (_currentScreen.value != AppScreen.MAIN) return
        val now = System.currentTimeMillis()
        if (now - lastRefreshTime < REFRESH_COOLDOWN_MS) return
        val status = withTimeoutOrNull(3_000) {
            SupabaseClient.client.auth.sessionStatus
                .first { it !is SessionStatus.Initializing }
        } ?: return
        if (status !is SessionStatus.Authenticated) {
            // Avant de déconnecter, vérifier le cache local :
            // un refresh token encore présent signifie que la session est toujours valide
            // côté serveur, le SDK n'a simplement pas eu le temps de la rafraîchir.
            val cachedSession = SupabaseClient.client.auth.currentSessionOrNull()
            if (cachedSession != null) return
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

    /** Vérifie compagnon + pseudo, puis route vers profil/catégories/main.
     *  [isNewUserFlow] = true uniquement lors d'une connexion/inscription fraîche (handleAuthentication).
     *  En recovery de session (initialize), pas de compagnon = erreur de chargement → AUTH. */
    private suspend fun routeAuthenticatedUser(isNewUserFlow: Boolean) {
        val hasCompanion = try {
            userRepository.checkCompanion()
        } catch (e: Exception) {
            _currentScreen.value = AppScreen.AUTH
            return
        }

        if (!hasCompanion) {
            _currentScreen.value = if (isNewUserFlow) AppScreen.COMPANION_SELECTION else AppScreen.AUTH
            return
        }

        loadProfileAndRoute(isNewUserFlow = isNewUserFlow)
    }

    /** Charge le profil et route vers catégories ou main selon les données.
     *  [isNewUserFlow] = true uniquement quand l'utilisateur vient de choisir son compagnon
     *  (première configuration). Dans tous les autres cas (recovery de session), des catégories
     *  vides indiquent une erreur de chargement → AUTH. */
    private suspend fun loadProfileAndRoute(isNewUserFlow: Boolean = false) {
        try {
            userRepository.loadUserProfile()

            if (userRepository.preferredCategories.value.isEmpty()) {
                _currentScreen.value = if (isNewUserFlow) AppScreen.CATEGORY_SELECTION else AppScreen.AUTH
                return
            }

            userRepository.loadArticlesAndStats()
            _currentScreen.value = AppScreen.MAIN

        } catch (e: Exception) {
            _currentScreen.value = AppScreen.AUTH
        }
    }
}
