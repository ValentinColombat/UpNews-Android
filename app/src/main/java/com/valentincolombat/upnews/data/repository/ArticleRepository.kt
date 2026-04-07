package com.valentincolombat.upnews.data.repository

import com.valentincolombat.upnews.data.model.Article
import com.valentincolombat.upnews.data.remote.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import java.time.LocalDate

class ArticleRepository private constructor() {

    companion object {
        val shared = ArticleRepository()
    }

    private val client = SupabaseClient.client

    // MARK: - Récupère tous les articles du jour — équivalent fetchTodayArticles()
    suspend fun fetchTodayArticles(): List<Article> {
        val today = LocalDate.now().toString() // "yyyy-MM-dd"

        return client.from("articles")
            .select {
                filter {
                    eq("language", "fr")
                    eq("published_date", today)
                }
                order("created_at", Order.DESCENDING)
            }
            .decodeList<Article>()
    }

    // MARK: - Récupère un article spécifique — équivalent fetchArticle(id:)
    suspend fun fetchArticle(id: String): Article {
        return client.from("articles")
            .select {
                filter { eq("id", id) }
                limit(1)
            }
            .decodeList<Article>()
            .first()
    }

    // MARK: - Récupère les N derniers articles — équivalent fetchRecentArticles(limit:)
    suspend fun fetchRecentArticles(limit: Int = 7): List<Article> {
        return client.from("articles")
            .select {
                filter { eq("language", "fr") }
                order("published_date", Order.DESCENDING)
                limit(limit.toLong())
            }
            .decodeList<Article>()
    }

    // MARK: - Récupère tous les articles publiés jusqu'à aujourd'hui (pour la bibliothèque)
    suspend fun fetchAllArticles(): List<Article> {
        val today = LocalDate.now().toString()
        return client.from("articles")
            .select {
                filter { lte("published_date", today) }
                order("published_date", Order.DESCENDING)
            }
            .decodeList<Article>()
    }
}
