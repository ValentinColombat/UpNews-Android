package com.valentincolombat.upnews.data.repository

import android.util.Log
import com.valentincolombat.upnews.data.remote.SupabaseClient
import com.valentincolombat.upnews.service.AppStateService
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.providers.builtin.IDToken
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.auth.user.UserInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull

class AuthRepository private constructor() {

    // MARK: - Singleton

    companion object {
        val shared = AuthRepository()
    }

    // MARK: - State (équivalent @Published iOS)

    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    private val _currentUser = MutableStateFlow<UserInfo?>(null)
    val currentUser: StateFlow<UserInfo?> = _currentUser.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val client = SupabaseClient.client

    // MARK: - Auth Status

    /** Vérifie si l'utilisateur est déjà connecté.
     *
     *  awaitInitialization() suspend jusqu'à ce que le SDK ait :
     *    1. chargé la session depuis SharedPreferences
     *    2. rafraîchi l'access token si expiré (échange du refresh token)
     *  Après ce point, currentSessionOrNull() est fiable à 100% — pas besoin
     *  de timeout ni de fallback : l'état est définitif.
     */
    suspend fun checkAuthStatus() {
        // Timeout de sécurité : si le SDK reste bloqué en Initializing (bug SDK,
        // stockage corrompu), on ne suspend pas indéfiniment. Au-delà de 10s,
        // currentSessionOrNull() est appelé tel quel — s'il retourne null,
        // l'utilisateur va sur AUTH, ce qui est le comportement le plus sûr.
        withTimeoutOrNull(10_000) {
            client.auth.sessionStatus.first { it !is SessionStatus.Initializing }
        }

        val session = client.auth.currentSessionOrNull()
        if (session != null) {
            _isAuthenticated.value = true
            _currentUser.value = client.auth.currentUserOrNull()
        } else {
            _isAuthenticated.value = false
            _currentUser.value = null
        }
    }

    // MARK: - Connexion Email

    suspend fun signIn(email: String, password: String) {
        _isLoading.value = true
        _errorMessage.value = null

        val cleanEmail = email.trim().lowercase()
        val cleanPassword = password.trim()

        if (cleanEmail.isEmpty() || cleanPassword.isEmpty()) {
            _errorMessage.value = "Email et mot de passe requis"
            _isLoading.value = false
            return
        }

        try {
            client.auth.signInWith(Email) {
                this.email = cleanEmail
                this.password = cleanPassword
            }
            _isAuthenticated.value = true
            _currentUser.value = client.auth.currentUserOrNull()
            _isLoading.value = false
            AppStateService.shared.handleAuthentication()
        } catch (e: Exception) {
            _errorMessage.value = formatSignInError(e.message ?: "")
            _isAuthenticated.value = false
            _isLoading.value = false
        }
    }

    // MARK: - Inscription

    suspend fun signUp(email: String, password: String) {
        _isLoading.value = true
        _errorMessage.value = null

        val cleanEmail = email.trim().lowercase()
        val cleanPassword = password.trim()

        if (cleanEmail.isEmpty()) {
            _errorMessage.value = "Email requis"
            _isLoading.value = false
            return
        }

        if (cleanPassword.length < 6) {
            _errorMessage.value = "Le mot de passe doit contenir au moins 6 caractères"
            _isLoading.value = false
            return
        }

        try {
            client.auth.signUpWith(Email) {
                this.email = cleanEmail
                this.password = cleanPassword
            }
            _isAuthenticated.value = true
            _currentUser.value = client.auth.currentUserOrNull()
            _isLoading.value = false
            AppStateService.shared.handleAuthentication()
        } catch (e: Exception) {
            _errorMessage.value = formatSignUpError(e.message ?: "")
            _isAuthenticated.value = false
            _isLoading.value = false
        }
    }

    // MARK: - Google Sign-In
    //
    // Divergence iOS → Android :
    // iOS lance GIDSignIn depuis le service (UIKit VC disponible).
    // Android : le lancement du sélecteur Google se fait depuis AuthScreen
    // via ActivityResultLauncher. Le repository reçoit uniquement l'idToken résultant.

    suspend fun signInWithGoogle(idToken: String) {
        Log.d("GoogleAuth", "signInWithGoogle appelé, idToken=${idToken.take(20)}...")
        _isLoading.value = true
        _errorMessage.value = null

        try {
            Log.d("GoogleAuth", "Appel Supabase signInWith IDToken...")
            client.auth.signInWith(IDToken) {
                provider = Google
                this.idToken = idToken
            }
            val user = client.auth.currentUserOrNull()
            Log.d("GoogleAuth", "Supabase OK — user=${user?.email}, id=${user?.id}")
            _isAuthenticated.value = true
            _currentUser.value = user
            AppStateService.shared.handleAuthentication()
        } catch (e: Exception) {
            Log.e("GoogleAuth", "Erreur Supabase: ${e::class.simpleName} — ${e.message}")
            _errorMessage.value = "Connexion Google impossible. Réessaie dans quelques instants."
            _isAuthenticated.value = false
        } finally {
            _isLoading.value = false
        }
    }

    // MARK: - Helpers

    fun clearError()           { _errorMessage.value = null }
    fun setError(msg: String)  { _errorMessage.value = msg  }

    private fun formatSignInError(msg: String): String = when {
        msg.contains("Invalid login credentials") ->
            "Email ou mot de passe incorrect. Si tu t'es inscrit avec Google, utilise le bouton Google."
        msg.contains("Email not confirmed") ->
            "Vérifie ton email pour confirmer ton compte."
        else -> "Connexion impossible. Vérifie ta connexion et réessaie."
    }

    private fun formatSignUpError(msg: String): String = when {
        msg.contains("User already registered", ignoreCase = true) ||
        msg.contains("already been registered", ignoreCase = true) ->
            "Un compte existe déjà avec cet email. Connecte-toi ou utilise 'Mot de passe oublié'."
        msg.contains("Password should be at least", ignoreCase = true) ->
            "Le mot de passe doit contenir au moins 6 caractères."
        msg.contains("Unable to validate email", ignoreCase = true) ||
        msg.contains("invalid email", ignoreCase = true) ->
            "L'adresse email n'est pas valide."
        msg.contains("rate limit", ignoreCase = true) ->
            "Trop de tentatives. Réessaie dans quelques minutes."
        else -> "Inscription impossible. Vérifie ta connexion et réessaie."
    }

    // MARK: - Déconnexion

    suspend fun signOut() {
        try {
            client.auth.signOut()
            _isAuthenticated.value = false
            _currentUser.value = null
            AppStateService.shared.handleSignOut()
        } catch (e: Exception) {
            // Erreur silencieuse — même comportement iOS
        }
    }
}
