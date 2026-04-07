package com.valentincolombat.upnews

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.valentincolombat.upnews.data.remote.SupabaseClient
import com.valentincolombat.upnews.service.AppStateService
import com.valentincolombat.upnews.service.GoogleSecrets
import com.valentincolombat.upnews.ui.AppContent
import com.valentincolombat.upnews.ui.theme.UpNewsTheme
import io.github.jan.supabase.auth.handleDeeplinks
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        installSplashScreen()

        // Force Light Mode géré via UpNewsTheme(darkTheme = false) ci-dessous
        configureGoogleSignIn()

        // Gère le deep link OAuth si l'app est lancée depuis le callback
        handleOAuthIntent(intent)

        enableEdgeToEdge()
        setContent {
            UpNewsTheme(darkTheme = false) {
                AppContent()
            }
        }
    }

    // Gère le deep link quand l'app est déjà ouverte (singleTop)
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleOAuthIntent(intent)
    }

    // Efface le badge notifications + rafraîchit les données quand l'app devient active
    override fun onResume() {
        super.onResume()
        clearNotificationBadge()
        saveOpenDate()
        lifecycleScope.launch {
            AppStateService.shared.refreshIfActive()
        }
    }

    // MARK: - Private

    private fun configureGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(GoogleSecrets.SERVER_CLIENT_ID)
            .requestEmail()
            .build()
        GoogleSignIn.getClient(this, gso)
    }

    // Équivalent .onOpenURL { url in ... } — gère uniquement Supabase
    // (Google Sign-In Android n'utilise pas de deep link, géré via ActivityResultLauncher dans AuthRepository)
    private fun handleOAuthIntent(intent: Intent) {
        val uri = intent.data ?: return
        if (uri.scheme != "upnews") return

        lifecycleScope.launch {
            try {
                SupabaseClient.client.handleDeeplinks(intent)
            } catch (e: Exception) {
                Log.e("MainActivity", "Erreur session Supabase depuis URL: $e")
            }
        }
    }

    private fun saveOpenDate() {
        getSharedPreferences(com.valentincolombat.upnews.service.NotificationManager.PREFS_NAME, MODE_PRIVATE)
            .edit()
            .putString(com.valentincolombat.upnews.service.NotificationManager.KEY_LAST_OPEN_DATE, java.time.LocalDate.now().toString())
            .apply()
    }

    private fun clearNotificationBadge() {
        NotificationManagerCompat.from(this).cancel(com.valentincolombat.upnews.service.NotificationManager.NOTIFICATION_ID)
    }
}
