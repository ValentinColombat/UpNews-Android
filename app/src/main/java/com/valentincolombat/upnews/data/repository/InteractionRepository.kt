package com.valentincolombat.upnews.data.repository

import com.valentincolombat.upnews.data.remote.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.Instant
import java.time.format.DateTimeFormatter

class InteractionRepository private constructor() {

    companion object {
        val shared = InteractionRepository()
    }

    private val client = SupabaseClient.client

    // MARK: - Modèle public

    @Serializable
    data class ArticleInteraction(
        @SerialName("is_liked")      val isLiked: Boolean     = false,
        @SerialName("is_favorite")   val isFavorite: Boolean  = false,
        @SerialName("is_read")       val isRead: Boolean      = false,
        @SerialName("has_claimed_xp") val hasClaimedXp: Boolean = false
    )

    // MARK: - Modèles privés (DB)

    @Serializable
    private data class UpsertRow(
        val user_id: String,
        val article_id: String,
        val is_liked: Boolean,
        val is_favorite: Boolean,
        val is_read: Boolean,
        val has_claimed_xp: Boolean
    )

    @Serializable
    private data class ReadAtRow(
        val user_id: String,
        val article_id: String,
        val is_read: Boolean,
        val read_at: String,
        val is_liked: Boolean       = false,
        val is_favorite: Boolean    = false,
        val has_claimed_xp: Boolean = false
    )

    @Serializable
    private data class ArticleIdRow(@SerialName("article_id") val articleId: String)

    @Serializable
    private data class FavUpsertRow(
        val user_id: String,
        val article_id: String,
        val is_favorite: Boolean
    )

    // MARK: - Lecture

    suspend fun loadInteractions(articleId: String): ArticleInteraction? {
        val userId = client.auth.currentSessionOrNull()?.user?.id?.toString() ?: return null
        return client.from("user_article_interactions")
            .select(Columns.list("is_liked", "is_favorite", "is_read", "has_claimed_xp")) {
                filter { eq("user_id", userId); eq("article_id", articleId) }
            }
            .decodeList<ArticleInteraction>()
            .firstOrNull()
    }

    suspend fun loadFavoriteIds(): Set<String> {
        val userId = client.auth.currentSessionOrNull()?.user?.id?.toString() ?: return emptySet()
        return client.from("user_article_interactions")
            .select {
                filter {
                    eq("user_id", userId)
                    eq("is_favorite", true)
                }
            }
            .decodeList<ArticleIdRow>()
            .map { it.articleId }
            .toSet()
    }

    suspend fun loadReadIds(): Set<String> {
        val userId = client.auth.currentSessionOrNull()?.user?.id?.toString() ?: return emptySet()
        return client.from("user_article_interactions")
            .select {
                filter {
                    eq("user_id", userId)
                    eq("is_read", true)
                }
            }
            .decodeList<ArticleIdRow>()
            .map { it.articleId }
            .toSet()
    }

    // MARK: - Écriture

    suspend fun upsertInteraction(
        articleId: String,
        isLiked: Boolean,
        isFavorite: Boolean,
        isRead: Boolean,
        hasClaimedXp: Boolean
    ) {
        val userId = client.auth.currentSessionOrNull()?.user?.id?.toString() ?: return
        client.from("user_article_interactions").upsert(
            UpsertRow(userId, articleId, isLiked, isFavorite, isRead, hasClaimedXp)
        ) { onConflict = "user_id,article_id" }
    }

    suspend fun upsertReadAt(
        articleId: String,
        isLiked: Boolean,
        isFavorite: Boolean,
        hasClaimedXp: Boolean
    ) {
        val userId = client.auth.currentSessionOrNull()?.user?.id?.toString() ?: return
        val now = DateTimeFormatter.ISO_INSTANT.format(Instant.now())
        client.from("user_article_interactions").upsert(
            ReadAtRow(userId, articleId, true, now, isLiked, isFavorite, hasClaimedXp)
        ) { onConflict = "user_id,article_id" }
    }

    suspend fun toggleFavorite(articleId: String, isFavorite: Boolean) {
        val userId = client.auth.currentSessionOrNull()?.user?.id?.toString() ?: return
        client.from("user_article_interactions")
            .upsert(listOf(FavUpsertRow(userId, articleId, isFavorite))) {
                onConflict = "user_id,article_id"
            }
    }
}
