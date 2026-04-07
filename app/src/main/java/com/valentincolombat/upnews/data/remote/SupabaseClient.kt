package com.valentincolombat.upnews.data.remote

// Proxy vers SupabaseClientProvider — le vrai client est initialisé dans UpNewsApplication
object SupabaseClient {
    val client get() = SupabaseClientProvider.client

    const val REDIRECT_URL = "upnews://login-callback"
}
