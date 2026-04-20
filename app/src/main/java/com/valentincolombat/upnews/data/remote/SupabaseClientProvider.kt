package com.valentincolombat.upnews.data.remote

import android.content.Context
import android.content.SharedPreferences
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.SessionManager
import io.github.jan.supabase.auth.user.UserSession
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.functions.Functions
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private class SharedPreferencesSessionManager(context: Context) : SessionManager {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("supabase_session", Context.MODE_PRIVATE)

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun loadSession(): UserSession? {
        val raw = prefs.getString(KEY_SESSION, null) ?: return null
        return try {
            json.decodeFromString<UserSession>(raw)
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun saveSession(session: UserSession) {
        prefs.edit().putString(KEY_SESSION, json.encodeToString(session)).apply()
    }

    override suspend fun deleteSession() {
        prefs.edit().remove(KEY_SESSION).apply()
    }

    companion object {
        private const val KEY_SESSION = "session"
    }
}

object SupabaseClientProvider {

    lateinit var client: io.github.jan.supabase.SupabaseClient
        private set

    fun init(context: Context) {
        client = createSupabaseClient(
            supabaseUrl = SupabaseSecrets.URL,
            supabaseKey = SupabaseSecrets.ANON_KEY
        ) {
            install(Auth) {
                sessionManager = SharedPreferencesSessionManager(context.applicationContext)
            }
            install(Postgrest)
            install(Realtime)
            install(Functions)
        }
    }
}
