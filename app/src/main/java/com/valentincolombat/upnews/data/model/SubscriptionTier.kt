package com.valentincolombat.upnews.data.model

enum class SubscriptionTier(val value: String) {
    FREE("free"),
    PREMIUM("premium");

    companion object {
        fun from(value: String?): SubscriptionTier =
            entries.find { it.value == value?.trim()?.lowercase() } ?: FREE
    }
}
