package com.valentincolombat.upnews.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.valentincolombat.upnews.data.model.SubscriptionTier
import com.valentincolombat.upnews.data.repository.InteractionRepository
import com.valentincolombat.upnews.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {

    private val repo            = UserRepository.shared
    private val interactionRepo = InteractionRepository.shared

    // Expose UserRepository state — équivalent @EnvironmentObject UserDataService iOS
    val isDataReady         = repo.isDataReady
    val mainArticle         = repo.mainArticle
    val secondaryArticles   = repo.secondaryArticles
    val displayName         = repo.displayName
    val currentStreak       = repo.currentStreak
    val currentLevel        = repo.currentLevel
    val currentXp           = repo.currentXp
    val maxXp               = repo.maxXp
    val selectedCompanionId = repo.selectedCompanionId
    val isOGMember          = repo.isOGMember
    val isPremium           = repo.subscriptionTier
        .map { it == SubscriptionTier.PREMIUM }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), repo.isPremium)

    val xpProgress = combine(repo.currentXp, repo.maxXp) { xp, max ->
        if (max > 0) xp.toFloat() / max.toFloat() else 0f
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0f)

    // MARK: - Articles lus

    private val _readIds = MutableStateFlow<Set<String>>(emptySet())
    val readIds: StateFlow<Set<String>> = _readIds.asStateFlow()

    init { refreshReadIds() }

    fun refreshReadIds() {
        viewModelScope.launch {
            runCatching { _readIds.value = interactionRepo.loadReadIds() }
        }
    }
}
