package com.valentincolombat.upnews.data.repository

import android.util.Log
import com.valentincolombat.upnews.data.remote.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.serialization.Serializable
import java.time.LocalDate
import java.time.temporal.ChronoUnit

private const val TAG = "StreakRepository"

class StreakRepository private constructor() {

    companion object {
        val shared = StreakRepository()
    }

    private val client = SupabaseClient.client

    /**
     * Met à jour le streak de connexion (même logique que iOS StreakService).
     *  - Si last_connection_date = aujourd'hui → déjà connecté, retourne current_streak
     *  - Si last_connection_date = hier → incrémente
     *  - Sinon → reset à 1 (première connexion ou série cassée)
     */
    suspend fun updateStreak(): Int? {
        return try {
            val session = client.auth.currentSessionOrNull() ?: run {
                Log.w(TAG, "updateStreak: session null")
                return null
            }
            val userId = session.user?.id?.toString() ?: return null

            @Serializable
            data class StreakData(
                val current_streak: Int = 0,
                val last_connection_date: String? = null
            )

            val users = client.from("users")
                .select(Columns.list("current_streak", "last_connection_date")) {
                    filter { eq("id", userId) }
                }
                .decodeList<StreakData>()

            val data = users.firstOrNull() ?: return null
            val today = LocalDate.now().toString()

            Log.d(TAG, "updateStreak: last=${ data.last_connection_date }, streak=${data.current_streak}, today=$today")

            val newStreak = calculateNewStreak(data.current_streak, data.last_connection_date, today)

            if (data.last_connection_date != today) {
                Log.d(TAG, "updateStreak: saving newStreak=$newStreak")
                saveStreak(userId, newStreak, today)
            }

            Log.d(TAG, "updateStreak: returning $newStreak")
            newStreak
        } catch (e: Exception) {
            Log.e(TAG, "updateStreak: exception", e)
            null
        }
    }

    private fun calculateNewStreak(currentStreak: Int, lastConnectionDate: String?, today: String): Int {
        if (lastConnectionDate == null) return 1
        if (lastConnectionDate == today) return currentStreak

        val last = LocalDate.parse(lastConnectionDate)
        val current = LocalDate.parse(today)
        val daysDiff = ChronoUnit.DAYS.between(last, current)

        return when (daysDiff) {
            1L   -> currentStreak + 1
            0L   -> currentStreak
            else -> 1
        }
    }

    private suspend fun saveStreak(userId: String, streak: Int, date: String) {
        @Serializable
        data class StreakUpdate(val current_streak: Int, val last_connection_date: String)

        client.from("users")
            .update(StreakUpdate(streak, date)) {
                filter { eq("id", userId) }
            }
    }
}
